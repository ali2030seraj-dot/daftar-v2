package com.daftar.v2.ui.currency

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.CurrencyUtils
import com.daftar.v2.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySummaryScreen(
    navController: NavHostController,
    viewModel: CurrencySummaryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "ملخص العملات",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default,
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
            when {
                state.loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "الرصيد حسب العملة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // ⭐⭐⭐ نعرض كل العملات — حتى الفارغة
                        state.summaries.forEach { summary ->
                            CurrencyCard(summary)
                        }

                        Spacer(Modifier.height(40.dp))
                    }
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

// ═══════════════════════════════════════════════════════════
//              بطاقة العملة (ثابتة الحجم)
// ═══════════════════════════════════════════════════════════

@Composable
private fun CurrencyCard(summary: CurrencySummary) {
    // ⭐ اللون يعتمد على الرصيد الفعلي
    val netColor = when {
        summary.netCents > 0 -> SuccessGreen
        summary.netCents < 0 -> DangerRed
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // ⭐ شفافية البطاقة الفارغة أقل قليلاً
    val isEmpty = !summary.hasData

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {

            // ─── رأس البطاقة ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = TertiaryAmber.copy(alpha = if (isEmpty) 0.08f else 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = CurrencyUtils.arabicSymbol(summary.currency),
                            fontSize = 12.sp,
                            color = if (isEmpty)
                                TertiaryAmber.copy(alpha = 0.6f)
                            else
                                TertiaryAmber,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = CurrencyUtils.arabicFullName(summary.currency),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        color = if (isEmpty)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${summary.peopleCount} شخص",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
            }

            Spacer(Modifier.height(14.dp))

            // ─── الصافي ───
            Text(
                text = "الصافي",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Default
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = MoneyFormatter.format(summary.netCents, ""),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = netColor,
                fontFamily = FontFamily.Default
            )

            Spacer(Modifier.height(14.dp))

            // ⭐ 3 صناديق: لك + عليك + مصروفات
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = if (isEmpty) 0.15f else 0.3f
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // لك
                Column {
                    Text(
                        text = "لك",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Default
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = MoneyFormatter.formatShort(summary.forYouCents),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEmpty)
                            SuccessGreen.copy(alpha = 0.5f)
                        else
                            SuccessGreen,
                        fontFamily = FontFamily.Default
                    )
                }
                // عليك
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "عليك",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Default
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = MoneyFormatter.formatShort(summary.onYouCents),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEmpty)
                            DangerRed.copy(alpha = 0.5f)
                        else
                            DangerRed,
                        fontFamily = FontFamily.Default
                    )
                }
                // مصروفات
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "مصروفات",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Default
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = MoneyFormatter.formatShort(summary.expensesCents),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEmpty)
                            TertiaryAmber.copy(alpha = 0.5f)
                        else
                            TertiaryAmber,
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    }
}