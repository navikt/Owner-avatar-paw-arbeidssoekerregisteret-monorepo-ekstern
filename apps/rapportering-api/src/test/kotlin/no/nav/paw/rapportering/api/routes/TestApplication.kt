package no.nav.paw.rapportering.api.routes

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import no.nav.paw.kafkakeygenerator.client.KafkaKeysClient
import no.nav.paw.rapportering.api.config.AuthProviders
import no.nav.paw.rapportering.api.kafka.RapporteringProducer
import no.nav.paw.rapportering.api.kafka.RapporteringTilgjengeligState
import no.nav.paw.rapportering.api.plugins.configureAuthentication
import no.nav.paw.rapportering.api.plugins.configureSerialization
import no.nav.paw.rapportering.api.services.AutorisasjonService
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore

fun sharedTestApplication(
    kafkaKeyClient: KafkaKeysClient,
    rapporteringStateStore: ReadOnlyKeyValueStore<Long, RapporteringTilgjengeligState>,
    kafkaStreams: KafkaStreams,
    httpClient: HttpClient,
    rapporteringProducer: RapporteringProducer,
    autorisasjonService: AutorisasjonService,
    authProviders: AuthProviders,
    testBlock: suspend (testClient: HttpClient) -> Unit
) = testApplication {
    val testClient = createClient {
        install(ContentNegotiation) {
            jackson {
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                registerModule(JavaTimeModule())
                registerKotlinModule()
            }
        }
    }
    application {
        configureAuthentication(authProviders)
        configureSerialization()
        routing {
            rapporteringRoutes(
                kafkaKeyClient,
                "stateStore",
                rapporteringStateStore,
                kafkaStreams,
                httpClient,
                rapporteringProducer,
                autorisasjonService,
            )
        }
    }
    testBlock(testClient)
}

