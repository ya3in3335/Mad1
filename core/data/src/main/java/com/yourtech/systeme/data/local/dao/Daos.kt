package com.yourtech.systeme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
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
import com.yourtech.systeme.data.local.entity.RequestWithDetails
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity
import com.yourtech.systeme.data.local.entity.ServiceRequestEntity
import com.yourtech.systeme.data.local.entity.TestimonialEntity
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.RequestType
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    // Service categories
    @Query("SELECT * FROM service_categories WHERE isActive = 1 ORDER BY sortOrder")
    fun activeServices(): Flow<List<ServiceCategoryEntity>>

    @Query("SELECT * FROM service_categories ORDER BY sortOrder")
    fun allServices(): Flow<List<ServiceCategoryEntity>>

    @Query("SELECT * FROM service_categories WHERE id = :id")
    fun service(id: String): Flow<ServiceCategoryEntity?>

    @Query("SELECT * FROM service_categories WHERE id = :id")
    suspend fun getService(id: String): ServiceCategoryEntity?

    @Query("SELECT COUNT(*) FROM service_categories")
    suspend fun serviceCount(): Int

    @Upsert suspend fun upsertService(item: ServiceCategoryEntity)
    @Query("DELETE FROM service_categories WHERE id IN (:ids)") suspend fun deleteServices(ids: List<String>)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertServices(items: List<ServiceCategoryEntity>)

    // Product categories
    @Query("SELECT * FROM product_categories WHERE isActive = 1 ORDER BY sortOrder")
    fun activeProductCategories(): Flow<List<ProductCategoryEntity>>

    @Query("SELECT * FROM product_categories ORDER BY sortOrder")
    fun allProductCategories(): Flow<List<ProductCategoryEntity>>

    @Upsert suspend fun upsertProductCategory(item: ProductCategoryEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertProductCategories(items: List<ProductCategoryEntity>)

    @Query("SELECT COUNT(*) FROM products WHERE categoryId = :categoryId")
    suspend fun productsInCategory(categoryId: String): Int

    @Query("DELETE FROM product_categories WHERE id = :id")
    suspend fun deleteProductCategory(id: String)

    // Products
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY isFeatured DESC, updatedAt DESC")
    fun activeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun allProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun product(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProduct(id: String): ProductEntity?

    @Query(
        """SELECT * FROM products WHERE isActive = 1 AND (name LIKE '%' || :q || '%' OR brand LIKE '%' || :q || '%'
           OR model LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%' OR specs LIKE '%' || :q || '%')
           ORDER BY isFeatured DESC, name"""
    )
    fun search(q: String): Flow<List<ProductEntity>>

    @Upsert suspend fun upsertProduct(item: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    // Favorites
    @Query("SELECT productId FROM favorites") fun favoriteIds(): Flow<List<String>>
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE productId = :id)") suspend fun isFavorite(id: String): Boolean
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addFavorite(f: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE productId = :id") suspend fun removeFavorite(id: String)
}

@Dao
interface RequestDao {
    @Insert suspend fun insert(request: ServiceRequestEntity): Long
    @Insert suspend fun insertPhotos(photos: List<RequestPhotoEntity>)
    @Insert suspend fun insertEvent(event: RequestEventEntity)
    @Upsert suspend fun update(request: ServiceRequestEntity)

    @Transaction
    @Query("SELECT * FROM service_requests ORDER BY createdAt DESC")
    fun all(): Flow<List<RequestWithDetails>>

    @Transaction
    @Query("SELECT * FROM service_requests WHERE type IN (:types) ORDER BY createdAt DESC")
    fun byTypes(types: List<RequestType>): Flow<List<RequestWithDetails>>

    @Transaction
    @Query("SELECT * FROM service_requests WHERE id = :id")
    fun request(id: Long): Flow<RequestWithDetails?>

    @Query("SELECT * FROM service_requests WHERE id = :id")
    suspend fun get(id: Long): ServiceRequestEntity?

    @Transaction
    @Query("SELECT * FROM service_requests WHERE scheduledAt IS NOT NULL AND status NOT IN ('COMPLETED','CANCELLED') ORDER BY scheduledAt")
    fun appointments(): Flow<List<RequestWithDetails>>

    @Query("SELECT COUNT(*) FROM service_requests WHERE status = :status")
    fun countByStatus(status: RequestStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM service_requests WHERE createdAt >= :since")
    fun countSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM service_requests WHERE status NOT IN ('COMPLETED','CANCELLED')")
    fun openCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM service_requests WHERE scheduledAt BETWEEN :from AND :to AND status NOT IN ('COMPLETED','CANCELLED')")
    fun scheduledBetween(from: Long, to: Long): Flow<Int>
}

@Dao
interface ContentDao {
    // Projects
    @Query("SELECT * FROM projects WHERE isPublished = 1 AND clientPermissionConfirmed = 1 ORDER BY COALESCE(completedAt, createdAt) DESC")
    fun publishedProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun allProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id") fun project(id: Long): Flow<ProjectEntity?>
    @Query("SELECT * FROM projects WHERE id = :id") suspend fun getProject(id: Long): ProjectEntity?
    @Upsert suspend fun upsertProject(p: ProjectEntity): Long
    @Query("DELETE FROM projects WHERE id = :id") suspend fun deleteProject(id: Long)

    // Promotions
    @Query("SELECT * FROM promotions WHERE isActive = 1 AND startsAt <= :now AND (endsAt IS NULL OR endsAt > :now) ORDER BY startsAt DESC")
    fun activePromotions(now: Long = System.currentTimeMillis()): Flow<List<PromotionEntity>>

    @Query("SELECT * FROM promotions ORDER BY startsAt DESC") fun allPromotions(): Flow<List<PromotionEntity>>
    @Upsert suspend fun upsertPromotion(p: PromotionEntity)
    @Query("DELETE FROM promotions WHERE id = :id") suspend fun deletePromotion(id: Long)

    // Testimonials
    @Query("SELECT * FROM testimonials WHERE isVerified = 1 ORDER BY createdAt DESC") fun verifiedTestimonials(): Flow<List<TestimonialEntity>>
    @Query("SELECT * FROM testimonials ORDER BY createdAt DESC") fun allTestimonials(): Flow<List<TestimonialEntity>>
    @Upsert suspend fun upsertTestimonial(t: TestimonialEntity)
    @Query("DELETE FROM testimonials WHERE id = :id") suspend fun deleteTestimonial(id: Long)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC") fun notifications(): Flow<List<NotificationEntity>>
    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0") fun unreadCount(): Flow<Int>
    @Insert suspend fun insertNotification(n: NotificationEntity)
    @Query("UPDATE notifications SET isRead = 1") suspend fun markAllRead()

    // Business info & profile
    @Query("SELECT * FROM business_info WHERE id = 1") fun businessInfo(): Flow<BusinessInfoEntity?>
    @Query("SELECT * FROM business_info WHERE id = 1") suspend fun getBusinessInfo(): BusinessInfoEntity?
    @Upsert suspend fun upsertBusinessInfo(info: BusinessInfoEntity)
    @Query("SELECT * FROM profile WHERE id = 1") fun profile(): Flow<ProfileEntity?>
    @Query("SELECT * FROM profile WHERE id = 1") suspend fun getProfile(): ProfileEntity?
    @Upsert suspend fun upsertProfile(p: ProfileEntity)
}

@Dao
interface AdminDao {
    @Query("SELECT COUNT(*) FROM admin_accounts") fun accountCount(): Flow<Int>
    @Query("SELECT COUNT(*) FROM admin_accounts") suspend fun accountCountNow(): Int
    @Query("SELECT * FROM admin_accounts ORDER BY createdAt") fun accounts(): Flow<List<AdminAccountEntity>>
    @Query("SELECT * FROM admin_accounts WHERE username = :username COLLATE NOCASE LIMIT 1") suspend fun byUsername(username: String): AdminAccountEntity?
    @Query("SELECT * FROM admin_accounts WHERE id = :id") suspend fun byId(id: Long): AdminAccountEntity?
    @Insert suspend fun insertAccount(a: AdminAccountEntity): Long
    @Upsert suspend fun updateAccount(a: AdminAccountEntity)

    @Insert suspend fun log(entry: AuditLogEntity)
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT :limit") fun auditLog(limit: Int = 300): Flow<List<AuditLogEntity>>
}
