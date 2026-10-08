package com.madak.spices.data.model

import java.net.URLEncoder

/**
 * Public store contact details shown in the app.
 *
 * Source: the official TikTok account @madak.spices ("Madak Spices-مذاق للتوابل"), whose videos
 * place the shop near the M Suite hotel in Dar El Beïda, Algiers.
 * Leave a value empty when it is not known: the matching contact button is hidden, so the app never
 * shows a made-up number. Fill PHONE / WHATSAPP / INSTAGRAM_HANDLE / EMAIL / hours when available.
 */
object StoreInfo {
    const val BRAND_AR = "مذاق لتوابل"
    const val BRAND_EN = "MADAK SPICES"
    const val SLOGAN_AR = "مذاق سرّ لذة الأطباق"
    const val SLOGAN_FR = "Madak, le secret des bons plats"

    /** International format without "+", e.g. "213550123456". */
    const val PHONE = ""
    const val WHATSAPP = ""
    const val INSTAGRAM_HANDLE = ""
    const val EMAIL = ""
    const val TIKTOK_HANDLE = "madak.spices"

    const val ADDRESS_AR = "بالقرب من فندق M Suite، الدار البيضاء، الجزائر العاصمة"
    const val ADDRESS_FR = "Près de l'hôtel M Suite, Dar El Beïda, Alger"
    const val OPENING_HOURS_AR = ""
    const val OPENING_HOURS_FR = ""

    /** Searched in the maps app; the hotel is the landmark the shop itself gives. */
    const val MAPS_QUERY = "M Suite Hotel, 9 Rue de l'ALN, Dar El Beïda, Alger"

    val hasPhone get() = PHONE.isNotBlank()
    val hasWhatsapp get() = WHATSAPP.isNotBlank()
    val hasInstagram get() = INSTAGRAM_HANDLE.isNotBlank()
    val hasEmail get() = EMAIL.isNotBlank()
    val hasTiktok get() = TIKTOK_HANDLE.isNotBlank()
    val hasOpeningHours get() = OPENING_HOURS_AR.isNotBlank()

    val phoneDisplay get() = "+$PHONE"
    val tiktokUrl get() = "https://www.tiktok.com/@$TIKTOK_HANDLE"
    val instagramUrl get() = "https://instagram.com/$INSTAGRAM_HANDLE"
    fun whatsappUrl(message: String = "") =
        "https://wa.me/$WHATSAPP" + if (message.isBlank()) "" else "?text=" + URLEncoder.encode(message, "UTF-8")
    val mapsUri get() = "geo:0,0?q=" + URLEncoder.encode(MAPS_QUERY, "UTF-8")
    val mapsWebUrl get() = "https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(MAPS_QUERY, "UTF-8")
}
