package com.madak.spices.data.seed

import androidx.room.withTransaction
import com.madak.spices.data.local.MadakDatabase
import com.madak.spices.data.local.entity.CartEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Prepares an empty store on first launch (active cart + category shelves). Idempotent. */
@Singleton
class StoreInitializer @Inject constructor(private val db: MadakDatabase) {
    private val mutex = Mutex()

    suspend fun seedIfEmpty() = mutex.withLock {
        db.withTransaction {
            db.cartDao().ensureCart(CartEntity())
            if (db.catalogDao().observeAllCategories().first().isEmpty()) {
                db.catalogDao().insertCategories(CatalogDefaults.categories)
            }
        }
    }
}
