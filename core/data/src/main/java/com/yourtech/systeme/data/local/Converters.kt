package com.yourtech.systeme.data.local

import androidx.room.TypeConverter
import com.yourtech.systeme.data.model.PropertyType
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.RequestType
import com.yourtech.systeme.data.model.StockStatus

class Converters {
    @TypeConverter fun fromStatus(v: RequestStatus) = v.name
    @TypeConverter fun toStatus(v: String) = RequestStatus.entries.firstOrNull { it.name == v } ?: RequestStatus.SUBMITTED
    @TypeConverter fun fromType(v: RequestType) = v.name
    @TypeConverter fun toType(v: String) = RequestType.entries.firstOrNull { it.name == v } ?: RequestType.QUOTE
    @TypeConverter fun fromProperty(v: PropertyType?) = v?.name
    @TypeConverter fun toProperty(v: String?) = PropertyType.entries.firstOrNull { it.name == v }
    @TypeConverter fun fromStock(v: StockStatus) = v.name
    @TypeConverter fun toStock(v: String) = StockStatus.entries.firstOrNull { it.name == v } ?: StockStatus.UNKNOWN
}
