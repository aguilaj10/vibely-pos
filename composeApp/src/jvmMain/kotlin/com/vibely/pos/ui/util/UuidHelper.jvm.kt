package com.vibely.pos.ui.util

import java.util.UUID

actual fun randomUuidString(): String = UUID.randomUUID().toString()
