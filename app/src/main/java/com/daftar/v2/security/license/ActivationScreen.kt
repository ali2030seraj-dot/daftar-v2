package com.daftar.v2.ui.license

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.security.license.LicenseStatus
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.util.WhatsAppOpener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(
    navController: NavHostController,
    viewModel: ActivationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    AmbientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("تفعيل التطبيق", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusCard(status = state.status)

                Spacer(Modifier.height(4.dp))

                DeviceIdCard(
                    fingerprint = state.deviceFingerprint,
                    onCopy = {
                        clipboard.setText(AnnotatedString(state.deviceFingerprint))
                    },
                    onShare = {
                        val msg = "أريد تفعيل تطبيق دفتر حساباتي.\n" +
                                "بصمة جهازي: ${state.deviceFingerprint}"
                        WhatsAppOpener.openWhatsApp(
                            context = context,
                            phone = "775670090",
                            message = msg
                        )
                    }
                )

                Spacer(Modifier.height(4.dp))

                ActivationCodeCard(
                    codeInput = state.codeInput,
                    onCodeChange = viewModel::updateCodeInput,
                    onActivate = viewModel::activate,
                    activating = state.activating,
                    message = state.message,
                    isError = state.isError
                )

                Spacer(Modifier.height(16.dp))
            }
        }

        GlassToast(
            message = state.message,
            isError = state.isError,
            onDismiss = { viewModel.clearMessage() }
        )

        LaunchedEffect(state.activationSuccess) {
            if (state.activationSuccess) {
                navController.popBackStack()
            }
        }
    }
}

@Composable
private fun StatusCard(status: LicenseStatus?) {
    val (title, description, color) = when (status) {
        null -> Triple("جاري التحميل", "يرجى الانتظار", MaterialTheme.colorScheme.onSurfaceVariant)
        is LicenseStatus.Trial -> Triple(
            "الفترة التجريبية",
            "متبقي ${status.daysRemaining} يومًا",
            MaterialTheme.colorScheme.primary
        )
        is LicenseStatus.TrialExpired -> Triple(
            "انتهت الفترة التجريبية",
            "الرجاء إدخال كود التفعيل",
            DangerRed
        )
        is LicenseStatus.Active -> Triple(
            "ترخيص نشط",
            "متبقي ${status.daysRemaining} يومًا",
            SuccessGreen
        )
        is LicenseStatus.ExpiringSoon -> Triple(
            "قارب على الانتهاء",
            "متبقي ${status.daysRemaining} يومًا",
            MaterialTheme.colorScheme.tertiary
        )
        is LicenseStatus.GracePeriod -> Triple(
            "فترة السماح",
            "متبقي ${status.daysRemaining} يومًا",
            MaterialTheme.colorScheme.tertiary
        )
        is LicenseStatus.Expired -> Triple(
            "انتهى الترخيص",
            "التطبيق في وضع القراءة فقط",
            DangerRed
        )
        is LicenseStatus.Permanent -> Triple(
            "ترخيص دائم",
            "لا يحتاج تجديد",
            SuccessGreen
        )
        is LicenseStatus.Tampered -> Triple(
            "اكتُشف تلاعب",
            "يرجى التواصل مع المطور",
            DangerRed
        )
        is LicenseStatus.Error -> Triple(
            "خطأ",
            status.message,
            DangerRed
        )
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeviceIdCard(
    fingerprint: String,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                "بصمة هذا الجهاز",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "أرسل هذه البصمة للمطور للحصول على كود التفعيل",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    fingerprint,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Spacer(Modifier.height(0.dp))
                    Text(" نسخ")
                }
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Text(" واتساب")
                }
            }
        }
    }
}

@Composable
private fun ActivationCodeCard(
    codeInput: String,
    onCodeChange: (String) -> Unit,
    onActivate: () -> Unit,
    activating: Boolean,
    message: String?,
    isError: Boolean
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                "كود التفعيل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "الصق الكود الذي أرسله لك المطور",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = codeInput,
                onValueChange = onCodeChange,
                placeholder = { Text("SA1-XXXXX-XXXXX-...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 6
            )

            message?.let {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        else
                            SuccessGreen.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(10.dp),
                        color = if (isError) DangerRed else SuccessGreen,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onActivate,
                enabled = !activating && codeInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (activating) "جاري التحقق..." else "تفعيل")
            }
        }
    }
}