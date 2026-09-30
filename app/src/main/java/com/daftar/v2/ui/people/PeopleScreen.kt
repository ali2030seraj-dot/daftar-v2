package com.daftar.v2.ui.people

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.PersonType
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.components.AddPersonDialog
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.EmptyState
import com.daftar.v2.ui.components.FabAction
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassFab
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.components.SkeletonBox
import com.daftar.v2.ui.components.StaggeredAppear
import com.daftar.v2.ui.navigation.Destinations
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.MoneyFormatter
import com.daftar.v2.util.SmsHelper
import com.daftar.v2.util.WhatsAppOpener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    navController: NavHostController,
    type: PersonType,
    viewModel: PeopleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val tokens = LocalAdaptiveTokens.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPerson by remember { mutableStateOf<PersonEntity?>(null) }
    var deletingPerson by remember { mutableStateOf<PersonEntity?>(null) }

    LaunchedEffect(type) {
        viewModel.init(type)
    }

    AmbientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {

                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (type == PersonType.CREDITOR) "ما لك" else "ما عليك",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (type == PersonType.CREDITOR)
                                    "أشخاص عندهم مال لك"
                                else
                                    "أشخاص عليك مال لهم",
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
                    StaggeredAppear(index = 0) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::setSearchQuery,
                                placeholder = {
                                    Text(
                                        "بحث بالاسم...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    when {
                        state.loading -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SkeletonBox(height = 72.dp)
                                SkeletonBox(height = 72.dp)
                                SkeletonBox(height = 72.dp)
                            }
                        }
                        state.people.isEmpty() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                EmptyState(
                                    emoji = "📒",
                                    title = "لا توجد حسابات حتى الآن",
                                    subtitle = "ابدأ بإضافة أول حساب لك",
                                    actionLabel = "إضافة حساب",
                                    onAction = { showAddDialog = true }
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 120.dp)
                            ) {
                                items(state.people, key = { it.person.id }) { item ->
                                    CompactPersonCard(
                                        item = item,
                                        type = type,
                                        onClick = {
                                            navController.navigate(
                                                Destinations.statement(item.person.id)
                                            )
                                        },
                                        onCall = { person ->
                                            if (person.phone.isNotBlank()) {
                                                val cleanPhone = person.phone
                                                    .replace(Regex("[^0-9+]"), "")
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:$cleanPhone")
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) { }
                                            }
                                        },
                                        onWhatsAppReminder = { person, balance ->
                                            if (person.phone.isNotBlank()) {
                                                val balanceText = MoneyFormatter.format(
                                                    kotlin.math.abs(balance),
                                                    person.currency
                                                )
                                                // ⭐⭐⭐ تحديد الاتجاه:
                                                // type == CREDITOR → هو مدين لي (لك)
                                                // type == DEBTOR → أنا مدين له (عليك)
                                                val isTheyOweMe = type == PersonType.CREDITOR
                                                val message = buildReminderMessage(
                                                    person.name,
                                                    balanceText,
                                                    isTheyOweMe
                                                )
                                                WhatsAppOpener.openWhatsApp(
                                                    context,
                                                    person.phone,
                                                    message
                                                )
                                            }
                                        },
                                        onSmsReminder = { person, balance ->
                                            if (person.phone.isNotBlank()) {
                                                val balanceText = MoneyFormatter.format(
                                                    kotlin.math.abs(balance),
                                                    person.currency
                                                )
                                                // ⭐⭐⭐ تحديد الاتجاه
                                                val isTheyOweMe = type == PersonType.CREDITOR
                                                val message = buildReminderMessage(
                                                    person.name,
                                                    balanceText,
                                                    isTheyOweMe
                                                )
                                                SmsHelper.openSmsApp(
                                                    context,
                                                    person.phone,
                                                    message
                                                )
                                            }
                                        },
                                        onEdit = { editingPerson = item.person },
                                        onArchive = { viewModel.archive(item.person.id) },
                                        onDelete = { deletingPerson = item.person }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            GlassFab(
                actions = listOf(
                    FabAction("إضافة حساب", "➕", { showAddDialog = true })
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
        AddPersonDialog(
            type = type,
            existingDuplicates = state.duplicatesForDialog,
            onNameChange = { name, currency ->
                viewModel.checkDuplicates(name, currency)
            },
            onDismiss = {
                showAddDialog = false
                viewModel.clearDuplicatesForDialog()
            },
            onConfirm = { name, phone, note, currency ->
                viewModel.createPerson(name, phone, note, currency) {
                    showAddDialog = false
                }
            }
        )
    }

    editingPerson?.let { person ->
        EditPersonDialog(
            person = person,
            onDismiss = { editingPerson = null },
            onConfirm = { name, phone, note ->
                viewModel.updatePerson(person.id, name, phone, note)
                editingPerson = null
            }
        )
    }

    deletingPerson?.let { person ->
        AlertDialog(
            onDismissRequest = { deletingPerson = null },
            title = { Text("تأكيد الحذف النهائي", fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "سيتم حذف حساب (${person.name}) وكل عملياته نهائيًا.",
                        fontSize = 14.sp
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
                    viewModel.delete(person.id)
                    deletingPerson = null
                }) {
                    Text("حذف", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPerson = null }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
//              ⭐⭐⭐ رسالة التذكير — توضيح الاتجاه
// ═══════════════════════════════════════════════════════════

/**
 * ⭐⭐⭐ يبني رسالة تذكير توضح اتجاه الدين.
 *
 * @param isTheyOweMe  true = الشخص مدين لي (لك) — أطالبه بالسداد
 *                     false = أنا مدين له (عليك) — أؤكد له أنني سأسدد
 */
private fun buildReminderMessage(
    name: String,
    balanceText: String,
    isTheyOweMe: Boolean
): String {
    return if (isTheyOweMe) {
        // ⭐⭐ أنا الدائن — أُذكّر الشخص بالمبلغ الذي في ذمته
        """السلام عليكم ورحمة الله وبركاته
أخي/تــــ $name،

📌 تذكير ودّي

المبلغ المستحق لنا في ذمتكم:
$balanceText

نرجو التكرم بسداده في أقرب وقت.

شكرًا لتعاملكم معنا.""".trimIndent()
    } else {
        // ⭐⭐ أنا المدين — أُطمئن الشخص أنني سأسدد
        """السلام عليكم ورحمة الله وبركاته
أخي/تــــ $name،

📌 تذكير ودّي

المبلغ المستحق لكم في ذمتنا:
$balanceText

نؤكد لكم أننا سنقوم بسداده في أقرب وقت إن شاء الله.

شكراً لتفهمكم.""".trimIndent()
    }
}

// ═══════════════════════════════════════════════════════════
//              بطاقة شخص مضغوطة
// ═══════════════════════════════════════════════════════════

@Composable
private fun CompactPersonCard(
    item: PersonWithBalance,
    type: PersonType,
    onClick: () -> Unit,
    onCall: (PersonEntity) -> Unit,
    onWhatsAppReminder: (PersonEntity, Long) -> Unit,
    onSmsReminder: (PersonEntity, Long) -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val color = if (type == PersonType.CREDITOR) SuccessGreen else DangerRed
    var menuExpanded by remember { mutableStateOf(false) }

    val hasPhone = item.person.phone.isNotBlank()
    val hasBalance = item.balanceCents != 0L
    val isSettled = item.balanceCents == 0L

    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val statusText = if (isSettled) "مسدَّد" else "قائم"
    val statusColor = if (isSettled) SuccessGreen else TertiaryAmber
    val statusBg = if (isSettled) {
        SuccessGreen.copy(alpha = 0.15f)
    } else {
        TertiaryAmber.copy(alpha = 0.15f)
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ⭐ الأيقونة الدائرية
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.person.name.take(1),
                    fontSize = 18.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(10.dp))

            // ⭐ الوسط: الاسم + السطر الثانوي
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.person.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        color = primaryTextColor,
                        maxLines = 1
                    )
                    if (item.hasSameName) {
                        Spacer(Modifier.width(4.dp))
                        Text("⚠️", fontSize = 11.sp)
                    }
                }
                val subText = when {
                    item.person.note.isNotBlank() -> item.person.note
                    item.person.phone.isNotBlank() -> item.person.phone
                    else -> null
                }
                if (subText != null) {
                    Text(
                        text = subText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Default,
                        color = secondaryTextColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // ⭐ اليمين: شارة + المبلغ
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                }

                Text(
                    text = MoneyFormatter.formatShort(item.balanceCents),
                    fontSize = 15.sp,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    maxLines = 1
                )
            }

            // ⭐ قائمة الخيارات
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
                    if (hasPhone) {
                        DropdownMenuItem(
                            text = {
                                Text("📞 اتصال", fontSize = 13.sp, color = primaryTextColor)
                            },
                            onClick = { menuExpanded = false; onCall(item.person) }
                        )
                    }
                    if (hasPhone && hasBalance) {
                        DropdownMenuItem(
                            text = {
                                Text("💬 واتساب", fontSize = 13.sp, color = primaryTextColor)
                            },
                            onClick = {
                                menuExpanded = false
                                onWhatsAppReminder(item.person, item.balanceCents)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("📱 رسالة SMS", fontSize = 13.sp, color = primaryTextColor)
                            },
                            onClick = {
                                menuExpanded = false
                                onSmsReminder(item.person, item.balanceCents)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Text("✏️ تعديل", fontSize = 13.sp, color = primaryTextColor)
                        },
                        onClick = { menuExpanded = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("📦 أرشفة", fontSize = 13.sp, color = primaryTextColor)
                        },
                        onClick = { menuExpanded = false; onArchive() }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "🗑️ حذف",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              نافذة تعديل الشخص
// ═══════════════════════════════════════════════════════════

@Composable
private fun EditPersonDialog(
    person: PersonEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, note: String) -> Unit
) {
    var name by remember { mutableStateOf(person.name) }
    var phone by remember { mutableStateOf(person.phone) }
    var note by remember { mutableStateOf(person.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل الحساب", fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("الهاتف", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة تمييز", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name, phone, note)
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}