package com.aster.ui.screens.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aster.ui.components.AsterButton
import com.aster.ui.components.AsterButtonVariant
import com.aster.ui.components.AsterCard
import com.aster.ui.components.AsterSectionHeader
import com.aster.ui.components.AsterTopBar
import com.aster.ui.theme.AsterTheme
import com.aster.util.PermissionType
import com.aster.util.PermissionUtils
import compose.icons.FeatherIcons
import compose.icons.feathericons.Battery
import compose.icons.feathericons.Bell
import compose.icons.feathericons.Camera
import compose.icons.feathericons.Check
import compose.icons.feathericons.Crosshair
import compose.icons.feathericons.Eye
import compose.icons.feathericons.Folder
import compose.icons.feathericons.Info
import compose.icons.feathericons.Layers
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Phone
import compose.icons.feathericons.Users

@Composable
fun PermissionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AsterTheme.colors
    val context = LocalContext.current
    var permissionResult by remember { mutableStateOf(PermissionUtils.checkAllPermissions(context)) }

    val guidedFlowRef = remember { mutableStateOf<GuidedPermissionFlow?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionResult = PermissionUtils.checkAllPermissions(context)
        guidedFlowRef.value?.onRuntimeResult(context)
    }

    val guidedFlow = remember {
        GuidedPermissionFlow(
            launchRuntime = { permissionLauncher.launch(it) },
            launchSettings = { context.startActivity(it) }
        ).also { guidedFlowRef.value = it }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionResult = PermissionUtils.checkAllPermissions(context)
                guidedFlow.onResume(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        permissionResult = PermissionUtils.checkAllPermissions(context)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            AsterTopBar(
                title = "Разрешения",
                onBack = onNavigateBack,
                actions = {
                    val badgeColor = if (permissionResult.allGranted) colors.success else colors.warning
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (permissionResult.allGranted) "Все разрешено" else "${permissionResult.grantedCount}/${permissionResult.totalCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (!permissionResult.allGranted) {
                Column(modifier = Modifier.fillMaxWidth().background(colors.bg)) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))
                    Column(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsterButton(
                            onClick = { guidedFlow.start(context) },
                            text = if (guidedFlow.isRunning) "Продолжить в настройках…" else "Запросить все разрешения",
                            variant = AsterButtonVariant.PRIMARY,
                            enabled = !guidedFlow.isRunning,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = guidedFlow.currentStepLabel?.let { label ->
                                val step = (guidedFlow.stepsDone + 1).coerceAtMost(guidedFlow.stepsTotal)
                                "Шаг $step из ${guidedFlow.stepsTotal} — $label"
                            } ?: "Можно предоставить все необходимые разрешения в едином пошаговом режиме.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSubtle
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AsterCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${permissionResult.grantedCount} из ${permissionResult.totalCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (permissionResult.allGranted) colors.success else colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (permissionResult.allGranted) "Все разрешения предоставлены" else "Разрешения предоставлены частично",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSubtle
                        )
                    }
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(
                            if (permissionResult.allGranted) colors.success.copy(alpha = 0.12f) else colors.warning.copy(alpha = 0.12f)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (permissionResult.allGranted) {
                            Icon(FeatherIcons.Check, "Готово", tint = colors.success, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "${permissionResult.totalCount - permissionResult.grantedCount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.warning,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            AsterSectionHeader(
                label = "Основные разрешения",
                count = listOf(PermissionType.NOTIFICATIONS, PermissionType.LOCATION, PermissionType.PHONE_SMS, PermissionType.CONTACTS, PermissionType.CAMERA)
                    .count { permissionResult.permissions[it] == true }
            )

            AsterCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    PermissionItem(FeatherIcons.Bell, "Уведомления", "Показывать и управлять уведомлениями", permissionResult.permissions[PermissionType.NOTIFICATIONS] == true, colors.warning) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                    }
                    PermissionDivider()
                    PermissionItem(FeatherIcons.MapPin, "Местоположение", "Получать местоположение по GPS и сети", permissionResult.permissions[PermissionType.LOCATION] == true, colors.info) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                    PermissionDivider()
                    PermissionItem(FeatherIcons.Phone, "Телефон и SMS", "Читать и отправлять SMS, совершать звонки", permissionResult.permissions[PermissionType.PHONE_SMS] == true, colors.success) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG))
                    }
                    PermissionDivider()
                    PermissionItem(FeatherIcons.Users, "Контакты", "Искать и читать контакты устройства", permissionResult.permissions[PermissionType.CONTACTS] == true, colors.info) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
                    }
                    PermissionDivider()
                    PermissionItem(FeatherIcons.Camera, "Камера", "Снимать фото и видео", permissionResult.permissions[PermissionType.CAMERA] == true, colors.accent) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    }
                }
            }

            AsterSectionHeader(
                label = "Специальный доступ",
                count = listOf(PermissionType.STORAGE, PermissionType.ACCESSIBILITY, PermissionType.NOTIFICATION_LISTENER, PermissionType.OVERLAY, PermissionType.BATTERY)
                    .count { permissionResult.permissions[it] == true }
            )

            AsterCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    PermissionItem(FeatherIcons.Folder, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "Доступ ко всем файлам" else "Хранилище", "Полный доступ к чтению и записи файлов", permissionResult.permissions[PermissionType.STORAGE] == true, colors.error) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}")))
                        } else {
                            permissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))
                        }
                    }
                    PermissionDivider()
                    PermissionItem(FeatherIcons.Crosshair, "Служба специальных возможностей", "Управление экраном, жестами и интерфейсом", permissionResult.permissions[PermissionType.ACCESSIBILITY] == true, colors.info) {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                    PermissionDivider()
                    PermissionItem(FeatherIcons.Eye, "Доступ к уведомлениям", "Читать входящие уведомления и реагировать на них", permissionResult.permissions[PermissionType.NOTIFICATION_LISTENER] == true, colors.primary) {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                    PermissionDivider()
                    PermissionItem(FeatherIcons.Layers, "Отображение поверх других приложений", "Показывать окна Светланы поверх других приложений", permissionResult.permissions[PermissionType.OVERLAY] == true, colors.accent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                    }
                    PermissionDivider()
                    PermissionItem(FeatherIcons.Battery, "Оптимизация батареи", "Исключить Светлану из оптимизации для стабильной фоновой работы", permissionResult.permissions[PermissionType.BATTERY] == true, colors.success) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}")))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).background(colors.primary.copy(alpha = 0.05f)).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(FeatherIcons.Info, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                Text(
                    text = "Нажмите на нужное разрешение, чтобы предоставить доступ. Некоторые специальные разрешения открывают системные настройки Android и требуют ручного включения.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSubtle,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    name: String,
    description: String,
    isGranted: Boolean,
    accentColor: Color,
    onGrant: () -> Unit
) {
    val colors = AsterTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = colors.text, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        AsterButton(
            onClick = onGrant,
            text = if (isGranted) "Разрешено" else "Разрешить",
            variant = if (isGranted) AsterButtonVariant.SECONDARY else AsterButtonVariant.PRIMARY
        )
    }
}

@Composable
private fun PermissionDivider() {
    val colors = AsterTheme.colors
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))
}
