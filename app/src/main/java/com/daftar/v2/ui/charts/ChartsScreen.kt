package com.daftar.v2.ui.charts

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.EmptyState
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.MoneyFormatter
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    navController: NavHostController,
    viewModel: ChartsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("الرسوم البيانية", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
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

                    // ===== 1. عمودي: آخر 6 أشهر =====
                    Text(
                        "مصروفات آخر 6 أشهر",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TertiaryAmber,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        BarChartSafe(bars = state.monthlyBars)
                    }

                    // ===== 2. دائري: الشهر الحالي =====
                    Text(
                        "توزيع ${state.currentMonthLabel}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TertiaryAmber,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )

                    if (state.categorySlices.isEmpty()) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState(
                                    emoji = "🥧",
                                    title = "لا توجد مصروفات",
                                    subtitle = "أضف مصروفات لعرض التوزيع"
                                )
                            }
                        }
                    } else {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {

                                Text(
                                    "الإجمالي: ${MoneyFormatter.format(state.totalCurrentMonthCents, "")}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = DangerRed
                                )

                                Spacer(Modifier.height(16.dp))

                                state.categorySlices.forEach { slice ->
                                    CategoryRow(
                                        slice = slice,
                                        totalCents = state.totalCurrentMonthCents
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }

        GlassToast(
            message = state.message,
            isError = state.isError,
            onDismiss = { viewModel.clearMessage() }
        )
    }
}

/**
 * رسم بياني عمودي بدون Canvas.
 */
@Composable
private fun BarChartSafe(bars: List<MonthBar>) {
    if (bars.isEmpty()) return

    val maxAmount = bars.maxOfOrNull { it.amountCents } ?: 0L
    val safeMax = max(maxAmount, 1L)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bars.forEach { bar ->
            val ratio = if (bar.amountCents > 0)
                (bar.amountCents.toFloat() / safeMax.toFloat()).coerceIn(0.08f, 1f)
            else 0f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // القيمة
                Text(
                    text = MoneyFormatter.formatShort(bar.amountCents),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))

                // العمود (بحساب ارتفاع يدوي)
                val barHeight = (150f * ratio).dp
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(if (bar.amountCents > 0) barHeight else 4.dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            color = if (bar.amountCents > 0)
                                TertiaryAmber
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                )

                Spacer(Modifier.height(6.dp))

                // اسم الشهر
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * صف تصنيف + شريط نسبة.
 */
@Composable
private fun CategoryRow(
    slice: CategorySlice,
    totalCents: Long
) {
    val color = Color(slice.colorArgb)

    val percent = if (totalCents > 0)
        ((slice.amountCents.toDouble() / totalCents.toDouble()) * 100).toInt()
    else 0

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                slice.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$percent%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(10.dp))
            Text(
                MoneyFormatter.formatShort(slice.amountCents),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(6.dp))

        // شريط النسبة
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}