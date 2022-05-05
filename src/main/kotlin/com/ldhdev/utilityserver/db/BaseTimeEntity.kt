package com.ldhdev.utilityserver.db

import java.time.ZoneId
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.MappedSuperclass
import javax.persistence.PrePersist
import javax.persistence.PreUpdate

@MappedSuperclass
abstract class BaseTimeEntity {

    @Column(name = "created_at")
    lateinit var createdDate: ZonedDateTime

    @Column(name = "modified_at")
    lateinit var modifiedDate: ZonedDateTime

    @PrePersist
    fun prePersist() {
        createdDate = ZonedDateTime.now(zoneId)
        modifiedDate = ZonedDateTime.now(zoneId)
    }

    @PreUpdate
    fun preUpdate() {
        modifiedDate = ZonedDateTime.now(zoneId)
    }

    companion object {
        private val zoneId by lazy { ZoneId.of("UTC+9") }
    }
}