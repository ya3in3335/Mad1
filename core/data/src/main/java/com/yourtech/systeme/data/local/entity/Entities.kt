package com.yourtech.systeme.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yourtech.systeme.data.model.PropertyType
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.RequestType
import com.yourtech.systeme.data.model.StockStatus

/**
 * Security solution / service category (CCTV, alarms, access control…). Seeded as a proposal;
 * the company confirms, edits or hides each one from the Admin app.
 */
@Entity(tableName = "service_categories")
data class ServiceCategoryEntity(
    @PrimaryKey val id: String,
    val nameAr: String, val nameFr: String, val nameEn: String,
    val summaryAr: String, val summaryFr: String, val summaryEn: String,
    /** One item per line. */
    val benefitsAr: String = "", val benefitsFr: String = "", val benefitsEn: String = "",
    val useCasesAr: String = "", val useCasesFr: String = "", val useCasesEn: String = "",
    val specsAr: String = "", val specsFr: String = "", val specsEn: String = "",
    val photoKey: String? = null,
    val iconKey: String = "shield",
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
)

@Entity(tableName = "product_categories")
data class ProductCategoryEntity(
    @PrimaryKey val id: String,
    val nameAr: String, val nameFr: String, val nameEn: String,
    val iconKey: String = "camera",
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
)

/** Security equipment. Nothing is pre-filled: products, prices, brands and warranties come from the company. */
@Entity(
    tableName = "products",
    foreignKeys = [ForeignKey(entity = ProductCategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("categoryId")],
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val brand: String = "",
    val model: String = "",
    val description: String = "",
    /** "Key: value" per line, e.g. "Resolution: 4 MP". */
    val specs: String = "",
    /** null = price on request. */
    val priceDzd: Int? = null,
    val stockStatus: StockStatus = StockStatus.UNKNOWN,
    val stockQty: Int? = null,
    /** Free text, only what the company actually offers. Empty = not shown. */
    val warranty: String = "",
    val installationAvailable: Boolean = false,
    val imageUrl: String? = null,
    /** Image uploaded from the admin's gallery (re-encoded, metadata stripped). */
    val localImagePath: String? = null,
    val photoKey: String? = null,
    val isFeatured: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "favorites", foreignKeys = [ForeignKey(entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.CASCADE)])
data class FavoriteEntity(@PrimaryKey val productId: String, val addedAt: Long = System.currentTimeMillis())

/** Installation, maintenance, consultation or quotation request (customer ↔ company). */
@Entity(tableName = "service_requests", indices = [Index(value = ["reference"], unique = true), Index("status"), Index("type"), Index("scheduledAt")])
data class ServiceRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reference: String,
    val type: RequestType,
    val status: RequestStatus = RequestStatus.SUBMITTED,
    val customerName: String,
    val phone: String,
    val wilaya: String,
    val commune: String,
    val address: String = "",
    val propertyType: PropertyType? = null,
    /** Service category id (installation) or equipment category (maintenance). */
    val systemType: String? = null,
    val deviceCount: Int? = null,
    val problemDescription: String = "",
    val productId: String? = null,
    val preferredDate: Long? = null,
    val notes: String = "",
    val scheduledAt: Long? = null,
    val quoteAmountDzd: Int? = null,
    val companyNote: String = "",
    val assignedTo: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "request_photos",
    foreignKeys = [ForeignKey(entity = ServiceRequestEntity::class, parentColumns = ["id"], childColumns = ["requestId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("requestId")],
)
data class RequestPhotoEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val requestId: Long, val path: String)

@Entity(
    tableName = "request_events",
    foreignKeys = [ForeignKey(entity = ServiceRequestEntity::class, parentColumns = ["id"], childColumns = ["requestId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("requestId")],
)
data class RequestEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: Long,
    val status: RequestStatus,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

/** Completed installation shown in the portfolio. Only company-approved, non-sensitive content. */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val serviceCategoryId: String? = null,
    /** General area only (e.g. "Oran"), never an exact address. */
    val generalLocation: String = "",
    val description: String = "",
    val equipment: String = "",
    /** Newline-separated local paths or https URLs. */
    val photos: String = "",
    val clientPermissionConfirmed: Boolean = false,
    val isPublished: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String = "",
    val imagePath: String? = null,
    val serviceCategoryId: String? = null,
    val productId: String? = null,
    val isActive: Boolean = true,
    val startsAt: Long = System.currentTimeMillis(),
    val endsAt: Long? = null,
)

/** Customer testimonial — displayed only once verified by the company. */
@Entity(tableName = "testimonials")
data class TestimonialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val text: String,
    val rating: Int = 5,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "notifications", indices = [Index("createdAt")])
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val requestId: Long? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Company details (single row). Defaults come from [com.yourtech.systeme.data.model.BusinessDefaults]. */
@Entity(tableName = "business_info")
data class BusinessInfoEntity(
    @PrimaryKey val id: Int = 1,
    val companyName: String,
    val taglineAr: String, val taglineFr: String, val taglineEn: String,
    val phone: String,
    val whatsapp: String,
    val email: String = "",
    val addressAr: String, val addressFr: String, val addressEn: String,
    val latitude: Double,
    val longitude: Double,
    /** "6:08:00-17:30,0:08:00-17:30,…" → see BusinessHours. */
    val openingHours: String,
    val facebookUrl: String = "",
    val instagramUrl: String = "",
    val tiktokUrl: String = "",
    val websiteUrl: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Customer's own profile on this device. */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "",
    val phone: String = "",
    val wilaya: String = "",
    val commune: String = "",
    val address: String = "",
)

// ---- Admin-only tables -----------------------------------------------------------------------

@Entity(tableName = "admin_accounts", indices = [Index(value = ["username"], unique = true)])
data class AdminAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val displayName: String,
    val role: String,
    val hashAlgorithm: String,
    val iterations: Int,
    val saltHex: String,
    val hashHex: String,
    val failedAttempts: Int = 0,
    val lockedUntil: Long = 0,
    val mustChangePassword: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "audit_log", indices = [Index("timestamp")])
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actor: String,
    val role: String,
    val action: String,
    val entityType: String,
    val entityId: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)
