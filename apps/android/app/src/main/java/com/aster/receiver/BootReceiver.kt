package com.aster.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aster.data.local.SettingsDataStore
import com.aster.service.AsterService
import com.aster.service.mode.ModeType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    companion object { private const val TAG = "BootReceiver" }

    @Inject lateinit var settingsDataStore: SettingsDataStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Must run before reading any auto-start state: a predecessor's
                // persisted remote URL/token must never be reused by this build.
                settingsDataStore.enforceOwnershipBoundary()

                val autoStart = settingsDataStore.autoStartOnBoot.first()
                if (!autoStart) return@launch

                val autoStartMode = settingsDataStore.autoStartMode.first()
                val modeType = if (autoStartMode != null) {
                    ModeType.fromString(autoStartMode)
                } else {
                    val lastMode = settingsDataStore.lastMode.first()
                    if (lastMode != null) {
                        ModeType.fromString(lastMode)
                    } else {
                        Log.d(TAG, "No mode configured for auto-start")
                        return@launch
                    }
                }

                when (modeType) {
                    ModeType.REMOTE_WS -> {
                        val serverUrl = settingsDataStore.serverUrl.first()
                        if (!serverUrl.isNullOrBlank()) AsterService.startService(context, modeType, serverUrl)
                    }
                    ModeType.IPC -> AsterService.startService(context, modeType, "")
                    ModeType.LOCAL_MCP -> {
                        val port = settingsDataStore.mcpPort.first()
                        AsterService.startService(context, modeType, port.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during boot handling", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
