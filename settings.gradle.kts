plugins {
    kotlin("jvm") version "2.0.0" apply false
    id("io.ktor.plugin") version "2.3.10" apply false
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0" apply false
    id("com.google.cloud.tools.jib") version "3.4.1" apply false
    id("org.openapi.generator") version "7.4.0" apply false
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1" apply false
}

rootProject.name = "paw-arbeidssoekerregisteret-monorepo-ekstern"

include(
    "lib:app-config",
    "lib:kafka",
    "lib:kafka-streams",
    "lib:kafka-key-generator-client",
    "domain:main-avro-schema",
    "apps:rapportering-api",
    "apps:rapporterings-tjeneste",
    "apps:microfrontend-toggler",
    "domain:rapportering-interne-hendelser",
    "domain:rapporteringsansvar-schema",
    "domain:rapporteringsmelding-schema",
)

dependencyResolutionManagement {
    val githubPassword: String by settings
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
        maven {
            setUrl("https://maven.pkg.github.com/navikt/poao-tilgang")
            credentials {
                username = "x-access-token"
                password = githubPassword
            }
        }
        maven {
            setUrl("https://maven.pkg.github.com/navikt/tms-varsel-authority")
            credentials {
                username = "x-access-token"
                password = githubPassword
            }
        }
        maven {
            setUrl("https://maven.pkg.github.com/navikt/paw-arbeidssokerregisteret-api")
            credentials {
                username = "x-access-token"
                password = githubPassword
            }
        }
    }
    versionCatalogs {
        // PAW greier
        val pawPdlClientVersion = "24.03.20.30-1"
        val pawAaregClientVersion = "24.01.12.16-1"
        val arbeidssokerregisteretVersion = "1.8062260419.22-1"

        // TMS
        val tmsVarselVersion = "1.0.4"

        // NAV
        val noNavCommonVersion = "3.2024.02.21_11.18-8f9b43befae1"
        val noNavSecurityVersion = "3.1.5"

        // Konfigurasjon
        val comSksamuelHopliteVersion = "2.8.0.RC3"

        // Kafkawhic
        val orgApacheKafkaVersion = "3.6.0"
        val ioConfluentKafkaVersion = "7.6.0"

        // Serialisering
        val orgApacheAvroVersion = "1.11.3"
        val comFasterxmlJacksonVersion = "2.17.1"

        // ---- //
        val kotlinExposedVersion = "0.50.0"
        val logstashVersion = "7.3"
        val logbackVersion = "1.4.14"
        val kotestVersion = "5.8.1"
        val mockkVersion = "1.13.10"
        val testContainersVersion = "1.19.6"
        val mockOauth2ServerVersion = "2.0.0"
        val micrometerVersion = "1.12.3"
        val otelTargetSdkVersion = "1.36.0"
        val otelInstrumentationVersion = "2.1.0"
        val coroutinesVersion = "1.8.0"
        val rapporteringsSchemaVersion = "24.05.15.2-1"

        fun VersionCatalogBuilder.ktorLib(
            alias: String,
            artifactId: String,
        ) = library(alias, "io.ktor", artifactId).version("2.3.10")

        fun VersionCatalogBuilder.ktorLibs(vararg aliases: Pair<String, String>) =
            aliases.forEach { (artifactId, alias) -> ktorLib(alias, artifactId) }

        infix fun String.alias(alias: String) = this to alias
        create("kotlinx") {
            library("coroutinesCore", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version(coroutinesVersion)
        }
        create("loggingLibs") {
            library("logbackClassic", "ch.qos.logback", "logback-classic").version(logbackVersion)
            library("logstashLogbackEncoder", "net.logstash.logback", "logstash-logback-encoder").version(logstashVersion)
        }
        create("ktorClient") {
            ktorLibs(
                "ktor-client-content-negotiation" alias "contentNegotiation",
                "ktor-client-core" alias "core",
                "ktor-client-cio" alias "cio",
            )
        }
        create("ktorServer") {
            bundle(
                "withNettyAndMicrometer",
                listOf(
                    "core",
                    "coreJvm",
                    "netty",
                    "micrometer",
                ),
            )
            ktorLibs(
                "ktor-server-cors" alias "cors",
                "ktor-server-swagger" alias "swagger",
                "ktor-server-call-id" alias "callId",
                "ktor-server-status-pages" alias "statusPages",
                "ktor-server-content-negotiation" alias "contentNegotiation",
                "ktor-server-core-jvm" alias "coreJvm",
                "ktor-server-core" alias "core",
                "ktor-server-openapi" alias "openapi",
                "ktor-server-netty" alias "netty",
                "ktor-server-auth" alias "auth",
                "ktor-server-metrics-micrometer" alias "micrometer",
                "ktor-server-tests-jvm" alias "testJvm",
            )
        }
        create("ktor") {
            ktorLibs(
                "ktor-serialization" alias "serialization",
                "ktor-serialization-jvm" alias "serializationJvm",
                "ktor-serialization-jackson" alias "serializationJackson",
                "ktor-serialization-kotlinx-json" alias "serializationJson",
            )
        }
        create("otel") {
            library("api", "io.opentelemetry", "opentelemetry-api").version(otelTargetSdkVersion)
            library("ktor", "io.opentelemetry.instrumentation", "opentelemetry-ktor-2.0").version(otelInstrumentationVersion)
            library(
                "annotations",
                "io.opentelemetry.instrumentation",
                "opentelemetry-instrumentation-annotations",
            ).version(otelInstrumentationVersion)
        }
        create("micrometer") {
            library("core", "io.micrometer", "micrometer-core").version(micrometerVersion)
            library("registryPrometheus", "io.micrometer", "micrometer-registry-prometheus").version(micrometerVersion)
        }
        create("pawClients") {
            library("pawPdlClient", "no.nav.paw", "pdl-client").version(pawPdlClientVersion)
            library("pawAaregClient", "no.nav.paw", "aareg-client").version(pawAaregClientVersion)
        }
        create("arbeidssoekerRegisteret") {
            library(
                "apiKotlin",
                "no.nav.paw.arbeidssokerregisteret.api.schema",
                "arbeidssoekerregisteret-kotlin",
            ).version(arbeidssokerregisteretVersion)
            library("mainAvroSchema", "no.nav.paw.arbeidssokerregisteret.api", "main-avro-schema").version(arbeidssokerregisteretVersion)
        }
        create("orgApacheKafka") {
            library("kafkaClients", "org.apache.kafka", "kafka-clients").version(orgApacheKafkaVersion)
            library("kafkaStreams", "org.apache.kafka", "kafka-streams").version(orgApacheKafkaVersion)
            library("streamsTest", "org.apache.kafka", "kafka-streams-test-utils").version(orgApacheKafkaVersion)
        }
        create("apacheAvro") {
            library("avro", "org.apache.avro", "avro").version(orgApacheAvroVersion)
            library("kafkaSerializer", "io.confluent", "kafka-avro-serializer").version(ioConfluentKafkaVersion)
            library("kafkaStreamsAvroSerde", "io.confluent", "kafka-streams-avro-serde").version(ioConfluentKafkaVersion)
        }
        create("jackson") {
            library("datatypeJsr310", "com.fasterxml.jackson.datatype", "jackson-datatype-jsr310").version(comFasterxmlJacksonVersion)
            library("kotlin", "com.fasterxml.jackson.module", "jackson-module-kotlin").version(comFasterxmlJacksonVersion)
        }
        create("navCommon") {
            library("tokenClient", "no.nav.common", "token-client").version(noNavCommonVersion)
            library("log", "no.nav.common", "log").version(noNavCommonVersion)
            library("auditLog", "no.nav.common", "audit-log").version(noNavCommonVersion)
        }
        create("navSecurity") {
            library("tokenValidationKtorV2", "no.nav.security", "token-validation-ktor-v2").version(noNavSecurityVersion)
            library("tokenClient", "no.nav.security", "token-client-core").version(noNavSecurityVersion)
        }
        create("tmsVarsel") {
            library("kotlinBuilder", "no.nav.tms.varsel", "kotlin-builder").version(tmsVarselVersion)
        }
        create("hoplite") {
            library("hopliteCore", "com.sksamuel.hoplite", "hoplite-core").version(comSksamuelHopliteVersion)
            library("hopliteToml", "com.sksamuel.hoplite", "hoplite-toml").version(comSksamuelHopliteVersion)
            library("hopliteYaml", "com.sksamuel.hoplite", "hoplite-yaml").version(comSksamuelHopliteVersion)
        }
        create("exposed") {
            library("core", "org.jetbrains.exposed", "exposed-core").version(kotlinExposedVersion)
            library("crypt", "org.jetbrains.exposed", "exposed-crypt").version(kotlinExposedVersion)
            library("dao", "org.jetbrains.exposed", "exposed-dao").version(kotlinExposedVersion)
            library("jdbc", "org.jetbrains.exposed", "exposed-jdbc").version(kotlinExposedVersion)
            library("javaTime", "org.jetbrains.exposed", "exposed-java-time").version(kotlinExposedVersion)
        }
        create("testLibs") {
            bundle(
                "withUnitTesting",
                listOf(
                    "runnerJunit5",
                    "assertionsCore",
                    "mockk",
                ),
            )
            library("runnerJunit5", "io.kotest", "kotest-runner-junit5").version(kotestVersion)
            library("assertionsCore", "io.kotest", "kotest-assertions-core").version(kotestVersion)
            library("mockk", "io.mockk", "mockk").version(mockkVersion)
            library("testContainers", "org.testcontainers", "testcontainers").version(testContainersVersion)
            library("postgresql", "org.testcontainers", "postgresql").version(testContainersVersion)
            library("mockOauth2Server", "no.nav.security", "mock-oauth2-server").version(mockOauth2ServerVersion)
            library("ktorServerTests", "io.ktor", "ktor-server-tests").version("2.3.11")
            library("kotlinTest", "org.jetbrains.kotlin", "kotlin-test").version("1.6.21")
            library("kotestFrameworkEngine", "io.kotest", "kotest-framework-engine").version(kotestVersion)
            library("kafkaStreamsTestUtils", "org.apache.kafka", "kafka-streams-test-utils").version(orgApacheKafkaVersion)
            library("kafkaClients", "org.apache.kafka", "kafka-clients").version(orgApacheKafkaVersion)
        }
        create("poao") {
            library("tilgangClient", "no.nav.poao-tilgang", "client").version("2024.04.29_13.59-a0ddddd36ac9")
        }
        create("rapportering") {
            library(
                "rapporteringsansvarSchema",
                "no.nav.paw.arbeidssokerregisteret.api",
                "rapporteringsansvar-schema",
            ).version(rapporteringsSchemaVersion)
            library(
                "rapporteringsmeldingSchema",
                "no.nav.paw.arbeidssokerregisteret.api",
                "rapporteringsmelding-schema",
            ).version(rapporteringsSchemaVersion)
        }
    }
}
