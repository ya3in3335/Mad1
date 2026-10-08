package com.madak.spices.data

import com.madak.spices.data.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderStatusTest {
    @Test
    fun `happy path follows the lifecycle`() {
        var status = OrderStatus.NEW
        val visited = mutableListOf(status)
        while (true) {
            val next = status.next() ?: break
            assertTrue(status.canTransitionTo(next))
            status = next
            visited += status
        }
        assertEquals(OrderStatus.timeline, visited)
    }

    @Test
    fun `steps cannot be skipped or reversed`() {
        assertFalse(OrderStatus.NEW.canTransitionTo(OrderStatus.DELIVERED))
        assertFalse(OrderStatus.PREPARING.canTransitionTo(OrderStatus.CONFIRMED))
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.NEW))
    }

    @Test
    fun `cancellation only before the order leaves the store`() {
        assertTrue(OrderStatus.NEW.canTransitionTo(OrderStatus.CANCELLED))
        assertTrue(OrderStatus.PREPARING.canTransitionTo(OrderStatus.CANCELLED))
        assertFalse(OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.CANCELLED))
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED))
        assertNull(OrderStatus.CANCELLED.next())
    }
}
