package com.ldhdev.utilityserver

import org.springframework.web.filter.OncePerRequestFilter
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

abstract class RSATokenFilter : OncePerRequestFilter() {
    protected abstract val publicKey: PublicKey

    private val md = MessageDigest.getInstance("SHA-256")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestWrapper = SaveCodeBodyRequest(request)
        val authorized = isAuthorized(request.getHeader("Authorization")) { requestWrapper.body }

        if (!authorized) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
            return
        }

        filterChain.doFilter(requestWrapper, response)

    }

    private fun isAuthorized(authorizationWithType: String?, bodyGetter: () -> ByteArray): Boolean {
        if (authorizationWithType == null || !authorizationWithType.startsWith("Bearer ")) return false
        val authorization = authorizationWithType.substringAfter("Bearer ")
        val split = authorization.split(".")
        if (split.size != 2) return false
        val (signed, salt) = split.map {
            runCatching {
                Base64.getDecoder().decode(it)
            }.getOrElse {
                return false
            }
        }

        val signature = Signature.getInstance("SHA256withRSA").apply {
            initVerify(publicKey)
            update(md.digest(bodyGetter() + salt))
        }
        return signature.verify(signed)
    }


    private class SaveCodeBodyRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
        val body by lazy { request.inputStream.readBytes() }

        override fun getInputStream(): ServletInputStream = object : ServletInputStream() {
            private val stream = body.inputStream()

            override fun read() = stream.read()

            override fun isFinished() = false

            override fun isReady() = true

            override fun setReadListener(listener: ReadListener?) {
                throw UnsupportedOperationException()
            }
        }
    }
}