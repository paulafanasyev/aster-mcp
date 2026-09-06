package com.aster.ui.screens.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aster.ui.components.AsterButton
import com.aster.ui.components.AsterButtonVariant
import com.aster.ui.theme.AsterTheme

@Composable
fun ContactsScreen(onNavigateBack: () -> Unit) {
    val colors = AsterTheme.colors
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    fun openPhone() = context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+84843012046")))
    fun openEmail() = context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:xongphavietnam@gmail.com")))
    fun openWhatsApp() = uriHandler.openUri("https://wa.me/7948289964")
    fun openTelegram() = uriHandler.openUri("https://t.me/PaulPavel_it_dev")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = colors.text)
            }
            Text(
                text = "Контакты",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.text,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Павел Афанасьев",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Разработчик",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textMuted,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Автономная Некоммерческая Организация Центр Поддержки Самозанятых \"Мир Самозанятых\"",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted
        )
        Text(
            text = "ИНН 9724016805",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        AsterButton(
            onClick = ::openPhone,
            text = "Телефон: +84 843 012 046",
            variant = AsterButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )
        AsterButton(
            onClick = ::openEmail,
            text = "Email: xongphavietnam@gmail.com",
            variant = AsterButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )
        AsterButton(
            onClick = ::openWhatsApp,
            text = "WhatsApp: +7948289964",
            variant = AsterButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )
        AsterButton(
            onClick = ::openTelegram,
            text = "Telegram: @PaulPavel_it_dev",
            variant = AsterButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
