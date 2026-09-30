package com.daftar.v2.ui.branding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandingScreen(
    navController: NavHostController,
    viewModel: BrandingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var localName by remember(state.storeName) { mutableStateOf(state.storeName) }
    var localPhone by remember(state.storePhone) { mutableStateOf(state.storePhone) }
    var localAddress by remember(state.storeAddress) { mutableStateOf(state.storeAddress) }

    // ⭐⭐⭐ محرر الصورة
    var rotation by remember(state.logoBitmap) { mutableStateOf(0f) }
    var scale by remember(state.logoBitmap) { mutableStateOf(1f) }
    var offsetX by remember(state.logoBitmap) { mutableStateOf(0f) }
    var offsetY by remember(state.logoBitmap) { mutableStateOf(0f) }

    // ⭐ قائمة الأدوات المنسدلة
    var showTools by remember { mutableStateOf(false) }

    val hasChanges = localName != state.storeName ||
            localPhone != state.storePhone ||
            localAddress != state.storeAddress

    val hasLogoChanges = rotation != 0f || scale != 1f ||
            offsetX != 0f || offsetY != 0f

    // ⭐ إعادة تعيين التحويلات عند تغيير الصورة
    LaunchedEffect(state.logoBitmap) {
        rotation = 0f
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        showTools = false
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadLogo(it) }
    }

    fun resetTransforms() {
        rotation = 0f
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("هوية المتجر", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        if (hasChanges) {
                            IconButton(
                                onClick = {
                                    viewModel.saveAll(
                                        name = localName,
                                        phone = localPhone,
                                        address = localAddress
                                    )
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Save,
                                    contentDescription = "حفظ",
                                    tint = SuccessGreen
                                )
                            }
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ═══════════════════════════════════════════
                    //              الشعار الدائري
                    // ═══════════════════════════════════════════
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "شعار المتجر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (state.logoBitmap != null) "اسحب الصورة لضبط موضعها"
                                else "الصورة تُضبط تلقائياً لتملأ الدائرة",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(14.dp))

                            // ⭐ الإطار الدائري الأبيض
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.logoBitmap != null) {
                                    Image(
                                        bitmap = state.logoBitmap!!.asImageBitmap(),
                                        contentDescription = "الشعار",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                                rotationZ = rotation
                                                translationX = offsetX
                                                translationY = offsetY
                                            }
                                            .clip(CircleShape)
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    offsetX += dragAmount.x
                                                    offsetY += dragAmount.y
                                                    if (!showTools) showTools = true
                                                }
                                            },
                                        contentScale = ContentScale.Crop,
                                        alignment = Alignment.Center
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Filled.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(60.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "لا يوجد شعار",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // ⭐⭐⭐ قائمة الأدوات المنسدلة
                            if (state.logoBitmap != null) {
                                Button(
                                    onClick = { showTools = !showTools },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (showTools)
                                            MaterialTheme.colorScheme.surfaceVariant
                                        else
                                            MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Filled.Tune,
                                        contentDescription = null,
                                        tint = if (showTools)
                                            MaterialTheme.colorScheme.onSurface
                                        else
                                            Color.White
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (showTools) "إخفاء الأدوات"
                                        else "🎨 تعديل الصورة",
                                        fontWeight = FontWeight.Bold,
                                        color = if (showTools)
                                            MaterialTheme.colorScheme.onSurface
                                        else
                                            Color.White
                                    )
                                }

                                // القائمة المنسدلة
                                AnimatedVisibility(
                                    visible = showTools,
                                    enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                                    exit = shrinkVertically(tween(250)) + fadeOut(tween(250))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // الصف الأول: 45°
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            EditButton(
                                                emoji = "↺",
                                                label = "45° يسار",
                                                color = Color(0xFF8B5CF6),
                                                modifier = Modifier.weight(1f),
                                                onClick = { rotation = (rotation - 45f) % 360f }
                                            )
                                            EditButton(
                                                emoji = "↻",
                                                label = "45° يمين",
                                                color = Color(0xFF8B5CF6),
                                                modifier = Modifier.weight(1f),
                                                onClick = { rotation = (rotation + 45f) % 360f }
                                            )
                                        }

                                        // الصف الثاني: 90°
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            EditButton(
                                                emoji = "↺",
                                                label = "90° يسار",
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f),
                                                onClick = { rotation = (rotation - 90f) % 360f }
                                            )
                                            EditButton(
                                                emoji = "↻",
                                                label = "90° يمين",
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f),
                                                onClick = { rotation = (rotation + 90f) % 360f }
                                            )
                                        }

                                        // الصف الثالث: 180° + إعادة تعيين
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            EditButton(
                                                emoji = "🔄",
                                                label = "180°",
                                                color = Color(0xFFEA580C),
                                                modifier = Modifier.weight(1f),
                                                onClick = { rotation = (rotation + 180f) % 360f }
                                            )
                                            EditButton(
                                                emoji = "↺",
                                                label = "إعادة تعيين",
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.weight(1f),
                                                onClick = { resetTransforms() }
                                            )
                                        }

                                        // الصف الرابع: تصغير/تكبير
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            EditButton(
                                                emoji = "➖",
                                                label = "تصغير",
                                                color = SuccessGreen,
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    scale = (scale * 0.85f).coerceAtLeast(0.5f)
                                                }
                                            )
                                            EditButton(
                                                emoji = "➕",
                                                label = "تكبير",
                                                color = SuccessGreen,
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    scale = (scale * 1.15f).coerceAtMost(4f)
                                                }
                                            )
                                        }

                                        // ⭐ عرض الحالة
                                        Spacer(Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                        .copy(alpha = 0.4f)
                                                )
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "الزاوية: ${rotation.toInt()}°    " +
                                                        "•    التكبير: ${
                                                            String.format(
                                                                Locale.US,
                                                                "%.2f",
                                                                scale
                                                            )
                                                        }x    " +
                                                        "•    الإزاحة: (${offsetX.toInt()}, ${
                                                            offsetY.toInt()
                                                        })",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        // ⭐ زر حفظ التعديلات
                                        if (hasLogoChanges) {
                                            Spacer(Modifier.height(6.dp))
                                            Button(
                                                onClick = {
                                                    viewModel.saveTransformedLogo(
                                                        rotation = rotation,
                                                        scale = scale,
                                                        offsetX = offsetX,
                                                        offsetY = offsetY
                                                    )
                                                    showTools = false
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = SuccessGreen
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Filled.Save,
                                                    contentDescription = null
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    "حفظ تعديلات الشعار",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                            }

                            // ⭐ أزرار اختيار/حذف الصورة
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { imagePicker.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Filled.Image, contentDescription = null)
                                    Spacer(Modifier.size(6.dp))
                                    Text(
                                        if (state.logoBitmap == null) "اختر صورة" else "تغيير"
                                    )
                                }

                                if (state.logoBitmap != null) {
                                    OutlinedButton(
                                        onClick = {
                                            resetTransforms()
                                            viewModel.deleteLogo()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = DangerRed
                                        )
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "حذف")
                                    }
                                }
                            }
                        }
                    }

                    // ═══════════════════════════════════════════
                    //              بيانات المتجر
                    // ═══════════════════════════════════════════
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth()) {
                            Text(
                                "بيانات المتجر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = localName,
                                onValueChange = { localName = it },
                                label = { Text("اسم المتجر") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    textAlign = TextAlign.Right,
                                    textDirection = TextDirection.Rtl,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )

                            Spacer(Modifier.height(10.dp))

                            OutlinedTextField(
                                value = localPhone,
                                onValueChange = { localPhone = it },
                                label = { Text("هاتف المتجر") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    textAlign = TextAlign.Right,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                )
                            )

                            Spacer(Modifier.height(10.dp))

                            OutlinedTextField(
                                value = localAddress,
                                onValueChange = { localAddress = it },
                                label = { Text("عنوان المتجر") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    textAlign = TextAlign.Right,
                                    textDirection = TextDirection.Rtl,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                )
                            )

                            if (hasChanges) {
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        viewModel.saveAll(
                                            name = localName,
                                            phone = localPhone,
                                            address = localAddress
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SuccessGreen
                                    )
                                ) {
                                    Icon(Icons.Filled.Save, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("حفظ التغييرات", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // ═══════════════════════════════════════════
                    //              معاينة
                    // ═══════════════════════════════════════════
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "معاينة",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))

                            if (state.logoBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(3.dp)
                                        .clip(CircleShape)
                                ) {
                                    Image(
                                        bitmap = state.logoBitmap!!.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                                rotationZ = rotation
                                                translationX = offsetX * (70f / 240f)
                                                translationY = offsetY * (70f / 240f)
                                            }
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        alignment = Alignment.Center
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                            }

                            Text(
                                localName.ifBlank { "دفتر حساباتي" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (localPhone.isNotBlank()) {
                                Text(
                                    localPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (localAddress.isNotBlank()) {
                                Text(
                                    localAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
//              زر تعديل مخصص
// ═══════════════════════════════════════════════════════════

@Composable
private fun EditButton(
    emoji: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontFamily = FontFamily.Default
        )
    }
}