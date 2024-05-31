package no.nav.paw.rapportering.api.domain.response

import no.nav.paw.rapportering.internehendelser.RapporteringTilgjengelig
import java.time.Instant
import java.util.UUID

data class TilgjengeligRapportering(
    val periodeId: UUID,
    val rapporteringsId: UUID,
    val gjelderFra: Instant,
    val gjelderTil: Instant,
)

typealias TilgjengeligRapporteringerResponse = List<TilgjengeligRapportering>

fun List<RapporteringTilgjengelig>.toResponse(): TilgjengeligRapporteringerResponse =
    this.map {
        TilgjengeligRapportering(
            periodeId = it.periodeId,
            rapporteringsId = it.rapporteringsId,
            gjelderFra = it.gjelderFra,
            gjelderTil = it.gjelderTil,
        )
    }
