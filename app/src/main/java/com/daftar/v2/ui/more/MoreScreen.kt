package com.daftar.v2.ui.more

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.StaggeredAppear
import com.daftar.v2.ui.navigation.Destinations
import com.daftar.v2.ui.theme.AppIcons
import com.daftar.v2.ui.theme.GoldAccent
import com.daftar.v2.ui.theme.GoldDark
import com.daftar.v2.util.HijriDateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoreScreen(navController: NavHostController) {
    val tokens = LocalAdaptiveTokens.current

    AmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = tokens.screenPaddingHorizontal,
                    vertical = tokens.screenPaddingVertical
                ),
            verticalArrangement = Arrangement.spacedBy(tokens.cardSpacing)
        ) {
            Spacer(Modifier.height(4.dp))

            StaggeredAppear(index = 0) {
                Text(
                    text = "المزيد",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 1. دليل الاستخدام
            StaggeredAppear(index = 1) {
                MoreItem(
                    icon = AppIcons.statement,
                    title = "دليل الاستخدام",
                    subtitle = "شرح مفصل لكل ميزات التطبيق",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.GUIDE) }
                )
            }

            // 2. الأرشيف
            StaggeredAppear(index = 2) {
                MoreItem(
                    icon = AppIcons.archive,
                    title = "الأرشيف",
                    subtitle = "الحسابات المؤرشفة",
                    color = AppIcons.Colors.neutral,
                    onClick = { navController.navigate(Destinations.ARCHIVE) }
                )
            }

            // 3. التقرير الشهري
            StaggeredAppear(index = 3) {
                MoreItem(
                    icon = AppIcons.reports,
                    title = "التقرير الشهري",
                    subtitle = "إحصائيات ومقارنة بالأشهر",
                    color = AppIcons.Colors.navy,
                    onClick = { navController.navigate(Destinations.MONTHLY_REPORT) }
                )
            }

            // 4. التقرير السنوي
            StaggeredAppear(index = 4) {
                MoreItem(
                    icon = AppIcons.calendar,
                    title = "التقرير السنوي",
                    subtitle = "ملخص كامل للسنة",
                    color = AppIcons.Colors.navy,
                    onClick = { navController.navigate(Destinations.ANNUAL_REPORT) }
                )
            }

            // ⭐⭐⭐ جديد: 5. تقرير المصروفات
            StaggeredAppear(index = 5) {
                MoreItem(
                    icon = AppIcons.reports,
                    title = "تقرير المصروفات",
                    subtitle = "عرض تفصيلي للمصروفات",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.EXPENSE_REPORT) }
                )
            }

            // 6. التقارير المتقدمة
            StaggeredAppear(index = 6) {
                MoreItem(
                    icon = AppIcons.reports,
                    title = "التقارير المتقدمة",
                    subtitle = "أعمار الديون، التدفق النقدي، المقارنة",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.ADVANCED_REPORTS) }
                )
            }

            // 7. ملخص العملات
            StaggeredAppear(index = 7) {
                MoreItem(
                    icon = AppIcons.currency,
                    title = "ملخص العملات",
                    subtitle = "رصيدك في كل عملة على حدة",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.CURRENCY_SUMMARY) }
                )
            }

            // 8. حاسبة الزكاة
            StaggeredAppear(index = 8) {
                MoreItem(
                    icon = AppIcons.zakat,
                    title = "حاسبة الزكاة",
                    subtitle = "احسب زكاة مالك وذهبك",
                    color = AppIcons.Colors.incoming,
                    onClick = { navController.navigate(Destinations.ZAKAT) }
                )
            }

            // 9. التقويم الهجري
            StaggeredAppear(index = 9) {
                HijriCalendarCard(
                    onClick = { navController.navigate(Destinations.HIJRI_CALENDAR) }
                )
            }

            // 10. قوالب المصروفات
            StaggeredAppear(index = 10) {
                MoreItem(
                    icon = AppIcons.templates,
                    title = "قوالب المصروفات",
                    subtitle = "إيجار، كهرباء، إنترنت...",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.EXPENSE_TEMPLATES) }
                )
            }

            // 11. سجل التغييرات
            StaggeredAppear(index = 11) {
                MoreItem(
                    icon = AppIcons.history,
                    title = "سجل التغييرات",
                    subtitle = "كل ما حدث في التطبيق",
                    color = AppIcons.Colors.neutral,
                    onClick = { navController.navigate(Destinations.AUDIT_LOG) }
                )
            }

            // 12. النسخ الاحتياطي
            StaggeredAppear(index = 12) {
                MoreItem(
                    icon = AppIcons.backup,
                    title = "النسخ الاحتياطي",
                    subtitle = "محلي + سحابي تلقائي",
                    color = AppIcons.Colors.navy,
                    onClick = { navController.navigate(Destinations.BACKUP) }
                )
            }

            // 13. هوية المتجر
            StaggeredAppear(index = 13) {
                MoreItem(
                    icon = AppIcons.settings,
                    title = "هوية المتجر",
                    subtitle = "الشعار والاسم والبيانات",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.BRANDING) }
                )
            }

            // 14. الأمان
            StaggeredAppear(index = 14) {
                MoreItem(
                    icon = AppIcons.security,
                    title = "الأمان وكلمة المرور",
                    subtitle = "قفل التطبيق والتغيير",
                    color = AppIcons.Colors.outgoing,
                    onClick = { navController.navigate(Destinations.SECURITY) }
                )
            }

            // 15. إعدادات التنبيهات
            StaggeredAppear(index = 15) {
                MoreItem(
                    icon = AppIcons.bell,
                    title = "إعدادات التنبيهات",
                    subtitle = "تنبيهات الديون المتأخرة",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.REMINDER_SETTINGS) }
                )
            }

            // 16. قوالب الرسائل
            StaggeredAppear(index = 16) {
                MoreItem(
                    icon = AppIcons.bell,
                    title = "قوالب الرسائل",
                    subtitle = "تخصيص نصوص SMS",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.SMS_TEMPLATES) }
                )
            }

            // 17. الفواتير
            StaggeredAppear(index = 17) {
                MoreItem(
                    icon = AppIcons.statement,
                    title = "الفواتير",
                    subtitle = "إنشاء وإدارة الفواتير",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.INVOICE_LIST) }
                )
            }

            // 18. تفعيل التطبيق
            StaggeredAppear(index = 18) {
                MoreItem(
                    icon = AppIcons.activate,
                    title = "تفعيل التطبيق",
                    subtitle = "إدخال كود التفعيل أو التجديد",
                    color = GoldAccent,
                    onClick = { navController.navigate(Destinations.ACTIVATION) }
                )
            }

            // 19. لوحة الإدارة
            StaggeredAppear(index = 19) {
                MoreItem(
                    icon = AppIcons.settings,
                    title = "لوحة الإدارة",
                    subtitle = "للمطور فقط",
                    color = AppIcons.Colors.neutral,
                    onClick = { navController.navigate(Destinations.ADMIN_LOGIN) }
                )
            }

            // 20. حول التطبيق
            StaggeredAppear(index = 20) {
                MoreItem(
                    icon = AppIcons.person,
                    title = "حول التطبيق",
                    subtitle = "المميزات والتواصل والسياسات",
                    color = AppIcons.Colors.navy,
                    onClick = { navController.navigate(Destinations.ABOUT) }
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun HijriCalendarCard(onClick: () -> Unit) {
    val now = remember { System.currentTimeMillis() }
    val hijriLabel = remember(now) { HijriDateUtils.hijriLabel(now) }
    val gregorianLabel = remember(now) {
        val sdf = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar", "SA"))
        sdf.format(Date(now))
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTileSmall(icon = AppIcons.calendar, color = GoldAccent)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "التقويم الهجري",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = hijriLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default,
                    color = GoldDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = gregorianLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun MoreItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTileSmall(icon = icon, color = color)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun IconTileSmall(
    icon: ImageVector,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
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
}