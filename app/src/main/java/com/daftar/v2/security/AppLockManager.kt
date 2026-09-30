package com.daftar.v2.security

import com.daftar.v2.data.db.dao.SettingsDao
import com.daftar.v2.data.entity.SettingsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(
    private val settingsDao: SettingsDao
) {

    companion object {
        private const val KEY_HASH = "password_hash"
        private const val KEY_SALT = "password_salt"
        private const val KEY_ITER = "password_iterations"
        private const val KEY_ENABLED = "lock_enabled"
    }

    suspend fun isLockEnabled(): Boolean =
        settingsDao.get(KEY_ENABLED) == "true"

    suspend fun hasPassword(): Boolean =
        settingsDao.get(KEY_HASH) != null

    suspend fun setPassword(password: String) {
        require(password.length >= 4) { "كلمة المرور قصيرة جدًا" }
        val r = PasswordHasher.hash(password)
        settingsDao.put(SettingsEntity(KEY_HASH, r.hashBase64))
        settingsDao.put(SettingsEntity(KEY_SALT, r.saltBase64))
        settingsDao.put(SettingsEntity(KEY_ITER, r.iterations.toString()))
    }

    suspend fun verify(password: String): Boolean {
        val h = settingsDao.get(KEY_HASH) ?: return false
        val s = settingsDao.get(KEY_SALT) ?: return false
        val i = settingsDao.get(KEY_ITER)?.toIntOrNull() ?: return false
        return PasswordHasher.verify(password, h, s, i)
    }

    suspend fun enableLock() {
        require(hasPassword()) { "لا توجد كلمة مرور معيّنة" }
        settingsDao.put(SettingsEntity(KEY_ENABLED, "true"))
    }

    suspend fun disableLock() {
        settingsDao.put(SettingsEntity(KEY_ENABLED, "false"))
    }

    suspend fun changePassword(current: String, new: String) {
        require(verify(current)) { "كلمة المرور الحالية غير صحيحة" }
        setPassword(new)
    }
}