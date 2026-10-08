package com.madak.spices.data.repository

import com.madak.spices.data.local.dao.NotificationDao
import com.madak.spices.data.local.dao.OfferDao
import com.madak.spices.data.local.dao.UserDao
import com.madak.spices.data.local.entity.AddressEntity
import com.madak.spices.data.local.entity.NotificationEntity
import com.madak.spices.data.local.entity.OfferEntity
import com.madak.spices.data.local.entity.UserEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {
    val currentUser: Flow<UserEntity?> = userDao.observeCurrentUser()
    val defaultAddress: Flow<AddressEntity?> = userDao.observeDefaultAddress()

    suspend fun saveProfile(fullName: String, phone: String, email: String?) {
        val existing = userDao.getCurrentUser()
        userDao.upsertUser(
            (existing ?: UserEntity(fullName = fullName, phone = phone, isCurrent = true))
                .copy(fullName = fullName.trim(), phone = phone.trim(), email = email?.trim()?.ifBlank { null })
        )
    }
}

@Singleton
class NotificationRepository @Inject constructor(private val dao: NotificationDao) {
    val notifications: Flow<List<NotificationEntity>> = dao.observeAll()
    val unreadCount: Flow<Int> = dao.observeUnreadCount()

    suspend fun markAllRead() = dao.markAllRead()
    suspend fun delete(id: Long) = dao.delete(id)

    /** Admin broadcast. Delivered locally today; a push provider (FCM) can relay it later. */
    suspend fun broadcast(titleAr: String, bodyAr: String, titleFr: String = "", bodyFr: String = "", type: String = NotificationEntity.TYPE_GENERAL) =
        dao.insert(NotificationEntity(titleAr = titleAr, titleFr = titleFr, bodyAr = bodyAr, bodyFr = bodyFr, type = type))
}

@Singleton
class OfferRepository @Inject constructor(private val dao: OfferDao) {
    val active: Flow<List<OfferEntity>> = dao.observeActive()
    val all: Flow<List<OfferEntity>> = dao.observeAll()

    suspend fun save(offer: OfferEntity) = dao.upsert(offer)
    suspend fun setActive(id: String, active: Boolean) = dao.setActive(id, active)
    suspend fun delete(id: String) = dao.delete(id)
}
