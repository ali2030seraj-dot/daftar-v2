package com.daftar.v2.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.PersonType
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    // ============ Flow ============

    @Query("SELECT * FROM people WHERE isArchived = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeActive(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE isArchived = 1 ORDER BY name COLLATE NOCASE ASC")
    fun observeArchived(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE type = :type AND isArchived = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeByType(type: PersonType): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<PersonEntity?>

    // ============ suspend ============

    @Query("SELECT * FROM people")
    suspend fun getAll(): List<PersonEntity>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getById(id: Long): PersonEntity?

    @Query("SELECT * FROM people WHERE type = :type AND isArchived = 0 ORDER BY name COLLATE NOCASE ASC")
    suspend fun getByType(type: PersonType): List<PersonEntity>

    /**
     * ✅ الإصلاح الجوهري:
     * - يُعيد List (ليس PersonEntity?)
     * - يدعم excludeId
     */
    @Query("""
        SELECT * FROM people 
        WHERE name = :name 
          AND type = :type 
          AND currency = :currency 
          AND isArchived = 0
          AND (:excludeId IS NULL OR id != :excludeId)
    """)
    suspend fun findByNameTypeCurrency(
        name: String,
        type: PersonType,
        currency: String,
        excludeId: Long? = null
    ): List<PersonEntity>

    // ============ كتابة ============

    @Insert
    suspend fun insert(person: PersonEntity): Long

    @Update
    suspend fun update(person: PersonEntity)

    @Delete
    suspend fun delete(person: PersonEntity)

    @Query("UPDATE people SET isArchived = :archived, updatedAt = :now WHERE id = :id")
    suspend fun setArchived(
        id: Long,
        archived: Boolean,
        now: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM people")
    suspend fun deleteAll()
}