package no.nav.paw.arbeidssoekerregisteret.topology.streams

import no.nav.paw.arbeidssoekerregisteret.config.buildToggleSerde
import no.nav.paw.arbeidssoekerregisteret.context.ConfigContext
import no.nav.paw.arbeidssoekerregisteret.context.LoggingContext
import no.nav.paw.arbeidssoekerregisteret.model.PeriodeInfo
import no.nav.paw.arbeidssoekerregisteret.model.Toggle
import no.nav.paw.config.kafka.streams.genericProcess
import no.nav.paw.kafkakeygenerator.client.KafkaKeysResponse
import no.nav.paw.rapportering.internehendelser.EksternGracePeriodeUtloept
import no.nav.paw.rapportering.internehendelser.LeveringsfristUtloept
import no.nav.paw.rapportering.internehendelser.PeriodeAvsluttet
import no.nav.paw.rapportering.internehendelser.RapporteringTilgjengelig
import no.nav.paw.rapportering.internehendelser.RapporteringsHendelse
import no.nav.paw.rapportering.internehendelser.RapporteringsMeldingMottatt
import no.nav.paw.rapportering.internehendelser.RegisterGracePeriodeUtloept
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.KeyValueStore

/**
 * TODO Venter med å implementere til etter registeret er i prod
 */
context(ConfigContext, LoggingContext)
fun StreamsBuilder.buildRapporteringKStream(hentKafkaKeys: (ident: String) -> KafkaKeysResponse?) {
    val kafkaStreamsConfig = appConfig.kafkaStreams

    this.stream<Long, RapporteringsHendelse>(kafkaStreamsConfig.periodeTopic)
        .genericProcess<Long, RapporteringsHendelse, Long, Toggle>(
            name = "handtereToggleForRapportering",
            stateStoreNames = arrayOf(kafkaStreamsConfig.periodeStoreName)
        ) { record ->
            val keyValueStore: KeyValueStore<Long, PeriodeInfo> = getStateStore(kafkaStreamsConfig.periodeStoreName)
            when (val event = record.value()) {
                is RapporteringTilgjengelig -> processEvent(event)
                is RapporteringsMeldingMottatt -> processEvent(event)
                is PeriodeAvsluttet -> processEvent(event)
                is LeveringsfristUtloept -> processEvent(event)
                is RegisterGracePeriodeUtloept -> processEvent(event)
                is EksternGracePeriodeUtloept -> processEvent(event)
            }
        }
        .to(kafkaStreamsConfig.microfrontendTopic, Produced.with(Serdes.Long(), buildToggleSerde()))
}

private fun processEvent(event: RapporteringTilgjengelig) {

}

private fun processEvent(event: RapporteringsMeldingMottatt) {

}

private fun processEvent(event: PeriodeAvsluttet) {

}

private fun processEvent(event: LeveringsfristUtloept) {

}

private fun processEvent(event: RegisterGracePeriodeUtloept) {

}

private fun processEvent(event: EksternGracePeriodeUtloept) {

}
