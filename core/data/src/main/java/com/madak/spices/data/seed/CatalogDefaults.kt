package com.madak.spices.data.seed

import com.madak.spices.data.local.entity.CategoryEntity

/**
 * The store ships with an EMPTY catalogue: no products, prices, offers or orders are invented.
 * Only the three category shelves exist so the owner can start adding real products from the
 * Madak Admin app (or a connected backend).
 */
object CatalogDefaults {
    val categories = listOf(
        CategoryEntity("spices", "توابل أساسية", "Épices essentielles", "توابل نقية مطحونة بعناية", "Épices pures moulues avec soin", 0xFFC0632DL, 0),
        CategoryEntity("blends", "خلطات مذاق", "Mélanges Madak", "خلطاتنا الخاصة لكل طبق", "Nos mélanges signature pour chaque plat", 0xFFD6247CL, 1),
        CategoryEntity("herbs", "أعشاب", "Herbes", "أعشاب مجففة طبيعياً", "Herbes séchées naturellement", 0xFF6BA539L, 2),
    )

    fun variantId(productId: String, grams: Int) = "${productId}_$grams"
}
