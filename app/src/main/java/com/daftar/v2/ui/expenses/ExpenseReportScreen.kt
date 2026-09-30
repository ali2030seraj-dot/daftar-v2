package com.daftar.v2.ui.expenses

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.InfoCyan
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.DateFormatter
import com.daftar.v2.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseReportScreen(
    navController: NavHostController,
    viewModel: ExpenseReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("تقرير المصروفات", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            copyToClipboard(context, buildReportText(state), "تقرير المصروفات")
                            Toast.makeText(context, "✅ تم النسخ", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ")
                        }
                        IconButton(onClick = {
                            shareText(context, buildReportText(state))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "مشاركة")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ===== اختيار الشهر =====
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                state.monthLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "إجمالي: ${MoneyFormatter.format(state.totalCents, "")}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TertiaryAmber
                            )
                        }
                    }

                    // ===== أزرار التنقل =====
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.previousMonth() }
                        ) {
                            Text(
                                "◀ السابق",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            onClick = { if (state.canGoNext) viewModel.nextMonth() }
                        ) {
                            Text(
                                "التالي ▶",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.canGoNext)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ===== إحصائيات عامة =====
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatBox(
                            modifier = Modifier.weight(1f),
                            emoji = "🧾",
                            label = "عدد المصروفات",
                            value = state.expenseCount.toString(),
                            color = InfoCyan
                        )
                        StatBox(
                            modifier = Modifier.weight(1f),
                            emoji = "📊",
                            label = "المتوسط",
                            value = MoneyFormatter.formatShort(state.averageCents),
                            color = TertiaryAmber
                        )
                    }

                    // ===== المقارنة =====
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "مقارنة بالشهر السابق",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                when {
                                    state.changePercent > 0 -> "▲ ${state.changePercent}%"
                                    state.changePercent < 0 -> "▼ ${-state.changePercent}%"
                                    else -> "—"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    state.changePercent > 0 -> DangerRed
                                    state.changePercent < 0 -> SuccessGreen
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // ===== تفصيل حسب التصنيف =====
                    if (state.byCategory.isNotEmpty()) {
                        SectionTitle("حسب التصنيف")
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                // شريط نسب
                                CategoryStackedBar(state.byCategory)

                                Spacer(Modifier.height(14.dp))

                                state.byCategory.forEachIndexed { index, cat ->
                                    CategoryRow(
                                        cat = cat,
                                        color = categoryColor(index)
                                    )
                                    if (index < state.byCategory.size - 1) {
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    // ===== آخر 6 أشهر =====
                    if (state.monthly.any { it.totalCents > 0 }) {
                        SectionTitle("آخر 6 أشهر")
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            SixMonthChart(state.monthly)
                        }
                    }

                    // ===== أعلى 5 مصروفات =====
                    if (state.topExpenses.isNotEmpty()) {
                        SectionTitle("أعلى 5 مصروفات")
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                state.topExpenses.forEachIndexed { index, expense ->
                                    TopExpenseRow(
                                        rank = index + 1,
                                        expense = expense
                                    )
                                    if (index < state.topExpenses.size - 1) {
                                        Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//                       Composables
// ═══════════════════════════════════════════════════════════

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TertiaryAmber,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String,
    color: Color
) {
    GlassCard(modifier = modifier) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun CategoryStackedBar(categories: List<CategoryStat>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
    ) {
        categories.forEachIndexed { index, cat ->
            if (cat.percent > 0f) {
                Box(
                    modifier = Modifier
                        .weight(cat.percent.coerceAtLeast(0.01f))
                        .fillMaxSize()
                        .background(categoryColor(index))
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(cat: CategoryStat, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cat.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${cat.count} عملية • ${(cat.percent * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            MoneyFormatter.formatShort(cat.totalCents),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun SixMonthChart(months: List<MonthlyPoint>) {
    val max = months.maxOfOrNull { it.totalCents } ?: 0L
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        months.forEach { point ->
            val ratio = if (max == 0L) 0f
            else point.totalCents.toFloat() / max.toFloat()

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    MoneyFormatter.formatShort(point.totalCents),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height((70 * ratio).coerceAtLeast(4f).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    TertiaryAmber,
                                    TertiaryAmber.copy(alpha = 0.55f)
                                )
                            )
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    point.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopExpenseRow(rank: Int, expense: TopExpense) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when (rank) {
                        1 -> TertiaryAmber
                        2 -> Color(0xFFB0BEC5)
                        3 -> Color(0xFFBF8970)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                rank.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                expense.statement,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                buildString {
                    expense.categoryName?.let { append(it).append(" • ") }
                    append(DateFormatter.fullLabel(expense.timestamp))
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            MoneyFormatter.formatShort(expense.amountCents),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = DangerRed
        )
    }
}

// ═══════════════════════════════════════════════════════════
//                       Helpers
// ═══════════════════════════════════════════════════════════

private val CATEGORY_PALETTE = listOf(
    Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFFF59E0B),
    Color(0xFF16A34A), Color(0xFFDC2626), Color(0xFF0891B2),
    Color(0xFFEC4899), Color(0xFF84CC16), Color(0xFFF97316),
    Color(0xFF64748B)
)

private fun categoryColor(index: Int): Color =
    CATEGORY_PALETTE[index % CATEGORY_PALETTE.size]

private fun buildReportText(state: ExpenseReportState): String {
    return buildString {
        appendLine("🧾 تقرير المصروفات — ${state.monthLabel}")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine()

        appendLine("📌 الملخص:")
        appendLine("• الإجمالي: ${MoneyFormatter.format(state.totalCents, "")}")
        appendLine("• عدد المصروفات: ${state.expenseCount}")
        appendLine("• المتوسط: ${MoneyFormatter.format(state.averageCents, "")}")
        appendLine(
            "• مقارنة بالسابق: " + when {
                state.changePercent > 0 -> "▲ ${state.changePercent}%"
                state.changePercent < 0 -> "▼ ${-state.changePercent}%"
                else -> "— نفس الشهر"
            }
        )
        appendLine()

        if (state.byCategory.isNotEmpty()) {
            appendLine("📊 حسب التصنيف:")
            state.byCategory.forEach { cat ->
                appendLine(
                    "• ${cat.name}: ${MoneyFormatter.formatShort(cat.totalCents)} " +
                            "(${cat.count} عملية • ${(cat.percent * 100).toInt()}%)"
                )
            }
            appendLine()
        }

        if (state.topExpenses.isNotEmpty()) {
            appendLine("🔥 أعلى 5 مصروفات:")
            state.topExpenses.forEachIndexed { i, e ->
                appendLine("${i + 1}. ${e.statement}: ${MoneyFormatter.formatShort(e.amountCents)}")
            }
            appendLine()
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

private fun shareText(context: Context, text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "تقرير المصروفات - دفتر حساباتي")
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة التقرير"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}