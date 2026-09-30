package com.daftar.v2.security.license

import com.daftar.v2.data.db.dao.LicenseDao
import com.daftar.v2.data.entity.LicenseEntity
import com.daftar.v2.data.entity.UsedLicenseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseRepository @Inject constructor(
    private val licenseDao: LicenseDao
) {

    fun observeActiveLicense(): Flow<LicenseEntity?> =
        licenseDao.observeActiveLicense()

    suspend fun getActiveLicense(): LicenseEntity? =
        licenseDao.getActiveLicense()

    suspend fun getLicenseById(licenseId: String): LicenseEntity? =
        licenseDao.getByLicenseId(licenseId)

    suspend fun saveLicense(license: LicenseEntity): Long =
        licenseDao.insertLicense(license)

    suspend fun deactivateLicense(id: Long) =
        licenseDao.deactivateLicense(id)

    suspend fun isLicenseUsed(licenseId: String): Boolean =
        licenseDao.getUsedLicense(licenseId) != null

    suspend fun markLicenseUsed(licenseId: String, deviceFingerprint: String) {
        licenseDao.insertUsedLicense(
            UsedLicenseEntity(
                licenseId = licenseId,
                deviceFingerprint = deviceFingerprint
            )
        )
    }

    suspend fun deleteAll() {
        licenseDao.deleteAllLicenses()
        licenseDao.deleteAllUsedLicenses()
    }
}