package com.ldhdev.utilityserver

import com.ldhdev.utilityserver.dto.ScriptResult
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.script.ScriptEngineManager

@RestController
class AndroidController {

    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    private val publicKey: PublicKey = run {
        val inputStream = javaClass.classLoader.getResourceAsStream("android.pub")!!
        val keyBytes = Base64.getDecoder().decode(inputStream.readBytes())
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    @PostMapping("/execution", consumes = ["application/octet-stream"])
    fun executeKotlinScript(@RequestBody encryptedCode: ByteArray): ScriptResult {

        val cipher = Cipher.getInstance("RSA").apply {
            init(Cipher.DECRYPT_MODE, publicKey)
        }
        val code = runCatching {
            cipher.doFinal(encryptedCode).decodeToString()
        }.getOrElse {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid private key used for code encryption", it)
        }

        return runCatching {
            val value: Any? = engine.eval(code)
            ScriptResult.success(value.toString())
        }.getOrElse {
            ScriptResult.failure(it)
        }
    }
}