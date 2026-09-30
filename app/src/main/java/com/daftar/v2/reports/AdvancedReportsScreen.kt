package com.daftar.v2.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.CurrencyUtils
import com.daftar.v2.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedReportsScreen(
    navController: NavHostController,
    viewModel: AdvancedReportsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text("التقارير المتقدمة", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
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
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.error!!, color = DangerRed)
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

                    HeroCard()

                    state.aging?.let { aging ->
                        AgingSection(aging)
                    }

                    state.cashFlow?.let { cashFlow ->
                        CashFlowSection(cashFlow)
                    }

                    state.comparison?.let { comparison ->
                        ComparisonSection(comparison)
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F1F4A), Color(0xFF1E3A8A))
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            Text(
                text = "📊 التقارير المتقدمة",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontFamily = FontFamily.Default
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "تحليل عميق لأموالك وديونك",
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryAmber,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Default
            )
        }
    }
}

@Composable
private fun AgingSection(report: AgingReport) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TertiaryAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏳", fontSize = 18.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "أعمار الديون",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "كم مضى على كل دين بدون سداد",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default
            )

            Spacer(Modifier.height(14.dp))

            report.buckets.forEach { bucket ->
                AgingRow(bucket)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الإجمالي",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                    Text(
                        text = MoneyFormatter.format(report.totalCents, report.currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TertiaryAmber,
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    }
}

@Composable
private fun AgingRow(bucket: AgingBucket) {
    val color = Color(bucket.color)

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
                text = bucket.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Default
            )
            Text(
                text = "${bucket.count} عميل",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default
            )
        }

        Text(
            text = MoneyFormatter.formatShort(bucket.totalCents),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
private fun CashFlowSection(report: CashFlowReport) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💵", fontSize = 18.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "التدفق النقدي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "آخر 6 أشهر — الدخل مقابل المصروفات",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CashFlowSummaryCell(
                    modifier = Modifier.weight(1f),
                    label = "الدخل",
                    valueCents = report.totalIncomeCents,
                    currencyCode = report.currency,
                    color = SuccessGreen
                )
                CashFlowSummaryCell(
                    modifier = Modifier.weight(1f),
                    label = "المصروفات",
                    valueCents = report.totalExpenseCents,
                    currencyCode = report.currency,
                    color = DangerRed
                )
                CashFlowSummaryCell(
                    modifier = Modifier.weight(1f),
                    label = "الصافي",
                    valueCents = report.netCents,
                    currencyCode = report.currency,
                    color = if (report.netCents >= 0) SuccessGreen else DangerRed
                )
            }

            Spacer(Modifier.height(16.dp))

            report.months.reversed().forEach { month ->
                CashFlowRow(month, report.currency)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun CashFlowRow(month: CashFlowMonth, currency: String) {
    val netColor = if (month.netCents >= 0) SuccessGreen else DangerRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = month.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
                Text(
                    text = MoneyFormatter.formatShort(month.netCents),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = netColor,
                    fontFamily = FontFamily.Default
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⬆ ${MoneyFormatter.formatShort(month.incomeCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
                    fontFamily = FontFamily.Default
                )
                Text(
                    text = "⬇ ${MoneyFormatter.formatShort(month.expenseCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = DangerRed,
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

@Composable
private fun CashFlowSummaryCell(
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

@Composable
private fun ComparisonSection(report: ComparisonReport) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📈", fontSize = 18.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "المقارنة الشهرية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "${report.previousMonth.label} ← ${report.currentMonth.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default
            )

            Spacer(Modifier.height(14.dp))

            ComparisonRow(
                label = "الدخل",
                currentCents = report.currentMonth.incomeCents,
                previousCents = report.previousMonth.incomeCents,
                changePercent = report.incomeChangePercent
            )
            Spacer(Modifier.height(8.dp))

            ComparisonRow(
                label = "المصروفات",
                currentCents = report.currentMonth.expenseCents,
                previousCents = report.previousMonth.expenseCents,
                changePercent = report.expenseChangePercent,
                invertColors = true
            )
            Spacer(Modifier.height(8.dp))

            ComparisonRow(
                label = "الصافي",
                currentCents = report.currentMonth.netCents,
                previousCents = report.previousMonth.netCents,
                changePercent = report.netChangePercent
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    currentCents: Long,
    previousCents: Long,
    changePercent: Double,
    invertColors: Boolean = false
) {
    val isPositive = changePercent >= 0
    val color = if (invertColors) {
        if (isPositive) DangerRed else SuccessGreen
    } else {
        if (isPositive) SuccessGreen else DangerRed
    }

    val arrow = if (isPositive) "▲" else "▼"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = MoneyFormatter.formatShort(currentCents),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Default
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = arrow,
                    fontSize = 14.sp,
                    color = color,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${"%.1f".format(kotlin.math.abs(changePercent))}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = color,
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}