package com.daftar.v2.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.DashboardSkeleton
import com.daftar.v2.ui.components.IconTile
import com.daftar.v2.ui.components.JiibIconTile
import com.daftar.v2.ui.components.StaggeredAppear
import com.daftar.v2.ui.navigation.Destinations
import com.daftar.v2.ui.theme.AppIcons
import com.daftar.v2.ui.theme.GoldAccent
import com.daftar.v2.ui.theme.GoldDark
import com.daftar.v2.ui.theme.NavyLight
import com.daftar.v2.ui.theme.NavyPrimary
import com.daftar.v2.util.CurrencyUtils
import com.daftar.v2.util.MoneyFormatter

// ═══════════════════════════════════════════════════════════
//              Helper
// ═══════════════════════════════════════════════════════════

private fun safeNavigate(navController: NavHostController, route: String) {
    try {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    } catch (e: Exception) {
        android.util.Log.e("NAV_ERROR", "فشل الانتقال إلى $route", e)
    }
}

private data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val route: String? = null,
    val action: String? = null
)

// ═══════════════════════════════════════════════════════════
//              الشاشة الرئيسية
// ═══════════════════════════════════════════════════════════

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel(),
    quickSettingsViewModel: QuickSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val darkMode by quickSettingsViewModel.darkMode.collectAsState()
    val tokens = LocalAdaptiveTokens.current

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showReportsDialog by remember { mutableStateOf(false) }

    AmbientBackground {
        if (state.loading && state.stats == null) {
            DashboardSkeleton()
            return@AmbientBackground
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = tokens.screenPaddingHorizontal,
                    vertical = tokens.screenPaddingVertical
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(2.dp))

            // ⭐ 1. رأس الصفحة
            StaggeredAppear(index = 0) {
                DashboardHeader(
                    currency = state.currency,
                    onNotificationsClick = {
                        safeNavigate(navController, Destinations.REMINDER_SETTINGS)
                    },
                    onSettingsClick = { showSettingsDialog = true }
                )
            }

            // ⭐ 2. الوضع المالي
            StaggeredAppear(index = 1) {
                FinancialStatusCard(
                    youHave = state.stats?.totalReceivableCents ?: 0,
                    youOwe = state.stats?.totalPayableCents ?: 0,
                    expenses = state.stats?.currentMonthExpensesCents ?: 0,
                    currency = state.currency,
                    onYouHaveClick = { safeNavigate(navController, Destinations.CREDITORS) },
                    onYouOweClick = { safeNavigate(navController, Destinations.DEBTORS) },
                    onExpensesClick = { safeNavigate(navController, Destinations.EXPENSES) }
                )
            }

            // ⭐ 3. الإجراءات السريعة
            StaggeredAppear(index = 2) {
                QuickActionsSection(
                    navController = navController,
                    onReportsClick = { showReportsDialog = true }
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    if (showSettingsDialog) {
        QuickSettingsDialog(
            currentCurrency = state.currency,
            isDarkMode = darkMode,
            onDismiss = { showSettingsDialog = false },
            onBrandingClick = {
                showSettingsDialog = false
                safeNavigate(navController, Destinations.BRANDING)
            },
            onDarkModeToggle = { quickSettingsViewModel.setDarkMode(it) },
            onOpenCurrencyPicker = {
                showSettingsDialog = false
                safeNavigate(navController, Destinations.CURRENCY_PICKER)
            }
        )
    }

    // ⭐⭐⭐ حوار التقارير — مع 4 خيارات الآن
    if (showReportsDialog) {
        ReportsChooserDialog(
            onDismiss = { showReportsDialog = false },
            onMonthly = {
                showReportsDialog = false
                safeNavigate(navController, Destinations.MONTHLY_REPORT)
            },
            onAnnual = {
                showReportsDialog = false
                safeNavigate(navController, Destinations.ANNUAL_REPORT)
            },
            // ⭐ جديد: تقرير المصروفات
            onExpenseReport = {
                showReportsDialog = false
                safeNavigate(navController, Destinations.EXPENSE_REPORT)
            },
            // ⭐ جديد: التقارير المتقدمة
            onAdvancedReports = {
                showReportsDialog = false
                safeNavigate(navController, Destinations.ADVANCED_REPORTS)
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
//              1. رأس الصفحة
// ═══════════════════════════════════════════════════════════

@Composable
private fun DashboardHeader(
    currency: String,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val greeting = "السلام عليكم ورحمة الله وبركاته"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "دفتر حساباتي",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = CurrencyUtils.arabicShortName(currency),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDark,
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconTile(
                icon = AppIcons.bell,
                tint = GoldAccent,
                size = 40.dp,
                iconSize = 20.dp,
                cornerRadius = 12.dp,
                modifier = Modifier.clickable(onClick = onNotificationsClick)
            )
            IconTile(
                icon = AppIcons.settings,
                tint = NavyPrimary,
                size = 40.dp,
                iconSize = 20.dp,
                cornerRadius = 12.dp,
                modifier = Modifier.clickable(onClick = onSettingsClick)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              2. بطاقة الوضع المالي
// ═══════════════════════════════════════════════════════════

@Composable
private fun FinancialStatusCard(
    youHave: Long,
    youOwe: Long,
    expenses: Long,
    currency: String,
    onYouHaveClick: () -> Unit,
    onYouOweClick: () -> Unit,
    onExpensesClick: () -> Unit
) {
    val net = youHave - youOwe - expenses
    val netIsPositive = net > 0
    val netIsZero = net == 0L

    val netColor = when {
        netIsZero -> Color.White
        netIsPositive -> Color(0xFF34D399)
        else -> Color(0xFFF87171)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(NavyPrimary, NavyLight)
                )
            )
            .padding(18.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الوضع المالي الحالي",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Default
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = CurrencyUtils.arabicFullName(currency),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontFamily = FontFamily.Default
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatBox(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onYouHaveClick),
                    icon = Icons.Rounded.TrendingUp,
                    label = "ما لك",
                    cents = youHave,
                    color = Color(0xFF34D399)
                )
                MiniStatBox(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onYouOweClick),
                    icon = Icons.Rounded.TrendingDown,
                    label = "ما عليك",
                    cents = youOwe,
                    color = Color(0xFFF87171)
                )
                MiniStatBox(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onExpensesClick),
                    icon = Icons.Rounded.Receipt,
                    label = "مصروفات",
                    cents = expenses,
                    color = GoldAccent
                )
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الصافي",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Default
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!netIsZero) {
                        Icon(
                            imageVector = if (netIsPositive)
                                Icons.Rounded.TrendingUp
                            else
                                Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = netColor,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }

                    if (!netIsZero) {
                        Text(
                            text = if (netIsPositive) "+" else "-",
                            color = netColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default,
                            fontSize = 24.sp
                        )
                        Spacer(Modifier.width(2.dp))
                    }

                    CompositionLocalProvider(
                        LocalLayoutDirection provides LayoutDirection.Ltr
                    ) {
                        Text(
                            text = MoneyFormatter.formatShort(kotlin.math.abs(net)),
                            color = netColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    cents: Long,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 6.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.9f),
                    fontFamily = FontFamily.Default,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(6.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = MoneyFormatter.formatShort(cents),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              3. الإجراءات السريعة
// ═══════════════════════════════════════════════════════════

@Composable
private fun QuickActionsSection(
    navController: NavHostController,
    onReportsClick: () -> Unit
) {
    val actions = remember {
        listOf(
            QuickAction(AppIcons.trendingUp, "ما لك", AppIcons.Colors.incoming, Destinations.CREDITORS),
            QuickAction(AppIcons.trendingDown, "ما عليك", AppIcons.Colors.outgoing, Destinations.DEBTORS),
            QuickAction(AppIcons.receipt, "مصروف", AppIcons.Colors.gold, Destinations.EXPENSES),

            QuickAction(AppIcons.currency, "عملات", AppIcons.Colors.navy, Destinations.CURRENCY_SUMMARY),

            QuickAction(AppIcons.reports, "تقارير", AppIcons.Colors.navy, action = "reports"),
            QuickAction(AppIcons.calendar, "التقويم", AppIcons.Colors.navy, Destinations.HIJRI_CALENDAR),

            QuickAction(AppIcons.zakat, "الزكاة", AppIcons.Colors.navy, Destinations.ZAKAT),
            QuickAction(AppIcons.security, "أمان", AppIcons.Colors.neutral, Destinations.SECURITY),
            QuickAction(AppIcons.activate, "تفعيل", AppIcons.Colors.gold, Destinations.ACTIVATION)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            actions.take(3).forEach { action ->
                JiibIconTile(
                    icon = action.icon,
                    label = action.label,
                    accentColor = action.color,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        when {
                            action.action == "reports" -> onReportsClick()
                            action.route != null -> safeNavigate(navController, action.route)
                        }
                    }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            actions.drop(3).take(3).forEach { action ->
                JiibIconTile(
                    icon = action.icon,
                    label = action.label,
                    accentColor = action.color,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        when {
                            action.action == "reports" -> onReportsClick()
                            action.route != null -> safeNavigate(navController, action.route)
                        }
                    }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            actions.drop(6).forEach { action ->
                JiibIconTile(
                    icon = action.icon,
                    label = action.label,
                    accentColor = action.color,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        when {
                            action.action == "reports" -> onReportsClick()
                            action.route != null -> safeNavigate(navController, action.route)
                        }
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              4. حوار الإعدادات السريعة
// ═══════════════════════════════════════════════════════════

@Composable
private fun QuickSettingsDialog(
    currentCurrency: String,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onBrandingClick: () -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onOpenCurrencyPicker: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "الإعدادات السريعة",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppIcons.Colors.navy.copy(alpha = 0.06f))
                        .clickable(onClick = onBrandingClick)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppIcons.Colors.navy.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.settings,
                            contentDescription = null,
                            tint = AppIcons.Colors.navy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "هوية المتجر",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIos,
                        contentDescription = null,
                        tint = AppIcons.Colors.navy,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppIcons.Colors.navy.copy(alpha = 0.06f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppIcons.Colors.navy.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.security,
                            contentDescription = null,
                            tint = AppIcons.Colors.navy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "الوضع الليلي",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppIcons.Colors.gold
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppIcons.Colors.gold.copy(alpha = 0.08f))
                        .clickable(onClick = onOpenCurrencyPicker)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppIcons.Colors.gold.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.currency,
                            contentDescription = null,
                            tint = GoldDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تغيير العملة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = CurrencyUtils.arabicFullName(currentCurrency),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldDark
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIos,
                        contentDescription = null,
                        tint = GoldDark,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════
//              5. حوار التقارير — مع 4 تقارير
// ═══════════════════════════════════════════════════════════

@Composable
private fun ReportsChooserDialog(
    onDismiss: () -> Unit,
    onMonthly: () -> Unit,
    onAnnual: () -> Unit,
    onExpenseReport: () -> Unit,
    onAdvancedReports: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختر التقرير",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // 1. التقرير الشهري
                ReportOptionRow(
                    icon = AppIcons.calendar,
                    title = "التقرير الشهري",
                    subtitle = "إحصائيات شهر معين مع مقارنات",
                    color = AppIcons.Colors.navy,
                    onClick = onMonthly
                )

                // 2. التقرير السنوي
                ReportOptionRow(
                    icon = AppIcons.reports,
                    title = "التقرير السنوي",
                    subtitle = "ملخص كامل لكل أشهر السنة",
                    color = AppIcons.Colors.gold,
                    onClick = onAnnual
                )

                // ⭐ 3. تقرير المصروفات (جديد)
                ReportOptionRow(
                    icon = Icons.Rounded.Receipt,
                    title = "تقرير المصروفات",
                    subtitle = "عرض تفصيلي لكل المصروفات",
                    color = Color(0xFFDC2626),
                    onClick = onExpenseReport
                )

                // ⭐ 4. التقارير المتقدمة (جديد)
                ReportOptionRow(
                    icon = AppIcons.reports,
                    title = "التقارير المتقدمة",
                    subtitle = "أعمار الديون، التدفق النقدي، المقارنة",
                    color = Color(0xFF7C3AED),
                    onClick = onAdvancedReports
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun ReportOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Default
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default
            )
        }
        Icon(
            imageVector = Icons.Filled.ArrowBackIos,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
    }
}