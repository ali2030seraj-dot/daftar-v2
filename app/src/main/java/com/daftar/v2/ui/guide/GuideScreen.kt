package com.daftar.v2.ui.guide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.StaggeredAppear

// ═══════════════════════════════════════════════════════════
//              نموذج بيانات الأقسام
// ═══════════════════════════════════════════════════════════

data class GuideSection(
    val emoji: String,
    val title: String,
    val accent: Color,
    val steps: List<GuideStep>
)

data class GuideStep(
    val title: String,
    val description: String
)

private val guideSections = listOf(

    // ⭐⭐⭐ 1) الميزات التلقائية (الجديد — الأهم)
    GuideSection(
        emoji = "🚀",
        title = "الميزات التلقائية (تعمل عند بدء التطبيق)",
        accent = Color(0xFF0EA5E9),
        steps = listOf(
            GuideStep(
                "فحص التحديثات تلقائياً",
                "عند فتح التطبيق، يتصل بسيرفر GitHub ليتحقق من وجود إصدار جديد. " +
                "إذا وُجد تحديث، يظهر حوار مع التفاصيل وزر التحميل."
            ),
            GuideStep(
                "الفترة التجريبية (60 يوم)",
                "عند أول تشغيل، تبدأ فترة تجريبية 60 يوماً. " +
                "ستظهر شارة \"تجربة\" في الأعلى مع عدد الأيام المتبقية."
            ),
            GuideStep(
                "قفل التطبيق تلقائياً",
                "إذا فعّلت القفل من الأمان، سيُطلب منك البصمة أو الرمز عند كل فتح للتطبيق."
            ),
            GuideStep(
                "النسخ الاحتياطي التلقائي اليومي",
                "التطبيق يُنشئ نسخة احتياطية تلقائياً كل يوم في مجلد داخلي. " +
                "يحتفظ بآخر 7 نسخ، ويحذف الأقدم تلقائياً."
            ),
            GuideStep(
                "النسخ السحابي التلقائي",
                "إذا فعّلت النسخ السحابي من إعدادات النسخ، سيُرفع تلقائياً كل 24 ساعة إلى Google Drive."
            ),
            GuideStep(
                "المعاملات المتكررة",
                "المعاملات التي فعّلت عليها \"🔄 تكرار تلقائي\" تُنفَّذ تلقائياً: " +
                "شهرياً، أسبوعياً، أو سنوياً في نفس اليوم."
            ),
            GuideStep(
                "التنبيهات المجدولة",
                "التطبيق يفحص الديون المتأخرة ويعرض تنبيهات في مواعيد محددة."
            ),
            GuideStep(
                "طلب الصلاحيات",
                "عند أول تشغيل، سيطلب الإذن للوصول إلى: الإشعارات، التخزين، وجهات الاتصال."
            ),
            GuideStep(
                "شاشة الترحيب (Onboarding)",
                "عند أول تشغيل فقط، تظهر 3 شاشات ترحيبية تشرح الميزات الأساسية."
            ),
            GuideStep(
                "تفعيل قنوات الإشعارات",
                "التطبيق يُنشئ تلقائياً قنوات الإشعارات (للديون، للسحابة، للترخيص)."
            )
        )
    ),

    // ⭐⭐⭐ 2) الميزات الحديثة
    GuideSection(
        emoji = "✨",
        title = "الميزات الحديثة",
        accent = Color(0xFF9333EA),
        steps = listOf(
            GuideStep(
                "🔄 المعاملات المتكررة",
                "عند تسجيل دين أو دفعة، فعّل خيار \"🔄 تكرار تلقائي\" واختر: " +
                "أسبوعي / شهري / سنوي. سيُسجَّل تلقائياً في نفس اليوم."
            ),
            GuideStep(
                "📊 التقارير المتقدمة",
                "من المزيد ← التقارير المتقدمة. تحتوي على 3 أقسام:\n" +
                "• أعمار الديون (0-30، 31-60، 61-90، +90 يوم)\n" +
                "• التدفق النقدي (6 أشهر)\n" +
                "• المقارنة الشهرية"
            ),
            GuideStep(
                "📱 قوالب الرسائل (SMS)",
                "من المزيد ← قوالب الرسائل. يمكنك تعديل نصوص SMS:\n" +
                "• تسجيل دين جديد\n" +
                "• استلام دفعة\n" +
                "• تذكير بالدين\n" +
                "المتغيرات: {name}, {amount}, {remaining}"
            ),
            GuideStep(
                "🧾 الفواتير",
                "من المزيد ← الفواتير. أنشئ فاتورة احترافية بـ:\n" +
                "• بنود متعددة\n" +
                "• ضريبة وخصم\n" +
                "• PDF عربي احترافي\n" +
                "• QR Code للتحقق"
            ),
            GuideStep(
                "☁️ النسخ الاحتياطي السحابي",
                "من المزيد ← النسخ الاحتياطي ← قسم ☁️:\n" +
                "• اختر مجلد Google Drive\n" +
                "• حدد كلمة مرور\n" +
                "• فعّل النسخ التلقائي أو بإذنك"
            ),
            GuideStep(
                "📇 اختيار من جهات الاتصال",
                "عند إضافة عميل جديد، اضغط أيقونة 📇 بجانب حقل الاسم " +
                "لاختيار من جهات الاتصال مع جلب الاسم والهاتف تلقائياً."
            ),
            GuideStep(
                "📱 إرسال SMS / 💬 واتساب",
                "من قائمة الأشخاص، اضغط ⋮ على أي عميل له رقم:\n" +
                "• 📱 رسالة SMS — يفتح تطبيق الرسائل مع نص جاهز\n" +
                "• 💬 واتساب — يفتح واتساب مع نص جاهز\n" +
                "الرسالة توضح تلقائياً: هل هو مدين لك أم أنت مدين له"
            )
        )
    ),

    // ⭐ 3) البداية السريعة (الموجود سابقاً)
    GuideSection(
        emoji = "🏠",
        title = "البداية السريعة",
        accent = Color(0xFF2563EB),
        steps = listOf(
            GuideStep(
                "الخطوة 1: اختر عملتك",
                "من الصفحة الرئيسية، اختر العملة التي تتعامل بها من شريط \"عملتك الأساسية\". ستظهر بالذهبي مع علامة ✓."
            ),
            GuideStep(
                "الخطوة 2: أضف من لك",
                "اضغط على \"ما لك\" ← زر ➕ ← أدخل اسم الشخص والهاتف (اختياري) والعملة. هذا لمن لهم مبالغ عندك."
            ),
            GuideStep(
                "الخطوة 3: أضف من عليك",
                "نفس الشي مع \"ما عليك\" — لمن عليك مبالغ لهم."
            ),
            GuideStep(
                "الخطوة 4: سجّل العمليات",
                "افتح حساب أي شخص ← اضغط ➕ ← اختر \"إضافة مبلغ\" (دين) أو \"تسجيل سداد\"."
            )
        )
    ),

    // ⭐ 4) العملات
    GuideSection(
        emoji = "💱",
        title = "العملات المتعددة",
        accent = Color(0xFFF59E0B),
        steps = listOf(
            GuideStep(
                "تغيير العملة الأساسية",
                "من الصفحة الرئيسية، اضغط على أي عملة في شريط العملات. " +
                "أو من الجير ⚙️ ← اختر العملة من الشبكة."
            ),
            GuideStep(
                "العملات المدعومة",
                "6 عملات: الريال اليمني، السعودي، العماني، الدرهم الإماراتي، الدولار، والجنيه المصري."
            ),
            GuideStep(
                "فصل الحسابات حسب العملة",
                "كل شخص له عملة محددة. الإحصائيات في الصفحة الرئيسية تعرض فقط عملتك الحالية. " +
                "لرؤية عملة أخرى، اخترها من الشريط."
            ),
            GuideStep(
                "التقارير بكل العملات",
                "التقارير الشهرية والسنوية تعرض كل عملة في بطاقة منفصلة. لا تختلط الأرقام أبداً."
            )
        )
    ),

    // ⭐ 5) إدارة الأشخاص
    GuideSection(
        emoji = "👥",
        title = "إدارة الأشخاص",
        accent = Color(0xFF7C3AED),
        steps = listOf(
            GuideStep(
                "إضافة شخص جديد",
                "من قائمة \"ما لك\" أو \"ما عليك\" ← اضغط ➕ ← أدخل الاسم والهاتف والعملة."
            ),
            GuideStep(
                "⭐ إضافة من جهات الاتصال",
                "عند إضافة عميل، اضغط أيقونة 📇 بجانب حقل الاسم، " +
                "أو زر \"اختيار من جهات الاتصال\" أسفل الحقل."
            ),
            GuideStep(
                "البحث السريع",
                "في أعلى القائمة، اكتب أي حرف من الاسم أو الهاتف — تُفلتر القائمة فوراً."
            ),
            GuideStep(
                "التعديل والأرشفة",
                "اضغط على النقاط الثلاث (⋮) بجانب أي شخص ← اختر \"تعديل\" أو \"أرشفة\"."
            ),
            GuideStep(
                "⭐ التذكير عبر واتساب/SMS",
                "من قائمة الشخص ← النقاط الثلاث ← اختر:\n" +
                "• 💬 واتساب\n" +
                "• 📱 رسالة SMS\n" +
                "الرسالة توضح الاتجاه تلقائياً (لك/عليك)."
            ),
            GuideStep(
                "الاتصال المباشر",
                "إذا كان للشخص رقم هاتف، يمكنك الاتصال مباشرة من النقاط الثلاث ← \"📞 اتصال\"."
            ),
            GuideStep(
                "حالة الحساب",
                "كل شخص تظهر بجانبه شارة: \"مسدَّد\" (أخضر) أو \"قائم\" (برتقالي)."
            )
        )
    ),

    // ⭐ 6) تسجيل العمليات
    GuideSection(
        emoji = "📝",
        title = "تسجيل العمليات",
        accent = Color(0xFF16A34A),
        steps = listOf(
            GuideStep(
                "إضافة دين (مبلغ)",
                "افتح كشف حساب الشخص ← ➕ ← \"إضافة مبلغ\" ← أدخل البيان والمبلغ ← احفظ."
            ),
            GuideStep(
                "تسجيل سداد",
                "نفس الشي لكن اختر \"تسجيل سداد\" — لتسجيل ما استلمته أو دفعته."
            ),
            GuideStep(
                "⭐ تكرار تلقائي",
                "عند إضافة مبلغ أو دفعة، فعّل \"🔄 تكرار تلقائي\" واختر:\n" +
                "• أسبوعي\n" +
                "• شهري\n" +
                "• سنوي\n" +
                "سيُعاد تلقائياً في نفس اليوم."
            ),
            GuideStep(
                "تعديل عملية",
                "من كشف الحساب ← اضغط ⋮ على العملية ← \"✏️ تعديل\" ← عدّل ← احفظ."
            ),
            GuideStep(
                "حذف عملية",
                "من كشف الحساب ← اضغط ⋮ على العملية ← \"🗑️ حذف\"."
            ),
            GuideStep(
                "ملاحظة صوتية",
                "من كشف الحساب ← ⋮ على العملية ← \"🎤 تسجيل ملاحظة\" — سجّل صوتك مع العملية."
            ),
            GuideStep(
                "إيصال السداد",
                "من كشف الحساب ← ⋮ على العملية ← \"📄 إيصال السداد\" — يُنشئ PDF للإيصال."
            )
        )
    ),

    // ⭐ 7) التقارير
    GuideSection(
        emoji = "📊",
        title = "التقارير",
        accent = Color(0xFF0891B2),
        steps = listOf(
            GuideStep(
                "التقارير الأساسية",
                "من الإجراءات السريعة ← \"تقارير\" ← اختر شهري أو سنوي."
            ),
            GuideStep(
                "التقرير الشهري",
                "يعرض تفاصيل شهر معين مع مقارنة بالأشهر السابقة، لكل عملة على حدة."
            ),
            GuideStep(
                "التقرير السنوي",
                "عرض كامل للسنة مع توزيع شهري ورسم بياني (12 شهر) لكل عملة."
            ),
            GuideStep(
                "⭐ التقارير المتقدمة",
                "من المزيد ← التقارير المتقدمة. تحتوي على:\n" +
                "• ⏳ أعمار الديون (4 فئات ملونة)\n" +
                "• 💵 التدفق النقدي (6 أشهر)\n" +
                "• 📈 المقارنة الشهرية (بالنسب %)"
            ),
            GuideStep(
                "تصدير PDF",
                "من داخل أي تقرير ← اضغط 📄 PDF — يُحفظ في التنزيلات."
            ),
            GuideStep(
                "مشاركة التقرير",
                "من داخل التقرير ← اضغط 🔗 مشاركة — يُرسل كنص."
            )
        )
    ),

    // ⭐ 8) الفواتير
    GuideSection(
        emoji = "🧾",
        title = "الفواتير",
        accent = Color(0xFFEA580C),
        steps = listOf(
            GuideStep(
                "الوصول للفواتير",
                "من المزيد ← الفواتير."
            ),
            GuideStep(
                "إنشاء فاتورة جديدة",
                "اضغط ➕ فاتورة جديدة ← اختر العميل ← أضف البنود (البيان، الكمية، السعر)."
            ),
            GuideStep(
                "الضريبة والخصم",
                "يمكنك إضافة نسبة ضريبة أو مبلغ خصم قبل الإجمالي."
            ),
            GuideStep(
                "حفظ الفاتورة",
                "اضغط 💾 حفظ الفاتورة — تُخزَّن في القائمة."
            ),
            GuideStep(
                "⭐ مشاركة PDF عربي احترافي",
                "من صفحة عرض الفاتورة ← 📤 مشاركة — يُنشئ PDF احترافي بالعربية مع:\n" +
                "• الشعار الدائري\n" +
                "• اسم المتجر\n" +
                "• جدول منظم\n" +
                "• تنسيق RTL كامل"
            ),
            GuideStep(
                "تعليم كمدفوعة",
                "من صفحة عرض الفاتورة ← ✅ تعليم كمدفوعة."
            )
        )
    ),

    // ⭐ 9) المصروفات
    GuideSection(
        emoji = "💰",
        title = "المصروفات",
        accent = Color(0xFFF97316),
        steps = listOf(
            GuideStep(
                "تسجيل مصروف",
                "من الإجراءات السريعة ← \"مصروف\" ← أدخل البيان والمبلغ والعملة ← احفظ."
            ),
            GuideStep(
                "الوصول لشاشة المصروفات",
                "من الصفحة الرئيسية ← اضغط على بطاقة \"المصروفات\" في الوضع المالي."
            ),
            GuideStep(
                "تعديل مصروف",
                "من قائمة المصروفات ← اضغط ✏️ بجانب المصروف."
            ),
            GuideStep(
                "قوالب المصروفات",
                "من المزيد ← \"قوالب المصروفات\" — إيجار، كهرباء، إنترنت... إلخ."
            )
        )
    ),

    // ⭐ 10) الأمان والنسخ الاحتياطي
    GuideSection(
        emoji = "🔐",
        title = "الأمان والنسخ الاحتياطي",
        accent = Color(0xFF64748B),
        steps = listOf(
            GuideStep(
                "قفل التطبيق",
                "من المزيد ← \"الأمان وكلمة المرور\" ← فعّل قفل التطبيق بالبصمة أو الرمز."
            ),
            GuideStep(
                "نسخة احتياطية محلية",
                "من المزيد ← النسخ الاحتياطي ← قسم 📱:\n" +
                "• تصدير نسخة (اسم متسلسل تلقائي: 001، 002...)\n" +
                "• أو اكتب اسماً مخصصاً"
            ),
            GuideStep(
                "⭐ النسخ الاحتياطي السحابي",
                "من المزيد ← النسخ الاحتياطي ← قسم ☁️:\n" +
                "• اختر مجلد Google Drive\n" +
                "• أدخل كلمة مرور (6 أحرف)\n" +
                "• اختر الوضع: معطل / بإذن / تلقائي\n" +
                "• اضغط \"نسخ الآن\""
            ),
            GuideStep(
                "⭐ استرداد من السحابة",
                "من قسم ☁️ ← اضغط \"استرداد\" ← تظهر قائمة الملفات:\n" +
                "• اختر ملفاً ← أدخل كلمة المرور\n" +
                "• اختر: دمج / استبدال"
            ),
            GuideStep(
                "⭐ تصدير من السحابة للجهاز",
                "من قائمة الملفات ← اختر ملفاً ← \"💾 تصدير للجهاز\"."
            ),
            GuideStep(
                "سجل التعديلات",
                "من المزيد ← \"سجل التغييرات\" — يعرض كل العمليات السابقة."
            )
        )
    ),

    // ⭐ 11) قوالب الرسائل
    GuideSection(
        emoji = "📱",
        title = "قوالب الرسائل (SMS)",
        accent = Color(0xFF14B8A6),
        steps = listOf(
            GuideStep(
                "الوصول للقوالب",
                "من المزيد ← قوالب الرسائل."
            ),
            GuideStep(
                "القوالب المتاحة",
                "3 قوالب:\n" +
                "• 📝 تسجيل دين جديد\n" +
                "• 💵 استلام دفعة\n" +
                "• 🔔 تذكير بالدين"
            ),
            GuideStep(
                "تعديل قالب",
                "اضغط \"✏️ تعديل\" على أي قالب ← اكتب النص الجديد ← احفظ."
            ),
            GuideStep(
                "المتغيرات المتاحة",
                "يمكنك استخدام:\n" +
                "• {name} — اسم العميل\n" +
                "• {amount} — المبلغ\n" +
                "• {remaining} — المتبقي\n" +
                "• {statement} — البيان"
            ),
            GuideStep(
                "استرجاع الافتراضي",
                "إذا خربطت، اضغط \"🔄 افتراضي\" لاسترجاع النص الأصلي."
            )
        )
    ),

    // ⭐ 12) الإعدادات السريعة
    GuideSection(
        emoji = "⚙️",
        title = "الإعدادات السريعة",
        accent = Color(0xFF8B5CF6),
        steps = listOf(
            GuideStep(
                "الوصول للإعدادات",
                "اضغط على أيقونة الجير ⚙️ في أعلى يمين الصفحة الرئيسية."
            ),
            GuideStep(
                "هوية المتجر",
                "من الإعدادات السريعة ← \"هوية المتجر\" ← أضف شعارك واسمك وبياناتك. " +
                "تظهر في كل الفواتير والتقارير."
            ),
            GuideStep(
                "الوضع الليلي",
                "من الإعدادات السريعة ← فعّل \"الوضع الليلي\"."
            ),
            GuideStep(
                "العملة الافتراضية",
                "من الإعدادات السريعة ← اختر العملة من الشبكة."
            ),
            GuideStep(
                "تفعيل التطبيق",
                "من الإجراءات السريعة ← \"تفعيل\" — لإدخال كود التفعيل."
            )
        )
    ),

    // ⭐ 13) نصائح احترافية
    GuideSection(
        emoji = "🎨",
        title = "نصائح احترافية",
        accent = Color(0xFFF43F5E),
        steps = listOf(
            GuideStep(
                "ابدأ بهوية المتجر",
                "أول ما تثبّت التطبيق، اضبط هوية المتجر. ستظهر في كل PDF."
            ),
            GuideStep(
                "فعّل النسخ السحابي",
                "لحماية بياناتك، فعّل النسخ السحابي إلى Google Drive."
            ),
            GuideStep(
                "استخدم التقارير المتقدمة",
                "لرؤية أعمار الديون، والتدفق النقدي، والمقارنة الشهرية."
            ),
            GuideStep(
                "قوالب الرسائل المخصصة",
                "خصّص نصوص SMS بما يناسب نشاطك."
            ),
            GuideStep(
                "جرّب الفواتير",
                "أنشئ فاتورة احترافية بـ PDF عربي، وشاركها عبر واتساب."
            )
        )
    )
)

