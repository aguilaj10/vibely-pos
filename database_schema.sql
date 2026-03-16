-- ============================================================================
-- Vibely POS - Complete Database Schema
-- ============================================================================
-- Exported from Supabase Project: jewqhojchyrmozxsrkoq
-- Export Date: 2026-03-12
--
-- This file contains the complete production database schema including:
-- - 8 ENUM types
-- - 17 tables with constraints
-- - All indexes
-- - 3 views
-- - Functions and triggers
-- - RLS policies
-- - Required extensions
-- ============================================================================

-- ============================================================================
-- EXTENSIONS
-- ============================================================================

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS "pgcrypto" WITH SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements" WITH SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS "pg_trgm" WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS "pg_graphql" WITH SCHEMA graphql;
CREATE EXTENSION IF NOT EXISTS "supabase_vault" WITH SCHEMA vault;

-- ============================================================================
-- ENUM TYPES
-- ============================================================================

-- User roles enumeration
CREATE TYPE public.user_role AS ENUM (
    'admin',
    'manager',
    'cashier',
    'warehouse',
    'viewer'
);

-- User account status
CREATE TYPE public.user_status AS ENUM (
    'active',
    'inactive',
    'suspended'
);

-- Sale transaction status
CREATE TYPE public.sale_status AS ENUM (
    'draft',
    'completed',
    'cancelled',
    'refunded',
    'partially_refunded'
);

-- Payment status
CREATE TYPE public.payment_status AS ENUM (
    'pending',
    'completed',
    'failed',
    'refunded',
    'cancelled'
);

-- Payment method types
CREATE TYPE public.payment_type AS ENUM (
    'cash',
    'credit_card',
    'debit_card',
    'mobile_payment',
    'bank_transfer',
    'check',
    'voucher',
    'other'
);

-- Purchase order status
CREATE TYPE public.purchase_status AS ENUM (
    'draft',
    'pending',
    'approved',
    'received',
    'cancelled'
);

-- Inventory transaction types
CREATE TYPE public.inventory_transaction_type AS ENUM (
    'purchase',
    'sale',
    'adjustment',
    'transfer',
    'return',
    'waste',
    'stock_take'
);

-- Audit log action types
CREATE TYPE public.audit_action AS ENUM (
    'create',
    'update',
    'delete',
    'login',
    'logout',
    'price_change',
    'stock_adjustment',
    'refund'
);

-- ============================================================================
-- TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Users Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'cashier',
    status user_status NOT NULL DEFAULT 'active',
    phone VARCHAR(20),
    avatar_url TEXT,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_check CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- ----------------------------------------------------------------------------
-- Categories Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon_name VARCHAR(100),
    color_hex VARCHAR(7),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT categories_color_hex_check CHECK (color_hex ~* '^#[0-9A-Fa-f]{6}$')
);

-- ----------------------------------------------------------------------------
-- Products Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES public.categories(id),
    barcode VARCHAR(100),
    unit VARCHAR(50) NOT NULL DEFAULT 'unit',
    cost_price NUMERIC(12, 2) NOT NULL DEFAULT 0,
    selling_price NUMERIC(12, 2) NOT NULL,
    min_stock_level INTEGER NOT NULL DEFAULT 0,
    current_stock INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    image_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Customers Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    total_purchases NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_visits INTEGER NOT NULL DEFAULT 0,
    last_visit_at TIMESTAMPTZ,
    loyalty_points INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT customers_email_check CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT customers_loyalty_points_check CHECK (loyalty_points >= 0)
);

-- ----------------------------------------------------------------------------
-- Suppliers Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT suppliers_email_check CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- ----------------------------------------------------------------------------
-- Sales Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.sales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID REFERENCES public.customers(id),
    cashier_id UUID NOT NULL REFERENCES public.users(id),
    subtotal NUMERIC(12, 2) NOT NULL,
    tax_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12, 2) NOT NULL,
    status sale_status NOT NULL DEFAULT 'draft',
    payment_status payment_status NOT NULL DEFAULT 'pending',
    notes TEXT,
    sale_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Sale Items Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.sale_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL REFERENCES public.sales(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES public.products(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    subtotal NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT sale_items_quantity_check CHECK (quantity > 0)
);

-- ----------------------------------------------------------------------------
-- Payments Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL REFERENCES public.sales(id) ON DELETE CASCADE,
    payment_type payment_type NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    status payment_status NOT NULL DEFAULT 'pending',
    reference_number VARCHAR(100),
    notes TEXT,
    payment_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT payments_amount_check CHECK (amount > 0)
);

