package no.nav.paw.rapportering.api.domain.request

data class TilgjengeligeRapporteringerRequest(
    val identitetsnummer: String?,
)

fun TilgjengeligeRapporteringerRequest.toJson() = """{"identitetsnummer": "$identitetsnummer"}"""
