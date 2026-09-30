package com.daftar.v2.ui.hijri

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.HijriDateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HIJRI_DAYS_SHORT = listOf(
    "أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriCalendarScreen(navController: NavHostController) {
    val tokens = LocalAdaptiveTokens.current

    val now = remember { System.currentTimeMillis() }
    val todayHijri = remember { HijriDateUtils.fromMillis(now) }

    var displayYear by remember(todayHijri.year) { mutableStateOf(todayHijri.year) }
    var displayMonth by remember(todayHijri.month) { mutableStateOf(todayHijri.month) }

    AmbientBackground {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "التقويم الهجري",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = tokens.screenPaddingHorizontal,
                        vertical = tokens.screenPaddingVertical
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TodayCard(hijri = todayHijri, gregorianMillis = now)

                MonthNavigator(
                    monthName = HijriDateUtils.getMonthName(displayMonth),
                    year = displayYear,
                    onPrevious = {
                        if (displayMonth == 1) {
                            displayMonth = 12
                            displayYear -= 1
                        } else {
                            displayMonth -= 1
                        }
                    },
                    onNext = {
                        if (displayMonth == 12) {
                            displayMonth = 1
                            displayYear += 1
                        } else {
                            displayMonth += 1
                        }
                    }
                )

                CalendarGrid(
                    year = displayYear,
                    month = displayMonth,
                    todayHijri = todayHijri
                )

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TodayCard(
    hijri: HijriDateUtils.HijriDate,
    gregorianMillis: Long
) {
    val gregorianLabel = remember(gregorianMillis) {
        val sdf = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar", "SA"))
        sdf.format(Date(gregorianMillis))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F1F4A), Color(0xFF1E3A8A))
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Text(
                text = "التاريخ اليوم",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
                fontFamily = FontFamily.Default
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = hijri.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = gregorianLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryAmber,
                fontFamily = FontFamily.Default
            )
        }
    }
}

@Composable
private fun MonthNavigator(
    monthName: String,
    year: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "الشهر السابق",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$year هـ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
            }
            IconButton(onClick = onNext) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "الشهر التالي",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    todayHijri: HijriDateUtils.HijriDate
) {
    val daysInMonth = HijriDateUtils.getDaysInMonth(year, month)
    val firstDayOfWeek = HijriDateUtils.getFirstDayOfWeek(year, month)

    val cells = remember(year, month) {
        val list = mutableListOf<Int?>()
        repeat(firstDayOfWeek) { list.add(null) }
        for (d in 1..daysInMonth) list.add(d)
        while (list.size % 7 != 0) list.add(null)
        list
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HIJRI_DAYS_SHORT.forEach { dayName ->
                    Text(
                        text = dayName.take(3),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(((cells.size / 7) * 52).dp),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(cells) { day ->
                    DayCell(
                        day = day,
                        isToday = day != null
                                && day == todayHijri.day
                                && month == todayHijri.month
                                && year == todayHijri.year
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int?, isToday: Boolean) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isToday -> MaterialTheme.colorScheme.primary
                    day == null -> Color.Transparent
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isToday) Color.White
                else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Default,
                fontSize = 13.sp
            )
        }
    }
}