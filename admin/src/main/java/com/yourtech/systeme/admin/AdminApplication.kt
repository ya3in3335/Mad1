package com.yourtech.systeme.admin

import android.app.Application
import com.yourtech.systeme.data.di.ApplicationScope
import com.yourtech.systeme.data.repository.StoreInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class AdminApplication : Application() {
    @Inject lateinit var initializer: StoreInitializer
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        scope.launch { initializer.ensureInitialized() }
    }
}
