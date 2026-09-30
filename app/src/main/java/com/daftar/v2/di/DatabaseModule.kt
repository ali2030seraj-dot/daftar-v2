package com.daftar.v2.di

import android.content.Context
import androidx.room.Room
import com.daftar.v2.data.db.AppDatabase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10   // ⭐ جديد
            )
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun personDao(db: AppDatabase): PersonDao = db.personDao()

    @Provides
    fun transactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun expenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun expenseCategoryDao(db: AppDatabase): ExpenseCategoryDao = db.expenseCategoryDao()

    @Provides
    fun settingsDao(db: AppDatabase): SettingsDao = db.settingsDao()

    @Provides
    fun auditLogDao(db: AppDatabase): AuditLogDao = db.auditLogDao()

    @Provides
    fun licenseDao(db: AppDatabase): LicenseDao = db.licenseDao()

    @Provides
    fun recurringTransactionDao(db: AppDatabase): RecurringTransactionDao =
        db.recurringTransactionDao()

    @Provides
    fun smsTemplateDao(db: AppDatabase): SmsTemplateDao = db.smsTemplateDao()

    // ⭐ جديد
    @Provides
    fun invoiceDao(db: AppDatabase): InvoiceDao = db.invoiceDao()
}