package com.daftar.v2.ui.license

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daftar.v2.security.license.LicenseStatus
import com.daftar.v2.ui.theme.DangerRed

/**
 * شريط تنبيه يظهر أعلى الشاشة عند قرب انتهاء الترخيص.
 */
@Composable
fun SubscriptionBanner(
    status: LicenseStatus?,
    onActivateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = status is LicenseStatus.ExpiringSoon ||
            status is LicenseStatus.GracePeriod

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        val (text, color) = when (status) {
            is LicenseStatus.ExpiringSoon ->
                "تنبيه: ترخيصك ينتهي خلال ${status.daysRemaining} يومًا" to
                        MaterialTheme.colorScheme.tertiary

            is LicenseStatus.GracePeriod ->
                "انتهى ترخيصك — أنت في فترة السماح (${status.daysRemaining} يومًا)" to
                        DangerRed

            else -> "" to Color.Transparent
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
            TextButton(onClick = onActivateClick) {
                Text("تجديد", color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}