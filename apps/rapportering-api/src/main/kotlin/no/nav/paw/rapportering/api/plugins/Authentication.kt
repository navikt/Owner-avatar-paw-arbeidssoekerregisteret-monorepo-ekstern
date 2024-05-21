package no.nav.paw.rapportering.api.plugins

import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import no.nav.paw.rapportering.api.config.AuthProviders
import no.nav.security.token.support.v2.IssuerConfig
import no.nav.security.token.support.v2.RequiredClaims
import no.nav.security.token.support.v2.TokenSupportConfig
import no.nav.security.token.support.v2.tokenValidationSupport

fun Application.configureAuthentication(authProviders: AuthProviders) {
    val (azure, tokenx) = authProviders
    authentication {
        tokenValidationSupport(
            name = azure.name,
            requiredClaims = RequiredClaims(azure.name, azure.claims.toTypedArray()),
            config = TokenSupportConfig(
                IssuerConfig(
                    name = azure.name,
                    discoveryUrl = azure.discoveryUrl,
                    acceptedAudience = listOf(azure.clientId)
                )
            )
        )
        tokenValidationSupport(
            name = tokenx.name,
            requiredClaims = RequiredClaims(tokenx.name, tokenx.claims.toTypedArray(), true),
            config = TokenSupportConfig(
                IssuerConfig(
                    name = tokenx.name,
                    discoveryUrl = tokenx.discoveryUrl,
                    acceptedAudience = listOf(tokenx.clientId)
                )
            )
        )
    }
}
