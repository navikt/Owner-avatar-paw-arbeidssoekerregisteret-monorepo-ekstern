# AIA Microfrontend Toggler

Denne applikasjonen skal lytte på hendelser i arbeidssøkerdomenet, spesielt start/stopp av perioder. Henselsene skal
tolkes for å bestemme hvilke microfrontends som skal være aktivert eller deaktivert på "Min Side" for
respektive brukere. Dette kontrolleres ved å sende en ny hendelse på `aapen-microfrontend-v1` topic'en
i Kafka. Microfrontenden som skal toggles har ID `aia-min-side`.

* [Repo for topic](https://github.com/navikt/min-side-microfrontend-topic-iac)
* [Beskrivelse av toggle-funksjonalitet](https://navikt.github.io/tms-dokumentasjon/microfrontend/#toggle-pa-microfrontend)
