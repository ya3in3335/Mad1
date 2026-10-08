package com.madak.spices.admin

import android.app.Application
import com.madak.spices.data.di.ApplicationScope
import com.madak.spices.data.seed.StoreInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class AdminApplication : Application() {
    @Inject lateinit var seeder: StoreInitializer
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        appScope.launch { seeder.seedIfEmpty() }
    }
}
