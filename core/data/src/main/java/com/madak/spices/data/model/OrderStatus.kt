package com.madak.spices.data.model

/**
 * Order lifecycle. The happy path is NEW → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED.
 * An order can be CANCELLED until it leaves the store.
 */
enum class OrderStatus {
    NEW,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    val isTerminal: Boolean get() = this == DELIVERED || this == CANCELLED
    val isPending: Boolean get() = !isTerminal

    /** The next step on the happy path, or null when the order is finished. */
    fun next(): OrderStatus? = when (this) {
        NEW -> CONFIRMED
        CONFIRMED -> PREPARING
        PREPARING -> OUT_FOR_DELIVERY
        OUT_FOR_DELIVERY -> DELIVERED
        DELIVERED, CANCELLED -> null
    }

    fun canTransitionTo(target: OrderStatus): Boolean = when (target) {
        CANCELLED -> this == NEW || this == CONFIRMED || this == PREPARING
        else -> next() == target
    }

    companion object {
        /** Ordered steps shown on the tracking timeline. */
        val timeline = listOf(NEW, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED)
        val pendingStatuses = listOf(NEW, CONFIRMED, PREPARING, OUT_FOR_DELIVERY)
    }
}
