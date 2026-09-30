package com.daftar.v2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.daftar.v2.security.IntegrityChecker
import com.daftar.v2.ui.DaftarAppRoot
import com.daftar.v2.ui.theme.DaftarTheme
import com.daftar.v2.worker.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ⭐⭐⭐ فحص سلامة التطبيق (قبل أي شيء)
        if (!IntegrityChecker.isAppSafe(this)) {
            Log.e("MainActivity", "🚨 فشل فحص السلامة — إغلاق التطبيق")
            finishAffinity()
            kotlin.system.exitProcess(0)
            return
        }

        // ⭐ تحذير فقط لأجهزة root (لا يمنع التشغيل)
        if (IntegrityChecker.isDeviceRootedFull(this)) {
            Log.w("MainActivity", "⚠️ جهاز root مكتشف — تحذير فقط")
        }

        // ⭐ جدولة الأرشفة الشهرية
        WorkScheduler.scheduleMonthlyArchive(applicationContext)

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val darkMode by mainViewModel.darkMode.collectAsState()

            DaftarTheme(darkTheme = darkMode) {
                DaftarAppRoot()
            }
        }
    }
}