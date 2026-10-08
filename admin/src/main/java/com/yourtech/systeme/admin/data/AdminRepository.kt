package com.yourtech.systeme.admin.data

import androidx.room.withTransaction
import com.yourtech.systeme.admin.auth.AdminAuthRepository
import com.yourtech.systeme.admin.auth.AdminPermission
import com.yourtech.systeme.admin.auth.AdminUser
import com.yourtech.systeme.data.local.YourTechDatabase
import com.yourtech.systeme.data.local.entity.AuditLogEntity
import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import com.yourtech.systeme.data.local.entity.ProductCategoryEntity
import com.yourtech.systeme.data.local.entity.ProductEntity
import com.yourtech.systeme.data.local.entity.PromotionEntity
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity
import com.yourtech.systeme.data.local.entity.TestimonialEntity
import com.yourtech.systeme.data.media.ImageImporter
import com.yourtech.systeme.data.model.BusinessDefaults
import com.yourtech.systeme.data.model.BusinessHours
import com.yourtech.systeme.data.model.DashboardStats
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.StockStatus
import com.yourtech.systeme.data.model.Validation
import com.yourtech.systeme.data.repository.RequestRepository
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AdminException(message: String) : Exception(message)

/**
 * Every back-office write goes through here: permission check → input validation → write → audit log.
 * When a backend is added, the same checks must also be enforced server-side (RLS / security rules).
 */
