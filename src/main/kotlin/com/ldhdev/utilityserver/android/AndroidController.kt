package com.ldhdev.utilityserver.android

import com.ldhdev.utilityserver.dto.ScriptResult
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/android")
class AndroidController {

    @PostMapping("/execution", consumes = ["text/plain"])
    fun executeScript(@RequestBody code: String, @RequestParam(defaultValue = "kotlin") type: String): ScriptResult {
        val executor = ScriptExecutor.createScriptExecutor(type) ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Unknown script type $type"
        )
        return runCatching {
            ScriptResult.success(executor.executeCode(code).toString())
        }.getOrElse {
            ScriptResult.failure(it)
        }
    }
}