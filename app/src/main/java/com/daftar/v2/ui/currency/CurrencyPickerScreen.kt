package com.daftar.v2.ui.currency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ListAlt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.navigation.Destinations
import com.daftar.v2.ui.theme.GoldAccent
import com.daftar.v2.ui.theme.GoldDark
import com.daftar.v2.util.CurrencyUtils

// ═══════════════════════════════════════════════════════════
//              بيانات العملات (مع الأعلام والرموز)
// ═══════════════════════════════════════════════════════════

private data class CurrencyItem(
    val code: String,
    val flag: String,
    val nameAr: String,
    val shortAr: String,
    val color: Color
)

private val currenciesList = listOf(
    CurrencyItem("YER", "🇾🇪", "ريال يمني", "يمني", Color(0xFFC2414B)),
    CurrencyItem("SAR", "🇸🇦", "ريال سعودي", "سعودي", Color(0xFF16805C)),
    CurrencyItem("OMR", "🇴🇲", "ريال عماني", "عماني", Color(0xFF7C3AED)),
    CurrencyItem("AED", "🇦🇪", "درهم إماراتي", "إماراتي", Color(0xFF0891B2)),
    CurrencyItem("USD", "🇺🇸", "دولار أمريكي", "دولار", Color(0xFF2563EB)),
    CurrencyItem("EGP", "🇪🇬", "جنيه مصري", "مصري", Color(0xFFD97706))
)

// ═══════════════════════════════════════════════════════════
//              الشاشة
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerScreen(
    navController: NavHostController,
    viewModel: CurrencyPickerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💱", fontSize = 18.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "تغيير العملة",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ⭐ البطاقة الحالية
                CurrentCurrencyCard(currentCode = state.currentCurrency)

                // ⭐ قائمة العملات
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(currenciesList, key = { it.code }) { currency ->
                        CurrencyListItem(
                            currency = currency,
                            isSelected = currency.code == state.currentCurrency,
                            onClick = {
                                viewModel.selectCurrency(currency.code)
                            }
                        )
                    }

                    // ⭐ زر ملخص العملات
                    item {
                        Spacer(Modifier.height(8.dp))
                        SummaryButton(
                            onClick = {
                                navController.navigate(Destinations.CURRENCY_SUMMARY)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              بطاقة العملة الحالية
// ═══════════════════════════════════════════════════════════

@Composable
private fun CurrentCurrencyCard(currentCode: String) {
    val current = currenciesList.firstOrNull { it.code == currentCode }
        ?: currenciesList.first()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GoldAccent.copy(alpha = 0.12f))
            .border(
                width = 1.5.dp,
                color = GoldAccent.copy(alpha = 0.4f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = current.flag, fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "العملة الحالية",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = current.nameAr,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = current.code,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GoldAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              عنصر عملة في القائمة
// ═══════════════════════════════════════════════════════════

@Composable
private fun CurrencyListItem(
    currency: CurrencyItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ⭐ العلم
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(currency.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = currency.flag, fontSize = 24.sp)
            }

            Spacer(Modifier.width(14.dp))

            // ⭐ الاسم
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currency.nameAr,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${currency.shortAr} • ${currency.code}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ⭐ علامة الاختيار أو سهم
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GoldAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "مختار",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              زر ملخص العملات
// ═══════════════════════════════════════════════════════════

@Composable
private fun SummaryButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ListAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ملخص كل العملات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "عرض رصيدك حسب كل عملة",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}