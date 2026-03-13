-- ============================================================================
-- DATABASE SCHEMA - POS System (Figma-Derived)
-- ============================================================================
-- Project: vibely-pos (Kotlin Multiplatform)
-- Database: PostgreSQL 17.6 (Supabase)
-- Date: 2026-03-12
-- Source: Figma Make analysis
-- Tables: 8 core tables (simplified from original 23)
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. CATEGORIES
-- ============================================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    color TEXT NOT NULL DEFAULT '#000000',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE categories IS 'Product categories with UI theming colors';
COMMENT ON COLUMN categories.color IS 'Hex color code for category badge/UI (e.g., #000000)';

-- Indexes
CREATE INDEX idx_categories_name ON categories(name) WHERE deleted_at IS NULL;
CREATE INDEX idx_categories_deleted ON categories(deleted_at);

-- ============================================================================
-- 2. PRODUCTS
-- ============================================================================
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    sku TEXT NOT NULL UNIQUE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    size TEXT,
    weight TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE products IS 'Product inventory with stock tracking';
COMMENT ON COLUMN products.sku IS 'Stock Keeping Unit - unique identifier (e.g., ELEC-001)';
COMMENT ON COLUMN products.size IS 'Physical dimensions (e.g., "10x5x3 cm")';
COMMENT ON COLUMN products.weight IS 'Product weight (e.g., "0.5 kg")';

