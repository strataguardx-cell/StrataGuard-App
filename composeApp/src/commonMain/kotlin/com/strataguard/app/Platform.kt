package com.strataguard.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform