package no.nav.paw.arbeidssokerregisteret.routes

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.healthRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {
    routing {
        get("/internal/isAlive") {
            call.respondText("ALIVE", ContentType.Text.Plain)
        }
        get("/internal/isReady") {
            call.respondText("READY", ContentType.Text.Plain)
        }
        get("/internal/metrics") {
            call.respond(prometheusMeterRegistry.scrape())
        }
    }
}
