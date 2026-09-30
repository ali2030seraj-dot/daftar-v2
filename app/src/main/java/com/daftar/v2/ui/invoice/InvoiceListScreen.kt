package com.daftar.v2.ui.invoice

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.data.entity.InvoiceEntity
import com.daftar.v2.data.entity.InvoiceStatus
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.EmptyState
import com.daftar.v2.ui.components.FabAction
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassFab
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.navigation.Destinations
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    navController: NavHostController,
    viewModel: InvoiceListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var deleting by remember { mutableStateOf<InvoiceEntity?>(null) }

    AmbientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text("📄 الفواتير", fontWeight = FontWeight.Bold)
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
                if (state.invoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            emoji = "📄",
                            title = "لا توجد فواتير",
                            subtitle = "ابدأ بإنشاء أول فاتورة",
                            actionLabel = "إنشاء فاتورة",
                            onAction = {
                                // ⭐ تصحيح: تمرير 0L كـ ID
                                navController.navigate(Destinations.invoiceEdit(0L))
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(state.invoices, key = { it.id }) { invoice ->
                            InvoiceCard(
                                invoice = invoice,
                                onClick = {
                                    navController.navigate(
                                        Destinations.invoiceView(invoice.id)
                                    )
                                },
                                onDelete = { deleting = invoice }
                            )
                        }
                    }
                }
            }

            GlassFab(
                actions = listOf(
                    FabAction("فاتورة جديدة", "➕", {
                        // ⭐ تصحيح: تمرير 0L كـ ID
                        navController.navigate(Destinations.invoiceEdit(0L))
                    })
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

    deleting?.let { invoice ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("حذف الفاتورة؟") },
            text = { Text("سيتم حذف الفاتورة ${invoice.number} نهائياً.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(invoice.id)
                    deleting = null
                }) {
                    Text("حذف", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun InvoiceCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (invoice.status) {
        InvoiceStatus.PAID -> SuccessGreen
        InvoiceStatus.ISSUED -> TertiaryAmber
        InvoiceStatus.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
        InvoiceStatus.CANCELLED -> DangerRed
    }

    val statusLabel = when (invoice.status) {
        InvoiceStatus.PAID -> "مدفوعة"
        InvoiceStatus.ISSUED -> "صادرة"
        InvoiceStatus.DRAFT -> "مسودة"
        InvoiceStatus.CANCELLED -> "ملغاة"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.number,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "حذف",
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "العميل #${invoice.personId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
                Text(
                    text = MoneyFormatter.format(invoice.totalCents, invoice.currency),
                    fontSize = 16.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}