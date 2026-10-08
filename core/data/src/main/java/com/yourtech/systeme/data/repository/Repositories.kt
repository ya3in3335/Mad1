package com.yourtech.systeme.data.repository

import androidx.room.withTransaction
import com.yourtech.systeme.data.local.YourTechDatabase
import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import com.yourtech.systeme.data.local.entity.FavoriteEntity
import com.yourtech.systeme.data.local.entity.NotificationEntity
import com.yourtech.systeme.data.local.entity.ProductCategoryEntity
import com.yourtech.systeme.data.local.entity.ProfileEntity
import com.yourtech.systeme.data.local.entity.ProjectEntity
import com.yourtech.systeme.data.local.entity.PromotionEntity
import com.yourtech.systeme.data.local.entity.RequestEventEntity
import com.yourtech.systeme.data.local.entity.RequestPhotoEntity
import com.yourtech.systeme.data.local.entity.RequestWithDetails
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity
import com.yourtech.systeme.data.local.entity.ServiceRequestEntity
import com.yourtech.systeme.data.local.entity.TestimonialEntity
import com.yourtech.systeme.data.model.BusinessDefaults
import com.yourtech.systeme.data.model.Product
import com.yourtech.systeme.data.model.RequestForm
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.RequestType
import com.yourtech.systeme.data.model.Validation
import com.yourtech.systeme.data.seed.SeedContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** First-launch setup: proposed service/product categories and the company details. Idempotent. */
@Singleton
class StoreInitializer @Inject constructor(private val db: YourTechDatabase) {
    private val mutex = Mutex()
    suspend fun ensureInitialized() = mutex.withLock {
        db.withTransaction {
            if (db.catalogDao().serviceCount() == 0) {
                db.catalogDao().insertServices(SeedContent.services)
                db.catalogDao().insertProductCategories(SeedContent.productCategories)
            }
            // The store sells equipment only: no installation, repair or consulting services.
            db.catalogDao().deleteServices(listOf("installation", "consulting"))
            if (db.contentDao().getBusinessInfo() == null) db.contentDao().upsertBusinessInfo(BusinessDefaults.info)
        }
    }
}

@Singleton
class CatalogRepository @Inject constructor(private val db: YourTechDatabase) {
    private val dao get() = db.catalogDao()
    private val favs = dao.favoriteIds().map { it.toSet() }

    val services: Flow<List<ServiceCategoryEntity>> = dao.activeServices()
    fun service(id: String) = dao.service(id)
    val productCategories: Flow<List<ProductCategoryEntity>> = dao.activeProductCategories()

    val products: Flow<List<Product>> = combine(dao.activeProducts(), favs) { list, f -> list.map { Product(it, it.id in f) } }
    val favorites: Flow<List<Product>> = products.map { l -> l.filter { it.isFavorite } }
    fun product(id: String): Flow<Product?> = combine(dao.product(id), favs) { p, f -> p?.let { Product(it, it.id in f) } }
    fun search(q: String): Flow<List<Product>> = combine(dao.search(q.trim()), favs) { l, f -> l.map { Product(it, it.id in f) } }

    suspend fun toggleFavorite(id: String) {
        if (dao.isFavorite(id)) dao.removeFavorite(id) else dao.addFavorite(FavoriteEntity(id))
    }
}

@Singleton
class ContentRepository @Inject constructor(private val db: YourTechDatabase) {
    private val dao get() = db.contentDao()
    val business: Flow<BusinessInfoEntity> = dao.businessInfo().map { it ?: BusinessDefaults.info }
    val projects: Flow<List<ProjectEntity>> = dao.publishedProjects()
    fun project(id: Long) = dao.project(id)
    val promotions: Flow<List<PromotionEntity>> = dao.activePromotions()
    val testimonials: Flow<List<TestimonialEntity>> = dao.verifiedTestimonials()
    val notifications: Flow<List<NotificationEntity>> = dao.notifications()
    val unread: Flow<Int> = dao.unreadCount()
    val profile: Flow<ProfileEntity?> = dao.profile()
    suspend fun markAllRead() = dao.markAllRead()
    suspend fun saveProfile(p: ProfileEntity) = dao.upsertProfile(p)
}

