package com.ldhdev.utilityserver.selftest

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToStream
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/selfTest")
class SelfTestController {

    private val selfTestDir = File("selfTest").apply { mkdirs() }

    @OptIn(ExperimentalSerializationApi::class)
    @PostMapping
    fun postSelfTestData(@RequestBody datas: List<JsonObject>, request: HttpServletRequest): String {
        val ip = if (request.remoteAddr == "0:0:0:0:0:0:0:1") "127.0.0.1" else request.remoteAddr
        File(selfTestDir, "$ip.json").outputStream().buffered().use {
            Json.encodeToStream(datas, it)
        }

        return ""
    }
}