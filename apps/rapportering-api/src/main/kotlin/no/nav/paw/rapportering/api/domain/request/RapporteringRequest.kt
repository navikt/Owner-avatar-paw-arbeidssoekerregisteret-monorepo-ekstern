package no.nav.paw.rapportering.api.domain.request

import java.util.UUID

data class RapporteringRequest(
    // Identitetsnummer må sendes med hvis det er en veileder som rapporterer
    val identitetsnummer: String? = null,
    val rapporteringsId: UUID,
    val harJobbetIDennePerioden: Boolean,
    val vilFortsetteSomArbeidssoeker: Boolean,
)
