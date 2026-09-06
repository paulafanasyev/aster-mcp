package com.aster.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.aster.ui.theme.AsterTheme

/**
 * User-facing assistant name. Keep the internal package/module namespace unchanged,
 * but do not expose the legacy Aster/OpenAlly brands in the application UI.
 */
@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    tagline: String? = null,
) {
    val colors = AsterTheme.colors

    Text(
        modifier = modifier,
        text = "Светлана",
        style = if (compact) {
            MaterialTheme.typography.headlineSmall
        } else {
            MaterialTheme.typography.headlineLarge
        },
        fontWeight = FontWeight.Bold,
        color = colors.text,
    )
}
