package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.inventory.entity.Category
import com.vibely.pos.shared.domain.inventory.repository.CategoryRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CreateProductUseCaseTest {

    private class FakeProductRepository : ProductRepository {
        val products = mutableMapOf<String, Product>()
        var searchResult: Result<List<Product>>? = null
        var shouldFail = false
        var failMessage = "Repository error"
        var failCode = "REPO_ERROR"
        var createdProduct: Product? = null

        override suspend fun search(query: String): Result<List<Product>> = searchResult ?: Result.Success(
            products.values.filter {
                it.sku == query
            },
        )

        override suspend fun getById(id: String): Result<Product> = products[id]?.let { Result.Success(it) }
            ?: Result.Error("Product not found", "NOT_FOUND")

        override suspend fun checkStock(productId: String, quantity: Int): Result<Boolean> {
            val product = products[productId] ?: return Result.Error("Product not found")
            return Result.Success(product.canSell(quantity))
        }

        override suspend fun getAll(
            categoryId: String?,
            isActive: Boolean?,
            lowStockOnly: Boolean,
            page: Int,
            pageSize: Int,
        ): Result<List<Product>> = Result.Success(products.values.toList())

        override suspend fun create(product: Product): Result<Product> = if (shouldFail) {
            Result.Error(failMessage, failCode)
        } else {
            createdProduct = product
            products[product.id] = product
            Result.Success(product)
        }

        override suspend fun update(product: Product): Result<Product> {
            products[product.id] = product
            return Result.Success(product)
        }

        override suspend fun delete(id: String): Result<Unit> {
            products.remove(id)
            return Result.Success(Unit)
        }
    }

    private class FakeCategoryRepository : CategoryRepository {
        val categories = mutableMapOf<String, Category>()
        var shouldFail = false
        var failMessage = "Category not found"
        var failCode = "NOT_FOUND"

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Category>> = Result.Success(categories.values.toList())

        override suspend fun getById(id: String): Result<Category> = if (shouldFail) {
            Result.Error(failMessage, failCode)
        } else {
            categories[id]?.let { Result.Success(it) }
                ?: Result.Error("Category not found", "NOT_FOUND")
        }

        override suspend fun create(category: Category): Result<Category> {
            categories[category.id] = category
            return Result.Success(category)
        }

        override suspend fun update(category: Category): Result<Category> {
            categories[category.id] = category
            return Result.Success(category)
        }

        override suspend fun delete(id: String): Result<Unit> {
            categories.remove(id)
            return Result.Success(Unit)
        }

        override suspend fun search(query: String): Result<List<Category>> = Result.Success(
            categories.values.filter {
                it.name.contains(query, ignoreCase = true)
            },
        )
    }

    @Test
    fun `creates product with valid data`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals("Test Product", result.data.name)
        assertEquals("SKU-001", result.data.sku)
        assertEquals(100.0, result.data.sellingPrice)
        assertEquals(10, result.data.currentStock)
    }

    @Test
    fun `returns error when name is blank`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("cannot be blank"))
    }

    @Test
    fun `returns error when name is whitespace only`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "   ",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("cannot be blank"))
    }

    @Test
    fun `returns error when name is too short`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "AB",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("must be between 3 and 100 characters"))
    }

    @Test
    fun `returns error when name is too long`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val longName = "A".repeat(101)
        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = longName,
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("must be between 3 and 100 characters"))
    }

    @Test
    fun `returns error when SKU is blank`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("SKU"))
        assertTrue(result.message.contains("cannot be blank"))
    }

    @Test
    fun `returns error when selling price is zero`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 0.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Selling price must be greater than 0"))
    }

    @Test
    fun `returns error when selling price is negative`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = -10.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Selling price must be greater than 0"))
    }

    @Test
    fun `returns error when cost price is negative`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = -50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Cost price cannot be negative"))
    }

    @Test
    fun `returns error when current stock is negative`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = -5,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Current stock cannot be negative"))
    }

    @Test
    fun `returns error when min stock level is negative`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = -2,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Minimum stock level cannot be negative"))
    }

    @Test
    fun `returns error when SKU already exists`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()

        val existingProduct = Product.create(
            id = "existing-prod",
            sku = "SKU-001",
            name = "Existing Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[existingProduct.id] = existingProduct

        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "New Product",
            costPrice = 60.0,
            sellingPrice = 120.0,
            currentStock = 5,
            minStockLevel = 1,
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("already exists"))
    }

    @Test
    fun `returns error when category not found`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
            categoryId = "non-existent-category",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Category not found"))
    }

    @Test
    fun `returns error when category is inactive`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()

        val inactiveCategory = Category.create(
            id = "cat-1",
            name = "Inactive Category",
            isActive = false,
        )
        fakeCategoryRepo.categories[inactiveCategory.id] = inactiveCategory

        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
            categoryId = "cat-1",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("inactive category"))
    }

    @Test
    fun `creates product successfully with valid category`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()

        val activeCategory = Category.create(
            id = "cat-1",
            name = "Active Category",
            isActive = true,
        )
        fakeCategoryRepo.categories[activeCategory.id] = activeCategory

        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
            categoryId = "cat-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals("cat-1", result.data.categoryId)
    }

    @Test
    fun `returns error when repository fails on create`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        fakeProductRepo.shouldFail = true
        fakeProductRepo.failMessage = "Database connection failed"

        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Error>(result)
        assertEquals("Database connection failed", result.message)
    }

    @Test
    fun `creates product with optional fields`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
            barcode = "1234567890123",
            description = "A test product description",
            unit = "kg",
            imageUrl = "https://example.com/image.jpg",
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals("1234567890123", result.data.barcode)
        assertEquals("A test product description", result.data.description)
        assertEquals("kg", result.data.unit)
        assertEquals("https://example.com/image.jpg", result.data.imageUrl)
    }

    @Test
    fun `allows zero current stock`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 0,
            minStockLevel = 2,
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals(0, result.data.currentStock)
    }

    @Test
    fun `allows zero min stock level`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 0,
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals(0, result.data.minStockLevel)
    }

    @Test
    fun `allows zero cost price`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeCategoryRepo = FakeCategoryRepository()
        val useCase = CreateProductUseCase(fakeProductRepo, fakeCategoryRepo)

        val result = useCase(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 0.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals(0.0, result.data.costPrice)
    }
}
