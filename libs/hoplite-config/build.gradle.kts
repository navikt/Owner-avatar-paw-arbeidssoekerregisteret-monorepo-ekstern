plugins {
    kotlin("jvm")
}

dependencies {
    implementation(hoplite.hopliteCore)
    implementation(hoplite.hopliteToml)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
