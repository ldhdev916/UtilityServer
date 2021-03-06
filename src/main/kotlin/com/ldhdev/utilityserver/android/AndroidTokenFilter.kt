package com.ldhdev.utilityserver.android

import com.ldhdev.utilityserver.RSATokenFilter
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest

@WebFilter("/android/execution")
class AndroidTokenFilter : RSATokenFilter() {

    override val publicKey: PublicKey = run {
        val inputStream = javaClass.classLoader.getResourceAsStream("android.pub")!!
        val keyBytes = Base64.getDecoder().decode(inputStream.readBytes())
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.method != "POST"
    }
}