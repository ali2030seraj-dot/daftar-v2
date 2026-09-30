# ═══════════════════════════════════════════════════════════
#              ProGuard Rules — Daftar V2
#              ⚠️ لا تحذف أي قاعدة — قد يكسر التطبيق
# ═══════════════════════════════════════════════════════════

# ⭐ معلومات عامة
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions

# ═══════════════════════════════════════════════════════════
#              ⭐⭐⭐ الحماية الأمنية (لا تُشوّش!)
# ═══════════════════════════════════════════════════════════

-keep class com.daftar.v2.security.** { *; }
-keep class com.daftar.v2.security.license.** { *; }
-keep class com.daftar.v2.backup.BackupCrypto { *; }
-keep class com.daftar.v2.util.ShareHelper { *; }
-keep class com.daftar.v2.util.SmsHelper { *; }
-keep class com.daftar.v2.util.ContactHelper { *; }

-keepclassmembers class com.daftar.v2.security.** {
    private *;
    protected *;
    public *;
}

-repackageclasses ''
-allowaccessmodification
-optimizationpasses 5
-mergeinterfacesaggressively

# ═══════════════════════════════════════════════════════════
#              Kotlin
# ═══════════════════════════════════════════════════════════

-dontwarn kotlin.**
-dontwarn kotlinx.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-dontwarn kotlin.Unit
-dontwarn kotlin.jvm.internal.**
-dontwarn kotlin.reflect.**
-keep class kotlin.reflect.** { *; }

# ═══════════════════════════════════════════════════════════
#              Coroutines
# ═══════════════════════════════════════════════════════════

-keepclassmembernames class kotlinx.** { volatile <fields>; }
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**
-dontwarn kotlinx.coroutines.flow.**

# ═══════════════════════════════════════════════════════════
#              Hilt / Dagger
# ═══════════════════════════════════════════════════════════

-dontwarn dagger.**
-dontwarn javax.inject.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembers class * { @dagger.Provides <methods>; }
-keepclasseswithmembers class * { @dagger.Module <methods>; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }

# ⭐ Hilt — القواعد الجديدة الحرجة
-keep,allowobfuscation @interface dagger.hilt.android.HiltAndroidApp
-keep,allowobfuscation @interface dagger.hilt.android.AndroidEntryPoint
-keep,allowobfuscation @interface dagger.hilt.android.lifecycle.HiltViewModel

-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ═══════════════════════════════════════════════════════════
#              Room
# ═══════════════════════════════════════════════════════════

-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# ⭐ Entity / DAO للحماية
-keep class com.daftar.v2.data.entity.TimeGuardEntity { *; }
-keep class com.daftar.v2.data.entity.LicenseEntity { *; }
-keep class com.daftar.v2.data.entity.UsedLicenseEntity { *; }
-keep class com.daftar.v2.data.entity.SettingsEntity { *; }
-keep interface com.daftar.v2.data.db.dao.** { *; }

# ═══════════════════════════════════════════════════════════
#              Jetpack Compose
# ═══════════════════════════════════════════════════════════

-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.ui.platform.** { *; }
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}

# ═══════════════════════════════════════════════════════════
#              Lifecycle / ViewModel
# ═══════════════════════════════════════════════════════════

-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ═══════════════════════════════════════════════════════════
#              Sealed classes للحالة
# ═══════════════════════════════════════════════════════════

-keep class com.daftar.v2.security.license.LicenseStatus { *; }
-keep class com.daftar.v2.security.license.LicenseStatus$* { *; }
-keep class com.daftar.v2.security.license.LicensePlan { *; }
-keep class com.daftar.v2.security.license.ValidationOutcome { *; }
-keep class com.daftar.v2.security.license.ValidationOutcome$* { *; }

# ═══════════════════════════════════════════════════════════
#              Kotlinx Serialization
# ═══════════════════════════════════════════════════════════

-dontwarn kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.daftar.v2.**$$serializer { *; }
-keepclassmembers class com.daftar.v2.** { *** Companion; }
-keepclasseswithmembers class com.daftar.v2.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ═══════════════════════════════════════════════════════════
#              Gson
# ═══════════════════════════════════════════════════════════

-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ═══════════════════════════════════════════════════════════
#              ZXing (QR Codes)
# ═══════════════════════════════════════════════════════════

-dontwarn com.google.zxing.**
-keep class com.google.zxing.** { *; }

# ═══════════════════════════════════════════════════════════
#              Ed25519 (net.i2p.crypto)
# ═══════════════════════════════════════════════════════════

-dontwarn net.i2p.crypto.**
-keep class net.i2p.crypto.eddsa.** { *; }
-keep class net.i2p.crypto.eddsa.math.** { *; }
-keep class net.i2p.crypto.eddsa.spec.** { *; }

