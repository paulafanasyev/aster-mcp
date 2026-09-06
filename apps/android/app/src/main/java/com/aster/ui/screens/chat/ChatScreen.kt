package com.aster.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aster.mobileagent.MobileAgentChatClient
import com.aster.mobileagent.MobileAgentSettingsStore
import com.aster.ui.theme.AsterTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

private data class ChatMessage(val role: String, val text: String)

@Composable
fun ChatScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { MobileAgentSettingsStore(context) }
    val client = remember { MobileAgentChatClient() }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var baseUrl by remember { mutableStateOf(store.baseUrl()) }
    var model by remember { mutableStateOf(store.model()) }
    var apiKey by remember { mutableStateOf(store.apiKey()) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || sending) return
        val configuredUrl = store.baseUrl()
        val configuredModel = store.model()
        val configuredKey = store.apiKey()
        if (configuredKey.isBlank()) {
            showSettings = true
            return
        }

        input = ""
        messages.add(ChatMessage("user", text))
        messages.add(ChatMessage("assistant", ""))
        sending = true
        error = null
        scope.launch {
            try {
                val payload = messages.dropLast(1).takeLast(20).map {
                    MobileAgentChatClient.Message(it.role, it.text)
                }
                client.stream(configuredUrl, configuredModel, configuredKey, payload) { reply ->
                    scope.launch {
                        val index = messages.lastIndex
                        if (index >= 0 && messages[index].role == "assistant") {
                            messages[index] = ChatMessage("assistant", reply)
                        }
                    }
                }
            } catch (t: Throwable) {
                if (messages.lastOrNull()?.role == "assistant" && messages.last().text.isBlank()) {
                    messages.removeAt(messages.lastIndex)
                }
                error = t.message ?: "Не удалось получить ответ Светланы"
            } finally {
                sending = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AsterTheme.colors.bg)
            .imePadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Чат со Светланой", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("MobileAgent", style = MaterialTheme.typography.labelSmall, color = AsterTheme.colors.textMuted)
            }
            IconButton(onClick = { showSettings = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Настроить AI")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Text(
                        "Напишите Светлане сообщение. Ответ поступит от выбранной вами AI-модели через MobileAgent.",
                        modifier = Modifier.padding(16.dp),
                        color = AsterTheme.colors.textMuted
                    )
                }
            }
            items(messages) { message ->
                val isUser = message.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Text(
                        text = message.text.ifBlank { if (sending && !isUser) "Светлана печатает…" else "" },
                        modifier = Modifier.padding(12.dp),
                        color = if (isUser) AsterTheme.colors.text else AsterTheme.colors.textMuted
                    )
                }
            }
        }

        if (error != null) {
            Text(error.orEmpty(), modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.error)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                enabled = !sending,
                placeholder = { Text("Напишите Светлане…") },
                singleLine = true
            )
            IconButton(onClick = ::send, enabled = input.isNotBlank() && !sending) {
                Icon(Icons.Default.Send, contentDescription = "Отправить")
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Настройка AI для Светланы") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("MobileAgent использует совместимый с OpenAI API интерфейс. Ключ хранится зашифрованно в Android Keystore.")
                    OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, label = { Text("Адрес API") }, singleLine = true)
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Модель") }, singleLine = true)
                    OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API-ключ") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    store.save(baseUrl, model, apiKey)
                    showSettings = false
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { showSettings = false }) { Text("Отмена") } }
        )
    }
}
