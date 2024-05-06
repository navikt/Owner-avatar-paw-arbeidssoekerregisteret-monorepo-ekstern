package no.nav.paw.arbeidssoekerregisteret.routes

import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Route.healthRoutes(prometheusMeterRegistry: PrometheusMeterRegistry) {
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
