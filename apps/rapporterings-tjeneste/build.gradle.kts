import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.google.cloud.tools.jib")
}

dependencies {
    implementation(project(":lib:hoplite-config"))
    implementation(project(":lib:kafka-streams"))
    implementation(project(":lib:kafka-key-generator-client"))
    implementation(project(":domain:main-avro-schema"))
    implementation(project(":domain:rapportering-interne-hendelser"))
    implementation(project(":domain:rapporteringsansvar-schema"))
    implementation(project(":domain:rapporteringsmelding-schema"))
    implementation(orgApacheKafka.kafkaStreams)
    implementation(jackson.datatypeJsr310)
    implementation(jackson.kotlin)
    implementation(apacheAvro.kafkaStreamsAvroSerde)
}

//enable context receiver
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}