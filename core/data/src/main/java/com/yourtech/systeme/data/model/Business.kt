package com.yourtech.systeme.data.model

import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import java.net.URLEncoder
import java.util.Calendar
import java.util.TimeZone

/**
 * Company details supplied by YOURTECH SYSTEME. They are configurable (Admin app → Business info)
 * and must be re-verified before production.
 */
object BusinessDefaults {
    val info = BusinessInfoEntity(
        companyName = "YOURTECH SYSTEME",
        taglineAr = "حلول أمنية ذكية",
        taglineFr = "Solutions de sécurité intelligentes",
        taglineEn = "Smart Security Solutions",
        phone = "213561034149",
        whatsapp = "213561034149",
        addressAr = "بئر الجير، وهران، الجزائر",
        addressFr = "Bir El Djir, Oran, Algérie",
        addressEn = "Bir El Djir, Oran, Algeria",
        latitude = 35.7190252,
        longitude = -0.5664739,
        // Saturday–Thursday 08:00–17:30, Friday closed.
        openingHours = BusinessHours.format(
            mapOf(
                Calendar.SATURDAY to (8 * 60 to 17 * 60 + 30),
                Calendar.SUNDAY to (8 * 60 to 17 * 60 + 30),
                Calendar.MONDAY to (8 * 60 to 17 * 60 + 30),
                Calendar.TUESDAY to (8 * 60 to 17 * 60 + 30),
                Calendar.WEDNESDAY to (8 * 60 to 17 * 60 + 30),
                Calendar.THURSDAY to (8 * 60 to 17 * 60 + 30),
            )
        ),
    )
}

/** Weekly opening hours, stored as "day:HH:MM-HH:MM" entries (Calendar day constants). */
object BusinessHours {
    val ALGIERS: TimeZone = TimeZone.getTimeZone("Africa/Algiers")

    fun format(days: Map<Int, Pair<Int, Int>>): String = days.entries.sortedBy { it.key }.joinToString(",") { (d, r) ->
        "%d:%02d:%02d-%02d:%02d".format(java.util.Locale.US, d, r.first / 60, r.first % 60, r.second / 60, r.second % 60)
    }

    fun parse(value: String): Map<Int, Pair<Int, Int>> = value.split(",").mapNotNull { part ->
        val m = Regex("""^(\d):(\d{2}):(\d{2})-(\d{2}):(\d{2})$""").find(part.trim()) ?: return@mapNotNull null
        val (d, h1, m1, h2, m2) = m.destructured
        d.toInt() to (h1.toInt() * 60 + m1.toInt() to h2.toInt() * 60 + m2.toInt())
    }.toMap()

    data class Status(val isOpen: Boolean, val opensAtMinutes: Int?, val closesAtMinutes: Int?, val nextOpenDay: Int?)

    fun status(value: String, nowMillis: Long = System.currentTimeMillis()): Status {
        val days = parse(value)
        val cal = Calendar.getInstance(ALGIERS).apply { timeInMillis = nowMillis }
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val today = days[day]
        if (today != null && minutes in today.first until today.second) {
            return Status(true, today.first, today.second, null)
        }
        if (today != null && minutes < today.first) return Status(false, today.first, today.second, day)
        // Next opening day
        for (i in 1..7) {
            val d = ((day - 1 + i) % 7) + 1
            days[d]?.let { return Status(false, it.first, it.second, d) }
        }
        return Status(false, null, null, null)
    }

    fun hhmm(minutes: Int) = "%02d:%02d".format(java.util.Locale.US, minutes / 60, minutes % 60)
}

fun BusinessInfoEntity.phoneDisplay(): String = displayPhone(phone)
fun BusinessInfoEntity.whatsappDisplay(): String = displayPhone(whatsapp)

/** "213561034149" → "+213 561 03 41 49" */
fun displayPhone(international: String): String {
    val d = international.filter(Char::isDigit)
    if (d.length != 12 || !d.startsWith("213")) return "+$d"
    val local = d.drop(3)
    return "+213 ${local.take(3)} ${local.drop(3).chunked(2).joinToString(" ")}"
}

fun BusinessInfoEntity.whatsappUrl(message: String = ""): String =
    "https://wa.me/${whatsapp.filter(Char::isDigit)}" + if (message.isBlank()) "" else "?text=" + URLEncoder.encode(message, "UTF-8").replace("+", "%20")

fun BusinessInfoEntity.directionsUrl(): String =
    "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"

fun BusinessInfoEntity.navigationUri(): String = "google.navigation:q=$latitude,$longitude"

fun BusinessInfoEntity.address(language: AppLanguage) = language.pick(addressAr, addressFr, addressEn)
fun BusinessInfoEntity.tagline(language: AppLanguage) = language.pick(taglineAr, taglineFr, taglineEn)
