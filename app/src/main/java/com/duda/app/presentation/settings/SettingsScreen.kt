package com.duda.app.presentation.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duda.app.presentation.common.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "duda_settings")

object PreferencesKeys {
    val APP_THEME = stringPreferencesKey("app_theme")
    val READER_FONT_SIZE = floatPreferencesKey("reader_font_size")
}

data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val readerFontSize: Float = 16f
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = context.dataStore.data
        .map { preferences ->
            SettingsUiState(
                appTheme = AppTheme.valueOf(
                    preferences[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name
                ),
                readerFontSize = preferences[PreferencesKeys.READER_FONT_SIZE] ?: 16f
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.APP_THEME] = theme.name
            }
        }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.READER_FONT_SIZE] = size
            }
        }
    }
}

// ─── SettingsScreen ───────────────────────────────────────────────────────────

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurações") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Tema ──────────────────────────────────────────────────────────
            Text("Tema", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTheme.values().forEach { theme ->
                    FilterChip(
                        selected = uiState.appTheme == theme,
                        onClick = { viewModel.setAppTheme(theme) },
                        label = {
                            Text(
                                when (theme) {
                                    AppTheme.SYSTEM -> "Sistema"
                                    AppTheme.LIGHT -> "Claro"
                                    AppTheme.DARK -> "Escuro"
                                }
                            )
                        }
                    )
                }
            }

            Divider()

            // ── Tamanho da fonte ──────────────────────────────────────────────
            Text("Tamanho da fonte no leitor", style = MaterialTheme.typography.titleMedium)

            Text(
                text = "${uiState.readerFontSize.toInt()}sp",
                style = MaterialTheme.typography.bodyMedium
            )

            Slider(
                value = uiState.readerFontSize,
                onValueChange = { viewModel.setFontSize(it) },
                valueRange = 12f..28f,
                steps = 7,
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            // ── Sobre ─────────────────────────────────────────────────────────
            Text("Sobre", style = MaterialTheme.typography.titleMedium)
            Text("Duda — Leitor de ebooks", style = MaterialTheme.typography.bodyMedium)
            Text("Versão 1.0.0", style = MaterialTheme.typography.bodySmall)
        }
    }
}
