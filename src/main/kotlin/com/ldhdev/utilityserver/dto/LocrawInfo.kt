package com.ldhdev.utilityserver.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.persistence.AttributeConverter

@Serializable
data class LocrawInfo(
    var server: String,
    @SerialName("gametype") var gameType: String = "",
    var mode: String = "lobby",
    var map: String = ""
) {
    object Converter : AttributeConverter<LocrawInfo?, String> {
        override fun convertToDatabaseColumn(attribute: LocrawInfo?): String? {
            return attribute?.let(Json::encodeToString)
        }

        override fun convertToEntityAttribute(dbData: String?): LocrawInfo? {
            return dbData?.let(Json::decodeFromString)
        }
    }
}