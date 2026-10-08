package com.madak.spices.designsystem.component

import com.madak.spices.designsystem.R

/** Bundled real product photos (drawable-nodpi/spice_*.webp); credits in [PhotoCredits] and docs/brand/PHOTO_CREDITS.md. */
object SpicePhotos {
    fun drawableFor(key: String?): Int? = when (key) {
        "cumin" -> R.drawable.spice_cumin
        "black_pepper" -> R.drawable.spice_black_pepper
        "turmeric" -> R.drawable.spice_turmeric
        "paprika" -> R.drawable.spice_paprika
        "ginger" -> R.drawable.spice_ginger
        "cinnamon" -> R.drawable.spice_cinnamon
        "ras_el_hanout" -> R.drawable.spice_ras_el_hanout
        "spice_mix" -> R.drawable.spice_spice_mix
        "herbs" -> R.drawable.spice_herbs
        "coriander" -> R.drawable.spice_coriander
        "cloves" -> R.drawable.spice_cloves
        "chili" -> R.drawable.spice_chili
        "saffron" -> R.drawable.spice_saffron
        "nigella" -> R.drawable.spice_nigella
        "fenugreek" -> R.drawable.spice_fenugreek
        "sesame" -> R.drawable.spice_sesame
        "cardamom" -> R.drawable.spice_cardamom
        "star_anise" -> R.drawable.spice_star_anise
        "bay_leaf" -> R.drawable.spice_bay_leaf
        "mint" -> R.drawable.spice_mint
        "rosemary" -> R.drawable.spice_rosemary
        "garlic_powder" -> R.drawable.spice_garlic_powder
        "nutmeg" -> R.drawable.spice_nutmeg
        else -> null
    }
}
