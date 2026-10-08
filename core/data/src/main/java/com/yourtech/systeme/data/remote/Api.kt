package com.yourtech.systeme.data.remote

import com.yourtech.systeme.data.BuildConfig
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Contract for the future shared backend (Supabase / Firebase / REST). Until it is configured the
 * apps run entirely on their local Room database and contact the company through WhatsApp/phone.
 */
interface YourTechApi {
    @GET("v1/catalog") suspend fun catalog(): Map<String, Any>
    @POST("v1/requests") suspend fun submitRequest(@Body body: Map<String, Any?>): Map<String, Any>
}

object RemoteConfig {
    val baseUrl: String = BuildConfig.YT_API_BASE_URL
    val isEnabled: Boolean get() = !baseUrl.contains(".example")
}
