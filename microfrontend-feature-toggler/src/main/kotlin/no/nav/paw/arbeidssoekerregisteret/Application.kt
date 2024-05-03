package no.nav.paw.arbeidssoekerregisteret

import io.ktor.server.application.Application
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.paw.arbeidssokerregisteret.ApplicationInfo
import no.nav.paw.arbeidssokerregisteret.plugins.configureMetrics
import no.nav.paw.arbeidssokerregisteret.routes.healthRoutes
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("app")
    logger.info("Starter ${ApplicationInfo.id}")

    val server = embeddedServer(
        factory = Netty,
        port = 8080,
        configure = {
            connectionGroupSize = 8
            workerGroupSize = 8
            callGroupSize = 16
        }
    ) {
        module(
            registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        )
    }
    server.addShutdownHook {
        server.stop(300, 300)
    }
    server.start(wait = true)
}

fun Application.module(registry: PrometheusMeterRegistry) {
    configureMetrics(registry)
    healthRoutes(registry)
}