package com.daftar.v2.ui.templates

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.data.template.ExpenseTemplate
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.EmptyState
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.util.MoneyFormatter
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTemplatesScreen(
    navController: NavHostController,
    viewModel: ExpenseTemplatesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<ExpenseTemplate?>(null) }
    var deletingTemplate by remember { mutableStateOf<ExpenseTemplate?>(null) }

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("قوالب المصروفات", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "إضافة", tint = Color.White)
                }
            }
        ) { padding ->
            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else if (state.templates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        emoji = "📋",
                        title = "لا توجد قوالب",
                        subtitle = "أضف قالبًا للإيجار أو الكهرباء لتسريع الإدخال",
                        actionLabel = "إضافة قالب",
                        onAction = { showAddDialog = true }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(state.templates, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            onEdit = { editingTemplate = template },
                            onDelete = { deletingTemplate = template }
                        )
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

    if (showAddDialog) {
        TemplateDialog(
            categories = state.categories,
            existing = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amountCents, categoryId, note ->
                viewModel.addTemplate(
                    name = name,
                    amountCents = amountCents,
                    currency = "ريال يمني",
                    categoryId = categoryId,
                    note = note
                )
                showAddDialog = false
            }
        )
    }

    editingTemplate?.let { template ->
        TemplateDialog(
            categories = state.categories,
            existing = template,
            onDismiss = { editingTemplate = null },
            onConfirm = { name, amountCents, categoryId, note ->
                viewModel.updateTemplate(
                    template.copy(
                        name = name,
                        defaultAmountCents = amountCents,
                        categoryId = categoryId,
                        note = note
                    )
                )
                editingTemplate = null
            }
        )
    }

    deletingTemplate?.let { template ->
        AlertDialog(
            onDismissRequest = { deletingTemplate = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل تريد حذف القالب \"${template.name}\"؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTemplate(template.id)
                    deletingTemplate = null
                }) {
                    Text("حذف", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTemplate = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun TemplateCard(
    template: ExpenseTemplate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = SuccessGreen.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = template.name.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    MoneyFormatter.format(template.defaultAmountCents, template.currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuccessGreen,
                    fontWeight = FontWeight.SemiBold
                )
                if (template.note.isNotBlank()) {
                    Text(
                        template.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "خيارات",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("تعديل") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("حذف", color = DangerRed) },
                        leadingIcon = {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = DangerRed)
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateDialog(
    categories: List<com.daftar.v2.data.entity.ExpenseCategoryEntity>,
    existing: ExpenseTemplate?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amountCents: Long, categoryId: Long?, note: String) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var amount by remember {
        mutableStateOf(
            existing?.let { MoneyFormatter.formatShort(it.defaultAmountCents) } ?: ""
        )
    }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var categoryId by remember { mutableStateOf(existing?.categoryId) }
    var categoryMenuOpen by remember { mutableStateOf(false) }

    val selectedCategory = categories.firstOrNull { it.id == categoryId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "إضافة قالب" else "تعديل القالب") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم القالب") },
                    placeholder = { Text("مثال: إيجار") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("المبلغ الافتراضي") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // اختيار التصنيف (اختياري)
                Box {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "بدون تصنيف",
                        onValueChange = {},
                        label = { Text("التصنيف (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(top = 8.dp)
                            .background(Color.Transparent)
                            .clickable(enabled = true) { categoryMenuOpen = true }
                    )
                }
                DropdownMenu(
                    expanded = categoryMenuOpen,
                    onDismissRequest = { categoryMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("بدون تصنيف") },
                        onClick = {
                            categoryId = null
                            categoryMenuOpen = false
                        }
                    )
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                categoryId = cat.id
                                categoryMenuOpen = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cents = MoneyFormatter.parseToCents(amount) ?: 0L
                    if (name.isNotBlank() && cents > 0) {
                        onConfirm(name, cents, categoryId, note)
                    }
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}