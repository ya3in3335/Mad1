package com.madak.spices.data.model

data class Wilaya(val code: Int, val nameAr: String, val nameFr: String) {
    fun label(language: AppLanguage): String =
        "%02d - %s".format(code, language.pick(nameAr, nameFr))

    /** Stable value stored on orders. */
    val storageValue: String get() = "%02d - %s".format(code, nameAr)
}

object Wilayas {
    private val southCodes = setOf(1, 8, 11, 30, 33, 37, 39, 47, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58)

    fun isSouth(code: Int) = code in southCodes

    fun byCode(code: Int?): Wilaya? = all.firstOrNull { it.code == code }

    fun fromStorage(value: String?): Wilaya? =
        value?.substringBefore(" -")?.trim()?.toIntOrNull()?.let(::byCode)

    val all = listOf(
        Wilaya(1, "أدرار", "Adrar"), Wilaya(2, "الشلف", "Chlef"), Wilaya(3, "الأغواط", "Laghouat"),
        Wilaya(4, "أم البواقي", "Oum El Bouaghi"), Wilaya(5, "باتنة", "Batna"), Wilaya(6, "بجاية", "Béjaïa"),
        Wilaya(7, "بسكرة", "Biskra"), Wilaya(8, "بشار", "Béchar"), Wilaya(9, "البليدة", "Blida"),
        Wilaya(10, "البويرة", "Bouira"), Wilaya(11, "تمنراست", "Tamanrasset"), Wilaya(12, "تبسة", "Tébessa"),
        Wilaya(13, "تلمسان", "Tlemcen"), Wilaya(14, "تيارت", "Tiaret"), Wilaya(15, "تيزي وزو", "Tizi Ouzou"),
        Wilaya(16, "الجزائر", "Alger"), Wilaya(17, "الجلفة", "Djelfa"), Wilaya(18, "جيجل", "Jijel"),
        Wilaya(19, "سطيف", "Sétif"), Wilaya(20, "سعيدة", "Saïda"), Wilaya(21, "سكيكدة", "Skikda"),
        Wilaya(22, "سيدي بلعباس", "Sidi Bel Abbès"), Wilaya(23, "عنابة", "Annaba"), Wilaya(24, "قالمة", "Guelma"),
        Wilaya(25, "قسنطينة", "Constantine"), Wilaya(26, "المدية", "Médéa"), Wilaya(27, "مستغانم", "Mostaganem"),
        Wilaya(28, "المسيلة", "M'Sila"), Wilaya(29, "معسكر", "Mascara"), Wilaya(30, "ورقلة", "Ouargla"),
        Wilaya(31, "وهران", "Oran"), Wilaya(32, "البيض", "El Bayadh"), Wilaya(33, "إليزي", "Illizi"),
        Wilaya(34, "برج بوعريريج", "Bordj Bou Arréridj"), Wilaya(35, "بومرداس", "Boumerdès"),
        Wilaya(36, "الطارف", "El Tarf"), Wilaya(37, "تندوف", "Tindouf"), Wilaya(38, "تيسمسيلت", "Tissemsilt"),
        Wilaya(39, "الوادي", "El Oued"), Wilaya(40, "خنشلة", "Khenchela"), Wilaya(41, "سوق أهراس", "Souk Ahras"),
        Wilaya(42, "تيبازة", "Tipaza"), Wilaya(43, "ميلة", "Mila"), Wilaya(44, "عين الدفلى", "Aïn Defla"),
        Wilaya(45, "النعامة", "Naâma"), Wilaya(46, "عين تموشنت", "Aïn Témouchent"), Wilaya(47, "غرداية", "Ghardaïa"),
        Wilaya(48, "غليزان", "Relizane"), Wilaya(49, "تيميمون", "Timimoun"), Wilaya(50, "برج باجي مختار", "Bordj Badji Mokhtar"),
        Wilaya(51, "أولاد جلال", "Ouled Djellal"), Wilaya(52, "بني عباس", "Béni Abbès"), Wilaya(53, "عين صالح", "In Salah"),
        Wilaya(54, "عين قزام", "In Guezzam"), Wilaya(55, "تقرت", "Touggourt"), Wilaya(56, "جانت", "Djanet"),
        Wilaya(57, "المغير", "El M'Ghair"), Wilaya(58, "المنيعة", "El Meniaa"),
    )
}
