package com.aster

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.aster.service.overlay.CompanionFaceOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class AsterApplication : Application() {

    private val companionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerCompanionFaceLifecycle()
    }

    private fun registerCompanionFaceLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                if (activity.javaClass.name != "com.aster.ui.MainActivity") return

                val overlay = EntryPointAccessors.fromApplication(
                    this@AsterApplication,
                    CompanionFaceEntryPoint::class.java,
                ).companionFaceOverlay

                companionScope.launch {
                    if (Settings.canDrawOverlays(this@AsterApplication)) {
                        val attached = overlay.show()
                        if (BuildConfig.DEBUG) {
                            Log.d("SvetlanaFace", "startup overlay attached=$attached")
                        }
                    } else if (BuildConfig.DEBUG) {
                        Log.d("SvetlanaFace", "startup overlay waiting for SYSTEM_ALERT_WINDOW")
                    }
                }

                // Aster is the secure host for the original companion face; OpenAlly
                // remains the face/voice source of truth. Bring its Android client to
                // the foreground when installed so the existing IPC face stream starts
                // instead of leaving our restored host window empty.
                runCatching {
                    packageManager.getLaunchIntentForPackage(OPENALLY_PACKAGE)?.let { intent ->
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }.onFailure {
                    if (BuildConfig.DEBUG) {
                        Log.d("SvetlanaFace", "OpenAlly is not installed; face source unavailable", it)
                    }
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val OPENALLY_PACKAGE = "openally.ai"
        const val NOTIFICATION_CHANNEL_ID = "aster_service"
        const val NOTIFICATION_ID = 1
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CompanionFaceEntryPoint {
    val companionFaceOverlay: CompanionFaceOverlay
}
