# Development Guide

Implementation roadmap and development tasks for Vibely POS.

> **Note:** This is an internal development guide. For contributing guidelines, see [CONTRIBUTING.md](../CONTRIBUTING.md).

**Timeline:** 16 weeks (4 phases)
**Target Platforms:** Desktop (JVM), Mobile (Android/iOS), Web (JS/Wasm)
**Status:** ✅ Phase 0 Complete (Weeks 1-2) | 🚧 Phase 1 Ready to Start

---

## Executive Summary

This implementation plan consolidates the work of 5 specialist teams (UI Designer, Data Engineer, Backend Architect, Mobile App Builder × 2) into a single, actionable development roadmap. The plan spans 16 weeks across 4 major phases, with clear deliverables, dependencies, and risk mitigation strategies.

### Architecture Validation ✅

**Excellent Cross-Layer Alignment:**
- ✅ **Database ↔ Domain:** 16 database tables map directly to 16 domain entities
- ✅ **Backend ↔ UI:** All 13 planned screens have corresponding backend services
- ✅ **Design ↔ Implementation:** 8 screens fully designed in Figma, ready for implementation
- ⚠️ **Design Gaps:** 5 critical screens need designs (Login, Dashboard, User Mgmt, Shift Mgmt, Settings)

**No Critical Conflicts:** The architecture is consistent across all layers. The only gaps are missing UI designs for 5 screens, which can be addressed in parallel with Phase 0 (foundation) work.

---

## Table of Contents

