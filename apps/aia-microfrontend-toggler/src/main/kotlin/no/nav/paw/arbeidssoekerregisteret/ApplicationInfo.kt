package no.nav.paw.arbeidssoekerregisteret

object ApplicationInfo {
    val id = System.getenv("IMAGE_WITH_VERSION")?: "UNSPECIFIED"
}
