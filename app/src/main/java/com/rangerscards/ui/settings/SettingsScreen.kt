package com.rangerscards.ui.settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rangerscards.AppViewModel
import com.rangerscards.ui.components.RangersLoadingDialog
import com.rangerscards.ui.settings.components.AccountCard
import com.rangerscards.ui.settings.components.CardsCard
import com.rangerscards.ui.settings.components.SettingsCard
import com.rangerscards.ui.settings.components.SocialsCard
import com.rangerscards.ui.settings.components.SupportCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.utils.applyScaffoldPaddings

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    navigateToAbout: () -> Unit,
    navigateToDiagnostics: () -> Unit,
    navigateToFriends: () -> Unit,
    navigateToCollection: () -> Unit,
    appViewModel: AppViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val activity = LocalActivity.current
    BackHandler {
        activity?.finish()
    }
    val user by appViewModel.userUiState.collectAsState()
    val themeInt by appViewModel.themeState.collectAsState()
    val englishResults by settingsViewModel.isIncludeEnglishSearchResultsState.collectAsState()
    val settingsUiState by settingsViewModel.settingsUiState.collectAsState()

    LaunchedEffect(Unit) {
        settingsViewModel.events.collect {
            appViewModel.emitError(it.exception)
        }
    }
    LaunchedEffect(Unit) {
        settingsViewModel.userEvents.collect {
            appViewModel.emitUserEvent()
        }
    }

    if (settingsUiState is SettingsUiState.Loading) RangersLoadingDialog(isDarkTheme = isDarkTheme)


    LazyColumn(
        modifier = modifier
            .background(CustomTheme.colors.l10)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
    ) {
        item {
            AccountCard(
                isDarkTheme = isDarkTheme,
                user = user,
                settingsUiState = settingsUiState,
                signIn = settingsViewModel::signIn,
                signOut = settingsViewModel::signOut,
                createAccount = settingsViewModel::createAccount,
                deleteAccount = settingsViewModel::deleteUser,
                updateHandle = settingsViewModel::updateHandle,
                navigateToFriends = navigateToFriends
            )
        }
        item {
            CardsCard(
                isDarkTheme = isDarkTheme,
                userUIState = user,
                navigateToCollection = navigateToCollection,
                updateLocale = appViewModel::updateLocale,
                updateCards = appViewModel::updateCardsIfAvailable,
                setTaboo = settingsViewModel::setTaboo,
            )
        }
        item {
            SettingsCard(
                isDarkTheme = isDarkTheme,
                themeInt = themeInt ?: 2,
                englishResults = englishResults,
                language = user.language,
                onSelectTheme = settingsViewModel::selectTheme,
                onSetEnglishSearchResults = settingsViewModel::setEnglishSearchResultsSetting,
            )
        }
        item {
            SocialsCard(
                isDarkTheme = isDarkTheme,
                language = user.language
            )
        }
        item {
            SupportCard(
                isDarkTheme = isDarkTheme,
                language = user.language,
                navigateToAbout = navigateToAbout,
                navigateToDiagnostics = navigateToDiagnostics
            )
        }
    }
}