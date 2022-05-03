package com.ldhdev.utilityserver.nameless

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "mod_player_session")
class ModPlayerSession {

    @Column(name = "uuid")
    @JsonProperty("uuid")
    lateinit var playerUUID: String

    lateinit var name: String

    lateinit var version: String

    var online = false

    @Id
    var id = UUID.randomUUID().toString()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModPlayerSession

        if (playerUUID != other.playerUUID) return false
        if (name != other.name) return false
        if (online != other.online) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playerUUID.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + online.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "ModPlayerSession(playerUUID='$playerUUID', name='$name', online=$online, id='$id')"
    }

}
