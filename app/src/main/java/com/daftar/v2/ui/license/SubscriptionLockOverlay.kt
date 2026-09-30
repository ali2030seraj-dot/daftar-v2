package com.daftar.v2.ui.license

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftar.v2.security.license.LicenseStatus
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import kotlin.math.roundToInt

/**
 * ⭐ غطاء يُعرض فوق الشاشة عند انتهاء الترخيص.
 *
 * ⭐ الإصلاحات:
 *  - لا يعرض شيئاً عند إعادة التثبيت (البانر يتولى ذلك)
 *  - يستهلك كل اللمسات
 */
@Composable
fun SubscriptionLockOverlay(
    status: LicenseStatus?,
    deviceFingerprint: String,
    onActivateClick: () -> Unit,
    wasReinstalled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val developerWhatsApp = "967775670090"

    // ⭐⭐⭐ الإصلاح الأساسي:
    // إذا كان إعادة تثبيت → لا قفل (البانر يتولى)
    if (wasReinstalled) {
        return
    }

    // ⭐ الحالة العادية (قفل الترخيص)
    val isLocked = status is LicenseStatus.Expired ||
            status is LicenseStatus.TrialExpired ||
            status is LicenseStatus.Tampered

    if (!isLocked) return

    val title = when (status) {
        is LicenseStatus.TrialExpired -> "انتهت الفترة التجريبية"
        is LicenseStatus.Tampered -> "تم اكتشاف تلاعب في الوقت"
        else -> "انتهى الترخيص"
    }

    val subtitle = when (status) {
        is LicenseStatus.TrialExpired ->
            "للاستمرار في استخدام التطبيق، يرجى إدخال كود التفعيل أو التواصل مع المطور."
        is LicenseStatus.Tampered ->
            "يرجى ضبط تاريخ الجهاز بشكل صحيح، ثم إعادة فتح التطبيق."
        else ->
            "التطبيق الآن في وضع القراءة فقط. لا يمكن إضافة أو تعديل البيانات."
    }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                enabled = true,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        )
                )

                Spacer(Modifier.height(2.dp))

                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(52.dp)
                )

                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DangerRed,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Default
                )

                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Default
                )

                Spacer(Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بصمة هذا الجهاز",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Default
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = deviceFingerprint.ifBlank { "—" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onActivateClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    )
                ) {
                    Text(
                        "🔑 إدخال كود التفعيل",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Default
                    )
                }

                OutlinedButton(
                    onClick = {
                        val message = buildString {
                            appendLine("السلام عليكم،")
                            appendLine("أحتاج تفعيل تطبيق \"دفتر حساباتي\".")
                            appendLine()
                            appendLine("بصمة جهازي:")
                            appendLine(deviceFingerprint)
                            appendLine()
                            appendLine("أرجو إرسال كود التفعيل.")
                        }
                        val url = "https://wa.me/$developerWhatsApp?text=" +
                                Uri.encode(message)
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            )
                        } catch (_: Exception) { }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "📱 إرسال البصمة عبر واتساب",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Default
                    )
                }

                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(deviceFingerprint))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "📋 نسخ البصمة",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Default
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragIndicator,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = "اسحب البطاقة لتحريكها",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    }
}