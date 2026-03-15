@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
package com.vibely.pos.backend.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateSupplierRequest(
    val code: String,
    val name: String,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class UpdateSupplierRequest(
    val code: String? = null,
    val name: String? = null,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val isActive: Boolean? = null
)
