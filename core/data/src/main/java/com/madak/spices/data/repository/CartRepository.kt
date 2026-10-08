package com.madak.spices.data.repository

import androidx.room.withTransaction
import com.madak.spices.data.local.MadakDatabase
import com.madak.spices.data.local.dao.CartDao
import com.madak.spices.data.local.entity.CartEntity
import com.madak.spices.data.local.entity.CartItemEntity
import com.madak.spices.data.model.CartLine
import com.madak.spices.data.model.toProduct
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CartRepository @Inject constructor(
    private val db: MadakDatabase,
    private val cartDao: CartDao,
) {
    val lines: Flow<List<CartLine>> = cartDao.observeItems().map { items ->
        items.mapNotNull { row ->
            val variant = row.product.variants.firstOrNull { it.id == row.item.variantId } ?: return@mapNotNull null
            CartLine(row.item.id, row.product.toProduct(), variant, row.item.quantity)
        }
    }

    val count: Flow<Int> = cartDao.observeCount()

    suspend fun add(productId: String, variantId: String, quantity: Int = 1) = db.withTransaction {
        require(quantity > 0)
        cartDao.ensureCart(CartEntity())
        val existing = cartDao.findByVariant(variantId)
        if (existing != null) {
            cartDao.updateQuantity(existing.id, (existing.quantity + quantity).coerceAtMost(MAX_QUANTITY))
        } else {
            cartDao.insertItem(CartItemEntity(productId = productId, variantId = variantId, quantity = quantity.coerceAtMost(MAX_QUANTITY)))
        }
    }

    suspend fun setQuantity(itemId: Long, quantity: Int) {
        if (quantity <= 0) cartDao.deleteItem(itemId)
        else cartDao.updateQuantity(itemId, quantity.coerceAtMost(MAX_QUANTITY))
    }

    /** Switches an item to another weight; merges with an existing line of that weight. */
    suspend fun changeVariant(itemId: Long, variantId: String) = db.withTransaction {
        val item = cartDao.getItem(itemId) ?: return@withTransaction
        if (item.variantId == variantId) return@withTransaction
        val other = cartDao.findByVariant(variantId)
        if (other != null) {
            cartDao.updateQuantity(other.id, (other.quantity + item.quantity).coerceAtMost(MAX_QUANTITY))
            cartDao.deleteItem(item.id)
        } else {
            cartDao.updateVariant(item.id, variantId)
        }
    }

    suspend fun remove(itemId: Long) = cartDao.deleteItem(itemId)

    suspend fun clear() = cartDao.clear()

    companion object { const val MAX_QUANTITY = 99 }
}
