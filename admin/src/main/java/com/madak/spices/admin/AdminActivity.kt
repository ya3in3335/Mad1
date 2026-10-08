package com.madak.spices.admin

import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.madak.spices.admin.auth.AdminAuthRepository
import com.madak.spices.admin.ui.AdminApp
import com.madak.spices.designsystem.theme.MadakTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdminActivity : ComponentActivity() {
    @Inject lateinit var auth: AdminAuthRepository
    private var backgroundedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        // Back-office screens never appear in screenshots or the recents preview.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            MadakTheme(rtl = true) { AdminApp() }
        }
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
    }
}
