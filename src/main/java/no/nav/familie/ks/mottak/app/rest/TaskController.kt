package no.nav.familie.ks.mottak.app.rest

import no.nav.familie.ks.kontrakter.sak.Ressurs
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.sikkerhet.OIDCUtil
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class TaskController(
        private val restTaskService: RestTaskService,
        private val oidcUtil: OIDCUtil) {

    @GetMapping(path = ["/task"])
    fun task(@RequestHeader status: Status): ResponseEntity<Ressurs> {
        val saksbehandlerId = oidcUtil.navIdent

        return ResponseEntity.ok(restTaskService.hentTasks(status, saksbehandlerId))
    }

    @PutMapping(path = ["/task/rekjor"])
    fun rekjørTask(@RequestParam taskId: Long): ResponseEntity<Ressurs> {
        val saksbehandlerId = oidcUtil.navIdent

        return ResponseEntity.ok(restTaskService.rekjørTask(taskId, saksbehandlerId))
    }

    @PutMapping(path = ["task/rekjørAlle"])
    fun rekjørTasks(@RequestHeader status: Status): ResponseEntity<Ressurs> {
        val saksbehandlerId = oidcUtil.navIdent

        return ResponseEntity.ok(restTaskService.rekjørTasks(status, saksbehandlerId))
    }

    @PutMapping(path = ["/task/avvikshaandter"])
    fun avvikshåndterTask(@RequestParam taskId: Long, @RequestBody avvikshåndterDTO: AvvikshåndterDTO): ResponseEntity<Ressurs> {
        val saksbehandlerId = oidcUtil.navIdent

        return ResponseEntity.ok(restTaskService.avvikshåndterTask(taskId, avvikshåndterDTO.avvikstype, avvikshåndterDTO.årsak, saksbehandlerId))
    }
}
