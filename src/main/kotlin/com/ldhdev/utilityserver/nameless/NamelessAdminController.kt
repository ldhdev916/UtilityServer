package com.ldhdev.utilityserver.nameless

import com.ldhdev.namelessstd.*
import com.ldhdev.utilityserver.db.ModPlayerSession
import com.ldhdev.utilityserver.db.ModSessionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/nameless/admin")
class NamelessAdminController(
    private val repository: ModSessionRepository,
    private val template: SimpMessagingTemplate
) {

    private val positionDeferred = mutableMapOf<String, MutableList<CompletableDeferred<String>>>()

    @GetMapping("/sessions")
    @ResponseBody
    fun showAllSessions(): List<ModPlayerSession> = repository.findAll()

    @GetMapping("/position/{name}")
    @ResponseBody
    fun getPlayerPosition(@PathVariable name: String): ResponseEntity<String> {
        val session = repository.findByNameEqualsIgnoreCase(name) ?: return ResponseEntity.badRequest()
            .body("Cannot find session by name $name")

        if (!session.online) {
            return ResponseEntity.unprocessableEntity().body("$session is not online")
        }
        val deferred = CompletableDeferred<String>()
        positionDeferred.getOrPut(session.name) { mutableListOf() }.add(deferred)
        template.convertAndSend(Route.Client.Position.withVariables(Variable.Id to session.id).client, "")

        return runBlocking {
            runCatching {
                withTimeout(5000) {
                    ResponseEntity.ok(deferred.await())
                }
            }
        }.getOrElse {
            ResponseEntity.internalServerError().body("Timed out $session")
        }
    }

    @MessageMapping(Route.Server.Position)
    fun getPosition(@Header(Headers.ModId) id: String, payload: String) {
        val session = repository.findByIdOrNull(id) ?: return
        val iterator = positionDeferred[session.name]?.listIterator() ?: return

        for (deferred in iterator) {
            deferred.complete(payload)
            iterator.remove()
        }
    }
}