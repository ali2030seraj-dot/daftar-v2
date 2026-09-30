package com.daftar.v2.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daftar.v2.data.entity.SmsTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsTemplateDao {

    @Query("SELECT * FROM sms_templates")
    fun observeAll(): Flow<List<SmsTemplateEntity>>

    @Query("SELECT * FROM sms_templates WHERE type = :type LIMIT 1")
    suspend fun getByType(type: String): SmsTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(template: SmsTemplateEntity)

    @Query("DELETE FROM sms_templates WHERE type = :type")
    suspend fun deleteByType(type: String)

    @Query("DELETE FROM sms_templates")
    suspend fun deleteAll()
}