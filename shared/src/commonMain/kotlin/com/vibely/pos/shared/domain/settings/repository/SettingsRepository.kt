package com.vibely.pos.shared.domain.settings.repository

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.settings.entity.ReceiptSettings
import com.vibely.pos.shared.domain.settings.entity.StoreSettings
import com.vibely.pos.shared.domain.settings.entity.TaxSettings
import com.vibely.pos.shared.domain.settings.entity.UserPreferences

/**
 * Repository interface for settings data operations.
 *
 * Defines the contract for retrieving and updating system-wide configuration
 * including store information, receipt formatting, tax rates, and user preferences.
 * Implementations handle data persistence, caching, and backend communication.
 */
interface SettingsRepository {

    /**
     * Retrieves current store information settings.
     *
     * @return [Result.Success] with [StoreSettings] if retrieval succeeds,
     *         [Result.Error] if an error occurs.
     *
     * Possible error codes:
     * - "NETWORK_ERROR": Network request failed
     * - "UNAUTHORIZED": Access token is invalid or expired
     * - "SERVER_ERROR": Backend service error
     */
    suspend fun getStoreSettings(): Result<StoreSettings>

    /**
     * Updates store information settings.
     *
     * @param settings New store settings to persist.
     * @return [Result.Success] with Unit if update succeeds,
     *         [Result.Error] if validation or update fails.
     *
     * Possible error codes:
     * - "VALIDATION_ERROR": Invalid field values
     * - "NETWORK_ERROR": Network request failed
     * - "UNAUTHORIZED": Access token is invalid or expired
     */
    suspend fun updateStoreSettings(settings: StoreSettings): Result<Unit>

    /**
     * Retrieves current receipt formatting settings.
     *
     * @return [Result.Success] with [ReceiptSettings] if retrieval succeeds,
     *         [Result.Error] if an error occurs.
     */
    suspend fun getReceiptSettings(): Result<ReceiptSettings>

    /**
     * Updates receipt formatting settings.
     *
     * @param settings New receipt settings to persist.
     * @return [Result.Success] with Unit if update succeeds,
     *         [Result.Error] if validation or update fails.
     */
    suspend fun updateReceiptSettings(settings: ReceiptSettings): Result<Unit>

    /**
     * Retrieves current tax and currency settings.
     *
     * @return [Result.Success] with [TaxSettings] if retrieval succeeds,
     *         [Result.Error] if an error occurs.
     */
    suspend fun getTaxSettings(): Result<TaxSettings>

    /**
     * Updates tax and currency settings.
     *
     * @param settings New tax settings to persist.
     * @return [Result.Success] with Unit if update succeeds,
     *         [Result.Error] if validation or update fails.
     */
    suspend fun updateTaxSettings(settings: TaxSettings): Result<Unit>

    /**
     * Retrieves user-specific preferences for the current authenticated user.
     *
     * @return [Result.Success] with [UserPreferences] if retrieval succeeds,
     *         [Result.Error] if an error occurs.
     */
    suspend fun getUserPreferences(): Result<UserPreferences>

    /**
     * Updates user-specific preferences.
     *
     * @param preferences New user preferences to persist.
     * @return [Result.Success] with Unit if update succeeds,
     *         [Result.Error] if validation or update fails.
     */
    suspend fun updateUserPreferences(preferences: UserPreferences): Result<Unit>

    /**
     * Refreshes the settings data cache.
     *
     * Invalidates cached settings and triggers fresh retrieval from backend.
     * Useful for ensuring data consistency after external modifications.
     *
     * @return [Result.Success] with Unit if refresh succeeds,
     *         [Result.Error] if an error occurs.
     */
    suspend fun refreshSettings(): Result<Unit>
}
