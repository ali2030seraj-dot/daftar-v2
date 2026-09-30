package com.daftar.v2.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.security.license.LicenseStatus
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.DangerRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    navController: NavHostController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var oldPassVisible by remember { mutableStateOf(false) }
    var newPassVisible by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isAuthenticated) {
        if (!state.isAuthenticated) {
            navController.popBackStack()
        }
    }

    AmbientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("لوحة الإدارة", fontWeight = FontWeight.Bold) },
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
                    ),
                    actions = {
                        TextButton(onClick = {
                            viewModel.logout()
                            navController.popBackStack()
                        }) {
                            Text("خروج")
                        }
                    }
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
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "حالة الترخيص",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            statusText(state.currentStatus),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "بصمة الجهاز",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.deviceFingerprint,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "كلمة مرور الإدارة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = viewModel::toggleChangePassword) {
                                Text(if (state.showChangePassword) "إلغاء" else "تغيير")
                            }
                        }

                        if (state.showChangePassword) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.oldPasswordInput,
                                onValueChange = viewModel::updateOldPassword,
                                label = { Text("كلمة المرور الحالية") },
                                visualTransformation = if (oldPassVisible)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { oldPassVisible = !oldPassVisible }) {
                                        Icon(
                                            imageVector = if (oldPassVisible)
                                                Icons.Filled.Visibility
                                            else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.newPasswordInput,
                                onValueChange = viewModel::updateNewPassword,
                                label = { Text("كلمة المرور الجديدة") },
                                visualTransformation = if (newPassVisible)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { newPassVisible = !newPassVisible }) {
                                        Icon(
                                            imageVector = if (newPassVisible)
                                                Icons.Filled.Visibility
                                            else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.confirmPasswordInput,
                                onValueChange = viewModel::updateConfirmPassword,
                                label = { Text("تأكيد كلمة المرور") },
                                visualTransformation = if (confirmPassVisible)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        confirmPassVisible = !confirmPassVisible
                                    }) {
                                        Icon(
                                            imageVector = if (confirmPassVisible)
                                                Icons.Filled.Visibility
                                            else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = viewModel::changePassword,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("حفظ")
                            }
                        }
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "أدوات متقدمة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "حذف كل التراخيص — استخدم فقط عند الحاجة القصوى.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = viewModel::resetAllLicenses,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DangerRed
                            )
                        ) {
                            Text("حذف كل التراخيص")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        GlassToast(
            message = state.message,
            isError = state.isError,
            onDismiss = { viewModel.clearMessage() }
        )
    }
}

private fun statusText(status: LicenseStatus?): String = when (status) {
    null -> "جاري التحميل..."
    is LicenseStatus.Trial -> "تجريبي — متبقي ${status.daysRemaining} يومًا"
    is LicenseStatus.TrialExpired -> "انتهت الفترة التجريبية"
    is LicenseStatus.Active -> "نشط — متبقي ${status.daysRemaining} يومًا"
    is LicenseStatus.ExpiringSoon -> "قارب على الانتهاء — ${status.daysRemaining} يومًا"
    is LicenseStatus.GracePeriod -> "فترة سماح — ${status.daysRemaining} يومًا"
    is LicenseStatus.Expired -> "منتهٍ"
    is LicenseStatus.Permanent -> "دائم"
    is LicenseStatus.Tampered -> "تلاعب في الوقت"
    is LicenseStatus.Error -> "خطأ: ${status.message}"
}