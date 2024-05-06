package no.nav.paw.arbeidssoekerregisteret.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.paw.arbeidssoekerregisteret.routes.healthRoutes

fun Application.configureRouting(prometheusMeterRegistry: PrometheusMeterRegistry) {
    routing {
        healthRoutes(prometheusMeterRegistry)
    }
}
