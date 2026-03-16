package com.vibely.pos.backend.services

import com.vibely.pos.backend.dto.request.UpdateReceiptSettingsRequest
import com.vibely.pos.backend.dto.request.UpdateStoreInfoRequest
import com.vibely.pos.backend.dto.request.UpdateTaxSettingsRequest
import com.vibely.pos.backend.dto.request.UpdateUserPreferencesRequest
import com.vibely.pos.shared.data.settings.dto.ReceiptSettingsDTO
import com.vibely.pos.shared.data.settings.dto.StoreSettingsDTO
import com.vibely.pos.shared.data.settings.dto.TaxSettingsDTO
import com.vibely.pos.shared.data.settings.dto.UserPreferencesDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TABLE_SETTINGS = "app_settings"
private const val SETTING_KEY_STORE = "store_settings"
private const val SETTING_KEY_RECEIPT = "receipt_settings"
private const val SETTING_KEY_TAX = "tax_settings"
private const val SETTING_KEY_USER_PREFIX = "user_preferences_"

// Column name constants
private const val COL_ID = "id"
private const val COL_SETTING_VALUE = "setting_value"
private const val COL_CREATED_AT = "created_at"
private const val COL_UPDATED_AT = "updated_at"
private const val COL_SETTING_KEY = "setting_key"
private const val COL_IS_PUBLIC = "is_public"
private const val COL_UPDATED_BY = "updated_by"

// SQL function constants
private const val SQL_NOW = "now()"

/**
 * Service for managing application settings including store information,
 * receipt configuration, tax settings, and user preferences.
 *
 * Handles CRUD operations for settings stored in the app_settings table
 * using Supabase Postgrest client.
 *
 * @param supabaseClient Supabase client for database operations.
 */
