package com.daftar.v2.ui.zakat

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatScreen(
    navController: NavHostController,
    viewModel: ZakatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text("حاسبة الزكاة", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.reset() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "إعادة تعيين")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ===== النتيجة =====
                ResultCard(
                    netWealthCents = state.netWealthCents,
                    nisabCents = state.nisabCents,
                    zakatCents = state.zakatCents,
                    isEligible = state.isEligible,
                    currency = state.currency
                )

                // ===== الأصول =====
                SectionTitle("أصولك")

                // النقد
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    AmountField(
                        label = "النقد والبنوك",
                        value = state.cashInput,
                        onValueChange = viewModel::setCash,
                        currency = state.currency
                    )
                }

                // الذهب
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "الذهب",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TertiaryAmber
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AmountField(
                                label = "الوزن (جرام)",
                                value = state.goldGramsInput,
                                onValueChange = viewModel::setGoldGrams,
                                modifier = Modifier.weight(1f)
                            )
                            AmountField(
                                label = "سعر الجرام",
                                value = state.goldPriceInput,
                                onValueChange = viewModel::setGoldPrice,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // الفضة
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "الفضة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AmountField(
                                label = "الوزن (جرام)",
                                value = state.silverGramsInput,
                                onValueChange = viewModel::setSilverGrams,
                                modifier = Modifier.weight(1f)
                            )
                            AmountField(
                                label = "سعر الجرام",
                                value = state.silverPriceInput,
                                onValueChange = viewModel::setSilverPrice,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ديون لك عند الناس
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    AmountField(
                        label = "ديون لك عند الناس (قابلة للتحصيل)",
                        value = state.debtsOwedToYouInput,
                        onValueChange = viewModel::setDebtsOwedToYou,
                        currency = state.currency
                    )
                }

                // ===== الخصوم =====
                SectionTitle("الديون عليك")

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    AmountField(
                        label = "الديون التي عليك (تُخصم)",
                        value = state.debtsYouOweInput,
                        onValueChange = viewModel::setDebtsYouOwe,
                        currency = state.currency
                    )
                }

                // ===== شرح النصاب =====
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "النصاب",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TertiaryAmber
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "• نصاب الذهب: 85 جرام",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "• نصاب الفضة: 595 جرام",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "• مقدار الزكاة: 2.5%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "إذا بلغ مالك النصاب وحال عليه الحول (سنة هجرية)، وجبت الزكاة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TertiaryAmber
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ResultCard(
    netWealthCents: Long,
    nisabCents: Long,
    zakatCents: Long,
    isEligible: Boolean,
    currency: String
) {
    val accent = if (isEligible) SuccessGreen else TertiaryAmber

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isEligible) "وجبت عليك الزكاة" else "لم تبلغ النصاب",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )

            Spacer(Modifier.height(14.dp))

            Text(
                "صافي الثروة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                MoneyFormatter.format(netWealthCents, currency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "الزكاة الواجبة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        MoneyFormatter.format(zakatCents, currency),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isEligible) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (nisabCents > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "النصاب الحالي: ${MoneyFormatter.format(nisabCents, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TertiaryAmber,
        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
    )
}

@Composable
private fun AmountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    currency: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = currency?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } }
        )
    }
}