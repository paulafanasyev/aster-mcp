package com.aster.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

enum class PermissionType {
    LOCATION,
    NOTIFICATIONS,
    PHONE_SMS,
    ACCESSIBILITY,
    NOTIFICATION_LISTENER,
    OVERLAY,
    STORAGE,
    BATTERY,
    CAMERA,
    CONTACTS
}

data class PermissionCheckResult(
    val allGranted: Boolean,
    val permissions: Map<PermissionType, Boolean>,
    val missingPermissions: List<PermissionType>
) {
    val grantedCount: Int get() = permissions.count { it.value }
    val totalCount: Int get() = permissions.size
}

object PermissionUtils {
    fun checkAllPermissions(context: Context): PermissionCheckResult {
        val permissions = mapOf(
            PermissionType.LOCATION to checkLocationPermission(context),
            PermissionType.NOTIFICATIONS to checkNotificationPermission(context),
            PermissionType.PHONE_SMS to checkPhoneSmsPermission(context),
            PermissionType.ACCESSIBILITY to checkAccessibilityPermission(context),
            PermissionType.NOTIFICATION_LISTENER to checkNotificationListenerPermission(context),
            PermissionType.OVERLAY to checkOverlayPermission(context),
            PermissionType.STORAGE to checkStoragePermission(context),
            PermissionType.BATTERY to checkBatteryOptimization(context),
            PermissionType.CAMERA to checkCameraPermission(context),
            PermissionType.CONTACTS to checkContactsPermission(context)
        )
        val missing = permissions.filter { !it.value }.keys.toList()
        return PermissionCheckResult(missing.isEmpty(), permissions, missing)
    }

    fun checkCriticalPermissions(context: Context): PermissionCheckResult {
        val permissions = mapOf(
            PermissionType.ACCESSIBILITY to checkAccessibilityPermission(context),
            PermissionType.NOTIFICATION_LISTENER to checkNotificationListenerPermission(context),
            PermissionType.BATTERY to checkBatteryOptimization(context)
        )
        val missing = permissions.filter { !it.value }.keys.toList()
        return PermissionCheckResult(missing.isEmpty(), permissions, missing)
    }

    fun getPermissionName(type: PermissionType): String = when (type) {
        PermissionType.LOCATION -> "Доступ к местоположению"
        PermissionType.NOTIFICATIONS -> "Уведомления"
        PermissionType.PHONE_SMS -> "Телефон и SMS"
        PermissionType.ACCESSIBILITY -> "Служба специальных возможностей"
        PermissionType.NOTIFICATION_LISTENER -> "Доступ к уведомлениям"
        PermissionType.OVERLAY -> "Отображение поверх других приложений"
        PermissionType.STORAGE -> "Доступ к файлам"
        PermissionType.BATTERY -> "Оптимизация батареи"
        PermissionType.CAMERA -> "Камера"
        PermissionType.CONTACTS -> "Контакты"
    }

    fun isGranted(context: Context, type: PermissionType): Boolean = when (type) {
        PermissionType.LOCATION -> checkLocationPermission(context)
        PermissionType.NOTIFICATIONS -> checkNotificationPermission(context)
        PermissionType.PHONE_SMS -> checkPhoneSmsPermission(context)
        PermissionType.ACCESSIBILITY -> checkAccessibilityPermission(context)
        PermissionType.NOTIFICATION_LISTENER -> checkNotificationListenerPermission(context)
        PermissionType.OVERLAY -> checkOverlayPermission(context)
        PermissionType.STORAGE -> checkStoragePermission(context)
        PermissionType.BATTERY -> checkBatteryOptimization(context)
        PermissionType.CAMERA -> checkCameraPermission(context)
        PermissionType.CONTACTS -> checkContactsPermission(context)
    }

    fun runtimePermissionsFor(context: Context, type: PermissionType): List<String> {
        if (isGranted(context, type)) return emptyList()
        return when (type) {
            PermissionType.NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.POST_NOTIFICATIONS)
            } else emptyList()
            PermissionType.LOCATION -> listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            PermissionType.PHONE_SMS -> listOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG)
            PermissionType.CONTACTS -> listOf(Manifest.permission.READ_CONTACTS)
            PermissionType.CAMERA -> listOf(Manifest.permission.CAMERA)
            PermissionType.STORAGE -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            } else emptyList()
            else -> emptyList()
        }
    }

    fun missingRuntimePermissions(context: Context): List<String> = listOf(
        PermissionType.NOTIFICATIONS,
        PermissionType.LOCATION,
        PermissionType.PHONE_SMS,
        PermissionType.CONTACTS,
        PermissionType.CAMERA,
        PermissionType.STORAGE
    ).flatMap { runtimePermissionsFor(context, it) }.distinct()

    private val SPECIAL_ACCESS_ORDER = listOf(
        PermissionType.STORAGE,
        PermissionType.ACCESSIBILITY,
        PermissionType.NOTIFICATION_LISTENER,
        PermissionType.OVERLAY,
        PermissionType.BATTERY
    )

    fun missingSpecialAccess(context: Context): List<PermissionType> = SPECIAL_ACCESS_ORDER.filter { type ->
        specialAccessIntent(context, type) != null && !isGranted(context, type)
    }

    fun specialAccessIntent(context: Context, type: PermissionType): Intent? {
        val packageUri = Uri.parse("package:${context.packageName}")
        return when (type) {
            PermissionType.STORAGE -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, packageUri)
            } else null
            PermissionType.ACCESSIBILITY -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            PermissionType.NOTIFICATION_LISTENER -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            PermissionType.OVERLAY -> Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri)
            PermissionType.BATTERY -> Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri)
            else -> null
        }
    }

    fun checkLocationPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun checkNotificationPermission(context: Context): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    fun checkPhoneSmsPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

    fun checkAccessibilityPermission(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabledServices.contains(context.packageName)
    }

    fun checkNotificationListenerPermission(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        return enabledListeners.contains(context.packageName)
    }

    fun checkOverlayPermission(context: Context): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true

    fun checkStoragePermission(context: Context): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun checkBatteryOptimization(context: Context): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    } else true

    fun checkCameraPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun checkContactsPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
}
