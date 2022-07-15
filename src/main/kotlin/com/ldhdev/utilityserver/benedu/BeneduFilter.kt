package com.ldhdev.utilityserver.benedu

import org.springframework.http.HttpHeaders
import org.springframework.web.filter.OncePerRequestFilter
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebFilter("/benedu", "/benedu/")
class BeneduFilter : OncePerRequestFilter() {

    private val publicKey by lazy {
        val inputStream = javaClass.classLoader.getResourceAsStream("benedu.pub")!!

        val keyBytes = Base64.getDecoder().decode(inputStream.readBytes())

        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    private val signingBytes by lazy {
        val md = MessageDigest.getInstance("SHA-256")

        md.digest("benedu".toByteArray())
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (isAuthorized(request.getHeader(HttpHeaders.AUTHORIZATION))) {
            filterChain.doFilter(request, response)
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        }
    }

    private fun isAuthorized(header: String?): Boolean {
        if (header == null) return false
        if (!header.startsWith("Bearer ")) return false
        val token = header.substringAfter("Bearer ")

        val signature = Signature.getInstance("SHA256withRSA").apply {
            initVerify(publicKey)
            update(signingBytes)
        }

        return signature.verify(Base64.getDecoder().decode(token))
    }
}