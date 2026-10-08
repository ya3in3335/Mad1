package com.madak.spices

import com.madak.spices.data.repository.AdminRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Access point for code that cannot use constructor injection (e.g. JVM UI tests). */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface CatalogEntryPoint {
    fun adminRepository(): AdminRepository
}
