package com.aster.ui.screens.mcp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aster.service.mode.ModeState
import com.aster.ui.components.AsterButton
import com.aster.ui.components.AsterButtonVariant
import com.aster.ui.components.AsterCard
import com.aster.ui.components.AsterSectionHeader
import com.aster.ui.components.AsterTopBar
import com.aster.ui.components.CodeBlock
import com.aster.ui.components.GlowOrb
import com.aster.ui.components.ToolsSection
import com.aster.ui.theme.AsterTheme
import com.aster.util.PermissionUtils

private val Violet = Color(0xFF8B5CF6)

@Composable
fun McpDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToLogs: () -> Unit,
    viewModel: McpDashboardViewModel = hiltViewModel()
) {
    val colors = AsterTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val status by viewModel.status.collectAsState()
    val port by viewModel.port.collectAsState()
    val tools = viewModel.tools
    val isRunning = status.state == ModeState.RUNNING
    val isTransitioning = status.state == ModeState.STARTING || status.state == ModeState.STOPPING

    val statusText = when (status.state) {
        ModeState.IDLE -> "Не запущено"
        ModeState.STARTING -> "Запускается…"
        ModeState.RUNNING -> "Работает на порту $port"
        ModeState.ERROR -> "Ошибка: ${status.message}"
        ModeState.STOPPING -> "Останавливается…"
    }

    val orbColor = when (status.state) {
        ModeState.RUNNING -> Violet
        ModeState.ERROR -> colors.error
        ModeState.STARTING, ModeState.STOPPING -> colors.warning
        else -> colors.textMuted
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = { AsterTopBar(title = "Локальный MCP", onBack = onNavigateBack) },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(colors.bg).padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding()) {
                AsterButton(
                    onClick = {
                        if (isRunning) {
                            viewModel.stopMcp()
                        } else if (!PermissionUtils.checkAllPermissions(context).allGranted) {
                            Toast.makeText(context, "Перед запуском предоставьте все необходимые разрешения", Toast.LENGTH_SHORT).show()
                            onNavigateToPermissions()
                        } else {
                            viewModel.startMcp()
                        }
                    },
                    text = if (isRunning) "Остановить MCP" else "Запустить MCP",
                    variant = if (isRunning) AsterButtonVariant.DANGER else AsterButtonVariant.PRIMARY,
                    enabled = !isTransitioning,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ServerEndpointsSection(
                localhostUrl = viewModel.localhostUrl,
                lanUrl = viewModel.lanUrl,
                tailscaleUrl = viewModel.tailscaleUrl,
                tailscaleDnsUrl = viewModel.tailscaleDnsUrl,
                onCopy = { url -> clipboardManager.setText(AnnotatedString(url)) }
            )
            StatusSection(statusText, orbColor, status.connectedClients, isRunning || isTransitioning)
            McpConfigSection(port)
            SetupGuideSection(port)
            TailscaleTipCallout()
            if (tools.isNotEmpty()) ToolsSection(tools = tools, accentColor = Violet)
            AsterButton(onClick = onNavigateToLogs, text = "Открыть журнал вызовов инструментов", variant = AsterButtonVariant.SECONDARY, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ServerEndpointsSection(localhostUrl: String, lanUrl: String?, tailscaleUrl: String?, tailscaleDnsUrl: String? = null, onCopy: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AsterSectionHeader(label = "Адреса сервера")
        CodeBlock(code = localhostUrl, label = "Это устройство", onCopy = { onCopy(localhostUrl) }, modifier = Modifier.fillMaxWidth())
        if (lanUrl != null) CodeBlock(code = lanUrl, label = "Локальная сеть", onCopy = { onCopy(lanUrl) }, modifier = Modifier.fillMaxWidth())
        if (tailscaleDnsUrl != null) CodeBlock(code = tailscaleDnsUrl, label = "Tailscale (DNS)", onCopy = { onCopy(tailscaleDnsUrl) }, modifier = Modifier.fillMaxWidth())
        if (tailscaleUrl != null) CodeBlock(code = tailscaleUrl, label = if (tailscaleDnsUrl != null) "Tailscale (IP)" else "Tailscale", onCopy = { onCopy(tailscaleUrl) }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun StatusSection(statusText: String, orbColor: Color, connectedClients: Int, isAnimating: Boolean) {
    val colors = AsterTheme.colors
    AsterCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GlowOrb(color = orbColor, size = 48.dp, isAnimating = isAnimating)
            Column {
                Text(statusText, style = MaterialTheme.typography.titleMedium, color = colors.text)
                if (connectedClients > 0) Text("Подключено клиентов: $connectedClients", style = MaterialTheme.typography.bodySmall, color = colors.textSubtle)
            }
        }
    }
}

@Composable
private fun McpConfigSection(port: Int) {
    val colors = AsterTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AsterSectionHeader(label = "Настройка MCP-клиента")
        AsterCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Добавьте в .mcp.json (Claude Desktop / Cursor):", style = MaterialTheme.typography.bodySmall, color = colors.textSubtle)
                CodeBlock(
                    code = "{\n  \"mcpServers\": {\n    \"aster-local\": {\n      \"type\": \"http\",\n      \"url\": \"http://<ip>:$port/mcp\"\n    }\n  }\n}",
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Замените <ip> на IP-адрес локальной сети или Tailscale. Используется транспорт Streamable HTTP.", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
            }
        }
    }
}

@Composable
private fun SetupGuideSection(port: Int) {
    val colors = AsterTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AsterSectionHeader(label = "Порядок настройки")
        AsterCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Claude Desktop / Cursor", style = MaterialTheme.typography.titleSmall, color = Violet, fontWeight = FontWeight.SemiBold)
                SetupStep(1, "Запустите MCP-сервер кнопкой ниже")
                SetupStep(2, "Скопируйте адрес сервера выше")
                SetupStep(3, "Откройте настройки Claude Desktop или конфигурацию MCP в Cursor")
                SetupStep(4, "Добавьте новый MCP-сервер с типом «http» и вставьте адрес")
                SetupStep(5, "Перезапустите клиент — инструменты Android-устройства появятся в списке")
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                ConnectionMethodHint("Это устройство", "Используйте локальный адрес: http://127.0.0.1:$port/mcp")
                ConnectionMethodHint("Локальная сеть", "Используйте IP-адрес LAN. Оба устройства должны быть подключены к одной Wi-Fi сети.")
                ConnectionMethodHint("Через Tailscale (рекомендуется для удаленной работы)", "Установите Tailscale на оба устройства. Используйте адрес Tailscale для защищенного доступа из любой точки — перенаправление портов не требуется.")
            }
        }
    }
}

