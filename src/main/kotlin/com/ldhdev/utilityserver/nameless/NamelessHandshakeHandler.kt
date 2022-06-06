package com.ldhdev.utilityserver.nameless

import com.ldhdev.utilityserver.db.ModPlayerSession
import com.ldhdev.utilityserver.db.ModSessionRepository
import com.ldhdev.utilityserver.dto.MojangProfile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.server.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

@Component
class NamelessHandshakeHandler(private val repository: ModSessionRepository) : DefaultHandshakeHandler() {

    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Principal? {
        val uuid = request.headers.getFirst("uuid")!!
        val modVersion = request.headers.getFirst("mod-version")!!
        val profile = MojangProfile.getFromUUID(uuid)

        val session = repository.findByIdOrNull(profile.id) ?: ModPlayerSession()

        with(session) {
            id = profile.id
            name = profile.name
            version = modVersion
            online = true
        }

        repository.save(session)

        return NamelessUser(session.id)
    }


}