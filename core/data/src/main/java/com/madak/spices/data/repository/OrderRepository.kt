package com.madak.spices.data.repository

import androidx.room.withTransaction
import com.madak.spices.data.local.MadakDatabase
import com.madak.spices.data.local.entity.AddressEntity
import com.madak.spices.data.local.entity.NotificationEntity
import com.madak.spices.data.local.entity.OrderEntity
import com.madak.spices.data.local.entity.OrderItemEntity
import com.madak.spices.data.local.entity.OrderStatusEventEntity
import com.madak.spices.data.local.entity.OrderWithItems
import com.madak.spices.data.local.entity.UserEntity
import com.madak.spices.data.model.CartSummary
import com.madak.spices.data.model.CheckoutForm
import com.madak.spices.data.model.OrderStatus
import com.madak.spices.data.remote.MadakApi
import com.madak.spices.data.remote.RemoteConfig
import com.madak.spices.data.remote.dto.OrderLineDto
import com.madak.spices.data.remote.dto.OrderRequestDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout

@Singleton
class OrderRepository @Inject constructor(
    private val db: MadakDatabase,
    private val api: MadakApi,
) {
    private val orderDao get() = db.orderDao()

    val orders: Flow<List<OrderWithItems>> = orderDao.observeOrders()

    fun order(id: Long): Flow<OrderWithItems?> = orderDao.observeOrder(id)

    fun ordersByStatus(statuses: List<OrderStatus>): Flow<List<OrderWithItems>> =
        orderDao.observeOrdersByStatus(statuses)

    /**
     * Places a cash-on-delivery order from the cart: snapshots the lines, decrements stock,
     * clears the cart, remembers the customer details and posts an in-app notification.
     */
    suspend fun placeOrder(form: CheckoutForm, cart: CartSummary): OrderEntity {
        require(!cart.isEmpty) { "Cart is empty" }
        require(form.validate().isEmpty()) { "Invalid checkout form" }
        val wilaya = requireNotNull(form.wilaya)
        val now = System.currentTimeMillis()
        val order = db.withTransaction {
            val draft = OrderEntity(
                orderNumber = generateOrderNumber(now),
                customerName = form.fullName.trim(),
                phone = CheckoutForm.normalizePhone(form.phone),
                wilaya = wilaya.storageValue,
                commune = form.commune.trim(),
                address = form.address.trim(),
                notes = form.notes.trim(),
                subtotalDzd = cart.subtotal,
                deliveryFeeDzd = cart.deliveryFee,
                totalDzd = cart.total,
                createdAt = now,
                updatedAt = now,
            )
            val id = orderDao.insertOrder(draft)
            orderDao.insertItems(cart.lines.map { line ->
                OrderItemEntity(
                    orderId = id,
                    productId = line.product.id,
                    variantId = line.variant.id,
                    productNameAr = line.product.entity.nameAr,
                    productNameFr = line.product.entity.nameFr,
                    weightGrams = line.variant.weightGrams,
                    unitPriceDzd = line.variant.priceDzd,
                    quantity = line.quantity,
                    lineTotalDzd = line.lineTotal,
                )
            })
            orderDao.insertEvent(OrderStatusEventEntity(orderId = id, status = OrderStatus.NEW, timestamp = now))
            cart.lines.forEach { db.catalogDao().adjustStock(it.variant.id, -it.quantity) }
            db.cartDao().clear()

            val userDao = db.userDao()
            val user = userDao.getCurrentUser()
            userDao.upsertUser(
                (user ?: UserEntity(fullName = draft.customerName, phone = draft.phone, isCurrent = true))
                    .copy(fullName = draft.customerName, phone = draft.phone)
            )
            val address = userDao.getDefaultAddress()
            userDao.upsertAddress(
                AddressEntity(
                    id = address?.id ?: 0, fullName = draft.customerName, phone = draft.phone,
                    wilaya = draft.wilaya, commune = draft.commune, street = draft.address, isDefault = true,
                )
            )
            db.notificationDao().insert(
                NotificationEntity(
                    titleAr = "تم استلام طلبك ${draft.orderNumber}",
                    titleFr = "Commande ${draft.orderNumber} reçue",
                    bodyAr = "شكراً لثقتك بمذاق! سنتصل بك قريباً لتأكيد الطلب.",
                    bodyFr = "Merci pour votre confiance ! Nous vous appellerons pour confirmer.",
                    type = NotificationEntity.TYPE_ORDER,
                    orderId = id,
                    createdAt = now,
                )
            )
            draft.copy(id = id)
        }
        submitRemote(order, cart)
        return order
    }

    /** Best effort: the order is already safe locally, a backend can pick it up later. */
    private suspend fun submitRemote(order: OrderEntity, cart: CartSummary) {
        if (!RemoteConfig.isEnabled) return
        try {
            withTimeout(10_000) {
                api.submitOrder(
                    OrderRequestDto(
                        order.orderNumber, order.customerName, order.phone, order.wilaya, order.commune,
                        order.address, order.notes, order.paymentMethod,
                        cart.lines.map { OrderLineDto(it.variant.id, it.quantity) }, order.totalDzd,
                    )
                )
            }
        } catch (_: Exception) {
            // Offline: keep the local order.
        }
    }

    /** Moves an order along its lifecycle. Invalid transitions are rejected. */
    suspend fun updateStatus(orderId: Long, target: OrderStatus, note: String = ""): Boolean = db.withTransaction {
        val order = orderDao.getOrder(orderId) ?: return@withTransaction false
        if (!order.status.canTransitionTo(target)) return@withTransaction false
        val now = System.currentTimeMillis()
        orderDao.updateStatus(orderId, target, now)
        orderDao.insertEvent(OrderStatusEventEntity(orderId = orderId, status = target, note = note, timestamp = now))
        if (target == OrderStatus.CANCELLED) {
            // Return the reserved quantities to the inventory.
            orderDao.getItems(orderId).forEach { db.catalogDao().adjustStock(it.variantId, it.quantity) }
        }
        val (ar, fr) = statusMessage(target)
        db.notificationDao().insert(
            NotificationEntity(
                titleAr = "طلبك ${order.orderNumber}", titleFr = "Commande ${order.orderNumber}",
                bodyAr = ar, bodyFr = fr, type = NotificationEntity.TYPE_ORDER, orderId = orderId, createdAt = now,
            )
        )
        true
    }

    suspend fun cancel(orderId: Long) = updateStatus(orderId, OrderStatus.CANCELLED)

    private fun statusMessage(status: OrderStatus): Pair<String, String> = when (status) {
        OrderStatus.NEW -> "تم استلام طلبك." to "Votre commande a été reçue."
        OrderStatus.CONFIRMED -> "تم تأكيد طلبك ✅" to "Votre commande est confirmée ✅"
        OrderStatus.PREPARING -> "نقوم الآن بتحضير توابلك الطازجة 🌶️" to "Nous préparons vos épices 🌶️"
        OrderStatus.OUT_FOR_DELIVERY -> "طلبك في الطريق إليك 🚚" to "Votre commande est en route 🚚"
        OrderStatus.DELIVERED -> "تم توصيل طلبك. بالهناء والشفاء!" to "Commande livrée. Bon appétit !"
        OrderStatus.CANCELLED -> "تم إلغاء طلبك." to "Votre commande a été annulée."
    }

    private fun generateOrderNumber(now: Long): String =
        "MDK-" + (now % 100_000_000).toString().padStart(8, '0').takeLast(6) + (10..99).random()
}