@Suppress("TooManyFunctions")
class SettingsService(
    private val supabaseClient: SupabaseClient,
) : BaseService() {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches store settings from the database.
     *
     * @return [Result.Success] with [StoreSettingsDTO] if successful,
     *         [Result.Error] if not found or on error.
     */
    suspend fun getStoreSettings(): Result<StoreSettingsDTO> = executeQuery("Failed to get store settings") {
        val row = supabaseClient.from(TABLE_SETTINGS)
            .select(columns = Columns.list(COL_ID, COL_SETTING_VALUE, COL_CREATED_AT, COL_UPDATED_AT)) {
                filter { eq(COL_SETTING_KEY, SETTING_KEY_STORE) }
                limit(1)
            }
            .decodeSingleOrNull<SettingsRow>()

        row?.let {
            val settings = json.decodeFromString<StoreSettingsData>(it.settingValue)
            StoreSettingsDTO(
                id = it.id,
                storeName = settings.storeName,
                address = settings.address,
                phone = settings.phone,
                email = settings.email,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        } ?: createDefaultStoreSettings()
    }

    /**
     * Updates store settings in the database.
     *
     * @param dto The updated store settings.
     * @return [Result.Success] with [Unit] if successful,
     *         [Result.Error] if update fails.
     */
    suspend fun updateStoreSettings(dto: UpdateStoreInfoRequest): Result<Unit> =
        executeQuery("Failed to update store settings") {
        val settingsData = StoreSettingsData(
            storeName = dto.storeName,
            address = dto.address,
            phone = dto.phone,
            email = dto.email
        )
        val jsonValue = json.encodeToString(settingsData)

        val existing = supabaseClient.from(TABLE_SETTINGS)
            .select(columns = Columns.list(COL_ID)) {
                filter { eq(COL_SETTING_KEY, SETTING_KEY_STORE) }
                limit(1)
            }
            .decodeSingleOrNull<SettingsIdRow>()

        if (existing != null) {
            supabaseClient.from(TABLE_SETTINGS)
                .update(
                    mapOf(
                        COL_SETTING_VALUE to jsonValue,
                        COL_UPDATED_AT to SQL_NOW
                    )
                ) {
                    filter { eq(COL_SETTING_KEY, SETTING_KEY_STORE) }
                }
        } else {
            supabaseClient.from(TABLE_SETTINGS)
                .insert(
                    mapOf(
                        COL_SETTING_KEY to SETTING_KEY_STORE,
                        COL_SETTING_VALUE to jsonValue,
                        COL_IS_PUBLIC to true
                    )
                )
        }
    }

    /**
     * Fetches receipt settings from the database.
     *
     * @return [Result.Success] with [ReceiptSettingsDTO] if successful,
     *         [Result.Error] if not found or on error.
     */
    suspend fun getReceiptSettings(): Result<ReceiptSettingsDTO> = executeQuery("Failed to get receipt settings") {
        val row = supabaseClient.from(TABLE_SETTINGS)
            .select(columns = Columns.list(COL_ID, COL_SETTING_VALUE, COL_CREATED_AT, COL_UPDATED_AT)) {
                filter { eq(COL_SETTING_KEY, SETTING_KEY_RECEIPT) }
                limit(1)
            }
            .decodeSingleOrNull<SettingsRow>()

        row?.let {
            val settings = json.decodeFromString<ReceiptSettingsData>(it.settingValue)
            ReceiptSettingsDTO(
                id = it.id,
                header = settings.header,
                footer = settings.footer,
                logoUrl = settings.logoUrl,
                showTax = settings.showTax,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        } ?: createDefaultReceiptSettings()
    }

    /**
     * Updates receipt settings in the database.
     *
     * @param dto The updated receipt settings.
     * @return [Result.Success] with [Unit] if successful,
     *         [Result.Error] if update fails.
     */
    suspend fun updateReceiptSettings(dto: UpdateReceiptSettingsRequest): Result<Unit> =
        executeQuery("Failed to update receipt settings") {
        val settingsData = ReceiptSettingsData(
            header = dto.header,
            footer = dto.footer,
            logoUrl = dto.logoUrl,
            showTax = dto.showTax
        )
        val jsonValue = json.encodeToString(settingsData)

        val existing = supabaseClient.from(TABLE_SETTINGS)
            .select(columns = Columns.list(COL_ID)) {
                filter { eq(COL_SETTING_KEY, SETTING_KEY_RECEIPT) }
                limit(1)
            }
            .decodeSingleOrNull<SettingsIdRow>()

        if (existing != null) {
            supabaseClient.from(TABLE_SETTINGS)
                .update(
                    mapOf(
                        COL_SETTING_VALUE to jsonValue,
                        COL_UPDATED_AT to SQL_NOW
                    )
                ) {
                    filter { eq(COL_SETTING_KEY, SETTING_KEY_RECEIPT) }
                }
        } else {
            supabaseClient.from(TABLE_SETTINGS)
                .insert(
                    mapOf(
                        COL_SETTING_KEY to SETTING_KEY_RECEIPT,
                        COL_SETTING_VALUE to jsonValue,
                        COL_IS_PUBLIC to true
                    )
                )
        }
    }

    /**
     * Fetches tax settings from the database.
     *
     * @return [Result.Success] with [TaxSettingsDTO] if successful,
     *         [Result.Error] if not found or on error.
     */
    suspend fun getTaxSettings(): Result<TaxSettingsDTO> = executeQuery("Failed to get tax settings") {
        val row = supabaseClient.from(TABLE_SETTINGS)
            .select(columns = Columns.list(COL_ID, COL_SETTING_VALUE, COL_CREATED_AT, COL_UPDATED_AT)) {
                filter { eq(COL_SETTING_KEY, SETTING_KEY_TAX) }
                limit(1)
            }
            .decodeSingleOrNull<SettingsRow>()

        row?.let {
            val settings = json.decodeFromString<TaxSettingsData>(it.settingValue)
            TaxSettingsDTO(
                id = it.id,
                taxRate = settings.taxRate,
                currency = settings.currency,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        } ?: createDefaultTaxSettings()
    }

    /**
     * Updates tax settings in the database.
     *
     * @param dto The updated tax settings.
     * @return [Result.Success] with [Unit] if successful,
     *         [Result.Error] if update fails.
     */
    suspend fun updateTaxSettings(dto: UpdateTaxSettingsRequest): Result<Unit> =
        executeQuery("Failed to update tax settings") {
        val settingsData = TaxSettingsData(
            taxRate = dto.taxRate,
            currency = dto.currency
        )
        val jsonValue = json.encodeToString(settingsData)

        val existing = supabaseClient.from(TABLE_SETTINGS)
            .select(columns = Columns.list(COL_ID)) {
                filter { eq(COL_SETTING_KEY, SETTING_KEY_TAX) }
                limit(1)
            }
            .decodeSingleOrNull<SettingsIdRow>()

        if (existing != null) {
            supabaseClient.from(TABLE_SETTINGS)
                .update(
                    mapOf(
                        COL_SETTING_VALUE to jsonValue,
                        COL_UPDATED_AT to SQL_NOW
                    )
                ) {
                    filter { eq(COL_SETTING_KEY, SETTING_KEY_TAX) }
                }
        } else {
            supabaseClient.from(TABLE_SETTINGS)
                .insert(
                    mapOf(
                        COL_SETTING_KEY to SETTING_KEY_TAX,
                        COL_SETTING_VALUE to jsonValue,
                        COL_IS_PUBLIC to true
                    )
                )
        }
    }

    /**
     * Fetches user preferences from the database.
     *
     * @param userId The user ID to fetch preferences for.
     * @return [Result.Success] with [UserPreferencesDTO] if successful,
     *         [Result.Error] if not found or on error.
     */
    suspend fun getUserPreferences(userId: String): Result<UserPreferencesDTO> =
        executeQuery("Failed to get user preferences") {
        val settingKey = "$SETTING_KEY_USER_PREFIX$userId"

        val row = supabaseClient.from(TABLE_SETTINGS)
            .select(columns = Columns.list(COL_ID, COL_SETTING_VALUE, COL_CREATED_AT, COL_UPDATED_AT)) {
                filter { eq(COL_SETTING_KEY, settingKey) }
                limit(1)
            }
            .decodeSingleOrNull<SettingsRow>()

        row?.let {
            val settings = json.decodeFromString<UserPreferencesData>(it.settingValue)
            UserPreferencesDTO(
                id = it.id,
                language = settings.language,
                theme = settings.theme,
                enableNotifications = settings.enableNotifications,
                autoLogoutTimeoutMinutes = settings.autoLogoutTimeoutMinutes,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        } ?: createDefaultUserPreferences(userId)
    }

    /**
     * Updates user preferences in the database.
     *
     * @param userId The user ID to update preferences for.
     * @param dto The updated user preferences.
     * @return [Result.Success] with [Unit] if successful,
     *         [Result.Error] if update fails.
     */
    suspend fun updateUserPreferences(
        userId: String,
        dto: UpdateUserPreferencesRequest,
    ): Result<Unit> = executeQuery("Failed to update user preferences") {
        val settingKey = "$SETTING_KEY_USER_PREFIX$userId"
        val settingsData = UserPreferencesData(
            language = dto.language,
            theme = dto.theme,
            enableNotifications = dto.enableNotifications,
            autoLogoutTimeoutMinutes = dto.autoLogoutTimeoutMinutes
        )
        val jsonValue = json.encodeToString(settingsData)

        val existing = supabaseClient.from(TABLE_SETTINGS)
            .select(columns = Columns.list(COL_ID)) {
                filter { eq(COL_SETTING_KEY, settingKey) }
                limit(1)
            }
            .decodeSingleOrNull<SettingsIdRow>()

        if (existing != null) {
            supabaseClient.from(TABLE_SETTINGS)
                .update(
                    mapOf(
                        COL_SETTING_VALUE to jsonValue,
                        COL_UPDATED_AT to SQL_NOW,
                        COL_UPDATED_BY to userId
                    )
                ) {
                    filter { eq(COL_SETTING_KEY, settingKey) }
                }
        } else {
            supabaseClient.from(TABLE_SETTINGS)
                .insert(
                    mapOf(
                        COL_SETTING_KEY to settingKey,
                        COL_SETTING_VALUE to jsonValue,
                        COL_IS_PUBLIC to false,
                        COL_UPDATED_BY to userId
                    )
                )
        }
    }

    private fun createDefaultStoreSettings(): StoreSettingsDTO {
        val now = System.currentTimeMillis()
        return StoreSettingsDTO(
            id = "",
            storeName = "My Store",
            address = "",
            phone = "",
            email = "",
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createDefaultReceiptSettings(): ReceiptSettingsDTO {
        val now = System.currentTimeMillis()
        return ReceiptSettingsDTO(
            id = "",
            header = "Thank you for your purchase!",
            footer = "Please come again",
            logoUrl = null,
            showTax = true,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createDefaultTaxSettings(): TaxSettingsDTO {
        val now = System.currentTimeMillis()
        return TaxSettingsDTO(
            id = "",
            taxRate = 0.0,
            currency = "USD",
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createDefaultUserPreferences(userId: String): UserPreferencesDTO {
        val now = System.currentTimeMillis()
        return UserPreferencesDTO(
            id = userId,
            language = "en",
            theme = "system",
            enableNotifications = true,
            autoLogoutTimeoutMinutes = 30,
            createdAt = now,
            updatedAt = now
        )
    }

    @Serializable
    private data class SettingsRow(
        @SerialName("id") val id: String,
        @SerialName("setting_value") val settingValue: String,
        @SerialName("created_at") val createdAt: Long,
        @SerialName("updated_at") val updatedAt: Long,
    )

    @Serializable
    private data class SettingsIdRow(
        @SerialName("id") val id: String,
    )

    @Serializable
    private data class StoreSettingsData(
        @SerialName("store_name") val storeName: String,
        @SerialName("address") val address: String,
        @SerialName("phone") val phone: String,
        @SerialName("email") val email: String,
    )

    @Serializable
    private data class ReceiptSettingsData(
        @SerialName("header") val header: String,
        @SerialName("footer") val footer: String,
        @SerialName("logo_url") val logoUrl: String?,
        @SerialName("show_tax") val showTax: Boolean,
    )

    @Serializable
    private data class TaxSettingsData(
        @SerialName("tax_rate") val taxRate: Double,
        @SerialName("currency") val currency: String,
    )

    @Serializable
    private data class UserPreferencesData(
        @SerialName("language") val language: String,
        @SerialName("theme") val theme: String,
        @SerialName("enable_notifications") val enableNotifications: Boolean,
        @SerialName("auto_logout_timeout_minutes") val autoLogoutTimeoutMinutes: Int,
    )
}
