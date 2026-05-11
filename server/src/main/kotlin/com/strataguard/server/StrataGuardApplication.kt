package com.strataguard.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StrataGuardApplication

fun main(args: Array<String>) {
    runApplication<StrataGuardApplication>(*args)
}
