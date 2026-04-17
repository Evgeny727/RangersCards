package com.rangerscards.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rangerscards.R
import com.rangerscards.ui.settings.components.SettingsBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun SettingsDiagnosticsScreen(
    settingsViewModel: SettingsViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val coroutine = rememberCoroutineScope()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    if (showLoadingDialog) Dialog(
        onDismissRequest = { showLoadingDialog = false },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        SettingsBaseCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = R.string.saving_changes_header
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = CustomTheme.colors.m)
            }
        }
    }
    LazyColumn(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            ),
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().clickable { coroutine.launch { showLoadingDialog = true
                settingsViewModel.clearLocalData()
            }.invokeOnCompletion { showLoadingDialog = false
                Toast.makeText(
                    context,
                    context.getString(R.string.diagnostics_clear_local_data_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }}) {
                Text(
                    text = stringResource(R.string.diagnostics_clear_local_data),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(8.dp)
                )
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
        }
        item {
            Column(modifier = Modifier.fillMaxWidth().clickable { context.clearCoilCache()
                Toast.makeText(
                    context,
                    context.getString(R.string.diagnostics_clear_coil_cache_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                Text(
                    text = stringResource(R.string.diagnostics_clear_coil_cache),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(8.dp)
                )
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
        }
        item {
            Column(modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.clearApolloCache()
                Toast.makeText(
                    context,
                    context.getString(R.string.diagnostics_clear_apollo_cache_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                Text(
                    text = stringResource(R.string.diagnostics_clear_apollo_cache),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(8.dp)
                )
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
        }
    }
}