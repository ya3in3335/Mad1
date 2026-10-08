package com.madak.spices.data.remote.dto

data class CatalogDto(
    val products: List<ProductDto> = emptyList(),
)

data class ProductDto(
    val id: String,
    val variants: List<VariantDto> = emptyList(),
    val isActive: Boolean = true,
    val imageUrl: String? = null,
)

data class VariantDto(
    val id: String,
    val priceDzd: Int,
    val stock: Int,
)

data class OrderRequestDto(
    val orderNumber: String,
    val customerName: String,
    val phone: String,
    val wilaya: String,
    val commune: String,
    val address: String,
    val notes: String,
    val paymentMethod: String,
    val items: List<OrderLineDto>,
    val totalDzd: Int,
)

data class OrderLineDto(val variantId: String, val quantity: Int)

data class OrderAckDto(val orderNumber: String, val status: String)
