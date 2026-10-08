package com.madak.spices.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.madak.spices.data.BuildConfig
import com.madak.spices.data.local.MadakDatabase
import com.madak.spices.data.local.dao.CartDao
import com.madak.spices.data.local.dao.CatalogDao
import com.madak.spices.data.local.dao.FavoriteDao
import com.madak.spices.data.local.dao.NotificationDao
import com.madak.spices.data.local.dao.OfferDao
import com.madak.spices.data.local.dao.OrderDao
import com.madak.spices.data.local.dao.UserDao
import com.madak.spices.data.remote.MadakApi
import com.madak.spices.data.remote.RemoteConfig
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
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

private val Context.madakDataStore: DataStore<Preferences> by preferencesDataStore(name = "madak_settings")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MadakDatabase =
        Room.databaseBuilder(context, MadakDatabase::class.java, MadakDatabase.NAME)
            .addMigrations(MadakDatabase.MIGRATION_1_2)
            .build()

    @Provides fun catalogDao(db: MadakDatabase): CatalogDao = db.catalogDao()
    @Provides fun cartDao(db: MadakDatabase): CartDao = db.cartDao()
    @Provides fun orderDao(db: MadakDatabase): OrderDao = db.orderDao()
    @Provides fun userDao(db: MadakDatabase): UserDao = db.userDao()
    @Provides fun favoriteDao(db: MadakDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun notificationDao(db: MadakDatabase): NotificationDao = db.notificationDao()
    @Provides fun offerDao(db: MadakDatabase): OfferDao = db.offerDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.madakDataStore

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideApi(client: OkHttpClient): MadakApi = Retrofit.Builder()
        .baseUrl(RemoteConfig.baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MadakApi::class.java)
}
