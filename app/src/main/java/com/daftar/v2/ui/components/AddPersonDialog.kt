package com.daftar.v2.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.PersonType
import com.daftar.v2.util.ContactHelper
import com.daftar.v2.util.CurrencyUtils
import kotlinx.coroutines.delay

@Composable
fun AddPersonDialog(
    type: PersonType,
    existingDuplicates: List<PersonEntity>,
    onNameChange: (name: String, currency: String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, note: String, currency: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var currencyCode by remember { mutableStateOf(CurrencyUtils.DEFAULT_CODE) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // ⭐ منتقي جهات الاتصال
    val contactPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val contact = ContactHelper.readContact(context, uri)
            if (contact != null) {
                if (contact.name.isNotBlank()) name = contact.name
                if (contact.phone.isNotBlank()) phone = contact.phone
            }
        }
    }

    // ⭐ طلب الصلاحية
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            contactPicker.launch(null)
        } else {
            validationError = "لم يتم منح صلاحية جهات الاتصال"
        }
    }

    // ⭐ دالة تفتح جهات الاتصال (مع طلب الصلاحية أولاً)
    fun openContactPicker() {
        val permission = Manifest.permission.READ_CONTACTS
        val granted = ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED

        if (granted) {
            contactPicker.launch(null)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    LaunchedEffect(name, currencyCode) {
        validationError = null
        if (name.length >= 2) {
            delay(250)
            onNameChange(name, currencyCode)
        } else {
            onNameChange("", currencyCode)
        }
    }

    val hasDuplicates = existingDuplicates.isNotEmpty()

    GlassModal(
        title = if (type == PersonType.CREDITOR) "إضافة عميل - ما لك"
        else "إضافة دائن - ما عليك",
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ⭐ حقل الاسم — مع أيقونة جهات الاتصال
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; validationError = null },
                label = { Text("الاسم") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { openContactPicker() }) {
                        Icon(
                            imageVector = Icons.Filled.Contacts,
                            contentDescription = "اختيار من جهات الاتصال",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            // ⭐ زر إضافي أسفل حقل الاسم
            TextButton(
                onClick = { openContactPicker() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Contacts,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = "اختيار من جهات الاتصال",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
            }

            if (hasDuplicates) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "⚠ يوجد ${existingDuplicates.size} حساب بنفس الاسم:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        existingDuplicates.forEach { person ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "• ${person.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (person.phone.isNotBlank()) {
                                    Text(
                                        " — ${person.phone}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (person.note.isNotBlank()) {
                                    Text(
                                        " (${person.note})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "لإضافة حساب جديد بنفس الاسم، أضف:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "• رقم هاتف مختلف  أو  ملاحظة تمييز",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; validationError = null },
                label = {
                    Text(
                        if (hasDuplicates) "رقم الهاتف (مطلوب للتمييز)"
                        else "رقم الهاتف - اختياري"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it; validationError = null },
                label = {
                    Text(
                        if (hasDuplicates) "ملاحظة التمييز (مطلوبة للتمييز)"
                        else "ملاحظة - اختياري"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "العملة",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CurrencyUtils.ALL_CODES.forEach { code ->
                    val isSelected = code == currencyCode
                    TextButton(
                        onClick = {
                            currencyCode = code
                            validationError = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            CurrencyUtils.arabicShortName(code),
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            validationError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

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
                        validationError = null
                        onConfirm(name, phone, note, currencyCode)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank()
                ) {
                    Text("حفظ")
                }
            }
        }
    }
}