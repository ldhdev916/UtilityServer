package com.ldhdev.utilityserver

import com.ldhdev.utilityserver.dto.ScriptResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import javax.script.ScriptEngineManager

@RestController
class AndroidController {

    private val engine by lazy { ScriptEngineManager().getEngineByExtension("kts")!! }
    private val androidDir by lazy { File("android") }

    @PostMapping("/execution")
    fun executeKotlinScript(@RequestBody code: String): ScriptResult {
        return runCatching {
            val value: Any? = engine.eval(code)
            ScriptResult.success(value.toString())
        }.getOrElse {
            ScriptResult.failure(it)
        }
    }

    @PutMapping("/androidFiles/{path}")
    fun saveAndroidFiles(@PathVariable path: String, @RequestBody fileBytes: ByteArray): ResponseEntity<String> {
        val file = File(androidDir, path).apply { parentFile.mkdirs() }
        file.writeBytes(fileBytes)

        return ResponseEntity.created(file.toURI()).build()
    }
}