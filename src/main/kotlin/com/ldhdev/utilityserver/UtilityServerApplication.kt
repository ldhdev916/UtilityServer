package com.ldhdev.utilityserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan

@ServletComponentScan
@SpringBootApplication
class UtilityServerApplication

fun main(args: Array<String>) {
    runApplication<UtilityServerApplication>(*args)
}
