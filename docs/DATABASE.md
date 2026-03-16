# Database Schema

Complete database schema reference for Vibely POS. This document contains the full PostgreSQL schema including tables, relationships, indexes, and constraints.

> **Note:** This is a reference document. For architecture overview, see [ARCHITECTURE.md](ARCHITECTURE.md).

**Database:** Supabase PostgreSQL
**Version:** PostgreSQL 15+

> **Note:** Set up your own Supabase project and configure connection details in your `.env` file. See [Backend README](../backend/README.md) for setup instructions.

---

## Table of Contents

1. [ENUM Types](#enum-types)
2. [Core Tables](#core-tables)
3. [Complete DDL](#complete-ddl)
4. [Indexes](#indexes)
5. [Foreign Key Relationships](#foreign-key-relationships)
6. [Database Diagram](#database-diagram)

---

## ENUM Types

### User and Permission ENUMs

```sql
-- User role types
CREATE TYPE user_role AS ENUM ('admin', 'manager', 'cashier', 'warehouse', 'viewer');

-- User status
CREATE TYPE user_status AS ENUM ('active', 'inactive', 'suspended');
```

### Transaction and Payment ENUMs

```sql
-- Payment method types
CREATE TYPE payment_type AS ENUM ('cash', 'credit_card', 'debit_card', 'mobile_payment', 'bank_transfer', 'check', 'voucher', 'other');

-- Payment status
CREATE TYPE payment_status AS ENUM ('pending', 'completed', 'failed', 'refunded', 'cancelled');

-- Sale status
CREATE TYPE sale_status AS ENUM ('draft', 'completed', 'cancelled', 'refunded', 'partially_refunded');

-- Inventory transaction types
CREATE TYPE inventory_transaction_type AS ENUM ('purchase', 'sale', 'adjustment', 'transfer', 'return', 'waste', 'stock_take');

-- Purchase order status
CREATE TYPE purchase_status AS ENUM ('draft', 'pending', 'approved', 'received', 'cancelled');
```

### Audit and System ENUMs

```sql
-- Audit action types
CREATE TYPE audit_action AS ENUM ('create', 'update', 'delete', 'login', 'logout', 'price_change', 'stock_adjustment', 'refund');

-- Shift status
CREATE TYPE shift_status AS ENUM ('open', 'closed', 'reconciled');
```

---

## Core Tables

### 1. **users** - System Users
Stores all system users with their authentication and profile information.

### 2. **roles** - User Roles and Permissions
Defines role-based access control with granular permissions.

### 3. **categories** - Product Categories
Hierarchical product categorization system.

### 4. **products** - Products/Services
Main product catalog with pricing and inventory tracking.

### 5. **suppliers** - Supplier Information
Vendor and supplier management.

### 6. **customers** - Customer Information
Customer database with loyalty and contact information.

### 7. **currencies** - Currency Types
Multi-currency support for international operations.

### 8. **exchange_rates** - Currency Exchange Rates
Historical exchange rates for currency conversion.

### 9. **payment_methods** - Payment Method Configuration
Available payment methods and their settings.

### 10. **shifts** - Cashier Shifts
Track cashier shifts and cash drawer operations.

### 11. **sales** - Sales Transactions
Main sales transaction header.

### 12. **sale_items** - Sales Line Items
Individual items within each sale transaction.

### 13. **purchases** - Purchase Orders
Purchase orders from suppliers.

### 14. **purchase_items** - Purchase Order Line Items
Individual items within purchase orders.

### 15. **inventory_transactions** - Inventory Movement Log
Complete audit trail of all inventory movements.

### 16. **audit_logs** - System Audit Trail
Comprehensive audit logging for all system activities.

---

## Complete DDL

### Extensions

```sql
-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
```

### ENUM Types Creation

```sql
-- User and Permission ENUMs
CREATE TYPE user_role AS ENUM ('admin', 'manager', 'cashier', 'warehouse', 'viewer');
CREATE TYPE user_status AS ENUM ('active', 'inactive', 'suspended');

-- Transaction and Payment ENUMs
CREATE TYPE payment_type AS ENUM ('cash', 'credit_card', 'debit_card', 'mobile_payment', 'bank_transfer', 'check', 'voucher', 'other');
CREATE TYPE payment_status AS ENUM ('pending', 'completed', 'failed', 'refunded', 'cancelled');
CREATE TYPE sale_status AS ENUM ('draft', 'completed', 'cancelled', 'refunded', 'partially_refunded');
CREATE TYPE inventory_transaction_type AS ENUM ('purchase', 'sale', 'adjustment', 'transfer', 'return', 'waste', 'stock_take');
CREATE TYPE purchase_status AS ENUM ('draft', 'pending', 'approved', 'received', 'cancelled');

-- Audit and System ENUMs
CREATE TYPE audit_action AS ENUM ('create', 'update', 'delete', 'login', 'logout', 'price_change', 'stock_adjustment', 'refund');
CREATE TYPE shift_status AS ENUM ('open', 'closed', 'reconciled');
```

### Table Definitions

```sql
-- =====================================================
-- CURRENCIES TABLE
-- =====================================================
CREATE TABLE currencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(3) NOT NULL UNIQUE, -- ISO 4217 code (USD, EUR, etc.)
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    is_base BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    decimal_places SMALLINT DEFAULT 2 CHECK (decimal_places >= 0 AND decimal_places <= 4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID
);

COMMENT ON TABLE currencies IS 'Currency types for multi-currency support';
COMMENT ON COLUMN currencies.is_base IS 'Indicates if this is the base currency for the system';
COMMENT ON COLUMN currencies.decimal_places IS 'Number of decimal places for this currency';

-- =====================================================
-- EXCHANGE_RATES TABLE
-- =====================================================
CREATE TABLE exchange_rates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    from_currency_id UUID NOT NULL REFERENCES currencies(id) ON DELETE RESTRICT,
    to_currency_id UUID NOT NULL REFERENCES currencies(id) ON DELETE RESTRICT,
    rate NUMERIC(20, 8) NOT NULL CHECK (rate > 0),
    effective_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    UNIQUE(from_currency_id, to_currency_id, effective_date)
);

COMMENT ON TABLE exchange_rates IS 'Historical exchange rates for currency conversion';
COMMENT ON COLUMN exchange_rates.rate IS 'Exchange rate from source to target currency';

-- =====================================================
-- ROLES TABLE
-- =====================================================
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    role_type user_role NOT NULL,
    permissions JSONB DEFAULT '{}', -- Store granular permissions
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID
);

COMMENT ON TABLE roles IS 'User roles with granular permissions';
COMMENT ON COLUMN roles.permissions IS 'JSON object containing permission flags for various system operations';

-- =====================================================
-- USERS TABLE
-- =====================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    auth_user_id UUID UNIQUE, -- Link to Supabase Auth
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role_id UUID REFERENCES roles(id) ON DELETE RESTRICT,
    status user_status DEFAULT 'active',
    pin_hash VARCHAR(255), -- For quick POS login
    last_login_at TIMESTAMPTZ,
    last_login_ip INET,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMPTZ,
    profile_image_url TEXT,
    preferences JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE users IS 'System users with authentication and profile information';
COMMENT ON COLUMN users.pin_hash IS 'Hashed PIN for quick POS terminal login';
COMMENT ON COLUMN users.preferences IS 'User preferences like theme, language, default view, etc.';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp';

-- =====================================================
-- CATEGORIES TABLE
-- =====================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES categories(id) ON DELETE CASCADE,
    image_url TEXT,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE categories IS 'Hierarchical product categories';
COMMENT ON COLUMN categories.parent_id IS 'For nested categories - NULL for root categories';
COMMENT ON COLUMN categories.display_order IS 'Order in which categories are displayed';

-- =====================================================
-- SUPPLIERS TABLE
-- =====================================================
CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(20),
    mobile VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    tax_id VARCHAR(50),
    payment_terms TEXT,
    credit_limit NUMERIC(15, 2),
    currency_id UUID REFERENCES currencies(id) ON DELETE RESTRICT,
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE suppliers IS 'Supplier and vendor information';
COMMENT ON COLUMN suppliers.code IS 'Unique supplier code for easy reference';
COMMENT ON COLUMN suppliers.payment_terms IS 'Payment terms (e.g., Net 30, COD, etc.)';

-- =====================================================
-- PRODUCTS TABLE
-- =====================================================
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sku VARCHAR(100) NOT NULL UNIQUE,
    barcode VARCHAR(100) UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    supplier_id UUID REFERENCES suppliers(id) ON DELETE SET NULL,
    cost_price NUMERIC(15, 4) NOT NULL DEFAULT 0 CHECK (cost_price >= 0),
    selling_price NUMERIC(15, 4) NOT NULL CHECK (selling_price >= 0),
    tax_rate NUMERIC(5, 2) DEFAULT 0 CHECK (tax_rate >= 0 AND tax_rate <= 100),
    currency_id UUID REFERENCES currencies(id) ON DELETE RESTRICT,
    unit_of_measure VARCHAR(20) DEFAULT 'unit', -- unit, kg, lb, liter, etc.
    min_stock_level NUMERIC(15, 2) DEFAULT 0,
    max_stock_level NUMERIC(15, 2),
    current_stock NUMERIC(15, 2) DEFAULT 0 CHECK (current_stock >= 0),
    reorder_point NUMERIC(15, 2) DEFAULT 0,
    image_url TEXT,
    images JSONB DEFAULT '[]', -- Array of image URLs
    is_active BOOLEAN DEFAULT TRUE,
    is_serialized BOOLEAN DEFAULT FALSE,
    track_inventory BOOLEAN DEFAULT TRUE,
    allow_negative_stock BOOLEAN DEFAULT FALSE,
    attributes JSONB DEFAULT '{}', -- Custom attributes (size, color, etc.)
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE products IS 'Product catalog with inventory tracking';
COMMENT ON COLUMN products.sku IS 'Stock Keeping Unit - unique identifier';
COMMENT ON COLUMN products.barcode IS 'Product barcode (EAN, UPC, etc.)';
COMMENT ON COLUMN products.is_serialized IS 'Whether product uses serial numbers';
COMMENT ON COLUMN products.track_inventory IS 'Whether to track inventory for this product';
COMMENT ON COLUMN products.attributes IS 'JSON storage for custom product attributes';

-- =====================================================
-- CUSTOMERS TABLE
-- =====================================================
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    company_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(20),
    mobile VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    tax_id VARCHAR(50),
    date_of_birth DATE,
    loyalty_points INTEGER DEFAULT 0 CHECK (loyalty_points >= 0),
    loyalty_tier VARCHAR(50),
    credit_limit NUMERIC(15, 2),
    current_balance NUMERIC(15, 2) DEFAULT 0,
    total_purchases NUMERIC(15, 2) DEFAULT 0,
    last_purchase_date TIMESTAMPTZ,
    notes TEXT,
    preferences JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE customers IS 'Customer information and loyalty tracking';
COMMENT ON COLUMN customers.code IS 'Unique customer code';
COMMENT ON COLUMN customers.loyalty_points IS 'Accumulated loyalty points';
COMMENT ON COLUMN customers.preferences IS 'Customer preferences and marketing opt-ins';

-- =====================================================
-- PAYMENT_METHODS TABLE
-- =====================================================
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    type payment_type NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    requires_authorization BOOLEAN DEFAULT FALSE,
    processing_fee_percent NUMERIC(5, 2) DEFAULT 0,
    processing_fee_fixed NUMERIC(10, 2) DEFAULT 0,
    configuration JSONB DEFAULT '{}', -- API keys, gateway settings, etc.
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL
);

COMMENT ON TABLE payment_methods IS 'Available payment methods configuration';
COMMENT ON COLUMN payment_methods.configuration IS 'Payment gateway configuration (encrypted sensitive data)';

-- =====================================================
-- SHIFTS TABLE
-- =====================================================
CREATE TABLE shifts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shift_number VARCHAR(50) UNIQUE NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status shift_status DEFAULT 'open',
    opened_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ,
    opening_cash NUMERIC(15, 2) DEFAULT 0,
    closing_cash NUMERIC(15, 2),
    expected_cash NUMERIC(15, 2),
    cash_difference NUMERIC(15, 2),
    total_sales NUMERIC(15, 2) DEFAULT 0,
    total_refunds NUMERIC(15, 2) DEFAULT 0,
    total_transactions INTEGER DEFAULT 0,
    payment_summary JSONB DEFAULT '{}', -- Breakdown by payment method
    notes TEXT,
    reconciled_at TIMESTAMPTZ,
    reconciled_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE shifts IS 'Cashier shifts for cash drawer management';
COMMENT ON COLUMN shifts.payment_summary IS 'JSON breakdown of payments by method';
COMMENT ON COLUMN shifts.cash_difference IS 'Difference between expected and actual cash';

-- =====================================================
-- SALES TABLE
-- =====================================================
CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    shift_id UUID REFERENCES shifts(id) ON DELETE SET NULL,
    status sale_status DEFAULT 'draft',
    sale_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    tax_amount NUMERIC(15, 2) DEFAULT 0 CHECK (tax_amount >= 0),
    discount_amount NUMERIC(15, 2) DEFAULT 0 CHECK (discount_amount >= 0),
    discount_percent NUMERIC(5, 2) DEFAULT 0 CHECK (discount_percent >= 0 AND discount_percent <= 100),
    shipping_amount NUMERIC(15, 2) DEFAULT 0 CHECK (shipping_amount >= 0),
    total_amount NUMERIC(15, 2) NOT NULL CHECK (total_amount >= 0),
    amount_paid NUMERIC(15, 2) DEFAULT 0 CHECK (amount_paid >= 0),
    amount_due NUMERIC(15, 2) DEFAULT 0,
    change_amount NUMERIC(15, 2) DEFAULT 0,
    currency_id UUID REFERENCES currencies(id) ON DELETE RESTRICT,
    exchange_rate NUMERIC(20, 8) DEFAULT 1,
    payment_method_id UUID REFERENCES payment_methods(id) ON DELETE SET NULL,
    payment_status payment_status DEFAULT 'pending',
    payment_reference VARCHAR(255),
    invoice_number VARCHAR(50),
    notes TEXT,
    internal_notes TEXT,
    metadata JSONB DEFAULT '{}', -- Custom fields, integrations, etc.
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    cancelled_by UUID REFERENCES users(id) ON DELETE SET NULL,
    cancellation_reason TEXT
);

COMMENT ON TABLE sales IS 'Sales transaction headers';
COMMENT ON COLUMN sales.sale_number IS 'Human-readable sale number (e.g., SAL-2026-00001)';
COMMENT ON COLUMN sales.exchange_rate IS 'Exchange rate used if foreign currency';
COMMENT ON COLUMN sales.metadata IS 'Additional custom data for integrations';

-- =====================================================
-- SALE_ITEMS TABLE
-- =====================================================
CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id UUID NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity NUMERIC(15, 2) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(15, 4) NOT NULL CHECK (unit_price >= 0),
    cost_price NUMERIC(15, 4) DEFAULT 0 CHECK (cost_price >= 0),
    discount_percent NUMERIC(5, 2) DEFAULT 0 CHECK (discount_percent >= 0 AND discount_percent <= 100),
    discount_amount NUMERIC(15, 2) DEFAULT 0 CHECK (discount_amount >= 0),
    tax_rate NUMERIC(5, 2) DEFAULT 0 CHECK (tax_rate >= 0 AND tax_rate <= 100),
    tax_amount NUMERIC(15, 2) DEFAULT 0 CHECK (tax_amount >= 0),
    line_total NUMERIC(15, 2) NOT NULL CHECK (line_total >= 0),
    serial_numbers JSONB DEFAULT '[]', -- Array of serial numbers if applicable
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE sale_items IS 'Individual line items within sales transactions';
COMMENT ON COLUMN sale_items.serial_numbers IS 'Serial numbers for serialized products';
COMMENT ON COLUMN sale_items.cost_price IS 'Cost price at time of sale for profit calculation';

-- =====================================================
-- PURCHASES TABLE
-- =====================================================
CREATE TABLE purchases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    purchase_number VARCHAR(50) UNIQUE NOT NULL,
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE RESTRICT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status purchase_status DEFAULT 'draft',
    order_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expected_delivery_date DATE,
    received_date TIMESTAMPTZ,
    subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    tax_amount NUMERIC(15, 2) DEFAULT 0 CHECK (tax_amount >= 0),
    shipping_amount NUMERIC(15, 2) DEFAULT 0 CHECK (shipping_amount >= 0),
    other_charges NUMERIC(15, 2) DEFAULT 0,
    discount_amount NUMERIC(15, 2) DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount NUMERIC(15, 2) NOT NULL CHECK (total_amount >= 0),
    amount_paid NUMERIC(15, 2) DEFAULT 0 CHECK (amount_paid >= 0),
    amount_due NUMERIC(15, 2) DEFAULT 0,
    currency_id UUID REFERENCES currencies(id) ON DELETE RESTRICT,
    exchange_rate NUMERIC(20, 8) DEFAULT 1,
    payment_terms TEXT,
    payment_status payment_status DEFAULT 'pending',
    supplier_invoice_number VARCHAR(100),
    notes TEXT,
    internal_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    approved_at TIMESTAMPTZ,
    approved_by UUID REFERENCES users(id) ON DELETE SET NULL,
    cancelled_at TIMESTAMPTZ,
    cancelled_by UUID REFERENCES users(id) ON DELETE SET NULL,
    cancellation_reason TEXT
);

COMMENT ON TABLE purchases IS 'Purchase orders from suppliers';
COMMENT ON COLUMN purchases.purchase_number IS 'Human-readable PO number (e.g., PO-2026-00001)';
COMMENT ON COLUMN purchases.supplier_invoice_number IS 'Supplier\'s invoice number for reference';

-- =====================================================
-- PURCHASE_ITEMS TABLE
-- =====================================================
CREATE TABLE purchase_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    purchase_id UUID NOT NULL REFERENCES purchases(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity_ordered NUMERIC(15, 2) NOT NULL CHECK (quantity_ordered > 0),
    quantity_received NUMERIC(15, 2) DEFAULT 0 CHECK (quantity_received >= 0),
    unit_cost NUMERIC(15, 4) NOT NULL CHECK (unit_cost >= 0),
    tax_rate NUMERIC(5, 2) DEFAULT 0 CHECK (tax_rate >= 0 AND tax_rate <= 100),
    tax_amount NUMERIC(15, 2) DEFAULT 0 CHECK (tax_amount >= 0),
    discount_percent NUMERIC(5, 2) DEFAULT 0 CHECK (discount_percent >= 0 AND discount_percent <= 100),
    discount_amount NUMERIC(15, 2) DEFAULT 0 CHECK (discount_amount >= 0),
    line_total NUMERIC(15, 2) NOT NULL CHECK (line_total >= 0),
    expiry_date DATE,
    batch_number VARCHAR(100),
    serial_numbers JSONB DEFAULT '[]',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE purchase_items IS 'Individual line items within purchase orders';
COMMENT ON COLUMN purchase_items.quantity_received IS 'Actual quantity received (may differ from ordered)';
COMMENT ON COLUMN purchase_items.batch_number IS 'Batch or lot number from supplier';

-- =====================================================
-- INVENTORY_TRANSACTIONS TABLE
-- =====================================================
CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_number VARCHAR(50) UNIQUE NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    transaction_type inventory_transaction_type NOT NULL,
    quantity NUMERIC(15, 2) NOT NULL,
    unit_cost NUMERIC(15, 4),
    reference_type VARCHAR(50), -- 'sale', 'purchase', 'adjustment', etc.
    reference_id UUID, -- ID of related transaction
    previous_stock NUMERIC(15, 2),
    new_stock NUMERIC(15, 2),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    transaction_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notes TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE inventory_transactions IS 'Complete audit trail of inventory movements';
COMMENT ON COLUMN inventory_transactions.reference_type IS 'Type of related document (sale, purchase, adjustment)';
COMMENT ON COLUMN inventory_transactions.reference_id IS 'ID of related transaction document';
COMMENT ON COLUMN inventory_transactions.quantity IS 'Quantity change (positive or negative)';

-- =====================================================
-- AUDIT_LOGS TABLE
-- =====================================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action audit_action NOT NULL,
    entity_type VARCHAR(100) NOT NULL, -- Table name
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(255),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    metadata JSONB DEFAULT '{}'
);

COMMENT ON TABLE audit_logs IS 'Comprehensive audit trail for all system activities';
COMMENT ON COLUMN audit_logs.entity_type IS 'Name of the table/entity being audited';
COMMENT ON COLUMN audit_logs.old_values IS 'Previous values before change (for updates)';
COMMENT ON COLUMN audit_logs.new_values IS 'New values after change';
```

---

## Indexes

```sql
-- Users indexes
CREATE INDEX idx_users_username ON users(username) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_auth_user_id ON users(auth_user_id);

-- Products indexes
CREATE INDEX idx_products_sku ON products(sku) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_barcode ON products(barcode) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_supplier_id ON products(supplier_id);
CREATE INDEX idx_products_name ON products USING gin(to_tsvector('english', name));
CREATE INDEX idx_products_is_active ON products(is_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_current_stock ON products(current_stock) WHERE track_inventory = TRUE;

-- Categories indexes
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_name ON categories(name);
CREATE INDEX idx_categories_is_active ON categories(is_active) WHERE deleted_at IS NULL;

-- Customers indexes
CREATE INDEX idx_customers_email ON customers(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_code ON customers(code) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_name ON customers USING gin(
    to_tsvector('english', COALESCE(first_name, '') || ' ' || COALESCE(last_name, '') || ' ' || COALESCE(company_name, ''))
);

-- Suppliers indexes
CREATE INDEX idx_suppliers_code ON suppliers(code) WHERE deleted_at IS NULL;
CREATE INDEX idx_suppliers_name ON suppliers(name);
CREATE INDEX idx_suppliers_is_active ON suppliers(is_active) WHERE deleted_at IS NULL;

-- Sales indexes
CREATE INDEX idx_sales_sale_number ON sales(sale_number);
CREATE INDEX idx_sales_customer_id ON sales(customer_id);
CREATE INDEX idx_sales_user_id ON sales(user_id);
CREATE INDEX idx_sales_shift_id ON sales(shift_id);
CREATE INDEX idx_sales_status ON sales(status);
CREATE INDEX idx_sales_sale_date ON sales(sale_date);
CREATE INDEX idx_sales_payment_status ON sales(payment_status);
CREATE INDEX idx_sales_created_at ON sales(created_at);

-- Sale Items indexes
CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items(product_id);

-- Purchases indexes
CREATE INDEX idx_purchases_purchase_number ON purchases(purchase_number);
CREATE INDEX idx_purchases_supplier_id ON purchases(supplier_id);
CREATE INDEX idx_purchases_user_id ON purchases(user_id);
CREATE INDEX idx_purchases_status ON purchases(status);
CREATE INDEX idx_purchases_order_date ON purchases(order_date);
CREATE INDEX idx_purchases_expected_delivery_date ON purchases(expected_delivery_date);

-- Purchase Items indexes
CREATE INDEX idx_purchase_items_purchase_id ON purchase_items(purchase_id);
CREATE INDEX idx_purchase_items_product_id ON purchase_items(product_id);

-- Inventory Transactions indexes
CREATE INDEX idx_inventory_transactions_product_id ON inventory_transactions(product_id);
CREATE INDEX idx_inventory_transactions_transaction_type ON inventory_transactions(transaction_type);
CREATE INDEX idx_inventory_transactions_transaction_date ON inventory_transactions(transaction_date);
CREATE INDEX idx_inventory_transactions_reference ON inventory_transactions(reference_type, reference_id);
CREATE INDEX idx_inventory_transactions_user_id ON inventory_transactions(user_id);

-- Shifts indexes
CREATE INDEX idx_shifts_user_id ON shifts(user_id);
CREATE INDEX idx_shifts_status ON shifts(status);
CREATE INDEX idx_shifts_opened_at ON shifts(opened_at);
CREATE INDEX idx_shifts_closed_at ON shifts(closed_at);

-- Exchange Rates indexes
CREATE INDEX idx_exchange_rates_from_currency ON exchange_rates(from_currency_id);
CREATE INDEX idx_exchange_rates_to_currency ON exchange_rates(to_currency_id);
CREATE INDEX idx_exchange_rates_effective_date ON exchange_rates(effective_date);

-- Audit Logs indexes
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

-- Payment Methods indexes
CREATE INDEX idx_payment_methods_type ON payment_methods(type);
CREATE INDEX idx_payment_methods_is_active ON payment_methods(is_active);

-- Currencies indexes
CREATE INDEX idx_currencies_code ON currencies(code);
CREATE INDEX idx_currencies_is_active ON currencies(is_active);
CREATE INDEX idx_currencies_is_base ON currencies(is_base) WHERE is_base = TRUE;
```

---

## Foreign Key Relationships

### Users & Roles
- `users.role_id` → `roles.id`
- `users.created_by` → `users.id`
- `users.updated_by` → `users.id`

### Products
- `products.category_id` → `categories.id`
- `products.supplier_id` → `suppliers.id`
- `products.currency_id` → `currencies.id`
- `products.created_by` → `users.id`
- `products.updated_by` → `users.id`

### Categories
- `categories.parent_id` → `categories.id` (self-referencing)
- `categories.created_by` → `users.id`
- `categories.updated_by` → `users.id`

### Suppliers
- `suppliers.currency_id` → `currencies.id`
- `suppliers.created_by` → `users.id`
- `suppliers.updated_by` → `users.id`

### Customers
- `customers.created_by` → `users.id`
- `customers.updated_by` → `users.id`

### Sales
- `sales.customer_id` → `customers.id`
- `sales.user_id` → `users.id`
- `sales.shift_id` → `shifts.id`
- `sales.currency_id` → `currencies.id`
- `sales.payment_method_id` → `payment_methods.id`
- `sales.cancelled_by` → `users.id`

### Sale Items
- `sale_items.sale_id` → `sales.id` (CASCADE delete)
- `sale_items.product_id` → `products.id`

### Purchases
- `purchases.supplier_id` → `suppliers.id`
- `purchases.user_id` → `users.id`
- `purchases.currency_id` → `currencies.id`
- `purchases.approved_by` → `users.id`
- `purchases.cancelled_by` → `users.id`

### Purchase Items
- `purchase_items.purchase_id` → `purchases.id` (CASCADE delete)
- `purchase_items.product_id` → `products.id`

### Inventory Transactions
- `inventory_transactions.product_id` → `products.id`
- `inventory_transactions.user_id` → `users.id`

### Shifts
- `shifts.user_id` → `users.id`
- `shifts.reconciled_by` → `users.id`

### Exchange Rates
- `exchange_rates.from_currency_id` → `currencies.id`
- `exchange_rates.to_currency_id` → `currencies.id`

### Payment Methods
- `payment_methods.created_by` → `users.id`
- `payment_methods.updated_by` → `users.id`

### Audit Logs
- `audit_logs.user_id` → `users.id`

---

## Database Diagram

```
┌─────────────────┐         ┌──────────────┐         ┌──────────────┐
│     ROLES       │◄───────┤    USERS      │────────►│  AUDIT_LOGS  │
│                 │         │               │         │              │
│ • id            │         │ • id          │         │ • id         │
│ • name          │         │ • username    │         │ • user_id    │
│ • role_type     │         │ • email       │         │ • action     │
│ • permissions   │         │ • role_id     │         │ • entity     │
└─────────────────┘         │ • status      │         └──────────────┘
                            └──────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
            ┌───────▼──────┐ ┌─────▼──────┐ ┌─────▼──────┐
            │   SHIFTS     │ │   SALES    │ │ PURCHASES  │
            │              │ │            │ │            │
            │ • id         │ │ • id       │ │ • id       │
            │ • user_id    │ │ • user_id  │ │ • user_id  │
            │ • status     │ │ • shift_id │ │ • supplier │
            │ • open_cash  │ │ • customer │ │ • status   │
            └──────────────┘ │ • status   │ └────┬───────┘
                             └─────┬──────┘      │
                                   │             │
                          ┌────────▼──────┐ ┌────▼────────────┐
                          │  SALE_ITEMS   │ │ PURCHASE_ITEMS  │
                          │               │ │                 │
                          │ • id          │ │ • id            │
                          │ • sale_id     │ │ • purchase_id   │
                          │ • product_id  │ │ • product_id    │
                          │ • quantity    │ │ • quantity      │
                          └───────┬───────┘ └────┬────────────┘
                                  │              │
                                  └──────┬───────┘
                                         │
┌──────────────┐         ┌───────────────▼─────────────┐         ┌──────────────┐
│ CATEGORIES   │◄───────┤      PRODUCTS              │────────►│  SUPPLIERS   │
│              │         │                            │         │              │
│ • id         │         │ • id                       │         │ • id         │
│ • name       │         │ • sku                      │         │ • name       │
│ • parent_id  │         │ • name                     │         │ • contact    │
└──────────────┘         │ • category_id              │         │ • tax_id     │
                         │ • supplier_id              │         └──────────────┘
                         │ • current_stock            │
                         │ • cost_price               │
                         │ • selling_price            │
                         └────────────┬───────────────┘
                                      │
                         ┌────────────▼──────────────┐
                         │ INVENTORY_TRANSACTIONS    │
                         │                           │
                         │ • id                      │
                         │ • product_id              │
                         │ • transaction_type        │
                         │ • quantity                │
                         │ • previous_stock          │
                         │ • new_stock               │
                         └───────────────────────────┘

┌──────────────┐         ┌──────────────────┐         ┌──────────────────┐
│ CURRENCIES   │◄───────┤ EXCHANGE_RATES   │────────►│ PAYMENT_METHODS  │
│              │         │                  │         │                  │
│ • id         │         │ • id             │         │ • id             │
│ • code       │         │ • from_currency  │         │ • name           │
│ • symbol     │         │ • to_currency    │         │ • type           │
│ • is_base    │         │ • rate           │         │ • is_active      │
└──────────────┘         └──────────────────┘         └──────────────────┘

┌──────────────┐
│  CUSTOMERS   │
│              │
│ • id         │
│ • email      │
│ • phone      │
│ • loyalty    │
└──────────────┘
```

---

## Triggers and Functions

### Update Timestamp Trigger

```sql
-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to all tables with updated_at column
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_suppliers_updated_at BEFORE UPDATE ON suppliers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_methods_updated_at BEFORE UPDATE ON payment_methods
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_currencies_updated_at BEFORE UPDATE ON currencies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sales_updated_at BEFORE UPDATE ON sales
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_purchases_updated_at BEFORE UPDATE ON purchases
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_purchase_items_updated_at BEFORE UPDATE ON purchase_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shifts_updated_at BEFORE UPDATE ON shifts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### Inventory Update Trigger

```sql
-- Function to update product stock and create inventory transaction
CREATE OR REPLACE FUNCTION update_product_inventory()
RETURNS TRIGGER AS $$
BEGIN
    -- Update product stock
    IF TG_OP = 'INSERT' THEN
        UPDATE products
        SET current_stock = current_stock + NEW.quantity
        WHERE id = NEW.product_id AND track_inventory = TRUE;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for inventory transactions
CREATE TRIGGER trigger_update_product_inventory
    AFTER INSERT ON inventory_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_product_inventory();
```

### Sale Calculation Trigger

```sql
-- Function to recalculate sale totals when items change
CREATE OR REPLACE FUNCTION recalculate_sale_totals()
RETURNS TRIGGER AS $$
DECLARE
    v_subtotal NUMERIC(15,2);
    v_tax_amount NUMERIC(15,2);
BEGIN
    -- Calculate new totals
    SELECT
        COALESCE(SUM(line_total), 0),
        COALESCE(SUM(tax_amount), 0)
    INTO v_subtotal, v_tax_amount
    FROM sale_items
    WHERE sale_id = COALESCE(NEW.sale_id, OLD.sale_id);

    -- Update sale record
    UPDATE sales
    SET
        subtotal = v_subtotal,
        tax_amount = v_tax_amount,
        total_amount = v_subtotal + tax_amount + shipping_amount - discount_amount,
        updated_at = NOW()
    WHERE id = COALESCE(NEW.sale_id, OLD.sale_id);

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Triggers for sale items
CREATE TRIGGER trigger_recalculate_sale_totals_insert
    AFTER INSERT ON sale_items
    FOR EACH ROW
    EXECUTE FUNCTION recalculate_sale_totals();

CREATE TRIGGER trigger_recalculate_sale_totals_update
    AFTER UPDATE ON sale_items
    FOR EACH ROW
    EXECUTE FUNCTION recalculate_sale_totals();

CREATE TRIGGER trigger_recalculate_sale_totals_delete
    AFTER DELETE ON sale_items
    FOR EACH ROW
    EXECUTE FUNCTION recalculate_sale_totals();
```

### Audit Log Trigger

```sql
-- Function to create audit log entries
CREATE OR REPLACE FUNCTION create_audit_log()
RETURNS TRIGGER AS $$
DECLARE
    v_action audit_action;
    v_old_values JSONB;
    v_new_values JSONB;
BEGIN
    -- Determine action
    IF TG_OP = 'INSERT' THEN
        v_action := 'create';
        v_old_values := NULL;
        v_new_values := to_jsonb(NEW);
    ELSIF TG_OP = 'UPDATE' THEN
        v_action := 'update';
        v_old_values := to_jsonb(OLD);
        v_new_values := to_jsonb(NEW);
    ELSIF TG_OP = 'DELETE' THEN
        v_action := 'delete';
        v_old_values := to_jsonb(OLD);
        v_new_values := NULL;
    END IF;

    -- Insert audit log
    INSERT INTO audit_logs (
        user_id,
        action,
        entity_type,
        entity_id,
        old_values,
        new_values
    ) VALUES (
        COALESCE(NEW.updated_by, NEW.created_by, OLD.updated_by),
        v_action,
        TG_TABLE_NAME,
        COALESCE(NEW.id, OLD.id),
        v_old_values,
        v_new_values
    );

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Apply audit trigger to key tables (add as needed)
CREATE TRIGGER trigger_audit_products
    AFTER INSERT OR UPDATE OR DELETE ON products
    FOR EACH ROW
    EXECUTE FUNCTION create_audit_log();

CREATE TRIGGER trigger_audit_sales
    AFTER INSERT OR UPDATE OR DELETE ON sales
    FOR EACH ROW
    EXECUTE FUNCTION create_audit_log();

CREATE TRIGGER trigger_audit_purchases
    AFTER INSERT OR UPDATE OR DELETE ON purchases
    FOR EACH ROW
    EXECUTE FUNCTION create_audit_log();
```

---

## Row Level Security (RLS)

```sql
-- Enable RLS on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE suppliers ENABLE ROW LEVEL SECURITY;
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE currencies ENABLE ROW LEVEL SECURITY;
ALTER TABLE exchange_rates ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_methods ENABLE ROW LEVEL SECURITY;
ALTER TABLE shifts ENABLE ROW LEVEL SECURITY;
ALTER TABLE sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE sale_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE purchases ENABLE ROW LEVEL SECURITY;
ALTER TABLE purchase_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE inventory_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- Example RLS policies (customize based on your requirements)

-- Users can read their own record
CREATE POLICY "Users can view own record" ON users
    FOR SELECT
    USING (auth.uid() = auth_user_id);

-- Admin users can view all records
CREATE POLICY "Admins can view all users" ON users
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.auth_user_id = auth.uid()
            AND r.role_type = 'admin'
        )
    );

-- Products readable by authenticated users
CREATE POLICY "Authenticated users can view products" ON products
    FOR SELECT
    USING (auth.role() = 'authenticated' AND deleted_at IS NULL);

-- Only managers and admins can create/update products
CREATE POLICY "Managers can modify products" ON products
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.auth_user_id = auth.uid()
            AND r.role_type IN ('admin', 'manager')
        )
    );

-- Sales readable by all authenticated users
CREATE POLICY "Users can view sales" ON sales
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- Cashiers can create sales
CREATE POLICY "Cashiers can create sales" ON sales
    FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.auth_user_id = auth.uid()
            AND r.role_type IN ('admin', 'manager', 'cashier')
        )
    );

