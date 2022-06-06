package com.ldhdev.utilityserver.db

import com.ldhdev.utilityserver.dto.LocrawInfo
import org.hibernate.Hibernate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.*

@Entity
@Table(name = "sessions")
@EntityListeners(AuditingEntityListener::class)
class ModPlayerSession : BaseTimeEntity() {

    @Id
    lateinit var id: String

    lateinit var name: String

    lateinit var version: String

    var online = false

    @Convert(converter = LocrawInfo.Converter::class)
    var locraw: LocrawInfo? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as ModPlayerSession

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
    override fun toString(): String {
        return "ModPlayerSession(id='$id', name='$name', version='$version', online=$online, locraw=$locraw)"
    }


}
