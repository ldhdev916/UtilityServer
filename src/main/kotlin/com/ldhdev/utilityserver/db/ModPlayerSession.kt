package com.ldhdev.utilityserver.db

import com.fasterxml.jackson.annotation.JsonProperty
import com.ldhdev.utilityserver.dto.LocrawInfo
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "sessions")
@EntityListeners(AuditingEntityListener::class)
class ModPlayerSession : BaseTimeEntity() {

    @Column(name = "uuid")
    @JsonProperty("uuid")
    lateinit var playerUUID: String

    lateinit var name: String

    lateinit var version: String

    var online = false

    @Convert(converter = LocrawInfo.Converter::class)
    var locraw: LocrawInfo? = null

    @Id
    var id = UUID.randomUUID().toString()
    override fun toString(): String {
        return "ModPlayerSession(playerUUID='$playerUUID', name='$name', version='$version', online=$online, locraw=$locraw, id='$id')"
    }

}
