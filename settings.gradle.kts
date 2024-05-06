plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
    kotlin("jvm") version "1.9.22" apply false
    id("io.ktor.plugin") version "2.3.10" apply false
    id("com.google.cloud.tools.jib") version "3.4.1" apply false
    id("org.openapi.generator") version "7.4.0" apply false
}

rootProject.name = "paw-arbeidssoekerregisteret-monorepo-ekstern"

include(":libs:hoplite-config")

include(":apps:aia-microfrontend-toggler")
project(":apps:aia-microfrontend-toggler").name = "paw-arbeidssokerregisteret-aia-microfrontend-toggler"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {

    val githubPassword: String by settings

    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/*")
            credentials {
                username = "x-access-token"
                password = githubPassword
            }
        }
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }

    versionCatalogs {
        // PAW greier
        val pawPdlClientVersion = "24.03.20.30-1"
        val pawAaregClientVersion = "24.01.12.16-1"
        val arbeidssokerregisteretVersion = "1.8062260419.22-1"

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
        val comFasterxmlJacksonVersion = "2.16.1"

        // ---- //
        val kotlinExposedVersion = "0.50.0"
        val logstashVersion = "7.3"
        val logbackVersion = "1.4.14"
        val kotestVersion = "5.8.1"
        val mockkVersion = "1.13.10"
        val testContainersVersion = "1.19.6"
        val mockOauth2ServerVersion = "2.0.0"
        val micrometerVersion = "1.12.3"

        create("loggingLibs") {
            library("logbackClassic", "ch.qos.logback", "logback-classic").version(logbackVersion)
            library("logstashLogbackEncoder", "net.logstash.logback", "logstash-logback-encoder").version(
                logstashVersion
            )
        }
        create("ktorClient") {
            library("contentNegotiation", "io.ktor", "ktor-client-content-negotiation").withoutVersion()
            library("core", "io.ktor", "ktor-client-core").withoutVersion()
            library("cio", "io.ktor", "ktor-client-cio").withoutVersion()
        }
        create("ktorServer") {
            bundle(
                "nettyInstrumented", listOf(
                    "core",
                    "coreJvm",
                    "netty",
                    "micrometer",
                    /*"openTelemetryApi",
                    "openTelemetryKtor",
                    "registryPrometheus"*/
                )
            )
            library("cors", "io.ktor", "ktor-server-cors").withoutVersion()
            library("swagger", "io.ktor", "ktor-server-swagger").withoutVersion()
            library("callId", "io.ktor", "ktor-server-call-id").withoutVersion()
            library("statusPages", "io.ktor", "ktor-server-status-pages").withoutVersion()
            library("contentNegotiation", "io.ktor", "ktor-server-content-negotiation").withoutVersion()
            library("coreJvm", "io.ktor", "ktor-server-core-jvm").withoutVersion()
            library("core", "io.ktor", "ktor-server-core").withoutVersion()
            library("openapi", "io.ktor", "ktor-server-openapi").withoutVersion()
            library("netty", "io.ktor", "ktor-server-netty").withoutVersion()
            library("auth", "io.ktor", "ktor-server-auth").withoutVersion()
            library("micrometer", "io.ktor", "ktor-server-metrics-micrometer").withoutVersion()
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
                "arbeidssoekerregisteret-kotlin"
            ).version(arbeidssokerregisteretVersion)
            library("mainAvroSchema", "no.nav.paw.arbeidssokerregisteret.api", "main-avro-schema")
                .version(arbeidssokerregisteretVersion)
        }
        create("apacheKafka") {
            library("kafkaClients", "org.apache.kafka", "kafka-clients").version(orgApacheKafkaVersion)
            library("kafkaStreams", "org.apache.kafka", "kafka-streams").version(orgApacheKafkaVersion)
            library("streamsTest", "org.apache.kafka", "kafka-streams-test-utils").version(orgApacheKafkaVersion)
        }
        create("apacheAvro") {
            library("avro", "org.apache.avro", "avro").version(orgApacheAvroVersion)
            library("kafkaSerializer", "io.confluent", "kafka-avro-serializer").version(ioConfluentKafkaVersion)
            library(
                "kafkaStreamsAvroSerde",
                "io.confluent",
                "kafka-streams-avro-serde"
            ).version(ioConfluentKafkaVersion)
        }
        create("jackson") {
            library("datatypeJsr310", "com.fasterxml.jackson.datatype", "jackson-datatype-jsr310").version(
                comFasterxmlJacksonVersion
            )
            library("kotlin", "com.fasterxml.jackson.module", "jackson-module-kotlin").version(
                comFasterxmlJacksonVersion
            )
            library("ktorSerialization", "io.ktor", "ktor-serialization-jackson").withoutVersion()
            library("serializationJvm", "io.ktor", "ktor-serialization-jackson-jvm").withoutVersion()
        }
        create("navCommon") {
            library("tokenClient", "no.nav.common", "token-client").version(noNavCommonVersion)
            library("log", "no.nav.common", "log").version(noNavCommonVersion)
        }
        create("navSecurity") {
            library(
                "tokenValidationKtorV2",
                "no.nav.security",
                "token-validation-ktor-v2"
            ).version(noNavSecurityVersion)
            library("tokenClient", "no.nav.security", "token-client-core").version(noNavSecurityVersion)
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
            library("runnerJunit5", "io.kotest", "kotest-runner-junit5").version(kotestVersion)
            library("assertionsCore", "io.kotest", "kotest-assertions-core").version(kotestVersion)
            library("mockk", "io.mockk", "mockk").version(mockkVersion)
            library("testContainers", "org.testcontainers", "testcontainers").version(testContainersVersion)
            library("postgresql", "org.testcontainers", "postgresql").version(testContainersVersion)
            library("mockOauth2Server", "no.nav.security", "mock-oauth2-server").version(mockOauth2ServerVersion)
        }
    }
}
