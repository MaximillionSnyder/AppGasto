package com.example.appgasto.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appgasto.domain.model.AppLanguage

@Composable
fun LanguageSettingsDialog(
    currentLanguage: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Idioma") },
        text = {
            Column {
                LanguageOption(AppLanguage.SYSTEM, "Sistema", currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.SPANISH, "Español", currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.ENGLISH, "English", currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.PORTUGUESE, "Português", currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.ITALIAN, "Italiano", currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.GERMAN, "Deutsch", currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.JAPANESE, "日本語", currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.KOREAN, "한국어", currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.QUECHUA, "Runasimi", currentLanguage, onSelect)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun LanguageOption(
    language: AppLanguage,
    label: String,
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(language) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = current == language,
            onClick = { onSelect(language) }
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
