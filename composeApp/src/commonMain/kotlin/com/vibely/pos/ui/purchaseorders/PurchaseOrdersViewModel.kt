@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.vibely.pos.ui.purchaseorders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrderItem
import com.vibely.pos.shared.domain.purchaseorder.usecase.CreatePurchaseOrderUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.DeletePurchaseOrderUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.GetAllPurchaseOrdersUseCase
import com.vibely.pos.shared.domain.purchaseorder.usecase.UpdatePurchaseOrderUseCase
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.usecase.GetAllSuppliersUseCase
import com.vibely.pos.ui.dialogs.PurchaseOrderFormData
import com.vibely.pos.ui.util.randomUuidString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class PurchaseOrdersState(
    val purchaseOrders: List<PurchaseOrder> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val totalAmount: Double = 0.0,
    val showPODialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val editingPO: PurchaseOrder? = null,
    val deletingPOId: String? = null,
)

class PurchaseOrdersViewModel(
    private val getAllPurchaseOrdersUseCase: GetAllPurchaseOrdersUseCase,
    private val createPurchaseOrderUseCase: CreatePurchaseOrderUseCase,
    private val updatePurchaseOrderUseCase: UpdatePurchaseOrderUseCase,
    private val deletePurchaseOrderUseCase: DeletePurchaseOrderUseCase,
    private val getAllSuppliersUseCase: GetAllSuppliersUseCase,
    private val getAllProductsUseCase: com.vibely.pos.shared.domain.inventory.usecase.GetAllProductsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PurchaseOrdersState())
    val state: StateFlow<PurchaseOrdersState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadPurchaseOrders()
        loadSuppliersAndProducts()
    }

    fun loadPurchaseOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getAllPurchaseOrdersUseCase()) {
                is Result.Success -> {
                    val orders = result.data

                    _state.update {
                        it.copy(
                            purchaseOrders = orders,
                            isLoading = false,
                            totalOrders = orders.size,
                            pendingOrders = orders.count { order -> order.status == PurchaseOrderStatus.PENDING },
                            totalAmount = orders.sumOf { order -> order.totalAmount },
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun loadSuppliersAndProducts() {
        viewModelScope.launch {
            when (val suppliersResult = getAllSuppliersUseCase()) {
                is Result.Success -> {
                    _state.update { it.copy(suppliers = suppliersResult.data) }
                }
                is Result.Error -> {}
            }

            when (val productsResult = getAllProductsUseCase()) {
                is Result.Success -> {
                    _state.update { it.copy(products = productsResult.data) }
                }
                is Result.Error -> {}
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        if (query.isBlank()) {
            loadPurchaseOrders()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            searchPurchaseOrders(query)
        }
    }

    private fun searchPurchaseOrders(query: String) {
        val allOrders = _state.value.purchaseOrders
        val filteredOrders = allOrders.filter { order ->
            order.poNumber.contains(query, ignoreCase = true) ||
                order.supplierName?.contains(query, ignoreCase = true) == true ||
                order.status.name.contains(query, ignoreCase = true)
        }

        _state.update {
            it.copy(
                purchaseOrders = filteredOrders,
                totalOrders = filteredOrders.size,
                pendingOrders = filteredOrders.count { order -> order.status == PurchaseOrderStatus.PENDING },
                totalAmount = filteredOrders.sumOf { order -> order.totalAmount },
            )
        }
    }

    fun onClearSearch() {
        _state.update { it.copy(searchQuery = "") }
        loadPurchaseOrders()
    }

    fun onCreatePurchaseOrder() {
        _state.update { it.copy(showPODialog = true, editingPO = null) }
    }

    fun onEditPurchaseOrder(purchaseOrderId: String) {
        val order = _state.value.purchaseOrders.find { it.id == purchaseOrderId }
        _state.update { it.copy(showPODialog = true, editingPO = order) }
    }

    fun onDeletePurchaseOrder(purchaseOrderId: String) {
        _state.update { it.copy(showDeleteDialog = true, deletingPOId = purchaseOrderId) }
    }

    fun onViewPurchaseOrder(purchaseOrderId: String) {
        // For now, redirect to edit
        onEditPurchaseOrder(purchaseOrderId)
    }

    fun onDismissPODialog() {
        _state.update { it.copy(showPODialog = false, editingPO = null) }
    }

    fun onDismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false, deletingPOId = null) }
    }

    fun onSavePurchaseOrder(formData: PurchaseOrderFormData) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val items = formData.lineItems.map { lineItem ->
                val product = _state.value.products.find { it.id == lineItem.productId }
                PurchaseOrderItem(
                    id = lineItem.id.ifBlank { randomUuidString() },
                    purchaseOrderId = formData.id.ifBlank { randomUuidString() },
                    productId = lineItem.productId,
                    productName = product?.name,
                    productSku = product?.sku,
                    quantity = lineItem.quantity.toIntOrNull() ?: 1,
                    unitCost = lineItem.unitCost.toDoubleOrNull() ?: 0.0,
                    subtotal = lineItem.calculateSubtotal(),
                    createdAt = Clock.System.now(),
                )
            }

            val supplier = _state.value.suppliers.find { it.id == formData.supplierId }

            val purchaseOrder = PurchaseOrder(
                id = formData.id.ifBlank { randomUuidString() },
                poNumber = generatePONumber(),
                supplierId = formData.supplierId,
                supplierName = supplier?.name,
                createdById = "", // Would come from auth
                totalAmount = formData.calculateTotal(),
                status = PurchaseOrderStatus.DRAFT,
                orderDate = Clock.System.now(),
                expectedDeliveryDate = null,
                receivedDate = null,
                notes = formData.notes.ifBlank { null },
                items = items,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
            )

            val result = if (formData.id.isBlank()) {
                createPurchaseOrderUseCase(purchaseOrder)
            } else {
                updatePurchaseOrderUseCase(purchaseOrder)
            }

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showPODialog = false,
                            editingPO = null,
                            successMessage = if (formData.id.isBlank()) {
                                "Purchase order created successfully"
                            } else {
                                "Purchase order updated successfully"
                            },
                        )
                    }
                    loadPurchaseOrders()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onConfirmDelete() {
        val poId = _state.value.deletingPOId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteDialog = false) }

            when (val result = deletePurchaseOrderUseCase(poId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingPOId = null,
                            successMessage = "Purchase order deleted successfully",
                        )
                    }
                    loadPurchaseOrders()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingPOId = null,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSuccessMessageDismiss() {
        _state.update { it.copy(successMessage = null) }
    }

    private fun generatePONumber(): String = "PO-${Clock.System.now().toEpochMilliseconds().toString().takeLast(6)}"
}
