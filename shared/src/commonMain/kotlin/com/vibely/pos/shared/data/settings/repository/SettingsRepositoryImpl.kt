package com.vibely.pos.shared.data.settings.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.settings.datasource.RemoteSettingsDataSource
import com.vibely.pos.shared.data.settings.mapper.ReceiptSettingsMapper
import com.vibely.pos.shared.data.settings.mapper.StoreSettingsMapper
import com.vibely.pos.shared.data.settings.mapper.TaxSettingsMapper
import com.vibely.pos.shared.data.settings.mapper.UserPreferencesMapper
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.map
import com.vibely.pos.shared.domain.settings.entity.ReceiptSettings
import com.vibely.pos.shared.domain.settings.entity.StoreSettings
import com.vibely.pos.shared.domain.settings.entity.TaxSettings
import com.vibely.pos.shared.domain.settings.entity.UserPreferences
import com.vibely.pos.shared.domain.settings.repository.SettingsRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Implementation of [SettingsRepository] using remote data source with in-memory caching.
 *
 * Implements a simple cache with 5-minute TTL to reduce backend load for settings data.
 *
 * @param remoteDataSource Remote data source for backend API calls.
 */
class SettingsRepositoryImpl(private val remoteDataSource: RemoteSettingsDataSource) :
    BaseRepository(),
    SettingsRepository {

    private val cacheTTL = 5.minutes

    // Cache for store settings with 5-minute TTL
    private var cachedStoreSettings: StoreSettings? = null
    private var storeSettingsFetchedAt: Long? = null

    // Cache for receipt settings with 5-minute TTL
    private var cachedReceiptSettings: ReceiptSettings? = null
    private var receiptSettingsFetchedAt: Long? = null

    // Cache for tax settings with 5-minute TTL
    private var cachedTaxSettings: TaxSettings? = null
    private var taxSettingsFetchedAt: Long? = null

    // Cache for user preferences with 5-minute TTL
    private var cachedUserPreferences: UserPreferences? = null
    private var userPreferencesFetchedAt: Long? = null

    override suspend fun getStoreSettings(): Result<StoreSettings> {
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedStoreSettingsLocal = cachedStoreSettings
        val storeSettingsFetchedAtLocal = storeSettingsFetchedAt

        if (cachedStoreSettingsLocal != null && storeSettingsFetchedAtLocal != null) {
            val cacheAge = now - storeSettingsFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                return Result.Success(cachedStoreSettingsLocal)
            }
        }

        val result = mapSingle(remoteDataSource.getStoreSettings(), StoreSettingsMapper::toDomain)
        if (result is Result.Success) {
            cachedStoreSettings = result.data
            storeSettingsFetchedAt = now
        }
        return result
    }

    override suspend fun updateStoreSettings(settings: StoreSettings): Result<Unit> {
        val dto = StoreSettingsMapper.toDTO(settings)
        val result = remoteDataSource.updateStoreSettings(dto)
        if (result is Result.Success) {
            cachedStoreSettings = StoreSettingsMapper.toDomain(result.data)
            storeSettingsFetchedAt = Clock.System.now().toEpochMilliseconds()
        }
        return result.map { }
    }

    override suspend fun getReceiptSettings(): Result<ReceiptSettings> {
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedReceiptSettingsLocal = cachedReceiptSettings
        val receiptSettingsFetchedAtLocal = receiptSettingsFetchedAt

        if (cachedReceiptSettingsLocal != null && receiptSettingsFetchedAtLocal != null) {
            val cacheAge = now - receiptSettingsFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                return Result.Success(cachedReceiptSettingsLocal)
            }
        }

        val result = mapSingle(remoteDataSource.getReceiptSettings(), ReceiptSettingsMapper::toDomain)
        if (result is Result.Success) {
            cachedReceiptSettings = result.data
            receiptSettingsFetchedAt = now
        }
        return result
    }

    override suspend fun updateReceiptSettings(settings: ReceiptSettings): Result<Unit> {
        val dto = ReceiptSettingsMapper.toDTO(settings)
        val result = remoteDataSource.updateReceiptSettings(dto)
        if (result is Result.Success) {
            cachedReceiptSettings = ReceiptSettingsMapper.toDomain(result.data)
            receiptSettingsFetchedAt = Clock.System.now().toEpochMilliseconds()
        }
        return result.map { }
    }

    override suspend fun getTaxSettings(): Result<TaxSettings> {
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedTaxSettingsLocal = cachedTaxSettings
        val taxSettingsFetchedAtLocal = taxSettingsFetchedAt

        if (cachedTaxSettingsLocal != null && taxSettingsFetchedAtLocal != null) {
            val cacheAge = now - taxSettingsFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                return Result.Success(cachedTaxSettingsLocal)
            }
        }

        val result = mapSingle(remoteDataSource.getTaxSettings(), TaxSettingsMapper::toDomain)
        if (result is Result.Success) {
            cachedTaxSettings = result.data
            taxSettingsFetchedAt = now
        }
        return result
    }

    override suspend fun updateTaxSettings(settings: TaxSettings): Result<Unit> {
        val dto = TaxSettingsMapper.toDTO(settings)
        val result = remoteDataSource.updateTaxSettings(dto)
        if (result is Result.Success) {
            cachedTaxSettings = TaxSettingsMapper.toDomain(result.data)
            taxSettingsFetchedAt = Clock.System.now().toEpochMilliseconds()
        }
        return result.map { }
    }

    override suspend fun getUserPreferences(): Result<UserPreferences> {
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedUserPreferencesLocal = cachedUserPreferences
        val userPreferencesFetchedAtLocal = userPreferencesFetchedAt

        if (cachedUserPreferencesLocal != null && userPreferencesFetchedAtLocal != null) {
            val cacheAge = now - userPreferencesFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                return Result.Success(cachedUserPreferencesLocal)
            }
        }

        val result = mapSingle(remoteDataSource.getUserPreferences(), UserPreferencesMapper::toDomain)
        if (result is Result.Success) {
            cachedUserPreferences = result.data
            userPreferencesFetchedAt = now
        }
        return result
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<Unit> {
        val dto = UserPreferencesMapper.toDTO(preferences)
        val result = remoteDataSource.updateUserPreferences(dto)
        if (result is Result.Success) {
            cachedUserPreferences = UserPreferencesMapper.toDomain(result.data)
            userPreferencesFetchedAt = Clock.System.now().toEpochMilliseconds()
        }
        return result.map { }
    }

    override suspend fun refreshSettings(): Result<Unit> {
        cachedStoreSettings = null
        storeSettingsFetchedAt = null
        cachedReceiptSettings = null
        receiptSettingsFetchedAt = null
        cachedTaxSettings = null
        taxSettingsFetchedAt = null
        cachedUserPreferences = null
        userPreferencesFetchedAt = null
        return Result.Success(Unit)
    }
}
