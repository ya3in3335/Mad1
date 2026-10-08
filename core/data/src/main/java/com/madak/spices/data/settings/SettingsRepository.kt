package com.madak.spices.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.madak.spices.data.model.AppLanguage
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class SettingsRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private val onboardingKey = booleanPreferencesKey("onboarding_done")
    private val splashSoundKey = booleanPreferencesKey("splash_sound")

    private val prefs: Flow<Preferences> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val onboardingDone: Flow<Boolean> = prefs.map { it[onboardingKey] ?: false }
    val splashSoundEnabled: Flow<Boolean> = prefs.map { it[splashSoundKey] ?: true }

    suspend fun isOnboardingDone() = onboardingDone.first()

    suspend fun setOnboardingDone() {
        dataStore.edit { it[onboardingKey] = true }
    }

    suspend fun setSplashSound(enabled: Boolean) {
        dataStore.edit { it[splashSoundKey] = enabled }
    }
}

/**
 * The UI language must be known synchronously in Activity.attachBaseContext, so it is kept in
 * plain SharedPreferences rather than DataStore. Arabic is the default.
 */
object LocaleStore {
    private const val FILE = "madak_locale"
    private const val KEY = "language"

    fun get(context: Context): AppLanguage =
        AppLanguage.fromTag(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null))

    fun set(context: Context, language: AppLanguage) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, language.tag).apply()
    }
}
