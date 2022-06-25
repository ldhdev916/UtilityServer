package com.ldhdev.utilityserver.backup

import com.ldhdev.utilityserver.RSATokenFilter
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest

@WebFilter("/backup")
class BackupTokenFilter : RSATokenFilter() {

    override val publicKey: PublicKey = run {
        val inputStream = javaClass.classLoader.getResourceAsStream("backup.pub")!!
        val keyBytes = Base64.getDecoder().decode(inputStream.readBytes())
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.method != "POST"
    }
}