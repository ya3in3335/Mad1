package com.yourtech.systeme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yourtech.systeme.data.local.dao.AdminDao
import com.yourtech.systeme.data.local.dao.CatalogDao
import com.yourtech.systeme.data.local.dao.ContentDao
import com.yourtech.systeme.data.local.dao.RequestDao
import com.yourtech.systeme.data.local.entity.AdminAccountEntity
import com.yourtech.systeme.data.local.entity.AuditLogEntity
import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import com.yourtech.systeme.data.local.entity.FavoriteEntity
import com.yourtech.systeme.data.local.entity.NotificationEntity
import com.yourtech.systeme.data.local.entity.ProductCategoryEntity
import com.yourtech.systeme.data.local.entity.ProductEntity
import com.yourtech.systeme.data.local.entity.ProfileEntity
import com.yourtech.systeme.data.local.entity.ProjectEntity
import com.yourtech.systeme.data.local.entity.PromotionEntity
import com.yourtech.systeme.data.local.entity.RequestEventEntity
import com.yourtech.systeme.data.local.entity.RequestPhotoEntity
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity
import com.yourtech.systeme.data.local.entity.ServiceRequestEntity
import com.yourtech.systeme.data.local.entity.TestimonialEntity

@Database(
    entities = [
        ServiceCategoryEntity::class, ProductCategoryEntity::class, ProductEntity::class, FavoriteEntity::class,
        ServiceRequestEntity::class, RequestPhotoEntity::class, RequestEventEntity::class,
        ProjectEntity::class, PromotionEntity::class, TestimonialEntity::class, NotificationEntity::class,
        BusinessInfoEntity::class, ProfileEntity::class, AdminAccountEntity::class, AuditLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class YourTechDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun requestDao(): RequestDao
    abstract fun contentDao(): ContentDao
    abstract fun adminDao(): AdminDao

    companion object { const val NAME = "yourtech.db" }
}
