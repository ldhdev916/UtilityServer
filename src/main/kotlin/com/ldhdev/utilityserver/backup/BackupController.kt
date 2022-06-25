package com.ldhdev.utilityserver.backup

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

@RestController
@RequestMapping("/backup")
class BackupController {

    private val backupDir = File("backup").apply { mkdirs() }

    private val publicKey = run {
        val inputStream = javaClass.classLoader.getResourceAsStream("backup.pub")!!
        val keyBytes = Base64.getDecoder().decode(inputStream.readBytes())
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    private fun generateBackupUUID(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return if (File(backupDir, uuid).isFile) generateBackupUUID() else uuid
    }

    @PostMapping
    fun doBackup(@RequestBody content: ByteArray): String {
        val uuid = generateBackupUUID()
        val cipher = Cipher.getInstance("RSA").apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }
        File(backupDir, uuid).writeBytes(Base64.getEncoder().encode(cipher.doFinal(content)))
        return uuid
    }

    @GetMapping("/{uuid}")
    fun sendBackupFile(@PathVariable uuid: String): String {
        val file = File(backupDir, uuid)
        if (!file.isFile) throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        return file.readText()
    }

    @DeleteMapping("/{uuid}")
    fun deleteBackupFile(@PathVariable uuid: String) = File(backupDir, uuid).delete()
}