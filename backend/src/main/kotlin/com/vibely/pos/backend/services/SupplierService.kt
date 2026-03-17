@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "LongParameterList",
    "StringLiteralDuplication",
    "MaxLineLength",
)

package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.backend.dto.request.CreateSupplierRequest
import com.vibely.pos.backend.dto.request.UpdateSupplierRequest
import com.vibely.pos.shared.data.supplier.dto.SupplierDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val ERROR_FETCH_FAILED = "Failed to fetch suppliers"
private const val ERROR_SUPPLIER_NOT_FOUND = "Supplier not found"
private const val ERROR_CREATE_FAILED = "Failed to create supplier"
private const val ERROR_UPDATE_FAILED = "Failed to update supplier"
private const val ERROR_DELETE_FAILED = "Failed to delete supplier"

class SupplierService(
    private val supabaseClient: SupabaseClient,
) : BaseService() {
    suspend fun getAllSuppliers(
        userId: String,
        isActive: Boolean?,
        searchQuery: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<SupplierDTO>> {
        val (from, to) = calculatePaginationRange(page, pageSize)
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient
                .from(TableNames.SUPPLIERS)
                .select {
                    filter {
                        eq(DatabaseColumns.USER_ID, userId)
                        isActive?.let { eq(DatabaseColumns.IS_ACTIVE, it) }
                        searchQuery?.let { query ->
                            val searchPattern = "%$query%"
                            or {
                                ilike(DatabaseColumns.NAME, searchPattern)
                                ilike(DatabaseColumns.CONTACT_PERSON, searchPattern)
                                ilike(DatabaseColumns.EMAIL, searchPattern)
                                ilike(DatabaseColumns.PHONE, searchPattern)
                                ilike(DatabaseColumns.SUPPLIER_CODE, searchPattern)
                            }
                        }
                    }
                    order(DatabaseColumns.NAME, Order.ASCENDING)
                    range(from, to)
                }.decodeList<SupplierDTO>()
        }
    }

    suspend fun getSupplierById(
        userId: String,
        supplierId: String,
    ): Result<SupplierDTO> =
        executeQuery(ERROR_SUPPLIER_NOT_FOUND) {
            supabaseClient
                .from(TableNames.SUPPLIERS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, supplierId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                }.decodeSingle<SupplierDTO>()
        }

    suspend fun createSupplier(
        userId: String,
        request: CreateSupplierRequest,
    ): Result<SupplierDTO> =
        executeQuery(ERROR_CREATE_FAILED) {
            val data =
                buildJsonObject {
                    put(DatabaseColumns.USER_ID, userId)
                    put(DatabaseColumns.CODE, request.code)
                    put(DatabaseColumns.NAME, request.name)
                    request.contactPerson?.let { put(DatabaseColumns.CONTACT_PERSON, it) }
                    request.email?.let { put(DatabaseColumns.EMAIL, it) }
                    request.phone?.let { put(DatabaseColumns.PHONE, it) }
                    request.address?.let { put(DatabaseColumns.ADDRESS, it) }
                    put(DatabaseColumns.IS_ACTIVE, request.isActive)
                }

            supabaseClient
                .from(TableNames.SUPPLIERS)
                .insert(data) {
                    select()
                }.decodeSingle<SupplierDTO>()
        }

    suspend fun updateSupplier(
        userId: String,
        supplierId: String,
        request: UpdateSupplierRequest,
    ): Result<SupplierDTO> =
        executeQuery(ERROR_UPDATE_FAILED) {
            val data =
                buildJsonObject {
                    request.code?.let { put(DatabaseColumns.CODE, it) }
                    request.name?.let { put(DatabaseColumns.NAME, it) }
                    request.contactPerson?.let { put(DatabaseColumns.CONTACT_PERSON, it) }
                    request.email?.let { put(DatabaseColumns.EMAIL, it) }
                    request.phone?.let { put(DatabaseColumns.PHONE, it) }
                    request.address?.let { put(DatabaseColumns.ADDRESS, it) }
                    request.isActive?.let { put(DatabaseColumns.IS_ACTIVE, it) }
                }

            supabaseClient
                .from(TableNames.SUPPLIERS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, supplierId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                    select()
                }.decodeSingle<SupplierDTO>()
        }

    suspend fun deleteSupplier(
        userId: String,
        supplierId: String,
    ): Result<Unit> =
        executeQuery(ERROR_DELETE_FAILED) {
            supabaseClient
                .from(TableNames.SUPPLIERS)
                .delete {
                    filter {
                        eq(DatabaseColumns.ID, supplierId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                }
        }
}
