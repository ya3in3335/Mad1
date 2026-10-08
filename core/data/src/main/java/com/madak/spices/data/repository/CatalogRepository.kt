package com.madak.spices.data.repository

import com.madak.spices.data.local.dao.CatalogDao
import com.madak.spices.data.local.dao.FavoriteDao
import com.madak.spices.data.local.dao.OfferDao
import com.madak.spices.data.local.entity.CategoryEntity
import com.madak.spices.data.local.entity.FavoriteEntity
import com.madak.spices.data.local.entity.OfferEntity
import com.madak.spices.data.model.Dish
import com.madak.spices.data.model.Product
import com.madak.spices.data.model.toProduct
import com.madak.spices.data.remote.MadakApi
import com.madak.spices.data.remote.RemoteConfig
import com.madak.spices.data.seed.StoreInitializer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout

@Singleton
class CatalogRepository @Inject constructor(
    private val catalogDao: CatalogDao,
    private val favoriteDao: FavoriteDao,
    private val offerDao: OfferDao,
    private val api: MadakApi,
    private val seeder: StoreInitializer,
) {
    private val favoriteIds: Flow<Set<String>> = favoriteDao.observeFavoriteIds().map { it.toSet() }

    val categories: Flow<List<CategoryEntity>> = catalogDao.observeActiveCategories()

    val products: Flow<List<Product>> =
        combine(catalogDao.observeActiveProducts(), favoriteIds) { list, favs -> list.map { it.toProduct(favs) } }

    val favorites: Flow<List<Product>> = products.map { list -> list.filter { it.isFavorite } }

    val activeOffers: Flow<List<OfferEntity>> = offerDao.observeActive()

    fun product(id: String): Flow<Product?> =
        combine(catalogDao.observeProduct(id), favoriteIds) { p, favs -> p?.toProduct(favs) }

    fun search(query: String): Flow<List<Product>> =
        combine(catalogDao.search(query.trim()), favoriteIds) { list, favs -> list.map { it.toProduct(favs) } }

    /** Catalogue products that suit a dish, most relevant first (see [Dish.keywords]). */
    fun recommendationsFor(dish: Dish): Flow<List<Product>> = products.map { list ->
        list.mapNotNull { p -> dish.relevance(p.entity.nameAr, p.entity.nameFr, p.entity.tags)?.let { it to p } }
            .sortedBy { it.first }
            .map { it.second }
    }

    suspend fun toggleFavorite(productId: String) {
        if (favoriteDao.isFavorite(productId)) favoriteDao.delete(productId)
        else favoriteDao.insert(FavoriteEntity(productId))
    }

    /**
     * Pull-to-refresh. Always re-validates the local catalogue, then tries the remote API when one
     * is configured. Network failures never propagate: the offline catalogue stays authoritative.
     */
    suspend fun refresh(): RefreshResult {
        seeder.seedIfEmpty()
        if (!RemoteConfig.isEnabled) {
            delay(500)
            return RefreshResult.LOCAL_ONLY
        }
        return try {
            val remote = withTimeout(10_000) { api.catalog() }
            remote.products.forEach { p ->
                catalogDao.setProductActive(p.id, p.isActive)
                p.variants.forEach { v ->
                    catalogDao.updatePrice(v.id, v.priceDzd)
                }
            }
            RefreshResult.SYNCED
        } catch (e: Exception) {
            RefreshResult.OFFLINE
        }
    }

    enum class RefreshResult { SYNCED, LOCAL_ONLY, OFFLINE }
}
