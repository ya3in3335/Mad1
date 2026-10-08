package com.madak.spices.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.madak.spices.data.local.entity.CartEntity
import com.madak.spices.data.local.entity.CartItemEntity
import com.madak.spices.data.local.entity.CartItemWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ensureCart(cart: CartEntity)

    @Transaction
    @Query("SELECT * FROM cart_items WHERE cartId = :cartId ORDER BY addedAt DESC")
    fun observeItems(cartId: Long = CartEntity.ACTIVE_CART_ID): Flow<List<CartItemWithProduct>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE cartId = :cartId")
    fun observeCount(cartId: Long = CartEntity.ACTIVE_CART_ID): Flow<Int>

    @Query("SELECT * FROM cart_items WHERE cartId = :cartId AND variantId = :variantId LIMIT 1")
    suspend fun findByVariant(variantId: String, cartId: Long = CartEntity.ACTIVE_CART_ID): CartItemEntity?

    @Query("SELECT * FROM cart_items WHERE id = :id")
    suspend fun getItem(id: Long): CartItemEntity?

    @Transaction
    @Query("SELECT * FROM cart_items WHERE cartId = :cartId")
    suspend fun getItems(cartId: Long = CartEntity.ACTIVE_CART_ID): List<CartItemWithProduct>

    @Insert
    suspend fun insertItem(item: CartItemEntity): Long

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int)

    @Query("UPDATE cart_items SET variantId = :variantId WHERE id = :id")
    suspend fun updateVariant(id: Long, variantId: String)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM cart_items WHERE cartId = :cartId")
    suspend fun clear(cartId: Long = CartEntity.ACTIVE_CART_ID)
}
