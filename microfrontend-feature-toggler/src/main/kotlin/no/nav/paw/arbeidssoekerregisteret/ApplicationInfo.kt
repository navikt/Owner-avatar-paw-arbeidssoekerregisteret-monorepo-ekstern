package no.nav.paw.arbeidssokerregisteret

object ApplicationInfo {
    val id = System.getenv("IMAGE_WITH_VERSION")?: "UNSPECIFIED"
}