# ═══════════════════════════════════════════════════════════
#              OkHttp
# ═══════════════════════════════════════════════════════════

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ═══════════════════════════════════════════════════════════
#              AndroidX
# ═══════════════════════════════════════════════════════════

-keep class androidx.core.app.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.**
-keep class androidx.work.** { *; }
-keep class androidx.security.crypto.** { *; }
-keep class androidx.documentfile.** { *; }

# ═══════════════════════════════════════════════════════════
#              مكتبات Java المفقودة
# ═══════════════════════════════════════════════════════════

-dontwarn javax.annotation.**
-dontwarn javax.annotation.concurrent.**
-dontwarn javax.lang.model.**
-dontwarn javax.lang.model.element.**
-dontwarn javax.lang.model.type.**
-dontwarn javax.lang.model.util.**
-dontwarn javax.tools.**
-dontwarn javax.naming.**
-dontwarn javax.script.**
-dontwarn javax.xml.**
-dontwarn javax.xml.namespace.**
-dontwarn java.awt.**
-dontwarn java.awt.event.**
-dontwarn java.awt.geom.**
-dontwarn java.awt.image.**
-dontwarn java.beans.**
-dontwarn sun.misc.**
-dontwarn sun.reflect.**
-dontwarn org.w3c.dom.**
-dontwarn org.xml.sax.**
-dontwarn org.xmlpull.v1.**
-dontwarn org.apache.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.http.**
-dontwarn org.bouncycastle.**
-dontwarn org.bouncycastle.jce.**
-dontwarn org.bouncycastle.jce.provider.**
-dontwarn androidx.test.**
-dontwarn androidx.test.ext.**
-dontwarn junit.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.google.android.material.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn com.google.protobuf.**
-dontwarn kotlinx.atomicfu.**
-dontwarn kotlinx.coroutines.debug.**
-dontwarn kotlinx.coroutines.scheduling.**
-dontwarn coil.**
-dontwarn org.slf4j.**
-dontwarn org.antlr.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ═══════════════════════════════════════════════════════════
#              قواعد إضافية من missing_rules.txt
# ═══════════════════════════════════════════════════════════

-dontwarn com.google.api.client.http.GenericUrl
-dontwarn com.google.api.client.http.HttpHeaders
-dontwarn com.google.api.client.http.HttpRequest
-dontwarn com.google.api.client.http.HttpRequestFactory
-dontwarn com.google.api.client.http.HttpResponse
-dontwarn com.google.api.client.http.HttpTransport
-dontwarn com.google.api.client.http.javanet.NetHttpTransport$Builder
-dontwarn com.google.api.client.http.javanet.NetHttpTransport

-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.InlineMe
-dontwarn com.google.errorprone.annotations.RestrictedApi

-dontwarn org.joda.time.Instant
-dontwarn org.joda.time.**
-dontwarn org.joda.convert.**
-keep class org.joda.time.** { *; }

-dontwarn com.google.api.client.**
-dontwarn com.google.api.client.googleapis.**
-dontwarn com.google.api.client.googleapis.auth.**
-dontwarn com.google.api.client.googleapis.javanet.**
-dontwarn com.google.api.client.json.**
-dontwarn com.google.api.client.util.**
-dontwarn com.google.api.services.**

-dontwarn io.opencensus.**
-dontwarn io.opencensus.contrib.**
-dontwarn io.opencensus.stats.**
-dontwarn io.opencensus.tags.**
-dontwarn io.opencensus.trace.**

-dontwarn com.google.common.**
-dontwarn com.google.common.base.**
-dontwarn com.google.common.collect.**
-dontwarn com.google.common.util.concurrent.**
-dontwarn com.google.common.annotations.**

-dontwarn org.apache.http.**
-dontwarn org.apache.http.client.**
-dontwarn org.apache.http.conn.**
-dontwarn org.apache.http.impl.**
-dontwarn org.apache.http.message.**

-dontwarn org.apache.commons.**
-dontwarn org.apache.commons.codec.**
-dontwarn org.apache.commons.io.**
-dontwarn org.apache.commons.lang3.**

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }

-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ═══════════════════════════════════════════════════════════
#              حذف Logs في Release
# ═══════════════════════════════════════════════════════════

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}

# ═══════════════════════════════════════════════════════════
#              إخفاء أسماء الملفات
# ═══════════════════════════════════════════════════════════

-renamesourcefileattribute SourceFile

# ═══════════════════════════════════════════════════════════
#              Reflection
# ═══════════════════════════════════════════════════════════

-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}
-keep @androidx.annotation.Keep class * { *; }

# ═══════════════════════════════════════════════════════════
#              Enum / Parcelable
# ═══════════════════════════════════════════════════════════

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ═══════════════════════════════════════════════════════════
#              Serialization (Java)
# ═══════════════════════════════════════════════════════════

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}