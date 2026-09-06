package com.aster.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ServerConfig(
    val host: String = "",
    val port: Int = 5987,
    val autoConnect: Boolean = false
)

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val SERVER_HOST = stringPreferencesKey("server_host")
        val SERVER_PORT = intPreferencesKey("server_port")
        val SERVER_URL = stringPreferencesKey("server_url")
        val AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        val AUTO_START_ON_BOOT = booleanPreferencesKey("auto_start_on_boot")
        val DEVICE_ID = stringPreferencesKey("device_id")

        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val LAST_MODE = stringPreferencesKey("last_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IPC_TOKEN = stringPreferencesKey("ipc_token")
        val MCP_PORT = intPreferencesKey("mcp_port")
        val AUTO_START_MODE = stringPreferencesKey("auto_start_mode")
        val STAR_PROMPT_DISMISSED = booleanPreferencesKey("star_prompt_dismissed")
        val OWNERSHIP_BOUNDARY_V1 = booleanPreferencesKey("ownership_boundary_v1")
    }

    val serverConfig: Flow<ServerConfig> = dataStore.data.map { prefs ->
        ServerConfig(
            host = prefs[Keys.SERVER_HOST] ?: "",
            port = prefs[Keys.SERVER_PORT] ?: 5987,
            autoConnect = prefs[Keys.AUTO_CONNECT] ?: false
        )
    }

    val serverUrl: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.SERVER_URL] }
    val autoStartOnBoot: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.AUTO_START_ON_BOOT] ?: false }
    val deviceId: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.DEVICE_ID] }
    val onboardingComplete: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.ONBOARDING_COMPLETE] ?: false }
    val lastMode: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.LAST_MODE] }
    val themeMode: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.THEME_MODE] ?: "system" }
    val starPromptDismissed: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.STAR_PROMPT_DISMISSED] ?: false }
    val ipcToken: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.IPC_TOKEN] }
    val mcpPort: Flow<Int> = dataStore.data.map { prefs -> prefs[Keys.MCP_PORT] ?: 8080 }
    val autoStartMode: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.AUTO_START_MODE] }

    /**
     * One-time ownership boundary for the Светлана build.
     * Removes persisted remote endpoints/tokens from any predecessor installation
     * before boot auto-start or remote reconnect can reuse them.
     */
    suspend fun enforceOwnershipBoundary() {
        dataStore.edit { prefs ->
            if (prefs[Keys.OWNERSHIP_BOUNDARY_V1] != true) {
                prefs.remove(Keys.SERVER_HOST)
                prefs.remove(Keys.SERVER_PORT)
                prefs.remove(Keys.SERVER_URL)
                prefs[Keys.AUTO_CONNECT] = false
                prefs.remove(Keys.IPC_TOKEN)
                prefs.remove(Keys.DEVICE_ID)
                prefs[Keys.OWNERSHIP_BOUNDARY_V1] = true
            }
        }
    }

    suspend fun saveServerUrl(url: String) { dataStore.edit { it[Keys.SERVER_URL] = url } }
    suspend fun saveServerConfig(host: String, port: Int, autoConnect: Boolean = false) = dataStore.edit {
        it[Keys.SERVER_HOST] = host
        it[Keys.SERVER_PORT] = port
        it[Keys.AUTO_CONNECT] = autoConnect
    }
    suspend fun setAutoStartOnBoot(enabled: Boolean) { dataStore.edit { it[Keys.AUTO_START_ON_BOOT] = enabled } }
    suspend fun saveDeviceId(deviceId: String) { dataStore.edit { it[Keys.DEVICE_ID] = deviceId } }
    suspend fun clearDeviceId() { dataStore.edit { it.remove(Keys.DEVICE_ID) } }
    suspend fun setStarPromptDismissed(dismissed: Boolean) { dataStore.edit { it[Keys.STAR_PROMPT_DISMISSED] = dismissed } }
    suspend fun setOnboardingComplete(complete: Boolean) { dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete } }
    suspend fun setLastMode(mode: String) { dataStore.edit { it[Keys.LAST_MODE] = mode } }
    suspend fun setThemeMode(mode: String) { dataStore.edit { it[Keys.THEME_MODE] = mode } }
    suspend fun saveIpcToken(token: String) { dataStore.edit { it[Keys.IPC_TOKEN] = token } }
    suspend fun setMcpPort(port: Int) { dataStore.edit { it[Keys.MCP_PORT] = port } }
    suspend fun setAutoStartMode(mode: String?) { dataStore.edit { prefs -> if (mode != null) prefs[Keys.AUTO_START_MODE] = mode else prefs.remove(Keys.AUTO_START_MODE) } }
}