@Singleton
class RequestRepository @Inject constructor(private val db: YourTechDatabase) {
    private val dao get() = db.requestDao()

    val all: Flow<List<RequestWithDetails>> = dao.all()
    fun byTypes(types: List<RequestType>) = dao.byTypes(types)
    fun request(id: Long) = dao.request(id)
    val appointments = dao.appointments()

    /** Saves a validated customer request with its photos, timeline entry and a notification. */
    suspend fun submit(form: RequestForm, now: Long = System.currentTimeMillis()): ServiceRequestEntity {
        require(form.validate(now).isEmpty()) { "Invalid request form" }
        val entity = ServiceRequestEntity(
            reference = reference(form.type, now),
            type = form.type,
            customerName = Validation.clean(form.customerName, 80),
            phone = Validation.normalizePhone(form.phone),
            wilaya = form.wilaya?.storageValue.orEmpty(),
            commune = Validation.clean(form.commune, 80),
            address = Validation.clean(form.address, 300),
            propertyType = form.propertyType,
            systemType = form.systemType,
            deviceCount = form.deviceCount.trim().toIntOrNull(),
            problemDescription = Validation.clean(form.problemDescription),
            productId = form.productId,
            preferredDate = form.preferredDate,
            notes = Validation.clean(form.notes),
            createdAt = now, updatedAt = now,
        )
        return db.withTransaction {
            val id = dao.insert(entity)
            dao.insertPhotos(form.photoPaths.take(RequestForm.MAX_PHOTOS).map { RequestPhotoEntity(requestId = id, path = it) })
            dao.insertEvent(RequestEventEntity(requestId = id, status = RequestStatus.SUBMITTED, timestamp = now))
            db.contentDao().insertNotification(NotificationEntity(title = entity.reference, body = "SUBMITTED", requestId = id, createdAt = now))
            val profile = db.contentDao().getProfile() ?: ProfileEntity()
            db.contentDao().upsertProfile(
                profile.copy(
                    fullName = entity.customerName, phone = entity.phone, wilaya = entity.wilaya.ifBlank { profile.wilaya }, commune = entity.commune.ifBlank { profile.commune },
                    address = entity.address.ifBlank { profile.address },
                )
            )
            entity.copy(id = id)
        }
    }

    /** Moves a request forward (or cancels it). Returns false for an invalid transition. */
    suspend fun updateStatus(id: Long, target: RequestStatus, note: String = "", now: Long = System.currentTimeMillis()): Boolean =
        db.withTransaction {
            val r = dao.get(id) ?: return@withTransaction false
            if (!r.status.canTransitionTo(target)) return@withTransaction false
            dao.update(r.copy(status = target, updatedAt = now))
            dao.insertEvent(RequestEventEntity(requestId = id, status = target, note = Validation.clean(note, 500), timestamp = now))
            db.contentDao().insertNotification(NotificationEntity(title = r.reference, body = target.name, requestId = id, createdAt = now))
            true
        }

    suspend fun cancel(id: Long) = updateStatus(id, RequestStatus.CANCELLED)

    suspend fun saveCompanyDetails(id: Long, scheduledAt: Long?, quoteDzd: Int?, note: String, assignedTo: String) {
        val r = dao.get(id) ?: return
        dao.update(
            r.copy(
                scheduledAt = scheduledAt, quoteAmountDzd = quoteDzd?.takeIf { it >= 0 },
                companyNote = Validation.clean(note, 1000), assignedTo = Validation.clean(assignedTo, 80),
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    companion object {
        fun reference(type: RequestType, now: Long): String {
            val prefix = when (type) {
                RequestType.INSTALLATION -> "INS"
                RequestType.MAINTENANCE -> "SAV"
                RequestType.CONSULTATION -> "CNS"
                RequestType.QUOTE -> "DEV"
            }
            val date = SimpleDateFormat("yyMMdd", Locale.US).format(Date(now))
            return "YT-$prefix-$date-" + (now % 10_000).toString().padStart(4, '0')
        }
    }
}
