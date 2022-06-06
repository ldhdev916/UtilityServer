package com.ldhdev.utilityserver.nameless

import com.ldhdev.utilityserver.db.ModPlayerSession
import com.ldhdev.utilityserver.db.ModSessionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException

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
    fun getPlayerPosition(@PathVariable name: String): String {
        val session = repository.findByNameEqualsIgnoreCaseAndOnlineIsTrue(name) ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot process $name"
        )
        val deferred = CompletableDeferred<String>()
        positionDeferred.getOrPut(session.name) { mutableListOf() }.add(deferred)
        template.convertAndSendToUser(session.id, "/topic/position", "")

        return runBlocking {
            runCatching {
                withTimeout(5000) {
                    deferred.await()
                }
            }
        }.getOrElse {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Timed out $session")
        }
    }

    @MessageMapping("/position")
    fun getPosition(user: NamelessUser, payload: String) {
        val session = repository.findByIdOrNull(user.name) ?: return
        val iterator = positionDeferred[session.name]?.listIterator() ?: return

        for (deferred in iterator) {
            deferred.complete(payload)
            iterator.remove()
        }
    }
}