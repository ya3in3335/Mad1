package com.yourtech.systeme.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RequestWithDetails(
    @Embedded val request: ServiceRequestEntity,
    @Relation(parentColumn = "id", entityColumn = "requestId") val photos: List<RequestPhotoEntity>,
    @Relation(parentColumn = "id", entityColumn = "requestId") val events: List<RequestEventEntity>,
)

data class CountByStatus(val status: String, val count: Int)
