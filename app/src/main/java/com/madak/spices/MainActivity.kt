package com.madak.spices

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.madak.spices.data.model.AppLanguage
import com.madak.spices.data.settings.LocaleStore
import com.madak.spices.designsystem.theme.MadakTheme
import com.madak.spices.navigation.MadakApp
import com.madak.spices.ui.LocalAppLanguage
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Arabic (RTL) is the default UI language; French is available from the profile screen.
        super.attachBaseContext(newBase.withLanguage(LocaleStore.get(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        val language = LocaleStore.get(this)
        setContent {
            CompositionLocalProvider(LocalAppLanguage provides language) {
                MadakTheme(rtl = language.isRtl) {
                    MadakApp(
                        onLanguageChange = { newLanguage ->
                            if (newLanguage != language) {
                                LocaleStore.set(this, newLanguage)
                                recreate()
                            }
                        },
                    )
                }
            }
        }
    }
}

private fun Context.withLanguage(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.localeTag)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