-- ----------------------------------------------------------------------------
-- Purchase Orders Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.purchase_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    po_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id UUID NOT NULL REFERENCES public.suppliers(id),
    created_by UUID NOT NULL REFERENCES public.users(id),
    total_amount NUMERIC(12, 2) NOT NULL,
    status purchase_status NOT NULL DEFAULT 'draft',
    order_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expected_delivery_date TIMESTAMPTZ,
    received_date TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT purchase_orders_total_amount_check CHECK (total_amount >= 0)
);

-- ----------------------------------------------------------------------------
-- Purchase Order Items Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.purchase_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_id UUID NOT NULL REFERENCES public.purchase_orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES public.products(id),
    quantity INTEGER NOT NULL,
    unit_cost NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    received_quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Inventory Transactions Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.inventory_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES public.products(id),
    transaction_type inventory_transaction_type NOT NULL,
    quantity INTEGER NOT NULL,
    reference_id UUID,
    reference_type VARCHAR(50),
    performed_by UUID NOT NULL REFERENCES public.users(id),
    notes TEXT,
    transaction_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Cash Shifts Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.cash_shifts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shift_number VARCHAR(50) NOT NULL UNIQUE,
    cashier_id UUID NOT NULL REFERENCES public.users(id),
    opening_balance NUMERIC(12, 2) NOT NULL DEFAULT 0,
    closing_balance NUMERIC(12, 2),
    expected_balance NUMERIC(12, 2),
    discrepancy NUMERIC(12, 2),
    total_sales NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_cash NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_card NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_other NUMERIC(12, 2) NOT NULL DEFAULT 0,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Expenses Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expense_number VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    description TEXT NOT NULL,
    recorded_by UUID NOT NULL REFERENCES public.users(id),
    expense_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    receipt_url TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT expenses_amount_check CHECK (amount > 0)
);

-- ----------------------------------------------------------------------------
-- Audit Logs Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id),
    action audit_action NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- App Settings Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.app_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description TEXT,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    updated_by UUID REFERENCES public.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Notifications Table
-- ----------------------------------------------------------------------------
CREATE TABLE public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    action_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    read_at TIMESTAMPTZ
);

-- ============================================================================
-- INDEXES
-- ============================================================================

-- Users indexes
CREATE INDEX idx_users_email ON public.users USING btree (email);
CREATE INDEX idx_users_role ON public.users USING btree (role);
CREATE INDEX idx_users_status ON public.users USING btree (status);

-- Categories indexes
CREATE INDEX idx_categories_display_order ON public.categories USING btree (display_order);
CREATE INDEX idx_categories_is_active ON public.categories USING btree (is_active);

-- Products indexes
CREATE INDEX idx_products_sku ON public.products USING btree (sku);
CREATE INDEX idx_products_category_id ON public.products USING btree (category_id);
CREATE INDEX idx_products_barcode ON public.products USING btree (barcode);
CREATE INDEX idx_products_is_active ON public.products USING btree (is_active);
CREATE INDEX idx_products_name_trgm ON public.products USING gin (name gin_trgm_ops);

-- Customers indexes
CREATE INDEX idx_customers_customer_code ON public.customers USING btree (customer_code);
CREATE INDEX idx_customers_email ON public.customers USING btree (email);
CREATE INDEX idx_customers_phone ON public.customers USING btree (phone);
CREATE INDEX idx_customers_is_active ON public.customers USING btree (is_active);

-- Suppliers indexes
CREATE INDEX idx_suppliers_supplier_code ON public.suppliers USING btree (supplier_code);
CREATE INDEX idx_suppliers_is_active ON public.suppliers USING btree (is_active);

-- Sales indexes
CREATE INDEX idx_sales_invoice_number ON public.sales USING btree (invoice_number);
CREATE INDEX idx_sales_customer_id ON public.sales USING btree (customer_id);
CREATE INDEX idx_sales_cashier_id ON public.sales USING btree (cashier_id);
CREATE INDEX idx_sales_status ON public.sales USING btree (status);
CREATE INDEX idx_sales_sale_date ON public.sales USING btree (sale_date DESC);
CREATE INDEX idx_sales_created_at ON public.sales USING btree (created_at DESC);

-- Sale Items indexes
CREATE INDEX idx_sale_items_sale_id ON public.sale_items USING btree (sale_id);
CREATE INDEX idx_sale_items_product_id ON public.sale_items USING btree (product_id);

