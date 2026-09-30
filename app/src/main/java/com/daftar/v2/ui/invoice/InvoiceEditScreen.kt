package com.daftar.v2.ui.invoice

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.data.entity.InvoiceItemEntity
import com.daftar.v2.data.entity.PersonEntity
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
fun InvoiceEditScreen(
    navController: NavHostController,
    viewModel: InvoiceEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) {
            navController.popBackStack()
        }
    }

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (state.isNew) "📄 فاتورة جديدة" else "✏️ تعديل الفاتورة",
                            fontWeight = FontWeight.Bold
                        )
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
            if (state.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // ⭐ رقم الفاتورة
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    "رقم الفاتورة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    state.number,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Default
                                )
                            }
                        }
                    }

                    // ⭐ العميل — مع زر اختيار
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    "العميل",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))

                                if (state.personName.isNotBlank()) {
                                    Text(
                                        state.personName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "العملة: ${CurrencyUtils.arabicFullName(state.currency)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(10.dp))
                                }

                                OutlinedButton(
                                    onClick = { viewModel.openPersonPicker() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (state.personName.isBlank())
                                            "اختيار عميل"
                                        else
                                            "تغيير العميل"
                                    )
                                }
                            }
                        }
                    }

                    // ⭐ البنود
                    item {
                        Text(
                            "البنود",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    itemsIndexed(state.items) { index, item ->
                        InvoiceItemEditor(
                            item = item,
                            onChange = { viewModel.updateItem(index, it) },
                            onDelete = { viewModel.removeItem(index) }
                        )
                    }

                    item {
                        TextButton(
                            onClick = { viewModel.addItem() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("➕ إضافة بند", color = SuccessGreen)
                        }
                    }

                    // ⭐ الضريبة والخصم
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                OutlinedTextField(
                                    value = if (state.taxPercent == 0.0) ""
                                    else state.taxPercent.toString(),
                                    onValueChange = {
                                        val d = it.toDoubleOrNull() ?: 0.0
                                        viewModel.setTaxPercent(d)
                                    },
                                    label = { Text("الضريبة %") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    )
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = if (state.discountCents == 0L) ""
                                    else MoneyFormatter.formatShort(state.discountCents),
                                    onValueChange = {
                                        val cents = MoneyFormatter.parseToCents(it) ?: 0L
                                        viewModel.setDiscountCents(cents)
                                    },
                                    label = { Text("الخصم") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    )
                                )
                            }
                        }
                    }

                    // ⭐ الملاحظات
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.note,
                                onValueChange = { viewModel.setNote(it) },
                                label = { Text("ملاحظات") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ⭐ الإجماليات
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                TotalRow("المجموع", state.subtotalCents)
                                TotalRow("الخصم", -state.discountCents)
                                TotalRow("الضريبة", state.taxCents)
                                Spacer(Modifier.height(6.dp))
                                TotalRow(
                                    "الإجمالي",
                                    state.totalCents,
                                    color = SuccessGreen,
                                    bold = true
                                )
                            }
                        }
                    }

                    // ⭐ زر الحفظ
                    item {
                        Button(
                            onClick = { viewModel.save { } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💾 حفظ الفاتورة", fontWeight = FontWeight.Bold)
                        }
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

    // ⭐ نافذة اختيار العميل
    if (state.showPersonPicker) {
        AlertDialog(
            onDismissRequest = { viewModel.closePersonPicker() },
            title = { Text("اختيار عميل", fontWeight = FontWeight.Bold) },
            text = {
                if (state.allPeople.isEmpty()) {
                    Text(
                        "لا يوجد عملاء. أضف عميلاً أولاً من شاشة \"ما لك\" أو \"ما عليك\"."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.allPeople, key = { it.id }) { person ->
                            PersonPickerRow(
                                person = person,
                                isSelected = person.id == state.personId,
                                onClick = { viewModel.selectPerson(person) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closePersonPicker() }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
//              ⭐ صف اختيار العميل
// ═══════════════════════════════════════════════════════════

@Composable
private fun PersonPickerRow(
    person: PersonEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.take(1),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) SuccessGreen
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    person.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
                if (person.phone.isNotBlank()) {
                    Text(
                        person.phone,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Text(
                    "✓",
                    fontSize = 18.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              ⭐⭐⭐ محرر البند — كمية أعداد صحيحة
// ═══════════════════════════════════════════════════════════

@Composable
private fun InvoiceItemEditor(
    item: InvoiceItemEntity,
    onChange: (InvoiceItemEntity) -> Unit,
    onDelete: () -> Unit
) {
    // ⭐ حالة نصية للكمية (أعداد صحيحة فقط)
    var qtyText by remember(item.id) {
        mutableStateOf(
            if (item.quantity <= 0.0) ""
            else item.quantity.toInt().toString()
        )
    }

    // ⭐ حالة نصية للسعر
    var priceText by remember(item.id) {
        mutableStateOf(
            if (item.unitPriceCents == 0L) ""
            else MoneyFormatter.formatShort(item.unitPriceCents)
        )
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "بند",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ⭐ البيان
            OutlinedTextField(
                value = item.description,
                onValueChange = { onChange(item.copy(description = it)) },
                label = { Text("البيان") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                // ⭐⭐⭐ الكمية — أعداد صحيحة فقط (1، 2، 3...)
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { raw ->
                        val normalized = normalizeDigits(raw)
                        // ⭐ أرقام فقط — لا نقاط، لا فواصل، لا رموز
                        val filtered = normalized.filter { it.isDigit() }
                        qtyText = filtered

                        val q = filtered.toIntOrNull()?.toDouble() ?: 0.0

                        onChange(
                            item.copy(
                                quantity = q,
                                totalCents = (q * item.unitPriceCents).toLong()
                            )
                        )
                    },
                    label = { Text("الكمية") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                // ⭐⭐⭐ السعر
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { raw ->
                        val normalized = normalizeDigits(raw)
                        val filtered = normalized.filter {
                            it.isDigit() || it == '.' || it == ','
                        }
                        priceText = filtered

                        val cents = (filtered.replace(',', '.').toDoubleOrNull() ?: 0.0)
                            .let { (it * 100).toLong() }

                        onChange(
                            item.copy(
                                unitPriceCents = cents,
                                totalCents = (item.quantity * cents).toLong()
                            )
                        )
                    },
                    label = { Text("السعر") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "الإجمالي: ${MoneyFormatter.formatShort(item.totalCents)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TertiaryAmber
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              ⭐ صف الإجماليات
// ═══════════════════════════════════════════════════════════

@Composable
private fun TotalRow(
    label: String,
    cents: Long,
    color: Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = if (bold) 14.sp else 12.sp,
            fontWeight = if (bold) FontWeight.Black else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            MoneyFormatter.formatShort(cents),
            fontSize = if (bold) 16.sp else 13.sp,
            fontWeight = if (bold) FontWeight.Black else FontWeight.SemiBold,
            color = color
        )
    }
}

// ═══════════════════════════════════════════════════════════
//              ⭐ أدوات مساعدة
// ═══════════════════════════════════════════════════════════

/**
 * ⭐ يحوّل الأرقام العربية (٠١٢٣...) والفارسية إلى إنجليزية.
 */
private fun normalizeDigits(input: String): String {
    val arabic = "٠١٢٣٤٥٦٧٨٩"
    val persian = "۰۱۲۳۴۵۶۷۸۹"
    val sb = StringBuilder()
    for (c in input) {
        val arIndex = arabic.indexOf(c)
        val peIndex = persian.indexOf(c)
        when {
            arIndex >= 0 -> sb.append(arIndex)
            peIndex >= 0 -> sb.append(peIndex)
            else -> sb.append(c)
        }
    }
    return sb.toString()
}