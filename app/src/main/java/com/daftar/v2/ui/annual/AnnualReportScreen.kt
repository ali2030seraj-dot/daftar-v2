package com.daftar.v2.ui.annual

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
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
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
import com.daftar.v2.util.CurrencyUtils
import com.daftar.v2.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualReportScreen(
    navController: NavHostController,
    viewModel: AnnualReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("التقرير السنوي", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            copyReportAsText(context, state)
                            Toast.makeText(context, "تم نسخ التقرير", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ")
                        }
                        IconButton(onClick = { shareReportAsText(context, state) }) {
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
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

                    YearHeroCard(
                        year = state.year,
                        canGoNext = state.canGoNext,
                        onPrev = { viewModel.previousYear() },
                        onNext = { viewModel.nextYear() }
                    )

                    PersonsRow(
                        active = state.activePersonsCount,
                        archived = state.archivedPersonsCount
                    )

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
                                subtitle = "لم تُسجل أي عمليات في هذه السنة"
                            )
                        }
                    } else {
                        Text(
                            text = "التفصيل حسب العملة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TertiaryAmber,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                        state.currencies.forEach { stats ->
                            CurrencyYearCard(stats)
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              Hero Card — السنة
// ═══════════════════════════════════════════════════════════

@Composable
private fun YearHeroCard(
    year: Int,
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
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIos,
                    contentDescription = "السابق",
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Default
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "التقرير السنوي",
                    style = MaterialTheme.typography.bodySmall,
                    color = TertiaryAmber,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Default
                )
            }

            IconButton(onClick = onNext, enabled = canGoNext) {
                Icon(
                    imageVector = Icons.Filled.ArrowForwardIos,
                    contentDescription = "التالي",
                    tint = if (canGoNext) Color.White.copy(alpha = 0.9f)
                    else Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              بطاقة الأشخاص
// ═══════════════════════════════════════════════════════════

@Composable
private fun PersonsRow(active: Int, archived: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PersonStatCard(
            modifier = Modifier.weight(1f),
            emoji = "👥",
            label = "أشخاص نشطون",
            value = active,
            accent = SuccessGreen
        )
        PersonStatCard(
            modifier = Modifier.weight(1f),
            emoji = "📦",
            label = "مؤرشفون",
            value = archived,
            accent = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PersonStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: Int,
    accent: Color
) {
    GlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
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
//              بطاقة العملة السنوية
// ═══════════════════════════════════════════════════════════

@Composable
private fun CurrencyYearCard(stats: CurrencyYearStats) {
    val netColor = if (stats.netCents >= 0) SuccessGreen else DangerRed

    val highestMonth = stats.months.maxByOrNull { it.expensesCents }
    val hasAnyExpense = (highestMonth?.expensesCents ?: 0L) > 0L

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {

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
                        Text(text = "💱", fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stats.currencyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

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
                Column {
                    Text(
                        text = "صافي السنة",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Default
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = MoneyFormatter.format(stats.netCents, stats.currencyCode),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = netColor,
                        fontFamily = FontFamily.Default
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DetailCell(
                    modifier = Modifier.weight(1f),
                    label = "الديون",
                    valueCents = stats.totalDebtsCents,
                    currencyCode = stats.currencyCode,
                    color = DangerRed
                )
                DetailCell(
                    modifier = Modifier.weight(1f),
                    label = "السداد",
                    valueCents = stats.totalPaymentsCents,
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

            if (stats.months.any { it.debtsCents > 0 || it.paymentsCents > 0 || it.expensesCents > 0 }) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "التوزيع الشهري",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
                Spacer(Modifier.height(8.dp))

                MiniBarChart12Months(stats.months)

                if (hasAnyExpense && highestMonth != null) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TertiaryAmber.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "أعلى شهر",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TertiaryAmber,
                                fontFamily = FontFamily.Default
                            )
                            Text(
                                text = highestMonth.monthName + " - " +
                                        MoneyFormatter.formatShort(highestMonth.expensesCents),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              Mini Bar Chart - 12 شهر
// ═══════════════════════════════════════════════════════════

@Composable
private fun MiniBarChart12Months(months: List<MonthSummary>) {
    val maxValue = months.maxOfOrNull {
        maxOf(it.debtsCents, it.paymentsCents, it.expensesCents)
    } ?: 0L

    val barMaxHeight = 60.dp
    val barMinHeight = 3.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(barMaxHeight + 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        months.forEach { month ->
            val total = month.debtsCents + month.paymentsCents + month.expensesCents
            val ratio = if (maxValue > 0) total.toFloat() / maxValue.toFloat() else 0f
            val barHeight = (barMaxHeight * ratio).coerceAtLeast(barMinHeight)

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (total > 0) TertiaryAmber
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = month.monthName.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default,
                    maxLines = 1
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              DetailCell
// ═══════════════════════════════════════════════════════════

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
                text = CurrencyUtils.arabicShortName(currencyCode),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default,
                fontSize = 9.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              Helpers
// ═══════════════════════════════════════════════════════════

private fun buildReportText(state: AnnualReportState): String {
    return buildString {
        appendLine("التقرير السنوي - ${state.year}")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine()
        appendLine("إحصائيات:")
        appendLine("- أشخاص نشطون: ${state.activePersonsCount}")
        appendLine("- مؤرشفون: ${state.archivedPersonsCount}")
        appendLine()

        if (state.currencies.isEmpty()) {
            appendLine("لا توجد عمليات في هذه السنة.")
        } else {
            state.currencies.forEach { stats ->
                appendLine("${stats.currencyName}:")
                appendLine("- الديون: ${MoneyFormatter.format(stats.totalDebtsCents, stats.currencyCode)}")
                appendLine("- السداد: ${MoneyFormatter.format(stats.totalPaymentsCents, stats.currencyCode)}")
                appendLine("- المصروفات: ${MoneyFormatter.format(stats.totalExpensesCents, stats.currencyCode)}")
                appendLine("- الصافي: ${MoneyFormatter.format(stats.netCents, stats.currencyCode)}")
                appendLine()
            }
        }

        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("من تطبيق: دفتر حساباتي")
    }
}

private fun copyReportAsText(context: Context, state: AnnualReportState) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("التقرير السنوي", buildReportText(state))
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun shareReportAsText(context: Context, state: AnnualReportState) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, buildReportText(state))
            putExtra(Intent.EXTRA_SUBJECT, "التقرير السنوي - دفتر حساباتي")
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة التقرير"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}