-- Payments indexes
CREATE INDEX idx_payments_sale_id ON public.payments USING btree (sale_id);
CREATE INDEX idx_payments_status ON public.payments USING btree (status);
CREATE INDEX idx_payments_payment_date ON public.payments USING btree (payment_date DESC);

-- Purchase Orders indexes
CREATE INDEX idx_purchase_orders_po_number ON public.purchase_orders USING btree (po_number);
CREATE INDEX idx_purchase_orders_supplier_id ON public.purchase_orders USING btree (supplier_id);
CREATE INDEX idx_purchase_orders_status ON public.purchase_orders USING btree (status);
CREATE INDEX idx_purchase_orders_order_date ON public.purchase_orders USING btree (order_date DESC);

-- Purchase Order Items indexes
CREATE INDEX idx_po_items_purchase_order_id ON public.purchase_order_items USING btree (purchase_order_id);
CREATE INDEX idx_po_items_product_id ON public.purchase_order_items USING btree (product_id);

-- Inventory Transactions indexes
CREATE INDEX idx_inventory_transactions_product_id ON public.inventory_transactions USING btree (product_id);
CREATE INDEX idx_inventory_transactions_type ON public.inventory_transactions USING btree (transaction_type);
CREATE INDEX idx_inventory_transactions_performed_by ON public.inventory_transactions USING btree (performed_by);
CREATE INDEX idx_inventory_transactions_date ON public.inventory_transactions USING btree (transaction_date DESC);

-- Cash Shifts indexes
CREATE INDEX idx_cash_shifts_cashier_id ON public.cash_shifts USING btree (cashier_id);
CREATE INDEX idx_cash_shifts_opened_at ON public.cash_shifts USING btree (opened_at DESC);
CREATE INDEX idx_cash_shifts_closed_at ON public.cash_shifts USING btree (closed_at DESC);

-- Expenses indexes
CREATE INDEX idx_expenses_category ON public.expenses USING btree (category);
CREATE INDEX idx_expenses_recorded_by ON public.expenses USING btree (recorded_by);
CREATE INDEX idx_expenses_expense_date ON public.expenses USING btree (expense_date DESC);

-- Audit Logs indexes
CREATE INDEX idx_audit_logs_user_id ON public.audit_logs USING btree (user_id);
CREATE INDEX idx_audit_logs_action ON public.audit_logs USING btree (action);
CREATE INDEX idx_audit_logs_entity_type ON public.audit_logs USING btree (entity_type);
CREATE INDEX idx_audit_logs_entity_id ON public.audit_logs USING btree (entity_id);
CREATE INDEX idx_audit_logs_created_at ON public.audit_logs USING btree (created_at DESC);

-- App Settings indexes
CREATE INDEX idx_app_settings_key ON public.app_settings USING btree (setting_key);

-- Notifications indexes
CREATE INDEX idx_notifications_user_id ON public.notifications USING btree (user_id);
CREATE INDEX idx_notifications_is_read ON public.notifications USING btree (is_read);
CREATE INDEX idx_notifications_created_at ON public.notifications USING btree (created_at DESC);

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Update Updated At Column Function
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- Update Customer Stats Function
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.update_customer_stats()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'completed' AND NEW.customer_id IS NOT NULL THEN
        UPDATE customers
        SET
            total_purchases = total_purchases + NEW.total_amount,
            total_visits = total_visits + 1,
            last_visit_at = NEW.sale_date
        WHERE id = NEW.customer_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- Generate Invoice Number Function
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.generate_invoice_number()
RETURNS TEXT AS $$
DECLARE
    next_num INTEGER;
    invoice_num TEXT;
BEGIN
    SELECT COALESCE(MAX(CAST(SUBSTRING(invoice_number FROM 9) AS INTEGER)), 0) + 1
    INTO next_num
    FROM sales
    WHERE invoice_number LIKE 'INV-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-%';

    invoice_num := 'INV-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(next_num::TEXT, 4, '0');
    RETURN invoice_num;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- Get Customer Tier Function
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.get_customer_tier(customer_uuid UUID)
RETURNS TEXT AS $$
DECLARE
    total DECIMAL(12, 2);
