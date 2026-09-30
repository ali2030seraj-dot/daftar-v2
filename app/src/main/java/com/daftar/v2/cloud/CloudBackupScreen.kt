package com.daftar.v2.cloud

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.GoldAccent
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBackupScreen(
    navController: NavHostController,
    viewModel: CloudBackupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.onFolderPicked(it) }
    }

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text("☁️ النسخ الاحتياطي السحابي", fontWeight = FontWeight.Bold)
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
            if (state.busy) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoldAccent)
                        Spacer(Modifier.height(12.dp))
                        Text("جاري النسخ...", color = GoldAccent)
                    }
                }
            } else {
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
                                "💡 كيف يعمل؟",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Default
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "١. اختر مجلداً (Google Drive / Dropbox / أي مجلد).\n" +
                                "٢. أدخل كلمة مرور للتشفير.\n" +
                                "٣. اختر وضع النسخ.\n\n" +
                                "🔐 لن يتم النسخ بدون إذنك الصريح.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // ⭐ المجلد
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Folder,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "المجلد المختار",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.folderName,
                                fontSize = 13.sp,
                                color = if (state.folderUri != null)
                                    SuccessGreen
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { folderPicker.launch(null) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (state.folderUri == null)
                                        "📂 اختيار مجلد"
                                    else
                                        "📂 تغيير المجلد"
                                )
                            }
                        }
                    }

                    // ⭐ كلمة المرور
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                "كلمة المرور (للتشفير)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.password,
                                onValueChange = { viewModel.setPassword(it) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("6 أحرف على الأقل") },
                                visualTransformation = if (passwordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible)
                                                Icons.Filled.Visibility
                                            else
                                                Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "⚠️ احفظ كلمة المرور في مكان آمن.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // ⭐⭐⭐ اختيار وضع النسخ
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                "وضع النسخ الاحتياطي",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(10.dp))

                            ModeOption(
                                emoji = "🟢",
                                title = "تلقائي (بعد الموافقة)",
                                subtitle = "ينسخ كل 24 ساعة تلقائياً",
                                isSelected = state.mode == BackupMode.AUTO,
                                color = SuccessGreen,
                                onClick = { viewModel.requestModeChange(BackupMode.AUTO) }
                            )

                            Spacer(Modifier.height(8.dp))

                            ModeOption(
                                emoji = "🟡",
                                title = "بإذن قبل كل نسخة",
                                subtitle = "يُرسل إشعاراً → توافق → ينسخ",
                                isSelected = state.mode == BackupMode.ASK_EACH_TIME,
                                color = TertiaryAmber,
                                onClick = { viewModel.requestModeChange(BackupMode.ASK_EACH_TIME) }
                            )

                            Spacer(Modifier.height(8.dp))

                            ModeOption(
                                emoji = "🔴",
                                title = "معطل",
                                subtitle = "لا ينسخ أبداً",
                                isSelected = state.mode == BackupMode.OFF,
                                color = DangerRed,
                                onClick = { viewModel.requestModeChange(BackupMode.OFF) }
                            )
                        }
                    }

                    // ⭐ آخر نسخة
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Cloud,
                                contentDescription = null,
                                tint = TertiaryAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "آخر نسخة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    state.lastBackupLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ⭐ زر النسخ الفوري
                    Button(
                        onClick = { viewModel.runBackupNow() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        enabled = state.folderUri != null && state.password.length >= 6
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("🚀 نسخ الآن (يدوياً)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }

        GlassToast(
            message = state.message,
            isError = state.isError,
            onDismiss = { viewModel.clearMessage() }
        )
    }

    // ⭐⭐⭐ حوار الموافقة
    if (state.showConsentDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConsent() },
            title = {
                Text(
                    "🔐 تأكيد الإذن",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "أنت توافق على:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "• إنشاء نسخ احتياطية مشفرة.\n" +
                        "• حفظها في المجلد الذي اخترته.\n" +
                        "• الاحتفاظ بـ 7 نسخ كحد أقصى (يُحذف الأقدم تلقائياً).\n\n" +
                        if (state.pendingMode == BackupMode.ASK_EACH_TIME)
                            "🔔 سيُطلب إذنك قبل كل نسخة عبر إشعار."
                        else
                            "⚠️ النسخ سيجري تلقائياً كل 24 ساعة."
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "يمكنك إلغاء الإذن في أي وقت.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmConsent() },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("✅ أوافق")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConsent() }) {
                    Text("❌ إلغاء")
                }
            }
        )
    }
}

@Composable
private fun ModeOption(
    emoji: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Text(
                    "✓",
                    fontSize = 18.sp,
                    color = color,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}