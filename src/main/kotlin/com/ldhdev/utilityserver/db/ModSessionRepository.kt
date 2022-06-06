package com.ldhdev.utilityserver.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModSessionRepository : JpaRepository<ModPlayerSession, String> {

    fun findByOnlineIsTrue(): List<ModPlayerSession>

    fun findByNameEqualsIgnoreCaseAndOnlineIsTrue(name: String): ModPlayerSession?
}