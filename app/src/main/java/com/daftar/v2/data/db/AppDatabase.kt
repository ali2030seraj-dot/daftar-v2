package com.daftar.v2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.daftar.v2.data.db.dao.AuditLogDao
import com.daftar.v2.data.db.dao.ExpenseCategoryDao
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.db.dao.InvoiceDao
import com.daftar.v2.data.db.dao.LicenseDao
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.RecurringTransactionDao
import com.daftar.v2.data.db.dao.SettingsDao
import com.daftar.v2.data.db.dao.SmsTemplateDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.AuditLogEntity
import com.daftar.v2.data.entity.ExpenseCategoryEntity
import com.daftar.v2.data.entity.ExpenseEntity
import com.daftar.v2.data.entity.InvoiceEntity
import com.daftar.v2.data.entity.InvoiceItemEntity
import com.daftar.v2.data.entity.LicenseEntity
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.RecurringTransactionEntity
import com.daftar.v2.data.entity.SettingsEntity
import com.daftar.v2.data.entity.SmsTemplateEntity
import com.daftar.v2.data.entity.TimeGuardEntity
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.UsedLicenseEntity

@Database(
    entities = [
        PersonEntity::class,
        TransactionEntity::class,
        ExpenseEntity::class,
        ExpenseCategoryEntity::class,
        SettingsEntity::class,
        AuditLogEntity::class,
        LicenseEntity::class,
        UsedLicenseEntity::class,
        TimeGuardEntity::class,
        RecurringTransactionEntity::class,
        SmsTemplateEntity::class,
        InvoiceEntity::class,        // ⭐ جديد
        InvoiceItemEntity::class     // ⭐ جديد
    ],
    version = 10,   // ⭐ 10
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): TransactionDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun licenseDao(): LicenseDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun smsTemplateDao(): SmsTemplateDao
    abstract fun invoiceDao(): InvoiceDao    // ⭐ جديد

    companion object {
        const val NAME = "daftar_v2.db"

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN voicePath TEXT")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `recurring_transactions` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `personId` INTEGER NOT NULL,
                        `type` TEXT NOT NULL,
                        `statement` TEXT NOT NULL,
                        `amountCents` INTEGER NOT NULL,
                        `note` TEXT NOT NULL,
                        `interval` TEXT NOT NULL,
                        `dayOfMonth` INTEGER NOT NULL,
                        `nextRunAt` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `lastRunAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sms_templates` (
                        `type` TEXT NOT NULL PRIMARY KEY,
                        `body` TEXT NOT NULL,
                        `isCustom` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        // ⭐ Migration جديد 9 → 10
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `invoices` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `number` TEXT NOT NULL,
                        `personId` INTEGER NOT NULL,
                        `currency` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `subtotalCents` INTEGER NOT NULL,
                        `taxPercent` REAL NOT NULL,
                        `taxCents` INTEGER NOT NULL,
                        `discountCents` INTEGER NOT NULL,
                        `totalCents` INTEGER NOT NULL,
                        `issuedAt` INTEGER NOT NULL,
                        `dueAt` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `invoice_items` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `invoiceId` INTEGER NOT NULL,
                        `description` TEXT NOT NULL,
                        `quantity` REAL NOT NULL,
                        `unitPriceCents` INTEGER NOT NULL,
                        `totalCents` INTEGER NOT NULL,
                        `position` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}