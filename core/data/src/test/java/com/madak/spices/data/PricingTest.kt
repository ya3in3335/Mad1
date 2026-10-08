package com.madak.spices.data

import com.madak.spices.data.model.AppLanguage
import com.madak.spices.data.model.Pricing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PricingTest {
    @Test
    fun `variant prices scale with weight and are rounded to 10 DZD`() {
        assertEquals(140, Pricing.variantPrice(250, 50))
        assertEquals(250, Pricing.variantPrice(250, 100))
        assertEquals(580, Pricing.variantPrice(250, 250))
        assertEquals(1080, Pricing.variantPrice(250, 500))
        Pricing.WEIGHTS.forEach { assertEquals(0, Pricing.variantPrice(333, it) % 10) }
    }

    @Test
    fun `bigger packs are cheaper per gram`() {
        val perGram = Pricing.WEIGHTS.map { Pricing.variantPrice(400, it) / it.toDouble() }
        assertTrue(perGram.zipWithNext().all { (a, b) -> b <= a })
    }

    @Test
    fun `delivery fee depends on wilaya and becomes free above threshold`() {
        assertEquals(Pricing.DELIVERY_FEE_ALGIERS_DZD, Pricing.deliveryFee(16, 1000))
        assertEquals(Pricing.DELIVERY_FEE_NORTH_DZD, Pricing.deliveryFee(31, 1000))
        assertEquals(Pricing.DELIVERY_FEE_SOUTH_DZD, Pricing.deliveryFee(11, 1000))
        assertEquals(Pricing.DELIVERY_FEE_NORTH_DZD, Pricing.deliveryFee(null, 1000))
        assertEquals(0, Pricing.deliveryFee(11, Pricing.FREE_DELIVERY_THRESHOLD_DZD))
        assertEquals(0, Pricing.deliveryFee(16, 0))
    }

    @Test
    fun `formats amounts per language`() {
        assertEquals("1\u202F250 د.ج", Pricing.format(1250, AppLanguage.ARABIC))
        assertEquals("1\u202F250 DA", Pricing.format(1250, AppLanguage.FRENCH))
    }

    @Test
    fun `discount is applied and rounded`() {
        assertEquals(900, Pricing.discounted(1000, 10))
        assertEquals(360, Pricing.discounted(420, 15))
    }
}
