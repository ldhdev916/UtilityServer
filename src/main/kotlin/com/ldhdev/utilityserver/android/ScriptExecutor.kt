package com.ldhdev.utilityserver.android

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

fun interface ScriptExecutor {
    fun executeCode(code: String): Any?

    companion object {
        fun createScriptExecutor(scriptType: String): ScriptExecutor? = when (scriptType) {
            "kotlin", "kts" -> KotlinScriptExecutor
            "javascript", "js" -> JavaScriptExecutor
            else -> null
        }
    }
}

private abstract class EngineScriptExecutor(private val engine: ScriptEngine) : ScriptExecutor {
    override fun executeCode(code: String): Any? {
        return engine.eval(code)
    }
}

private object KotlinScriptExecutor : EngineScriptExecutor(ScriptEngineManager().getEngineByName("kotlin"))

private object JavaScriptExecutor : EngineScriptExecutor(ScriptEngineManager().getEngineByName("js"))