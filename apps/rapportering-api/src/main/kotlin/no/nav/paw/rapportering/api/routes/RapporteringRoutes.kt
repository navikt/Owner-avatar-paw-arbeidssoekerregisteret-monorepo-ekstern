package no.nav.paw.rapportering.api.routes

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import no.nav.paw.kafkakeygenerator.client.KafkaKeysClient
import no.nav.paw.rapportering.api.domain.request.RapporteringRequest
import no.nav.paw.rapportering.api.domain.request.TilgjengeligeRapporteringerRequest
import no.nav.paw.rapportering.api.domain.response.TilgjengeligRapporteringerResponse
import no.nav.paw.rapportering.api.domain.response.toResponse
import no.nav.paw.rapportering.api.kafka.RapporteringProducer
import no.nav.paw.rapportering.api.kafka.RapporteringTilgjengeligState
import no.nav.paw.rapportering.api.kafka.createMelding
import no.nav.paw.rapportering.api.services.AutorisasjonService
import no.nav.paw.rapportering.api.utils.logger
import no.nav.poao_tilgang.client.TilgangType
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyQueryMetadata
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore

fun Route.rapporteringRoutes(
    kafkaKeyClient: KafkaKeysClient,
    rapporteringStateStoreName: String,
    rapporteringStateStore: ReadOnlyKeyValueStore<Long, RapporteringTilgjengeligState>,
    kafkaStreams: KafkaStreams,
    httpClient: HttpClient,
    rapporteringProducer: RapporteringProducer,
    autorisasjonService: AutorisasjonService,
) {
    route("/api/v1") {
        authenticate("tokenx", "azure") {
            post<TilgjengeligeRapporteringerRequest>("/tilgjengelige-rapporteringer") { request ->
                with(requestScope(request.identitetsnummer, autorisasjonService, kafkaKeyClient, TilgangType.LESE)) {
                    val arbeidssoekerId = this

                    rapporteringStateStore
                        .get(arbeidssoekerId)
                        ?.rapporteringer
                        ?.toResponse()
                        ?.let { rapporteringerTilgjengelig ->
                            logger.info("Fant ${rapporteringerTilgjengelig.size} tilgjengelige rapporteringer")
                            return@post call.respond(HttpStatusCode.OK, rapporteringerTilgjengelig)
                        }

                    val metadata =
                        kafkaStreams.queryMetadataForKey(
                            rapporteringStateStoreName,
                            arbeidssoekerId,
                            Serdes.Long().serializer(),
                        )

                    if (metadata == null || metadata == KeyQueryMetadata.NOT_AVAILABLE) {
                        logger.info("Fant ikke metadata for arbeidsoeker, $metadata")
                        return@post call.respond(HttpStatusCode.OK, emptyList<TilgjengeligRapporteringerResponse>())
                    } else {
                        val response =
                            httpClient.post("http://${metadata.activeHost().host()}/api/v1/tilgjengelige-rapporteringer") {
                                call.request.headers["Authorization"]?.let { bearerAuth(it) }
                                setBody(request)
                            }
                        return@post call.respond(response.status, response.body())
                    }
                }
            }
            post<RapporteringRequest>("/rapportering") { rapportering ->
                with(requestScope(rapportering.identitetsnummer, autorisasjonService, kafkaKeyClient, TilgangType.SKRIVE)) {
                    val arbeidsoekerId = this

                    rapporteringStateStore
                        .get(arbeidsoekerId)
                        ?.rapporteringer
                        ?.firstOrNull { it.rapporteringsId == rapportering.rapporteringsId }
                        ?.let {
                            logger.info("Rapportering med id ${rapportering.rapporteringsId} funnet")
                            val rapporteringsMelding = createMelding(it, rapportering)
                            rapporteringProducer.produceMessage(arbeidsoekerId, rapporteringsMelding)

                            return@post call.respond(HttpStatusCode.OK)
                        }

                    val metadata =
                        kafkaStreams.queryMetadataForKey(
                            rapporteringStateStoreName,
                            arbeidsoekerId,
                            Serdes.Long().serializer(),
                        )

                    if (metadata == null || metadata == KeyQueryMetadata.NOT_AVAILABLE) {
                        logger.info("Fant ikke metadata for arbeidsoeker, $metadata")
                        return@post call.respond(HttpStatusCode.NotFound)
                    } else {
                        val response =
                            httpClient.post("http://${metadata.activeHost().host()}/api/v1/rapportering") {
                                call.request.headers["Authorization"]?.let { bearerAuth(it) }
                                setBody(rapportering)
                            }
                        return@post call.respond(response.status)
                    }
                }
            }
        }
    }
}
