package com.vibely.pos.shared.domain.shift.valueobject

enum class ShiftStatus {
    OPEN,
    CLOSED,
    ;

    companion object {
        fun fromString(value: String): ShiftStatus = entries.find { it.name.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown shift status: $value")
    }
}
