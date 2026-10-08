package com.madak.spices.data.model

/**
 * Library of real product photos bundled with the app (see docs/brand/PHOTO_CREDITS.md).
 * A product gets a photo from, in order: its pasted image URL, the photo the owner picked in the
 * Admin app, or the first library entry whose keywords match its name/tags.
 */
object ProductPhotos {
    data class Entry(val key: String, val labelAr: String, val keywords: List<String>)

    /** Ordered from most specific to most generic so e.g. "حبة البركة" wins over "كمون". */
    val all = listOf(
        Entry("ras_el_hanout", "رأس الحانوت", listOf("رأس الحانوت", "راس الحانوت", "ras el hanout", "ras-el-hanout")),
        Entry("nigella", "حبة البركة", listOf("حبة البركة", "الحبة السوداء", "سانوج", "nigelle", "nigella", "habba")),
        Entry("black_pepper", "فلفل أسود", listOf("فلفل أسود", "فلفل اسود", "poivre noir", "poivre", "black pepper")),
        Entry("chili", "فلفل حار", listOf("فلفل حار", "شطة", "هريسة", "piment", "chili", "harissa", "فلفل أحمر حار")),
        Entry("paprika", "بابريكا", listOf("بابريكا", "فلفل حلو", "فلفل أحمر", "paprika")),
        Entry("cumin", "كمون", listOf("كمون", "كامون", "cumin")),
        Entry("turmeric", "كركم", listOf("كركم", "عقدة صفراء", "curcuma", "turmeric")),
        Entry("ginger", "زنجبيل", listOf("زنجبيل", "سكنجبير", "gingembre", "ginger")),
        Entry("cinnamon", "قرفة", listOf("قرفة", "دارسين", "cannelle", "cinnamon")),
        Entry("saffron", "زعفران", listOf("زعفران", "safran", "saffron")),
        Entry("coriander", "كزبرة", listOf("كزبرة", "قصبر", "coriandre", "coriander")),
        Entry("cloves", "قرنفل", listOf("قرنفل", "عود النوار", "clou de girofle", "girofle", "cloves")),
        Entry("cardamom", "هيل", listOf("هيل", "حب الهال", "cardamome", "cardamom")),
        Entry("star_anise", "يانسون نجمي", listOf("يانسون نجمي", "badiane", "anis étoilé", "star anise")),
        Entry("anise", "يانسون", listOf("يانسون", "حبة حلاوة", "anis", "anise")),
        Entry("fenugreek", "حلبة", listOf("حلبة", "fenugrec", "fenugreek")),
        Entry("sesame", "سمسم", listOf("سمسم", "جلجلان", "sésame", "sesame")),
        Entry("nutmeg", "جوزة الطيب", listOf("جوزة الطيب", "noix de muscade", "muscade", "nutmeg")),
        Entry("garlic_powder", "ثوم مجفف", listOf("ثوم", "ail en poudre", "ail séché", "garlic")),
        Entry("bay_leaf", "ورق الغار", listOf("ورق الغار", "رند", "laurier", "bay leaf")),
        Entry("mint", "نعناع", listOf("نعناع", "فليو", "menthe", "mint")),
        Entry("rosemary", "إكليل الجبل", listOf("إكليل", "اكليل", "يازير", "romarin", "rosemary")),
        Entry("herbs", "أعشاب", listOf("أعشاب", "اعشاب", "زعتر", "herbes", "thym", "herbs")),
        Entry("spice_mix", "خلطة توابل", listOf("خلطة", "خليط", "mélange", "melange", "mix", "مشكل")),
    )

    private val byKey = all.associateBy { it.key }

    fun entry(key: String?): Entry? = key?.let(byKey::get)

    fun keyFor(nameAr: String, nameFr: String, tags: String): String? {
        val haystack = "$nameAr $nameFr $tags".lowercase()
        return all.firstOrNull { e -> e.keywords.any { haystack.contains(it.lowercase()) } }?.key
    }
}
