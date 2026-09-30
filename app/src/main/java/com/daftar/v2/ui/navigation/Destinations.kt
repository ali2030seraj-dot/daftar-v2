package com.daftar.v2.ui.navigation

import android.net.Uri

object Destinations {
    const val DASHBOARD = "dashboard"
    const val CREDITORS = "creditors"
    const val DEBTORS = "debtors"
    const val EXPENSES = "expenses"
    const val MORE = "more"
    const val STATEMENT = "statement"
    const val ARCHIVE = "archive"
    const val AUDIT_LOG = "audit_log"
    const val BACKUP = "backup"
    const val SETTINGS = "settings"
    const val SECURITY = "security"
    const val CURRENCY_PICKER = "currency_picker"

    const val GUIDE = "guide"

    const val ACTIVATION = "activation"
    const val ADMIN_LOGIN = "admin_login"
    const val ADMIN_PANEL = "admin_panel"

    const val RECEIPT = "receipt"
    const val MONTHLY_REPORT = "monthly_report"
    const val ANNUAL_REPORT = "annual_report"
    const val ADVANCED_REPORTS = "advanced_reports"
    const val REMINDER_SETTINGS = "reminder_settings"
    const val CURRENCY_SUMMARY = "currency_summary"
    const val ZAKAT = "zakat"
    const val EXPENSE_TEMPLATES = "expense_templates"
    const val BRANDING = "branding"
    const val EXPENSE_REPORT = "expense_report"
    const val HIJRI_CALENDAR = "hijri_calendar"
    const val SMS_TEMPLATES = "sms_templates"

    // ⭐ الفواتير
    const val INVOICE_LIST = "invoice_list"
    const val INVOICE_EDIT = "invoice_edit"
    const val INVOICE_VIEW = "invoice_view"

    // ⭐ النسخ السحابي
    const val CLOUD_BACKUP = "cloud_backup"

    const val ABOUT = "about"
    const val PRIVACY_POLICY = "privacy_policy"
    const val RESTORE = "restore"

    const val ARG_PERSON_ID = "personId"
    const val ARG_TRANSACTION_ID = "transactionId"
    const val ARG_FILE_URI = "fileUri"
    const val ARG_INVOICE_ID = "invoiceId"

    fun statement(personId: Long = 0L): String = "$STATEMENT/$personId"

    fun receipt(personId: Long, transactionId: Long): String =
        "$RECEIPT/$personId/$transactionId"

    fun restore(fileUri: String? = null): String {
        return if (fileUri.isNullOrBlank()) {
            RESTORE
        } else {
            "$RESTORE?$ARG_FILE_URI=${Uri.encode(fileUri)}"
        }
    }

    fun invoiceEdit(invoiceId: Long = 0L): String = "$INVOICE_EDIT/$invoiceId"

    fun invoiceView(invoiceId: Long): String = "$INVOICE_VIEW/$invoiceId"
}