@Singleton
class AdminRepository @Inject constructor(
    private val db: YourTechDatabase,
    private val auth: AdminAuthRepository,
    private val requestsRepo: RequestRepository,
    private val images: ImageImporter,
) {
    private val catalog get() = db.catalogDao()
    private val content get() = db.contentDao()
    private val requestsDao get() = db.requestDao()

    // ---- Reads --------------------------------------------------------------------------------
    val requests = requestsDao.all()
    fun request(id: Long) = requestsDao.request(id)
    val appointments = requestsDao.appointments()
    val products: Flow<List<ProductEntity>> = catalog.allProducts()
    fun product(id: String) = catalog.product(id)
    val productCategories = catalog.allProductCategories()
    val services = catalog.allServices()
    fun service(id: String) = catalog.service(id)
    val promotions = content.allPromotions()
    val testimonials = content.allTestimonials()
    val business: Flow<BusinessInfoEntity> = content.businessInfo().map { it ?: BusinessDefaults.info }
    val auditLog = db.adminDao().auditLog()

    fun stats(now: Long = System.currentTimeMillis()): Flow<DashboardStats> {
        val start = startOfDay(now)
        val end = start + 24 * 3600 * 1000L
        return combine(
            requestsDao.countSince(start), requestsDao.openCount(), requestsDao.countByStatus(RequestStatus.SUBMITTED),
            requestsDao.scheduledBetween(start, end), requestsDao.countByStatus(RequestStatus.COMPLETED),
        ) { today, open, submitted, scheduled, done -> DashboardStats(today, open, submitted, scheduled, done) }
    }

    // ---- Requests -----------------------------------------------------------------------------
    suspend fun updateStatus(id: Long, target: RequestStatus, note: String) {
        val u = require(AdminPermission.REQUESTS)
        if (!requestsRepo.updateStatus(id, target, note)) throw AdminException("انتقال غير مسموح لهذه الحالة")
        log(u, "REQUEST_STATUS", "request", id.toString(), target.name)
    }

    suspend fun saveRequestDetails(id: Long, scheduledAt: Long?, quoteDzd: String, note: String, assignedTo: String) {
        val u = require(AdminPermission.REQUESTS)
        val quote = quoteDzd.trim().takeIf { it.isNotEmpty() }?.let { it.toIntOrNull()?.takeIf { v -> v in 0..MAX_PRICE } ?: throw AdminException("مبلغ عرض السعر غير صالح") }
        requestsRepo.saveCompanyDetails(id, scheduledAt, quote, note, assignedTo)
        log(u, "REQUEST_DETAILS", "request", id.toString(), listOfNotNull(scheduledAt?.let { "rdv" }, quote?.let { "devis=$it" }, assignedTo.takeIf { it.isNotBlank() }?.let { "tech=$it" }).joinToString())
    }

    // ---- Products & inventory -----------------------------------------------------------------
    suspend fun saveProduct(p: ProductEntity, isNew: Boolean): String {
        val u = require(AdminPermission.PRODUCTS)
        val name = Validation.clean(p.name, 120)
        if (name.isEmpty()) throw AdminException("أدخل اسم المنتج")
        if (p.priceDzd != null && p.priceDzd !in 0..MAX_PRICE) throw AdminException("السعر غير صالح")
        if (p.stockQty != null && p.stockQty !in 0..1_000_000) throw AdminException("الكمية غير صالحة")
        val url = p.imageUrl?.trim()?.ifEmpty { null }
        if (url != null && !Validation.isHttpsUrl(url)) throw AdminException("رابط الصورة يجب أن يبدأ بـ https://")
        val id = if (isNew) "p_" + UUID.randomUUID().toString().take(12) else p.id
        val old = if (isNew) null else catalog.getProduct(id)
        if (old != null && old.localImagePath != null && old.localImagePath != p.localImagePath) images.delete(old.localImagePath)
        catalog.upsertProduct(
            p.copy(
                id = id, name = name, brand = Validation.clean(p.brand, 60), model = Validation.clean(p.model, 60),
                description = Validation.clean(p.description), specs = Validation.clean(p.specs), warranty = Validation.clean(p.warranty, 200),
                imageUrl = url, createdAt = old?.createdAt ?: System.currentTimeMillis(), updatedAt = System.currentTimeMillis(),
            )
        )
        log(u, if (isNew) "PRODUCT_CREATED" else "PRODUCT_UPDATED", "product", id, name)
        return id
    }

    suspend fun deleteProduct(id: String) {
        val u = require(AdminPermission.PRODUCTS)
        val p = catalog.getProduct(id) ?: return
        catalog.deleteProduct(id)
        images.delete(p.localImagePath)
        log(u, "PRODUCT_DELETED", "product", id, p.name)
    }

    suspend fun setStock(id: String, status: StockStatus, qty: Int?) {
        val u = require(AdminPermission.INVENTORY)
        if (qty != null && qty !in 0..1_000_000) throw AdminException("الكمية غير صالحة")
        val p = catalog.getProduct(id) ?: return
        catalog.upsertProduct(p.copy(stockStatus = status, stockQty = qty, updatedAt = System.currentTimeMillis()))
        log(u, "STOCK_UPDATED", "product", id, "${status.name} ${qty ?: ""}".trim())
    }

    suspend fun importImage(uri: android.net.Uri, folder: String): ImageImporter.Result = images.import(uri, folder)
    fun deleteImage(path: String?) = images.delete(path)

    // ---- Categories & services ----------------------------------------------------------------
    suspend fun saveProductCategory(c: ProductCategoryEntity, isNew: Boolean) {
        val u = require(AdminPermission.CATEGORIES)
        if (c.nameAr.isBlank() || c.nameFr.isBlank()) throw AdminException("أدخل الاسم بالعربية والفرنسية")
        val id = if (isNew) "c_" + UUID.randomUUID().toString().take(8) else c.id
        catalog.upsertProductCategory(c.copy(id = id, nameAr = Validation.clean(c.nameAr, 60), nameFr = Validation.clean(c.nameFr, 60), nameEn = Validation.clean(c.nameEn.ifBlank { c.nameFr }, 60)))
        log(u, if (isNew) "CATEGORY_CREATED" else "CATEGORY_UPDATED", "product_category", id, c.nameAr)
    }

    suspend fun deleteProductCategory(id: String) {
        val u = require(AdminPermission.CATEGORIES)
        if (catalog.productsInCategory(id) > 0) throw AdminException("الفئة تحتوي على منتجات — انقلها أو احذفها أولًا")
        catalog.deleteProductCategory(id)
        log(u, "CATEGORY_DELETED", "product_category", id)
    }

    suspend fun saveService(s: ServiceCategoryEntity) {
        val u = require(AdminPermission.SERVICES)
        if (s.nameAr.isBlank() || s.nameFr.isBlank()) throw AdminException("أدخل الاسم بالعربية والفرنسية")
        val old = catalog.getService(s.id)
        catalog.upsertService(s)
        val action = when {
            old == null -> "SERVICE_CREATED"
            old.isActive != s.isActive -> if (s.isActive) "SERVICE_CONFIRMED" else "SERVICE_HIDDEN"
            else -> "SERVICE_UPDATED"
        }
        log(u, action, "service", s.id, s.nameAr)
    }

    // ---- Portfolio, promotions, testimonials --------------------------------------------------
    suspend fun savePromotion(p: PromotionEntity) {
        val u = require(AdminPermission.PROMOTIONS)
        if (p.title.isBlank()) throw AdminException("أدخل عنوان العرض")
        val ends = p.endsAt; if (ends != null && ends <= p.startsAt) throw AdminException("تاريخ النهاية يجب أن يكون بعد البداية")
        content.upsertPromotion(p.copy(title = Validation.clean(p.title, 100), body = Validation.clean(p.body, 500)))
        log(u, if (p.id == 0L) "PROMOTION_CREATED" else "PROMOTION_UPDATED", "promotion", p.id.toString(), p.title)
    }

    suspend fun deletePromotion(p: PromotionEntity) {
        val u = require(AdminPermission.PROMOTIONS)
        content.deletePromotion(p.id)
        images.delete(p.imagePath)
        log(u, "PROMOTION_DELETED", "promotion", p.id.toString(), p.title)
    }

    suspend fun saveTestimonial(t: TestimonialEntity) {
        val u = require(AdminPermission.TESTIMONIALS)
        if (t.authorName.isBlank() || t.text.isBlank()) throw AdminException("أدخل الاسم والنص")
        content.upsertTestimonial(t.copy(authorName = Validation.clean(t.authorName, 60), text = Validation.clean(t.text, 600), rating = t.rating.coerceIn(1, 5)))
        log(u, if (t.isVerified) "TESTIMONIAL_VERIFIED" else "TESTIMONIAL_SAVED", "testimonial", t.id.toString(), t.authorName)
    }

    suspend fun deleteTestimonial(t: TestimonialEntity) {
        val u = require(AdminPermission.TESTIMONIALS)
        content.deleteTestimonial(t.id)
        log(u, "TESTIMONIAL_DELETED", "testimonial", t.id.toString(), t.authorName)
    }

    // ---- Business info ------------------------------------------------------------------------
    suspend fun saveBusiness(b: BusinessInfoEntity) {
        val u = require(AdminPermission.BUSINESS_INFO)
        val phone = Validation.toInternational(b.phone) ?: throw AdminException("رقم الهاتف غير صالح")
        val wa = Validation.toInternational(b.whatsapp) ?: throw AdminException("رقم واتساب غير صالح")
        if (b.companyName.isBlank()) throw AdminException("أدخل اسم الشركة")
        if (b.latitude !in -90.0..90.0 || b.longitude !in -180.0..180.0) throw AdminException("الإحداثيات غير صالحة")
        if (listOf(b.facebookUrl, b.instagramUrl, b.tiktokUrl, b.websiteUrl).any { !Validation.isHttpsUrl(it) }) throw AdminException("الروابط يجب أن تبدأ بـ https://")
        if (b.email.isNotBlank() && !EMAIL.matches(b.email.trim())) throw AdminException("البريد الإلكتروني غير صالح")
        if (BusinessHours.parse(b.openingHours).isEmpty()) throw AdminException("حدد يوم عمل واحدًا على الأقل")
        db.withTransaction {
            content.upsertBusinessInfo(b.copy(phone = phone, whatsapp = wa, email = b.email.trim(), updatedAt = System.currentTimeMillis()))
            log(u, "BUSINESS_INFO_UPDATED", "business", "1")
        }
    }

    // ---- Helpers ------------------------------------------------------------------------------
    private fun require(permission: AdminPermission): AdminUser {
        val user = auth.session.value ?: throw AdminException("انتهت الجلسة، سجّل الدخول من جديد")
        if (!user.can(permission)) throw AdminException("ليست لديك صلاحية لهذا الإجراء")
        return user
    }

    private suspend fun log(u: AdminUser, action: String, type: String, id: String = "", details: String = "") =
        db.adminDao().log(AuditLogEntity(actor = u.username, role = u.role.name, action = action, entityType = type, entityId = id, details = details.take(300)))

    companion object {
        const val MAX_PRICE = 100_000_000
        private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        fun startOfDay(now: Long): Long = Calendar.getInstance(BusinessHours.ALGIERS).apply {
            timeInMillis = now; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
