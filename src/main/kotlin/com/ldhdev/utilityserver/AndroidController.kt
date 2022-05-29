package com.ldhdev.utilityserver

import com.ldhdev.utilityserver.dto.ScriptResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.script.ScriptEngineManager

@RestController
@RequestMapping("/android")
class AndroidController {

    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    private val publicKey: PublicKey = run {
        val inputStream = javaClass.classLoader.getResourceAsStream("android.pub")!!
        val keyBytes = Base64.getDecoder().decode(inputStream.readBytes())
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    private val md = MessageDigest.getInstance("SHA-256")
    private val verifies = mutableMapOf<String, ByteArray>()
    private val verifyScope by lazy { CoroutineScope(Dispatchers.Default) }

    @PostMapping("/verify", consumes = ["text/plain"])
    fun preVerifyCode(@RequestBody code: String): String {
        if (code in verifies) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Already existing verify for code $code")
        }
        val random = SecureRandom()
        val salt = ByteArray(16).apply { random.nextBytes(this) }
        val verifyBytes = md.digest(code.toByteArray() + salt)
        verifies[code] = verifyBytes
        verifyScope.launch {
            delay(5 * 1000)
            verifies.remove(code)
        }
        return Base64.getEncoder().encodeToString(verifyBytes)
    }

    @PostMapping("/execution", consumes = ["text/plain"])
    fun executeKotlinScript(
        @RequestBody code: String,
        @RequestHeader("Authorization") verifyString: String
    ): ScriptResult {

        val verifyBytes = runCatching {
            Base64.getDecoder().decode(verifyString)
        }.getOrElse {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        val plainBytes =
            verifies.remove(code) ?: throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY)

        val signature = Signature.getInstance("SHA256withRSA").apply {
            initVerify(publicKey)
            update(plainBytes)
        }

        if (!signature.verify(verifyBytes)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        return runCatching {
            val value: Any? = engine.eval(code)
            ScriptResult.success(value.toString())
        }.getOrElse {
            ScriptResult.failure(it)
        }
    }
}