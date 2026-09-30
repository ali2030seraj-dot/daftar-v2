package com.daftar.v2.ui.about

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.navigation.Destinations
import com.daftar.v2.ui.theme.AppIcons
import com.daftar.v2.ui.theme.GoldAccent
import com.daftar.v2.ui.theme.GoldDark
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavHostController
) {
    val context = LocalContext.current

    val whatsappNumber = "967775670090"
    val developerEmail = "ali2030seraj@gmail.com"
    val appVersion = "2.0.0"

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "حول التطبيق",
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ⭐ شعار التطبيق
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            color = GoldAccent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.statement,
                        contentDescription = null,
                        tint = GoldDark,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "دفتر حساباتي",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Default
                )

                Text(
                    text = "الإصدار $appVersion",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )

                Text(
                    text = "دفتر مالي شخصي يعمل دون إنترنت",
                    fontSize = 14.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Default
                )

                Spacer(Modifier.height(8.dp))

                // ⭐⭐⭐ قسم جديد: الميزات التلقائية (تعمل عند بدء التطبيق)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.activate,
                                contentDescription = null,
                                tint = Color(0xFF0EA5E9),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "🚀 الميزات التلقائية",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Default
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "تعمل تلقائياً عند بدء التطبيق",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Default
                        )
                        Spacer(Modifier.height(12.dp))

                        AboutFeatureRow(AppIcons.history, "فحص التحديثات تلقائياً")
                        AboutFeatureRow(AppIcons.activate, "فترة تجريبية 60 يوم")
                        AboutFeatureRow(AppIcons.security, "قفل التطبيق بالبصمة")
                        AboutFeatureRow(AppIcons.backup, "نسخ احتياطي محلي يومي")
                        AboutFeatureRow(AppIcons.backup, "نسخ سحابي تلقائي إلى Google Drive")
                        AboutFeatureRow(AppIcons.bell, "تنبيهات مجدولة للديون المتأخرة")
                        AboutFeatureRow(AppIcons.history, "المعاملات المتكررة (أسبوعي/شهري/سنوي)")
                        AboutFeatureRow(AppIcons.person, "شاشة الترحيب (Onboarding)")
                        AboutFeatureRow(AppIcons.security, "طلب الصلاحيات عند التشغيل")
                        AboutFeatureRow(AppIcons.bell, "إنشاء قنوات الإشعارات")
                    }
                }

                // ⭐⭐⭐ قسم جديد: الميزات الحديثة
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.receipt,
                                contentDescription = null,
                                tint = Color(0xFF9333EA),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "✨ الميزات الحديثة",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Default
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        AboutFeatureRow(AppIcons.history, "المعاملات المتكررة التلقائية")
                        AboutFeatureRow(AppIcons.reports, "التقارير المتقدمة (أعمار الديون، التدفق النقدي)")
                        AboutFeatureRow(AppIcons.bell, "قوالب الرسائل (SMS) القابلة للتخصيص")
                        AboutFeatureRow(AppIcons.statement, "الفواتير مع PDF عربي احترافي")
                        AboutFeatureRow(AppIcons.backup, "النسخ الاحتياطي السحابي")
                        AboutFeatureRow(AppIcons.person, "اختيار العملاء من جهات الاتصال")
                        AboutFeatureRow(AppIcons.bell, "إرسال SMS / واتساب بتحديد الاتجاه")
                        AboutFeatureRow(AppIcons.currency, "إصلاحات في التقارير والكشوفات")
                    }
                }

                // ⭐ المميزات الأساسية
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.receipt,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "المميزات الأساسية",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Default
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        AboutFeatureRow(AppIcons.trendingUp, "إدارة الديون (لك/عليك)")
                        AboutFeatureRow(AppIcons.statement, "إيصالات QR احترافية")
                        AboutFeatureRow(AppIcons.reports, "تقارير شهرية وسنوية")
                        AboutFeatureRow(AppIcons.receipt, "إدارة المصروفات والتصنيفات")
                        AboutFeatureRow(AppIcons.calendar, "التقويم الهجري")
                        AboutFeatureRow(AppIcons.zakat, "حاسبة الزكاة")
                        AboutFeatureRow(AppIcons.currency, "ملخص العملات المتعددة")
                        AboutFeatureRow(AppIcons.settings, "هوية المتجر (شعار + بيانات)")
                        AboutFeatureRow(AppIcons.security, "خصوصية كاملة — بياناتك في جهازك")
                    }
                }

                // ⭐ معلومات المطور
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.person,
                                contentDescription = null,
                                tint = AppIcons.Colors.navy,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "المطور",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Default
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        AboutInfoRow("التطبيق", "دفتر حساباتي")
                        AboutInfoRow("الإصدار", appVersion)
                        AboutInfoRow("العملات", "6 عملات مدعومة")
                        AboutInfoRow("التواصل", "775670090")
                    }
                }

                // ⭐ التواصل
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Chat,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "التواصل والدعم",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Default
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val url = "https://wa.me/$whatsappNumber?text=" +
                                        Uri.encode("مرحبًا، أحتاج مساعدة في تطبيق دفتر حساباتي")
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    )
                                } catch (_: Exception) { }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "واتساب المطور",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$developerEmail")
                                    putExtra(Intent.EXTRA_SUBJECT, "استفسار عن دفتر حساباتي")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) { }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Rounded.Email,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "البريد الإلكتروني",
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                }

                // ⭐ سياسة الخصوصية
                Button(
                    onClick = { navController.navigate(Destinations.PRIVACY_POLICY) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "سياسة الخصوصية",
                        fontFamily = FontFamily.Default
                    )
                }

                // ⭐ شارك التطبيق
                OutlinedButton(
                    onClick = { ShareHelper.shareApp(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "شارك التطبيق",
                        fontFamily = FontFamily.Default
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "© 2026 دفتر حساباتي",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              Helpers
// ═══════════════════════════════════════════════════════════

@Composable
private fun AboutFeatureRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = GoldAccent.copy(alpha = 0.12f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldDark,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Default
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Default
        )
    }
}