package no.nav.paw.rapportering.api.services

import no.nav.paw.rapportering.api.utils.auditLogMelding
import no.nav.paw.rapportering.api.utils.auditLogger
import no.nav.paw.rapportering.api.utils.logger
import no.nav.poao_tilgang.client.NavAnsattTilgangTilEksternBrukerPolicyInput
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient
import no.nav.poao_tilgang.client.TilgangType
import java.util.UUID

class AutorisasjonService(
    private val poaoTilgangHttpClient: PoaoTilgangCachedClient,
) {
    fun verifiserTilgangTilBruker(
        navAnsatt: NavAnsatt,
        identitetsnummer: String,
        tilgangType: TilgangType,
    ): Boolean {
        val harNavAnsattTilgang =
            poaoTilgangHttpClient.evaluatePolicy(
                NavAnsattTilgangTilEksternBrukerPolicyInput(
                    navAnsattAzureId = navAnsatt.azureId,
                    tilgangType = tilgangType,
                    norskIdent = identitetsnummer,
                ),
            ).getOrThrow().isPermit

        if (!harNavAnsattTilgang) {
            logger.info("NAV-ansatt har ikke $tilgangType til bruker")
        } else {
            auditLogger.info(
                auditLogMelding(
                    identitetsnummer,
                    navAnsatt,
                    tilgangType,
                    "NAV ansatt har benyttet $tilgangType tilgang til informasjon om bruker",
                ),
            )
        }
        return harNavAnsattTilgang
    }
}

data class NavAnsatt(val azureId: UUID, val navIdent: String)
