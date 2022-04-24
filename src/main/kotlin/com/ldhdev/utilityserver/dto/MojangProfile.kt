package com.ldhdev.utilityserver.dto

import kotlinx.serialization.json.JsonElement

@kotlinx.serialization.Serializable
data class MojangProfile(val id: String, val name: String, val properties: List<JsonElement>)