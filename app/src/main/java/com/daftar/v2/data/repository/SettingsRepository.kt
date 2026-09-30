package com.daftar.v2.data.repository

import com.daftar.v2.data.db.dao.SettingsDao
import com.daftar.v2.data.entity.SettingsEntity
import com.daftar.v2.util.CurrencyUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {

    companion object {
        const val KEY_APP_NAME = "app_name"
        const val KEY_DEFAULT_CURRENCY = "default_currency"
        const val KEY_DARK_MODE = "dark_mode"
    }

    suspend fun get(key: String): String? = settingsDao.get(key)

    fun observe(key: String): Flow<String?> = settingsDao.observe(key)

    suspend fun put(key: String, value: String) {
        settingsDao.put(SettingsEntity(key, value))
    }

    suspend fun delete(key: String) = settingsDao.delete(key)

    suspend fun getAppName(): String = get(KEY_APP_NAME) ?: "دفتر حساباتي"

    fun observeAppName(): Flow<String> =
        settingsDao.observe(KEY_APP_NAME).map { it ?: "دفتر حساباتي" }

    suspend fun setAppName(name: String) = put(KEY_APP_NAME, name.trim())

    /** ⭐ الآن يرجع الكود "YER" مو الاسم العربي */
    suspend fun getDefaultCurrency(): String =
        CurrencyUtils.toCode(get(KEY_DEFAULT_CURRENCY))

    /** ⭐ يُحوّل تلقائياً للكود قبل الحفظ */
    suspend fun setDefaultCurrency(currency: String) =
        put(KEY_DEFAULT_CURRENCY, CurrencyUtils.toCode(currency))
}