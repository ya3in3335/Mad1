package com.madak.spices.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.madak.spices.data.local.entity.BestSellerRow
import com.madak.spices.data.local.entity.CustomerRow
import com.madak.spices.data.local.entity.OrderEntity
import com.madak.spices.data.local.entity.OrderItemEntity
import com.madak.spices.data.local.entity.OrderStatusEventEntity
import com.madak.spices.data.local.entity.OrderWithItems
import com.madak.spices.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertItems(items: List<OrderItemEntity>)

    @Insert
    suspend fun insertEvent(event: OrderStatusEventEntity)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun orderCount(): Int

    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeOrders(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE status IN (:statuses) ORDER BY createdAt DESC")
    fun observeOrdersByStatus(statuses: List<OrderStatus>): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeOrder(id: Long): Flow<OrderWithItems?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrder(id: Long): OrderEntity?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItems(orderId: Long): List<OrderItemEntity>

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: OrderStatus, updatedAt: Long = System.currentTimeMillis())

    // ---- Statistics -------------------------------------------------------------------------

    @Query("SELECT COUNT(*) FROM orders WHERE createdAt >= :since")
    fun observeOrdersSince(since: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalDzd), 0) FROM orders WHERE createdAt >= :since AND status != 'CANCELLED'")
    fun observeSalesSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status IN ('NEW','CONFIRMED','PREPARING','OUT_FOR_DELIVERY')")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED'")
    fun observeCompletedCount(): Flow<Int>

    @Query(
        """
        SELECT i.productId AS productId, i.productNameAr AS productNameAr, i.productNameFr AS productNameFr,
               SUM(i.quantity) AS quantity, SUM(i.lineTotalDzd) AS revenueDzd
        FROM order_items i INNER JOIN orders o ON o.id = i.orderId
        WHERE o.status != 'CANCELLED'
        GROUP BY i.productId
        ORDER BY quantity DESC
        LIMIT :limit
        """
    )
    fun observeBestSellers(limit: Int = 5): Flow<List<BestSellerRow>>

    @Query(
        """
        SELECT customerName, phone, wilaya, COUNT(*) AS ordersCount,
               SUM(CASE WHEN status != 'CANCELLED' THEN totalDzd ELSE 0 END) AS totalSpentDzd,
               MAX(createdAt) AS lastOrderAt
        FROM orders GROUP BY phone ORDER BY lastOrderAt DESC
        """
    )
    fun observeCustomers(): Flow<List<CustomerRow>>
}
