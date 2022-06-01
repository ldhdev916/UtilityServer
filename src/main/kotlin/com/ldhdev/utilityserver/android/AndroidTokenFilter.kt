package com.ldhdev.utilityserver.android

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
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

@WebFilter("/android/execution")
class AndroidTokenFilter : OncePerRequestFilter() {

    private val publicKey: PublicKey = run {
        val inputStream = javaClass.classLoader.getResourceAsStream("android.pub")!!
        val keyBytes = Base64.getDecoder().decode(inputStream.readBytes())
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    private val md = MessageDigest.getInstance("SHA-256")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestWrapper = SaveCodeBodyRequest(request)
        val authorized = isAuthorized(request.getHeader("Authorization")) { requestWrapper.code }

        if (!authorized) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
            return
        }

        filterChain.doFilter(requestWrapper, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !(request.method == "POST" && request.getHeader("Content-Type") == "text/plain")
    }

    private fun isAuthorized(authorizationWithType: String?, bodyGetter: () -> String): Boolean {
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
            update(md.digest(bodyGetter().toByteArray() + salt))
        }
        return signature.verify(signed)
    }

    private class SaveCodeBodyRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
        val code by lazy { request.inputStream.readBytes().decodeToString() }

        override fun getInputStream(): ServletInputStream = object : ServletInputStream() {
            private val stream = code.byteInputStream()

            override fun read() = stream.read()

            override fun isFinished() = false

            override fun isReady() = true

            override fun setReadListener(listener: ReadListener?) {
                throw UnsupportedOperationException()
            }
        }
    }
}