package com.ldhdev.utilityserver.dto

data class ScriptResult(val result: String, val success: Boolean) {
    companion object {
        fun success(result: String) = ScriptResult(result, true)

        fun failure(throwable: Throwable) = ScriptResult(throwable.message.toString(), false)
    }
}