package com.aster.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aster.R
import com.aster.service.mode.ModeType
import com.aster.ui.components.AnimatedEntrance
import com.aster.ui.components.AsterButton
import com.aster.ui.components.AsterButtonVariant
import com.aster.ui.components.BadgeItem
import com.aster.ui.components.BrandLockup
import com.aster.ui.components.GlowOrb
import com.aster.ui.components.ModeCard
import com.aster.ui.components.StarRepoCard
import com.aster.ui.theme.AsterTheme
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.Star

private val IpcColor = Color(0xFFF59E0B)
private val McpColor = Color(0xFF8B5CF6)
private val RemoteColor = Color(0xFF3B82F6)
private val NpmBadgeColor = Color(0xFFCB3837)
private val OpenClawBadgeColor = Color(0xFF8B5CF6)

@Composable
fun HomeScreen(
    onNavigateToIpc: () -> Unit,
    onNavigateToMcp: () -> Unit,
    onNavigateToRemote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToIpcDashboard: () -> Unit,
    onNavigateToMcpDashboard: () -> Unit,
    onNavigateToRemoteDashboard: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToChat: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val colors = AsterTheme.colors
    val lastUsedMode by viewModel.lastUsedMode.collectAsState()
    val isServiceRunning = viewModel.isServiceRunning
    val activeModes = viewModel.activeModeTypes
    val starPromptDismissed by viewModel.starPromptDismissed.collectAsState()
    val uriHandler = LocalUriHandler.current

    Column(Modifier.fillMaxSize().background(colors.bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BrandLockup(Modifier.weight(1f), tagline = stringResource(R.string.android_device_controller))
            IconButton(onClick = onNavigateToLogs) { Icon(FeatherIcons.Activity, stringResource(R.string.logs), tint = colors.textSubtle) }
            IconButton(onClick = onNavigateToPermissions) { Icon(Icons.Default.Security, stringResource(R.string.permissions_title), tint = colors.textSubtle) }
            IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, stringResource(R.string.settings), tint = colors.textSubtle) }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            AsterButton(
                onClick = onNavigateToChat,
                text = "Чат со Светланой",
                variant = AsterButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AsterButton(
                onClick = onNavigateToContacts,
                text = "Контакты",
                variant = AsterButtonVariant.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )

            if (isServiceRunning && activeModes.isNotEmpty()) {
                AnimatedEntrance(delayMillis = 0, durationMillis = 300) {
                    Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeModes.forEach { modeType ->
                            val activeColor = when (modeType) {
                                ModeType.IPC -> IpcColor
                                ModeType.LOCAL_MCP -> McpColor
                                ModeType.REMOTE_WS -> RemoteColor
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                GlowOrb(activeColor, 28.dp, true)
                                Spacer(Modifier.width(10.dp))
                                Text(modeType.toDisplayName(), MaterialTheme.typography.titleSmall, activeColor, FontWeight.SemiBold, Modifier.weight(1f))
                                AsterButton(onClick = {
                                    when (modeType) {
                                        ModeType.IPC -> onNavigateToIpcDashboard()
                                        ModeType.LOCAL_MCP -> onNavigateToMcpDashboard()
                                        ModeType.REMOTE_WS -> onNavigateToRemoteDashboard()
                                    }
                                }, text = stringResource(R.string.dashboard), variant = AsterButtonVariant.SECONDARY)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.choose_how_to_connect), MaterialTheme.typography.titleMedium, colors.text, FontWeight.SemiBold, Modifier.padding(bottom = 4.dp))
            Text(stringResource(R.string.choose_how_to_connect_description), MaterialTheme.typography.bodySmall, colors.textMuted, Modifier.padding(bottom = 16.dp))

            AnimatedEntrance(delayMillis = 100) {
                ModeCard(
                    title = stringResource(R.string.remote_server), tagline = stringResource(R.string.remote_server_tagline), description = stringResource(R.string.remote_server_description),
                    icon = Icons.Default.Cloud, accentColor = RemoteColor,
                    features = listOf(stringResource(R.string.feature_npm_install), stringResource(R.string.feature_40_tools), stringResource(R.string.feature_webhooks), stringResource(R.string.feature_tailscale_ready)),
                    badges = listOf(BadgeItem("NPM", NpmBadgeColor), BadgeItem("OpenClaw", OpenClawBadgeColor)), complexity = stringResource(R.string.recommended),
                    isSelected = lastUsedMode == ModeType.REMOTE_WS.name, isActive = activeModes.contains(ModeType.REMOTE_WS), onClick = onNavigateToRemote, modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(12.dp))
            AnimatedEntrance(delayMillis = 250) {
                ModeCard(
                    title = stringResource(R.string.ipc_mode), tagline = stringResource(R.string.ipc_mode_tagline), description = stringResource(R.string.ipc_mode_home_description),
                    icon = Icons.Default.PhoneAndroid, accentColor = IpcColor,
                    features = listOf(stringResource(R.string.feature_zero_latency), stringResource(R.string.feature_no_network), stringResource(R.string.feature_token_auth), stringResource(R.string.feature_40_tools)), complexity = stringResource(R.string.beginner),
                    isSelected = lastUsedMode == ModeType.IPC.name, isActive = activeModes.contains(ModeType.IPC), onClick = onNavigateToIpc, modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(12.dp))
            AnimatedEntrance(delayMillis = 400) {
                ModeCard(
                    title = stringResource(R.string.local_mcp_server), tagline = stringResource(R.string.local_mcp_server_tagline), description = stringResource(R.string.local_mcp_server_home_description),
                    icon = Icons.Default.Dns, accentColor = McpColor,
                    features = listOf(stringResource(R.string.feature_claude_desktop), stringResource(R.string.feature_http_transport), stringResource(R.string.feature_lan_access), stringResource(R.string.feature_tailscale)), complexity = stringResource(R.string.intermediate),
                    isSelected = lastUsedMode == ModeType.LOCAL_MCP.name, isActive = activeModes.contains(ModeType.LOCAL_MCP), onClick = onNavigateToMcp, modifier = Modifier.fillMaxWidth()
                )
            }
            if (!starPromptDismissed) {
                Spacer(Modifier.height(24.dp))
                AnimatedEntrance(delayMillis = 550) { StarRepoCard(onStar = { uriHandler.openUri(stringResource(R.string.star_repo_url)); viewModel.dismissStarPrompt() }, onDismiss = viewModel::dismissStarPrompt) }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun ModeType.toDisplayName(): String = when (this) { ModeType.IPC -> "IPC"; ModeType.LOCAL_MCP -> "MCP"; ModeType.REMOTE_WS -> "WebSocket" }
