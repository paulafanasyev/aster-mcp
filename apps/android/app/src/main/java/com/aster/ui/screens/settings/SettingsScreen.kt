package com.aster.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aster.BuildConfig
import com.aster.R
import com.aster.ui.components.AsterCard
import com.aster.ui.components.AsterSectionHeader
import com.aster.ui.components.AsterTopBar
import com.aster.ui.theme.AsterTheme
import compose.icons.FeatherIcons
import compose.icons.feathericons.BookOpen
import compose.icons.feathericons.ExternalLink
import compose.icons.feathericons.Github
import compose.icons.feathericons.Monitor
import compose.icons.feathericons.Moon
import compose.icons.feathericons.Server
import compose.icons.feathericons.Smartphone
import compose.icons.feathericons.Sun
import compose.icons.feathericons.Wifi

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val colors = AsterTheme.colors
    val themeMode by viewModel.themeMode.collectAsState()
    val autoStartOnBoot by viewModel.autoStartOnBoot.collectAsState()
    val autoStartMode by viewModel.autoStartMode.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = { AsterTopBar(title = "Настройки", onBack = onNavigateBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AsterSectionHeader(label = "Внешний вид")
            AsterCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Тема", style = MaterialTheme.typography.titleSmall, color = colors.text)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ThemeChip(FeatherIcons.Moon, "Тёмная", themeMode == "dark", { viewModel.setThemeMode("dark") }, Modifier.weight(1f))
                        ThemeChip(FeatherIcons.Sun, "Светлая", themeMode == "light", { viewModel.setThemeMode("light") }, Modifier.weight(1f))
                        ThemeChip(FeatherIcons.Monitor, "Системная", themeMode == "system", { viewModel.setThemeMode("system") }, Modifier.weight(1f))
                    }
                }
            }

            AsterSectionHeader(label = "Автозапуск")
            AsterCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Запуск при включении", style = MaterialTheme.typography.titleSmall, color = colors.text)
                            Text("Автоматически запускать службу Светланы после включения устройства", style = MaterialTheme.typography.bodySmall, color = colors.textSubtle)
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(checked = autoStartOnBoot, onCheckedChange = { viewModel.setAutoStartOnBoot(it) })
                    }

                    if (autoStartOnBoot) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                        Text("Режим автозапуска", style = MaterialTheme.typography.labelLarge, color = colors.textSubtle)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModeOption(FeatherIcons.Smartphone, "Режим IPC", "Локальное взаимодействие между процессами", autoStartMode == "IPC", { viewModel.setAutoStartMode("IPC") })
                            ModeOption(FeatherIcons.Server, "Локальный MCP", "Локальный MCP-сервер на устройстве", autoStartMode == "LOCAL_MCP", { viewModel.setAutoStartMode("LOCAL_MCP") })
                            ModeOption(FeatherIcons.Wifi, "Удалённый WebSocket", "Подключение к выбранному удалённому WebSocket-серверу", autoStartMode == "REMOTE_WS", { viewModel.setAutoStartMode("REMOTE_WS") })
                        }
                    }
                }
            }

            AsterSectionHeader(label = "О приложении")
            AsterCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(colors.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.bolt), null, tint = colors.primary, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Светлана", style = MaterialTheme.typography.titleMedium, color = colors.text, fontWeight = FontWeight.Bold)
                            Text("Помощник для управления Android", style = MaterialTheme.typography.bodySmall, color = colors.textSubtle)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    AboutInfoRow("Версия", BuildConfig.VERSION_NAME)
                    AboutInfoRow("Сборка", if (BuildConfig.DEBUG) "отладочная" else "релизная")
                    AboutInfoRow("Платформа", "Android ${android.os.Build.VERSION.RELEASE}")
                    Text(stringResource(R.string.brand_trademark_notice), style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                }
            }

            val uriHandler = LocalUriHandler.current
            AsterCard(modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri("https://github.com/paulafanasyev/aster-mcp") }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(FeatherIcons.Github, null, tint = colors.textSubtle, modifier = Modifier.size(20.dp))
                        Text("Репозиторий GitHub", style = MaterialTheme.typography.bodyMedium, color = colors.text)
                    }
                    Icon(FeatherIcons.ExternalLink, null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                }
            }

            AsterCard(modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri("https://github.com/paulafanasyev/aster-mcp/blob/main/LICENSE") }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(FeatherIcons.BookOpen, null, tint = colors.textSubtle, modifier = Modifier.size(20.dp))
                        Text("Лицензии открытого исходного кода", style = MaterialTheme.typography.bodyMedium, color = colors.text)
                    }
                    Icon(FeatherIcons.ExternalLink, null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ThemeChip(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AsterTheme.colors
    val borderColor = if (isSelected) colors.primary else colors.border
    val bgColor = if (isSelected) colors.primary.copy(alpha = 0.10f) else colors.surface2
    val contentColor = if (isSelected) colors.primary else colors.textSubtle
    Column(modifier.clip(RoundedCornerShape(12.dp)).border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)).background(bgColor).clickable { onClick() }.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, label, tint = contentColor, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun ModeOption(icon: ImageVector, label: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = AsterTheme.colors
    val borderColor = if (isSelected) colors.primary else colors.border
    val bgColor = if (isSelected) colors.primary.copy(alpha = 0.06f) else colors.surface2
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).border(1.dp, borderColor, RoundedCornerShape(10.dp)).background(bgColor).clickable { onClick() }.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (isSelected) colors.primary else colors.textSubtle, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) colors.text else colors.textSubtle, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
            Text(description, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        if (isSelected) Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(colors.primary))
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    val colors = AsterTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textSubtle)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.text)
    }
}
