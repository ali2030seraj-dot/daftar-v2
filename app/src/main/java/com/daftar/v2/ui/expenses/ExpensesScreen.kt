package com.daftar.v2.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.data.entity.ExpenseEntity
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.components.AddExpenseDialog
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.EmptyState
import com.daftar.v2.ui.components.FabAction
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassFab
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.components.SkeletonBox
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.DateFormatter
import com.daftar.v2.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    navController: NavHostController,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tokens = LocalAdaptiveTokens.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    AmbientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {

                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "المصروفات",
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Default,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "مصروفاتك المسجلة",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Default,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = tokens.screenPaddingHorizontal,
                            vertical = 8.dp
                        )
                ) {
                    when {
                        state.loading -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SkeletonBox(height = 70.dp)
                                SkeletonBox(height = 70.dp)
                                SkeletonBox(height = 70.dp)
                            }
                        }
                        state.expenses.isEmpty() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                EmptyState(
                                    emoji = "🧾",
                                    title = "لا توجد مصروفات",
                                    subtitle = "ابدأ بتسجيل أول مصروف",
                                    actionLabel = "إضافة مصروف",
                                    onAction = { showAddDialog = true }
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 120.dp)
                            ) {
                                items(state.expenses, key = { it.id }) { expense ->
                                    ExpenseRow(
                                        expense = expense,
                                        onEdit = { editingExpense = expense },
                                        onDelete = { viewModel.delete(expense.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ⭐⭐⭐ الإصلاح: إزالة "تقرير المصروفات" من FAB
            GlassFab(
                actions = listOf(
                    FabAction("إضافة مصروف", "🧾", { showAddDialog = true })
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }

        GlassToast(
            message = state.message,
            isError = state.isError,
            onDismiss = { viewModel.clearMessage() }
        )
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { statement, cents, currency, note ->
                viewModel.add(statement, cents, currency, note)
                showAddDialog = false
            }
        )
    }

    editingExpense?.let { expense ->
        AddExpenseDialog(
            existing = expense,
            onDismiss = { editingExpense = null },
            onConfirm = { statement, cents, currency, note ->
                viewModel.update(expense, statement, cents, currency, note)
                editingExpense = null
            }
        )
    }
}

@Composable
private fun ExpenseRow(
    expense: ExpenseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        TertiaryAmber.copy(alpha = 0.15f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🧾", fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = expense.statement,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateFormatter.fullLabel(expense.timestamp),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = MoneyFormatter.format(expense.amountCents, expense.currency),
                fontSize = 14.sp,
                color = DangerRed,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Default
            )

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "تعديل",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}