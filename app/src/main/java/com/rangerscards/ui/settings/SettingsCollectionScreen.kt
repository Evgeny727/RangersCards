package com.rangerscards.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.rangerscards.ui.components.RangersRadioButton
import com.rangerscards.ui.settings.components.SettingsBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun SettingsCollectionScreen(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val user by settingsViewModel.userUiState.collectAsState()
    val context = LocalContext.current.applicationContext
    val coroutine = rememberCoroutineScope()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
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
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l20)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        LazyColumn(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item("loa") {
                val selected = user.settings.collection.contains("loa")
                Column {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { coroutine.launch { showLoadingDialog = true
                                if (selected) settingsViewModel.setCollection(user.settings.collection.filterNot { it == "loa" }, context)
                                else settingsViewModel.setCollection(user.settings.collection + "loa", context)
                            }.invokeOnCompletion { showLoadingDialog = false } },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.loa_expansion),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        RangersRadioButton(
                            selected = selected,
                            onClick = { coroutine.launch { showLoadingDialog = true
                                if (selected) settingsViewModel.setCollection(user.settings.collection.filterNot { it == "loa" }, context)
                                else settingsViewModel.setCollection(user.settings.collection + "loa", context)
                            }.invokeOnCompletion { showLoadingDialog = false } },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
            item("sotv") {
                val selected = user.settings.collection.contains("sotv")
                Column {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { coroutine.launch { showLoadingDialog = true
                                if (selected) settingsViewModel.setCollection(user.settings.collection.filterNot { it == "sotv" }, context)
                                else settingsViewModel.setCollection(user.settings.collection + "sotv", context)
                            }.invokeOnCompletion { showLoadingDialog = false } },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.sotv_expansion),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        RangersRadioButton(
                            selected = selected,
                            onClick = { coroutine.launch { showLoadingDialog = true
                                if (selected) settingsViewModel.setCollection(user.settings.collection.filterNot { it == "sotv" }, context)
                                else settingsViewModel.setCollection(user.settings.collection + "sotv", context)
                            }.invokeOnCompletion { showLoadingDialog = false } },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
            item("sib") {
                val selected = user.settings.collection.contains("sib")
                Column {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { coroutine.launch { showLoadingDialog = true
                                if (selected) settingsViewModel.setCollection(user.settings.collection.filterNot { it == "sib" }, context)
                                else settingsViewModel.setCollection(user.settings.collection + "sib", context)
                            }.invokeOnCompletion { showLoadingDialog = false } },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.spire_in_bloom),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        RangersRadioButton(
                            selected = selected,
                            onClick = { coroutine.launch { showLoadingDialog = true
                                if (selected) settingsViewModel.setCollection(user.settings.collection.filterNot { it == "sib" }, context)
                                else settingsViewModel.setCollection(user.settings.collection + "sib", context)
                            }.invokeOnCompletion { showLoadingDialog = false } },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
            item("sos") {
                val selected = user.settings.collection.contains("sos")
                Column {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { coroutine.launch { showLoadingDialog = true
                                if (selected) settingsViewModel.setCollection(user.settings.collection.filterNot { it == "sos" }, context)
                                else settingsViewModel.setCollection(user.settings.collection + "sos", context)
                            }.invokeOnCompletion { showLoadingDialog = false } },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.shadow_of_the_storm),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        RangersRadioButton(
                            selected = selected,
                            onClick = { coroutine.launch { showLoadingDialog = true
                                if (selected) settingsViewModel.setCollection(user.settings.collection.filterNot { it == "sos" }, context)
                                else settingsViewModel.setCollection(user.settings.collection + "sos", context)
                            }.invokeOnCompletion { showLoadingDialog = false } },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
        }
    }
}