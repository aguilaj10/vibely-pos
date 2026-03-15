@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
package com.vibely.pos.backend.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenShiftRequest(
    @SerialName("opening_balance")
    val openingBalance: Double,
)

@Serializable
data class CloseShiftRequest(
    @SerialName("closing_balance")
    val closingBalance: Double,
    val notes: String? = null,
)
