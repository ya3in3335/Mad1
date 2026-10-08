package com.madak.spices.data

import com.madak.spices.data.model.Dish
import com.madak.spices.data.seed.CatalogDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTest {
    @Test
    fun `store starts with category shelves only`() {
        assertEquals(3, CatalogDefaults.categories.size)
        assertEquals("cumin_100", CatalogDefaults.variantId("cumin", 100))
    }

    @Test
    fun `dish recommendations match real product names in arabic and french`() {
        assertEquals(7, Dish.entries.size)
        assertTrue(Dish.CHICKEN.relevance("خلطة الدجاج", "", "") != null)
        assertTrue(Dish.MEAT.relevance("", "Cumin moulu", "") != null)
        assertTrue(Dish.FISH.relevance("شرمولة", "", "") != null)
        assertNull(Dish.CHICKEN.relevance("سكر", "Sucre", ""))
    }

    @Test
    fun `product names map to the right bundled photo`() {
        val photos = com.madak.spices.data.model.ProductPhotos
        assertEquals("cumin", photos.keyFor("كمون", "", ""))
        assertEquals("nigella", photos.keyFor("حبة البركة", "", ""))
        assertEquals("ras_el_hanout", photos.keyFor("رأس الحانوت", "", ""))
        assertEquals("chili", photos.keyFor("", "Piment fort", ""))
        assertEquals("spice_mix", photos.keyFor("خلطة الدجاج", "Mélange volaille", ""))
        assertNull(photos.keyFor("سكر", "Sucre", ""))
        assertEquals(photos.all.size, photos.all.map { it.key }.toSet().size)
    }

    @Test
    fun `store phone is formatted for display`() {
        assertEquals("+213 664 71 70 29", com.madak.spices.data.model.StoreInfo.phonePretty)
        assertEquals("https://wa.me/213664717029", com.madak.spices.data.model.StoreInfo.whatsappUrl())
    }

    @Test
    fun `more specific keywords rank first`() {
        val mix = Dish.CHICKEN.relevance("خلطة الدجاج", "", "")!!
        val pepper = Dish.CHICKEN.relevance("فلفل أسود", "", "")!!
        assertTrue(mix < pepper)
    }
}
