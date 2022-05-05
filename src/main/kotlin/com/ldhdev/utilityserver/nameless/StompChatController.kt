package com.ldhdev.utilityserver.nameless

import com.ldhdev.utilityserver.db.ModPlayerSession
import com.ldhdev.utilityserver.db.ModSessionRepository
import com.ldhdev.utilityserver.dto.MojangProfile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class StompChatController(private val template: SimpMessagingTemplate, private val repository: ModSessionRepository) {
    private val logger = LogManager.getLogger()

    @MessageMapping("/join")
    fun enter(@Header("uuid") playerUUID: String, @Header(MOD_VERSION) version: String) {
        val profile = MojangProfile.getFromUUID(playerUUID) ?: return
        val session = repository.findByPlayerUUID(profile.id) ?: ModPlayerSession()

        with(session) {
            this.playerUUID = profile.id
            this.name = profile.name
            this.version = version
            online = true
        }

        logger.info("$session joined")

        repository.save(session)
        template.convertAndSend("/topic/$playerUUID", session.id)
    }

    @MessageMapping("/onlines")
    fun getOnlinePlayers(@Header(MOD_ID) id: String) {
        val session = repository.findByIdOrNull(id) ?: return
        logger.info("$session requested online players")
        template.convertAndSend("/topic/onlines/$id", repository.findByOnlineIsTrue().map { it.name })
    }

    @MessageMapping("/chat/{to}")
    fun sendChatMessage(@Header(MOD_ID) id: String, @DestinationVariable("to") receiverName: String, payload: String) {
        val session = repository.findByIdOrNull(id) ?: return
        val receiverSession = repository.findByNameEqualsIgnoreCase(receiverName) ?: return

        logger.info("Sending a chat message '$payload' from $session to $receiverSession(${receiverSession.online})")
        if (!receiverSession.online) {
            template.convertAndSend(
                "/topic/chat/$id",
                "Player ${receiverSession.name} is offline",
                mapOf("sender" to "Server")
            )
        } else {
            template.convertAndSend("/topic/chat/${receiverSession.id}", payload, mapOf("sender" to session.name))
        }
    }

    @MessageMapping("/locraw")
    fun updateLocrawInfo(@Header(MOD_ID) id: String, payload: String?) {
        val session = repository.findByIdOrNull(id) ?: return
        session.locraw = payload?.let(Json::decodeFromString)
        repository.save(session)
    }

    @MessageMapping("/disconnect")
    fun disconnect(@Header(MOD_ID) id: String) {
        val session = repository.findByIdOrNull(id) ?: return

        with(session) {
            online = false
            locraw = null
        }
        repository.save(session)

        logger.info("$session disconnected")
    }

    companion object {
        private const val MOD_ID = "mod-uuid"
        private const val MOD_VERSION = "mod-version"
    }
}