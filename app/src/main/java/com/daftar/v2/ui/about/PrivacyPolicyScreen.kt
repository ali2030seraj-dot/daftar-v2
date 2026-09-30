package com.daftar.v2.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: NavHostController
) {
    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("سياسة الخصوصية", fontWeight = FontWeight.Bold) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "سياسة الخصوصية",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "آخر تحديث: 17 سبتمبر 2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PolicySection(
                    title = "1. البيانات التي نجمعها",
                    body = "لا يجمع التطبيق أي بيانات شخصية. جميع البيانات التي تدخلها (الأسماء، الأرقام، المبالغ، الملاحظات، الصور، التسجيلات الصوتية) تُخزَّن محليًا على جهازك فقط."
                )

                PolicySection(
                    title = "2. استخدام البيانات",
                    body = "تُستخدم بياناتك فقط لعرضها لك داخل التطبيق. لا يتم إرسالها إلى أي سيرفر خارجي."
                )

                PolicySection(
                    title = "3. المشاركة مع أطراف ثالثة",
                    body = "لا يشارك التطبيق أي بيانات مع أي طرف ثالث. عند مشاركة إيصال أو كشف حساب، يحدث ذلك بمبادرتك أنت فقط عبر تطبيقات التواصل المثبتة على جهازك."
                )

                PolicySection(
                    title = "4. الأمان",
                    body = "تُخزَّن البيانات في قاعدة بيانات محلية على جهازك. يمكنك تفعيل قفل التطبيق بكلمة مرور لحماية وصول الآخرين."
                )

                PolicySection(
                    title = "5. الاتصال بالإنترنت",
                    body = "التطبيق يعمل بالكامل دون إنترنت. لا يطلب الاتصال إلا عند استخدام ميزة QR أو مشاركة الإيصالات عبر تطبيقات التواصل."
                )

                PolicySection(
                    title = "6. الفترة التجريبية والترخيص",
                    body = "يوفر التطبيق فترة تجريبية 30 يومًا. بعدها يحتاج إلى كود تفعيل يُصدره المطور. كود التفعيل مرتبط ببصمة الجهاز فقط ولا يحمل أي معلومات شخصية."
                )

                PolicySection(
                    title = "7. النسخ الاحتياطي",
                    body = "يمكنك تصدير نسخة احتياطية ببياناتك. تُحفظ النسخة على جهازك فقط — أنت المسؤول عن حفظها. لا يمكن لأي طرف ثالث الوصول إليها."
                )

                PolicySection(
                    title = "8. حقوقك",
                    body = "لك الحق الكامل في:\n• حذف أي بيانات من التطبيق.\n• تصدير نسخة من بياناتك.\n• إلغاء تثبيت التطبيق (يُحذف كل شيء)."
                )

                PolicySection(
                    title = "9. التغييرات على السياسة",
                    body = "قد نُحدّث هذه السياسة عند الحاجة. ستظهر النسخة المحدثة داخل التطبيق مع تاريخ التحديث."
                )

                PolicySection(
                    title = "10. التواصل",
                    body = "لأي استفسار حول الخصوصية:\n\nواتساب:  775670090"
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.6
            )
        }
    }
}