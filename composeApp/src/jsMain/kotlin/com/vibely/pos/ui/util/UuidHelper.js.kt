package com.vibely.pos.ui.util

import kotlin.random.Random

actual fun randomUuidString(): String {
    val randomBytes = Random.Default.nextBytes(16)
    randomBytes[6] = ((randomBytes[6].toInt() and 0x0F) or 0x40).toByte()
    randomBytes[8] = ((randomBytes[8].toInt() and 0x3F) or 0x80).toByte()

    return buildString {
        randomBytes.forEachIndexed { index, byte ->
            if (index == 4 || index == 6 || index == 8 || index == 10) append('-')
            append(byte.toUByte().toString(16).padStart(2, '0'))
        }
    }
}
