package com.daftar.v2.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width       
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftar.v2.data.entity.RecurringInterval
import com.daftar.v2.util.MoneyFormatter

@Composable
fun AddDebtDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (
        statement: String,
        amountCents: Long,
        note: String,
        isRecurring: Boolean,
        recurringInterval: RecurringInterval
    ) -> Unit
) {
    var statement by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // ⭐ جديد — التكرار
    var isRecurring by remember { mutableStateOf(false) }
    var recurringInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }

    GlassModal(
        title = "تسجيل مبلغ - $currency",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = statement,
                onValueChange = { statement = it; error = null },
                label = { Text("البيان") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it; error = null },
                label = { Text("المبلغ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("ملاحظات - اختياري") },
                modifier = Modifier.fillMaxWidth()
            )

            // ⭐⭐⭐ خيار التكرار التلقائي
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it }
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "🔄 تكرار تلقائي",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isRecurring) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RecurringInterval.values().forEach { interval ->
                        val label = when (interval) {
                            RecurringInterval.WEEKLY -> "أسبوعي"
                            RecurringInterval.MONTHLY -> "شهري"
                            RecurringInterval.YEARLY -> "سنوي"
                        }
                        val isSelected = recurringInterval == interval
                        TextButton(
                            onClick = { recurringInterval = interval },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إلغاء")
                }
                Button(
                    onClick = {
                        if (statement.isBlank()) {
                            error = "البيان مطلوب"
                            return@Button
                        }
                        val cents = MoneyFormatter.parseToCents(amount)
                        if (cents == null || cents <= 0) {
                            error = "المبلغ غير صحيح"
                            return@Button
                        }
                        onConfirm(
                            statement,
                            cents,
                            note,
                            isRecurring,
                            recurringInterval
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("حفظ")
                }
            }
        }
    }
}