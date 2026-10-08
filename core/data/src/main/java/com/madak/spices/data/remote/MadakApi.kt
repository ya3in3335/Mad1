package com.madak.spices.data.remote

import com.madak.spices.data.BuildConfig
import com.madak.spices.data.remote.dto.CatalogDto
import com.madak.spices.data.remote.dto.OrderAckDto
import com.madak.spices.data.remote.dto.OrderRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Contract for a future Madak backend (Supabase edge functions, Firebase, or a custom REST API).
 * The app never requires it: every screen is driven by the local Room database.
 */
interface MadakApi {
    @GET("v1/catalog")
    suspend fun catalog(): CatalogDto

    @POST("v1/orders")
    suspend fun submitOrder(@Body order: OrderRequestDto): OrderAckDto
}

object RemoteConfig {
    val baseUrl: String = BuildConfig.MADAK_API_BASE_URL

    /** The placeholder ".example" domain disables remote calls until a real backend is configured. */
    val isEnabled: Boolean get() = !baseUrl.contains(".example")
}