1. [Phase 0: Foundation Setup](#phase-0-foundation-setup-weeks-1-2-)
2. [Phase 1: Authentication & Dashboard](#phase-1-authentication--dashboard-weeks-3-4-)
3. [Phase 2: Core POS Features](#phase-2-core-pos-features-weeks-5-8-)
4. [Phase 3: Management Features](#phase-3-management-features-weeks-9-12-)
5. [Phase 4: Analytics & Polish](#phase-4-analytics--polish-weeks-13-16-)
6. [Development Sequence](#development-sequence-priority-order)
7. [Technical Dependencies](#technical-dependencies)
8. [Risk Assessment](#risk-assessment)
9. [Success Metrics](#success-metrics)
10. [Team Structure](#team-structure--responsibilities)
11. [Next Steps](#next-steps)

---

## Phase 0: Foundation Setup (Weeks 1-2) 🏗️
**Goal:** Set up project infrastructure, database, and core architecture

### Week 1: Project Initialization ✅ COMPLETE

- [x] **Create KMP Project Structure** ✅
  - [x] Use Kotlin Multiplatform Wizard or Compose Multiplatform template ✅
  - [x] Configure gradle/libs.versions.toml (see 05-PROJECT_STRUCTURE.md) ✅
  - [x] Set up modules: shared/, composeApp/, backend/ ✅
  - [x] Configure platform-specific source sets (commonMain, androidMain, desktopMain, wasmJsMain) ✅
  - [x] Add type-safe project references (no deprecated resources) ✅

- [x] **Configure Build Tools** ✅
  - [x] Set up Spotless for code formatting (ktlint) ✅
  - [x] Configure Detekt for static analysis (detekt.yml from 05-PROJECT_STRUCTURE.md) ✅
  - [x] Add .editorconfig for IDE consistency ✅
  - [x] Configure Gradle properties (org.gradle.jvmargs=-Xmx4096m) ✅
  - [x] Ensure builds succeed on all platforms (Desktop, Android, iOS, Web) ✅

- [x] **Version Control Setup** ✅
  - [x] Initialize Git repository ✅
  - [x] Create .gitignore (IDE files, build/, .gradle/, *.iml) ✅
  - [x] Set up branch strategy (main, develop, feature/*) ✅
  - [x] Create README.md with setup instructions ✅
  - [x] Configure pre-commit hooks (Spotless, Detekt) ✅

- [x] **CI/CD Pipeline** ✅
  - [x] Create GitHub Actions workflow (.github/workflows/ci.yml) ✅
  - [x] Add build verification for all platforms ✅
  - [x] Configure automated tests (unit, integration) ✅
  - [x] Add Detekt checks to pipeline ✅
  - [x] Set up code coverage reporting (Kover) ✅

### Week 2: Database & Backend Foundation ✅ COMPLETE

- [x] **Supabase Setup** ✅ COMPLETE
  - [x] Verify access to Supabase project (ID: jewqhojchyrmozxsrkoq) ✅
  - [x] Database fully implemented with 17 production-ready tables ✅
    - [x] Create all 8 ENUM types (8/8 created) ✅
    - [x] Create all 17 tables with constraints (17 tables: users, categories, products, customers, suppliers, sales, sale_items, payments, purchase_orders, purchase_order_items, inventory_transactions, cash_shifts, expenses, audit_logs, app_settings, notifications) ✅
    - [x] Add indexes for performance (88 indexes created) ✅
    - [x] Create views (3 views created: categories_with_counts, products_low_stock, sales_with_details) ✅
    - [x] Add triggers (12 triggers: update timestamps, customer stats) ✅
    - [x] Create helper functions (generate_invoice_number, customer tier calculations) ✅
  - [x] Configure Row Level Security (RLS) policies (37 policies across all 17 tables) ✅
  - [x] Create seed data for testing (10 products, 5 categories, 2 users) ✅
  - [x] Export database schema to SQL file (database_schema.sql created) ✅
  - [x] Test database connectivity from backend (verified via Supabase dashboard) ✅

- [x] **Backend API Setup (Ktor)** ✅ COMPLETE
  - [x] Create backend/ module with Ktor 3.0.1 ✅
  - [x] Create basic Application.kt structure ✅
  - [x] Configure Application.kt with plugins: ✅
    - [x] ContentNegotiation (kotlinx.serialization) ✅
    - [x] Authentication (JWT) ✅
    - [x] CORS ✅
    - [x] CallLogging ✅
    - [x] StatusPages (error handling) ✅
  - [x] Set up Supabase client configuration ✅
  - [x] Create health check endpoint (GET /health) ✅
  - [x] Configure logging (Logback) ✅
  - [x] Test local server startup (http://localhost:8080) ✅

- [x] **Domain Layer Foundation** ✅ COMPLETE
  - [x] Create shared/domain/ structure ✅
  - [x] Define Result<T> sealed class for error handling ✅
  - [x] Create common value objects (Money, SKU, Email, Phone) ✅
  - [x] Define core domain exceptions (DomainException, ValidationException) ✅
  - [x] Add kotlin.time.Clock usage (NOT deprecated kotlinx.datetime.Clock) ✅
  - [x] Implement all 16 domain models (User, Category, Product, Customer, Supplier, Sale, SaleItem, Payment, PurchaseOrder, PurchaseOrderItem, InventoryTransaction, CashShift, Expense, AuditLog, AppSettings, Notification) ✅

- [x] **Dependency Injection (Koin)** ✅ COMPLETE
  - [x] Configure Koin 4.0.0 in shared module ✅
  - [x] Create module structure (domainModule, dataModule, presentationModule) ✅
  - [x] Set up DI for Supabase client ✅
  - [x] Configure Koin for Compose (koin-compose) ✅

- [x] **UI Foundation (Compose Multiplatform)** ✅ COMPLETE
  - [x] Create basic App.kt with MaterialTheme ✅
  - [x] Implement theme system from 04-UI_ARCHITECTURE.md: ✅
    - [x] AppColors (Primary, Secondary, Neutral, Status colors) ✅
    - [x] AppTypography (H1-H4, Body, Label styles) ✅
    - [x] AppShapes (corner radius system) ✅
    - [x] AppTheme composable ✅
  - [x] Create reusable components: ✅
    - [x] AppButton (Primary, Secondary, Destructive variants) ✅
    - [x] AppTextField (with validation states) ✅
    - [x] AppCard (Elevated, Outlined) ✅
    - [x] AppToast/Snackbar (Success, Error, Warning, Info) ✅
  - [x] Set up navigation structure (sealed class NavDestination) ✅
  - [x] Create sample screen to verify theme rendering ✅

**Deliverables:**
- [x] Compilable KMP project on all platforms ✅
- [x] Database fully set up with seed data ✅ (17 tables, 8 ENUMs, seed data complete)
- [x] Backend health endpoint responding ✅
- [x] Theme system and basic components working ✅ (AppColors, AppTypography, AppShapes, reusable components)
- [x] CI/CD pipeline green (all checks passing) ✅
- [x] Domain models implemented (all 16 entities) ✅
- [x] Dependency injection configured (Koin 4.0.0) ✅

**Estimated Effort:** 80 hours (2 developers × 40 hours)
**✅ PHASE 0 COMPLETE - All deliverables achieved**

---

## Phase 1: Authentication & Dashboard (Weeks 3-4) 🔐
**Goal:** Implement user authentication and main dashboard screen
**Status:** Week 3 ✅ 100% COMPLETE | Week 4 ⚠️ 0% COMPLETE (PENDING)

### Week 3: Authentication System ✅ COMPLETE

- [x] **Domain Layer - Authentication** ✅
  - [x] Create User entity (id, email, username, role, status, created_at) ✅
  - [x] Create Credentials value object (email, password validation) ✅
  - [x] Define AuthRepository interface (login, logout, getCurrentUser, refreshToken) ✅
  - [x] Implement LoginUseCase with validations: ✅
    - [x] Email format validation ✅
    - [x] Password not empty ✅
    - [x] Return Result<User> or error ✅
  - [x] Implement LogoutUseCase ✅
  - [x] Implement GetCurrentUserUseCase ✅
  - [x] Implement RefreshTokenUseCase ✅

- [x] **Data Layer - Authentication** ✅
  - [x] Create UserDTO data class ✅
  - [x] Create UserMapper (DTO ↔ Entity) ✅
  - [x] Implement AuthRepositoryImpl using Supabase Auth: ✅
    - [x] signInWithPassword() ✅
    - [x] signOut() ✅
    - [x] getCurrentUser() ✅
  - [x] Configure JWT token storage (secure preferences) ✅
  - [x] Add token refresh logic ✅

- [x] **Backend API - Authentication** ✅
  - [x] POST /api/auth/login (email, password → JWT token) ✅
  - [x] POST /api/auth/logout (invalidate token) ✅
  - [x] GET /api/auth/me (get current user profile) ✅
  - [x] POST /api/auth/refresh (refresh JWT token) ✅
  - [x] Configure JWT authentication plugin (secret from env) ✅
  - [x] Add role-based authorization middleware ✅

- [x] **UI - Login Screen** ✅
  - [x] Create LoginScreen composable ✅
  - [x] Create LoginViewModel with StateFlow<LoginState> ✅
  - [x] Implement LoginState (Idle, Loading, Success, Error) ✅
  - [x] Add email TextField with validation ✅
  - [x] Add password TextField (obscured) with validation ✅
  - [x] Add "Remember Me" checkbox ✅
  - [x] Add "Forgot Password?" link (placeholder) ✅
  - [x] Add Login button with loading indicator ✅
  - [x] Show error toast on failed login ✅
  - [x] Navigate to Dashboard on success ✅

**Testing:** ✅ COMPLETE
- [x] Unit tests for LoginUseCase (valid/invalid credentials) ✅
- [x] Unit tests for LoginViewModel (state transitions) ✅
- [x] Unit tests for GetCurrentUserUseCase ✅
- [x] Unit tests for LogoutUseCase ✅
- [x] Unit tests for RefreshTokenUseCase ✅
- [x] Integration test for AuthRepositoryImpl ✅
- [x] E2E test: Login with valid credentials → Dashboard appears ✅

### Week 4: Dashboard Screen ✅ COMPLETE

- [x] **Domain Layer - Dashboard Data** ✅
  - [x] Create DashboardSummary entity (todaySales, todayTransactions, lowStockCount, activeShift) ✅
  - [x] Create RecentTransaction entity (id, invoiceNumber, totalAmount, status, saleDate, customerName) ✅
  - [x] Create LowStockProduct entity (id, sku, name, currentStock, minStockLevel, sellingPrice, categoryName) with AlertSeverity ✅
  - [x] Define DashboardRepository interface ✅
  - [x] Implement GetDashboardSummaryUseCase ✅
  - [x] Implement GetRecentTransactionsUseCase (limit 1-100, default 10) ✅
  - [x] Implement GetLowStockProductsUseCase (with severity sorting) ✅
  - [x] Register dashboard use cases in Koin DomainModule ✅

- [x] **Data Layer - Dashboard** ✅
  - [x] Implement DashboardRepositoryImpl ✅
  - [x] Create aggregation queries for today's sales/transactions ✅
  - [x] Add caching layer (5-minute TTL using kotlin.time.Clock) ✅
  - [x] Create DTOs: DashboardSummaryDTO, RecentTransactionDTO, LowStockProductDTO, ActiveShiftInfoDTO ✅
  - [x] Create mappers: DashboardSummaryMapper, RecentTransactionMapper, LowStockProductMapper ✅
  - [x] Implement RemoteDashboardDataSource ✅
  - [x] Register dashboard repository in Koin DataModule ✅

- [x] **Backend API - Dashboard** ✅
  - [x] GET /api/dashboard/summary (today's metrics, JWT protected) ✅
  - [x] GET /api/dashboard/recent-transactions (last 10, limit param, JWT protected) ✅
  - [x] GET /api/dashboard/low-stock (products below min_stock_level, JWT protected) ✅
  - [x] DashboardService with proper exception handling (RestException, SerializationException) ✅
  - [x] SQL NULL query fixed (using "is.null" for active shift check) ✅

- [x] **UI - Dashboard Screen** ✅
  - [x] Create DashboardScreen composable ✅
  - [x] Create DashboardViewModel with StateFlow ✅
  - [x] Create DashboardState with computed properties ✅
  - [x] Display metric cards:
    - [x] Today's Sales (💰 amount with PHP currency) ✅
    - [x] Transactions Count (🧾) ✅
    - [x] Low Stock Alerts (⚠️ count with warning badge) ✅
    - [x] Active Shift Indicator (✅ Open / ❌ Closed) ✅
  - [x] Show recent transactions table (Invoice #, Time, Amount, Status with badges) ✅
  - [x] Add quick action buttons (hooked up to navigation):
    - [x] "New Sale" → Navigate to Checkout ✅
    - [x] "View Inventory" → Navigate to Inventory ✅
    - [x] "Reports" → Navigate to Reports ✅
  - [x] Implement pull-to-refresh (onRefresh function) ✅
  - [x] Add loading states (isLoading, isRefreshing) ✅
  - [x] Add error handling with toast messages ✅
  - [x] Remove generic Exception catches (use Result<T> pattern) ✅

- [x] **Navigation Setup** ✅
  - [x] Create Screen sealed class with routes ✅
  - [x] Implement bottom navigation bar (mobile) - 5 primary screens ✅
  - [x] Implement side navigation drawer component (desktop ready) ✅
  - [x] Define all navigation destinations:
    - [x] /login ✅
    - [x] /dashboard (home) ✅
    - [x] /checkout ✅
    - [x] /inventory ✅
    - [x] /sales ✅
    - [x] /reports ✅
    - [x] /customers ✅
    - [x] /suppliers ✅
    - [x] /purchase-orders ✅
    - [x] /categories ✅
    - [x] /users ✅
    - [x] /shifts ✅
    - [x] /settings ✅
  - [x] Add authentication guard (redirect to /login if not authenticated) ✅
  - [x] Create PlaceholderScreen for unimplemented routes ✅

- [x] **Debug Features (Development Only)** ✅
  - [x] Add `--skip-auth` and `-d` command-line parameters ✅
  - [x] Configure debug mode via environment variable (DEBUG_MODE=true) ✅
  - [x] When skip-auth enabled:
    - [x] Auto-login with mock admin user ✅
    - [x] Navigate directly to Dashboard on app start ✅
    - [x] Show debug indicator in UI ("🔧 DEBUG MODE" badge) ✅
  - [x] Ensure debug mode is disabled in production builds ✅
  - [x] Add README section documenting debug mode usage:
    - [x] Desktop: `./gradlew :composeApp:run --args="--skip-auth"` ✅
    - [x] Android: Set `DEBUG_MODE=true` in local.properties ✅
    - [x] iOS: Set Debug scheme argument `-skip-auth` ✅
  - [x] Log warning on startup when debug mode is active ✅

- [x] **Testing** ✅
  - [x] Unit tests for GetDashboardSummaryUseCase (5 tests) ✅
  - [x] Unit tests for GetRecentTransactionsUseCase (9 tests with limit validation) ✅
  - [x] Unit tests for GetLowStockProductsUseCase (7 tests with severity sorting) ✅
  - [x] Backend route tests for DashboardRoutesTest (15 tests with JWT auth) ✅
  - [x] 36 total dashboard tests passing ✅
  - [x] >60% code coverage achieved ✅

**Deliverables:** ✅ ALL COMPLETE
✅ Working login/logout flow
✅ Dashboard displaying real-time data
✅ Navigation structure for all screens
✅ Debug mode for development convenience
✅ 60%+ code coverage on dashboard logic
✅ Zero generic Exception catches
✅ Koin DI properly configured
✅ SQL queries optimized and correct

**Estimated Effort:** 100 hours (2 developers × 50 hours)

---

## Phase 2: Core POS Features (Weeks 5-8) 🛒
**Goal:** Implement checkout, inventory, and sales features
**Status:** Week 5-6 ✅ 100% COMPLETE | Week 7-8 ⚠️ 0% COMPLETE (PENDING)

### Week 5-6: Checkout & Sales ✅ COMPLETE

- [x] **Domain Layer - Sales** ✅ COMPLETE
  - [x] Create Product entity (id, name, sku, price, stock, category_id, image_url) ✅
  - [x] Create Sale entity (id, invoice_number, user_id, customer_id, total, status, payment_method, created_at) ✅
  - [x] Create SaleItem entity (sale_id, product_id, quantity, unit_price, subtotal) ✅
  - [x] Create Cart value object (items: List<CartItem>, total: Money) ✅
  - [x] Define ProductRepository interface (search, getById, checkStock) ✅
  - [x] Define SaleRepository interface (create, getAll, getById, updateStatus) ✅
  - [x] Implement SearchProductsUseCase (by name, SKU, barcode) ✅
  - [x] Implement AddToCartUseCase (with stock validation) ✅
  - [x] Implement RemoveFromCartUseCase ✅
  - [x] Implement GetSalesUseCase (with filters) ✅
  - [x] Implement CompleteSaleUseCase: ✅
    - [x] Validate cart not empty ✅
    - [x] Validate stock availability for all items ✅
    - [x] Generate invoice number (helper function) ✅
    - [x] Create sale record ✅
    - [x] Create sale_items records ✅
    - [x] Deduct stock (create inventory_transaction) ✅
    - [x] Return Result<Sale> ✅

- [x] **Data Layer - Sales** ✅ COMPLETE
  - [x] Create ProductDTO, SaleDTO, SaleItemDTO ✅
  - [x] Create mappers (DTO ↔ Entity) ✅
  - [x] Implement ProductRepositoryImpl ✅
  - [x] Implement SaleRepositoryImpl ✅
  - [x] Add database transactions for CompleteSale (atomic operation) ✅

- [x] **Backend API - Sales** ✅ COMPLETE
  - [x] GET /api/products/search?q={query} (search by name/SKU) ✅
  - [x] GET /api/products/:id (get product details) ✅
  - [x] POST /api/sales (create new sale) ✅
  - [x] GET /api/sales (list all sales with pagination) ✅
  - [x] GET /api/sales/:id (get sale details with items) ✅
  - [x] PUT /api/sales/:id/status (update sale status: cancel, refund) ✅

- [x] **UI - Checkout Screen** (✅ Design available in Figma) ✅ COMPLETE
  - [x] Create CheckoutScreen composable (see 04-UI_ARCHITECTURE.md) ✅
  - [x] Create CheckoutViewModel with cart StateFlow ✅
  - [x] Implement product search bar with autocomplete ✅
  - [x] Display product grid/list (Image, Name, Price, Stock) ✅
  - [x] Add "Add to Cart" button with quantity selector ✅
  - [x] Show shopping cart sidebar: ✅
    - [x] List cart items (Product, Qty, Price, Subtotal) ✅
    - [x] Show running total ✅
    - [x] Add remove item button (X icon) ✅
    - [x] Add clear cart button ✅
  - [x] Implement payment method selection (Cash, Card, Mobile Payment) ✅
  - [x] Add "Complete Sale" button ✅
  - [x] Show sale confirmation dialog with invoice number ✅
  - [ ] Print receipt (optional - Phase 4)
  - [x] Handle errors (insufficient stock, payment failure) ✅

- [x] **UI - Sales List Screen** (✅ Design available in Figma) ✅ COMPLETE
  - [x] Create SalesListScreen composable ✅
  - [x] Display sales in table (Invoice #, Date, Customer, Total, Status, Payment Method) ✅
  - [x] Add status badges (Completed, Cancelled, Refunded) ✅
  - [x] Implement date range filter ✅
  - [x] Add search by invoice number ✅
  - [x] Show sale details dialog on row click ✅
  - [ ] Add "Refund" action button (changes status to Refunded) - Deferred to Phase 3

**Testing:** ✅ COMPLETE
- [x] Unit tests for CompleteSaleUseCase (7 tests - happy path + edge cases) ✅
- [x] Unit tests for AddToCartUseCase (8 tests - cart validation logic) ✅
- [x] Unit tests for SearchProductsUseCase (10 tests - search functionality) ✅
- [x] All 25 sales use case tests passing ✅

**Code Quality:** ✅ COMPLETE
- [x] Zero detekt violations (all 22 documentation violations resolved) ✅
- [x] Refactored services to meet quality standards: ✅
  - [x] ProductService: Fixed StringLiteralDuplication, LongParameterList (created GetAllRequest data class) ✅
  - [x] SaleService: Fixed TooManyFunctions (extracted SaleCreationHelper), LongParameterList, UseCheckOrError ✅
  - [x] SaleCreationHelper: Internal helper class for sale creation operations ✅
- [x] All public API documented with minimal KDoc ✅
- [x] Zero compilation errors ✅
- [x] Zero type safety violations ✅

### Week 7-8: Inventory & Categories ✅ COMPLETE

- [x] **Domain Layer - Inventory** ✅
  - [x] Create Category entity (id, name, description, color, icon, parent_id, is_active, product_count) ✅
  - [x] Create InventoryTransaction entity (id, product_id, type, quantity, reason, created_at) ✅
  - [x] Define CategoryRepository interface ✅
  - [x] Define InventoryRepository interface ✅
  - [x] Implement CreateProductUseCase with validations: ✅
    - [x] Name not empty (3-100 chars) ✅
    - [x] SKU unique ✅
    - [x] Price > 0 ✅
    - [x] Stock >= 0 ✅
  - [x] Implement UpdateProductUseCase ✅
  - [x] Implement DeleteProductUseCase (soft delete) ✅
  - [x] Implement AdjustStockUseCase (manual adjustment, creates inventory_transaction) ✅
  - [x] Implement GetAllProductsUseCase (with filters: category, low stock) ✅
  - [x] Implement CreateCategoryUseCase ✅
  - [x] Implement UpdateCategoryUseCase ✅
  - [x] Implement GetCategoriesUseCase ✅

- [x] **Data Layer - Inventory** ✅
  - [x] Implement CategoryRepositoryImpl ✅
  - [x] Implement InventoryRepositoryImpl ✅
  - [x] Create CategoryDTO, InventoryTransactionDTO with mappers ✅
  - [x] Create RemoteCategoryDataSource (Ktor HTTP client) ✅
  - [x] Create RemoteInventoryDataSource (Ktor HTTP client) ✅

- [x] **Backend API - Inventory** ✅
  - [x] GET /api/products (list with filters: category, low_stock, pagination) ✅
  - [x] POST /api/products (create product) ✅
  - [x] PUT /api/products/:id (update product) ✅
  - [x] DELETE /api/products/:id (soft delete) ✅
  - [x] POST /api/products/:id/adjust-stock (manual stock adjustment) ✅
  - [x] GET /api/inventory/transactions (history with filters) ✅
  - [x] GET /api/categories (all categories with pagination) ✅
  - [x] POST /api/categories (create category) ✅
  - [x] PUT /api/categories/:id (update category) ✅
  - [x] DELETE /api/categories/:id (soft delete) ✅
  - [x] Create ProductService with create/update/delete/adjust-stock methods ✅
  - [x] Create CategoryService with CRUD operations ✅
  - [x] Create InventoryService for transaction queries ✅

- [x] **UI - Inventory Management Screen** (✅ Design implemented from inventory.png) ✅
  - [x] Create InventoryScreen composable ✅
  - [x] Create InventoryViewModel with StateFlow ✅
  - [x] Display products in table (SKU, Name, Category, Price, Stock, Size, Weight, Status, Actions) ✅
  - [x] Add stock status badges (Good - green, Medium - gray, Low - red) ✅
  - [x] Implement "Add Product" button ✅
  - [x] Add search by name/SKU ✅
  - [x] Display 4 KPI cards (Total Products, Low Stock, Total Value, Categories) ✅
  - [x] Add edit/delete action buttons (FontAwesome icons) ✅
  - [x] Use FontAwesome icons (faSearch, faPlus, faPencil, faTrash) ✅

- [x] **UI - Category Management Screen** (✅ Design implemented from categories.png) ✅
  - [x] Create CategoriesScreen composable ✅
  - [x] Create CategoriesViewModel with StateFlow ✅
  - [x] Display categories in card grid + table view ✅
  - [x] Add color swatches per category ✅
  - [x] Implement "Add Category" button ✅
  - [x] Display 4 KPI cards (Total Categories, Total Products, Avg per Category, Largest Category) ✅
  - [x] Show product count per category ✅
  - [x] Add edit/delete actions ✅
  - [x] Use FontAwesome icons (faFolder, faTags, faBoxes) ✅

**Testing:** ✅
- [x] Unit tests for CreateProductUseCase (validation rules) ✅
- [x] Unit tests for AdjustStockUseCase ✅
- [x] Code formatting with spotlessApply ✅
- [x] Static analysis with detekt ✅

**Deliverables:**
✅ Complete checkout flow (search → cart → payment)
✅ Sales history with search/filter
✅ Product CRUD with stock management
✅ Category management (hierarchical)
✅ 65%+ code coverage on sales/inventory logic

**Estimated Effort:** 200 hours (2 developers × 100 hours)

---

## Phase 3: Management Features (Weeks 9-12) 👥
**Goal:** Implement customer, supplier, purchase order, user, and shift management

### Week 9-10: Customers & Suppliers

- [ ] **Domain Layer - Customers**
  - [ ] Create Customer entity (id, name, email, phone, loyalty_points, tier, total_spent)
  - [ ] Create CustomerTier enum (Bronze, Silver, Gold, Platinum)
  - [ ] Define CustomerRepository interface
  - [ ] Implement CreateCustomerUseCase with validations:
    - [ ] Name not empty (2-100 chars)
    - [ ] Email valid format (if provided)
    - [ ] Phone valid format (if provided)
  - [ ] Implement UpdateCustomerUseCase
  - [ ] Implement AddLoyaltyPointsUseCase (updates tier automatically via DB trigger)
  - [ ] Implement GetAllCustomersUseCase (with filters: tier, search)
  - [ ] Implement GetCustomerPurchaseHistoryUseCase

- [ ] **Domain Layer - Suppliers**
  - [ ] Create Supplier entity (id, name, contact_person, email, phone, address, status)
  - [ ] Define SupplierRepository interface
  - [ ] Implement CreateSupplierUseCase
  - [ ] Implement UpdateSupplierUseCase
  - [ ] Implement GetAllSuppliersUseCase

- [ ] **Data Layer**
  - [ ] Implement CustomerRepositoryImpl
  - [ ] Implement SupplierRepositoryImpl
  - [ ] Create CustomerDTO, SupplierDTO with mappers

- [ ] **Backend API**
  - [ ] GET /api/customers (list with filters, pagination)
  - [ ] POST /api/customers (create customer)
  - [ ] PUT /api/customers/:id (update customer)
  - [ ] POST /api/customers/:id/loyalty-points (add points)
  - [ ] GET /api/customers/:id/purchase-history
  - [ ] GET /api/suppliers (list all suppliers)
  - [ ] POST /api/suppliers (create supplier)
  - [ ] PUT /api/suppliers/:id (update supplier)

- [ ] **UI - Customer Management Screen** (✅ Design available in Figma)
  - [ ] Create CustomersScreen composable
  - [ ] Display customers in table (Avatar, Name, Email, Phone, Loyalty Points, Tier, Total Spent)
  - [ ] Add tier badges (Bronze, Silver, Gold, Platinum with colors)
  - [ ] Implement "Add Customer" button → Customer form dialog
  - [ ] Show customer details dialog with purchase history
  - [ ] Add "Add Points" button → Loyalty points dialog
  - [ ] Add search by name/email/phone
  - [ ] Filter by tier dropdown

- [ ] **UI - Supplier Management Screen** (✅ Design available in Figma)
  - [ ] Create SuppliersScreen composable
  - [ ] Display suppliers in table (Name, Contact Person, Email, Phone, Status)
  - [ ] Add status badges (Active, Inactive)
  - [ ] Implement "Add Supplier" button → Supplier form dialog
  - [ ] Show supplier details dialog
  - [ ] Add search by name

**Testing:**
- [ ] Unit tests for AddLoyaltyPointsUseCase
- [ ] Unit tests for Customer tier calculation logic
- [ ] Integration tests for CustomerRepositoryImpl
- [ ] E2E test: Create customer → Add points → Verify tier upgrade

### Week 11-12: Purchase Orders, Users & Shifts

- [ ] **Domain Layer - Purchase Orders**
  - [ ] Create PurchaseOrder entity (id, supplier_id, status, total, order_date)
  - [ ] Create PurchaseOrderItem entity (po_id, product_id, quantity, unit_price)
  - [ ] Define PurchaseOrderRepository interface
  - [ ] Implement CreatePurchaseOrderUseCase
  - [ ] Implement UpdatePurchaseOrderStatusUseCase (workflow: Draft → Pending → Approved → Received)
  - [ ] Implement ReceivePurchaseOrderUseCase (update inventory on receive)
  - [ ] Implement GetAllPurchaseOrdersUseCase

- [ ] **Domain Layer - Users & Roles**
  - [ ] Implement GetAllUsersUseCase
  - [ ] Implement CreateUserUseCase with validations:
    - [ ] Username uniqueness
    - [ ] Email uniqueness
    - [ ] Password complexity (8+ chars, mixed case, number)
  - [ ] Implement UpdateUserUseCase
  - [ ] Implement DeleteUserUseCase (soft delete)
  - [ ] Implement AssignRoleUseCase
  - [ ] Implement ChangePasswordUseCase

- [ ] **Domain Layer - Shifts**
  - [ ] Create Shift entity (id, user_id, start_time, end_time, opening_balance, closing_balance, status)
  - [ ] Define ShiftRepository interface
  - [ ] Implement OpenShiftUseCase (only one open shift per cashier)
  - [ ] Implement CloseShiftUseCase with reconciliation:
    - [ ] Calculate expected cash (opening + sales - expenses)
    - [ ] Compare with actual cash entered
    - [ ] Calculate variance
    - [ ] Generate shift summary
  - [ ] Implement GetCurrentShiftUseCase
  - [ ] Implement GetShiftHistoryUseCase

- [ ] **Data Layer**
  - [ ] Implement PurchaseOrderRepositoryImpl
  - [ ] Implement ShiftRepositoryImpl
  - [ ] Create PurchaseOrderDTO, ShiftDTO with mappers

- [ ] **Backend API**
  - [ ] GET /api/purchase-orders (list with filters)
  - [ ] POST /api/purchase-orders (create)
  - [ ] PUT /api/purchase-orders/:id (update)
  - [ ] POST /api/purchase-orders/:id/receive (mark as received + update stock)
  - [ ] GET /api/users (list all users)
  - [ ] POST /api/users (create user)
  - [ ] PUT /api/users/:id (update user)
  - [ ] DELETE /api/users/:id (soft delete)
  - [ ] POST /api/users/:id/assign-role (assign role)
  - [ ] POST /api/shifts/open (open new shift)
  - [ ] POST /api/shifts/close (close shift with reconciliation)
  - [ ] GET /api/shifts/current (get current open shift)
  - [ ] GET /api/shifts/history (shift history with filters)

- [ ] **UI - Purchase Orders Screen** (✅ Design available)
  - [ ] Create PurchaseOrdersScreen composable
  - [ ] Display POs in table (PO #, Supplier, Date, Status, Total)
  - [ ] Add status badges (Draft/Pending/Approved/Received/Cancelled)
  - [ ] Implement "Create PO" button → PO form dialog
  - [ ] Add supplier selection dropdown
  - [ ] Implement product line items (add/remove products)
  - [ ] Show PO details dialog
  - [ ] Add "Receive PO" button (updates inventory)
  - [ ] Add status change workflow buttons

- [ ] **UI - User Management Screen** (🎨 Design needed)
  - [ ] Create UserManagementScreen composable
  - [ ] Display users in table (Name, Email, Role, Status)
  - [ ] Add role badges (Admin/Manager/Cashier/Warehouse/Viewer)
  - [ ] Implement "Add User" button → User form dialog
  - [ ] Add role assignment dropdown
  - [ ] Show password reset functionality
  - [ ] Add status toggle (Active/Inactive/Suspended)
  - [ ] Display last login timestamp
  - [ ] Show activity logs per user

- [ ] **UI - Shift Management Screen** (🎨 Design needed)
  - [ ] Create ShiftManagementScreen composable
  - [ ] Show current shift status card (Open/Closed)
  - [ ] Implement "Open Shift" button → Opening balance dialog
  - [ ] Implement "Close Shift" button → Reconciliation dialog:
    - [ ] Show expected cash calculation
    - [ ] Cash breakdown input (bills, coins)
    - [ ] Display variance (red if over/under)
    - [ ] Notes field for discrepancies
  - [ ] Display shift summary (Sales count, Revenue, Cash vs Card)
  - [ ] Show shift history table
  - [ ] Add export shift report button

**Testing:**
- [ ] Unit tests for OpenShiftUseCase (prevent multiple open shifts)
- [ ] Unit tests for CloseShiftUseCase (reconciliation logic)
- [ ] Unit tests for ReceivePurchaseOrderUseCase (inventory update)
- [ ] Integration tests for PurchaseOrderRepositoryImpl
- [ ] E2E test: Create PO → Approve → Receive → Verify inventory updated
- [ ] E2E test: Open shift → Make sale → Close shift → Verify reconciliation

**Deliverables:**
✅ Customer management with loyalty program
✅ Supplier management
✅ Purchase order workflow (create → receive → inventory update)
✅ User management with RBAC
✅ Shift management with cash reconciliation
✅ 70%+ code coverage on management features

**Estimated Effort:** 200 hours (2 developers × 100 hours)

---

## Phase 4: Analytics & Polish (Weeks 13-16) 📊
**Goal:** Implement reporting, analytics, and production-ready features

### Week 13-14: Reports & Analytics

- [ ] **Domain Layer - Reports**
  - [ ] Create SalesReport value object (period, revenue, profit, transactions)
  - [ ] Create ProductPerformance value object (product, quantity_sold, revenue)
  - [ ] Create CategoryBreakdown value object (category, percentage, revenue)
  - [ ] Define ReportRepository interface
  - [ ] Implement GetSalesReportUseCase (date range, aggregations)
  - [ ] Implement GetTopProductsUseCase (top 10 by revenue/quantity)
  - [ ] Implement GetCategoryBreakdownUseCase
  - [ ] Implement GetCustomerAnalyticsUseCase (top customers, visit frequency)
  - [ ] Implement GetSalesTrendUseCase (daily/weekly/monthly trends)

- [ ] **Data Layer**
  - [ ] Implement ReportRepositoryImpl with complex SQL aggregations
  - [ ] Optimize queries with database views (optional)
  - [ ] Add caching for frequently accessed reports (5-minute TTL)

- [ ] **Backend API**
  - [ ] GET /api/reports/sales (date range, grouping: day/week/month)
  - [ ] GET /api/reports/products/top (limit, order: revenue/quantity)
  - [ ] GET /api/reports/categories/breakdown (date range)
  - [ ] GET /api/reports/customers/top (date range, limit)
  - [ ] GET /api/reports/trends (period: daily/weekly/monthly)
  - [ ] POST /api/reports/export (PDF/Excel export - Phase 4)

- [ ] **UI - Reports & Analytics Screen** (✅ Design available)
  - [ ] Create ReportsScreen composable
  - [ ] Implement ReportsViewModel with date range selector
  - [ ] Display key metrics cards:
    - [ ] Total Revenue (with trend indicator)
    - [ ] Total Transactions
    - [ ] Average Order Value
    - [ ] Top Selling Product
  - [ ] Implement sales trend chart (line chart with date axis)
  - [ ] Add category breakdown (pie chart with percentages)
  - [ ] Show top products table (Product, Quantity, Revenue)
  - [ ] Add top customers table (Customer, Orders, Revenue)
  - [ ] Implement date range picker (Today, This Week, This Month, Custom)
  - [ ] Add export button (PDF/Excel)
  - [ ] Show loading skeletons for charts

**Chart Implementation:**
- [ ] Use Compose Charts library (or custom Canvas drawing)
- [ ] Implement responsive chart sizing
- [ ] Add hover tooltips (Desktop)
- [ ] Touch interaction for mobile

### Week 15: Settings & Configuration

- [ ] **Domain Layer - Settings**
  - [ ] Create StoreSettings entity (name, address, phone, tax_rate, currency)
  - [ ] Define SettingsRepository interface
  - [ ] Implement GetSettingsUseCase
  - [ ] Implement UpdateSettingsUseCase
  - [ ] Implement UpdateProfileUseCase (for current user)

- [ ] **Data Layer**
  - [ ] Implement SettingsRepositoryImpl
  - [ ] Store settings in database (settings table)

- [ ] **Backend API**
  - [ ] GET /api/settings (get all settings)
  - [ ] PUT /api/settings (update settings)
  - [ ] PUT /api/users/me/profile (update profile)
  - [ ] POST /api/users/me/change-password

- [ ] **UI - Settings Screen** (🎨 Design needed)
  - [ ] Create SettingsScreen composable
  - [ ] Implement SettingsViewModel
  - [ ] Add tabbed interface:
    - [ ] **Store Information Tab**
      - [ ] Store name input
      - [ ] Address input
      - [ ] Phone input
      - [ ] Tax rate input (percentage)
      - [ ] Currency dropdown (USD, EUR, etc.)
    - [ ] **Receipt Configuration Tab**
      - [ ] Header text input
      - [ ] Footer text input
      - [ ] Logo upload
      - [ ] Show tax toggle
      - [ ] Receipt preview
    - [ ] **User Preferences Tab**
      - [ ] Language dropdown (English, Spanish, etc.)
      - [ ] Theme toggle (Light/Dark)
      - [ ] Notification preferences
      - [ ] Auto-logout timeout
    - [ ] **Profile Tab**
      - [ ] Name input
      - [ ] Email input
      - [ ] Change password form
      - [ ] Avatar upload
  - [ ] Add "Save" button with confirmation toast
  - [ ] Implement form validation

### Week 16: Polish & Production Readiness

- [ ] **Empty States**
  - [ ] Design and implement empty states for all screens:
    - [ ] No products in inventory
    - [ ] No sales today
    - [ ] No customers yet
    - [ ] No purchase orders
    - [ ] Empty cart in checkout
  - [ ] Add illustrations/icons for empty states
  - [ ] Include CTA buttons (e.g., "Add Your First Product")

- [ ] **Loading States**
  - [ ] Implement skeleton screens for all list views
  - [ ] Add shimmer effect to skeletons
  - [ ] Show loading indicators for buttons during async operations
  - [ ] Add progressive loading for images

- [ ] **Error Handling**
  - [ ] Create error page for unhandled exceptions
  - [ ] Add retry button for failed operations
  - [ ] Implement offline mode detection
  - [ ] Show toast notifications for errors
  - [ ] Add error boundaries (Compose equivalent)

- [ ] **Accessibility**
  - [ ] Add content descriptions for all icons
  - [ ] Ensure 4.5:1 color contrast ratio (WCAG AA)
  - [ ] Test keyboard navigation (Desktop)
  - [ ] Add focus indicators
  - [ ] Test with screen readers

- [ ] **Performance Optimization**
  - [ ] Implement image lazy loading
  - [ ] Add pagination to all list screens (50 items per page)
  - [ ] Optimize database queries (review slow query log)
  - [ ] Add caching for frequently accessed data
  - [ ] Minimize recompositions (use remember, derivedStateOf)
  - [ ] Profile app with Compose Profiler

- [ ] **Platform-Specific Features**
  - [ ] **Desktop:**
    - [ ] Add keyboard shortcuts (Ctrl+N, Ctrl+F, etc.)
    - [ ] Implement multi-window support (optional)
    - [ ] Add right-click context menus
    - [ ] Configure window size and position persistence
  - [ ] **Mobile:**
    - [ ] Add swipe gestures (swipe to delete, etc.)
    - [ ] Implement pull-to-refresh on all lists
    - [ ] Add haptic feedback for actions
    - [ ] Test on iOS and Android
  - [ ] **Web:**
    - [ ] Add PWA manifest for installability
    - [ ] Implement service worker for offline support (basic)
    - [ ] Test on Chrome, Firefox, Safari
    - [ ] Add responsive meta tags

- [ ] **Documentation**
  - [ ] Write API documentation (OpenAPI/Swagger)
  - [ ] Create user manual (markdown + screenshots)
  - [ ] Add inline code comments for complex logic
  - [ ] Document deployment process
  - [ ] Create troubleshooting guide

- [ ] **Security Hardening**
  - [ ] Add rate limiting to API endpoints
  - [ ] Implement CSRF protection
  - [ ] Add SQL injection prevention (already using ORM)
  - [ ] Configure HTTPS for production
  - [ ] Add security headers (CSP, X-Frame-Options, etc.)
  - [ ] Conduct security audit

**Testing:**
- [ ] Conduct cross-platform testing (Desktop, Mobile, Web)
- [ ] Perform load testing (100 concurrent users)
- [ ] Execute security penetration testing
- [ ] Run accessibility audit
- [ ] Test on slow network (3G simulation)

**Deliverables:**
✅ Complete reports and analytics dashboard
✅ Settings screen with store configuration
✅ Empty/loading/error states for all screens
✅ Accessibility compliance (WCAG AA)
✅ Performance optimizations
✅ Platform-specific features
✅ Documentation and user manual
✅ Production-ready application
✅ 75%+ overall code coverage

**Estimated Effort:** 200 hours (2 developers × 100 hours)

---

## Development Sequence (Priority Order)

### Critical Path (Must Build First)
```
1. Foundation (Week 1-2)
   ↓
2. Authentication (Week 3)
   ↓
3. Navigation & Dashboard (Week 4)
   ↓
4. Products/Categories (Week 7-8)
   ↓
5. Checkout & Sales (Week 5-6)
   ↓
6. Inventory Management (Week 7-8)
```

### Parallel Tracks (Can Build Independently)
```
Track A: User & Access Control
- User Management (Week 11)
- Shift Management (Week 12)

Track B: External Relations
- Customer Management (Week 9)
- Supplier Management (Week 10)
- Purchase Orders (Week 11)

Track C: Analytics
- Reports & Analytics (Week 13-14)
- Settings (Week 15)
```

### Why This Order?
1. **Authentication First:** Required for all other features (gated access)
2. **Dashboard Early:** Provides navigation hub and demonstrates value
3. **Products Before Checkout:** Can't sell without products
4. **Checkout Before Reports:** Need sales data to generate reports
5. **Management Features Later:** Not blocking core POS functionality
6. **Analytics Last:** Requires historical data to be meaningful

---

## Technical Dependencies

### External Services
| Service | Purpose | Status | Notes |
|---------|---------|--------|-------|
| Supabase | PostgreSQL database + Auth | ✅ Configured | Project ID: jewqhojchyrmozxsrkoq |
| Ktor Server | Backend API | ⏳ To be deployed | Can run locally during development |
| Compose Multiplatform | UI Framework | ✅ Available | Version 1.7.1 |
| Koin | Dependency Injection | ✅ Available | Version 4.0.0 |

### Library Dependencies
```toml
# Critical (Must Have)
kotlin = "2.1.0"
compose = "1.7.1"
ktor = "3.0.1"
kotlinx-coroutines = "1.9.0"
kotlinx-serialization = "1.7.3"
kotlinx-datetime = "0.6.1"
koin = "4.0.0"

# Important (High Priority)
sqldelight = "2.0.2"  # If adding offline support
coil = "3.0.0"  # Image loading
androidx-navigation = "2.8.0-alpha10"  # Navigation

# Nice to Have (Can defer)
detekt = "1.23.7"  # Code quality
charts-library = "TBD"  # For analytics charts
```

### Platform Requirements
- **Desktop:** JDK 17+, Windows 10+ / macOS 12+ / Linux
- **Android:** Min SDK 24 (Android 7.0), Target SDK 34
- **iOS:** iOS 15+, Xcode 15+
- **Web:** Modern browsers (Chrome 90+, Firefox 88+, Safari 14+)

### Development Tools
- **IDE:** IntelliJ IDEA 2024.1+ or Android Studio Hedgehog+
- **Database Tool:** DBeaver or Supabase Studio
- **API Testing:** Postman or Bruno
- **Version Control:** Git 2.30+
- **CI/CD:** GitHub Actions (configured in Phase 0)

---

## Risk Assessment

### High Risk ⚠️

#### 1. Missing Design Assets
**Risk:** 5 critical screens lack Figma designs (Login, Dashboard, User Mgmt, Shift Mgmt, Settings)
**Impact:** Blocks Phase 1 and 3 UI development
**Mitigation:**
- Proceed with Phase 0 (infrastructure) without designs
- Request design team to prioritize missing screens
- Create wireframe mockups as temporary solution
- Estimate 1-week delay if designs not ready by Week 3
**Owner:** Design Team + Project Manager

#### 2. Kotlin Multiplatform Maturity
**Risk:** KMP 2.1.0 is relatively new, may have platform-specific bugs
**Impact:** Unexpected bugs, platform-specific workarounds needed
**Mitigation:**
- Allocate 10% time buffer for troubleshooting
- Use expect/actual for platform-specific code
- Stay on stable versions (avoid alphas)
- Monitor JetBrains issue tracker
- Have fallback to platform-native if critical issues
**Owner:** Tech Lead

#### 3. Performance with Large Datasets
**Risk:** App slowdown with 10,000+ products or 100,000+ sales records
**Impact:** Poor UX, customer dissatisfaction
**Mitigation:**
- Implement pagination early (50 items per page)
- Add database indexes (already defined)
- Profile app with Compose Profiler in Week 8
- Add caching layer (5-minute TTL for reports)
- Consider lazy loading for images
**Owner:** Backend Developer + Tech Lead

### Medium Risk ⚙️

#### 4. Supabase Rate Limits
**Risk:** Free tier has rate limits (could throttle requests)
**Impact:** API failures during testing/demo
**Mitigation:**
- Upgrade to paid tier before production
- Implement request caching
- Add retry logic with exponential backoff
- Monitor usage via Supabase dashboard
**Owner:** DevOps

#### 5. Team Capacity
**Risk:** 2 developers for 16-week project = 640 hours, but estimate is 780 hours
**Impact:** Schedule slip or reduced scope
**Mitigation:**
- Prioritize critical path features
- Defer "nice-to-have" features to v2.0 (e.g., batch import, advanced reports)
- Add 1 additional developer for Phase 2-3 (optional)
- Plan for overtime in final 2 weeks (crunch time)
**Owner:** Project Manager

#### 6. Cross-Platform UI Inconsistencies
**Risk:** UI looks/behaves differently on Desktop vs Mobile vs Web
**Impact:** Inconsistent UX, additional polish work
**Mitigation:**
- Test on all platforms weekly (from Week 4)
- Use platform-specific expect/actual sparingly
- Leverage WindowSizeClass for responsive layouts
- Allocate 1 week in Phase 4 for cross-platform polish
**Owner:** UI Developer

### Low Risk ✅

#### 7. Database Migration
**Risk:** Schema changes during development break existing data
**Impact:** Data loss, migration complexity
**Mitigation:**
- Use migration scripts (numbered: 001_initial.sql, 002_add_field.sql)
- Test migrations on staging DB first
- Backup database before migrations
- Low risk for v1.0 (green field project)
**Owner:** Data Engineer

#### 8. Third-Party Library Updates
**Risk:** Breaking changes in Compose/Ktor/Koin during development
**Impact:** Build failures, need to refactor
**Mitigation:**
- Lock dependency versions in libs.versions.toml
- Only update for critical security patches
- Test updates on separate branch first
- Defer major updates until post-v1.0
**Owner:** Tech Lead

---

## Success Metrics

### Technical Metrics
| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| Code Coverage | 75%+ | Kover (Kotlin coverage tool) |
| Build Time | <5 min for full build | Gradle Build Scan |
| API Response Time | <200ms (p95) | Ktor metrics |
| UI Frame Rate | 60 FPS | Compose Profiler |
| Crash-Free Sessions | >99% | Platform analytics (Crashlytics) |
| Bug Density | <5 bugs per 1000 LOC | Issue tracker |
| Security Vulnerabilities | 0 critical/high | Detekt + dependency check |

### Functional Metrics
| Feature | Acceptance Criteria |
|---------|-------------------|
| Authentication | User can login/logout, session persists, invalid credentials handled |
| Checkout | User can search products, add to cart, apply payment, complete sale |
| Inventory | User can add/edit/delete products, view stock levels, adjust stock |
| Sales History | User can view sales, filter by date/status, view sale details |
| Reports | User can view sales trends, top products, category breakdown |
| Customer Mgmt | User can add/edit customers, update loyalty points, view tier |
| Purchase Orders | User can create PO, receive PO (updates inventory) |
| User Management | Admin can add users, assign roles, reset passwords |
| Shift Management | Cashier can open/close shift, reconcile cash, view variance |
| Settings | User can configure store info, tax rate, preferences |

### Business Metrics (Post-Launch)
- Daily Active Users (Target: 10+ cashiers using daily)
- Transactions Processed (Target: 100+ transactions/day)
- Average Checkout Time (Target: <2 minutes)
- System Uptime (Target: 99.5%+)
- User Satisfaction (Target: 4.5/5 stars)

---

## Team Structure & Responsibilities

### Recommended Team (2-3 Developers)

#### Developer 1: Backend Specialist
**Responsibilities:**
- Database setup and migrations
- Domain layer entities and use cases
- Data layer repositories and DTOs
- Ktor API routes and controllers
- Backend testing (unit + integration)
- Performance optimization

**Phase Allocation:**
- Phase 0: Database setup (Week 1-2)
- Phase 1: Auth backend (Week 3-4)
- Phase 2: Sales/Inventory backend (Week 5-8)
- Phase 3: Management backend (Week 9-12)
- Phase 4: Reports backend (Week 13-14)

#### Developer 2: Frontend Specialist
**Responsibilities:**
- UI theme system and components
- Screen implementations (Compose)
- ViewModels and state management
- Navigation structure
- Platform-specific adaptations
- UI testing

**Phase Allocation:**
- Phase 0: Theme + components (Week 1-2)
- Phase 1: Login + Dashboard UI (Week 3-4)
- Phase 2: Checkout + Inventory UI (Week 5-8)
- Phase 3: Management UIs (Week 9-12)
- Phase 4: Reports UI + Polish (Week 13-16)

#### Developer 3 (Optional): Full-Stack Support
**Responsibilities:**
- Assist with critical path features
- Code reviews and PR approvals
- Testing (E2E, cross-platform)
- Documentation
- DevOps (CI/CD, deployment)

**Phase Allocation:**
- Phase 1-2: Help with checkout flow (Week 5-6)
- Phase 3: Help with shift management (Week 12)
- Phase 4: Cross-platform testing and polish (Week 15-16)

### External Roles

#### UI/UX Designer
**Needed:** Week 1-3 (before Phase 1 starts)
**Tasks:**
- Create missing screen designs (Login, Dashboard, User Mgmt, Shift Mgmt, Settings)
- Design empty/loading/error states
- Define animation specifications
- Review implemented UI for consistency

#### Project Manager
**Needed:** Throughout project
**Tasks:**
- Sprint planning (2-week sprints)
- Daily standups
- Risk monitoring and mitigation
- Stakeholder communication
- UAT coordination
- Go-live planning

#### QA Tester
**Needed:** Phase 3-4 (Week 9-16)
**Tasks:**
- Manual testing on all platforms
- Exploratory testing
- User acceptance testing (UAT)
- Bug reporting and verification
- Test case documentation

---

## Next Steps

### Immediate Actions (Week 1)

#### 1. Project Kickoff Meeting
- [ ] Review this implementation plan with full team
- [ ] Confirm timeline and resource availability
- [ ] Assign responsibilities (Developer 1, Developer 2, Designer)
- [ ] Set up communication channels (Slack, Daily standups)
- [ ] Define sprint cadence (2-week sprints)

#### 2. Development Environment Setup
- [ ] Install IntelliJ IDEA 2024.1+ on all dev machines
- [ ] Install Kotlin 2.1.0 and required SDKs
- [ ] Set up Git repository and branching strategy (main, develop, feature/*)
- [ ] Configure IDEs with code style (EditorConfig, Detekt)
- [ ] Test local compilation on all target platforms

#### 3. Design Asset Request
- [ ] Request missing screen designs from design team:
  - [ ] Login/Authentication screen (Critical - Week 3)
  - [ ] Dashboard/Home screen (Critical - Week 4)
  - [ ] User Management screen (High - Week 11)
  - [ ] Shift Management screen (High - Week 12)
  - [ ] Settings screen (Medium - Week 15)
- [ ] Request empty/loading/error state designs (Week 15)
- [ ] Schedule design review sessions (weekly)

#### 4. Supabase Configuration
- [ ] Verify access to Supabase project (jewqhojchyrmozxsrkoq)
- [ ] Review database schema document (02-DATABASE_SCHEMA.md)
- [ ] Prepare DDL script for execution
- [ ] Set up staging environment (separate Supabase project)
- [ ] Configure API keys and connection strings

#### 5. CI/CD Pipeline Setup
- [ ] Create GitHub Actions workflow file (.github/workflows/ci.yml)
- [ ] Configure build verification (Desktop, Android, iOS, Web)
- [ ] Add Detekt checks to pipeline
- [ ] Set up test execution (unit + integration)
- [ ] Configure deployment to staging (Phase 1+)

### Week 2 Deliverables
- [x] Compilable KMP project (empty, but compiles on all platforms) ✅
- [x] Database created with all tables and seed data ✅ (17 production tables)
- [x] Health check endpoint responding (GET /health) ✅
- [ ] Sample screen rendered with theme system (basic screen exists, custom theme needed)
- [x] CI/CD pipeline green (all checks passing) ✅

### Phase 1 Prerequisites
- [ ] Login screen design delivered by design team (by Week 3)
- [ ] Dashboard screen design delivered by design team (by Week 4)
- [ ] All developers have completed Phase 0 tasks
- [ ] Authentication endpoints defined and documented

---

## Appendix: Design Gaps Summary

### Critical Missing Designs (Block Development)
1. **Login/Authentication Screen** - Blocks Phase 1 (Week 3)
2. **Dashboard/Home Screen** - Blocks Phase 1 (Week 4)
3. **User Management Screen** - Blocks Phase 3 (Week 11)
4. **Shift Management Screen** - Blocks Phase 3 (Week 12)
5. **Settings Screen** - Blocks Phase 4 (Week 15)

### Missing UI States (Block Polish)
- Empty states for all 13 screens
- Loading states (skeletons)
- Error states (error pages, inline errors)
- Offline mode indicator
- Onboarding/first-time user flow

### Recommendations for Design Team
- Use existing 8 screens as style guide for new screens
- Maintain consistency with shadcn/ui components
- Provide Figma designs + exported assets (PNG/SVG)
- Include responsive breakpoints (Mobile, Tablet, Desktop)
- Specify animations/transitions (fade, slide, etc.)
- Define accessibility considerations (focus states, contrast)

---

## Appendix: Technology Stack Summary

### Frontend
- **Language:** Kotlin 2.1.0
- **UI Framework:** Compose Multiplatform 1.7.1
- **State Management:** StateFlow (kotlinx.coroutines)
- **Navigation:** Compose Navigation 2.8.0
- **Image Loading:** Coil 3.0.0
- **DI:** Koin 4.0.0

### Backend
- **Language:** Kotlin 2.1.0
- **Framework:** Ktor 3.0.1
- **Database:** PostgreSQL 17.6 (Supabase)
- **ORM:** Supabase Postgrest client
- **Authentication:** JWT (Ktor Auth plugin)
- **Serialization:** kotlinx.serialization 1.7.3
- **DI:** Koin 4.0.0

### Common
- **Coroutines:** kotlinx.coroutines 1.9.0
- **DateTime:** kotlinx.datetime 0.6.1 (use kotlin.time.Clock)
- **Serialization:** kotlinx.serialization 1.7.3
- **Testing:** kotlin-test 2.1.0, Kotest 5.9.1, Turbine 1.1.0

### DevOps
- **Build:** Gradle 8.5 with Kotlin DSL
- **CI/CD:** GitHub Actions
- **Code Quality:** Detekt 1.23.7
- **Formatting:** Spotless 6.25.0
- **Logging:** SLF4J + Logback 1.5.11

---

## Conclusion

This implementation plan provides a comprehensive roadmap for developing the Vibely POS system over 16 weeks (4 phases). The architecture has been validated across all layers (Database ↔ Domain ↔ Backend ↔ UI) with excellent alignment and minimal gaps.

### Key Success Factors
✅ Clear phase separation with concrete deliverables
✅ Critical path identified (Auth → Dashboard → Products → Checkout)
✅ Risks identified with mitigation strategies
✅ Realistic effort estimates (780 hours total)
✅ Clean architecture ensuring maintainability
✅ Cross-platform support from day one

### Critical Dependencies
⚠️ **Design Assets:** 5 missing screens must be delivered before respective phases
⚠️ **Team Capacity:** 2 developers minimum, 3 recommended
⚠️ **Supabase Access:** Ensure API keys and database access before Week 1

### Go/No-Go Decision Points
- **Week 2:** Foundation complete? (Project compiles, DB ready, CI/CD green)
- **Week 4:** Phase 1 complete? (Login works, Dashboard displays data)
- **Week 8:** Phase 2 complete? (Checkout flow works end-to-end)
- **Week 12:** Phase 3 complete? (All management features functional)
- **Week 16:** Production ready? (All tests pass, documentation complete)

---

**Document Status:** ✅ Complete - Ready for Team Review
**Next Review:** After Phase 0 completion (Week 2)
**Version:** 1.0
**Last Updated:** March 12, 2026

---

**Prepared by:** Implementation Planning Team
**Approved by:** [Pending Project Manager Approval]
**Distribution:** Development Team, Design Team, Project Stakeholders
