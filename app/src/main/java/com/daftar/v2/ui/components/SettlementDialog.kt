package com.daftar.v2.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.util.MoneyFormatter

/**
 * نافذة تسوية الحساب.
 *
 * @param balanceCents الرصيد المتبقي (موجب أو سالب).
 * @param currency عملة الحساب.
 * @param onDismiss إغلاق.
 * @param onConfirm (المبلغ المعبّأ، أرشفة بعد التسوية؟).
 */
@Composable
fun SettlementDialog(
    balanceCents: Long,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (settlementCents: Long, archiveAfter: Boolean) -> Unit
) {
    // الرصيد بالـ abs لأنه قد يكون سالبًا (رصيد دائن)
    val absoluteBalance = kotlin.math.abs(balanceCents)

    var amount by remember {
        mutableStateOf(MoneyFormatter.formatShort(absoluteBalance))
    }
    var archiveAfter by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val parsedCents = MoneyFormatter.parseToCents(amount) ?: 0L
    val isFullSettlement = parsedCents == absoluteBalance

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسوية الحساب", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // عرض الرصيد الحالي
                Column {
                    Text(
                        "الرصيد الحالي:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        MoneyFormatter.format(absoluteBalance, currency),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // زر التسوية الكاملة
                OutlinedButton(
                    onClick = {
                        amount = MoneyFormatter.formatShort(absoluteBalance)
                        showError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = parsedCents != absoluteBalance
                ) {
                    Text(
                        if (parsedCents == absoluteBalance)
                            "✓ التسوية الكاملة محددة"
                        else
                            "تسوية كاملة (${MoneyFormatter.formatShort(absoluteBalance)})"
                    )
                }

                // حقل المبلغ
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        showError = false
                    },
                    label = { Text("مبلغ التسوية") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text("أدخل مبلغًا صحيحًا أكبر من صفر")
                        } else if (isFullSettlement) {
                            Text(
                                "الرصيد سيصبح صفرًا ✓",
                                color = SuccessGreen
                            )
                        } else {
                            val remaining = absoluteBalance - parsedCents
                            if (remaining > 0) {
                                Text("سيتبقى: ${MoneyFormatter.formatShort(remaining)} $currency")
                            } else {
                                Text("المبلغ يتجاوز الرصيد")
                            }
                        }
                    }
                )

                // خيار الأرشفة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = archiveAfter,
                        onCheckedChange = { archiveAfter = it }
                    )
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            "أرشفة الحساب بعد التسوية",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "سيُخفى من القائمة الرئيسية ويمكن استرجاعه من الأرشيف",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (parsedCents <= 0) {
                        showError = true
                    } else {
                        onConfirm(parsedCents, archiveAfter)
                    }
                }
            ) {
                Text("تأكيد التسوية", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}