package com.aster.ui.screens.remote

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aster.data.model.ConnectionState
import com.aster.ui.components.AsterButton
import com.aster.ui.components.AsterButtonVariant
import com.aster.ui.components.AsterCard
import com.aster.ui.components.AsterSectionHeader
import com.aster.ui.components.AsterTextField
import com.aster.ui.components.AsterTopBar
import com.aster.ui.components.CodeBlock
import com.aster.ui.components.GlowOrb
import com.aster.ui.theme.AsterTheme
import com.aster.util.PermissionUtils
import com.aster.util.TailscaleUtils
import compose.icons.FeatherIcons
import compose.icons.feathericons.Globe
import compose.icons.feathericons.Info
import compose.icons.feathericons.Lock
import compose.icons.feathericons.Package
import compose.icons.feathericons.Shield
import compose.icons.feathericons.Unlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val BlueAccent = Color(0xFF3B82F6)

@Composable
fun RemoteConnectScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: RemoteViewModel = hiltViewModel()
) {
    val colors = AsterTheme.colors
    val context = LocalContext.current

    val serverUrl by viewModel.serverUrl.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val status by viewModel.status.collectAsState()
    val lastError by viewModel.lastError.collectAsState()

    var tailscaleStatus by remember { mutableStateOf<TailscaleUtils.TailscaleStatus?>(null) }

    // Load Tailscale status on background thread (DNS lookup can block)
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            tailscaleStatus = TailscaleUtils.getStatus(context)
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.APPROVED) {
            onNavigateToDashboard()
        }
    }

    val isConnecting = connectionState == ConnectionState.CONNECTING ||
            connectionState == ConnectionState.PENDING_APPROVAL ||
            connectionState == ConnectionState.RECONNECTING

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            AsterTopBar(
                title = "Удалённый сервер",
                onBack = onNavigateBack
            )
        },
        bottomBar = {
            // Sticky connect button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bg)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                AsterButton(
                    onClick = {
                        if (isConnecting) {
                            viewModel.disconnect()
                        } else {
                            if (!PermissionUtils.checkAllPermissions(context).allGranted) {
                                Toast.makeText(
                                    context,
                                    "Перед подключением предоставьте все необходимые разрешения",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onNavigateToPermissions()
                            } else {
                                viewModel.connect()
                            }
                        }
                    },
                    text = when {
                        isConnecting -> "Отмена"
                        else -> "Подключить"
                    },
                    variant = if (isConnecting) AsterButtonVariant.DANGER else AsterButtonVariant.PRIMARY,
                    enabled = serverUrl.isNotBlank() || isConnecting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // -- Server URL input (FIRST after toolbar) --
            ServerAddressCard(
                serverUrl = serverUrl,
                onServerUrlChange = { viewModel.updateServerUrl(it) }
            )

            // -- Tailscale auto-detect --
            val tsStatus = tailscaleStatus
            val tailscaleIp = tsStatus?.tailscaleIp
            if (tsStatus != null && tsStatus.isReady && tailscaleIp != null) {
                TailscaleDetectedCard(
                    tailscaleIp = tailscaleIp,
                    tailscaleDnsName = tsStatus.tailscaleDnsName,
                    onUseTailscale = { viewModel.updateServerUrl("ws://$tailscaleIp:5987") }
                )
            }

            // -- Connection status states --
            AnimatedVisibility(
                visible = connectionState == ConnectionState.ERROR,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                StatusBanner(
                    message = lastError
                        ?: "Не удалось подключиться. Проверьте адрес сервера и убедитесь, что aster-mcp запущен.",
                    bannerColor = colors.error
                )
            }

            AnimatedVisibility(
                visible = connectionState == ConnectionState.REJECTED,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                StatusBanner(
                    message = "Подключение отклонено. Разрешите это устройство в панели Aster (localhost:5989).",
                    bannerColor = colors.warning
                )
            }

            AnimatedVisibility(
                visible = connectionState == ConnectionState.PENDING_APPROVAL,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AsterCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GlowOrb(
                            color = colors.warning,
                            size = 56.dp,
                            isAnimating = true
                        )

                        Text(
                            text = "Ожидание подтверждения…",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.warning,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Откройте панель Aster по адресу localhost:5989 (или адресу вашего сервера) и разрешите это устройство.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSubtle,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = connectionState == ConnectionState.CONNECTING ||
                    connectionState == ConnectionState.RECONNECTING,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AsterCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlowOrb(
                            color = BlueAccent,
                            size = 40.dp,
                            isAnimating = true
                        )
                        Column {
                            Text(
                                text = if (connectionState == ConnectionState.RECONNECTING) {
                                    "Повторное подключение…"
                                } else {
                                    "Подключение…"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = BlueAccent
                            )
                            Text(
                                text = if (connectionState == ConnectionState.RECONNECTING) {
                                    "Восстановление WebSocket-соединения"
                                } else {
                                    "Установка WebSocket-соединения"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSubtle
                            )
                        }
                    }
                }
            }

            // -- Installation guide --
            InstallationSection()

            // -- About Remote Server --
            AboutRemoteSection()

            // -- Tailscale recommendation --
            TailscaleRecommendationSection()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =============================================================================
// SERVER ADDRESS CARD
// =============================================================================

@Composable
private fun ServerAddressCard(
    serverUrl: String,
    onServerUrlChange: (String) -> Unit
) {
    val colors = AsterTheme.colors

    AsterCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = FeatherIcons.Globe,
                    contentDescription = null,
                    tint = BlueAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Адрес сервера",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.text,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Введите WebSocket-адрес запущенного сервера aster-mcp. Порт по умолчанию — 5987.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSubtle
            )

            AsterTextField(
                value = serverUrl,
                onValueChange = onServerUrlChange,
                label = "Адрес сервера",
                placeholder = "ws://192.168.1.100:5987",
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProtocolChip(
                    icon = FeatherIcons.Lock,
                    label = "wss://",
                    description = "Зашифровано",
                    isSecure = true
                )
                ProtocolChip(
                    icon = FeatherIcons.Unlock,
                    label = "ws://",
                    description = "LAN / Tailscale",
                    isSecure = false
                )
            }
        }
    }
}

