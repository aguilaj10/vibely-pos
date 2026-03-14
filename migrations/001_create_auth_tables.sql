-- ============================================================================
-- Authentication System Database Migration
-- ============================================================================
-- Description: Creates tables for JWT token management (refresh tokens and blacklist)
-- Date: 2026-03-13
-- ============================================================================

-- ============================================================================
-- REFRESH TOKENS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT extensions.uuid_generate_v4(),
    user_id UUID NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Foreign key to users table
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES public.users (id)
        ON DELETE CASCADE
);

-- Index for faster lookups by user_id
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON public.refresh_tokens (user_id);

-- Index for faster lookups by token
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON public.refresh_tokens (token);

-- Index for automatic cleanup of expired tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON public.refresh_tokens (expires_at);

-- Comment on table
COMMENT ON TABLE public.refresh_tokens IS 'Stores refresh tokens for JWT authentication. Tokens are used to generate new access tokens without re-authentication.';

-- ============================================================================
-- TOKEN BLACKLIST TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.token_blacklist (
    id UUID PRIMARY KEY DEFAULT extensions.uuid_generate_v4(),
    token TEXT NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    blacklisted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Foreign key to users table
    CONSTRAINT fk_token_blacklist_user
        FOREIGN KEY (user_id)
        REFERENCES public.users (id)
        ON DELETE CASCADE
);

-- Index for faster lookups by token
CREATE INDEX IF NOT EXISTS idx_token_blacklist_token ON public.token_blacklist (token);

-- Index for faster lookups by user_id
CREATE INDEX IF NOT EXISTS idx_token_blacklist_user_id ON public.token_blacklist (user_id);

-- Index for automatic cleanup of expired tokens
CREATE INDEX IF NOT EXISTS idx_token_blacklist_expires_at ON public.token_blacklist (expires_at);

-- Comment on table
COMMENT ON TABLE public.token_blacklist IS 'Stores blacklisted JWT access tokens (logout). Prevents revoked tokens from being used until they naturally expire.';

-- ============================================================================
-- CLEANUP FUNCTION
-- ============================================================================

-- Function to clean up expired tokens (both refresh and blacklisted)
CREATE OR REPLACE FUNCTION public.cleanup_expired_tokens()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Delete expired refresh tokens
    DELETE FROM public.refresh_tokens
    WHERE expires_at < NOW();

    -- Delete expired blacklisted tokens
    DELETE FROM public.token_blacklist
    WHERE expires_at < NOW();
END;
$$;

COMMENT ON FUNCTION public.cleanup_expired_tokens() IS 'Removes expired tokens from refresh_tokens and token_blacklist tables. Should be run periodically via cron job.';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

-- Enable RLS on both tables
ALTER TABLE public.refresh_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.token_blacklist ENABLE ROW LEVEL SECURITY;

-- Policy: Users can only view their own refresh tokens
CREATE POLICY refresh_tokens_select_own
ON public.refresh_tokens
FOR SELECT
USING (auth.uid() = user_id);

-- Policy: Service role can manage all refresh tokens (for backend service)
CREATE POLICY refresh_tokens_service_role_all
ON public.refresh_tokens
FOR ALL
USING (auth.role() = 'service_role')
WITH CHECK (auth.role() = 'service_role');

-- Policy: Users can only view their own blacklisted tokens
CREATE POLICY token_blacklist_select_own
ON public.token_blacklist
FOR SELECT
USING (auth.uid() = user_id);

-- Policy: Service role can manage all blacklisted tokens (for backend service)
CREATE POLICY token_blacklist_service_role_all
ON public.token_blacklist
FOR ALL
USING (auth.role() = 'service_role')
WITH CHECK (auth.role() = 'service_role');

-- ============================================================================
-- GRANT PERMISSIONS
-- ============================================================================

-- Grant necessary permissions to authenticated users
GRANT SELECT ON public.refresh_tokens TO authenticated;
GRANT SELECT ON public.token_blacklist TO authenticated;

-- Grant full permissions to service role (backend API)
GRANT ALL ON public.refresh_tokens TO service_role;
GRANT ALL ON public.token_blacklist TO service_role;

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
