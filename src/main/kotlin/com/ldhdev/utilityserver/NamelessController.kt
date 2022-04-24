package com.ldhdev.utilityserver

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.ldhdev.utilityserver.dto.MojangProfile
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToStream
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File

@RestController
@RequestMapping("/nameless")
class NamelessController {

    private val jsonLoose = Json { ignoreUnknownKeys = true }
    private val prettyJson = Json { prettyPrint = true }
    private val namelessConfigDir = File("NamelessConfig").apply { mkdirs() }

    @OptIn(ExperimentalSerializationApi::class)
    @PostMapping("/config")
    fun receiveUserConfig(
        @RequestParam uuid: String,
        @RequestBody config: Map<String, JsonElement>
    ): ResponseEntity<String> {
        val (_, _, result) = "https://sessionserver.mojang.com/session/minecraft/profile/$uuid"
            .httpGet()
            .responseObject<MojangProfile>(jsonLoose)

        if (result is Result.Failure) {
            return ResponseEntity.badRequest().body("Error: $uuid")
        }

        File(namelessConfigDir, "$uuid.json").outputStream().buffered().use {
            prettyJson.encodeToStream(config, it)
        }

        return ResponseEntity.ok("Hello, ${result.get().name}")
    }
}