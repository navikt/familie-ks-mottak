package no.nav.familie.ks.mottak.app.rest

import no.nav.familie.ks.kontrakter.sak.Ressurs
import no.nav.familie.sikkerhet.OIDCUtil
import no.nav.familie.prosessering.domene.Status
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class TaskController(
        private val restTaskService: RestTaskService, private val oidcUtil: OIDCUtil) {


    @Value("\${MOTTAK_ROLLE}")
    lateinit var påkrevdeRolle: String

    fun hentBrukernavn(): String {
        return oidcUtil.getClaim("preferred_username")
    }

    @GetMapping(path = ["/task"])
    fun task(@RequestHeader status: Status): ResponseEntity<Ressurs> {
        when (oidcUtil.groups.contains(påkrevdeRolle)) {
            true -> return ResponseEntity.ok(restTaskService.hentTasks(status, hentBrukernavn()))
            false -> return ResponseEntity.ok(Ressurs.ikkeTilgang("Du har ikke tilgang til denne appen!"))
        }
    }

    @PutMapping(path = ["/task/rekjor"])
    fun rekjørTask(@RequestParam taskId: Long): ResponseEntity<Ressurs> {
        when (oidcUtil.groups.contains(påkrevdeRolle)) {
            true -> return ResponseEntity.ok(restTaskService.rekjørTask(taskId, hentBrukernavn()))
            false -> return ResponseEntity.ok(Ressurs.ikkeTilgang("Du har ikke tilgang til denne appen!"))
        }
    }

    @PutMapping(path = ["task/rekjørAlle"])
    fun rekjørTasks(@RequestHeader status: Status): ResponseEntity<Ressurs> {
        when (oidcUtil.groups.contains(påkrevdeRolle)) {
            true -> return ResponseEntity.ok(restTaskService.rekjørTasks(status, hentBrukernavn()))
            false -> return ResponseEntity.ok(Ressurs.ikkeTilgang("Du har ikke tilgang til denne appen!"))
        }
    }

    @PutMapping(path = ["/task/avvikshaandter"])
    fun avvikshåndterTask(@RequestParam taskId: Long, @RequestBody avvikshåndterDTO: AvvikshåndterDTO): ResponseEntity<Ressurs> {
        when (oidcUtil.groups.contains(påkrevdeRolle)) {
            true -> return ResponseEntity.ok(restTaskService.avvikshåndterTask(taskId, avvikshåndterDTO.avvikstype, avvikshåndterDTO.årsak, hentBrukernavn()))
            false -> return ResponseEntity.ok(Ressurs.ikkeTilgang("Du har ikke tilgang til denne appen!"))
        }

    }
}
