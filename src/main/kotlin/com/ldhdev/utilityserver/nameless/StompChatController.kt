package com.ldhdev.utilityserver.nameless

import com.ldhdev.namelessstd.*
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

    @MessageMapping(Route.Server.Join)
    fun enter(@Header(Headers.PlayerUUID) playerUUID: String, @Header(Headers.ModVersion) version: String) {
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

    @MessageMapping(Route.Server.ViewOnline)
    fun getOnlinePlayers(@Header(Headers.ModId) id: String) {
        val session = repository.findByIdOrNull(id) ?: return
        logger.info("$session requested online players")
        template.convertAndSend("/topic/onlines/$id", repository.findByOnlineIsTrue().map { it.name })
    }

    @MessageMapping(Route.Server.SendChat)
    fun sendChatMessage(
        @Header(Headers.ModId) id: String,
        @Header(Headers.ChatId) chatId: String,
        @DestinationVariable(Variable.To) receiverName: String,
        payload: String
    ) {
        val session = repository.findByIdOrNull(id) ?: return
        val receiverSession = repository.findByNameEqualsIgnoreCase(receiverName) ?: return

        logger.info("Sending a chat message '$payload' from $session to $receiverSession")
        if (receiverSession.online && session.online) {
            template.convertAndSend(
                Route.Client.Chat.withVariables(Variable.Id to receiverSession.id).withPrefix(Prefix.Client),
                payload,
                mapOf(Headers.Sender to session.name, Headers.ChatId to chatId)
            )
        }
    }

    @MessageMapping(Route.Server.ReadChat)
    fun readChat(
        @Header(Headers.ModId) id: String,
        @Header(Headers.ChatId) chatId: String,
        @DestinationVariable("sender") sender: String
    ) {
        val session = repository.findByIdOrNull(id) ?: return
        val senderSession = repository.findByNameEqualsIgnoreCase(sender) ?: return

        if (session.online && senderSession.online) {
            template.convertAndSend(
                Route.Client.NotifyRead.withVariables(Variable.Id to senderSession.id).withPrefix(Prefix.Client),
                "",
                mapOf(Headers.ChatId to chatId, Headers.From to session.name)
            )
        }
    }

    @MessageMapping(Route.Server.Locraw)
    fun updateLocrawInfo(@Header(Headers.ModId) id: String, payload: String?) {
        val session = repository.findByIdOrNull(id) ?: return
        session.locraw = payload?.let(Json::decodeFromString)
        repository.save(session)
    }

    @MessageMapping(Route.Server.Disconnect)
    fun disconnect(@Header(Headers.ModId) id: String) {
        val session = repository.findByIdOrNull(id) ?: return

        with(session) {
            online = false
            locraw = null
        }
        repository.save(session)

        logger.info("$session disconnected")
    }
}