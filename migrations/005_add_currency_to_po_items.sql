-- ============================================================================
-- Purchase Order Items Multi-Currency Support Migration
-- ============================================================================
-- Description: Adds cost_currency_code column to purchase_order_items table
--              to track the original currency of each line item's unit cost.
--              This enables proper currency conversion when completing POs.
-- Date: 2026-03-17
-- ============================================================================

-- ============================================================================
-- ADD COST CURRENCY COLUMN TO PURCHASE ORDER ITEMS
-- ============================================================================

-- Add cost_currency_code column with default USD
ALTER TABLE public.purchase_order_items
ADD COLUMN IF NOT EXISTS cost_currency_code VARCHAR(3) NOT NULL DEFAULT 'USD';

-- Add foreign key constraint to currencies table
ALTER TABLE public.purchase_order_items
ADD CONSTRAINT fk_po_items_currency
    FOREIGN KEY (cost_currency_code)
    REFERENCES public.currencies (code)
    ON DELETE RESTRICT;

-- Index for faster currency lookups
CREATE INDEX IF NOT EXISTS idx_po_items_currency
ON public.purchase_order_items (cost_currency_code);

-- ============================================================================
-- COMMENTS
-- ============================================================================

COMMENT ON COLUMN public.purchase_order_items.cost_currency_code IS 
'Currency code (ISO 4217) for the unit_cost field. Locked when PO is completed and used for currency conversion to MXN.';
