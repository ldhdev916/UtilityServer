package com.ldhdev.utilityserver.backup

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/backup")
class BackupController {

    private val backupDir = File("backup").apply { mkdirs() }
    private fun generateBackupUUID(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return if (File(backupDir, uuid).isFile) generateBackupUUID() else uuid
    }

    @PostMapping
    fun doBackup(@RequestBody content: ByteArray, response: HttpServletResponse): String {
        val uuid = generateBackupUUID()

        File(backupDir, uuid).writeBytes(Base64.getEncoder().encode(content))
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