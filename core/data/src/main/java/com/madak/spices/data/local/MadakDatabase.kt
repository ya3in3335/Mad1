package com.madak.spices.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.madak.spices.data.local.dao.CartDao
import com.madak.spices.data.local.dao.CatalogDao
import com.madak.spices.data.local.dao.FavoriteDao
import com.madak.spices.data.local.dao.NotificationDao
import com.madak.spices.data.local.dao.OfferDao
import com.madak.spices.data.local.dao.OrderDao
import com.madak.spices.data.local.dao.UserDao
import com.madak.spices.data.local.entity.AddressEntity
import com.madak.spices.data.local.entity.CartEntity
import com.madak.spices.data.local.entity.CartItemEntity
import com.madak.spices.data.local.entity.CategoryEntity
import com.madak.spices.data.local.entity.FavoriteEntity
import com.madak.spices.data.local.entity.NotificationEntity
import com.madak.spices.data.local.entity.OfferEntity
import com.madak.spices.data.local.entity.OrderEntity
import com.madak.spices.data.local.entity.OrderItemEntity
import com.madak.spices.data.local.entity.OrderStatusEventEntity
import com.madak.spices.data.local.entity.ProductEntity
import com.madak.spices.data.local.entity.ProductVariantEntity
import com.madak.spices.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        ProductVariantEntity::class,
        CartEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        OrderStatusEventEntity::class,
        AddressEntity::class,
        FavoriteEntity::class,
        NotificationEntity::class,
        OfferEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MadakDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun notificationDao(): NotificationDao
    abstract fun offerDao(): OfferDao

    companion object {
        const val NAME = "madak.db"

        /** v2: products.photoKey (bundled real product photo picked by the owner). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN photoKey TEXT")
            }
        }
    }
}
