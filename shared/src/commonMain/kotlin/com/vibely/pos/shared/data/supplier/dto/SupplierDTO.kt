package com.vibely.pos.shared.data.supplier.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupplierDTO(
    @SerialName("id")
    val id: String,
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
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
)
