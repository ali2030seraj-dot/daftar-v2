package com.daftar.v2.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daftar.v2.data.entity.ExpenseEntity
import com.daftar.v2.util.CurrencyUtils
import com.daftar.v2.util.MoneyFormatter

/**
 * نافذة إضافة/تعديل مصروف.
 *
 * @param existing إذا كان null → وضع "إضافة"، وإلا → وضع "تعديل".
 */
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (statement: String, amountCents: Long, currency: String, note: String) -> Unit,
    existing: ExpenseEntity? = null
) {
    val isEditMode = existing != null

    var statement by remember {
        mutableStateOf(existing?.statement ?: "")
    }
    var amount by remember {
        mutableStateOf(
            existing?.let { MoneyFormatter.formatShort(it.amountCents) } ?: ""
        )
    }
    // ⭐ التخزين دائماً بالكود (YER, SAR, ...)
    var currencyCode by remember {
        mutableStateOf(
            existing?.let { CurrencyUtils.toCode(it.currency) }
                ?: CurrencyUtils.DEFAULT_CODE
        )
    }
    var note by remember {
        mutableStateOf(existing?.note ?: "")
    }
    var error by remember { mutableStateOf<String?>(null) }

    GlassModal(
        title = if (isEditMode) "تعديل مصروف" else "تسجيل مصروف",
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

            Text(
                "العملة",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            // ⭐ الصف الأول — أول 3 عملات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CurrencyUtils.ALL_CODES.take(3).forEach { code ->
                    val isSelected = code == currencyCode
                    TextButton(
                        onClick = {
                            currencyCode = code
                            error = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = CurrencyUtils.arabicShortName(code),
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // ⭐ الصف الثاني — آخر عملتين + Spacer لموازنة العرض
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CurrencyUtils.ALL_CODES.drop(3).forEach { code ->
                    val isSelected = code == currencyCode
                    TextButton(
                        onClick = {
                            currencyCode = code
                            error = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = CurrencyUtils.arabicShortName(code),
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                // ⭐ Spacer لموازنة عرض الصف الثاني مع الأول (3 خلايا)
                Spacer(Modifier.weight(1f))
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("ملاحظات - اختياري") },
                modifier = Modifier.fillMaxWidth()
            )
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
                        // ⭐ نمرّر الكود (وليس الاسم العربي)
                        onConfirm(statement, cents, currencyCode, note)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEditMode) "حفظ التعديل" else "حفظ")
                }
            }
        }
    }
}