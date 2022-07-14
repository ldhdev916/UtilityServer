package com.ldhdev.utilityserver.benedu

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/benedu")
class BeneduController {

    private val authKeys = hashSetOf<String>()

    @GetMapping
    fun addBeneduAuthKey() = UUID.randomUUID().toString().replace("-", "").also { authKeys.add(it) }

    @GetMapping("/{key}")
    fun verifyBeneduAuthKeys(@PathVariable key: String) = authKeys.remove(key)
}