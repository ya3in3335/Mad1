package com.madak.spices.data.model

import com.madak.spices.data.local.entity.OrderEntity
import com.madak.spices.data.local.entity.ProductEntity
import com.madak.spices.data.local.entity.ProductVariantEntity
import com.madak.spices.data.local.entity.ProductWithVariants

data class Product(
    val entity: ProductEntity,
    val variants: List<ProductVariantEntity>,
    val isFavorite: Boolean = false,
) {
    val id: String get() = entity.id
    fun name(language: AppLanguage) = language.pick(entity.nameAr, entity.nameFr)
    fun description(language: AppLanguage) = language.pick(entity.descriptionAr, entity.descriptionFr)

    val sortedVariants: List<ProductVariantEntity> get() = variants.sortedBy { it.weightGrams }
    val startingPrice: Int get() = variants.minOfOrNull { it.priceDzd } ?: 0
    val defaultVariant: ProductVariantEntity?
        get() = variants.firstOrNull { it.weightGrams == 100 } ?: sortedVariants.firstOrNull()
    val inStock: Boolean get() = variants.any { it.stock > 0 }

    /** Bundled photo for this product: the owner's choice, else matched from its name. */
    val photoKey: String?
        get() = entity.photoKey ?: ProductPhotos.keyFor(entity.nameAr, entity.nameFr, entity.tags)
}

fun ProductWithVariants.toProduct(favorites: Set<String> = emptySet()) =
    Product(product, variants, product.id in favorites)

data class CartLine(
    val itemId: Long,
    val product: Product,
    val variant: ProductVariantEntity,
    val quantity: Int,
) {
    val lineTotal: Int get() = variant.priceDzd * quantity
}

data class CartSummary(
    val lines: List<CartLine> = emptyList(),
    val wilayaCode: Int? = null,
) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
    val subtotal: Int get() = lines.sumOf { it.lineTotal }
    val deliveryFee: Int get() = Pricing.deliveryFee(wilayaCode, subtotal)
    val total: Int get() = subtotal + deliveryFee
    val remainingForFreeDelivery: Int get() = Pricing.remainingForFreeDelivery(subtotal)
    val isEmpty: Boolean get() = lines.isEmpty()
}

data class CheckoutForm(
    val fullName: String,
    val phone: String,
    val wilaya: Wilaya?,
    val commune: String,
    val address: String,
    val notes: String,
) {
    enum class Field { NAME, PHONE, WILAYA, COMMUNE, ADDRESS }

    fun validate(): Set<Field> = buildSet {
        if (fullName.trim().length < 3) add(Field.NAME)
        if (!isValidAlgerianPhone(phone)) add(Field.PHONE)
        if (wilaya == null) add(Field.WILAYA)
        if (commune.trim().length < 2) add(Field.COMMUNE)
        if (address.trim().length < 5) add(Field.ADDRESS)
    }

    companion object {
        private val phoneRegex = Regex("^(?:\\+213|00213|0)([567]\\d{8})$")

        fun normalizePhone(raw: String) = raw.filter { it.isDigit() || it == '+' }

        fun isValidAlgerianPhone(raw: String) = phoneRegex.matches(normalizePhone(raw))
    }
}

data class DashboardStats(
    val todayOrders: Int = 0,
    val todaySalesDzd: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
)

fun OrderEntity.wilayaLabel(language: AppLanguage): String =
    Wilayas.fromStorage(wilaya)?.label(language) ?: wilaya
