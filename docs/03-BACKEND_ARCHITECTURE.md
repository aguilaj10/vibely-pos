# Backend Architecture - Clean Architecture for Kotlin Multiplatform

**Project:** vibely-pos
**Date:** 2026-03-12
**Architecture:** Clean Architecture (Domain-Data-Presentation)
**Framework:** Kotlin Multiplatform with Ktor
**Database:** PostgreSQL 17.6 (Supabase)
**DI:** Koin

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Layer Structure](#layer-structure)
3. [Domain Layer](#domain-layer)
4. [Data Layer](#data-layer)
5. [Presentation Layer](#presentation-layer)
6. [Dependency Injection](#dependency-injection)
7. [Validation Layer](#validation-layer)
8. [Error Handling](#error-handling)
9. [Project Structure](#project-structure)

---

## Architecture Overview

### Clean Architecture Principles
```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│   (API Routes, Controllers, DTOs)       │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│          Domain Layer                   │
│  (Entities, Use Cases, Interfaces)      │
│         [Business Logic]                │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│           Data Layer                    │
│  (Repositories, Supabase, DTOs, Mappers)│
└─────────────────────────────────────────┘
```

### Dependency Rule
- **Domain** depends on nothing (pure Kotlin)
- **Data** depends only on Domain
- **Presentation** depends on Domain (and indirectly on Data through DI)

---

## Layer Structure

### 1. Domain Layer (`domain/`)
**Purpose:** Business logic, entities, and use cases (framework-agnostic)

```
domain/
├── entities/          # Business entities
├── usecases/          # Business operations
├── repositories/      # Repository interfaces
├── exceptions/        # Domain-specific exceptions
└── common/            # Shared domain types (Result, ValueObjects)
```

### 2. Data Layer (`data/`)
**Purpose:** Data access, external services, and persistence

```
data/
├── repositories/      # Repository implementations
├── datasources/       # Remote/Local data sources
│   ├── remote/        # Supabase API calls
│   └── local/         # Cache/Preferences
├── models/            # DTOs (Data Transfer Objects)
├── mappers/           # DTO ↔ Entity conversion
└── supabase/          # Supabase client config
```

### 3. Presentation Layer (`presentation/`)
**Purpose:** API endpoints, request/response handling

```
presentation/
├── routes/            # Ktor routes
├── controllers/       # Request handlers
├── dto/               # API request/response DTOs
├── middleware/        # Auth, logging, error handling
└── validators/        # Request validation
```

---

## Domain Layer

### Entities

All entities use `kotlin.time.Clock` for time operations.

#### **Category.kt**
```kotlin
package com.vibely.pos.domain.entities

import kotlin.time.Clock

data class Category(
    val id: CategoryId,
    val name: String,
    val description: String?,
    val color: ColorHex,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
) {
    val isDeleted: Boolean get() = deletedAt != null

    companion object {
        fun create(
            name: String,
            description: String?,
            color: ColorHex,
            clock: Clock = Clock.System
        ): Category {
            val now = clock.now().toEpochMilliseconds()
            return Category(
                id = CategoryId.generate(),
                name = name,
                description = description,
                color = color,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

// Value Objects
@JvmInline
value class CategoryId(val value: String) {
    companion object {
        fun generate(): CategoryId = CategoryId(uuid4().toString())
    }
}

@JvmInline
value class ColorHex(val value: String) {
    init {
        require(value.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
            "Invalid color hex format. Expected #RRGGBB"
        }
    }
}
```

#### **Product.kt**
```kotlin
package com.vibely.pos.domain.entities

import kotlin.time.Clock

data class Product(
    val id: ProductId,
    val name: String,
    val sku: SKU,
    val categoryId: CategoryId,
    val price: Money,
    val stock: Stock,
    val size: String?,
    val weight: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
) {
    val isLowStock: Boolean get() = stock.value < 20
    val isOutOfStock: Boolean get() = stock.value == 0
    val isDeleted: Boolean get() = deletedAt != null

    fun decreaseStock(quantity: Int): Product {
        require(quantity > 0) { "Quantity must be positive" }
        require(stock.value >= quantity) { "Insufficient stock" }
        return copy(stock = Stock(stock.value - quantity))
    }

    fun increaseStock(quantity: Int): Product {
        require(quantity > 0) { "Quantity must be positive" }
        return copy(stock = Stock(stock.value + quantity))
    }
}

@JvmInline
value class ProductId(val value: String)

@JvmInline
value class SKU(val value: String) {
    init {
        require(value.isNotBlank()) { "SKU cannot be blank" }
    }
}

@JvmInline
value class Stock(val value: Int) {
    init {
        require(value >= 0) { "Stock cannot be negative" }
    }
}

@JvmInline
value class Money(val value: Double) {
    init {
        require(value >= 0) { "Money cannot be negative" }
    }

    operator fun plus(other: Money) = Money(value + other.value)
    operator fun times(quantity: Int) = Money(value * quantity)
}
```

#### **Customer.kt**
```kotlin
package com.vibely.pos.domain.entities

import kotlin.time.Clock

data class Customer(
    val id: CustomerId,
    val name: String,
    val email: Email?,
    val phone: Phone,
    val loyaltyPoints: LoyaltyPoints,
    val totalSpent: Money,
    val visitCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
) {
    val tier: CustomerTier get() = CustomerTier.fromPoints(loyaltyPoints.value)
    val isDeleted: Boolean get() = deletedAt != null

    fun addPurchase(amount: Money, pointsEarned: Int): Customer {
        return copy(
            totalSpent = totalSpent + amount,
            loyaltyPoints = LoyaltyPoints(loyaltyPoints.value + pointsEarned),
            visitCount = visitCount + 1
        )
    }
}

@JvmInline
value class CustomerId(val value: String)

@JvmInline
value class Email(val value: String) {
    init {
        require(value.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
            "Invalid email format"
        }
    }
}

@JvmInline
value class Phone(val value: String) {
    init {
        require(value.isNotBlank()) { "Phone cannot be blank" }
    }
}

@JvmInline
value class LoyaltyPoints(val value: Int) {
    init {
        require(value >= 0) { "Loyalty points cannot be negative" }
    }
}

enum class CustomerTier {
    BRONZE, SILVER, GOLD;

    companion object {
        fun fromPoints(points: Int): CustomerTier = when {
            points >= 1000 -> GOLD
            points >= 500 -> SILVER
            else -> BRONZE
        }
    }
}
```

#### **Supplier.kt**
```kotlin
package com.vibely.pos.domain.entities

data class Supplier(
    val id: SupplierId,
    val name: String,
    val contactPerson: String,
    val email: Email,
    val phone: Phone,
    val address: String?,
    val status: SupplierStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
) {
    val isActive: Boolean get() = status == SupplierStatus.ACTIVE
    val isDeleted: Boolean get() = deletedAt != null
}

@JvmInline
value class SupplierId(val value: String)

enum class SupplierStatus {
    ACTIVE, INACTIVE
}
```

#### **PurchaseOrder.kt**
```kotlin
package com.vibely.pos.domain.entities

import kotlin.time.Clock

data class PurchaseOrder(
    val id: PurchaseOrderId,
    val poNumber: PONumber,
    val supplierId: SupplierId,
    val orderDate: Long, // Date as epoch milliseconds
    val totalItems: Int,
    val totalAmount: Money,
    val status: PurchaseOrderStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
) {
    val isPending: Boolean get() = status == PurchaseOrderStatus.PENDING
    val isReceived: Boolean get() = status == PurchaseOrderStatus.RECEIVED
    val isCancelled: Boolean get() = status == PurchaseOrderStatus.CANCELLED
    val isDeleted: Boolean get() = deletedAt != null

    fun receive(): PurchaseOrder {
        require(isPending) { "Only pending orders can be received" }
        return copy(status = PurchaseOrderStatus.RECEIVED)
    }

    fun cancel(): PurchaseOrder {
        require(isPending) { "Only pending orders can be cancelled" }
        return copy(status = PurchaseOrderStatus.CANCELLED)
    }
}

@JvmInline
value class PurchaseOrderId(val value: String)

@JvmInline
value class PONumber(val value: String) {
    init {
        require(value.matches(Regex("^PO-\\d{4}-\\d{3}$"))) {
            "Invalid PO number format. Expected PO-YYYY-NNN"
        }
    }
}

enum class PurchaseOrderStatus {
    PENDING, RECEIVED, CANCELLED
}
```

#### **User.kt**
```kotlin
package com.vibely.pos.domain.entities

data class User(
    val id: UserId,
    val name: String,
    val email: Email,
    val role: UserRole,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
) {
    val isAdmin: Boolean get() = role == UserRole.ADMIN
    val isManager: Boolean get() = role == UserRole.MANAGER
    val isCashier: Boolean get() = role == UserRole.CASHIER
    val isDeleted: Boolean get() = deletedAt != null

    fun canManageUsers(): Boolean = isAdmin || isManager
    fun canProcessSales(): Boolean = true // All roles can process sales
}

@JvmInline
value class UserId(val value: String)

enum class UserRole {
    ADMIN, CASHIER, MANAGER
}
```

#### **Sale.kt**
```kotlin
package com.vibely.pos.domain.entities

import kotlin.time.Clock

data class Sale(
    val id: SaleId,
    val invoiceNumber: InvoiceNumber,
    val customerId: CustomerId?,
    val cashierId: UserId,
    val saleDate: Long, // Date as epoch milliseconds
    val saleTime: String, // HH:mm format
    val subtotal: Money,
    val tax: Money,
    val total: Money,
    val paymentMethod: PaymentMethod,
    val items: List<SaleItem>,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
    val isDeleted: Boolean get() = deletedAt != null

    companion object {
        fun create(
            customerId: CustomerId?,
            cashierId: UserId,
            items: List<SaleItem>,
            paymentMethod: PaymentMethod,
            taxRate: Double = 0.0,
            clock: Clock = Clock.System
        ): Sale {
            val subtotal = items.fold(Money(0.0)) { acc, item -> acc + item.subtotal }
            val tax = Money(subtotal.value * taxRate)
            val total = subtotal + tax
            val now = clock.now().toEpochMilliseconds()

            return Sale(
                id = SaleId.generate(),
                invoiceNumber = InvoiceNumber.generate(clock),
                customerId = customerId,
                cashierId = cashierId,
                saleDate = now,
                saleTime = formatTime(now),
                subtotal = subtotal,
                tax = tax,
                total = total,
                paymentMethod = paymentMethod,
                items = items,
                createdAt = now,
                updatedAt = now
            )
        }

        private fun formatTime(epochMillis: Long): String {
            // Format as HH:mm - implementation depends on platform
            return "00:00" // Placeholder
        }
    }
}

@JvmInline
value class SaleId(val value: String) {
    companion object {
        fun generate(): SaleId = SaleId(uuid4().toString())
    }
}

@JvmInline
value class InvoiceNumber(val value: String) {
    init {
        require(value.matches(Regex("^INV-\\d{4}-\\d{4}$"))) {
            "Invalid invoice number format. Expected INV-YYYY-NNNN"
        }
    }

    companion object {
        fun generate(clock: Clock = Clock.System): InvoiceNumber {
            val year = 2026 // Extract from clock
            val sequence = 1 // Get from database
            return InvoiceNumber("INV-$year-${sequence.toString().padStart(4, '0')}")
        }
    }
}

enum class PaymentMethod {
    CASH, CARD, DIGITAL
}
```

#### **SaleItem.kt**
```kotlin
package com.vibely.pos.domain.entities

data class SaleItem(
    val id: SaleItemId,
    val saleId: SaleId,
    val productId: ProductId,
    val quantity: Int,
    val unitPrice: Money,
    val subtotal: Money,
    val createdAt: Long
) {
    init {
        require(quantity > 0) { "Quantity must be positive" }
        require(subtotal.value == unitPrice.value * quantity) {
            "Subtotal must equal unitPrice * quantity"
        }
    }

    companion object {
        fun create(
            saleId: SaleId,
            productId: ProductId,
            quantity: Int,
            unitPrice: Money,
            clock: kotlin.time.Clock = kotlin.time.Clock.System
        ): SaleItem {
            return SaleItem(
                id = SaleItemId.generate(),
                saleId = saleId,
                productId = productId,
                quantity = quantity,
                unitPrice = unitPrice,
                subtotal = unitPrice * quantity,
                createdAt = clock.now().toEpochMilliseconds()
            )
        }
    }
}

@JvmInline
value class SaleItemId(val value: String) {
    companion object {
        fun generate(): SaleItemId = SaleItemId(uuid4().toString())
    }
}
```

### Repository Interfaces

```kotlin
package com.vibely.pos.domain.repositories

import com.vibely.pos.domain.entities.*
import com.vibely.pos.domain.common.Result

interface CategoryRepository {
    suspend fun findAll(): Result<List<Category>>
    suspend fun findById(id: CategoryId): Result<Category?>
    suspend fun findByName(name: String): Result<Category?>
    suspend fun create(category: Category): Result<Category>
    suspend fun update(category: Category): Result<Category>
    suspend fun delete(id: CategoryId): Result<Unit>
}

interface ProductRepository {
    suspend fun findAll(includeDeleted: Boolean = false): Result<List<Product>>
    suspend fun findById(id: ProductId): Result<Product?>
    suspend fun findBySku(sku: SKU): Result<Product?>
    suspend fun findByCategory(categoryId: CategoryId): Result<List<Product>>
    suspend fun findLowStock(threshold: Int = 20): Result<List<Product>>
    suspend fun search(query: String): Result<List<Product>>
    suspend fun create(product: Product): Result<Product>
    suspend fun update(product: Product): Result<Product>
    suspend fun delete(id: ProductId): Result<Unit>
    suspend fun updateStock(id: ProductId, quantity: Int): Result<Product>
}

interface CustomerRepository {
    suspend fun findAll(): Result<List<Customer>>
    suspend fun findById(id: CustomerId): Result<Customer?>
    suspend fun findByPhone(phone: Phone): Result<Customer?>
    suspend fun findByEmail(email: Email): Result<Customer?>
    suspend fun create(customer: Customer): Result<Customer>
    suspend fun update(customer: Customer): Result<Customer>
    suspend fun delete(id: CustomerId): Result<Unit>
}

interface SupplierRepository {
    suspend fun findAll(activeOnly: Boolean = false): Result<List<Supplier>>
    suspend fun findById(id: SupplierId): Result<Supplier?>
    suspend fun create(supplier: Supplier): Result<Supplier>
    suspend fun update(supplier: Supplier): Result<Supplier>
    suspend fun delete(id: SupplierId): Result<Unit>
}

interface PurchaseOrderRepository {
    suspend fun findAll(): Result<List<PurchaseOrder>>
    suspend fun findById(id: PurchaseOrderId): Result<PurchaseOrder?>
    suspend fun findBySupplier(supplierId: SupplierId): Result<List<PurchaseOrder>>
    suspend fun findByStatus(status: PurchaseOrderStatus): Result<List<PurchaseOrder>>
    suspend fun create(purchaseOrder: PurchaseOrder): Result<PurchaseOrder>
    suspend fun update(purchaseOrder: PurchaseOrder): Result<PurchaseOrder>
    suspend fun delete(id: PurchaseOrderId): Result<Unit>
}

interface UserRepository {
    suspend fun findAll(): Result<List<User>>
    suspend fun findById(id: UserId): Result<User?>
    suspend fun findByEmail(email: Email): Result<User?>
    suspend fun create(user: User): Result<User>
    suspend fun update(user: User): Result<User>
    suspend fun delete(id: UserId): Result<Unit>
}

interface SaleRepository {
    suspend fun findAll(): Result<List<Sale>>
    suspend fun findById(id: SaleId): Result<Sale?>
    suspend fun findByInvoiceNumber(invoiceNumber: InvoiceNumber): Result<Sale?>
    suspend fun findByCustomer(customerId: CustomerId): Result<List<Sale>>
    suspend fun findByCashier(cashierId: UserId): Result<List<Sale>>
    suspend fun findByDateRange(startDate: Long, endDate: Long): Result<List<Sale>>
    suspend fun create(sale: Sale): Result<Sale>
    suspend fun delete(id: SaleId): Result<Unit>
}
```

### Use Cases

```kotlin
package com.vibely.pos.domain.usecases

import com.vibely.pos.domain.entities.*
import com.vibely.pos.domain.repositories.*
import com.vibely.pos.domain.common.Result

// Category Use Cases
class GetAllCategoriesUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        return repository.findAll()
    }
}

class CreateCategoryUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String?,
        color: String
    ): Result<Category> {
        return try {
            val colorHex = ColorHex(color)
            val category = Category.create(name, description, colorHex)
            repository.create(category)
        } catch (e: IllegalArgumentException) {
            Result.Error(ValidationException(e.message ?: "Invalid input"))
        }
    }
}

// Product Use Cases
class GetProductsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(includeDeleted: Boolean = false): Result<List<Product>> {
        return repository.findAll(includeDeleted)
    }
}

class GetLowStockProductsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(threshold: Int = 20): Result<List<Product>> {
        return repository.findLowStock(threshold)
    }
}

class CreateProductUseCase(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(
        name: String,
        sku: String,
        categoryId: String,
        price: Double,
        stock: Int,
        size: String?,
        weight: String?
    ): Result<Product> {
        return try {
            // Validate category exists
            val catId = CategoryId(categoryId)
            when (val categoryResult = categoryRepository.findById(catId)) {
                is Result.Success -> {
                    if (categoryResult.data == null) {
                        return Result.Error(NotFoundException("Category not found"))
                    }
                }
                is Result.Error -> return Result.Error(categoryResult.exception)
            }

            val product = Product(
                id = ProductId.generate(),
                name = name,
                sku = SKU(sku),
                categoryId = catId,
                price = Money(price),
                stock = Stock(stock),
                size = size,
                weight = weight,
                createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                updatedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
            )

            productRepository.create(product)
        } catch (e: IllegalArgumentException) {
            Result.Error(ValidationException(e.message ?: "Invalid input"))
        }
    }
}

class SearchProductsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(query: String): Result<List<Product>> {
        if (query.isBlank()) {
            return Result.Success(emptyList())
        }
        return repository.search(query)
    }
}

// Customer Use Cases
class GetCustomerByPhoneUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(phone: String): Result<Customer?> {
        return try {
            val phoneVO = Phone(phone)
            repository.findByPhone(phoneVO)
        } catch (e: IllegalArgumentException) {
            Result.Error(ValidationException(e.message ?: "Invalid phone"))
        }
    }
}

class CreateCustomerUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String?,
        phone: String
    ): Result<Customer> {
        return try {
            val customer = Customer(
                id = CustomerId.generate(),
                name = name,
                email = email?.let { Email(it) },
                phone = Phone(phone),
                loyaltyPoints = LoyaltyPoints(0),
                totalSpent = Money(0.0),
                visitCount = 0,
                createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                updatedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
            )
            repository.create(customer)
        } catch (e: IllegalArgumentException) {
            Result.Error(ValidationException(e.message ?: "Invalid input"))
        }
    }
}

// Sale Use Cases
class CreateSaleUseCase(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(
        customerId: String?,
        cashierId: String,
        items: List<SaleItemInput>,
        paymentMethod: PaymentMethod,
        taxRate: Double = 0.0
    ): Result<Sale> {
        return try {
            // Validate products and stock
            val saleItems = mutableListOf<SaleItem>()

            for (input in items) {
                val productId = ProductId(input.productId)
                val productResult = productRepository.findById(productId)

                when (productResult) {
                    is Result.Success -> {
                        val product = productResult.data
                            ?: return Result.Error(NotFoundException("Product not found"))

                        if (product.stock.value < input.quantity) {
                            return Result.Error(ValidationException(
                                "Insufficient stock for product ${product.name}"
                            ))
                        }

                        saleItems.add(
                            SaleItem.create(
                                saleId = SaleId.generate(),
                                productId = productId,
                                quantity = input.quantity,
                                unitPrice = product.price
                            )
                        )
                    }
                    is Result.Error -> return Result.Error(productResult.exception)
                }
            }

            // Create sale
            val sale = Sale.create(
                customerId = customerId?.let { CustomerId(it) },
                cashierId = UserId(cashierId),
                items = saleItems,
                paymentMethod = paymentMethod,
                taxRate = taxRate
            )

            // Save sale
            val saleResult = saleRepository.create(sale)

            // Update stock if sale was successful
            if (saleResult is Result.Success) {
                for (input in items) {
                    val productId = ProductId(input.productId)
                    val product = (productRepository.findById(productId) as Result.Success).data!!
                    productRepository.update(product.decreaseStock(input.quantity))
                }
            }

            saleResult
        } catch (e: IllegalArgumentException) {
            Result.Error(ValidationException(e.message ?: "Invalid input"))
        }
    }
}

data class SaleItemInput(
    val productId: String,
    val quantity: Int
)

// Purchase Order Use Cases
class CreatePurchaseOrderUseCase(
    private val repository: PurchaseOrderRepository,
    private val supplierRepository: SupplierRepository
) {
    suspend operator fun invoke(
        supplierId: String,
        totalItems: Int,
        totalAmount: Double
    ): Result<PurchaseOrder> {
        return try {
            // Validate supplier exists
            val supId = SupplierId(supplierId)
            when (val supplierResult = supplierRepository.findById(supId)) {
                is Result.Success -> {
                    if (supplierResult.data == null) {
                        return Result.Error(NotFoundException("Supplier not found"))
                    }
                }
                is Result.Error -> return Result.Error(supplierResult.exception)
            }

            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val purchaseOrder = PurchaseOrder(
                id = PurchaseOrderId.generate(),
                poNumber = PONumber.generate(),
                supplierId = supId,
                orderDate = now,
                totalItems = totalItems,
                totalAmount = Money(totalAmount),
                status = PurchaseOrderStatus.PENDING,
                createdAt = now,
                updatedAt = now
            )

            repository.create(purchaseOrder)
        } catch (e: IllegalArgumentException) {
            Result.Error(ValidationException(e.message ?: "Invalid input"))
        }
    }
}

class ReceivePurchaseOrderUseCase(
    private val repository: PurchaseOrderRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        purchaseOrderId: String,
        productStockUpdates: Map<String, Int>
    ): Result<PurchaseOrder> {
        return try {
            val poId = PurchaseOrderId(purchaseOrderId)
            val poResult = repository.findById(poId)

            when (poResult) {
                is Result.Success -> {
                    val po = poResult.data
                        ?: return Result.Error(NotFoundException("Purchase order not found"))

                    val receivedPO = po.receive()
                    val updateResult = repository.update(receivedPO)

                    // Update product stock
                    if (updateResult is Result.Success) {
                        for ((productId, quantity) in productStockUpdates) {
                            val prodId = ProductId(productId)
                            val product = (productRepository.findById(prodId) as Result.Success).data!!
                            productRepository.update(product.increaseStock(quantity))
                        }
                    }

                    updateResult
                }
                is Result.Error -> Result.Error(poResult.exception)
            }
        } catch (e: IllegalArgumentException) {
            Result.Error(ValidationException(e.message ?: "Invalid input"))
        }
    }
}
```

### Domain Exceptions

```kotlin
package com.vibely.pos.domain.exceptions

sealed class DomainException(message: String) : Exception(message)

class ValidationException(message: String) : DomainException(message)
class NotFoundException(message: String) : DomainException(message)
class InsufficientStockException(message: String) : DomainException(message)
class DuplicateEntityException(message: String) : DomainException(message)
class UnauthorizedException(message: String) : DomainException(message)
class BusinessRuleViolationException(message: String) : DomainException(message)
```

### Common Types

```kotlin
package com.vibely.pos.domain.common

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
        }
    }

    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> {
        return when (this) {
            is Success -> transform(data)
            is Error -> Error(exception)
        }
    }
}
```

---

## Data Layer

### Supabase Client Configuration

```kotlin
package com.vibely.pos.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.SupabaseClient

object SupabaseConfig {
    fun create(url: String, key: String): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            install(Postgrest)
        }
    }
}
```

### Data Transfer Objects (DTOs)

```kotlin
package com.vibely.pos.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val description: String?,
    val color: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String? = null
)

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val sku: String,
    val category_id: String,
    val price: String, // Decimal as String
    val stock: Int,
    val size: String?,
    val weight: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String? = null
)

@Serializable
data class CustomerDto(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String,
    val loyalty_points: Int,
    val total_spent: String, // Decimal as String
    val visit_count: Int,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String? = null
)

@Serializable
data class SupplierDto(
    val id: String,
    val name: String,
    val contact_person: String,
    val email: String,
    val phone: String,
    val address: String?,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String? = null
)

@Serializable
data class PurchaseOrderDto(
    val id: String,
    val po_number: String,
    val supplier_id: String,
    val order_date: String,
    val total_items: Int,
    val total_amount: String,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String? = null
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String? = null
)

@Serializable
data class SaleDto(
    val id: String,
    val invoice_number: String,
    val customer_id: String?,
    val cashier_id: String,
    val sale_date: String,
    val sale_time: String,
    val subtotal: String,
    val tax: String,
    val total: String,
    val payment_method: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String? = null
)

@Serializable
data class SaleItemDto(
    val id: String,
    val sale_id: String,
    val product_id: String,
    val quantity: Int,
    val unit_price: String,
    val subtotal: String,
    val created_at: String
)
```

### Mappers

```kotlin
package com.vibely.pos.data.mappers

import com.vibely.pos.domain.entities.*
import com.vibely.pos.data.models.*
import java.time.Instant

object CategoryMapper {
    fun toDomain(dto: CategoryDto): Category {
        return Category(
            id = CategoryId(dto.id),
            name = dto.name,
            description = dto.description,
            color = ColorHex(dto.color),
            createdAt = Instant.parse(dto.created_at).toEpochMilli(),
            updatedAt = Instant.parse(dto.updated_at).toEpochMilli(),
            deletedAt = dto.deleted_at?.let { Instant.parse(it).toEpochMilli() }
        )
    }

    fun toDto(entity: Category): CategoryDto {
        return CategoryDto(
            id = entity.id.value,
            name = entity.name,
            description = entity.description,
            color = entity.color.value,
            created_at = Instant.ofEpochMilli(entity.createdAt).toString(),
            updated_at = Instant.ofEpochMilli(entity.updatedAt).toString(),
            deleted_at = entity.deletedAt?.let { Instant.ofEpochMilli(it).toString() }
        )
    }
}

object ProductMapper {
    fun toDomain(dto: ProductDto): Product {
        return Product(
            id = ProductId(dto.id),
            name = dto.name,
            sku = SKU(dto.sku),
            categoryId = CategoryId(dto.category_id),
            price = Money(dto.price.toDouble()),
            stock = Stock(dto.stock),
            size = dto.size,
            weight = dto.weight,
            createdAt = Instant.parse(dto.created_at).toEpochMilli(),
            updatedAt = Instant.parse(dto.updated_at).toEpochMilli(),
            deletedAt = dto.deleted_at?.let { Instant.parse(it).toEpochMilli() }
        )
    }

    fun toDto(entity: Product): ProductDto {
        return ProductDto(
            id = entity.id.value,
            name = entity.name,
            sku = entity.sku.value,
            category_id = entity.categoryId.value,
            price = entity.price.value.toString(),
            stock = entity.stock.value,
            size = entity.size,
            weight = entity.weight,
            created_at = Instant.ofEpochMilli(entity.createdAt).toString(),
            updated_at = Instant.ofEpochMilli(entity.updatedAt).toString(),
            deleted_at = entity.deletedAt?.let { Instant.ofEpochMilli(it).toString() }
        )
    }
}

object CustomerMapper {
    fun toDomain(dto: CustomerDto): Customer {
        return Customer(
            id = CustomerId(dto.id),
            name = dto.name,
            email = dto.email?.let { Email(it) },
            phone = Phone(dto.phone),
            loyaltyPoints = LoyaltyPoints(dto.loyalty_points),
            totalSpent = Money(dto.total_spent.toDouble()),
            visitCount = dto.visit_count,
            createdAt = Instant.parse(dto.created_at).toEpochMilli(),
            updatedAt = Instant.parse(dto.updated_at).toEpochMilli(),
            deletedAt = dto.deleted_at?.let { Instant.parse(it).toEpochMilli() }
        )
    }

    fun toDto(entity: Customer): CustomerDto {
        return CustomerDto(
            id = entity.id.value,
            name = entity.name,
            email = entity.email?.value,
            phone = entity.phone.value,
            loyalty_points = entity.loyaltyPoints.value,
            total_spent = entity.totalSpent.value.toString(),
            visit_count = entity.visitCount,
            created_at = Instant.ofEpochMilli(entity.createdAt).toString(),
            updated_at = Instant.ofEpochMilli(entity.updatedAt).toString(),
            deleted_at = entity.deletedAt?.let { Instant.ofEpochMilli(it).toString() }
        )
    }
}

// Similar mappers for other entities...
```

### Repository Implementations

```kotlin
package com.vibely.pos.data.repositories

import com.vibely.pos.domain.entities.*
import com.vibely.pos.domain.repositories.ProductRepository
import com.vibely.pos.domain.common.Result
import com.vibely.pos.data.models.ProductDto
import com.vibely.pos.data.mappers.ProductMapper
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class ProductRepositoryImpl(
    private val supabase: SupabaseClient
) : ProductRepository {

    override suspend fun findAll(includeDeleted: Boolean): Result<List<Product>> {
        return try {
            val query = supabase.from("products")
                .select()

            if (!includeDeleted) {
                query.filter {
                    isNull("deleted_at")
                }
            }

            val dtos = query.decodeList<ProductDto>()
            Result.Success(dtos.map { ProductMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun findById(id: ProductId): Result<Product?> {
        return try {
            val dto = supabase.from("products")
                .select {
                    filter {
                        eq("id", id.value)
                        isNull("deleted_at")
                    }
                }
                .decodeSingleOrNull<ProductDto>()

            Result.Success(dto?.let { ProductMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun findBySku(sku: SKU): Result<Product?> {
        return try {
            val dto = supabase.from("products")
                .select {
                    filter {
                        eq("sku", sku.value)
                        isNull("deleted_at")
                    }
                }
                .decodeSingleOrNull<ProductDto>()

            Result.Success(dto?.let { ProductMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun findByCategory(categoryId: CategoryId): Result<List<Product>> {
        return try {
            val dtos = supabase.from("products")
                .select {
                    filter {
                        eq("category_id", categoryId.value)
                        isNull("deleted_at")
                    }
                }
                .decodeList<ProductDto>()

            Result.Success(dtos.map { ProductMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun findLowStock(threshold: Int): Result<List<Product>> {
        return try {
            val dtos = supabase.from("products")
                .select {
                    filter {
                        lt("stock", threshold)
                        isNull("deleted_at")
                    }
                    order("stock", ascending = true)
                }
                .decodeList<ProductDto>()

            Result.Success(dtos.map { ProductMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun search(query: String): Result<List<Product>> {
        return try {
            val dtos = supabase.from("products")
                .select {
                    filter {
                        or {
                            ilike("name", "%$query%")
                            ilike("sku", "%$query%")
                        }
                        isNull("deleted_at")
                    }
                }
                .decodeList<ProductDto>()

            Result.Success(dtos.map { ProductMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun create(product: Product): Result<Product> {
        return try {
            val dto = ProductMapper.toDto(product)
            val created = supabase.from("products")
                .insert(dto)
                .decodeSingle<ProductDto>()

            Result.Success(ProductMapper.toDomain(created))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun update(product: Product): Result<Product> {
        return try {
            val dto = ProductMapper.toDto(product)
            val updated = supabase.from("products")
                .update(dto) {
                    filter {
                        eq("id", product.id.value)
                    }
                }
                .decodeSingle<ProductDto>()

            Result.Success(ProductMapper.toDomain(updated))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun delete(id: ProductId): Result<Unit> {
        return try {
            supabase.from("products")
                .update(mapOf("deleted_at" to "NOW()")) {
                    filter {
                        eq("id", id.value)
                    }
                }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateStock(id: ProductId, quantity: Int): Result<Product> {
        return try {
            val product = (findById(id) as Result.Success).data
                ?: return Result.Error(NotFoundException("Product not found"))

            val updated = product.copy(stock = Stock(quantity))
            update(updated)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

// Similar implementations for other repositories...
```

---

## Presentation Layer

### API DTOs

```kotlin
package com.vibely.pos.presentation.dto

import kotlinx.serialization.Serializable

// Request DTOs
@Serializable
data class CreateCategoryRequest(
    val name: String,
    val description: String?,
    val color: String
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val sku: String,
    val categoryId: String,
    val price: Double,
    val stock: Int,
    val size: String?,
    val weight: String?
)

@Serializable
data class CreateCustomerRequest(
    val name: String,
    val email: String?,
    val phone: String
)

@Serializable
data class CreateSaleRequest(
    val customerId: String?,
    val cashierId: String,
    val items: List<SaleItemRequest>,
    val paymentMethod: String,
    val taxRate: Double = 0.0
)

@Serializable
data class SaleItemRequest(
    val productId: String,
    val quantity: Int
)

// Response DTOs
@Serializable
data class CategoryResponse(
    val id: String,
    val name: String,
    val description: String?,
    val color: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    val sku: String,
    val categoryId: String,
    val price: Double,
    val stock: Int,
    val size: String?,
    val weight: String?,
    val isLowStock: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class CustomerResponse(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String,
    val loyaltyPoints: Int,
    val tier: String,
    val totalSpent: Double,
    val visitCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class SaleResponse(
    val id: String,
    val invoiceNumber: String,
    val customerId: String?,
    val cashierId: String,
    val saleDate: Long,
    val saleTime: String,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val paymentMethod: String,
    val items: List<SaleItemResponse>,
    val createdAt: Long
)

@Serializable
data class SaleItemResponse(
    val id: String,
    val productId: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
)

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)
```

### Controllers

```kotlin
package com.vibely.pos.presentation.controllers

import com.vibely.pos.domain.usecases.*
import com.vibely.pos.domain.common.Result
import com.vibely.pos.presentation.dto.*
import com.vibely.pos.presentation.mappers.PresentationMapper

class ProductController(
    private val getProductsUseCase: GetProductsUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val getLowStockProductsUseCase: GetLowStockProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase
) {
    suspend fun getAllProducts(): ApiResponse<List<ProductResponse>> {
        return when (val result = getProductsUseCase()) {
            is Result.Success -> ApiResponse(
                success = true,
                data = result.data.map { PresentationMapper.toProductResponse(it) }
            )
            is Result.Error -> ApiResponse(
                success = false,
                error = ErrorResponse(
                    code = "FETCH_ERROR",
                    message = result.exception.message ?: "Failed to fetch products"
                )
            )
        }
    }

    suspend fun getLowStockProducts(): ApiResponse<List<ProductResponse>> {
        return when (val result = getLowStockProductsUseCase()) {
            is Result.Success -> ApiResponse(
                success = true,
                data = result.data.map { PresentationMapper.toProductResponse(it) }
            )
            is Result.Error -> ApiResponse(
                success = false,
                error = ErrorResponse(
                    code = "FETCH_ERROR",
                    message = result.exception.message ?: "Failed to fetch low stock products"
                )
            )
        }
    }

    suspend fun createProduct(request: CreateProductRequest): ApiResponse<ProductResponse> {
        return when (val result = createProductUseCase(
            name = request.name,
            sku = request.sku,
            categoryId = request.categoryId,
            price = request.price,
            stock = request.stock,
            size = request.size,
            weight = request.weight
        )) {
            is Result.Success -> ApiResponse(
                success = true,
                data = PresentationMapper.toProductResponse(result.data)
            )
            is Result.Error -> ApiResponse(
                success = false,
                error = ErrorResponse(
                    code = "CREATE_ERROR",
                    message = result.exception.message ?: "Failed to create product"
                )
            )
        }
    }

    suspend fun searchProducts(query: String): ApiResponse<List<ProductResponse>> {
        return when (val result = searchProductsUseCase(query)) {
            is Result.Success -> ApiResponse(
                success = true,
                data = result.data.map { PresentationMapper.toProductResponse(it) }
            )
            is Result.Error -> ApiResponse(
                success = false,
                error = ErrorResponse(
                    code = "SEARCH_ERROR",
                    message = result.exception.message ?: "Failed to search products"
                )
            )
        }
    }
}

// Similar controllers for other resources...
```

### Ktor Routes

```kotlin
package com.vibely.pos.presentation.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import com.vibely.pos.presentation.controllers.ProductController
import com.vibely.pos.presentation.dto.CreateProductRequest
import org.koin.ktor.ext.inject

fun Route.productRoutes() {
    val controller by inject<ProductController>()

    route("/products") {
        get {
            val response = controller.getAllProducts()
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,
                response
            )
        }

        get("/low-stock") {
            val response = controller.getLowStockProducts()
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,
                response
            )
        }

        get("/search") {
            val query = call.request.queryParameters["q"] ?: ""
            val response = controller.searchProducts(query)
            call.respond(
                if (response.success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,
                response
            )
        }

        post {
            val request = call.receive<CreateProductRequest>()
            val response = controller.createProduct(request)
            call.respond(
                if (response.success) HttpStatusCode.Created else HttpStatusCode.BadRequest,
                response
            )
        }
    }
}

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            productRoutes()
            // Add other routes...
        }
    }
}
```

---

## Dependency Injection

### Koin Modules

```kotlin
package com.vibely.pos.di

import org.koin.dsl.module
import com.vibely.pos.data.supabase.SupabaseConfig
import com.vibely.pos.data.repositories.*
import com.vibely.pos.domain.repositories.*
import com.vibely.pos.domain.usecases.*
import com.vibely.pos.presentation.controllers.*

val dataModule = module {
    // Supabase Client
    single {
        SupabaseConfig.create(
            url = getProperty("supabase.url"),
            key = getProperty("supabase.key")
        )
    }

    // Repositories
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<ProductRepository> { ProductRepositoryImpl(get()) }
    single<CustomerRepository> { CustomerRepositoryImpl(get()) }
    single<SupplierRepository> { SupplierRepositoryImpl(get()) }
    single<PurchaseOrderRepository> { PurchaseOrderRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<SaleRepository> { SaleRepositoryImpl(get()) }
}

val domainModule = module {
    // Category Use Cases
    factory { GetAllCategoriesUseCase(get()) }
    factory { CreateCategoryUseCase(get()) }

    // Product Use Cases
    factory { GetProductsUseCase(get()) }
    factory { GetLowStockProductsUseCase(get()) }
    factory { CreateProductUseCase(get(), get()) }
    factory { SearchProductsUseCase(get()) }

    // Customer Use Cases
    factory { GetCustomerByPhoneUseCase(get()) }
    factory { CreateCustomerUseCase(get()) }

    // Sale Use Cases
    factory { CreateSaleUseCase(get(), get(), get()) }

    // Purchase Order Use Cases
    factory { CreatePurchaseOrderUseCase(get(), get()) }
    factory { ReceivePurchaseOrderUseCase(get(), get()) }
}

val presentationModule = module {
    // Controllers
    single { ProductController(get(), get(), get(), get()) }
    // Add other controllers...
}

val appModule = listOf(dataModule, domainModule, presentationModule)
```

### Application Setup

```kotlin
package com.vibely.pos

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import com.vibely.pos.di.appModule
import com.vibely.pos.presentation.routes.configureRouting

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Install Koin
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    // Install Content Negotiation
    install(ContentNegotiation) {
        json()
    }

    // Configure Routing
    configureRouting()
}
```

---

## Validation Layer

### Validators

```kotlin
package com.vibely.pos.presentation.validators

import com.vibely.pos.presentation.dto.*

object ProductValidator {
    fun validate(request: CreateProductRequest): List<String> {
        val errors = mutableListOf<String>()

        if (request.name.isBlank()) {
            errors.add("Product name cannot be blank")
        }

        if (request.sku.isBlank()) {
            errors.add("SKU cannot be blank")
        }

        if (request.price < 0) {
            errors.add("Price cannot be negative")
        }

        if (request.stock < 0) {
            errors.add("Stock cannot be negative")
        }

        if (!request.color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
            errors.add("Invalid color format. Expected #RRGGBB")
        }

        return errors
    }
}

object CustomerValidator {
    fun validate(request: CreateCustomerRequest): List<String> {
        val errors = mutableListOf<String>()

        if (request.name.isBlank()) {
            errors.add("Customer name cannot be blank")
        }

        if (request.phone.isBlank()) {
            errors.add("Phone cannot be blank")
        }

        request.email?.let { email ->
            if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
                errors.add("Invalid email format")
            }
        }

        return errors
    }
}

object SaleValidator {
    fun validate(request: CreateSaleRequest): List<String> {
        val errors = mutableListOf<String>()

        if (request.items.isEmpty()) {
            errors.add("Sale must have at least one item")
        }

        request.items.forEach { item ->
            if (item.quantity <= 0) {
                errors.add("Item quantity must be positive")
            }
        }

        if (request.taxRate < 0 || request.taxRate > 1) {
            errors.add("Tax rate must be between 0 and 1")
        }

        return errors
    }
}
```

### Validation Middleware

```kotlin
package com.vibely.pos.presentation.middleware

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import com.vibely.pos.presentation.dto.*
import com.vibely.pos.presentation.validators.*

suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.validateRequest(
    validator: (T) -> List<String>,
    crossinline onValid: suspend (T) -> Unit
) {
    val request = call.receive<T>()
    val errors = validator(request)

    if (errors.isNotEmpty()) {
        call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Nothing>(
                success = false,
                error = ErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = "Request validation failed",
                    details = errors.associateBy { it }
                )
            )
        )
    } else {
        onValid(request)
    }
}
```

---

## Error Handling

### Global Error Handler

```kotlin
package com.vibely.pos.presentation.middleware

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*
import com.vibely.pos.domain.exceptions.*
import com.vibely.pos.presentation.dto.*

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(
                    success = false,
                    error = ErrorResponse(
                        code = "VALIDATION_ERROR",
                        message = cause.message ?: "Validation failed"
                    )
                )
            )
        }

        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Nothing>(
                    success = false,
                    error = ErrorResponse(
                        code = "NOT_FOUND",
                        message = cause.message ?: "Resource not found"
                    )
                )
            )
        }

        exception<InsufficientStockException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ApiResponse<Nothing>(
                    success = false,
                    error = ErrorResponse(
                        code = "INSUFFICIENT_STOCK",
                        message = cause.message ?: "Insufficient stock"
                    )
                )
            )
        }

        exception<DuplicateEntityException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ApiResponse<Nothing>(
                    success = false,
                    error = ErrorResponse(
                        code = "DUPLICATE_ENTITY",
                        message = cause.message ?: "Entity already exists"
                    )
                )
            )
        }

        exception<UnauthorizedException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse<Nothing>(
                    success = false,
                    error = ErrorResponse(
                        code = "UNAUTHORIZED",
                        message = cause.message ?: "Unauthorized access"
                    )
                )
            )
        }

        exception<BusinessRuleViolationException> { call, cause ->
            call.respond(
                HttpStatusCode.UnprocessableEntity,
                ApiResponse<Nothing>(
                    success = false,
                    error = ErrorResponse(
                        code = "BUSINESS_RULE_VIOLATION",
                        message = cause.message ?: "Business rule violated"
                    )
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(
                    success = false,
                    error = ErrorResponse(
                        code = "INTERNAL_ERROR",
                        message = "An internal error occurred"
                    )
                )
            )
        }
    }
}
```

### Logging

```kotlin
package com.vibely.pos.presentation.middleware

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val status = call.response.status()
            val method = call.request.httpMethod.value
            val path = call.request.path()
            "$method $path -> $status"
        }
    }
}
```

---

## Project Structure

```
vibely-pos/
├── src/
│   ├── commonMain/
│   │   └── kotlin/
│   │       └── com.vibely.pos/
│   │           ├── domain/
│   │           │   ├── entities/
│   │           │   │   ├── Category.kt
│   │           │   │   ├── Product.kt
│   │           │   │   ├── Customer.kt
│   │           │   │   ├── Supplier.kt
│   │           │   │   ├── PurchaseOrder.kt
│   │           │   │   ├── User.kt
│   │           │   │   ├── Sale.kt
│   │           │   │   └── SaleItem.kt
│   │           │   ├── repositories/
│   │           │   │   ├── CategoryRepository.kt
│   │           │   │   ├── ProductRepository.kt
│   │           │   │   ├── CustomerRepository.kt
│   │           │   │   ├── SupplierRepository.kt
│   │           │   │   ├── PurchaseOrderRepository.kt
│   │           │   │   ├── UserRepository.kt
│   │           │   │   └── SaleRepository.kt
│   │           │   ├── usecases/
│   │           │   │   ├── category/
│   │           │   │   ├── product/
│   │           │   │   ├── customer/
│   │           │   │   ├── supplier/
│   │           │   │   ├── purchase_order/
│   │           │   │   ├── user/
│   │           │   │   └── sale/
│   │           │   ├── exceptions/
│   │           │   │   └── DomainExceptions.kt
│   │           │   └── common/
│   │           │       └── Result.kt
│   │           ├── data/
│   │           │   ├── repositories/
│   │           │   │   ├── CategoryRepositoryImpl.kt
│   │           │   │   ├── ProductRepositoryImpl.kt
│   │           │   │   └── ...
│   │           │   ├── models/
│   │           │   │   ├── CategoryDto.kt
│   │           │   │   ├── ProductDto.kt
│   │           │   │   └── ...
│   │           │   ├── mappers/
│   │           │   │   ├── CategoryMapper.kt
│   │           │   │   ├── ProductMapper.kt
│   │           │   │   └── ...
│   │           │   └── supabase/
│   │           │       └── SupabaseConfig.kt
│   │           └── presentation/
│   │               ├── routes/
│   │               │   ├── CategoryRoutes.kt
│   │               │   ├── ProductRoutes.kt
│   │               │   └── ...
│   │               ├── controllers/
│   │               │   ├── CategoryController.kt
│   │               │   ├── ProductController.kt
│   │               │   └── ...
│   │               ├── dto/
│   │               │   ├── Requests.kt
│   │               │   └── Responses.kt
│   │               ├── validators/
│   │               │   └── Validators.kt
│   │               └── middleware/
│   │                   ├── ErrorHandling.kt
│   │                   ├── Logging.kt
│   │                   └── Validation.kt
│   └── jvmMain/
│       └── kotlin/
│           └── com.vibely.pos/
│               ├── Application.kt
│               └── di/
│                   └── KoinModules.kt
├── resources/
│   └── application.conf
└── build.gradle.kts
```

---

## Configuration

### application.conf
```hocon
ktor {
    deployment {
        port = 8080
        port = ${?PORT}
    }
    application {
        modules = [ com.vibely.pos.ApplicationKt.module ]
    }
}

supabase {
    url = ${SUPABASE_URL}
    key = ${SUPABASE_KEY}
}
```

---

## Summary

This clean architecture provides:

1. **Separation of Concerns**: Clear boundaries between layers
2. **Testability**: Each layer can be tested independently
3. **Maintainability**: Easy to modify without affecting other layers
4. **Scalability**: Easy to add new features
5. **Type Safety**: Value objects and strong typing prevent bugs
6. **Error Handling**: Comprehensive error handling at all layers
7. **Validation**: Request validation at presentation layer, business rules at domain layer
8. **Dependency Injection**: Clean dependency management with Koin

All entities use `kotlin.time.Clock` for time operations as specified.