// ═══════════════════════════════════════════════════════════
//              شاشة الدليل
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    navController: NavHostController
) {
    var searchQuery by remember { mutableStateOf("") }
    val expandedStates = remember { mutableStateListOf(*Array(guideSections.size) { false }) }

    val filteredSections = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            guideSections.mapIndexed { index, section -> index to section }
        } else {
            val q = searchQuery.trim().lowercase()
            guideSections.mapIndexed { index, section -> index to section }
                .filter { (_, section) ->
                    section.title.lowercase().contains(q) ||
                    section.steps.any { step ->
                        step.title.lowercase().contains(q) ||
                        step.description.lowercase().contains(q)
                    }
                }
        }
    }

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📚", fontSize = 18.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "دليل الاستخدام",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                GuideHeroCard()

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("🔍 ابحث في الدليل...", fontSize = 13.sp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }

                if (filteredSections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "لا توجد نتائج",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "جرّب كلمة بحث أخرى",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    filteredSections.forEachIndexed { displayIndex, (originalIndex, section) ->
                        StaggeredAppear(index = displayIndex) {
                            ExpandableGuideSection(
                                section = section,
                                isExpanded = expandedStates[originalIndex],
                                onToggle = {
                                    expandedStates[originalIndex] = !expandedStates[originalIndex]
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              Hero Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun GuideHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F1F4A), Color(0xFF1E3A8A))
                )
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📖", fontSize = 26.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "دليل الاستخدام",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "13 قسم شامل — من البداية للاحتراف",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//              قسم قابل للتوسيع
// ═══════════════════════════════════════════════════════════

@Composable
private fun ExpandableGuideSection(
    section: GuideSection,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(250),
        label = "chevron"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(section.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(section.emoji, fontSize = 18.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Black,
                        color = section.accent,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${section.steps.size} خطوات",
                        fontFamily = FontFamily.Default,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "طي" else "توسيع",
                    tint = section.accent,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(250)
                ) + fadeIn(animationSpec = tween(250)),
                exit = shrinkVertically(
                    animationSpec = tween(250)
                ) + fadeOut(animationSpec = tween(250))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(section.accent.copy(alpha = 0.15f))
                    )

                    Spacer(Modifier.height(4.dp))

                    section.steps.forEachIndexed { index, step ->
                        GuideStepItem(
                            step = step,
                            stepNumber = index + 1,
                            accent = section.accent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideStepItem(
    step: GuideStep,
    stepNumber: Int,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                fontSize = 11.sp,
                color = accent,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = step.description,
                fontFamily = FontFamily.Default,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}