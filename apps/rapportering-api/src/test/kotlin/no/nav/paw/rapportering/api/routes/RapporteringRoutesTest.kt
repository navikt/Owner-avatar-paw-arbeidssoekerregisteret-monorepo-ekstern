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
import no.nav.paw.rapportering.api.config.AuthProviders
import no.nav.paw.rapportering.api.domain.request.RapporteringRequest
import no.nav.paw.rapportering.api.domain.request.TilgjengeligeRapporteringerRequest
import no.nav.paw.rapportering.api.domain.response.TilgjengeligRapportering
import no.nav.paw.rapportering.api.domain.response.TilgjengeligRapporteringerResponse
import no.nav.paw.rapportering.api.domain.response.toResponse
import no.nav.paw.rapportering.api.kafka.RapporteringProducer
import no.nav.paw.rapportering.api.kafka.RapporteringTilgjengeligState
import no.nav.paw.rapportering.api.services.AutorisasjonService
import no.nav.paw.rapportering.internehendelser.RapporteringTilgjengelig
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyQueryMetadata
import org.apache.kafka.streams.state.HostInfo
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
    var authProviders: AuthProviders = emptyList()

    val oauth = MockOAuth2Server()

    beforeSpec {
        oauth.start()
        val discoveryUrl = oauth.wellKnownUrl("default").toString()
        authProviders =
            applicationConfig.authProviders.map { it.copy(discoveryUrl = discoveryUrl, clientId = "default") }
    }

    afterSpec { oauth.shutdown() }

    "Post /api/v1/tilgjengelige-rapporteringer" - {
        "should return OK status and empty list when stateStore is null and queryMetadataForKey is unavailable" {
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
        "should return OK status and list of rapporteringer when stateStore is not null" {
            sharedTestApplication(
                kafkaKeyClient,
                rapporteringStateStore,
                kafkaStreams,
                httpClient,
                rapporteringProducer,
                autorisasjonService,
                authProviders
            ) { testClient ->
                val rapporteringState = RapporteringTilgjengeligState(
                    rapporteringer = listOf(
                        RapporteringTilgjengelig(
                            hendelseId = UUID.randomUUID(),
                            periodeId = UUID.randomUUID(),
                            identitetsnummer = "12345678901",
                            arbeidssoekerId = 1L,
                            rapporteringsId = UUID.randomUUID(),
                            gjelderFra = Instant.now(),
                            gjelderTil = Instant.now()
                        )
                    )
                )
                coEvery { kafkaKeyClient.getIdAndKey(any()) } returns KafkaKeysResponse(1L, 1234L)
                coEvery { autorisasjonService.verifiserTilgangTilBruker(any(), any(), any()) } returns true
                every { rapporteringStateStore.get(any()) } returns rapporteringState
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
                response.body<List<TilgjengeligRapportering>?>() shouldBe rapporteringState.rapporteringer.toResponse()
            }
        }
        "should return OK status and list of rapporteringer when stateStore is null and queryMetadataForKey exists" {
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
                every {
                    kafkaStreams.queryMetadataForKey(
                        any(),
                        any(),
                        any<Serializer<Long>>()
                    )
                } returns KeyQueryMetadata(
                    HostInfo("test", 8080),
                    null,
                    1
                )
                val rapporteringState = RapporteringTilgjengeligState(
                    rapporteringer = listOf(
                        RapporteringTilgjengelig(
                            hendelseId = UUID.randomUUID(),
                            periodeId = UUID.randomUUID(),
                            identitetsnummer = "12345678901",
                            arbeidssoekerId = 1L,
                            rapporteringsId = UUID.randomUUID(),
                            gjelderFra = Instant.now(),
                            gjelderTil = Instant.now()
                        )
                    )
                ).rapporteringer.toResponse()
                coEvery { httpClient.post(any<String>(), any()) } returns mockk {
                    every { status } returns HttpStatusCode.OK
                    coEvery { body<TilgjengeligRapporteringerResponse>() } returns rapporteringState
                }

                val token = oauth.issueToken(claims = mapOf("acr" to "idporten-loa-high", "pid" to "12345678901"))

                val postBody = TilgjengeligeRapporteringerRequest("12345678901")

                val response = testClient.post("/api/v1/tilgjengelige-rapporteringer") {
                    bearerAuth(token.serialize())
                    contentType(ContentType.Application.Json)
                    setBody(postBody)
                }
                response.status shouldBe HttpStatusCode.OK
                response.body<List<TilgjengeligRapportering>?>() shouldBe rapporteringState
            }
        }
    }

    "Post /api/v1/rapportering" - {
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