package com.daftar.v2.ui.statement

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.data.entity.PersonType
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.pdf.PdfViewModel
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.components.AddDebtDialog
import com.daftar.v2.ui.components.AddPaymentDialog
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.EmptyState
import com.daftar.v2.ui.components.FabAction
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassFab
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.components.SettlementDialog
import com.daftar.v2.ui.components.SkeletonBox
import com.daftar.v2.ui.components.StaggeredAppear
import com.daftar.v2.ui.components.VoiceRecorderSheet
import com.daftar.v2.ui.navigation.Destinations
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.CurrencyUtils
import com.daftar.v2.util.DateFormatter
import com.daftar.v2.util.MoneyFormatter
import com.daftar.v2.util.voice.VoicePlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementScreen(
    navController: NavHostController,
    personId: Long,
    viewModel: StatementViewModel = hiltViewModel(),
    pdfViewModel: PdfViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pdfState by pdfViewModel.state.collectAsState()
    val context = LocalContext.current
    val tokens = LocalAdaptiveTokens.current

    var showDebtDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showSettlementDialog by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var deletingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var recordingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var playingTxId by remember { mutableStateOf<Long?>(null) }

    val voicePlayer = remember { VoicePlayer() }

    DisposableEffect(Unit) {
        onDispose { voicePlayer.stop() }
    }

    LaunchedEffect(personId) {
        viewModel.init(personId)
    }

    val isAllMode = state.isAllMode

    AmbientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            when {
                                isAllMode -> "كل العمليات"
                                state.statement?.person != null -> state.statement!!.person!!.name
                                else -> "كشف حساب"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Default
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                )

                if (state.loading && state.statement == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = tokens.screenPaddingHorizontal,
                                vertical = 8.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBox(height = 100.dp)
                        SkeletonBox(height = 70.dp)
                        SkeletonBox(height = 70.dp)
                    }
                } else {
                    val stmt = state.statement
                    if (stmt == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                emoji = "❓",
                                title = "لم يتم العثور على الحساب",
                                subtitle = "قد يكون الحساب محذوفًا"
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    horizontal = tokens.screenPaddingHorizontal,
                                    vertical = 8.dp
                                ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(
                                bottom = if (isAllMode) 30.dp else 200.dp
                            )
                        ) {
                            item {
                                StaggeredAppear(index = 0) {
                                    CompactSummaryCard(
                                        finalBalanceCents = stmt.finalBalanceCents,
                                        totalDebtCents = stmt.totalDebtCents,
                                        totalPaymentCents = stmt.totalPaymentCents,
                                        currency = stmt.displayCurrency,
                                        count = stmt.rows.size,
                                        isAllMode = isAllMode,
                                        personType = stmt.person?.type
                                    )
                                }
                            }

                            if (!isAllMode && stmt.finalBalanceCents != 0L) {
                                item {
                                    StaggeredAppear(index = 1) {
                                        Button(
                                            onClick = { showSettlementDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = SuccessGreen
                                            ),
                                            contentPadding = PaddingValues(vertical = 10.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                "🤝 تسوية الحساب",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                fontFamily = FontFamily.Default
                                            )
                                        }
                                    }
                                }
                            }

                            if (!isAllMode) {
                                item {
                                    StaggeredAppear(index = 2) {
                                        CompactStatementActions(
                                            onSavePdf = {
                                                pdfViewModel.savePdfToDownloads(context, personId)
                                            },
                                            onSharePdf = {
                                                pdfViewModel.sharePdf(context, personId)
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "العمليات",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Default,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                )
                            }

                            if (stmt.rows.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        EmptyState(
                                            emoji = "📒",
                                            title = "لا توجد عمليات",
                                            subtitle = if (isAllMode) "لا توجد عمليات مسجلة بعد"
                                            else "ابدأ بتسجيل أول عملية",
                                            actionLabel = if (isAllMode) null else "إضافة مبلغ",
                                            onAction = if (isAllMode) null else {
                                                { showDebtDialog = true }
                                            }
                                        )
                                    }
                                }
                            } else {
                                items(stmt.rows, key = { it.transaction.id }) { row ->
                                    CompactTransactionCard(
                                        row = row,
                                        isPlaying = playingTxId == row.transaction.id,
                                        onEdit = { editingTx = row.transaction },
                                        onDelete = { deletingTx = row.transaction },
                                        onReceipt = {
                                            val targetPersonId = if (isAllMode) {
                                                row.transaction.personId
                                            } else personId
                                            if (targetPersonId > 0L) {
                                                navController.navigate(
                                                    Destinations.receipt(
                                                        targetPersonId,
                                                        row.transaction.id
                                                    )
                                                )
                                            }
                                        },
                                        onRecordVoice = { recordingTx = row.transaction },
                                        onDeleteVoice = { viewModel.removeVoice(row.transaction.id) },
                                        onPlayToggle = {
                                            val txId = row.transaction.id
                                            val path = row.transaction.voicePath
                                            if (path == null) return@CompactTransactionCard

                                            if (playingTxId == txId) {
                                                voicePlayer.stop()
                                                playingTxId = null
                                            } else {
                                                voicePlayer.stop()
                                                playingTxId = txId
                                                voicePlayer.play(path) { playingTxId = null }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!isAllMode) {
                GlassFab(
                    actions = listOf(
                        FabAction("إضافة مبلغ", "➕", { showDebtDialog = true }),
                        FabAction("تسجيل سداد", "💵", { showPaymentDialog = true })
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                )
            }
        }

        GlassToast(
            message = state.message,
            isError = state.isError,
            onDismiss = { viewModel.clearMessage() }
        )
        GlassToast(
            message = pdfState.message,
            isError = pdfState.isError,
            onDismiss = { pdfViewModel.clearMessage() }
        )
    }

    val currency = state.statement?.displayCurrency ?: ""

    // ⭐⭐⭐ تعديل استدعاء AddDebtDialog لاستقبال 5 باراميترات
    if (showDebtDialog && !isAllMode) {
        AddDebtDialog(
            currency = currency,
            onDismiss = { showDebtDialog = false },
            onConfirm = { statement, cents, note, isRecurring, recurringInterval ->
                viewModel.addDebtTransaction(
                    statement = statement,
                    amountCents = cents,
                    note = note,
                    isRecurring = isRecurring,
                    recurringInterval = recurringInterval
                )
                showDebtDialog = false
            }
        )
    }

    if (showPaymentDialog && !isAllMode) {
        AddPaymentDialog(
            currency = currency,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { cents, note ->
                viewModel.addPaymentTransaction(cents, note)
                showPaymentDialog = false
            }
        )
    }

    if (showSettlementDialog && state.statement?.finalBalanceCents != 0L && !isAllMode) {
        SettlementDialog(
            balanceCents = state.statement?.finalBalanceCents ?: 0L,
            currency = currency,
            onDismiss = { showSettlementDialog = false },
            onConfirm = { settlementCents, archiveAfter ->
                viewModel.addPaymentTransaction(settlementCents, "تسوية حساب")
                showSettlementDialog = false
            }
        )
    }

    recordingTx?.let { tx ->
        VoiceRecorderSheet(
            onSave = { path ->
                viewModel.attachVoice(tx.id, path)
                recordingTx = null
            },
            onDismiss = { recordingTx = null }
        )
    }

    editingTx?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            onDismiss = { editingTx = null },
            onConfirm = { newStatement, newCents, newNote ->
                viewModel.editTransaction(tx.id, newStatement, newCents, newNote)
                editingTx = null
            }
        )
    }

    deletingTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { deletingTx = null },
            title = { Text("تأكيد الحذف", fontSize = 16.sp, fontFamily = FontFamily.Default) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("هل تريد حذف هذه العملية نهائيًا؟", fontSize = 14.sp, fontFamily = FontFamily.Default)
                    Text(
                        tx.statement,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        MoneyFormatter.format(tx.amountCents, currency),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (tx.type == TransactionType.DEBT) DangerRed else SuccessGreen
                    )
                    Text(
                        DateFormatter.fullDualLabel(tx.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        "لا يمكن التراجع.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(tx.id)
                    deletingTx = null
                }) {
                    Text("حذف", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTx = null }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
//              بطاقة الملخص — المبلغ ثم اسم العملة
// ═══════════════════════════════════════════════════════════

@Composable
private fun CompactSummaryCard(
    finalBalanceCents: Long,
    totalDebtCents: Long,
    totalPaymentCents: Long,
    currency: String,
    count: Int,
    isAllMode: Boolean = false,
    personType: PersonType? = null
) {
    val isSettled = finalBalanceCents == 0L
    val isCreditor = personType == PersonType.CREDITOR

    val statusText: String = when {
        isAllMode -> "📚 عرض كلي"
        isSettled -> "مسدَّد"
        isCreditor -> "لك عنده"
        else -> "عليه لك"
    }

    val statusColor: Color = when {
        isAllMode -> MaterialTheme.colorScheme.primary
        isSettled -> SuccessGreen
        else -> TertiaryAmber
    }

    val balanceColor: Color = when {
        isSettled -> MaterialTheme.colorScheme.onSurfaceVariant
        isCreditor && finalBalanceCents > 0 -> SuccessGreen
        isCreditor && finalBalanceCents < 0 -> DangerRed
        !isCreditor && finalBalanceCents > 0 -> DangerRed
        !isCreditor && finalBalanceCents < 0 -> SuccessGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                }
                Text(
                    text = "$count عملية",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAllMode) "الرصيد الكلي" else "الرصيد المتبقي",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Default
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = MoneyFormatter.formatShort(kotlin.math.abs(finalBalanceCents)),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = balanceColor,
                            fontFamily = FontFamily.Default
                        )
                    }
                    Text(
                        text = CurrencyUtils.arabicFullName(currency),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = balanceColor.copy(alpha = 0.85f),
                        fontFamily = FontFamily.Default
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إجمالي المبلغ",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Default
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = MoneyFormatter.formatShort(totalDebtCents),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Default
                            )
                        }
                        Text(
                            text = CurrencyUtils.arabicFullName(currency),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Default
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "إجمالي السداد",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Default
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = MoneyFormatter.formatShort(totalPaymentCents),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                fontFamily = FontFamily.Default
                            )
                        }
                        Text(
                            text = CurrencyUtils.arabicFullName(currency),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Default
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              بطاقة عملية مضغوطة
// ═══════════════════════════════════════════════════════════

@Composable
private fun CompactTransactionCard(
    row: com.daftar.v2.domain.usecase.StatementRow,
    isPlaying: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReceipt: () -> Unit,
    onRecordVoice: () -> Unit,
    onDeleteVoice: () -> Unit,
    onPlayToggle: () -> Unit
) {
    val tx = row.transaction
    val isDebt = tx.type == TransactionType.DEBT
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isDebt) DangerRed else SuccessGreen)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = tx.statement,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                Text(
                    text = if (isDebt)
                        MoneyFormatter.formatShort(tx.amountCents)
                    else
                        "- ${MoneyFormatter.formatShort(tx.amountCents)}",
                    fontSize = 15.sp,
                    color = if (isDebt) DangerRed else SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "خيارات",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("📄 إيصال السداد", fontSize = 13.sp) },
                            onClick = { menuExpanded = false; onReceipt() }
                        )
                        if (tx.voicePath == null) {
                            DropdownMenuItem(
                                text = { Text("🎤 تسجيل ملاحظة", fontSize = 13.sp) },
                                onClick = { menuExpanded = false; onRecordVoice() }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("🗑️ حذف التسجيل", color = DangerRed, fontSize = 13.sp) },
                                onClick = { menuExpanded = false; onDeleteVoice() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("✏️ تعديل", fontSize = 13.sp) },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("🗑️ حذف", color = DangerRed, fontSize = 13.sp) },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateFormatter.fullDualLabel(tx.timestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "الرصيد: ${MoneyFormatter.formatShort(row.runningBalanceCents)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Default
                )
            }

            if (tx.note.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    tx.note,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default,
                    maxLines = 2
                )
            }

            if (tx.voicePath != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TertiaryAmber.copy(alpha = 0.12f))
                        .clickable { onPlayToggle() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) DangerRed else TertiaryAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Stop
                            else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "🎤 جاري التشغيل..." else "🎤 ملاحظة صوتية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TertiaryAmber,
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              أزرار PDF
// ═══════════════════════════════════════════════════════════

@Composable
private fun CompactStatementActions(
    onSavePdf: () -> Unit,
    onSharePdf: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSharePdf,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            contentPadding = PaddingValues(vertical = 10.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "🔗 مشاركة",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = FontFamily.Default
            )
        }
        Button(
            onClick = onSavePdf,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(vertical = 10.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "📄 PDF",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Default
            )
        }
    }
}

@Composable
private fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (statement: String, amountCents: Long, note: String) -> Unit
) {
    var statement by remember { mutableStateOf(transaction.statement) }
    var amount by remember {
        mutableStateOf(MoneyFormatter.formatShort(transaction.amountCents))
    }
    var note by remember { mutableStateOf(transaction.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل العملية", fontSize = 16.sp, fontFamily = FontFamily.Default) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = statement,
                    onValueChange = { statement = it },
                    label = { Text("البيان", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("المبلغ", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظات", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cents = MoneyFormatter.parseToCents(amount)
                    if (cents != null && cents > 0 && statement.isNotBlank()) {
                        onConfirm(statement, cents, note)
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