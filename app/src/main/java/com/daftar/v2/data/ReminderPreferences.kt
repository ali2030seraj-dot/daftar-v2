package com.daftar.v2.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.reminderDataStore by preferencesDataStore("reminder_prefs")

@Singleton
class ReminderPreferences @Inject constructor(
    // ✅ الحل: أضف @ApplicationContext
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val KEY_THRESHOLD_DAYS = intPreferencesKey("reminder_threshold_days")
        private val KEY_LAST_SENT = longPreferencesKey("reminder_last_sent")
    }

    val enabled: Flow<Boolean> = context.reminderDataStore.data
        .map { it[KEY_ENABLED] ?: true }

    val thresholdDays: Flow<Int> = context.reminderDataStore.data
        .map { it[KEY_THRESHOLD_DAYS] ?: 30 }

    val lastSent: Flow<Long> = context.reminderDataStore.data
        .map { it[KEY_LAST_SENT] ?: 0L }

    suspend fun setEnabled(value: Boolean) {
        context.reminderDataStore.edit { it[KEY_ENABLED] = value }
    }

    suspend fun setThresholdDays(days: Int) {
        context.reminderDataStore.edit { it[KEY_THRESHOLD_DAYS] = days }
    }

    suspend fun setLastSent(timestamp: Long) {
        context.reminderDataStore.edit { it[KEY_LAST_SENT] = timestamp }
    }
}