package com.daftar.v2.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.util.voice.VoiceRecorder
import kotlinx.coroutines.delay

/**
 * نافذة تسجيل صوتي.
 */
@Composable
fun VoiceRecorderSheet(
    onSave: (path: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val recorder = remember { VoiceRecorder(context) }

    var isRecording by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var permissionGranted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (!granted) {
            errorMessage = "يجب السماح بالميكروفون للتسجيل"
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            permissionGranted = true
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(500)
            elapsedSeconds = recorder.elapsedSeconds()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorder.cancel()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (isRecording) recorder.cancel()
            onDismiss()
        },
        title = {
            Text("🎤 تسجيل ملاحظة صوتية", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                errorMessage?.let {
                    Text(it, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(10.dp))
                }

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            color = if (isRecording)
                                DangerRed.copy(alpha = 0.15f)
                            else
                                SuccessGreen.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = if (isRecording) DangerRed else SuccessGreen,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording)
                                Icons.Filled.Stop
                            else
                                Icons.Filled.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = when {
                        isRecording -> "🔴 جاري التسجيل..."
                        else -> "اضغط لبدء التسجيل"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRecording)
                        DangerRed
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                if (isRecording) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = formatTime(elapsedSeconds),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DangerRed
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "الحد الأقصى: 5 دقائق",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isRecording) {
                        val file = recorder.stop()
                        if (file != null) {
                            onSave(file.absolutePath)
                        } else {
                            errorMessage = "فشل حفظ التسجيل"
                        }
                        isRecording = false
                    } else {
                        if (!permissionGranted) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@Button
                        }
                        try {
                            recorder.start()
                            isRecording = true
                            elapsedSeconds = 0
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "فشل بدء التسجيل: ${e.message}"
                        }
                    }
                },
                enabled = permissionGranted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) SuccessGreen else DangerRed
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isRecording) "حفظ التسجيل" else "بدء التسجيل",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isRecording) recorder.cancel()
                onDismiss()
            }) {
                Text("إلغاء")
            }
        }
    )
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}