package com.vibely.pos.shared

expect object Platform {
    val name: String
}

fun getPlatformName(): String = Platform.name
