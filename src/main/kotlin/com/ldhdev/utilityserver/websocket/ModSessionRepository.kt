package com.ldhdev.utilityserver.websocket

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModSessionRepository : JpaRepository<ModPlayerSession, String> {

    fun findByPlayerUUID(playerUUID: String): ModPlayerSession?

    fun findByOnlineIsTrue(): List<ModPlayerSession>

    fun findByNameEqualsIgnoreCase(name: String): ModPlayerSession?
}