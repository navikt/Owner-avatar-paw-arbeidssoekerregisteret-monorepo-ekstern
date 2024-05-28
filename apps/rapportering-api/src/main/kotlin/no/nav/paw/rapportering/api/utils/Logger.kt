package no.nav.paw.rapportering.api.utils

import no.nav.paw.rapportering.api.services.NavAnsatt
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.cef.CefMessageEvent
import no.nav.common.audit_log.cef.CefMessageSeverity

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline val <reified T> T.logger: Logger get() = LoggerFactory.getLogger(T::class.java)

inline val auditLogger get() = LoggerFactory.getLogger("AuditLogger")

fun auditLogMelding(
    identitetsnummer: String,
    navAnsatt: NavAnsatt,
    melding: String
): String =
    CefMessage.builder()
        .applicationName("paw-rapportering-api") // TODO: fra config
        .event(CefMessageEvent.ACCESS)
        .name("Sporingslogg")
        .severity(CefMessageSeverity.INFO)
        .sourceUserId(navAnsatt.navIdent)
        .destinationUserId(identitetsnummer)
        .timeEnded(System.currentTimeMillis())
        .extension("msg", melding)
        .build()
        .toString()