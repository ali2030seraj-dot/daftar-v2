package com.daftar.v2.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.adaptive.WindowSize
import com.daftar.v2.ui.theme.AppIcons
import com.daftar.v2.ui.theme.GlassTokens

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun FloatingBottomNav(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAdaptiveTokens.current

    val items = remember {
        listOf(
            NavItem(Destinations.DASHBOARD, "الرئيسية", Icons.Rounded.Home, AppIcons.Colors.info),
            NavItem(Destinations.ABOUT, "حول التطبيق", Icons.Rounded.Info, AppIcons.Colors.cyan),
            NavItem(Destinations.GUIDE, "دليل الاستخدام", Icons.Rounded.MenuBook, AppIcons.Colors.purple),
            NavItem(Destinations.CURRENCY_PICKER, "تغيير العملات", Icons.Rounded.CurrencyExchange, AppIcons.Colors.warning),
            NavItem(Destinations.MORE, "المزيد", Icons.Rounded.Settings, AppIcons.Colors.neutral)
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val horizontalPadding: Dp = when (tokens.windowSize) {
        WindowSize.COMPACT -> 12.dp
        WindowSize.MEDIUM -> 16.dp
        WindowSize.EXPANDED -> 24.dp
    }

    val rawMaxWidth = tokens.bottomNavMaxWidth
    val safeMaxWidth: Dp = if (
        rawMaxWidth != Dp.Unspecified &&
        !rawMaxWidth.value.isNaN() &&
        rawMaxWidth.value > 0f
    ) rawMaxWidth else 560.dp

    val safeElevation: Dp = if (
        tokens.floatingElevation != Dp.Unspecified &&
        !tokens.floatingElevation.value.isNaN() &&
        tokens.floatingElevation.value > 0f
    ) (tokens.floatingElevation * 0.8f) else 10.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = horizontalPadding, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = safeMaxWidth)
                .fillMaxWidth()
                .shadow(
                    elevation = safeElevation,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.20f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(vertical = 6.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.05f else 1f,
                    animationSpec = tween(GlassTokens.AnimationFast),
                    label = "navScale"
                )
                val tint by animateColorAsState(
                    targetValue = if (selected) item.color
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(GlassTokens.AnimationFast),
                    label = "navTint"
                )
                val boxBg by animateColorAsState(
                    targetValue = if (selected) item.color.copy(alpha = 0.15f)
                    else Color.Transparent,
                    animationSpec = tween(GlassTokens.AnimationFast),
                    label = "navBg"
                )

                val iconBoxSize: Dp = when (tokens.windowSize) {
                    WindowSize.COMPACT -> 34.dp
                    WindowSize.MEDIUM -> 36.dp
                    WindowSize.EXPANDED -> 40.dp
                }

                val actualIconSize: Dp = when (tokens.windowSize) {
                    WindowSize.COMPACT -> 20.dp
                    WindowSize.MEDIUM -> 22.dp
                    WindowSize.EXPANDED -> 24.dp
                }

                val labelFontSize = when (tokens.windowSize) {
                    WindowSize.COMPACT -> 10.sp
                    WindowSize.MEDIUM -> 11.sp
                    WindowSize.EXPANDED -> 12.sp
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentRoute == item.route) return@clickable

                            val startDestinationId =
                                navController.graph.findStartDestination().id

                            if (item.route == Destinations.DASHBOARD) {
                                navController.navigate(Destinations.DASHBOARD) {
                                    popUpTo(startDestinationId) { inclusive = false }
                                    launchSingleTop = true
                                }
                                return@clickable
                            }

                            navController.navigate(item.route) {
                                popUpTo(startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(iconBoxSize)
                            .clip(RoundedCornerShape(10.dp))
                            .background(boxBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(actualIconSize)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        fontFamily = FontFamily.Default,
                        fontSize = labelFontSize,
                        color = tint,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}