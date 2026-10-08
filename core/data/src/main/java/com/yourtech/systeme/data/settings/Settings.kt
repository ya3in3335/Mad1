package com.yourtech.systeme.data.settings

import android.content.Context
import com.yourtech.systeme.data.model.AppLanguage

/** UI language, read synchronously in attachBaseContext. Arabic (RTL) is the default. */
object LocaleStore {
    private const val FILE = "yt_locale"
    private const val KEY = "language"
    fun get(context: Context) = AppLanguage.fromTag(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null))
    fun set(context: Context, language: AppLanguage) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, language.tag).apply()
}
