package no.nav.paw.rapportering.api.utils

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.*
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.v2.TokenValidationContextPrincipal
import java.util.*

val claimsList =
    listOf(
        "tokenx" to "pid",
        "azure" to "NavIdent",
        "azure" to "oid",
    )

data class ResolvedClaim(
    val issuer: String,
    val claim: Claim,
)

data class Claim(
    val name: String,
    val value: String,
)

typealias ResolvedClaims = List<ResolvedClaim>

fun TokenValidationContext?.getResolvedClaims(): ResolvedClaims =
    claimsList.mapNotNull { (issuer, claimName) ->
        this.resolveClaim(issuer, claimName)?.let { claimValue ->
            ResolvedClaim(issuer, Claim(claimName, claimValue))
        }
    }

fun TokenValidationContext?.resolveClaim(
    issuer: String,
    claimName: String,
): String? = this?.getClaims(issuer)?.getStringClaim(claimName)

fun ApplicationCall.getClaims() =
    this.authentication.principal<TokenValidationContextPrincipal>()
        ?.context
        ?.getResolvedClaims()

val ResolvedClaims.isTokenx get(): Boolean = any { it.issuer == "tokenx" }
val ResolvedClaims.isAzure get(): Boolean = any { it.issuer == "azure" }

fun ResolvedClaims.getPid(): String =
    find { it.issuer == "tokenx" && it.claim.name == "pid" }
        ?.claim?.value
        ?: throw IllegalArgumentException("Fant ikke Pid i token")

fun ResolvedClaims.getOid(): UUID =
    find { it.issuer == "azure" && it.claim.name == "oid" }
        ?.claim?.value
        ?.let(UUID::fromString)
        ?: throw IllegalArgumentException("Fant ikke oid i token")

fun ResolvedClaims.getNAVident(): String =
    find { it.issuer == "azure" && it.claim.name == "NAVident" }
        ?.claim?.value
        ?: throw IllegalArgumentException("Fant ikke NAVident i token")
