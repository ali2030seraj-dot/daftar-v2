package com.daftar.v2.backup

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.cloud.BackupMode
import com.daftar.v2.cloud.CloudFileInfo
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.GoldAccent
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavHostController,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ═════ محلي — التصدير
    var showExportDialog by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var exportConfirmPassword by remember { mutableStateOf("") }
    var exportError by remember { mutableStateOf<String?>(null) }
    var exportPasswordVisible by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    // ⭐ اسم الملف
    var showFileNameDialog by remember { mutableStateOf(false) }
    var suggestedFileName by remember { mutableStateOf("Daftar_Backup_001.daftar") }
    var customFileName by remember { mutableStateOf("") }

    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var pendingExportContent by remember { mutableStateOf<String?>(null) }
    var pendingExportFileName by remember { mutableStateOf("") }

    // ═════ محلي — الاستيراد
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var importPassword by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    var importPasswordVisible by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportModeDialog by remember { mutableStateOf(false) }

    // ═════ سحابي
    var cloudPasswordVisible by remember { mutableStateOf(false) }
    var cloudPasswordInput by remember { mutableStateOf(state.cloudPassword) }
    var showCloudRestorePasswordDialog by remember { mutableStateOf(false) }
    var cloudRestorePassword by remember { mutableStateOf("") }
    var cloudRestorePasswordVisible by remember { mutableStateOf(false) }

    // ═════ منتقي الملفات المحلي
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            importPassword = ""
            importError = null
            importPasswordVisible = false
            showImportPasswordDialog = true
        }
    }

    // ═════ حفظ محلي
    val saveLocalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val content = pendingExportContent
        val fileName = pendingExportFileName
        if (content.isNullOrBlank()) {
            Toast.makeText(context, "❌ لا يوجد محتوى", Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                        out.write(content.toByteArray(Charsets.UTF_8))
                        out.flush()
                    } ?: throw IllegalStateException("فشل فتح الملف للكتابة")
                }
                viewModel.showSuccess("✅ تم الحفظ: $fileName")
            } catch (e: Exception) {
                viewModel.showError("❌ فشل الحفظ: ${e.message}")
            } finally {
                pendingExportContent = null
                pendingExportFileName = ""
            }
        }
    }

    // ═════ تصدير سحابي إلى الجهاز
    val exportCloudLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val file = state.selectedCloudFile ?: return@rememberLauncherForActivityResult
        viewModel.exportCloudFileToDevice(file, uri)
    }

    // ═════ منتقي المجلد السحابي
    val cloudFolderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.onCloudFolderPicked(it) }
    }

    // ═════ دالة مشاركة محلية
    fun shareContent(content: String, fileName: String) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val cacheDir = File(context.cacheDir, "backups").apply { mkdirs() }
                    val file = File(cacheDir, fileName)
                    file.writeText(content, Charsets.UTF_8)

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/octet-stream"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية — دفتر حساباتي")
                        putExtra(Intent.EXTRA_TEXT, "نسخة احتياطية مشفرة")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    withContext(Dispatchers.Main) {
                        val chooser = Intent.createChooser(intent, "مشاركة النسخة عبر")
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                    }
                }
            } catch (e: Exception) {
                viewModel.showError("❌ فشل المشاركة: ${e.message}")
            }
        }
    }

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("النسخ الاحتياطي", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                if (state.busy || isExporting) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        // ═══════════════════════════════════════
                        //              ☁️ قسم السحابة
                        // ═══════════════════════════════════════

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Cloud,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "☁️ النسخ الاحتياطي السحابي",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Default
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    "نسخ تلقائي إلى Google Drive أو أي مجلد تختاره.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    "المجلد: ${state.cloudFolderName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (state.cloudHasFolder) SuccessGreen
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    "آخر نسخة: ${state.cloudLastBackupLabel}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = { cloudFolderPicker.launch(null) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Filled.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (state.cloudHasFolder) "تغيير المجلد"
                                        else "📂 اختيار مجلد"
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                // ⭐ حقل كلمة المرور السحابي مع عين
                                OutlinedTextField(
                                    value = cloudPasswordInput,
                                    onValueChange = {
                                        cloudPasswordInput = it
                                        viewModel.setCloudPassword(it)
                                    },
                                    label = { Text("كلمة المرور للتشفير", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    placeholder = { Text("6 أحرف على الأقل", fontSize = 11.sp) },
                                    visualTransformation = if (cloudPasswordVisible)
                                        VisualTransformation.None
                                    else
                                        PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            cloudPasswordVisible = !cloudPasswordVisible
                                        }) {
                                            Icon(
                                                imageVector = if (cloudPasswordVisible)
                                                    Icons.Filled.Visibility
                                                else
                                                    Icons.Filled.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                )

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    "وضع النسخ:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ModeChip(
                                        label = "معطل",
                                        selected = state.cloudMode == BackupMode.OFF,
                                        color = DangerRed,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.setCloudMode(BackupMode.OFF) }
                                    )
                                    ModeChip(
                                        label = "بإذن",
                                        selected = state.cloudMode == BackupMode.ASK_EACH_TIME,
                                        color = TertiaryAmber,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.setCloudMode(BackupMode.ASK_EACH_TIME) }
                                    )
                                    ModeChip(
                                        label = "تلقائي",
                                        selected = state.cloudMode == BackupMode.AUTO,
                                        color = SuccessGreen,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.setCloudMode(BackupMode.AUTO) }
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.backupToCloudNow() },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldAccent
                                        ),
                                        enabled = state.cloudHasFolder &&
                                                cloudPasswordInput.length >= 6,
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("نسخ الآن", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.openCloudFilesDialog() },
                                        modifier = Modifier.weight(1f),
                                        enabled = state.cloudHasFolder,
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("استرداد", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // ═══════════════════════════════════════
                        //              📱 النسخ المحلي
                        // ═══════════════════════════════════════

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(
                                    "🔒 نسخة احتياطية محلية",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TertiaryAmber
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "احفظ نسخة على الجهاز، أو شاركها عبر واتساب / البريد / Drive.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                exportPassword = ""
                                exportConfirmPassword = ""
                                exportError = null
                                exportPasswordVisible = false
                                showExportDialog = true
                            }
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "تصدير نسخة محلية",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "احفظها في هاتفك أو شاركها",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { importLauncher.launch(arrayOf("*/*")) }
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Icon(
                                    Icons.Filled.Download,
                                    contentDescription = null,
                                    tint = SuccessGreen
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "استيراد من ملف محلي",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "استعد بياناتك من ملف .daftar",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(40.dp))
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

    // ═══════════════════════════════════════════════════════
    //              نافذة كلمة مرور التصدير المحلي — مع عين
    // ═══════════════════════════════════════════════════════

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isExporting) showExportDialog = false },
            title = { Text("اختر كلمة مرور للنسخة") },
            text = {
                Column {
                    // ⭐ كلمة المرور مع عين
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = { exportPassword = it; exportError = null },
                        label = { Text("كلمة المرور") },
                        singleLine = true,
                        enabled = !isExporting,
                        visualTransformation = if (exportPasswordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        isError = exportError != null,
                        trailingIcon = {
                            IconButton(onClick = {
                                exportPasswordVisible = !exportPasswordVisible
                            }) {
                                Icon(
                                    imageVector = if (exportPasswordVisible)
                                        Icons.Filled.Visibility
                                    else Icons.Filled.VisibilityOff,
                                    contentDescription = if (exportPasswordVisible)
                                        "إخفاء" else "إظهار"
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    // ⭐ تأكيد كلمة المرور مع نفس العين
                    OutlinedTextField(
                        value = exportConfirmPassword,
                        onValueChange = { exportConfirmPassword = it; exportError = null },
                        label = { Text("تأكيد كلمة المرور") },
                        singleLine = true,
                        enabled = !isExporting,
                        visualTransformation = if (exportPasswordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        isError = exportError != null,
                        trailingIcon = {
                            IconButton(onClick = {
                                exportPasswordVisible = !exportPasswordVisible
                            }) {
                                Icon(
                                    imageVector = if (exportPasswordVisible)
                                        Icons.Filled.Visibility
                                    else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    if (exportError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(exportError!!, color = DangerRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            exportPassword.length < 6 -> exportError = "6 أحرف على الأقل"
                            exportPassword != exportConfirmPassword -> exportError = "غير متطابقتين"
                            else -> {
                                isExporting = true
                                scope.launch {
                                    try {
                                        val content = withContext(Dispatchers.IO) {
                                            viewModel.buildEncryptedContent(exportPassword)
                                        }
                                        if (content.isNullOrBlank()) {
                                            isExporting = false
                                            return@launch
                                        }
                                        pendingExportContent = content

                                        val nextName = viewModel.getNextSequentialFileName()
                                        suggestedFileName = nextName
                                        customFileName = ""

                                        showExportDialog = false
                                        isExporting = false
                                        showFileNameDialog = true
                                    } catch (e: Exception) {
                                        isExporting = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isExporting,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text(if (isExporting) "..." else "متابعة")
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isExporting) showExportDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════
    //              نافذة اسم الملف
    // ═══════════════════════════════════════════════════════

    if (showFileNameDialog) {
        AlertDialog(
            onDismissRequest = { showFileNameDialog = false },
            title = { Text("💾 اسم ملف النسخة") },
            text = {
                Column {
                    Text(
                        "يمكنك استخدام الاسم المقترح أو كتابة اسم خاص بك.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customFileName.ifBlank { suggestedFileName },
                        onValueChange = { customFileName = it },
                        label = { Text("اسم الملف") },
                        placeholder = { Text(suggestedFileName) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        suffix = { Text(".daftar") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "💡 الاسم المقترح: $suggestedFileName",
                        fontSize = 11.sp,
                        color = GoldAccent
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = (customFileName.ifBlank { suggestedFileName })
                            .let { if (it.endsWith(".daftar")) it else "$it.daftar" }
                        pendingExportFileName = finalName
                        showFileNameDialog = false
                        customFileName = ""
                        showExportOptionsDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("متابعة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFileNameDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════
    //              نافذة خيارات الحفظ
    // ═══════════════════════════════════════════════════════

    if (showExportOptionsDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportOptionsDialog = false
                pendingExportContent = null
            },
            title = { Text("كيف تريد حفظ النسخة؟") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "📄 ${pendingExportFileName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Button(
                        onClick = {
                            val fileName = pendingExportFileName
                            showExportOptionsDialog = false
                            saveLocalLauncher.launch(fileName)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("💾 حفظ محلي")
                    }
                    OutlinedButton(
                        onClick = {
                            val content = pendingExportContent
                            val fileName = pendingExportFileName
                            showExportOptionsDialog = false
                            pendingExportContent = null
                            if (content != null && fileName.isNotBlank()) {
                                shareContent(content, fileName)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("📤 مشاركة")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showExportOptionsDialog = false
                    pendingExportContent = null
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════
    //              نافذة كلمة مرور الاستيراد المحلي — مع عين
    // ═══════════════════════════════════════════════════════

    if (showImportPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportPasswordDialog = false
                pendingImportUri = null
            },
            title = { Text("أدخل كلمة مرور النسخة") },
            text = {
                Column {
                    OutlinedTextField(
                        value = importPassword,
                        onValueChange = { importPassword = it; importError = null },
                        label = { Text("كلمة المرور") },
                        singleLine = true,
                        visualTransformation = if (importPasswordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        isError = importError != null,
                        // ⭐⭐⭐ العين
                        trailingIcon = {
                            IconButton(onClick = {
                                importPasswordVisible = !importPasswordVisible
                            }) {
                                Icon(
                                    imageVector = if (importPasswordVisible)
                                        Icons.Filled.Visibility
                                    else Icons.Filled.VisibilityOff,
                                    contentDescription = if (importPasswordVisible)
                                        "إخفاء" else "إظهار"
                                )
                            }
                        }
                    )
                    if (importError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(importError!!, color = DangerRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (importPassword.isBlank()) {
                        importError = "أدخل كلمة المرور"
                        return@Button
                    }
                    showImportPasswordDialog = false
                    showImportModeDialog = true
                }) {
                    Text("التالي")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportPasswordDialog = false
                    pendingImportUri = null
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════
    //              نافذة وضع الاستيراد
    // ═══════════════════════════════════════════════════════

    if (showImportModeDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportModeDialog = false
                pendingImportUri = null
            },
            title = { Text("طريقة الاستيراد") },
            text = {
                Column {
                    Text("• الدمج: يُحدّث الموجود ويُضيف الجديد.\n• الاستبدال: يحذف كل شيء.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingImportUri?.let { uri ->
                            viewModel.importFromUri(
                                uri, importPassword, ImportMode.MERGE
                            )
                        }
                        showImportModeDialog = false
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("دمج")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingImportUri?.let { uri ->
                        viewModel.importFromUri(
                            uri, importPassword, ImportMode.REPLACE
                        )
                    }
                    showImportModeDialog = false
                    pendingImportUri = null
                }) {
                    Text("استبدال", color = DangerRed)
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════
    //              نافذة قائمة الملفات السحابية
    // ═══════════════════════════════════════════════════════

    if (state.showCloudFilesDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeCloudFilesDialog() },
            title = { Text("📂 ملفات السحابة", fontWeight = FontWeight.Bold) },
            text = {
                if (state.cloudFiles.isEmpty()) {
                    Text("لا توجد نسخ احتياطية في المجلد السحابي.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(state.cloudFiles, key = { it.uri.toString() }) { file ->
                            CloudFileRow(
                                file = file,
                                onClick = { viewModel.selectCloudFileForRestore(file) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeCloudFilesDialog() }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════
    //              نافذة خيارات الملف المحدد
    // ═══════════════════════════════════════════════════════

    if (state.showExportCloudDialog && state.selectedCloudFile != null) {
        val file = state.selectedCloudFile!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissExportCloudDialog() },
            title = { Text("📄 ${file.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("الحجم: ${file.sizeLabel()}", fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "التاريخ: ${
                            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
                                .format(Date(file.lastModified))
                        }",
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "اختر الإجراء:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCloudRestorePasswordDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("🔄 استرداد")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { exportCloudLauncher.launch(file.name) }) {
                        Text("💾 تصدير للجهاز", color = GoldAccent)
                    }
                    TextButton(onClick = { viewModel.dismissExportCloudDialog() }) {
                        Text("إلغاء")
                    }
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════
    //              نافذة كلمة مرور الاسترداد السحابي — مع عين
    // ═══════════════════════════════════════════════════════

    if (showCloudRestorePasswordDialog && state.selectedCloudFile != null) {
        AlertDialog(
            onDismissRequest = {
                showCloudRestorePasswordDialog = false
                cloudRestorePassword = ""
                cloudRestorePasswordVisible = false
            },
            title = { Text("أدخل كلمة المرور") },
            text = {
                Column {
                    Text(
                        "كلمة المرور التي استُخدمت عند إنشاء النسخة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cloudRestorePassword,
                        onValueChange = { cloudRestorePassword = it },
                        label = { Text("كلمة المرور") },
                        singleLine = true,
                        visualTransformation = if (cloudRestorePasswordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        // ⭐⭐⭐ العين
                        trailingIcon = {
                            IconButton(onClick = {
                                cloudRestorePasswordVisible = !cloudRestorePasswordVisible
                            }) {
                                Icon(
                                    imageVector = if (cloudRestorePasswordVisible)
                                        Icons.Filled.Visibility
                                    else Icons.Filled.VisibilityOff,
                                    contentDescription = if (cloudRestorePasswordVisible)
                                        "إخفاء" else "إظهار"
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "اختر طريقة الاسترداد:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = state.selectedCloudFile!!
                        viewModel.restoreFromCloudFile(
                            file = file,
                            password = cloudRestorePassword,
                            mode = ImportMode.MERGE
                        )
                        showCloudRestorePasswordDialog = false
                        cloudRestorePassword = ""
                        cloudRestorePasswordVisible = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("دمج")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val file = state.selectedCloudFile!!
                        viewModel.restoreFromCloudFile(
                            file = file,
                            password = cloudRestorePassword,
                            mode = ImportMode.REPLACE
                        )
                        showCloudRestorePasswordDialog = false
                        cloudRestorePassword = ""
                        cloudRestorePasswordVisible = false
                    }) {
                        Text("استبدال", color = DangerRed)
                    }
                    TextButton(onClick = {
                        showCloudRestorePasswordDialog = false
                        cloudRestorePassword = ""
                        cloudRestorePasswordVisible = false
                    }) {
                        Text("إلغاء")
                    }
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
//              مكونات مساعدة
// ═══════════════════════════════════════════════════════════

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) color.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CloudFileRow(
    file: CloudFileInfo,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Cloud,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    "${file.sizeLabel()} • ${
                        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
                            .format(Date(file.lastModified))
                    }",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}