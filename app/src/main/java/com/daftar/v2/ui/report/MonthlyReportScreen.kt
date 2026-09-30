package com.daftar.v2.ui.report

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.EmptyState
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    navController: NavHostController,
    viewModel: MonthlyReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("التقرير الشهري", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            copyToClipboard(context, buildMonthlyReportText(state), "التقرير الشهري")
                            Toast.makeText(context, "✅ تم النسخ", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ")
                        }
                        IconButton(onClick = {
                            shareText(context, buildMonthlyReportText(state), "التقرير الشهري")
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "مشاركة")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                )
            }
        ) { padding ->
            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    // ⭐ Hero Card — عنوان الشهر + التنقل
                    MonthHeroCard(
                        monthLabel = state.monthLabel,
                        hijriLabel = state.hijriLabel,
                        canGoNext = state.canGoNext,
                        onPrev = { viewModel.previousMonth() },
                        onNext = { viewModel.nextMonth() }
                    )

                    // ⭐ الملخص العام
                    SummaryCards(
                        transactionCount = state.totalTransactionCount,
                        expenseCount = state.totalExpenseCount,
                        currencyCount = state.currencies.size
                    )

                    // ⭐ بطاقة كل عملة
                    if (state.currencies.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                emoji = "📊",
                                title = "لا توجد عمليات",
                                subtitle = "لم تُسجل أي عمليات في هذا الشهر"
                            )
                        }
                    } else {
                        state.currencies.forEach { stats ->
                            CurrencyReportCard(stats)
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              ⭐ Hero Card — عنوان + تنقل
// ═══════════════════════════════════════════════════════════

@Composable
private fun MonthHeroCard(
    monthLabel: String,
    hijriLabel: String,
    canGoNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F1F4A), Color(0xFF1E3A8A))
                )
            )
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ⭐ زر السابق
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "السابق",
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }

            // ⭐ العنوان في الوسط
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Default
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = hijriLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TertiaryAmber,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Default
                )
            }

            // ⭐ زر التالي
            IconButton(
                onClick = onNext,
                enabled = canGoNext
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "التالي",
                    tint = if (canGoNext) Color.White.copy(alpha = 0.9f)
                    else Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              ⭐ الملخص العام (3 بطاقات صغيرة)
// ═══════════════════════════════════════════════════════════

@Composable
private fun SummaryCards(
    transactionCount: Int,
    expenseCount: Int,
    currencyCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmallSummaryCard(
            modifier = Modifier.weight(1f),
            emoji = "📝",
            label = "العمليات",
            value = transactionCount.toString(),
            accent = Color(0xFF2563EB)
        )
        SmallSummaryCard(
            modifier = Modifier.weight(1f),
            emoji = "🧾",
            label = "المصروفات",
            value = expenseCount.toString(),
            accent = TertiaryAmber
        )
        SmallSummaryCard(
            modifier = Modifier.weight(1f),
            emoji = "💱",
            label = "العملات",
            value = currencyCount.toString(),
            accent = SuccessGreen
        )
    }
}

@Composable
private fun SmallSummaryCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String,
    accent: Color
) {
    GlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = accent,
                fontFamily = FontFamily.Default
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              ⭐ بطاقة العملة الاحترافية
// ═══════════════════════════════════════════════════════════

@Composable
private fun CurrencyReportCard(stats: CurrencyMonthStats) {
    val netColor = if (stats.netCents >= 0) SuccessGreen else DangerRed

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {

            // ─── رأس البطاقة ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TertiaryAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💱",
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stats.currencyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${stats.transactionCount} عملية",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Default
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── الصافي (بارز) ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                netColor.copy(alpha = 0.12f),
                                netColor.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الصافي",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Default
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = MoneyFormatter.format(stats.netCents, stats.currencyCode),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = netColor,
                            fontFamily = FontFamily.Default
                        )
                    }
                    Text(
                        text = if (stats.netCents >= 0) "▲" else "▼",
                        fontSize = 24.sp,
                        color = netColor,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ─── تفصيل: 3 بطاقات فرعية ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DetailCell(
                    modifier = Modifier.weight(1f),
                    label = "الديون",
                    valueCents = stats.totalDebtCents,
                    currencyCode = stats.currencyCode,
                    color = DangerRed
                )
                DetailCell(
                    modifier = Modifier.weight(1f),
                    label = "السداد",
                    valueCents = stats.totalPaymentCents,
                    currencyCode = stats.currencyCode,
                    color = SuccessGreen
                )
                DetailCell(
                    modifier = Modifier.weight(1f),
                    label = "المصروفات",
                    valueCents = stats.totalExpensesCents,
                    currencyCode = stats.currencyCode,
                    color = TertiaryAmber
                )
            }
        }
    }
}

@Composable
private fun DetailCell(
    modifier: Modifier = Modifier,
    label: String,
    valueCents: Long,
    currencyCode: String,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Default,
                fontSize = 10.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = MoneyFormatter.formatShort(valueCents),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = color,
                fontFamily = FontFamily.Default,
                maxLines = 1
            )
            Text(
                text = com.daftar.v2.util.CurrencyUtils.arabicShortName(currencyCode),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default,
                fontSize = 9.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              نص التقرير
// ═══════════════════════════════════════════════════════════

private fun buildMonthlyReportText(state: MonthlyReportState): String {
    return buildString {
        appendLine("📊 التقرير الشهري — ${state.monthLabel}")
        appendLine("(${state.hijriLabel})")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine()

        if (state.currencies.isEmpty()) {
            appendLine("لا توجد عمليات في هذا الشهر.")
        } else {
            state.currencies.forEach { stats ->
                appendLine("💱 ${stats.currencyName}:")
                appendLine("• الديون: ${MoneyFormatter.format(stats.totalDebtCents, stats.currencyCode)}")
                appendLine("• السداد: ${MoneyFormatter.format(stats.totalPaymentCents, stats.currencyCode)}")
                appendLine("• المصروفات: ${MoneyFormatter.format(stats.totalExpensesCents, stats.currencyCode)}")
                appendLine("• الصافي: ${MoneyFormatter.format(stats.netCents, stats.currencyCode)}")
                appendLine("• عدد العمليات: ${stats.transactionCount}")
                appendLine()
            }
        }

        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("من تطبيق: دفتر حساباتي")
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun shareText(context: Context, text: String, subject: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة التقرير"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}