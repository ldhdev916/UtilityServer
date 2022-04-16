package com.ldhdev.utilityserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UtilityServerApplication

fun main(args: Array<String>) {
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true")
    runApplication<UtilityServerApplication>(*args)
}
