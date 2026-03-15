package com.vibely.pos.shared.data.purchaseorder.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.purchaseorder.datasource.RemotePurchaseOrderDataSource
import com.vibely.pos.shared.data.purchaseorder.mapper.PurchaseOrderMapper
import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class PurchaseOrderRepositoryImpl(private val remoteDataSource: RemotePurchaseOrderDataSource) :
    BaseRepository(),
    PurchaseOrderRepository {

    override suspend fun getAll(supplierId: String?, status: PurchaseOrderStatus?, page: Int, pageSize: Int): Result<List<PurchaseOrder>> = mapList(
        remoteDataSource.getAllPurchaseOrders(
            supplierId = supplierId,
            status = status?.name?.lowercase(),
            page = page,
            pageSize = pageSize,
        ),
        PurchaseOrderMapper::toDomain,
    )

    override suspend fun getById(id: String): Result<PurchaseOrder> = mapSingle(
        remoteDataSource.getPurchaseOrderById(id),
        PurchaseOrderMapper::toDomain,
    )

    override suspend fun create(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> {
        val json = buildJsonObject {
            put("po_number", purchaseOrder.poNumber)
            put("supplier_id", purchaseOrder.supplierId)
            put("total_amount", purchaseOrder.totalAmount)
            put("status", purchaseOrder.status.name.lowercase())
            put("order_date", purchaseOrder.orderDate.toString())
            purchaseOrder.expectedDeliveryDate?.let { put("expected_delivery_date", it.toString()) }
            purchaseOrder.notes?.let { put("notes", it) }
            putJsonArray("items") {
                purchaseOrder.items.forEach { item ->
                    add(
                        buildJsonObject {
                            put("product_id", item.productId)
                            put("quantity", item.quantity)
                            put("unit_cost", item.unitCost)
                        },
                    )
                }
            }
        }
        return mapSingle(
            remoteDataSource.createPurchaseOrder(json),
            PurchaseOrderMapper::toDomain,
        )
    }

    override suspend fun update(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> {
        val json = buildJsonObject {
            put("po_number", purchaseOrder.poNumber)
            put("supplier_id", purchaseOrder.supplierId)
            put("total_amount", purchaseOrder.totalAmount)
            put("status", purchaseOrder.status.name.lowercase())
            put("order_date", purchaseOrder.orderDate.toString())
            purchaseOrder.expectedDeliveryDate?.let { put("expected_delivery_date", it.toString()) }
            purchaseOrder.notes?.let { put("notes", it) }
        }
        return mapSingle(
            remoteDataSource.updatePurchaseOrder(purchaseOrder.id, json),
            PurchaseOrderMapper::toDomain,
        )
    }

    override suspend fun updateStatus(id: String, status: PurchaseOrderStatus): Result<PurchaseOrder> = mapSingle(
        remoteDataSource.updatePurchaseOrderStatus(id, status.name.lowercase()),
        PurchaseOrderMapper::toDomain,
    )

    override suspend fun delete(id: String): Result<Unit> = remoteDataSource.deletePurchaseOrder(id)

    override suspend fun receive(id: String): Result<PurchaseOrder> = mapSingle(
        remoteDataSource.receivePurchaseOrder(id),
        PurchaseOrderMapper::toDomain,
    )

    override suspend fun generatePoNumber(): Result<String> = remoteDataSource.generatePoNumber()
}
