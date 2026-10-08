package com.yourtech.systeme.data.di

import android.content.Context
import androidx.room.Room
import com.yourtech.systeme.data.local.YourTechDatabase
import com.yourtech.systeme.data.remote.RemoteConfig
import com.yourtech.systeme.data.remote.YourTechApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): YourTechDatabase =
        Room.databaseBuilder(context, YourTechDatabase::class.java, YourTechDatabase.NAME).build()

    @Provides
    @Singleton
    @ApplicationScope
    fun appScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun api(): YourTechApi = Retrofit.Builder()
        .baseUrl(RemoteConfig.baseUrl)
        .client(OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YourTechApi::class.java)
}
