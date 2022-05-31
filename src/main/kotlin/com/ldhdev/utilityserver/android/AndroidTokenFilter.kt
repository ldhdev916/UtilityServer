package com.ldhdev.utilityserver.android

import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

@Component
class AndroidTokenFilter : OncePerRequestFilter() {

    private val publicKey: PublicKey = run {
        val inputStream = javaClass.classLoader.getResourceAsStream("android.pub")!!
        val keyBytes = Base64.getDecoder().decode(inputStream.readBytes())
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    private val md = MessageDigest.getInstance("SHA-256")

    private fun HttpServletResponse.sendUnauthorized() = sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val headerWithType = request.getHeader("Authorization")
        if (headerWithType == null || !headerWithType.startsWith("Bearer ")) {
            response.sendUnauthorized()
            return
        }
        val header = headerWithType.substringAfter("Bearer ")

        val split = header.split(".")
        if (split.size != 3) {
            response.sendUnauthorized()
            return
        }
        val (code, signed, salt) = split.map {
            runCatching {
                Base64.getDecoder().decode(it)
            }.getOrElse {
                response.sendUnauthorized()
                return
            }
        }

        val plainBytes = md.digest(code + salt)
        val signature = Signature.getInstance("SHA256withRSA").apply {
            initVerify(publicKey)
            update(plainBytes)
        }

        if (!signature.verify(signed)) {
            response.sendUnauthorized()
            return
        }

        filterChain.doFilter(AppendCodeBodyRequest(request, code), response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI != "/android/execution"
    }

    private class AppendCodeBodyRequest(wrapped: HttpServletRequest, private val code: ByteArray) :
        HttpServletRequestWrapper(wrapped) {

        override fun getInputStream(): ServletInputStream {

            val internal = code.inputStream()

            return object : ServletInputStream() {
                override fun read() = internal.read()

                override fun isFinished() = false

                override fun isReady() = true

                override fun setReadListener(listener: ReadListener?) {
                    throw UnsupportedOperationException()
                }
            }
        }
    }
}