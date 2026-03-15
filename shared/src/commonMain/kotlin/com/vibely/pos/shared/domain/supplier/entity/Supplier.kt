package com.vibely.pos.shared.domain.supplier.entity

import kotlin.time.Clock
import kotlin.time.Instant

data class Supplier(
    val id: String,
    val code: String,
    val name: String,
    val contactPerson: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val displayName: String
        get() = name

    fun withName(newName: String): Supplier = copy(
        name = newName,
        updatedAt = Clock.System.now(),
    )

    fun withContactPerson(newContactPerson: String?): Supplier = copy(
        contactPerson = newContactPerson,
        updatedAt = Clock.System.now(),
    )

    fun withEmail(newEmail: String?): Supplier = copy(
        email = newEmail,
        updatedAt = Clock.System.now(),
    )

    fun withPhone(newPhone: String?): Supplier = copy(
        phone = newPhone,
        updatedAt = Clock.System.now(),
    )

    fun withAddress(newAddress: String?): Supplier = copy(
        address = newAddress,
        updatedAt = Clock.System.now(),
    )

    fun deactivate(): Supplier = copy(
        isActive = false,
        updatedAt = Clock.System.now(),
    )

    fun activate(): Supplier = copy(
        isActive = true,
        updatedAt = Clock.System.now(),
    )

    companion object {
        fun create(
            id: String,
            code: String,
            name: String,
            contactPerson: String? = null,
            email: String? = null,
            phone: String? = null,
            address: String? = null,
            isActive: Boolean = true,
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): Supplier = Supplier(
            id = id,
            code = code,
            name = name,
            contactPerson = contactPerson,
            email = email,
            phone = phone,
            address = address,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