-- Audit logs are read-only for admins
CREATE POLICY "Admins can view audit logs" ON audit_logs
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.auth_user_id = auth.uid()
            AND r.role_type = 'admin'
        )
    );
```

---

## Initial Data Seeds

```sql
-- Insert base currency
INSERT INTO currencies (code, name, symbol, is_base, is_active) VALUES
('USD', 'US Dollar', '$', TRUE, TRUE),
('EUR', 'Euro', '€', FALSE, TRUE),
('GBP', 'British Pound', '£', FALSE, TRUE);

-- Insert default roles
INSERT INTO roles (name, description, role_type, permissions) VALUES
('Administrator', 'Full system access', 'admin', '{"all": true}'::jsonb),
('Store Manager', 'Manage store operations', 'manager', '{"sales": true, "inventory": true, "reports": true}'::jsonb),
('Cashier', 'Process sales', 'cashier', '{"sales": true}'::jsonb),
('Warehouse Staff', 'Manage inventory', 'warehouse', '{"inventory": true, "purchases": true}'::jsonb),
('Viewer', 'Read-only access', 'viewer', '{"reports": true}'::jsonb);

-- Insert default payment methods
INSERT INTO payment_methods (name, type, is_active, display_order) VALUES
('Cash', 'cash', TRUE, 1),
('Credit Card', 'credit_card', TRUE, 2),
('Debit Card', 'debit_card', TRUE, 3),
('Mobile Payment', 'mobile_payment', TRUE, 4);

