package com.daftar.v2.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.daftar.v2.data.entity.LicenseEntity
import com.daftar.v2.data.entity.TimeGuardEntity
import com.daftar.v2.data.entity.UsedLicenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LicenseDao {

    // ====== التراخيص ======

    @Query("SELECT * FROM licenses WHERE isActive = 1 ORDER BY activatedAt DESC LIMIT 1")
    fun observeActiveLicense(): Flow<LicenseEntity?>

    @Query("SELECT * FROM licenses WHERE isActive = 1 ORDER BY activatedAt DESC LIMIT 1")
    suspend fun getActiveLicense(): LicenseEntity?

    @Query("SELECT * FROM licenses WHERE licenseId = :licenseId LIMIT 1")
    suspend fun getByLicenseId(licenseId: String): LicenseEntity?

    @Insert
    suspend fun insertLicense(license: LicenseEntity): Long

    @Update
    suspend fun updateLicense(license: LicenseEntity)

    @Query("UPDATE licenses SET isActive = 0 WHERE id = :id")
    suspend fun deactivateLicense(id: Long)

    @Query("DELETE FROM licenses")
    suspend fun deleteAllLicenses()

    // ====== الأكواد المستخدمة ======

    @Query("SELECT * FROM used_licenses WHERE licenseId = :licenseId LIMIT 1")
    suspend fun getUsedLicense(licenseId: String): UsedLicenseEntity?

    @Insert
    suspend fun insertUsedLicense(used: UsedLicenseEntity): Long

    @Query("DELETE FROM used_licenses")
    suspend fun deleteAllUsedLicenses()

    // ====== حماية الوقت ======

    @Query("SELECT * FROM time_guard WHERE id = 1")
    suspend fun getTimeGuard(): TimeGuardEntity?

    @Insert
    suspend fun insertTimeGuard(guard: TimeGuardEntity): Long

    @Update
    suspend fun updateTimeGuard(guard: TimeGuardEntity)
}