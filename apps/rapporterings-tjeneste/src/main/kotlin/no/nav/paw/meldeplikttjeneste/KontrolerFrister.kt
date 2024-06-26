package no.nav.paw.meldeplikttjeneste

import no.nav.paw.config.kafka.streams.Punctuation
import no.nav.paw.meldeplikttjeneste.tilstand.InternTilstand
import no.nav.paw.meldeplikttjeneste.tilstand.RapporteringsKonfigurasjon
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import java.time.Instant
import java.util.*


context(ApplicationConfiguration, RapporteringsKonfigurasjon)
fun kontrollerFrister(): Punctuation<Long, Action> = Punctuation(
    interval = punctuateInterval,
    type = PunctuationType.WALL_CLOCK_TIME,
    function = { now , context ->
        val keyValueStore: KeyValueStore<UUID, InternTilstand> = context.getStateStore(statStoreName)
        keyValueStore
            .all()
            .use { iterator ->
                iterator
                    .asSequence()
                    .filter { (_, tilstand) -> tilstand.ansvarlige.isEmpty() }

            }
    }
)

operator fun <K, V> KeyValue<K, V>.component1(): K = key
operator fun <K, V> KeyValue<K, V>.component2(): V = value

context(Instant, RapporteringsKonfigurasjon)
fun rapporteringSkalTilgjengeliggjoeres(tilstand: InternTilstand): Boolean {
    TODO()
}