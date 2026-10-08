package com.madak.spices.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProductWithVariants(
    @Embedded val product: ProductEntity,
    @Relation(parentColumn = "id", entityColumn = "productId")
    val variants: List<ProductVariantEntity>,
)

data class CartItemWithProduct(
    @Embedded val item: CartItemEntity,
    @Relation(parentColumn = "productId", entityColumn = "id", entity = ProductEntity::class)
    val product: ProductWithVariants,
)

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<OrderItemEntity>,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val events: List<OrderStatusEventEntity>,
)

/** Aggregates for the admin dashboard. */
data class BestSellerRow(
    val productId: String,
    val productNameAr: String,
    val productNameFr: String,
    val quantity: Int,
    val revenueDzd: Int,
)

data class CustomerRow(
    val customerName: String,
    val phone: String,
    val wilaya: String,
    val ordersCount: Int,
    val totalSpentDzd: Int,
    val lastOrderAt: Long,
)

data class InventoryRow(
    val variantId: String,
    val productId: String,
    val productNameAr: String,
    val productNameFr: String,
    val weightGrams: Int,
    val priceDzd: Int,
    val stock: Int,
    val lowStockThreshold: Int,
    val sku: String,
)
