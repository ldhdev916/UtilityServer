package com.ldhdev.utilityserver.nameless

import com.ldhdev.utilityserver.dto.MojangProfile
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebFilter("/nameless/stomp")
class NamelessValidFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.getHeader("mod-version") == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mod version not specified")
            return
        }
        val uuid = request.getHeader("uuid")
        runCatching {
            MojangProfile.getFromUUID(uuid)
        }.onFailure {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Player UUID is not valid")
            return
        }
        filterChain.doFilter(request, response)
    }
}