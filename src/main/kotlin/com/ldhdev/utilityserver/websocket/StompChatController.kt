package com.ldhdev.utilityserver.websocket

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.ldhdev.utilityserver.dto.MojangProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

@Controller
class StompChatController(private val template: SimpMessagingTemplate) {

    private val jsonLoose = Json { ignoreUnknownKeys = true }
    private val joinedPlayers = hashMapOf<String, String>()
    private val logger = LogManager.getLogger()

    private val publicKey by lazy {
        val classLoader = Thread.currentThread().contextClassLoader
        val inputStream = classLoader.getResourceAsStream("Nameless.pem")!!
        val spec = X509EncodedKeySpec(Base64.getDecoder().decode(inputStream.readBytes()))
        KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    @ResponseBody
    @GetMapping("/nameless/admin/onlines")
    fun getOnlinePlayersByAdmin(): String {
        val cipher = Cipher.getInstance("RSA").apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }

        val encrypted = cipher.doFinal(Json.encodeToString(joinedPlayers).toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    @MessageMapping("/join/{uuid}")
    fun enter(@DestinationVariable("uuid") playerUUID: String) {
        val result = "https://sessionserver.mojang.com/session/minecraft/profile/$playerUUID".httpGet()
            .responseObject<MojangProfile>(jsonLoose)
            .third
        if (result is Result.Failure) return
        val userName = result.get().name
        val identifier = joinedPlayers.toList().find { it.second == userName }?.first ?: UUID.randomUUID().toString()
        joinedPlayers[identifier] = userName
        logger.info("$userName joined with identifier $identifier")

        template.convertAndSend("/topic/$playerUUID", identifier)
    }

    @MessageMapping("/onlines/{id}")
    fun getOnlinePlayers(@DestinationVariable("id") id: String) {
        val name = joinedPlayers[id] ?: return
        logger.info("$name requested online players")
        template.convertAndSend("/topic/onlines/$id", joinedPlayers.values.toString())
    }

    @MessageMapping("/disconnect/{id}")
    fun disconnect(@DestinationVariable("id") id: String) {
        val name = joinedPlayers.remove(id)
        logger.info("$name disconnected")
    }
}