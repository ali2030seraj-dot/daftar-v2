package com.daftar.v2.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftar.v2.ui.theme.SuccessGreen

@Composable
fun UpdateDialog(
    state: UpdateState,
    currentVersionName: String,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        is UpdateState.UpdateAvailable -> {
            val info = state.info
            AlertDialog(
                onDismissRequest = {
                    if (!state.isMandatory) onDismiss()
                },
                icon = {
                    Icon(
                        Icons.Filled.SystemUpdate,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = if (state.isMandatory)
                            "تحديث إجباري متوفر"
                        else
                            "🎉 تحديث جديد متوفر",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "الإصدار الحالي:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Default
                            )
                            Text(
                                currentVersionName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Default
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "الإصدار الجديد:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Default
                            )
                            Text(
                                info.latestVersionName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                fontFamily = FontFamily.Default
                            )
                        }

                        if (info.apkSizeMb > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "حجم التحديث:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Default
                                )
                                Text(
                                    "%.1f MB".format(info.apkSizeMb),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Default
                                )
                            }
                        }

                        if (info.releaseNotes.isNotEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "الجديد في هذا الإصدار:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            )
                            Spacer(Modifier.height(6.dp))
                            info.releaseNotes.forEach { note ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("•", fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.size(6.dp))
                                    Text(
                                        note,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Default
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(
                            "🔒 بياناتك محفوظة — لن تُفقد عند التحديث.",
                            fontSize = 11.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Default
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onUpdate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen
                        )
                    ) {
                        Text(
                            "تحديث الآن",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                    }
                },
                dismissButton = if (!state.isMandatory) {
                    {
                        TextButton(onClick = onDismiss) {
                            Text("لاحقاً", fontFamily = FontFamily.Default)
                        }
                    }
                } else null
            )
        }

        is UpdateState.Downloading -> {
            AlertDialog(
                onDismissRequest = { },
                icon = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = SuccessGreen
                    )
                },
                title = {
                    Text(
                        "جاري تحميل التحديث...",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                },
                text = {
                    Text(
                        "ستصلك رسالة عند اكتمال التحميل، ثم اطلب تثبيت التحديث.",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Default
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("حسناً", fontFamily = FontFamily.Default)
                    }
                }
            )
        }

        is UpdateState.DownloadFailed -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text("فشل التحميل", fontFamily = FontFamily.Default)
                },
                text = {
                    Text(state.reason, fontFamily = FontFamily.Default)
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("حسناً", fontFamily = FontFamily.Default)
                    }
                }
            )
        }

        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text("خطأ", fontFamily = FontFamily.Default)
                },
                text = {
                    Text(state.message, fontFamily = FontFamily.Default)
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("حسناً", fontFamily = FontFamily.Default)
                    }
                }
            )
        }

        else -> Unit
    }
}