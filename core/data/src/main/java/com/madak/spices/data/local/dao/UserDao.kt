package com.madak.spices.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.madak.spices.data.local.entity.AddressEntity
import com.madak.spices.data.local.entity.FavoriteEntity
import com.madak.spices.data.local.entity.NotificationEntity
import com.madak.spices.data.local.entity.OfferEntity
import com.madak.spices.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Upsert
    suspend fun upsertUser(user: UserEntity): Long

    @Query("SELECT * FROM addresses WHERE isDefault = 1 LIMIT 1")
    fun observeDefaultAddress(): Flow<AddressEntity?>

    @Query("SELECT * FROM addresses WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultAddress(): AddressEntity?

    @Query("UPDATE addresses SET isDefault = 0")
    suspend fun clearDefaultAddress()

    @Upsert
    suspend fun upsertAddress(address: AddressEntity): Long
}

@Dao
interface FavoriteDao {
    @Query("SELECT productId FROM favorites")
    fun observeFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE productId = :productId)")
    suspend fun isFavorite(productId: String): Boolean

    @Insert
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun delete(productId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers WHERE isActive = 1 AND (endsAt IS NULL OR endsAt > :now) ORDER BY startsAt DESC")
    fun observeActive(now: Long = System.currentTimeMillis()): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers ORDER BY startsAt DESC")
    fun observeAll(): Flow<List<OfferEntity>>

    @Upsert
    suspend fun upsert(offer: OfferEntity)

    @Upsert
    suspend fun upsertAll(offers: List<OfferEntity>)

    @Query("UPDATE offers SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: String, active: Boolean)

    @Query("DELETE FROM offers WHERE id = :id")
    suspend fun delete(id: String)
}
