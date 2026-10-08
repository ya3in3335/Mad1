package com.yourtech.systeme.admin

import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yourtech.systeme.admin.auth.AdminAuthRepository
import com.yourtech.systeme.admin.ui.AdminApp
import com.yourtech.systeme.designsystem.theme.YourTechTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdminActivity : ComponentActivity() {
    @Inject lateinit var auth: AdminAuthRepository
    private var backgroundedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        // Customer data and back-office screens never appear in screenshots, screen recordings or recents.
        if (secureWindow) window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent { YourTechTheme(rtl = true) { AdminApp() } }
    }

    override fun onStop() {
        super.onStop()
        backgroundedAt = SystemClock.elapsedRealtime()
    }

    override fun onStart() {
        super.onStart()
        if (backgroundedAt != 0L && SystemClock.elapsedRealtime() - backgroundedAt > AUTO_LOCK_MS) auth.logout()
    }

    companion object {
        private const val AUTO_LOCK_MS = 5 * 60 * 1000L
        /** Only test code turns this off (to capture review screenshots). */
        @JvmStatic var secureWindow = true
    }
}
