package com.daftar.v2.ui.security

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.components.StaggeredAppear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    navController: NavHostController,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tokens = LocalAdaptiveTokens.current

    // ⭐ حالات كشف كلمة المرور
    var newPassVisible by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }
    var currentPassVisible by remember { mutableStateOf(false) }
    var changeNewPassVisible by remember { mutableStateOf(false) }
    var changeConfirmPassVisible by remember { mutableStateOf(false) }

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    AmbientBackground {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("الأمان وكلمة المرور", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = tokens.screenPaddingHorizontal,
                        vertical = tokens.screenPaddingVertical
                    ),
                verticalArrangement = Arrangement.spacedBy(tokens.cardSpacing)
            ) {
                StaggeredAppear(index = 0) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "تفعيل القفل",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = state.lockEnabled,
                                onCheckedChange = { viewModel.toggleLock(it) }
                            )
                        }
                    }
                }

                StaggeredAppear(index = 1) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "تعيين كلمة مرور جديدة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("كلمة المرور الجديدة") },
                                visualTransformation = if (newPassVisible)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
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
                            OutlinedTextField(
                                value = confirmPass,
                                onValueChange = { confirmPass = it },
                                label = { Text("تأكيد كلمة المرور") },
                                visualTransformation = if (confirmPassVisible)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
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
                            Button(
                                onClick = {
                                    viewModel.setPassword(newPass, confirmPass)
                                    newPass = ""
                                    confirmPass = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("حفظ كلمة المرور")
                            }
                        }
                    }
                }

                StaggeredAppear(index = 2) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "تغيير كلمة المرور",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = currentPass,
                                onValueChange = { currentPass = it },
                                label = { Text("كلمة المرور الحالية") },
                                visualTransformation = if (currentPassVisible)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        currentPassVisible = !currentPassVisible
                                    }) {
                                        Icon(
                                            imageVector = if (currentPassVisible)
                                                Icons.Filled.Visibility
                                            else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("كلمة المرور الجديدة") },
                                visualTransformation = if (changeNewPassVisible)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        changeNewPassVisible = !changeNewPassVisible
                                    }) {
                                        Icon(
                                            imageVector = if (changeNewPassVisible)
                                                Icons.Filled.Visibility
                                            else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = confirmPass,
                                onValueChange = { confirmPass = it },
                                label = { Text("تأكيد الجديدة") },
                                visualTransformation = if (changeConfirmPassVisible)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        changeConfirmPassVisible = !changeConfirmPassVisible
                                    }) {
                                        Icon(
                                            imageVector = if (changeConfirmPassVisible)
                                                Icons.Filled.Visibility
                                            else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                            Button(
                                onClick = {
                                    viewModel.changePassword(currentPass, newPass, confirmPass)
                                    currentPass = ""
                                    newPass = ""
                                    confirmPass = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("تغيير كلمة المرور")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }

        GlassToast(
            message = state.message,
            onDismiss = { viewModel.clearMessage() }
        )
    }
}