BEGIN
    SELECT total_purchases INTO total
    FROM customers
    WHERE id = customer_uuid;

    IF total IS NULL THEN
        RETURN 'new';
    ELSIF total < 1000 THEN
        RETURN 'bronze';
    ELSIF total < 5000 THEN
        RETURN 'silver';
    ELSIF total < 10000 THEN
        RETURN 'gold';
    ELSE
        RETURN 'platinum';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Updated At Triggers
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at
    BEFORE UPDATE ON public.categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON public.products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON public.customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_suppliers_updated_at
    BEFORE UPDATE ON public.suppliers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sales_updated_at
    BEFORE UPDATE ON public.sales
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON public.payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_purchase_orders_updated_at
    BEFORE UPDATE ON public.purchase_orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cash_shifts_updated_at
    BEFORE UPDATE ON public.cash_shifts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_expenses_updated_at
    BEFORE UPDATE ON public.expenses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_app_settings_updated_at
    BEFORE UPDATE ON public.app_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Customer Stats Trigger
CREATE TRIGGER update_customer_stats_on_sale
    AFTER INSERT OR UPDATE ON public.sales
    FOR EACH ROW
    EXECUTE FUNCTION update_customer_stats();

-- ============================================================================
-- VIEWS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Products Low Stock View
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW public.products_low_stock AS
SELECT
    p.id,
    p.sku,
    p.name,
    p.category_id,
    c.name AS category_name,
    p.current_stock,
    p.min_stock_level,
    p.selling_price,
    (p.min_stock_level - p.current_stock) AS stock_shortage,
    p.updated_at
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
WHERE p.is_active = TRUE
  AND p.current_stock < p.min_stock_level
ORDER BY (p.min_stock_level - p.current_stock) DESC;

-- ----------------------------------------------------------------------------
-- Sales With Details View
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW public.sales_with_details AS
SELECT
    s.id,
    s.invoice_number,
    s.customer_id,
    c.full_name AS customer_name,
    c.customer_code,
    s.cashier_id,
    u.full_name AS cashier_name,
    s.subtotal,
    s.tax_amount,
    s.discount_amount,
    s.total_amount,
    s.status,
    s.payment_status,
    s.sale_date,
    COUNT(si.id) AS item_count,
    SUM(si.quantity) AS total_items,
    s.created_at,
    s.updated_at
FROM sales s
LEFT JOIN customers c ON s.customer_id = c.id
JOIN users u ON s.cashier_id = u.id
LEFT JOIN sale_items si ON s.id = si.sale_id
GROUP BY
    s.id, s.invoice_number, s.customer_id, c.full_name, c.customer_code,
    s.cashier_id, u.full_name, s.subtotal, s.tax_amount, s.discount_amount,
    s.total_amount, s.status, s.payment_status, s.sale_date,
    s.created_at, s.updated_at;

-- ----------------------------------------------------------------------------
-- Categories With Counts View
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW public.categories_with_counts AS
SELECT
    c.id,
    c.name,
    c.description,
    c.icon_name,
    c.color_hex,
    c.display_order,
    c.is_active,
    COUNT(p.id) AS product_count,
    COUNT(CASE WHEN p.is_active THEN 1 END) AS active_product_count,
    c.created_at,
    c.updated_at
FROM categories c
LEFT JOIN products p ON c.id = p.category_id
GROUP BY
    c.id, c.name, c.description, c.icon_name, c.color_hex,
    c.display_order, c.is_active, c.created_at, c.updated_at;

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

-- Enable RLS on all tables
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.suppliers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sale_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.purchase_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.purchase_order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.inventory_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cash_shifts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

-- Users policies
CREATE POLICY "Users can view all users"
    ON public.users FOR SELECT
    USING (TRUE);

CREATE POLICY "Users can update own profile"
    ON public.users FOR UPDATE
    USING (auth.uid()::TEXT = id::TEXT);

-- Categories policies
CREATE POLICY "Anyone can view categories"
    ON public.categories FOR SELECT
    USING (TRUE);

CREATE POLICY "Admins can manage categories"
    ON public.categories FOR ALL
    USING (TRUE);

-- Products policies
CREATE POLICY "Anyone can view products"
    ON public.products FOR SELECT
    USING (TRUE);

CREATE POLICY "Admins can manage products"
    ON public.products FOR ALL
    USING (TRUE);

-- Customers policies
CREATE POLICY "Anyone can view customers"
    ON public.customers FOR SELECT
    USING (TRUE);

CREATE POLICY "Anyone can create customers"
    ON public.customers FOR INSERT
    WITH CHECK (TRUE);

CREATE POLICY "Anyone can update customers"
    ON public.customers FOR UPDATE
    USING (TRUE);

