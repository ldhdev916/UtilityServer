package com.ldhdev.utilityserver

import com.ldhdev.utilityserver.dto.ScriptResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.script.ScriptEngineManager

@RestController
class AndroidController {

    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    @PostMapping("/execution")
    fun executeKotlinScript(@RequestBody code: String): ScriptResult {
        return runCatching {
            val value: Any? = engine.eval(code)
            ScriptResult.success(value.toString())
        }.getOrElse {
            ScriptResult.failure(it)
        }
    }
}