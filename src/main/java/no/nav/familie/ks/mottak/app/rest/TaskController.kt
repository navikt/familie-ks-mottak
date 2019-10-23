package no.nav.familie.ks.mottak.app.rest

import no.nav.familie.ks.kontrakter.sak.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class TaskController(
        private val restTaskService: RestTaskService) {

    @GetMapping(path = ["/task/feilede"])
    fun task(): ResponseEntity<Ressurs> {
        return ResponseEntity.ok(restTaskService.hentFeiledeTasks())
    }

    @PutMapping(path = ["/task/rekjor"])
    fun rekjørTask(@RequestParam taskId: Long?): ResponseEntity<Ressurs> {
        return ResponseEntity.ok(restTaskService.rekjørTask(taskId))
    }
}
