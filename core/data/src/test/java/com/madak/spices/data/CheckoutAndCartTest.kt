package com.madak.spices.data

import com.madak.spices.data.model.CartLine
import com.madak.spices.data.model.CartSummary
import com.madak.spices.data.model.CheckoutForm
import com.madak.spices.data.model.Pricing
import com.madak.spices.data.model.Product
import com.madak.spices.data.model.Wilayas
import com.madak.spices.data.local.entity.ProductEntity
import com.madak.spices.data.local.entity.ProductVariantEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutAndCartTest {
    private val validForm = CheckoutForm("أمينة بن علي", "0551234567", Wilayas.byCode(16), "باب الزوار", "حي 5 جويلية عمارة 3", "")

    @Test
    fun `valid form has no errors`() = assertTrue(validForm.validate().isEmpty())

    @Test
    fun `invalid fields are reported`() {
        val errors = CheckoutForm("A", "12345", null, "", "x", "").validate()
        assertEquals(CheckoutForm.Field.entries.toSet(), errors)
    }

    @Test
    fun `algerian phone formats`() {
        listOf("0551234567", "0661 23 45 67", "+213771234567", "00213551234567").forEach {
            assertTrue(it, CheckoutForm.isValidAlgerianPhone(it))
        }
        listOf("0451234567", "055123456", "+33612345678", "").forEach {
            assertFalse(it, CheckoutForm.isValidAlgerianPhone(it))
        }
    }

    @Test
    fun `cart summary totals`() {
        val product = ProductEntity("test", "spices", "منتج", "Produit", "", "", colorArgb = 0xFF000000)
        val variants = Pricing.WEIGHTS.map { ProductVariantEntity("test_$it", "test", it, Pricing.variantPrice(250, it), 10, "SKU-$it") }
        val p = Product(product, variants)
        val v100 = variants.first { it.weightGrams == 100 }
        val v250 = variants.first { it.weightGrams == 250 }
        val summary = CartSummary(listOf(CartLine(1, p, v100, 2), CartLine(2, p, v250, 1)), wilayaCode = 31)
        assertEquals(3, summary.itemCount)
        assertEquals(v100.priceDzd * 2 + v250.priceDzd, summary.subtotal)
        assertEquals(Pricing.DELIVERY_FEE_NORTH_DZD, summary.deliveryFee)
        assertEquals(summary.subtotal + summary.deliveryFee, summary.total)
    }

    @Test
    fun `wilaya storage round trip`() {
        Wilayas.all.forEach { assertEquals(it, Wilayas.fromStorage(it.storageValue)) }
        assertEquals(58, Wilayas.all.size)
    }
}
