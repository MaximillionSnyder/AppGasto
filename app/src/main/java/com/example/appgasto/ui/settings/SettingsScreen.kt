package com.example.appgasto.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appgasto.R
import com.example.appgasto.ui.theme.GradientTertiary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDark: Boolean,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = viewModel.importData(inputStream)
                    snackbarHostState.showSnackbar(
                        if (result.isSuccess) context.getString(R.string.import_success) else context.getString(R.string.import_error)
                    )
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val result = viewModel.exportData(outputStream)
                    snackbarHostState.showSnackbar(
                        if (result.isSuccess) context.getString(R.string.export_success) else context.getString(R.string.export_error)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(
                title = stringResource(R.string.monthly_budget),
                icon = Icons.Default.MonetizationOn,
                iconColor = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.monthly_budget),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (state.budgetEnabled && state.monthlyBudget > 0) {
                            Text(
                                text = "S/. ${String.format("%.2f", state.monthlyBudget)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Switch(
                        checked = state.budgetEnabled,
                        onCheckedChange = { viewModel.setBudgetEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (state.budgetEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBudgetDialog = true }
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Text(
                            text = if (state.monthlyBudget > 0) stringResource(R.string.change_amount) else stringResource(R.string.set_budget),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSection(
                title = stringResource(R.string.appearance),
                icon = Icons.Default.DarkMode,
                iconColor = MaterialTheme.colorScheme.secondary
            ) {
                SettingsRow(
                    icon = Icons.Default.DarkMode,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.theme),
                    subtitle = when (state.themeMode) {
                        com.example.appgasto.domain.model.ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                        com.example.appgasto.domain.model.ThemeMode.DARK -> stringResource(R.string.theme_dark)
                        com.example.appgasto.domain.model.ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                        com.example.appgasto.domain.model.ThemeMode.MATRIX -> stringResource(R.string.theme_matrix)
                    },
                    onClick = { showThemeDialog = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                SettingsRow(
                    icon = Icons.Default.Language,
                    iconColor = GradientTertiary,
                    title = stringResource(R.string.language),
                    subtitle = when (state.language) {
                        com.example.appgasto.domain.model.AppLanguage.SPANISH -> stringResource(R.string.lang_es)
                        com.example.appgasto.domain.model.AppLanguage.ENGLISH -> stringResource(R.string.lang_en)
                        com.example.appgasto.domain.model.AppLanguage.PORTUGUESE -> stringResource(R.string.lang_pt)
                        com.example.appgasto.domain.model.AppLanguage.ITALIAN -> stringResource(R.string.lang_it)
                        com.example.appgasto.domain.model.AppLanguage.GERMAN -> stringResource(R.string.lang_de)
                        com.example.appgasto.domain.model.AppLanguage.JAPANESE -> stringResource(R.string.lang_ja)
                        com.example.appgasto.domain.model.AppLanguage.KOREAN -> stringResource(R.string.lang_ko)
                        com.example.appgasto.domain.model.AppLanguage.QUECHUA -> stringResource(R.string.lang_qu)
                        else -> stringResource(R.string.lang_system)
                    },
                    onClick = { showLanguageDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSection(
                title = stringResource(R.string.currency),
                icon = Icons.Default.CurrencyExchange,
                iconColor = MaterialTheme.colorScheme.primary
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.base_currency_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.refreshRates { success ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(
                                            if (success) R.string.rates_updated else R.string.rates_error
                                        )
                                    )
                                }
                            }
                        },
                        enabled = !state.isRefreshingRates,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (state.isRefreshingRates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.refresh_rates))
                        }
                    }
                    if (state.ratesUpdatedAt > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.last_update,
                                java.text.SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm",
                                    java.util.Locale.getDefault()
                                ).format(java.util.Date(state.ratesUpdatedAt))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSection(
                title = stringResource(R.string.backup),
                icon = Icons.Default.CloudUpload,
                iconColor = GradientTertiary
            ) {
                SettingsRow(
                    icon = Icons.Default.CloudUpload,
                    iconColor = GradientTertiary,
                    title = stringResource(R.string.export_data),
                    subtitle = stringResource(R.string.export_description),
                    onClick = {
                        val dateStr = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        exportLauncher.launch("appgasto_backup_$dateStr.json")
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                SettingsRow(
                    icon = Icons.Default.CloudDownload,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.import_data),
                    subtitle = stringResource(R.string.import_description),
                    onClick = { importLauncher.launch("application/json") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSection(
                title = stringResource(R.string.about),
                icon = Icons.Default.Info,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Versión 0.2",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showThemeDialog) {
            ThemeSettingsDialog(
                currentTheme = state.themeMode,
                onSelect = {
                    viewModel.setThemeMode(it)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        if (showLanguageDialog) {
            LanguageSettingsDialog(
                currentLanguage = state.language,
                onSelect = {
                    viewModel.setLanguage(it)
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }

        if (showBudgetDialog) {
            BudgetDialog(
                currentBudget = state.monthlyBudget,
                onSave = { viewModel.setMonthlyBudget(it) },
                onDismiss = { showBudgetDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
