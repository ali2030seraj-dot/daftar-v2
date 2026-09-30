package com.daftar.v2.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object AppIcons {

    // ═══════ الشخص والأطراف ═══════
    val person: ImageVector = Icons.Rounded.Person
    val personAdd: ImageVector = Icons.Rounded.PersonAdd
    val people: ImageVector = Icons.Rounded.Person

    // ═══════ المال والعمليات ═══════
    val money: ImageVector = Icons.Rounded.Payments
    val wallet: ImageVector = Icons.Rounded.AccountBalanceWallet
    val card: ImageVector = Icons.Rounded.CreditCard
    val receipt: ImageVector = Icons.Rounded.Receipt
    val receiptLong: ImageVector = Icons.Rounded.ReceiptLong
    val payment: ImageVector = Icons.Rounded.Paid

    // ═══════ الحسابات ═══════
    val statement: ImageVector = Icons.Rounded.Description
    val addDebt: ImageVector = Icons.Rounded.PostAdd
    val addPayment: ImageVector = Icons.Rounded.Paid

    // ═══════ العملات ═══════
    val currency: ImageVector = Icons.Rounded.CurrencyExchange
    val moneyAlt: ImageVector = Icons.Rounded.AttachMoney

    // ═══════ التقارير ═══════
    val reports: ImageVector = Icons.Rounded.Assessment
    val analytics: ImageVector = Icons.Rounded.Assessment

    // ═══════ الإعدادات والأدوات ═══════
    val settings: ImageVector = Icons.Rounded.Settings
    val security: ImageVector = Icons.Rounded.Security
    val backup: ImageVector = Icons.Rounded.Backup
    val archive: ImageVector = Icons.Rounded.Archive
    val history: ImageVector = Icons.Rounded.History
    val bell: ImageVector = Icons.Rounded.Notifications
    val search: ImageVector = Icons.Rounded.Search
    val menu: ImageVector = Icons.Rounded.Menu
    val more: ImageVector = Icons.Rounded.MoreHoriz

    // ═══════ القوالب ═══════
    val templates: ImageVector = Icons.Rounded.ListAlt

    // ═══════ الزكاة والتقويم ═══════
    val zakat: ImageVector = Icons.Rounded.VolunteerActivism
    val calendar: ImageVector = Icons.Rounded.CalendarMonth

    // ═══════ التفعيل ═══════
    val activate: ImageVector = Icons.Rounded.VerifiedUser

    // ═══════ التنقل السفلي ═══════
    val home: ImageVector = Icons.Rounded.Home
    val info: ImageVector = Icons.Rounded.Info
    val guide: ImageVector = Icons.Rounded.MenuBook

    // ═══════ الاتجاهات ═══════
    val trendingUp: ImageVector = Icons.Rounded.TrendingUp
    val trendingDown: ImageVector = Icons.Rounded.TrendingDown

    /**
     * ⭐ الألوان الدلالية الموحّدة
     */
    object Colors {
        // ═══════ الأساسية ═══════
        val navy = Color(0xFF172554)
        val navyLight = Color(0xFF1E3A8A)
        val gold = Color(0xFFD4A84B)
        val goldLight = Color(0xFFF4D97A)
        val goldDark = Color(0xFFB8941F)
        val neutral = Color(0xFF64748B)

        // ═══════ الدلالية ═══════
        val incoming = Color(0xFF10B981)
        val outgoing = Color(0xFFEF4444)
        val expense = Color(0xFFD4A84B)

        // ═══════ مختصرات للتوافق ═══════
        val warning = gold
        val info = navyLight
        val purple = navy
        val cyan = neutral
    }
}