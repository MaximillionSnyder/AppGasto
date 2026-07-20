package com.example.appgasto.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appgasto.BuildConfig
import com.example.appgasto.R
import com.example.appgasto.ui.theme.GradientEnd
import com.example.appgasto.ui.theme.GradientStart
import com.example.appgasto.ui.theme.GradientTertiary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private val settingsDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
private val backupDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDark: Boolean,
    onNavigateBack: () -> Unit = {},
    embeddedInPager: Boolean = false,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBaseCurrencyDialog by remember { mutableStateOf(false) }
    var showFontScaleDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = viewModel.importData(inputStream)
                    val message = if (result.isSuccess) {
                        context.getString(R.string.import_success)
                    } else {
                        val error = result.exceptionOrNull()?.localizedMessage ?: ""
                        if (error.isNotBlank()) {
                            context.getString(R.string.import_error_detail, error)
                        } else {
                            context.getString(R.string.import_error)
                        }
                    }
                    snackbarHostState.showSnackbar(message)
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

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val result = viewModel.exportCsv(outputStream)
                    snackbarHostState.showSnackbar(
                        if (result.isSuccess) context.getString(R.string.csv_export_success) else context.getString(R.string.csv_export_error)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (!embeddedInPager) {
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
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            GeneralSettingsSection(
                budgetEnabled = state.budgetEnabled,
                monthlyBudget = state.monthlyBudget,
                monthlyExpenseTotal = state.monthlyExpenseTotal,
                baseCurrency = state.baseCurrency,
                onBudgetToggle = { enabled ->
                    viewModel.setBudgetEnabled(enabled)
                    if (enabled && state.monthlyBudget <= 0) {
                        showBudgetDialog = true
                    }
                },
                onBudgetClick = { showBudgetDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CurrencySettingsSection(
                baseCurrency = state.baseCurrency,
                isRefreshingRates = state.isRefreshingRates,
                ratesUpdatedAt = state.ratesUpdatedAt,
                onBaseCurrencyClick = { showBaseCurrencyDialog = true },
                onRefreshRates = {
                    viewModel.refreshRates { success ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(
                                    if (success) R.string.rates_updated else R.string.rates_error
                                )
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            AppearanceSettingsSection(
                themeMode = state.themeMode,
                fontScale = state.fontScale,
                language = state.language,
                onThemeClick = { showThemeDialog = true },
                onFontScaleClick = { showFontScaleDialog = true },
                onLanguageClick = { showLanguageDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))
            DataSettingsSection(
                onExportClick = {
                    val dateStr = java.time.LocalDateTime.now().format(backupDateFormatter)
                    exportLauncher.launch("appgasto_backup_$dateStr.json")
                },
                onImportClick = { importLauncher.launch("*/*") },
                onCsvExportClick = {
                    val dateStr = java.time.LocalDateTime.now().format(backupDateFormatter)
                    csvExportLauncher.launch("appgasto_$dateStr.csv")
                },
                onResetClick = { showResetDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))
            InfoSettingsSection()

            Spacer(modifier = Modifier.height(24.dp))
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

        if (showFontScaleDialog) {
            FontScaleDialog(
                currentScale = state.fontScale,
                onSelect = {
                    viewModel.setFontScale(it)
                    showFontScaleDialog = false
                },
                onDismiss = { showFontScaleDialog = false }
            )
        }

        if (showBaseCurrencyDialog) {
            CurrencySettingsDialog(
                currentCurrency = state.baseCurrency,
                onSelect = { viewModel.setBaseCurrency(it) },
                onDismiss = { showBaseCurrencyDialog = false }
            )
        }

        if (showBudgetDialog) {
            BudgetDialog(
                currentBudget = state.monthlyBudget,
                onSave = { viewModel.setMonthlyBudget(it) },
                onDismiss = { showBudgetDialog = false }
            )
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.reset_data),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.reset_data_confirm),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.reset_done)
                            )
                        }
                    }) {
                        Text(
                            stringResource(R.string.confirm),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                shape = MaterialTheme.shapes.large
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 4.dp, bottom = 4.dp, top = 4.dp)
            .semantics { heading() }
    )
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = stringResource(
                        if (expanded) R.string.cd_collapse else R.string.cd_expand
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String = "",
    onClick: () -> Unit,
    showArrow: Boolean = true
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
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showArrow) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GeneralSettingsSection(
    budgetEnabled: Boolean,
    monthlyBudget: Double,
    monthlyExpenseTotal: Double,
    baseCurrency: com.example.appgasto.domain.model.Currency,
    onBudgetToggle: (Boolean) -> Unit,
    onBudgetClick: () -> Unit
) {
    SettingsSectionHeader(stringResource(R.string.section_general))

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
                if (budgetEnabled && monthlyBudget > 0) {
                    Text(
                        text = baseCurrency.format(monthlyBudget),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Switch(
                checked = budgetEnabled,
                onCheckedChange = onBudgetToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        AnimatedVisibility(
            visible = budgetEnabled,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                if (monthlyBudget > 0) {
                    val ratio = (monthlyExpenseTotal / monthlyBudget).toFloat().coerceIn(0f, 1.5f)
                    val progressColor = when {
                        ratio >= 1f -> MaterialTheme.colorScheme.error
                        ratio >= 0.8f -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    LinearProgressIndicator(
                        progress = { ratio.coerceAtMost(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (ratio >= 1f) stringResource(R.string.budget_exceeded)
                               else "${baseCurrency.format(monthlyExpenseTotal)} de ${baseCurrency.format(monthlyBudget)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = progressColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                SettingsRow(
                    icon = Icons.Default.MonetizationOn,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = if (monthlyBudget > 0) stringResource(R.string.change_amount)
                            else stringResource(R.string.set_budget),
                    onClick = onBudgetClick,
                    showArrow = true
                )
            }
        }
    }
}

@Composable
private fun CurrencySettingsSection(
    baseCurrency: com.example.appgasto.domain.model.Currency,
    isRefreshingRates: Boolean,
    ratesUpdatedAt: Long,
    onBaseCurrencyClick: () -> Unit,
    onRefreshRates: () -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.currency),
        icon = Icons.Default.CurrencyExchange,
        iconColor = MaterialTheme.colorScheme.primary
    ) {
        SettingsRow(
            icon = Icons.Default.CurrencyExchange,
            iconColor = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.base_currency),
            subtitle = "${baseCurrency.symbol}  ${baseCurrency.code}",
            onClick = onBaseCurrencyClick,
            showArrow = true
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRefreshRates,
                enabled = !isRefreshingRates,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isRefreshingRates) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.refresh_rates))
                }
            }
        }

        if (ratesUpdatedAt > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.last_update,
                    settingsDateFormat.format(Date(ratesUpdatedAt))
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun AppearanceSettingsSection(
    themeMode: com.example.appgasto.domain.model.ThemeMode,
    fontScale: com.example.appgasto.domain.model.FontScale,
    language: com.example.appgasto.domain.model.AppLanguage,
    onThemeClick: () -> Unit,
    onFontScaleClick: () -> Unit,
    onLanguageClick: () -> Unit
) {
    SettingsSectionHeader(stringResource(R.string.section_appearance))

    SettingsSection(
        title = stringResource(R.string.appearance),
        icon = Icons.Default.DarkMode,
        iconColor = MaterialTheme.colorScheme.secondary
    ) {
        SettingsRow(
            icon = Icons.Default.DarkMode,
            iconColor = MaterialTheme.colorScheme.secondary,
            title = stringResource(R.string.theme),
            subtitle = when (themeMode) {
                com.example.appgasto.domain.model.ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                com.example.appgasto.domain.model.ThemeMode.DARK -> stringResource(R.string.theme_dark)
                com.example.appgasto.domain.model.ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                com.example.appgasto.domain.model.ThemeMode.MATRIX -> stringResource(R.string.theme_matrix)
                com.example.appgasto.domain.model.ThemeMode.HIGH_CONTRAST -> stringResource(R.string.theme_high_contrast)
            },
            onClick = onThemeClick
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        SettingsRow(
            icon = Icons.Default.FormatSize,
            iconColor = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.font_size),
            subtitle = when (fontScale) {
                com.example.appgasto.domain.model.FontScale.SMALL -> stringResource(R.string.font_scale_small)
                com.example.appgasto.domain.model.FontScale.NORMAL -> stringResource(R.string.font_scale_normal)
                com.example.appgasto.domain.model.FontScale.LARGE -> stringResource(R.string.font_scale_large)
                com.example.appgasto.domain.model.FontScale.EXTRA_LARGE -> stringResource(R.string.font_scale_extra_large)
            },
            onClick = onFontScaleClick
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        SettingsRow(
            icon = Icons.Default.Language,
            iconColor = GradientTertiary,
            title = stringResource(R.string.language),
            subtitle = when (language) {
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
            onClick = onLanguageClick
        )
    }
}

@Composable
private fun DataSettingsSection(
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onCsvExportClick: () -> Unit,
    onResetClick: () -> Unit
) {
    SettingsSectionHeader(stringResource(R.string.section_data))

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
            onClick = onExportClick
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
            onClick = onImportClick
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        SettingsRow(
            icon = Icons.Default.TableChart,
            iconColor = MaterialTheme.colorScheme.tertiary,
            title = stringResource(R.string.export_csv),
            subtitle = stringResource(R.string.export_csv_description),
            onClick = onCsvExportClick
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        SettingsRow(
            icon = Icons.Default.DeleteForever,
            iconColor = MaterialTheme.colorScheme.error,
            title = stringResource(R.string.reset_data),
            subtitle = stringResource(R.string.reset_data_description),
            onClick = onResetClick
        )
    }
}

@Composable
private fun InfoSettingsSection() {
    SettingsSectionHeader(stringResource(R.string.section_info))

    SettingsSection(
        title = stringResource(R.string.about),
        icon = Icons.Default.Info,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.made_with),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
