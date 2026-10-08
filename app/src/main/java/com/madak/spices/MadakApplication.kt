package com.madak.spices

import android.app.Application
import com.madak.spices.data.di.ApplicationScope
import com.madak.spices.data.seed.StoreInitializer
import com.madak.spices.ui.OrderNotifier
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class MadakApplication : Application() {
    @Inject lateinit var seeder: StoreInitializer
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        OrderNotifier.createChannel(this)
        // Room is the single source of truth, so the app keeps working without network.
        appScope.launch { seeder.seedIfEmpty() }
    }
}
