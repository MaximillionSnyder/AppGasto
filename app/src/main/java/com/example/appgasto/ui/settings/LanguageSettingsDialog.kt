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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.appgasto.R
import com.example.appgasto.domain.model.AppLanguage

@Composable
fun LanguageSettingsDialog(
    currentLanguage: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column {
                LanguageOption(AppLanguage.SYSTEM, R.string.lang_system, currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.SPANISH, R.string.lang_es, currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.ENGLISH, R.string.lang_en, currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.PORTUGUESE, R.string.lang_pt, currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.ITALIAN, R.string.lang_it, currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.GERMAN, R.string.lang_de, currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.JAPANESE, R.string.lang_ja, currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.KOREAN, R.string.lang_ko, currentLanguage, onSelect)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageOption(AppLanguage.QUECHUA, R.string.lang_qu, currentLanguage, onSelect)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun LanguageOption(
    language: AppLanguage,
    labelRes: Int,
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
            text = stringResource(labelRes),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
