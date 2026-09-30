package com.daftar.v2.domain.audit

enum class AuditAction(val label: String) {
    PERSON_CREATED("إنشاء حساب"),
    PERSON_UPDATED("تعديل حساب"),
    PERSON_ARCHIVED("أرشفة حساب"),
    PERSON_RESTORED("استعادة حساب"),
    PERSON_DELETED("حذف حساب نهائيًا"),

    DEBT_ADDED("تسجيل مبلغ"),
    PAYMENT_ADDED("تسجيل سداد"),
    TRANSACTION_UPDATED("تعديل عملية"),
    TRANSACTION_VOIDED("إلغاء عملية"),
    TRANSACTION_DELETED("حذف عملية نهائيًا"),

    EXPENSE_CREATED("تسجيل مصروف"),
    EXPENSE_UPDATED("تعديل مصروف"),
    EXPENSE_VOIDED("إلغاء مصروف"),
    EXPENSE_DELETED("حذف مصروف نهائيًا"),

    CATEGORY_CREATED("إنشاء تصنيف"),
    CATEGORY_DELETED("حذف تصنيف"),

    PASSWORD_SET("تعيين كلمة مرور"),
    PASSWORD_CHANGED("تغيير كلمة مرور"),
    LOCK_ENABLED("تفعيل القفل"),
    LOCK_DISABLED("تعطيل القفل"),

    BACKUP_EXPORTED("تصدير نسخة احتياطية"),
    BACKUP_IMPORTED("استيراد نسخة احتياطية"),
    DATA_RESET("إعادة تعيين التطبيق")
}