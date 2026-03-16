@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
package com.vibely.pos.backend.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSupplierRequest(
    @SerialName("code")
    val code: String,
    @SerialName("name")
    val name: String,
    @SerialName("contact_person")
    val contactPerson: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class UpdateSupplierRequest(
    @SerialName("code")
    val code: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("contact_person")
    val contactPerson: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null
)
