package com.daftar.v2.data.template

import com.daftar.v2.data.db.dao.SettingsDao
import com.daftar.v2.data.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseTemplateManager @Inject constructor(
    private val settingsDao: SettingsDao
) {

    companion object {
        private const val KEY = "expense_templates_json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * يُعيد كل القوالب.
     */
    suspend fun getAll(): List<ExpenseTemplate> {
        val raw = settingsDao.get(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<ExpenseTemplate>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Flow يُحدَّث تلقائيًا.
     */
    fun observeAll(): Flow<List<ExpenseTemplate>> {
        return settingsDao.observe(KEY).map { raw ->
            if (raw.isNullOrBlank()) return@map emptyList()
            try {
                json.decodeFromString<List<ExpenseTemplate>>(raw)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * إضافة قالب جديد.
     */
    suspend fun add(
        name: String,
        defaultAmountCents: Long,
        currency: String,
        categoryId: Long? = null,
        note: String = ""
    ): ExpenseTemplate {
        val template = ExpenseTemplate(
            id = UUID.randomUUID().toString(),
            name = name,
            defaultAmountCents = defaultAmountCents,
            currency = currency,
            categoryId = categoryId,
            note = note
        )
        val current = getAll().toMutableList()
        current.add(template)
        save(current)
        return template
    }

    /**
     * تحديث قالب.
     */
    suspend fun update(template: ExpenseTemplate) {
        val current = getAll().toMutableList()
        val idx = current.indexOfFirst { it.id == template.id }
        if (idx >= 0) {
            current[idx] = template
            save(current)
        }
    }

    /**
     * حذف قالب.
     */
    suspend fun delete(templateId: String) {
        val current = getAll().filter { it.id != templateId }
        save(current)
    }

    private suspend fun save(templates: List<ExpenseTemplate>) {
        settingsDao.put(
            SettingsEntity(KEY, json.encodeToString(templates))
        )
    }
}