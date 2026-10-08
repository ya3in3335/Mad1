package com.madak.spices.data.model

/**
 * Public store contact details shown in the app.
 * Replace these placeholders with the real Madak store information before publishing.
 */
object StoreInfo {
    const val BRAND_AR = "مذاق لتوابل"
    const val BRAND_EN = "MADAK SPICES"
    const val PHONE = "+213550000000"
    const val WHATSAPP = "213550000000"
    const val INSTAGRAM_HANDLE = "madak.spices"
    const val EMAIL = "contact@madak-spices.dz"
    const val ADDRESS_AR = "الجزائر العاصمة، الجزائر"
    const val ADDRESS_FR = "Alger, Algérie"
    const val OPENING_HOURS_AR = "السبت – الخميس: 08:00 – 20:00"
    const val OPENING_HOURS_FR = "Samedi – Jeudi : 08h00 – 20h00"
    const val LATITUDE = 36.7538
    const val LONGITUDE = 3.0588

    val instagramUrl get() = "https://instagram.com/$INSTAGRAM_HANDLE"
    fun whatsappUrl(message: String = "") =
        "https://wa.me/$WHATSAPP" + if (message.isBlank()) "" else "?text=" + java.net.URLEncoder.encode(message, "UTF-8")
    val mapsUri get() = "geo:$LATITUDE,$LONGITUDE?q=$LATITUDE,$LONGITUDE(${java.net.URLEncoder.encode(BRAND_AR, "UTF-8")})"
}
