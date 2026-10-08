package com.madak.spices.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.madak.spices.data.model.OrderStatus

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val isCurrent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameFr: String,
    val descriptionAr: String = "",
    val descriptionFr: String = "",
    val colorArgb: Long,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
)

@Entity(
    tableName = "products",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.RESTRICT,
    )],
    indices = [Index("categoryId")],
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val nameAr: String,
    val nameFr: String,
    val descriptionAr: String,
    val descriptionFr: String,
    val usageAr: String = "",
    val usageFr: String = "",
    val originAr: String = "",
    val originFr: String = "",
    /** Dominant spice colour, used to render the offline product illustration. */
    val colorArgb: Long,
    /** Optional remote image; the app never depends on it being reachable. */
    val imageUrl: String? = null,
    /** Average customer rating; 0 until real reviews exist. */
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isActive: Boolean = true,
    /** Comma separated search keywords (Arabic + French). */
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "product_variants",
    foreignKeys = [ForeignKey(
        entity = ProductEntity::class,
        parentColumns = ["id"],
        childColumns = ["productId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("productId")],
)
data class ProductVariantEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val weightGrams: Int,
    val priceDzd: Int,
    val stock: Int,
    val sku: String,
    val lowStockThreshold: Int = 10,
)

@Entity(tableName = "carts")
data class CartEntity(
    @PrimaryKey val id: Long = ACTIVE_CART_ID,
    val userId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object { const val ACTIVE_CART_ID = 1L }
}

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(entity = CartEntity::class, parentColumns = ["id"], childColumns = ["cartId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ProductVariantEntity::class, parentColumns = ["id"], childColumns = ["variantId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["cartId", "variantId"], unique = true), Index("variantId"), Index("productId")],
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cartId: Long = CartEntity.ACTIVE_CART_ID,
    val productId: String,
    val variantId: String,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "orders", indices = [Index(value = ["orderNumber"], unique = true), Index("status"), Index("createdAt")])
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val userId: Long? = null,
    val status: OrderStatus = OrderStatus.NEW,
    val customerName: String,
    val phone: String,
    val wilaya: String,
    val commune: String,
    val address: String,
    val notes: String = "",
    val paymentMethod: String = PAYMENT_CASH_ON_DELIVERY,
    val deliveryMethod: String = DELIVERY_HOME,
    val subtotalDzd: Int,
    val deliveryFeeDzd: Int,
    val discountDzd: Int = 0,
    val totalDzd: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val PAYMENT_CASH_ON_DELIVERY = "CASH_ON_DELIVERY"
        const val DELIVERY_HOME = "HOME_DELIVERY"
    }
}

@Entity(
    tableName = "order_items",
    foreignKeys = [ForeignKey(entity = OrderEntity::class, parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("orderId"), Index("productId")],
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: String,
    val variantId: String,
    /** Snapshot of the product at the time of purchase. */
    val productNameAr: String,
    val productNameFr: String,
    val weightGrams: Int,
    val unitPriceDzd: Int,
    val quantity: Int,
    val lineTotalDzd: Int,
)

@Entity(
    tableName = "order_status_events",
    foreignKeys = [ForeignKey(entity = OrderEntity::class, parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("orderId")],
)
data class OrderStatusEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val status: OrderStatus,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "addresses", indices = [Index("userId")])
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long? = null,
    val label: String = "",
    val fullName: String,
    val phone: String,
    val wilaya: String,
    val commune: String,
    val street: String,
    val isDefault: Boolean = false,
)

@Entity(
    tableName = "favorites",
    foreignKeys = [ForeignKey(entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.CASCADE)],
)
data class FavoriteEntity(
    @PrimaryKey val productId: String,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "notifications", indices = [Index("createdAt")])
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleAr: String,
    val titleFr: String,
    val bodyAr: String,
    val bodyFr: String,
    val type: String = TYPE_GENERAL,
    val orderId: Long? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val TYPE_GENERAL = "GENERAL"
        const val TYPE_ORDER = "ORDER"
        const val TYPE_OFFER = "OFFER"
    }
}

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey val id: String,
    val titleAr: String,
    val titleFr: String,
    val subtitleAr: String,
    val subtitleFr: String,
    val discountPercent: Int,
    val productId: String? = null,
    val categoryId: String? = null,
    val promoCode: String? = null,
    val colorArgb: Long,
    val isActive: Boolean = true,
    val startsAt: Long = System.currentTimeMillis(),
    val endsAt: Long? = null,
)
