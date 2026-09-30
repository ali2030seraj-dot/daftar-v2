package com.daftar.v2.ui.receipt

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.DateFormatter
import com.daftar.v2.util.MoneyFormatter
import com.daftar.v2.util.QrCodeGenerator
import com.daftar.v2.util.ReceiptImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    navController: NavHostController,
    personId: Long,
    transactionId: Long,
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(personId, transactionId) {
        viewModel.load(personId, transactionId)
    }

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("إيصال السداد", fontWeight = FontWeight.Bold) },
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
            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else if (state.person == null || state.transaction == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text(state.message ?: "لا يمكن تحميل الإيصال") }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ReceiptCard(state)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ⭐ زر المشاركة
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        // ⭐⭐⭐ الإصلاح: نوع صريح
                                        val bitmap: Bitmap? = withContext(Dispatchers.Default) {
                                            ReceiptImageGenerator.generate(context, state)
                                        }
                                        if (bitmap == null) {
                                            Toast.makeText(
                                                context,
                                                "❌ فشل توليد الصورة",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@launch
                                        }
                                        shareBitmap(context, bitmap, state.receiptNumber)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(
                                            context,
                                            "فشل: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("مشاركة")
                        }

                        // ⭐ زر الحفظ
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        // ⭐⭐⭐ الإصلاح: نوع صريح
                                        val bitmap: Bitmap? = withContext(Dispatchers.Default) {
                                            ReceiptImageGenerator.generate(context, state)
                                        }
                                        if (bitmap == null) {
                                            Toast.makeText(
                                                context,
                                                "❌ فشل توليد الصورة",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@launch
                                        }
                                        val saved = saveBitmapToGallery(
                                            context,
                                            bitmap,
                                            state.receiptNumber
                                        )
                                        Toast.makeText(
                                            context,
                                            if (saved) "✅ تم الحفظ في المعرض"
                                            else "❌ فشل الحفظ",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(
                                            context,
                                            "فشل: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("حفظ")
                        }
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
}

// ═══════════════════════════════════════════════════════════
//              بطاقة الإيصال (للعرض في التطبيق)
// ═══════════════════════════════════════════════════════════

@Composable
private fun ReceiptCard(state: ReceiptUiState) {
    val person = state.person ?: return
    val tx = state.transaction ?: return

    val isPayment = state.isPayment
    val accentColor = if (isPayment) SuccessGreen else DangerRed

    val qrBitmap = remember(state.receiptNumber, person.name, tx.amountCents) {
        if (state.receiptNumber.isNotEmpty()) {
            val qrContent = buildQrContent(state, person.name, tx.amountCents)
            QrCodeGenerator.generate(qrContent, 320)
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ⭐ الشعار
            val logo = state.logoBitmap
            if (logo != null) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = logo.asImageBitmap(),
                        contentDescription = "الشعار",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isPayment) "✓" else "📌",
                        style = MaterialTheme.typography.headlineMedium,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                state.storeName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (state.storePhone.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    state.storePhone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.storeAddress.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    state.storeAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                if (isPayment) "إيصال سداد" else "إيصال دين",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))
            DashedDivider()
            Spacer(Modifier.height(14.dp))

            ReceiptRow("العميل", person.name)
            Spacer(Modifier.height(6.dp))
            ReceiptRow("التاريخ", DateFormatter.fullDualLabel(tx.timestamp))

            Spacer(Modifier.height(14.dp))
            DashedDivider()
            Spacer(Modifier.height(14.dp))

            ReceiptRow(
                "الرصيد السابق",
                MoneyFormatter.format(state.balanceBeforeCents, person.currency),
                muted = true
            )
            Spacer(Modifier.height(8.dp))

            ReceiptRow(
                if (isPayment) "المبلغ المسدد" else "المبلغ المضاف",
                MoneyFormatter.format(tx.amountCents, person.currency),
                valueColor = accentColor,
                bold = true
            )
            Spacer(Modifier.height(8.dp))

            ReceiptRow(
                "الرصيد المتبقي",
                MoneyFormatter.format(state.balanceAfterCents, person.currency),
                bold = true
            )

            if (tx.statement.isNotBlank() || tx.note.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                DashedDivider()
                Spacer(Modifier.height(14.dp))

                if (tx.statement.isNotBlank()) {
                    ReceiptRow("البيان", tx.statement, muted = true)
                    if (tx.note.isNotBlank()) Spacer(Modifier.height(6.dp))
                }
                if (tx.note.isNotBlank()) {
                    ReceiptRow("ملاحظات", tx.note, muted = true)
                }
            }

            Spacer(Modifier.height(18.dp))
            DashedDivider()
            Spacer(Modifier.height(14.dp))

            Text(
                "رقم الإيصال: ${state.receiptNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            qrBitmap?.let { bmp ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "QR",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "امسح للتحقق",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            DashedDivider()
            Spacer(Modifier.height(14.dp))

            Text(
                "شكرًا لتعاملكم معنا",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TertiaryAmber
            )
        }
    }
}

private fun buildQrContent(
    state: ReceiptUiState,
    personName: String,
    amountCents: Long
): String {
    val amountText = MoneyFormatter.format(amountCents, state.person?.currency ?: "")
    val dateText = DateFormatter.fullDualLabel(state.transaction?.timestamp ?: 0L)
    val remainingText = MoneyFormatter.format(
        state.balanceAfterCents,
        state.person?.currency ?: ""
    )
    return buildString {
        appendLine(state.storeName)
        appendLine("إيصال: ${state.receiptNumber}")
        appendLine("العميل: $personName")
        appendLine("المبلغ: $amountText")
        appendLine("المتبقي: $remainingText")
        appendLine("التاريخ: $dateText")
    }.trim()
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    muted: Boolean = false,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = if (bold) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor ?: if (muted)
                MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DashedDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

// ═══════════════════════════════════════════════════════════
//              المشاركة والحفظ
// ═══════════════════════════════════════════════════════════

private suspend fun shareBitmap(
    context: Context,
    bitmap: Bitmap,
    receiptNumber: String
) {
    withContext(Dispatchers.IO) {
        try {
            val file = saveBitmapToCache(context, bitmap, receiptNumber)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "إيصال $receiptNumber")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "مشاركة الإيصال"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}

private suspend fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    receiptNumber: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        val filename = "receipt_${receiptNumber}_${System.currentTimeMillis()}.png"
        val mimeType = "image/png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/دفتر حساباتي"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            ) ?: return@withContext false

            context.contentResolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: return@withContext false

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            true
        } else {
            @Suppress("DEPRECATION")
            val picturesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            val appDir = File(picturesDir, "دفتر حساباتي")
            if (!appDir.exists()) appDir.mkdirs()
            val file = File(appDir, filename)
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun saveBitmapToCache(
    context: Context,
    bitmap: Bitmap,
    receiptNumber: String
): File {
    val dir = File(context.cacheDir, "receipts")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "receipt_$receiptNumber.png")
    FileOutputStream(file).use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    return file
}