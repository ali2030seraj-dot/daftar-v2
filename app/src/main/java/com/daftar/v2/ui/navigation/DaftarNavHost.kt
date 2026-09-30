package com.daftar.v2.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.daftar.v2.backup.BackupScreen
import com.daftar.v2.cloud.CloudBackupScreen
import com.daftar.v2.data.entity.PersonType
import com.daftar.v2.reports.AdvancedReportsScreen
import com.daftar.v2.ui.about.AboutScreen
import com.daftar.v2.ui.about.PrivacyPolicyScreen
import com.daftar.v2.ui.admin.AdminLoginScreen
import com.daftar.v2.ui.admin.AdminPanelScreen
import com.daftar.v2.ui.annual.AnnualReportScreen
import com.daftar.v2.ui.archive.ArchiveScreen
import com.daftar.v2.ui.audit.AuditLogScreen
import com.daftar.v2.ui.branding.BrandingScreen
import com.daftar.v2.ui.currency.CurrencyPickerScreen
import com.daftar.v2.ui.currency.CurrencySummaryScreen
import com.daftar.v2.ui.dashboard.DashboardScreen
import com.daftar.v2.ui.expenses.ExpenseReportScreen
import com.daftar.v2.ui.expenses.ExpensesScreen
import com.daftar.v2.ui.guide.GuideScreen
import com.daftar.v2.ui.hijri.HijriCalendarScreen
import com.daftar.v2.ui.invoice.InvoiceEditScreen
import com.daftar.v2.ui.invoice.InvoiceListScreen
import com.daftar.v2.ui.invoice.InvoiceViewScreen
import com.daftar.v2.ui.license.ActivationScreen
import com.daftar.v2.ui.more.MoreScreen
import com.daftar.v2.ui.people.PeopleScreen
import com.daftar.v2.ui.receipt.ReceiptScreen
import com.daftar.v2.ui.reminder.ReminderSettingsScreen
import com.daftar.v2.ui.report.MonthlyReportScreen
import com.daftar.v2.ui.restore.RestoreScreen
import com.daftar.v2.ui.security.SecurityScreen
import com.daftar.v2.ui.settings.SettingsScreen
import com.daftar.v2.ui.sms.SmsTemplatesScreen
import com.daftar.v2.ui.statement.StatementScreen
import com.daftar.v2.ui.templates.ExpenseTemplatesScreen
import com.daftar.v2.ui.zakat.ZakatScreen

