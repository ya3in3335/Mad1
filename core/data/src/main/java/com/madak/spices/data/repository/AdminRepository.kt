package com.madak.spices.data.repository

import com.madak.spices.data.local.dao.CatalogDao
import com.madak.spices.data.local.dao.OrderDao
import com.madak.spices.data.local.entity.BestSellerRow
import com.madak.spices.data.local.entity.CategoryEntity
import com.madak.spices.data.local.entity.CustomerRow
import com.madak.spices.data.local.entity.InventoryRow
import com.madak.spices.data.local.entity.ProductEntity
import com.madak.spices.data.local.entity.ProductVariantEntity
import com.madak.spices.data.model.DashboardStats
import com.madak.spices.data.model.Pricing
import com.madak.spices.data.model.Product
import com.madak.spices.data.model.toProduct
import com.madak.spices.data.seed.CatalogDefaults
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Back-office operations used by the Madak Admin app. */
@Singleton
class AdminRepository @Inject constructor(
    private val catalogDao: CatalogDao,
    private val orderDao: OrderDao,
) {
    val allProducts: Flow<List<Product>> = catalogDao.observeAllProducts().map { list -> list.map { it.toProduct() } }
    val allCategories: Flow<List<CategoryEntity>> = catalogDao.observeAllCategories()
    val inventory: Flow<List<InventoryRow>> = catalogDao.observeInventory()
    val customers: Flow<List<CustomerRow>> = orderDao.observeCustomers()
    val bestSellers: Flow<List<BestSellerRow>> = orderDao.observeBestSellers()

    fun dashboardStats(startOfDay: Long = startOfToday()): Flow<DashboardStats> = combine(
        orderDao.observeOrdersSince(startOfDay),
        orderDao.observeSalesSince(startOfDay),
        orderDao.observePendingCount(),
        orderDao.observeCompletedCount(),
    ) { orders, sales, pending, completed -> DashboardStats(orders, sales, pending, completed) }

    suspend fun setProductActive(id: String, active: Boolean) = catalogDao.setProductActive(id, active)
    suspend fun setCategoryActive(id: String, active: Boolean) = catalogDao.setCategoryActive(id, active)
    suspend fun adjustStock(variantId: String, delta: Int) = catalogDao.adjustStock(variantId, delta)
    suspend fun updatePrice(variantId: String, price: Int) = catalogDao.updatePrice(variantId, price.coerceAtLeast(0))
    suspend fun saveCategory(category: CategoryEntity) = catalogDao.upsertCategory(category)

    /** Creates or updates a product and (re)generates its four weight variants from a 100 g price. */
    suspend fun saveProduct(product: ProductEntity, pricePer100g: Int, initialStock: Int) {
        catalogDao.upsertProduct(product)
        Pricing.WEIGHTS.forEach { grams ->
            val id = CatalogDefaults.variantId(product.id, grams)
            val existing = catalogDao.getVariant(id)
            catalogDao.upsertVariant(
                ProductVariantEntity(
                    id = id,
                    productId = product.id,
                    weightGrams = grams,
                    priceDzd = Pricing.variantPrice(pricePer100g, grams),
                    stock = existing?.stock ?: initialStock,
                    sku = "MDK-${product.id.uppercase()}-$grams",
                )
            )
        }
    }

    companion object {
        fun startOfToday(): Long = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