-- Insert sample category
INSERT INTO categories (name, description, is_active, display_order) VALUES
('General', 'General products category', TRUE, 1);
```

---

## Performance Optimization Tips

1. **Partitioning**: Consider partitioning large tables like `sales`, `audit_logs`, and `inventory_transactions` by date
2. **Archiving**: Implement archiving strategy for old transactions (e.g., move records older than 2 years to archive tables)
3. **Materialized Views**: Create materialized views for complex reports and dashboard queries
4. **Query Optimization**: Use EXPLAIN ANALYZE to optimize slow queries
5. **Connection Pooling**: Implement connection pooling in your application layer
6. **Vacuum**: Ensure regular VACUUM and ANALYZE runs for optimal performance

---

## Backup and Maintenance

```sql
-- Example maintenance queries

-- Check table sizes
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Check index usage
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;

-- Find missing indexes
SELECT
    schemaname,
    tablename,
    seq_scan,
    seq_tup_read,
    idx_scan,
    seq_tup_read / seq_scan AS avg_seq_tuples
FROM pg_stat_user_tables
WHERE seq_scan > 0
    AND schemaname = 'public'
ORDER BY seq_tup_read DESC;
```

---

## Next Steps

1. Review the schema and adjust based on specific business requirements
2. Apply the DDL to create the database structure
3. Set up appropriate RLS policies based on your security requirements
4. Create additional indexes based on query patterns
5. Implement database functions for complex business logic
6. Set up regular backup schedules
7. Create database views for common reporting queries
8. Document custom business rules and constraints

---

**Document Version:** 1.0
**Last Updated:** 2026-03-12
**Author:** Claude Code
**Project:** Vibely POS System
