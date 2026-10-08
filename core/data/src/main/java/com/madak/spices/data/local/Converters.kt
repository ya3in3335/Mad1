package com.madak.spices.data.local

import androidx.room.TypeConverter
import com.madak.spices.data.model.OrderStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: OrderStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): OrderStatus =
        OrderStatus.entries.firstOrNull { it.name == value } ?: OrderStatus.NEW
}
