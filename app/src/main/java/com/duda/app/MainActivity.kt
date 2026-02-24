package com.duda.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.duda.app.core.navigation.DudaNavGraph
import com.duda.app.core.navigation.Screen
import com.duda.app.presentation.common.DudaTheme
import com.duda.app.presentation.common.MainScreen
import com.duda.app.presentation.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // URI recebida via Share Intent (pode ser null)
    private var sharedFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar splash screen ANTES do super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Processar Share Intent se houver
        sharedFileUri = getSharedUri(intent)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsUiState by settingsViewModel.uiState.collectAsState()

            DudaTheme(appTheme = settingsUiState.appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    MainScreen(navController = navController) {
                        // Se chegou via Share Intent, navegar para tela de Import
                        val startDest = if (sharedFileUri != null) {
                            Screen.Import.route
                        } else {
                            Screen.Library.route
                        }

                        DudaNavGraph(
                            navController = navController,
                            startDestination = startDest
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Lidar com novos Share Intents enquanto o app já está aberto
        sharedFileUri = intent?.let { getSharedUri(it) }
    }

    /**
     * Extrai a URI de um Share Intent (ACTION_SEND ou ACTION_VIEW).
     */
    private fun getSharedUri(intent: Intent): Uri? {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            }
            Intent.ACTION_VIEW -> intent.data
            else -> null
        }
    }
}
