package no.nav.paw.arbeidssoekerregisteret.config

import java.lang.System.getenv

enum class NaisEnv(val clusterName: String) {
    Local("local"),
    DevGCP("dev-gcp"),
    ProdGCP("prod-gcp")
}

val currentNaisEnv: NaisEnv
    get() =
        when (getenv("NAIS_CLUSTER_NAME")) {
            NaisEnv.DevGCP.clusterName -> NaisEnv.DevGCP
            NaisEnv.ProdGCP.clusterName -> NaisEnv.ProdGCP
            else -> NaisEnv.Local
        }
