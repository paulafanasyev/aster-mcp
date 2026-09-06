package com.aster.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aster.mobileagent.MobileAgentLlmClient
import com.aster.mobileagent.MobileAgentSettingsStore
import kotlinx.coroutines.launch

private data class ChatMessage(val role: String, val text: String)

@Composable
fun SvetlanaChatScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { MobileAgentSettingsStore(context) }
    val client = remember { MobileAgentLlmClient() }
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    val profileReady = store.apiKey().isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чат со Светланой") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки ИИ")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            if (!profileReady) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Чтобы начать настоящий разговор со Светланой, настройте подключение к ИИ.")
                    Text("Ключ хранится только на этом устройстве в Android Keystore.")
                    Button(onClick = { showSettings = true }) { Text("Настроить ИИ") }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { message ->
                    Text(
                        text = if (message.role == "user") "Вы: ${message.text}" else "Светлана: ${message.text}",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (sending) item { Text("Светлана отвечает…") }
                error?.let { message -> item { Text("Ошибка: $message") } }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    enabled = profileReady && !sending,
                    placeholder = { Text("Напишите Светлане…") },
                    singleLine = false
                )
                IconButton(
                    onClick = {
                        val text = input.trim()
                        if (text.isEmpty() || sending) return@IconButton
                        input = ""
                        error = null
                        messages = messages + ChatMessage("user", text) + ChatMessage("assistant", "")
                        sending = true
                        scope.launch {
                            runCatching {
                                val profile = store.read()
                                    ?: error("Не настроено подключение к ИИ")
                                val history = messages
                                    .filter { it.text.isNotBlank() }
                                    .takeLast(20)
                                    .map { MobileAgentLlmClient.Message(it.role, it.text) }
                                client.streamChat(
                                    baseUrl = profile.baseUrl,
                                    model = profile.model,
                                    apiKey = profile.apiKey,
                                    messages = history
                                ) { reply ->
                                    messages = messages.dropLast(1) + ChatMessage("assistant", reply)
                                }
                            }.onFailure { throwable ->
                                messages = messages.dropLast(1)
                                error = throwable.message ?: "Не удалось получить ответ"
                            }
                            sending = false
                        }
                    },
                    enabled = profileReady && input.isNotBlank() && !sending,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Отправить")
                }
            }
        }
    }

    if (showSettings) {
        MobileAgentSettingsDialog(
            store = store,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
private fun MobileAgentSettingsDialog(
    store: MobileAgentSettingsStore,
    onDismiss: () -> Unit
) {
    var baseUrl by remember { mutableStateOf(store.baseUrl()) }
    var model by remember { mutableStateOf(store.model()) }
    var apiKey by remember { mutableStateOf(store.apiKey()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка ИИ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("MobileAgent использует совместимый с OpenAI API интерфейс. Секретный ключ хранится зашифрованным.")
                OutlinedTextField(baseUrl, { baseUrl = it }, label = { Text("Адрес API") }, singleLine = true)
                OutlinedTextField(model, { model = it }, label = { Text("Модель") }, singleLine = true)
                OutlinedTextField(apiKey, { apiKey = it }, label = { Text("Ключ API") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    store.save(baseUrl, model, apiKey)
                    onDismiss()
                },
                enabled = baseUrl.isNotBlank() && model.isNotBlank() && apiKey.isNotBlank()
            ) { Text("Сохранить") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
