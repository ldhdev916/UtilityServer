package com.ldhdev.utilityserver.nameless

import com.ldhdev.utilityserver.db.ModSessionRepository
import com.ldhdev.utilityserver.dto.LocrawInfo
import org.apache.logging.log4j.LogManager
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Controller
class NamelessStompController(
    private val template: SimpMessagingTemplate,
    private val repository: ModSessionRepository
) {

    private val logger = LogManager.getLogger()

    @EventListener
    fun onConnect(e: SessionConnectEvent) {
        val session = repository.findByIdOrNull((e.user as NamelessUser).name) ?: return
        logger.info("$session joined")
    }

    @EventListener
    fun onDisconnect(e: SessionDisconnectEvent) {
        val session = repository.findByIdOrNull((e.user as NamelessUser).name) ?: return
        with(session) {
            online = false
            locraw = null
        }
        repository.save(session)
        logger.info("$session disconnected")
    }

    @MessageMapping("/onlines")
    @SendToUser("/topic/onlines")
    fun getOnlinePlayers() = repository.findByOnlineIsTrue().map { it.name }

    @MessageMapping("/locraw")
    fun updateLocrawInfo(user: NamelessUser, payload: LocrawInfo?) {
        val session = repository.findByIdOrNull(user.name) ?: return
        session.locraw = payload
        repository.save(session)
    }

    @MessageMapping("/chats/send/{to}")
    fun sendChat(
        user: NamelessUser,
        @Header("chat-id") chatId: String,
        @DestinationVariable("to") receiver: String,
        payload: String
    ) {
        val session = repository.findByIdOrNull(user.name) ?: return
        val receiverSession = repository.findByNameEqualsIgnoreCaseAndOnlineIsTrue(receiver) ?: return
        template.convertAndSendToUser(
            receiverSession.id,
            "/topic/chats/send",
            payload,
            mapOf("sender" to session.name, "chat-id" to chatId)
        )
    }

    @MessageMapping("/chats/read/{sender}")
    fun readChat(
        user: NamelessUser,
        @Header("chat-id") chatId: String,
        @DestinationVariable("sender") sender: String
    ) {
        val session = repository.findByIdOrNull(user.name) ?: return
        val senderSession = repository.findByNameEqualsIgnoreCaseAndOnlineIsTrue(sender) ?: return
        template.convertAndSendToUser(
            senderSession.id,
            "/topic/chats/read",
            "",
            mapOf("chat-id" to chatId, "reader" to session.name)
        )
    }
}