package no.nav.paw.arbeidssoekerregisteret.config

const val CONFIG_FILE_NAME = "application.yaml"

data class Config(
    val app: AppConfig,
    val naisEnv: NaisEnv = currentNaisEnv
)

data class AppConfig(
    val kafkaKeys: KafkaKeysConfig,
)

data class KafkaKeysConfig(
    val url: String,
    val scope: String
)