-- Indexes
CREATE INDEX idx_products_category ON products(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_sku ON products(sku) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_name ON products(name) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_stock_low ON products(stock) WHERE stock < 20 AND deleted_at IS NULL;
CREATE INDEX idx_products_deleted ON products(deleted_at);

-- Full-text search index for product search
CREATE INDEX idx_products_search ON products USING gin(to_tsvector('english', name || ' ' || sku));

-- ============================================================================
-- 3. CUSTOMERS
-- ============================================================================
CREATE TYPE customer_tier AS ENUM ('bronze', 'silver', 'gold');

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    phone TEXT NOT NULL UNIQUE,
    loyalty_points INTEGER NOT NULL DEFAULT 0 CHECK (loyalty_points >= 0),
    total_spent DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (total_spent >= 0),
    visit_count INTEGER NOT NULL DEFAULT 0 CHECK (visit_count >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE customers IS 'Customer records with loyalty program';
COMMENT ON COLUMN customers.email IS 'Optional - phone is primary identifier';
COMMENT ON COLUMN customers.loyalty_points IS 'Accumulated loyalty points for rewards';
COMMENT ON COLUMN customers.total_spent IS 'Lifetime spending total';
COMMENT ON COLUMN customers.visit_count IS 'Number of transactions/visits';

-- Indexes
CREATE INDEX idx_customers_phone ON customers(phone) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_email ON customers(email) WHERE deleted_at IS NULL AND email IS NOT NULL;
CREATE INDEX idx_customers_tier ON customers(loyalty_points) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_deleted ON customers(deleted_at);

-- Function to compute tier dynamically
CREATE OR REPLACE FUNCTION get_customer_tier(points INTEGER)
RETURNS customer_tier AS $$
BEGIN
    IF points >= 1000 THEN
        RETURN 'gold'::customer_tier;
    ELSIF points >= 500 THEN
        RETURN 'silver'::customer_tier;
    ELSE
        RETURN 'bronze'::customer_tier;
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- ============================================================================
-- 4. SUPPLIERS
-- ============================================================================
CREATE TYPE supplier_status AS ENUM ('active', 'inactive');

CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    contact_person TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT NOT NULL,
    address TEXT,
    status supplier_status NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE suppliers IS 'Supplier/vendor management';
COMMENT ON COLUMN suppliers.contact_person IS 'Primary contact name at supplier';

-- Indexes
CREATE INDEX idx_suppliers_name ON suppliers(name) WHERE deleted_at IS NULL;
CREATE INDEX idx_suppliers_status ON suppliers(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_suppliers_deleted ON suppliers(deleted_at);

-- ============================================================================
-- 5. PURCHASE ORDERS
-- ============================================================================
CREATE TYPE purchase_order_status AS ENUM ('pending', 'received', 'cancelled');

CREATE TABLE purchase_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    po_number TEXT NOT NULL UNIQUE,
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE RESTRICT,
    order_date DATE NOT NULL,
    total_items INTEGER NOT NULL CHECK (total_items > 0),
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    status purchase_order_status NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE purchase_orders IS 'Purchase orders from suppliers';
COMMENT ON COLUMN purchase_orders.po_number IS 'Format: PO-YYYY-NNN (e.g., PO-2024-001)';
COMMENT ON COLUMN purchase_orders.total_items IS 'Total quantity of items in order';

-- Indexes
CREATE INDEX idx_po_number ON purchase_orders(po_number) WHERE deleted_at IS NULL;
CREATE INDEX idx_po_supplier ON purchase_orders(supplier_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_po_status ON purchase_orders(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_po_date ON purchase_orders(order_date DESC);
CREATE INDEX idx_po_deleted ON purchase_orders(deleted_at);

-- Function to generate next PO number
CREATE OR REPLACE FUNCTION generate_po_number()
RETURNS TEXT AS $$
DECLARE
    current_year INTEGER := EXTRACT(YEAR FROM NOW());
    next_seq INTEGER;
BEGIN
    SELECT COALESCE(MAX(
        CAST(SUBSTRING(po_number FROM 'PO-\d{4}-(\d+)') AS INTEGER)
    ), 0) + 1
    INTO next_seq
    FROM purchase_orders
    WHERE po_number LIKE 'PO-' || current_year || '-%';

    RETURN 'PO-' || current_year || '-' || LPAD(next_seq::TEXT, 3, '0');
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 6. USERS (Cashiers/Staff)
-- ============================================================================
CREATE TYPE user_role AS ENUM ('admin', 'cashier', 'manager');

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    role user_role NOT NULL DEFAULT 'cashier',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE users IS 'System users (cashiers, managers, admins)';
COMMENT ON COLUMN users.role IS 'User access level';

-- Indexes
CREATE INDEX idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role ON users(role) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_deleted ON users(deleted_at);

-- ============================================================================
-- 7. SALES (Transactions)
-- ============================================================================
CREATE TYPE payment_method AS ENUM ('cash', 'card', 'digital');

CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_number TEXT NOT NULL UNIQUE,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    cashier_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    sale_date DATE NOT NULL,
    sale_time TIME NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    tax DECIMAL(10,2) NOT NULL CHECK (tax >= 0),
    total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    payment_method payment_method NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE sales IS 'Completed sales transactions';
COMMENT ON COLUMN sales.invoice_number IS 'Format: INV-YYYY-NNNN (e.g., INV-2024-0345)';
COMMENT ON COLUMN sales.customer_id IS 'Optional - links to customer if provided';
COMMENT ON COLUMN sales.sale_date IS 'Date of transaction';
COMMENT ON COLUMN sales.sale_time IS 'Time of transaction (HH:mm)';

-- Indexes
CREATE INDEX idx_sales_invoice ON sales(invoice_number) WHERE deleted_at IS NULL;
CREATE INDEX idx_sales_customer ON sales(customer_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_sales_cashier ON sales(cashier_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_sales_date ON sales(sale_date DESC);
CREATE INDEX idx_sales_payment ON sales(payment_method) WHERE deleted_at IS NULL;
CREATE INDEX idx_sales_deleted ON sales(deleted_at);

-- Function to generate next invoice number
CREATE OR REPLACE FUNCTION generate_invoice_number()
RETURNS TEXT AS $$
DECLARE
    current_year INTEGER := EXTRACT(YEAR FROM NOW());
    next_seq INTEGER;
BEGIN
    SELECT COALESCE(MAX(
        CAST(SUBSTRING(invoice_number FROM 'INV-\d{4}-(\d+)') AS INTEGER)
    ), 0) + 1
    INTO next_seq
    FROM sales
    WHERE invoice_number LIKE 'INV-' || current_year || '-%';

    RETURN 'INV-' || current_year || '-' || LPAD(next_seq::TEXT, 4, '0');
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 8. SALE ITEMS (Line Items)
-- ============================================================================
CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id UUID NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE sale_items IS 'Individual products in each sale transaction';
COMMENT ON COLUMN sale_items.unit_price IS 'Price at time of sale (may differ from current product price)';
COMMENT ON COLUMN sale_items.subtotal IS 'quantity * unit_price';

-- Indexes
CREATE INDEX idx_sale_items_sale ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product ON sale_items(product_id);
CREATE INDEX idx_sale_items_date ON sale_items(created_at DESC);

-- ============================================================================
-- TRIGGERS: Auto-update updated_at
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER suppliers_updated_at BEFORE UPDATE ON suppliers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER purchase_orders_updated_at BEFORE UPDATE ON purchase_orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER sales_updated_at BEFORE UPDATE ON sales
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ============================================================================
-- TRIGGERS: Customer Stats Auto-Update
-- ============================================================================
CREATE OR REPLACE FUNCTION update_customer_stats()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE customers
    SET
        total_spent = COALESCE((
            SELECT SUM(total)
            FROM sales
            WHERE customer_id = NEW.customer_id
            AND deleted_at IS NULL
        ), 0),
        visit_count = COALESCE((
            SELECT COUNT(*)
            FROM sales
            WHERE customer_id = NEW.customer_id
            AND deleted_at IS NULL
        ), 0)
    WHERE id = NEW.customer_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER sales_update_customer_stats AFTER INSERT OR UPDATE ON sales
    FOR EACH ROW
    WHEN (NEW.customer_id IS NOT NULL)
    EXECUTE FUNCTION update_customer_stats();

-- ============================================================================
-- VIEWS: Business Logic
-- ============================================================================

-- Category with product count
CREATE OR REPLACE VIEW categories_with_counts AS
SELECT
    c.*,
    COUNT(p.id) as product_count
FROM categories c
LEFT JOIN products p ON p.category_id = c.id AND p.deleted_at IS NULL
WHERE c.deleted_at IS NULL
GROUP BY c.id;

-- Products with low stock alert
CREATE OR REPLACE VIEW products_low_stock AS
SELECT
    p.*,
    c.name as category_name,
    CASE
        WHEN p.stock < 20 THEN 'Low'
        WHEN p.stock < 50 THEN 'Medium'
        ELSE 'Good'
    END as stock_status
FROM products p
JOIN categories c ON c.id = p.category_id
WHERE p.deleted_at IS NULL
AND p.stock < 20
ORDER BY p.stock ASC;

-- Sales with customer tier
CREATE OR REPLACE VIEW sales_with_details AS
SELECT
    s.*,
    c.name as customer_name,
    get_customer_tier(c.loyalty_points) as customer_tier,
    u.name as cashier_name,
    (SELECT COUNT(*) FROM sale_items WHERE sale_id = s.id) as item_count
FROM sales s
LEFT JOIN customers c ON c.id = s.customer_id
JOIN users u ON u.id = s.cashier_id
WHERE s.deleted_at IS NULL
ORDER BY s.sale_date DESC, s.sale_time DESC;

-- ============================================================================
-- SEED DATA (Sample)
-- ============================================================================

-- Default categories
INSERT INTO categories (name, description, color) VALUES
('Electronics', 'Electronic devices and components', '#000000'),
('Accessories', 'Add-ons and supplementary items', '#4B5563'),
('Tools', 'Hand tools and power tools', '#9CA3AF'),
('Supplies', 'General supplies and materials', '#D1D5DB');

-- Default admin user (password should be set via Supabase Auth)
INSERT INTO users (name, email, role) VALUES
('Admin User', 'admin@pos.local', 'admin'),
('John Doe', 'john@pos.local', 'cashier'),
('Sarah Manager', 'sarah@pos.local', 'manager');

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) - To be configured in separate migration
-- ============================================================================
-- RLS policies will be created after Supabase Auth integration
-- For now, all tables are accessible to authenticated users

COMMENT ON DATABASE postgres IS 'POS System Database - 8 core tables';
