package com.ldhdev.utilityserver.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.ldhdev.utilityserver.dto.MojangProfile
import org.apache.logging.log4j.LogManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.io.StringWriter
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Controller
class StompChatController(private val template: SimpMessagingTemplate, private val repository: ModSessionRepository) {
    private val logger = LogManager.getLogger()
    private val passwordHashed by lazy {
        val md = MessageDigest.getInstance("SHA-256")
        val pw = System.getProperty("nameless.adminpw")
        md.digest(pw.toByteArray())
    }

    @GetMapping("/nameless/admin/sessions")
    @ResponseBody
    fun getSessions(): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(passwordHashed, "AES"))
        }

        val writer = StringWriter()
        writer.use {
            ObjectMapper().writeValue(it, repository.findAll())
        }
        return Base64.getEncoder().encodeToString(cipher.iv + cipher.doFinal(writer.toString().toByteArray()))
    }

    @MessageMapping("/join")
    fun enter(@Header("uuid") playerUUID: String) {
        val profile = MojangProfile.getFromUUID(playerUUID) ?: return
        val session = repository.findByPlayerUUID(profile.id) ?: ModPlayerSession()

        with(session) {
            this.playerUUID = profile.id
            this.name = profile.name
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

    @MessageMapping("/disconnect")
    fun disconnect(@Header(MOD_ID) id: String) {
        val session = repository.findByIdOrNull(id) ?: return

        session.online = false
        repository.save(session)

        logger.info("$session disconnected")
    }

    companion object {
        private const val MOD_ID = "mod-uuid"
    }
}