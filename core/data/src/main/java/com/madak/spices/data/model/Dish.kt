package com.madak.spices.data.model

/**
 * "ماذا تطبخ اليوم؟" — maps a dish family to the kinds of spices that suit it. Recommendations are
 * matched by keyword against the real catalogue (product names and tags), so any product the owner
 * adds — e.g. "كمون" or "Cumin" — is suggested automatically. Keywords are ordered by relevance.
 */
enum class Dish(
    val nameAr: String,
    val nameFr: String,
    val emoji: String,
    val colorArgb: Long,
    val keywords: List<String>,
    val tipAr: String,
    val tipFr: String,
) {
    CHICKEN(
        "دجاج", "Poulet", "🍗", 0xFFE9A23BL,
        listOf("دجاج", "poulet", "بابريكا", "paprika", "كركم", "curcuma", "زنجبيل", "gingembre", "فلفل", "poivre"),
        "تبّل الدجاج بخلطة الدجاج مع البابريكا والكركم واتركه ساعة قبل الطهي.",
        "Marinez le poulet avec le mélange poulet, paprika et curcuma une heure avant cuisson.",
    ),
    MEAT(
        "لحم", "Viande", "🥩", 0xFF9E2B25L,
        listOf("لحم", "viande", "كفتة", "رأس الحانوت", "راس الحانوت", "ras el hanout", "فلفل", "poivre", "كمون", "cumin", "قرفة", "cannelle"),
        "رأس الحانوت مع رشة قرفة يمنح اللحم المطهو ببطء نكهة تقليدية عميقة.",
        "Le ras el hanout et une pincée de cannelle subliment les viandes mijotées.",
    ),
    FISH(
        "سمك", "Poisson", "🐟", 0xFF2F7F9EL,
        listOf("سمك", "poisson", "شرمولة", "chermoula", "كمون", "cumin", "بابريكا", "paprika", "أعشاب", "اعشاب", "herbes", "كركم", "curcuma"),
        "الكمون والأعشاب مع الليمون هي سر الشرمولة الناجحة للسمك.",
        "Cumin, herbes et citron : le secret d'une chermoula réussie.",
    ),
    RICE(
        "أرز", "Riz", "🍚", 0xFFE3C26FL,
        listOf("أرز", "riz", "كركم", "curcuma", "زعفران", "safran", "قرفة", "cannelle", "رأس الحانوت", "ras el hanout", "فلفل", "poivre"),
        "ملعقة صغيرة من الكركم تعطي الأرز لوناً ذهبياً ونكهة دافئة.",
        "Une cuillère de curcuma donne au riz une couleur dorée et un goût chaleureux.",
    ),
    SALAD(
        "سلطة", "Salade", "🥗", 0xFF6BA539L,
        listOf("سلطة", "salade", "أعشاب", "اعشاب", "herbes", "زعتر", "thym", "كمون", "cumin", "فلفل", "poivre", "بابريكا", "paprika"),
        "رشة كمون مع الأعشاب المشكلة تحوّل السلطة العادية إلى طبق مميز.",
        "Une pincée de cumin et d'herbes transforme une salade simple.",
    ),
    SOUP(
        "شوربة", "Chorba", "🍲", 0xFFC0632DL,
        listOf("شوربة", "chorba", "رأس الحانوت", "راس الحانوت", "ras el hanout", "بابريكا", "paprika", "قرفة", "cannelle", "أعشاب", "herbes", "فلفل", "poivre"),
        "للشوربة: رأس الحانوت مع البابريكا وعود قرفة صغير أثناء الطهي.",
        "Pour la chorba : ras el hanout, paprika et un bâton de cannelle.",
    ),
    GRILL(
        "مشاوي", "Grillades", "🔥", 0xFF5B3A29L,
        listOf("مشاوي", "grillade", "شواء", "كمون", "cumin", "بابريكا", "paprika", "فلفل", "poivre", "لحم", "viande", "دجاج", "poulet"),
        "اخلط الكمون والبابريكا مع زيت الزيتون وادهن المشاوي قبل الشواء.",
        "Mélangez cumin, paprika et huile d'olive avant de griller.",
    );

    fun name(language: AppLanguage) = language.pick(nameAr, nameFr)

    /** Relevance of a product for this dish: index of the first matching keyword, or null. */
    fun relevance(productNameAr: String, productNameFr: String, tags: String): Int? {
        val haystack = "$productNameAr $productNameFr $tags".lowercase()
        return keywords.indexOfFirst { haystack.contains(it.lowercase()) }.takeIf { it >= 0 }
    }
    fun tip(language: AppLanguage) = language.pick(tipAr, tipFr)
}
