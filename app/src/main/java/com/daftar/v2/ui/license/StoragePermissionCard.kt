package com.daftar.v2.ui.license

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ⭐ بطاقة تظهر عند عدم منح صلاحية التخزين.
 *
 * الميزات:
 *  - واضحة ومفهومة
 *  - زر واحد فقط
 *  - لا تُغلق إلا بعد منح الصلاحية
 */
@Composable
fun StoragePermissionCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFF3E0),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ⭐ رأس البطاقة
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE0B2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "صلاحية التخزين مطلوبة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBF360C),
                    fontFamily = FontFamily.Default
                )
            }

            // ⭐ الوصف
            Text(
                text = "لمنع تكرار الفترة التجريبية بعد حذف التطبيق، " +
                        "يحتاج التطبيق لصلاحية الوصول للملفات.",
                fontSize = 13.sp,
                color = Color(0xFF5D4037),
                fontFamily = FontFamily.Default,
                lineHeight = 20.sp,
                textAlign = TextAlign.Right
            )

            // ⭐ زر الفتح
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE65100)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "🔓 فتح الإعدادات لمنح الصلاحية",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Default
                )
            }

            // ⭐ ملاحظة صغيرة
            Text(
                text = "بعد الفتح، اضغط على \"السماح\" في أعلى الشاشة",
                fontSize = 11.sp,
                color = Color(0xFF8D6E63),
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Right
            )
        }
    }
}