// =============================================================================
// INSTALLATION SECTION
// =============================================================================

@Composable
private fun InstallationSection() {
    val colors = AsterTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AsterSectionHeader(label = "Инструкция по настройке")

        AsterCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // -- Install via NPM --
                OptionHeader(
                    label = "Шаг 1",
                    title = "Установка через NPM",
                    tag = null,
                    icon = FeatherIcons.Package
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumberedStep(1, "Установите сервер aster-mcp глобально на компьютере:")
                }

                CodeBlock(
                    code = "npm install -g aster-mcp",
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumberedStep(2, "Запустите сервер:")
                }

                CodeBlock(
                    code = "aster start",
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumberedStep(3, "Сервер запускается на 3 портах:")
                }

                // Port info
                PortInfoRow(port = "5987", label = "WebSocket", description = "Подключение устройства")
                PortInfoRow(port = "5988", label = "MCP HTTP", description = "Точка подключения ИИ-клиента")
                PortInfoRow(
                    port = "5989",
                    label = "Панель управления",
                    description = "Веб-интерфейс управления"
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumberedStep(
                        4,
                        "Введите ws://<ваш-ip>:5987 в поле «Адрес сервера» выше и нажмите «Подключить»"
                    )
                    NumberedStep(5, "Разрешите устройство в панели Aster по адресу localhost:5989")
                }

                // MCP config for AI client
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.border)
                )

                Text(
                    text = "Настройка MCP-клиента",
                    style = MaterialTheme.typography.titleSmall,
                    color = BlueAccent,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Добавьте это в .mcp.json Claude Desktop / Cursor:",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSubtle
                )

                CodeBlock(
                    code = "{\n  \"mcpServers\": {\n    \"aster\": {\n      \"type\": \"http\",\n      \"url\": \"http://localhost:5988/mcp\"\n    }\n  }\n}",
                    modifier = Modifier.fillMaxWidth()
                )

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.border)
                )

                // -- OpenClaw / ClawHub integration --
                OpenClawSection()
            }
        }
    }
}

@Composable
private fun OpenClawSection() {
    val colors = AsterTheme.colors
    val openClawColor = Color(0xFF8B5CF6)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = FeatherIcons.Globe,
                contentDescription = null,
                tint = openClawColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = "OpenClaw / ClawHub",
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Навык",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(openClawColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        Text(
            text = "Aster доступен как навык ClawHub для OpenClaw, MoltBot и ClawBot. Установите его напрямую:",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSubtle
        )

        CodeBlock(
            code = "clawhub install aster",
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Или откройте clawhub.com/skills/aster",
            style = MaterialTheme.typography.labelSmall,
            color = openClawColor
        )

        Text(
            text = "После установки сервер aster-mcp автоматически запускается в OpenClaw. Подключите это устройство к серверу, и ИИ получит доступ ко всем 40+ инструментам.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSubtle
        )
    }
}

