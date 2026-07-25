package com.rangerscards

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.rangerscards.ui.navigation.RangersNavHost
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.RangersCardsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: AppViewModel
    private var appUpdateManager: AppUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var isDataLoaded = false
        splashScreen.setKeepOnScreenCondition { !isDataLoaded }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val isPlayServicesAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this)

        if (isPlayServicesAvailable == ConnectionResult.SUCCESS) maybeCheckForUpdate()

        setContent {
            viewModel = hiltViewModel<AppViewModel>(this)
            val theme by viewModel.themeState.collectAsState()
            // Collecting user's theme from shared preferences via viewmodel - false = light, true = dark
            val currentTheme = when(theme) {
                0 -> false
                1 -> true
                2 -> isSystemInDarkTheme()
                else -> null
            }
            if (currentTheme != null) {
                isDataLoaded = true
                RangersCardsTheme(currentTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = CustomTheme.colors.l30
                    ) {
                        RangersNavHost(
                            isDarkTheme = currentTheme,
                            appViewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    private fun maybeCheckForUpdate() {
        val manager = runCatching { AppUpdateManagerFactory.create(this) }.getOrNull()
            ?: return

        appUpdateManager = manager

        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val allowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                val stalenessDays = info.clientVersionStalenessDays() ?: -1

                val shouldForceUpdate = available && allowed && stalenessDays >= 14
                if (shouldForceUpdate) {
                    manager.startUpdateFlow(
                        info,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
    }

    // Checks that the update is not stalled during 'onResume()'.
    override fun onResume() {
        super.onResume()

        val manager = appUpdateManager ?: return

        manager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    manager.startUpdateFlow(
                        appUpdateInfo,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
    }
}