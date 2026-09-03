package com.app.arco

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform