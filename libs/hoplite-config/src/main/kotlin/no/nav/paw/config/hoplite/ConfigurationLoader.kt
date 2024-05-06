package no.nav.paw.config.hoplite

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceSource

/**
 * Laster config basert på gjeldene NAIS miljø.
 * Forutsetter at config ligger i resources under /nais eller /local
 */
inline fun <reified A> loadNaisOrLocalConfiguration(resource: String): A {
    val fulltNavn =
        when (System.getenv("NAIS_CLUSTER_NAME")) {
            "prod-gcp" -> "/nais/$resource"
            "dev-gcp" -> "/nais/$resource"
            else -> "/local/$resource"
        }
    return loadConfigFromProvidedResource(fulltNavn)
}

/**
 * Laster config fra resources
 */
@OptIn(ExperimentalHoplite::class)
inline fun <reified A> loadConfigFromProvidedResource(resource: String): A {
    return ConfigLoaderBuilder
        .default()
        .strict()
        .withExplicitSealedTypes()
        .addResourceSource(resource)
        .build()
        .loadConfigOrThrow()
}
