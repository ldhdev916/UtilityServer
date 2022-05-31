package com.ldhdev.utilityserver.android

import com.ldhdev.utilityserver.dto.ScriptResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.script.ScriptEngineManager

@RestController
@RequestMapping("/android")
class AndroidController {

    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    @PostMapping("/execution", consumes = ["text/plain"])
    fun executeKotlinScript(@RequestBody code: String): ScriptResult {
        return runCatching {
            val result: Any? = engine.eval(code)
            ScriptResult.success(result.toString())
        }.getOrElse {
            ScriptResult.failure(it)
        }
    }
}