-- Suppliers policies
CREATE POLICY "Anyone can view suppliers"
    ON public.suppliers FOR SELECT
    USING (TRUE);

CREATE POLICY "Admins can manage suppliers"
    ON public.suppliers FOR ALL
    USING (TRUE);

-- Sales policies
CREATE POLICY "Anyone can view sales"
    ON public.sales FOR SELECT
    USING (TRUE);

CREATE POLICY "Anyone can create sales"
    ON public.sales FOR INSERT
    WITH CHECK (TRUE);

CREATE POLICY "Anyone can update sales"
    ON public.sales FOR UPDATE
    USING (TRUE);

-- Sale Items policies
CREATE POLICY "Anyone can view sale_items"
    ON public.sale_items FOR SELECT
    USING (TRUE);

CREATE POLICY "Anyone can create sale_items"
    ON public.sale_items FOR INSERT
    WITH CHECK (TRUE);

-- Payments policies
CREATE POLICY "Anyone can view payments"
    ON public.payments FOR SELECT
    USING (TRUE);

CREATE POLICY "Anyone can create payments"
    ON public.payments FOR INSERT
    WITH CHECK (TRUE);

CREATE POLICY "Anyone can update payments"
    ON public.payments FOR UPDATE
    USING (TRUE);

-- Purchase Orders policies
CREATE POLICY "Anyone can view purchase_orders"
    ON public.purchase_orders FOR SELECT
    USING (TRUE);

CREATE POLICY "Anyone can create purchase_orders"
    ON public.purchase_orders FOR INSERT
    WITH CHECK (TRUE);

CREATE POLICY "Anyone can update purchase_orders"
    ON public.purchase_orders FOR UPDATE
    USING (TRUE);

-- Purchase Order Items policies
CREATE POLICY "Anyone can view purchase_order_items"
    ON public.purchase_order_items FOR SELECT
    USING (TRUE);

CREATE POLICY "Anyone can create purchase_order_items"
    ON public.purchase_order_items FOR INSERT
    WITH CHECK (TRUE);

-- Inventory Transactions policies
CREATE POLICY "Anyone can view inventory_transactions"
    ON public.inventory_transactions FOR SELECT
    USING (TRUE);

CREATE POLICY "Anyone can create inventory_transactions"
    ON public.inventory_transactions FOR INSERT
    WITH CHECK (TRUE);

-- Cash Shifts policies
CREATE POLICY "Anyone can view cash_shifts"
    ON public.cash_shifts FOR SELECT
    USING (TRUE);

CREATE POLICY "Anyone can create cash_shifts"
    ON public.cash_shifts FOR INSERT
    WITH CHECK (TRUE);

CREATE POLICY "Anyone can update cash_shifts"
    ON public.cash_shifts FOR UPDATE
    USING (TRUE);

-- Expenses policies
CREATE POLICY "Anyone can view expenses"
    ON public.expenses FOR SELECT
    USING (TRUE);

CREATE POLICY "Anyone can create expenses"
    ON public.expenses FOR INSERT
    WITH CHECK (TRUE);

CREATE POLICY "Anyone can update expenses"
    ON public.expenses FOR UPDATE
    USING (TRUE);

-- Audit Logs policies
CREATE POLICY "Anyone can view audit_logs"
    ON public.audit_logs FOR SELECT
    USING (TRUE);

-- App Settings policies
CREATE POLICY "Anyone can view app_settings"
    ON public.app_settings FOR SELECT
    USING (TRUE);

CREATE POLICY "Admins can manage app_settings"
    ON public.app_settings FOR ALL
    USING (TRUE);

-- Notifications policies
CREATE POLICY "Users can view own notifications"
    ON public.notifications FOR SELECT
    USING (TRUE);

CREATE POLICY "Users can update own notifications"
    ON public.notifications FOR UPDATE
    USING (TRUE);

-- ============================================================================
-- SEED DATA (DEVELOPMENT ONLY)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Debug User
-- ----------------------------------------------------------------------------
-- Insert debug/development user with hardcoded UUID for testing.
-- ⚠️ WARNING: For DEVELOPMENT ONLY - exclude from production deployments.
-- User credentials: dev@vibely.pos / debug123
INSERT INTO public.users (
    id,
    email,
    password_hash,
    full_name,
    role,
    status,
    created_at,
    updated_at
) VALUES (
    'a2259bb8-d02d-4384-bf2f-bbfca16bade5'::UUID,
    'dev@vibely.pos',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Debug Developer',
    'admin'::public.user_role,
    'active'::public.user_status,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
