package com.vibely.pos.backend.data

private const val ENV_DB_STRATEGY = "DB_STRATEGY"
private const val STRATEGY_LOCAL = "local"

/**
 * Selects the database backend at startup via [ENV_DB_STRATEGY] env var.
 *
 * Defaults to [Remote] (Supabase) when the env var is absent or any value other than "local".
 */
sealed class DatabaseStrategy {
    /**
     * Use Supabase (remote PostgreSQL) — requires SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY.
     */
    object Remote : DatabaseStrategy()

    /**
     * Use Room/SQLite (local network mode) — no Supabase configuration required.
     */
    object Local : DatabaseStrategy()

    /** Factory for resolving the active strategy from environment. */
    companion object {
        /**
         * Returns the active [DatabaseStrategy] by reading the [ENV_DB_STRATEGY] environment
         * variable at runtime.
         *
         * @return [Local] when DB_STRATEGY=local, [Remote] otherwise.
         */
        val current: DatabaseStrategy
            get() = if (System.getenv(ENV_DB_STRATEGY) == STRATEGY_LOCAL) Local else Remote
    }
}
