package com.vibely.pos.shared.data.settings.datasource

import com.vibely.pos.shared.data.settings.dto.ReceiptSettingsDTO
import com.vibely.pos.shared.data.settings.dto.StoreSettingsDTO
import com.vibely.pos.shared.data.settings.dto.TaxSettingsDTO
import com.vibely.pos.shared.data.settings.dto.UserPreferencesDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Remote data source for settings API calls.
 *
 * Handles HTTP requests to the backend settings endpoints.
 *
 * @param httpClient The HTTP client for making requests.
 * @param baseUrl The base URL of the backend API.
 */
class RemoteSettingsDataSource(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Fetches store settings from the backend.
     *
     * GET /api/settings/store
     *
     * @return [Result.Success] with [StoreSettingsDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getStoreSettings(): Result<StoreSettingsDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/settings/store").body<StoreSettingsDTO>()
    }

    /**
     * Updates store settings on the backend.
     *
     * PUT /api/settings/store
     *
     * @param settings The store settings to update.
     * @return [Result.Success] with [StoreSettingsDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun updateStoreSettings(settings: StoreSettingsDTO): Result<StoreSettingsDTO> = Result.runCatching {
        httpClient
            .put("$baseUrl/api/settings/store") {
                contentType(ContentType.Application.Json)
                setBody(settings)
            }.body<StoreSettingsDTO>()
    }

    /**
     * Fetches receipt settings from the backend.
     *
     * GET /api/settings/receipt
     *
     * @return [Result.Success] with [ReceiptSettingsDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getReceiptSettings(): Result<ReceiptSettingsDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/settings/receipt").body<ReceiptSettingsDTO>()
    }

    /**
     * Updates receipt settings on the backend.
     *
     * PUT /api/settings/receipt
     *
     * @param settings The receipt settings to update.
     * @return [Result.Success] with [ReceiptSettingsDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun updateReceiptSettings(settings: ReceiptSettingsDTO): Result<ReceiptSettingsDTO> = Result.runCatching {
        httpClient
            .put("$baseUrl/api/settings/receipt") {
                contentType(ContentType.Application.Json)
                setBody(settings)
            }.body<ReceiptSettingsDTO>()
    }

    /**
     * Fetches tax settings from the backend.
     *
     * GET /api/settings/tax
     *
     * @return [Result.Success] with [TaxSettingsDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getTaxSettings(): Result<TaxSettingsDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/settings/tax").body<TaxSettingsDTO>()
    }

    /**
     * Updates tax settings on the backend.
     *
     * PUT /api/settings/tax
     *
     * @param settings The tax settings to update.
     * @return [Result.Success] with [TaxSettingsDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun updateTaxSettings(settings: TaxSettingsDTO): Result<TaxSettingsDTO> = Result.runCatching {
        httpClient
            .put("$baseUrl/api/settings/tax") {
                contentType(ContentType.Application.Json)
                setBody(settings)
            }.body<TaxSettingsDTO>()
    }

    /**
     * Fetches user preferences from the backend.
     *
     * GET /api/settings/preferences
     *
     * @return [Result.Success] with [UserPreferencesDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getUserPreferences(): Result<UserPreferencesDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/settings/preferences").body<UserPreferencesDTO>()
    }

    /**
     * Updates user preferences on the backend.
     *
     * PUT /api/settings/preferences
     *
     * @param preferences The user preferences to update.
     * @return [Result.Success] with [UserPreferencesDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun updateUserPreferences(preferences: UserPreferencesDTO): Result<UserPreferencesDTO> = Result.runCatching {
        httpClient
            .put("$baseUrl/api/settings/preferences") {
                contentType(ContentType.Application.Json)
                setBody(preferences)
            }.body<UserPreferencesDTO>()
    }
}