@Composable
private fun PortInfoRow(port: String, label: String, description: String) {
    val colors = AsterTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface2)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ":$port",
            style = MaterialTheme.typography.labelMedium,
            color = BlueAccent,
            fontWeight = FontWeight.Bold
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.text,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun OptionHeader(
    label: String,
    title: String,
    tag: String?,
    icon: ImageVector
) {
    val colors = AsterTheme.colors

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BlueAccent,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = "$label: $title",
            style = MaterialTheme.typography.titleSmall,
            color = colors.text,
            fontWeight = FontWeight.Medium
        )

        if (tag != null) {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(BlueAccent)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun NumberedStep(number: Int, text: String) {
    val colors = AsterTheme.colors

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(BlueAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 11.sp
                ),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSubtle,
            modifier = Modifier.weight(1f)
        )
    }
}

// =============================================================================
// ABOUT REMOTE SERVER
// =============================================================================

@Composable
private fun AboutRemoteSection() {
    val colors = AsterTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AsterSectionHeader(label = "О приложении")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BlueAccent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .background(BlueAccent.copy(alpha = 0.06f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = FeatherIcons.Info,
                contentDescription = null,
                tint = BlueAccent,
                modifier = Modifier.size(20.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Что такое удалённый сервер?",
                    style = MaterialTheme.typography.titleSmall,
                    color = BlueAccent,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Пакет aster-mcp для NPM запускает сервер-шлюз на вашем компьютере. " +
                            "Это устройство подключается к нему через WebSocket (порт 5987), а ваши ИИ-клиенты " +
                            "(Claude Desktop, Cursor и другие) подключаются через MCP HTTP (порт 5988). " +
                            "Сервер соединяет их и предоставляет ИИ 40+ инструментов для управления этим Android-устройством.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSubtle
                )

                Text(
                    text = "npmjs.com/package/aster-mcp",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueAccent
                )
            }
        }
    }
}

// =============================================================================
// TAILSCALE RECOMMENDATION
// =============================================================================

@Composable
private fun TailscaleRecommendationSection() {
    val colors = AsterTheme.colors
    val greenColor = Color(0xFF22C55E)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, greenColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(greenColor.copy(alpha = 0.04f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = FeatherIcons.Shield,
            contentDescription = null,
            tint = greenColor,
            modifier = Modifier.size(20.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Рекомендуется Tailscale",
                style = MaterialTheme.typography.titleSmall,
                color = greenColor,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Для удалённого доступа без проброса портов установите Tailscale на сервере и этом устройстве. " +
                        "Aster автоматически обнаруживает IP Tailscale. Используйте ws://<tailscale-ip>:5987 для защищённого зашифрованного подключения из любой точки.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSubtle
            )
        }
    }
}

// =============================================================================
// TAILSCALE DETECTED CARD
// =============================================================================

@Composable
private fun TailscaleDetectedCard(
    tailscaleIp: String,
    tailscaleDnsName: String? = null,
    onUseTailscale: () -> Unit
) {
    val colors = AsterTheme.colors

    AsterCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = FeatherIcons.Shield,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Tailscale обнаружен",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.success
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (tailscaleDnsName != null) {
                        Text(
                            text = tailscaleDnsName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = tailscaleIp,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSubtle
                        )
                    } else {
                        Text(
                            text = "Ваш IP Tailscale",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSubtle
                        )
                        Text(
                            text = tailscaleIp,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.text
                        )
                    }
                }

                AsterButton(
                    onClick = onUseTailscale,
                    text = "Использовать",
                    variant = AsterButtonVariant.SECONDARY
                )
            }
        }
    }
}

// =============================================================================
// STATUS BANNER
// =============================================================================

@Composable
private fun StatusBanner(
    message: String,
    bannerColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, bannerColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(bannerColor.copy(alpha = 0.08f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = FeatherIcons.Info,
            contentDescription = null,
            tint = bannerColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = bannerColor,
            modifier = Modifier.weight(1f)
        )
    }
}

// =============================================================================
// PROTOCOL CHIP
// =============================================================================

@Composable
private fun ProtocolChip(
    icon: ImageVector,
    label: String,
    description: String,
    isSecure: Boolean
) {
    val colors = AsterTheme.colors
    val chipColor = if (isSecure) colors.success else colors.textSubtle

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, chipColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .background(chipColor.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = chipColor,
            modifier = Modifier.size(14.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = chipColor
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}
