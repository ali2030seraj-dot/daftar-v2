package com.daftar.v2.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.daftar.v2.data.entity.ExpenseCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCategoryDao {

    @Query("SELECT * FROM expense_categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ExpenseCategoryEntity>>

    @Query("SELECT * FROM expense_categories")
    suspend fun getAll(): List<ExpenseCategoryEntity>

    // ✅ جديد — للاستيراد
    @Query("SELECT * FROM expense_categories WHERE id = :id")
    suspend fun getById(id: Long): ExpenseCategoryEntity?

    @Query("SELECT * FROM expense_categories WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): ExpenseCategoryEntity?

    @Insert
    suspend fun insert(category: ExpenseCategoryEntity): Long

    // ✅ جديد — للـ Merge
    @Update
    suspend fun update(category: ExpenseCategoryEntity)

    @Delete
    suspend fun delete(category: ExpenseCategoryEntity)

    @Query("DELETE FROM expense_categories WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("DELETE FROM expense_categories")
    suspend fun deleteAll()
}