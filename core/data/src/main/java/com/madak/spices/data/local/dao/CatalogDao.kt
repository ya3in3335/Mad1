package com.madak.spices.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.madak.spices.data.local.entity.CategoryEntity
import com.madak.spices.data.local.entity.InventoryRow
import com.madak.spices.data.local.entity.ProductEntity
import com.madak.spices.data.local.entity.ProductVariantEntity
import com.madak.spices.data.local.entity.ProductWithVariants
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT COUNT(*) FROM products")
    suspend fun productCount(): Int

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder")
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Transaction
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY isBestSeller DESC, nameAr")
    fun observeActiveProducts(): Flow<List<ProductWithVariants>>

    @Transaction
    @Query("SELECT * FROM products ORDER BY nameAr")
    fun observeAllProducts(): Flow<List<ProductWithVariants>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    fun observeProduct(id: String): Flow<ProductWithVariants?>

    @Transaction
    @Query(
        """
        SELECT * FROM products
        WHERE isActive = 1 AND (nameAr LIKE '%' || :query || '%'
            OR nameFr LIKE '%' || :query || '%'
            OR tags LIKE '%' || :query || '%'
            OR descriptionAr LIKE '%' || :query || '%')
        ORDER BY isBestSeller DESC, nameAr
        """
    )
    fun search(query: String): Flow<List<ProductWithVariants>>

    @Query("SELECT * FROM product_variants WHERE id = :id")
    suspend fun getVariant(id: String): ProductVariantEntity?

    @Query(
        """
        SELECT v.id AS variantId, v.productId, p.nameAr AS productNameAr, p.nameFr AS productNameFr,
               v.weightGrams, v.priceDzd, v.stock, v.lowStockThreshold, v.sku
        FROM product_variants v INNER JOIN products p ON p.id = v.productId
        ORDER BY (v.stock <= v.lowStockThreshold) DESC, p.nameAr, v.weightGrams
        """
    )
    fun observeInventory(): Flow<List<InventoryRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariantEntity>)

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity)

    @Upsert
    suspend fun upsertProduct(product: ProductEntity)

    @Upsert
    suspend fun upsertVariant(variant: ProductVariantEntity)

    @Query("UPDATE products SET isActive = :active WHERE id = :id")
    suspend fun setProductActive(id: String, active: Boolean)

    @Query("UPDATE categories SET isActive = :active WHERE id = :id")
    suspend fun setCategoryActive(id: String, active: Boolean)

    @Query("UPDATE product_variants SET stock = MAX(0, stock + :delta) WHERE id = :variantId")
    suspend fun adjustStock(variantId: String, delta: Int)

    @Query("UPDATE product_variants SET priceDzd = :price WHERE id = :variantId")
    suspend fun updatePrice(variantId: String, price: Int)
}
