package com.yourtech.systeme

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yourtech.systeme.data.model.AppLanguage
import com.yourtech.systeme.data.settings.LocaleStore
import com.yourtech.systeme.designsystem.theme.YourTechTheme
import com.yourtech.systeme.navigation.YourTechAppUi
import com.yourtech.systeme.ui.LocalLanguage
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withLanguage(LocaleStore.get(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        val language = LocaleStore.get(this)
        setContent {
            CompositionLocalProvider(LocalLanguage provides language) {
                YourTechTheme(rtl = language.isRtl) {
                    YourTechAppUi(onLanguageChange = { if (it != language) { LocaleStore.set(this, it); recreate() } })
                }
            }
        }
    }
}

private fun Context.withLanguage(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.localeTag)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration).apply { setLocale(locale); setLayoutDirection(locale) }
    return createConfigurationContext(config)
}
