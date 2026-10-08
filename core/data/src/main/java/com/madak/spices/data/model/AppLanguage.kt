package com.madak.spices.data.model

enum class AppLanguage(val tag: String) {
    ARABIC("ar"),
    FRENCH("fr");

    val isRtl: Boolean get() = this == ARABIC

    /** Algerian locale with Latin digits (0-9), as used on Algerian prices and phone numbers. */
    val localeTag: String get() = if (this == ARABIC) "ar-DZ-u-nu-latn" else "fr-DZ"

    fun pick(ar: String, fr: String): String = if (this == ARABIC) ar else fr.ifBlank { ar }

    companion object {
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: ARABIC
    }
}
