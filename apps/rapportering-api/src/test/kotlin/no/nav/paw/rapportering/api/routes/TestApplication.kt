package no.nav.paw.rapportering.api.routes

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import no.nav.paw.rapportering.api.plugins.configureAuthentication
import no.nav.paw.rapportering.api.plugins.configureSerialization

fun sharedTestApplication(testBlock: suspend ApplicationTestBuilder) {
    testApplication {
        createClient {
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
        testBlock()
    }
}

