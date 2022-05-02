package com.ldhdev.utilityserver.dto

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.getOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@kotlinx.serialization.Serializable
data class MojangProfile(val id: String, val name: String, val properties: List<JsonElement>) {
    companion object {

        private val jsonLoose = Json { ignoreUnknownKeys = true }

        fun getFromUUID(uuid: String): MojangProfile? {
            return "https://sessionserver.mojang.com/session/minecraft/profile/$uuid".httpGet()
                .responseObject<MojangProfile>(jsonLoose)
                .third.getOrNull()
        }
    }
}