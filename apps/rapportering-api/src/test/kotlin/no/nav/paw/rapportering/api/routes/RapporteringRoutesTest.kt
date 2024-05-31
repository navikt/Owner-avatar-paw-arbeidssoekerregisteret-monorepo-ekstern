package no.nav.paw.rapportering.api.routes

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.paw.config.hoplite.loadNaisOrLocalConfiguration
import no.nav.paw.kafkakeygenerator.client.KafkaKeysClient
import no.nav.paw.kafkakeygenerator.client.KafkaKeysResponse
import no.nav.paw.rapportering.api.config.APPLICATION_CONFIG_FILE_NAME
import no.nav.paw.rapportering.api.config.ApplicationConfig
import no.nav.paw.rapportering.api.domain.request.RapporteringRequest
import no.nav.paw.rapportering.api.domain.request.TilgjengeligeRapporteringerRequest
import no.nav.paw.rapportering.api.domain.response.TilgjengeligRapportering
import no.nav.paw.rapportering.api.kafka.RapporteringProducer
import no.nav.paw.rapportering.api.kafka.RapporteringTilgjengeligState
import no.nav.paw.rapportering.api.services.AutorisasjonService
import no.nav.paw.rapportering.internehendelser.RapporteringTilgjengelig
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyQueryMetadata
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import java.time.Instant
import java.util.*

class RapporteringRoutesTest : FreeSpec({
    val applicationConfig = loadNaisOrLocalConfiguration<ApplicationConfig>(APPLICATION_CONFIG_FILE_NAME)

    val kafkaKeyClient: KafkaKeysClient = mockk()
    val rapporteringStateStore: ReadOnlyKeyValueStore<Long, RapporteringTilgjengeligState> = mockk()
    val kafkaStreams: KafkaStreams = mockk()
    val httpClient: HttpClient = mockk()
    val rapporteringProducer: RapporteringProducer = mockk()
    val autorisasjonService: AutorisasjonService = mockk()

    val oauth = MockOAuth2Server()

    beforeSpec { oauth.start() }

    afterSpec { oauth.shutdown() }

    "Post /api/v1/tilgjengelige-rapporteringer" - {
        val discoveryUrl = oauth.wellKnownUrl("default").toString()
        val authProviders =
            applicationConfig.authProviders.map { it.copy(discoveryUrl = discoveryUrl, clientId = "default") }

        "should return OK status and empty list of TilgjengeligRapporteringerResponse" {
            sharedTestApplication(
                kafkaKeyClient,
                rapporteringStateStore,
                kafkaStreams,
                httpClient,
                rapporteringProducer,
                autorisasjonService,
                authProviders
            ) { testClient ->
                coEvery { kafkaKeyClient.getIdAndKey(any()) } returns KafkaKeysResponse(1L, 1234L)
                coEvery { autorisasjonService.verifiserTilgangTilBruker(any(), any(), any()) } returns true
                every { rapporteringStateStore.get(any()) } returns null
                every {
                    kafkaStreams.queryMetadataForKey(
                        any(),
                        any(),
                        any<Serializer<Long>>()
                    )
                } returns KeyQueryMetadata.NOT_AVAILABLE

                val token = oauth.issueToken(claims = mapOf("acr" to "idporten-loa-high", "pid" to "12345678901"))

                val postBody = TilgjengeligeRapporteringerRequest("12345678901")

                val response = testClient.post("/api/v1/tilgjengelige-rapporteringer") {
                    bearerAuth(token.serialize())
                    contentType(ContentType.Application.Json)
                    setBody(postBody)
                }
                response.status shouldBe HttpStatusCode.OK
                response.body<List<TilgjengeligRapportering>?>() shouldBe emptyList()
            }
        }
    }

    "Post /api/v1/rapportering" - {
        val discoveryUrl = oauth.wellKnownUrl("default").toString()
        val authProviders =
            applicationConfig.authProviders.map { it.copy(discoveryUrl = discoveryUrl, clientId = "default") }

        "should return OK status" {
            sharedTestApplication(
                kafkaKeyClient,
                rapporteringStateStore,
                kafkaStreams,
                httpClient,
                rapporteringProducer,
                autorisasjonService,
                authProviders
            ) { testClient ->
                val rapporteringsId = UUID.randomUUID()

                coEvery { kafkaKeyClient.getIdAndKey(any()) } returns KafkaKeysResponse(1L, 1L)
                coEvery { autorisasjonService.verifiserTilgangTilBruker(any(), any(), any()) } returns true
                every { rapporteringStateStore.get(any()) } returns RapporteringTilgjengeligState(
                    rapporteringer = listOf(
                        RapporteringTilgjengelig(
                            periodeId = UUID.randomUUID(),
                            hendelseId = UUID.randomUUID(),
                            rapporteringsId = rapporteringsId,
                            identitetsnummer = "12345678901",
                            arbeidssoekerId = 1L,
                            gjelderFra = Instant.now(),
                            gjelderTil = Instant.now(),
                        )
                    )
                )
                coEvery { rapporteringProducer.produceMessage(any(), any()) } returns Unit

                val postToken = oauth.issueToken(claims = mapOf("acr" to "idporten-loa-high", "pid" to "12345678901"))

                val postBody = RapporteringRequest("12345678901", rapporteringsId, true, true)

                val response = testClient.post("/api/v1/rapportering") {
                    bearerAuth(postToken.serialize())
                    contentType(ContentType.Application.Json)
                    setBody(postBody)
                }
                response.status shouldBe HttpStatusCode.OK
            }
        }
    }
})