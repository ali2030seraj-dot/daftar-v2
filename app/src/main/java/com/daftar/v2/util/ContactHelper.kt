package com.daftar.v2.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log

/**
 * ⭐ أداة قراءة جهات الاتصال.
 */
data class ContactInfo(
    val name: String,
    val phone: String
)

object ContactHelper {

    private const val TAG = "ContactHelper"

    /**
     * ⭐ يقرأ بيانات جهة الاتصال من Uri.
     * يدعم كل الإصدارات وكل أنواع Uri.
     */
    fun readContact(context: Context, uri: Uri): ContactInfo? {
        return try {
            var name = ""
            var phone = ""

            // ⭐ القراءة الأساسية للاسم
            context.contentResolver.query(
                uri, null, null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    // جرب عدة أعمدة محتملة
                    val nameCandidates = listOf(
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        "display_name"
                    )
                    for (col in nameCandidates) {
                        val idx = cursor.getColumnIndex(col)
                        if (idx >= 0) {
                            name = cursor.getString(idx) ?: ""
                            if (name.isNotBlank()) break
                        }
                    }
                }
            }

            // ⭐ إذا لم نجد الاسم من Uri، نبحث عن طريق البيانات
            if (name.isBlank()) {
                // جرّب قراءة الاسم من الـ Data table
                context.contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    arrayOf(ContactsContract.Data.DISPLAY_NAME),
                    "${ContactsContract.Data.CONTACT_ID} = ?",
                    arrayOf(uri.lastPathSegment),
                    null
                )?.use { c ->
                    if (c.moveToFirst()) {
                        name = c.getString(0) ?: ""
                    }
                }
            }

            // ⭐ قراءة رقم الهاتف
            phone = readPhoneNumber(context, uri) ?: ""

            Log.d(TAG, "📇 name=$name, phone=$phone")

            if (name.isNotBlank() || phone.isNotBlank()) {
                ContactInfo(name = name, phone = phone)
            } else null

        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل قراءة جهة الاتصال", e)
            null
        }
    }

    /**
     * ⭐ يقرأ رقم الهاتف من عدة مصادر.
     */
    private fun readPhoneNumber(context: Context, uri: Uri): String? {
        // الطريقة 1: إذا كان الـ Uri لجهة اتصال
        try {
            // احصل على الـ contactId
            var contactId: String? = null
            context.contentResolver.query(
                uri, null, null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (idIdx >= 0) {
                        contactId = cursor.getString(idIdx)
                    }
                }
            }

            if (contactId != null) {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )?.use { pc ->
                    if (pc.moveToFirst()) {
                        val phoneIdx = pc.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        )
                        if (phoneIdx >= 0) {
                            val rawPhone = pc.getString(phoneIdx) ?: ""
                            return cleanPhone(rawPhone)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ فشل الطريقة 1: ${e.message}")
        }

        // الطريقة 2: إذا كان الـ Uri مباشرة لرقم هاتف
        try {
            context.contentResolver.query(
                uri, null, null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val phoneIdx = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                    if (phoneIdx >= 0) {
                        val rawPhone = cursor.getString(phoneIdx) ?: ""
                        return cleanPhone(rawPhone)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ فشل الطريقة 2: ${e.message}")
        }

        // الطريقة 3: من الـ Uri مباشرة (بعض الأجهزة)
        try {
            val path = uri.lastPathSegment ?: return null
            // إذا كان الـ path رقم هاتف مباشرة
            if (path.matches(Regex("[0-9+\\-\\s()]+"))) {
                return cleanPhone(path)
            }
        } catch (e: Exception) {
            // تجاهل
        }

        return null
    }

    /**
     * ⭐ تنظيف الرقم من الرموز غير المرغوبة.
     */
    private fun cleanPhone(raw: String): String {
        return raw.replace(Regex("[^0-9+]"), "").trim()
    }
}