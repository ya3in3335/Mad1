package com.madak.spices.data.model

import kotlin.math.roundToInt

/** All prices are integer Algerian Dinars (DZD). */
object Pricing {
    val WEIGHTS = listOf(50, 100, 250, 500)

    const val FREE_DELIVERY_THRESHOLD_DZD = 6000
    const val DELIVERY_FEE_ALGIERS_DZD = 400
    const val DELIVERY_FEE_NORTH_DZD = 600
    const val DELIVERY_FEE_SOUTH_DZD = 900

    /** Bigger packs are cheaper per gram, rounded to the nearest 10 DZD. */
    fun variantPrice(pricePer100g: Int, grams: Int): Int {
        val factor = when (grams) {
            50 -> 0.55
            100 -> 1.0
            250 -> 2.3
            500 -> 4.3
            else -> grams / 100.0
        }
        return ((pricePer100g * factor) / 10.0).roundToInt() * 10
    }

    fun deliveryFee(wilayaCode: Int?, subtotalDzd: Int): Int = when {
        subtotalDzd <= 0 -> 0
        subtotalDzd >= FREE_DELIVERY_THRESHOLD_DZD -> 0
        wilayaCode == null -> DELIVERY_FEE_NORTH_DZD
        wilayaCode == 16 -> DELIVERY_FEE_ALGIERS_DZD
        Wilayas.isSouth(wilayaCode) -> DELIVERY_FEE_SOUTH_DZD
        else -> DELIVERY_FEE_NORTH_DZD
    }

    fun remainingForFreeDelivery(subtotalDzd: Int): Int =
        (FREE_DELIVERY_THRESHOLD_DZD - subtotalDzd).coerceAtLeast(0)

    fun discounted(priceDzd: Int, percent: Int): Int =
        ((priceDzd * (100 - percent.coerceIn(0, 90))) / 1000.0).roundToInt() * 10

    fun format(dzd: Int, language: AppLanguage = AppLanguage.ARABIC): String {
        // U+202F (narrow no-break space) is a bidi "common separator": the digit groups stay in one
        // run, so "1 080" never renders as "080 1" inside right-to-left text.
        val grouped = "%,d".format(java.util.Locale.US, dzd).replace(',', '\u202F')
        return if (language == AppLanguage.ARABIC) "$grouped د.ج" else "$grouped DA"
    }
}
