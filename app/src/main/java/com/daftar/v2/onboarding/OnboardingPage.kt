package com.daftar.v2.onboarding

import androidx.compose.ui.graphics.Color

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val accent: Color,
    val tips: List<String> = emptyList()
)

val onboardingPages = listOf(
    OnboardingPage(
        emoji = "👋",
        title = "أهلاً بك في دفتر حساباتي",
        description = "التطبيق الأذكى لإدارة أموالك وحساباتك مع الأشخاص.\n" +
                "سنشرح لك كل شيء في دقيقة واحدة فقط.",
        accent = Color(0xFF2563EB),
        tips = listOf(
            "📱 يعمل بدون إنترنت",
            "🔒 بياناتك محفوظة على جهازك",
            "⚡ سريع وبسيط"
        )
    ),
    OnboardingPage(
        emoji = "💱",
        title = "اختر عملتك الأساسية",
        description = "من الصفحة الرئيسية، اختر العملة التي تتعامل بها.\n" +
                "كل عملة لها حساباتها المستقلة.",
        accent = Color(0xFF0891B2),
        tips = listOf(
            "اضغط على أي عملة لتفعيلها",
            "تظهر بلون سماوي عند الاختيار",
            "الحسابات تُفصَل حسب العملة"
        )
    ),
    OnboardingPage(
        emoji = "👥",
        title = "أضف من لك ومن عليك",
        description = "سجّل الأشخاص الذين تتعامل معهم:\n" +
                "• \"ما لك\" = أشخاص لهم مبالغ عندك\n" +
                "• \"ما عليك\" = أشخاص عليك مبالغ لهم",
        accent = Color(0xFF7C3AED),
        tips = listOf(
            "أضف الاسم ورقم الهاتف (اختياري)",
            "حدّد العملة لكل شخص",
            "استخدم ملاحظة للتمييز عند تشابه الأسماء"
        )
    ),
    OnboardingPage(
        emoji = "📝",
        title = "سجّل العمليات بسهولة",
        description = "عند فتح حساب أي شخص، يمكنك تسجيل:\n" +
                "• دين جديد (أضفت مبلغ)\n" +
                "• سداد (استلمت مبلغ)",
        accent = Color(0xFF16A34A),
        tips = listOf(
            "استخدم زر ➕ في الأسفل",
            "أضف ملاحظة صوتية 🎤",
            "شارك كشف الحساب عبر PDF"
        )
    ),
    OnboardingPage(
        emoji = "📊",
        title = "تابع تقاريرك",
        description = "من قائمة \"المزيد\" يمكنك الوصول إلى:\n" +
                "• التقارير الشهرية والسنوية\n" +
                "• ملخص العملات\n" +
                "• الأرشيف والنسخ الاحتياطي",
        accent = Color(0xFFF59E0B),
        tips = listOf(
            "شارك التقارير عبر واتساب",
            "تابع التنبيهات على المديونيات",
            "انسخ بياناتك احتياطياً"
        )
    ),
    OnboardingPage(
        emoji = "🚀",
        title = "جاهز للبدء!",
        description = "أنت الآن مستعد لإدارة أموالك بذكاء.\n" +
                "ابدأ بإضافة أول شخص من قائمة \"ما لك\" أو \"ما عليك\".",
        accent = Color(0xFFDC2626),
        tips = emptyList()
    )
)