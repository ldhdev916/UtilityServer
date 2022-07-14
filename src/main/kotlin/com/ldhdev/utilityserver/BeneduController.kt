package com.ldhdev.utilityserver

import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/benedu")
class BeneduController {

    private val beneduData = mutableMapOf<LocalDate, Map<String, List<String>>>()

    @PutMapping(consumes = ["application/json"])
    fun putBeneduData(@RequestBody data: Map<String, List<String>>): Map<String, List<String>> {
        beneduData[LocalDate.now()] = data

        return data
    }

    @GetMapping
    fun getBeneduData() = beneduData[LocalDate.now()].orEmpty()
}