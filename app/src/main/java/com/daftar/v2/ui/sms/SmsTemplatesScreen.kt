package com.daftar.v2.ui.sms

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.GoldAccent
import com.daftar.v2.ui.theme.TertiaryAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsTemplatesScreen(
    navController: NavHostController,
    viewModel: SmsTemplatesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var editingType by remember { mutableStateOf<String?>(null) }
    var editingBody by remember { mutableStateOf("") }

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text("📱 قوالب الرسائل", fontWeight = FontWeight.Bold)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ⭐ شرح
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "💡 المتغيرات المتاحة:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Default
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "{name} — اسم العميل",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default
                        )
                        Text(
                            text = "{amount} — المبلغ",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default
                        )
                        Text(
                            text = "{remaining} — المتبقي",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default
                        )
                        Text(
                            text = "{statement} — البيان",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                }

                // ⭐ قائمة القوالب
                state.templates.forEach { template ->
                    TemplateCard(
                        typeName = template.type,
                        body = template.body,
                        isCustom = template.isCustom,
                        onEdit = {
                            editingType = template.type
                            editingBody = template.body
                        },
                        onReset = { viewModel.resetToDefault(template.type) }
                    )
                }

                Spacer(Modifier.height(40.dp))
            }
        }

        GlassToast(
            message = state.message,
            isError = state.isError,
            onDismiss = { viewModel.clearMessage() }
        )
    }

    // ⭐ نافذة التعديل
    editingType?.let { type ->
        AlertDialog(
            onDismissRequest = { editingType = null },
            title = { Text("تعديل القالب", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingBody,
                    onValueChange = { editingBody = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveTemplate(type, editingBody)
                    editingType = null
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingType = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun TemplateCard(
    typeName: String,
    body: String,
    isCustom: Boolean,
    onEdit: () -> Unit,
    onReset: () -> Unit
) {
    val label = when (typeName) {
        "DEBT_REGISTERED" -> "📝 تسجيل دين جديد"
        "PAYMENT_RECEIVED" -> "💵 استلام دفعة"
        "REMINDER" -> "🔔 تذكير بالدين"
        else -> typeName
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isCustom) {
                    Text(
                        text = "مخصص",
                        fontSize = 10.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = body,
                fontSize = 11.sp,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("✏️ تعديل", fontSize = 12.sp)
                }
                if (isCustom) {
                    TextButton(
                        onClick = onReset,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TertiaryAmber
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "افتراضي",
                            fontSize = 12.sp,
                            color = TertiaryAmber
                        )
                    }
                }
            }
        }
    }
}