@Composable
private fun SetupStep(number: Int, text: String) {
    val colors = AsterTheme.colors
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(Violet), contentAlignment = Alignment.Center) {
            Text(number.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = colors.text, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun ConnectionMethodHint(label: String, description: String) {
    val colors = AsterTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Violet, fontWeight = FontWeight.SemiBold)
        Text(description, style = MaterialTheme.typography.bodySmall, color = colors.textSubtle)
    }
}

@Composable
private fun TailscaleTipCallout() {
    val colors = AsterTheme.colors
    val tipBackground = if (colors.isDark) Violet.copy(alpha = 0.08f) else Violet.copy(alpha = 0.06f)
    val shape = RoundedCornerShape(12.dp)
    Box(Modifier.fillMaxWidth().clip(shape).border(1.dp, Violet.copy(alpha = 0.25f), shape).background(tipBackground).padding(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.Info, null, tint = Violet, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Совет по Tailscale", style = MaterialTheme.typography.titleSmall, color = Violet, fontWeight = FontWeight.SemiBold)
                Text("Для удобной удаленной работы установите Tailscale на этом устройстве и компьютере. Он создает защищенную частную сеть — перенаправление портов и настройка брандмауэра не нужны.", style = MaterialTheme.typography.bodySmall, color = colors.textSubtle)
            }
        }
    }
}