@Composable
fun DaftarNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.DASHBOARD,
        modifier = modifier
    ) {
        composable(Destinations.DASHBOARD) {
            DashboardScreen(navController = navController)
        }

        composable(Destinations.CREDITORS) {
            PeopleScreen(navController = navController, type = PersonType.CREDITOR)
        }
        composable(Destinations.DEBTORS) {
            PeopleScreen(navController = navController, type = PersonType.DEBTOR)
        }

        composable(Destinations.EXPENSES) {
            ExpensesScreen(navController = navController)
        }
        composable(Destinations.EXPENSE_REPORT) {
            ExpenseReportScreen(navController = navController)
        }

        composable(Destinations.MORE) {
            MoreScreen(navController = navController)
        }
        composable(Destinations.ARCHIVE) {
            ArchiveScreen(navController = navController)
        }
        composable(Destinations.AUDIT_LOG) {
            AuditLogScreen(navController = navController)
        }
        composable(Destinations.BACKUP) {
            BackupScreen(navController = navController)
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(navController = navController)
        }
        composable(Destinations.SECURITY) {
            SecurityScreen(navController = navController)
        }

        composable(Destinations.GUIDE) {
            GuideScreen(navController = navController)
        }

        composable(Destinations.CURRENCY_PICKER) {
            CurrencyPickerScreen(navController = navController)
        }

        composable("${Destinations.STATEMENT}/{${Destinations.ARG_PERSON_ID}}") { backStackEntry ->
            val personId = backStackEntry.arguments
                ?.getString(Destinations.ARG_PERSON_ID)
                ?.toLongOrNull() ?: 0L
            StatementScreen(navController = navController, personId = personId)
        }

        composable(Destinations.STATEMENT) {
            StatementScreen(navController = navController, personId = 0L)
        }

        composable(
            "${Destinations.RECEIPT}/{${Destinations.ARG_PERSON_ID}}/{${Destinations.ARG_TRANSACTION_ID}}"
        ) { backStackEntry ->
            val personId = backStackEntry.arguments
                ?.getString(Destinations.ARG_PERSON_ID)
                ?.toLongOrNull() ?: 0L
            val txId = backStackEntry.arguments
                ?.getString(Destinations.ARG_TRANSACTION_ID)
                ?.toLongOrNull() ?: 0L
            ReceiptScreen(
                navController = navController,
                personId = personId,
                transactionId = txId
            )
        }

        composable(Destinations.MONTHLY_REPORT) {
            MonthlyReportScreen(navController = navController)
        }
        composable(Destinations.ANNUAL_REPORT) {
            AnnualReportScreen(navController = navController)
        }
        composable(Destinations.ADVANCED_REPORTS) {
            AdvancedReportsScreen(navController = navController)
        }
        composable(Destinations.REMINDER_SETTINGS) {
            ReminderSettingsScreen(navController = navController)
        }
        composable(Destinations.CURRENCY_SUMMARY) {
            CurrencySummaryScreen(navController = navController)
        }
        composable(Destinations.ZAKAT) {
            ZakatScreen(navController = navController)
        }
        composable(Destinations.HIJRI_CALENDAR) {
            HijriCalendarScreen(navController = navController)
        }
        composable(Destinations.EXPENSE_TEMPLATES) {
            ExpenseTemplatesScreen(navController = navController)
        }
        composable(Destinations.BRANDING) {
            BrandingScreen(navController = navController)
        }
        composable(Destinations.SMS_TEMPLATES) {
            SmsTemplatesScreen(navController = navController)
        }

        // ⭐ الفواتير
        composable(Destinations.INVOICE_LIST) {
            InvoiceListScreen(navController = navController)
        }
        composable(
            route = "${Destinations.INVOICE_EDIT}/{${Destinations.ARG_INVOICE_ID}}",
            arguments = listOf(
                navArgument(Destinations.ARG_INVOICE_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "0"
                }
            )
        ) {
            InvoiceEditScreen(navController = navController)
        }
        composable(
            route = "${Destinations.INVOICE_VIEW}/{${Destinations.ARG_INVOICE_ID}}",
            arguments = listOf(
                navArgument(Destinations.ARG_INVOICE_ID) {
                    type = NavType.StringType
                    nullable = false
                    defaultValue = "0"
                }
            )
        ) {
            InvoiceViewScreen(navController = navController)
        }

        // ⭐ النسخ السحابي
        composable(Destinations.CLOUD_BACKUP) {
            CloudBackupScreen(navController = navController)
        }

        composable(Destinations.ABOUT) {
            AboutScreen(navController = navController)
        }
        composable(Destinations.PRIVACY_POLICY) {
            PrivacyPolicyScreen(navController = navController)
        }

        composable(Destinations.ACTIVATION) {
            ActivationScreen(navController = navController)
        }
        composable(Destinations.ADMIN_LOGIN) {
            AdminLoginScreen(navController = navController)
        }
        composable(Destinations.ADMIN_PANEL) {
            AdminPanelScreen(navController = navController)
        }

        composable(
            route = "${Destinations.RESTORE}?${Destinations.ARG_FILE_URI}={${Destinations.ARG_FILE_URI}}",
            arguments = listOf(
                navArgument(Destinations.ARG_FILE_URI) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString(Destinations.ARG_FILE_URI)
            val fileUri = encodedUri
                ?.takeIf { it.isNotBlank() }
                ?.let { Uri.parse(Uri.decode(it)) }

            RestoreScreen(
                incomingUri = fileUri,
                onBack = { navController.popBackStack() }
            )
        }
    }
}