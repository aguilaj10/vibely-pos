-- ============================================================================
-- Migration: Add user_id columns for audit trail
-- ============================================================================
-- This migration adds user_id columns to tables that need ownership tracking
-- for audit trails and multi-user support.
--
-- Tables affected:
-- - categories
-- - suppliers  
-- - customers
-- - products
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Add user_id to categories
-- ----------------------------------------------------------------------------
ALTER TABLE public.categories 
ADD COLUMN user_id UUID REFERENCES public.users(id);

-- Create index for performance
CREATE INDEX idx_categories_user_id ON public.categories USING btree (user_id);

-- Update RLS policy to filter by user_id (replace existing policy)
DROP POLICY IF EXISTS "Anyone can view categories" ON public.categories;
CREATE POLICY "Users can view own categories"
    ON public.categories FOR SELECT
    USING (user_id IS NULL OR user_id = auth.uid()::UUID);

DROP POLICY IF EXISTS "Admins can manage categories" ON public.categories;
CREATE POLICY "Users can manage own categories"
    ON public.categories FOR ALL
    USING (user_id IS NULL OR user_id = auth.uid()::UUID);

-- ----------------------------------------------------------------------------
-- Add user_id to suppliers
-- ----------------------------------------------------------------------------
ALTER TABLE public.suppliers 
ADD COLUMN user_id UUID REFERENCES public.users(id);

-- Create index for performance
CREATE INDEX idx_suppliers_user_id ON public.suppliers USING btree (user_id);

-- Update RLS policy to filter by user_id
DROP POLICY IF EXISTS "Anyone can view suppliers" ON public.suppliers;
CREATE POLICY "Users can view own suppliers"
    ON public.suppliers FOR SELECT
    USING (user_id IS NULL OR user_id = auth.uid()::UUID);

DROP POLICY IF EXISTS "Admins can manage suppliers" ON public.suppliers;
CREATE POLICY "Users can manage own suppliers"
    ON public.suppliers FOR ALL
    USING (user_id IS NULL OR user_id = auth.uid()::UUID);

-- ----------------------------------------------------------------------------
-- Add user_id to customers
-- ----------------------------------------------------------------------------
ALTER TABLE public.customers 
ADD COLUMN user_id UUID REFERENCES public.users(id);

-- Create index for performance
CREATE INDEX idx_customers_user_id ON public.customers USING btree (user_id);

-- Update RLS policies to filter by user_id
DROP POLICY IF EXISTS "Anyone can view customers" ON public.customers;
CREATE POLICY "Users can view own customers"
    ON public.customers FOR SELECT
    USING (user_id IS NULL OR user_id = auth.uid()::UUID);

DROP POLICY IF EXISTS "Anyone can create customers" ON public.customers;
CREATE POLICY "Users can create own customers"
    ON public.customers FOR INSERT
    WITH CHECK (user_id IS NULL OR user_id = auth.uid()::UUID);

DROP POLICY IF EXISTS "Anyone can update customers" ON public.customers;
CREATE POLICY "Users can update own customers"
    ON public.customers FOR UPDATE
    USING (user_id IS NULL OR user_id = auth.uid()::UUID);

-- ----------------------------------------------------------------------------
-- Add user_id to products
-- ----------------------------------------------------------------------------
ALTER TABLE public.products 
ADD COLUMN user_id UUID REFERENCES public.users(id);

-- Create index for performance
CREATE INDEX idx_products_user_id ON public.products USING btree (user_id);

-- Update RLS policies to filter by user_id
DROP POLICY IF EXISTS "Anyone can view products" ON public.products;
CREATE POLICY "Users can view own products"
    ON public.products FOR SELECT
    USING (user_id IS NULL OR user_id = auth.uid()::UUID);

DROP POLICY IF EXISTS "Admins can manage products" ON public.products;
CREATE POLICY "Users can manage own products"
    ON public.products FOR ALL
    USING (user_id IS NULL OR user_id = auth.uid()::UUID);

-- ============================================================================
-- Notes:
-- ============================================================================
-- 1. user_id is nullable to support existing data
-- 2. RLS policies allow NULL user_id for backward compatibility
-- 3. New inserts from backend will populate user_id
-- 4. Indexes added for query performance
-- 5. Foreign key constraints ensure referential integrity
-- ============================================================================
