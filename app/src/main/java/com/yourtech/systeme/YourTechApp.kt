package com.yourtech.systeme

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.yourtech.systeme.data.di.ApplicationScope
import com.yourtech.systeme.data.repository.StoreInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class YourTechApp : Application() {
    @Inject lateinit var initializer: StoreInitializer
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                NotificationChannel(CHANNEL_REQUESTS, getString(R.string.notif_channel), NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        scope.launch { initializer.ensureInitialized() }
    }

    companion object { const val CHANNEL_REQUESTS = "requests" }
}
