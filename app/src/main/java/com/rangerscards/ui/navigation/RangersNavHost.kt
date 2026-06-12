package com.rangerscards.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rangerscards.AppViewModel
import com.rangerscards.R
import com.rangerscards.domain.exceptions.DeckContainsErrorsException
import com.rangerscards.domain.exceptions.DeckContainsUpgradesException
import com.rangerscards.domain.exceptions.DeckInCampaignException
import com.rangerscards.domain.exceptions.HandleAlreadyTakenException
import com.rangerscards.domain.exceptions.InvalidEmailException
import com.rangerscards.domain.exceptions.InvalidHandleSizeException
import com.rangerscards.domain.exceptions.InvalidPasswordException
import com.rangerscards.domain.exceptions.NoSuchCampaignEventException
import com.rangerscards.domain.exceptions.NoSuchCampaignMissionException
import com.rangerscards.domain.exceptions.NoSuchCampaignNoteException
import com.rangerscards.domain.exceptions.NotAvailableWhileInEitModeException
import com.rangerscards.domain.exceptions.UploadingCampaignWithDecksException
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.ui.CardsSyncState
import com.rangerscards.ui.campaign.AddDeckToCampaignScreen
import com.rangerscards.ui.campaign.AddPlayersToCampaign
import com.rangerscards.ui.campaign.CampaignChallengeDeckScreen
import com.rangerscards.ui.campaign.CampaignJourneyScreen
import com.rangerscards.ui.campaign.CampaignRewardFullScreen
import com.rangerscards.ui.campaign.CampaignScreen
import com.rangerscards.ui.campaign.CampaignViewModel
import com.rangerscards.ui.campaign.dialogs.AddMissionDialog
import com.rangerscards.ui.campaign.dialogs.AddNoteDialog
import com.rangerscards.ui.campaign.dialogs.AddRemovedDialog
import com.rangerscards.ui.campaign.dialogs.CampaignEventDialog
import com.rangerscards.ui.campaign.dialogs.CampaignExpansionsDialog
import com.rangerscards.ui.campaign.dialogs.CampaignMissionDialog
import com.rangerscards.ui.campaign.dialogs.CampaignNoteDialog
import com.rangerscards.ui.campaign.dialogs.DayInfoDialog
import com.rangerscards.ui.campaign.dialogs.EndTheDayDialog
import com.rangerscards.ui.campaign.dialogs.RecordEventDialog
import com.rangerscards.ui.campaign.dialogs.TravelDialog
import com.rangerscards.ui.campaign.dialogs.UndoTravelDialog
import com.rangerscards.ui.campaigns.CampaignCreationScreen
import com.rangerscards.ui.campaigns.CampaignsScreen
import com.rangerscards.ui.campaigns.CampaignsViewModel
import com.rangerscards.ui.cards.CardsScreen
import com.rangerscards.ui.cards.CardsViewModel
import com.rangerscards.ui.cards.FullCardScreen
import com.rangerscards.ui.cards.components.RangersSpoilerSwitch
import com.rangerscards.ui.components.CardsFilterScreen
import com.rangerscards.ui.components.CardsSortScreen
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.deck.DeckCardsSearchingListScreen
import com.rangerscards.ui.deck.DeckCardsViewModel
import com.rangerscards.ui.deck.DeckChangingRole
import com.rangerscards.ui.deck.DeckChartsScreen
import com.rangerscards.ui.deck.DeckFullCardScreen
import com.rangerscards.ui.deck.DeckFullCardWithPagerScreen
import com.rangerscards.ui.deck.DeckMulliganScreen
import com.rangerscards.ui.deck.DeckMulliganViewModel
import com.rangerscards.ui.deck.DeckScreen
import com.rangerscards.ui.deck.DeckVersionsScreen
import com.rangerscards.ui.deck.DeckViewModel
import com.rangerscards.ui.decks.DeckCreationScreen
import com.rangerscards.ui.decks.DecksScreen
import com.rangerscards.ui.decks.DecksViewModel
import com.rangerscards.ui.settings.SettingsAboutScreen
import com.rangerscards.ui.settings.SettingsCollectionScreen
import com.rangerscards.ui.settings.SettingsDiagnosticsScreen
import com.rangerscards.ui.settings.SettingsFriendsScreen
import com.rangerscards.ui.settings.SettingsScreen
import com.rangerscards.ui.settings.SettingsUiState
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun RangersNavHost(
    isDarkTheme: Boolean,
    appViewModel: AppViewModel
) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(BottomNavScreen.Cards, BottomNavScreen.Decks,
        BottomNavScreen.Campaigns, BottomNavScreen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBars = remember(currentRoute) {
        currentRoute?.let { route ->
            // Hide the topBar and bottomBar when in the full-screen flow.
            !route.startsWith("deck/") && !route.contains("Options")
        } ?: true
    }
    val resources = LocalResources.current
    var title by rememberSaveable { mutableStateOf(resources.getString(BottomNavScreen.Settings.label)) }
    var actions: @Composable (RowScope.() -> Unit)? by remember { mutableStateOf(null) }
    var switch: @Composable (RowScope.() -> Unit)? = null
    val cardsState by appViewModel.cardsSyncState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            AnimatedVisibility(showBars) {
                RangersTopAppBar(
                    title = title,
                    canNavigateBack = bottomNavItems.none { it.startDestination == currentRoute },
                    navigateUp = { navController.navigateUp() },
                    actions = actions,
                    switch = switch
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(showBars) {
                RangersNavigationBar(navController, bottomNavItems, currentRoute)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { data ->
            Snackbar(
                data,
                containerColor = CustomTheme.colors.d30,
                contentColor = CustomTheme.colors.l30
            )
        } },
        containerColor = CustomTheme.colors.l30
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            appViewModel.checkIfCardsReady()
            appViewModel.events.collect { error ->
                val message = when (error.exception) {
                    is InvalidEmailException -> resources.getString(R.string.invalid_email_text)
                    is InvalidPasswordException -> resources.getString(R.string.invalid_password_text)
                    is HandleAlreadyTakenException -> resources.getString(R.string.handle_already_taken_text)
                    is InvalidHandleSizeException -> resources.getString(R.string.invalid_handle_text)
                    is UploadingCampaignWithDecksException -> resources.getString(R.string.upload_campaign_warning)
                    is NotAvailableWhileInEitModeException -> resources.getString(R.string.not_available_in_edit_mode)
                    is DeckContainsErrorsException -> resources.getString(R.string.campaign_section_camp_warning)
                    is DeckContainsUpgradesException -> resources.getString(R.string.options_section_upload_deck_warning)
                    is DeckInCampaignException -> resources.getString(R.string.options_section_upload_deck_in_campaign_warning)
                    else -> error.exception.localizedMessage ?:
                    resources.getString(R.string.something_went_wrong)
                }
                snackbarHostState.showSnackbar(message)
            }
        }
        if (cardsState is CardsSyncState.UpdateAvailable) RangersDialogWithContent(
            headerId = R.string.cards_update_available_header,
            isDarkTheme = isDarkTheme,
            onBack = appViewModel::cancelCardsUpdate
        ) {
            Text(
                text = stringResource(id = R.string.cards_update_available_text),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            SquareButton(
                stringId = R.string.cancel_button,
                leadingIcon = R.drawable.close_32dp,
                onClick = appViewModel::cancelCardsUpdate,
                buttonColor = ButtonDefaults.buttonColors()
                    .copy(CustomTheme.colors.d30),
                iconColor = CustomTheme.colors.warn,
                textColor = CustomTheme.colors.l30
            )
            SquareButton(
                stringId = R.string.update_cards_button,
                leadingIcon = R.drawable.done_32dp,
                onClick = appViewModel::confirmCardsUpdate
            )
        }
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Decks.route,
            enterTransition = {
                if (initialState.destination.parent == targetState.destination.parent) {
                    fadeIn(
                        animationSpec = tween(300, easing = LinearEasing)
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Up
                    )
                } else {
                    EnterTransition.None
                }
            },
            exitTransition = {
                if (initialState.destination.parent == targetState.destination.parent) {
                    fadeOut(
                        animationSpec = tween(400, easing = LinearEasing)
                    ) + slideOutOfContainer(
                        animationSpec = tween(400, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.Down
                    )
                } else {
                    ExitTransition.None
                }
            }
        ) {
            navigation(
                startDestination = BottomNavScreen.Settings.startDestination,
                route = BottomNavScreen.Settings.route
            ) {
                composable(BottomNavScreen.Settings.startDestination,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) {
                    if (cardsState !is CardsSyncState.Loading) {
                        SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            navigateToAbout = {
                                navController.navigate(
                                    "${BottomNavScreen.Settings.route}/about"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            navigateToDiagnostics = {
                                navController.navigate(
                                    "${BottomNavScreen.Settings.route}/diagnostics"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            navigateToFriends = {
                                navController.navigate(
                                    "${BottomNavScreen.Settings.route}/friends"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            navigateToCollection = {
                                navController.navigate(
                                    "${BottomNavScreen.Settings.route}/collection"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            appViewModel = appViewModel,
                            contentPadding = innerPadding
                        )
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Settings.label)
                    actions = null
                    switch = null
                }
                composable(BottomNavScreen.Settings.route + "/about") {
                    SettingsAboutScreen(contentPadding = innerPadding)
                    title = stringResource(R.string.about_button)
                    actions = null
                    switch = null
                }
                composable(BottomNavScreen.Settings.route + "/friends") {
                    SettingsFriendsScreen(
                        appViewModel = appViewModel,
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.your_friends)
                    actions = null
                    switch = null
                }
                composable(BottomNavScreen.Settings.route + "/diagnostics") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(BottomNavScreen.Settings.startDestination)
                    }
                    val settingsViewModel = hiltViewModel<SettingsViewModel>(parentEntry)
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
                    LaunchedEffect(Unit) {
                        settingsViewModel.settingsUiState.collect { state ->
                            if (state is SettingsUiState.Success) snackbarHostState.showSnackbar(state.message)
                        }
                    }
                    SettingsDiagnosticsScreen(
                        clearLocalData = settingsViewModel::clearLocalData,
                        clearImageCache = settingsViewModel::clearImageCache,
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.diagnostics_button)
                    actions = null
                    switch = null
                }
                composable(BottomNavScreen.Settings.route + "/collection") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(BottomNavScreen.Settings.startDestination)
                    }
                    val settingsViewModel = hiltViewModel<SettingsViewModel>(parentEntry)
                    val state by settingsViewModel.settingsUiState.collectAsState()
                    val user by appViewModel.userUiState.collectAsState()
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
                    SettingsCollectionScreen(
                        user = user,
                        setCollection = settingsViewModel::setCollection,
                        isLoading = state is SettingsUiState.Loading,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.collection_header)
                    actions = null
                    switch = null
                }
            }
            navigation(
                startDestination = BottomNavScreen.Cards.startDestination,
                route = BottomNavScreen.Cards.route
            ) {
                composable(BottomNavScreen.Cards.startDestination,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) {

                    val user by appViewModel.userUiState.collectAsState()
                    val cardsViewModel: CardsViewModel = hiltViewModel()
                    val spoiler by cardsViewModel.spoiler.collectAsState()

                    if (cardsState !is CardsSyncState.Loading) {
                        CardsScreen(
                            isDarkTheme = isDarkTheme,
                            userUIState = user,
                            cardsViewModel = cardsViewModel,
                            contentPadding = innerPadding,
                            navigateToCard = { cardId ->
                                navController.navigate(
                                    "${BottomNavScreen.Cards.route}/card/$cardId"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Cards.label)
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    "${BottomNavScreen.Cards.route}/sortOptions"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                            modifier = Modifier.size(32.dp),
                            enabled = cardsState !is CardsSyncState.Loading
                        ) {
                            Icon(
                                painterResource(id = R.drawable.sort_32dp),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    "${BottomNavScreen.Cards.route}/filterOptions"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                            modifier = Modifier.size(32.dp),
                            enabled = cardsState !is CardsSyncState.Loading
                        ) {
                            Icon(
                                painterResource(id = R.drawable.filter_32dp),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    switch = {
                        RangersSpoilerSwitch(spoiler, cardsViewModel::onSpoilerChanged)
                    }
                }
                val cardIdArgument = "cardId"
                composable(
                    route = BottomNavScreen.Cards.route + "/card/{$cardIdArgument}",
                    arguments = listOf(navArgument(cardIdArgument) { type = NavType.StringType })
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(BottomNavScreen.Cards.startDestination)
                    }
                    val cardsViewModel: CardsViewModel = hiltViewModel(parentEntry)
                    val cardId = backStackEntry.arguments?.getString(cardIdArgument)
                    if (cardId != null) {
                        FullCardScreen(
                            cardsViewModel = cardsViewModel,
                            cardId = cardId,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding
                        )
                        title = ""
                        actions = null
                        switch = null
                    } else {
                        appViewModel.emitError(IllegalStateException("cardIdArgument cannot be null"))
                        navController.navigateUp()
                    }
                }
                composable(route = BottomNavScreen.Cards.route + "/sortOptions") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(BottomNavScreen.Cards.startDestination)
                    }
                    val cardsViewModel: CardsViewModel = hiltViewModel(parentEntry)
                    val filterOptions by cardsViewModel.filterOptions.collectAsState()
                    CardsSortScreen(
                        navigateUp = navController::navigateUp,
                        clearSortOptions = { cardsViewModel.clearSortOptions()
                            navController.navigateUp() },
                        sortOptions = filterOptions.sortOrder,
                        onApply = { newSortOptions ->
                            cardsViewModel.applyNewSortOptions(newSortOptions)
                            navController.navigateUp() },
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                }
                composable(route = BottomNavScreen.Cards.route + "/filterOptions") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(BottomNavScreen.Cards.startDestination)
                    }
                    val cardsViewModel: CardsViewModel = hiltViewModel(parentEntry)
                    val filterOptions by cardsViewModel.filterOptions.collectAsState()
                    CardsFilterScreen(
                        navigateUp = navController::navigateUp,
                        clearFilterOptions = { cardsViewModel.clearFilterOptions()
                            navController.navigateUp() },
                        filterOptions = filterOptions,
                        onApply = { newFilterOptions ->
                            cardsViewModel.applyNewFilterOptions(newFilterOptions)
                            navController.navigateUp() },
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                }
            }
            navigation(
                startDestination = BottomNavScreen.Decks.startDestination,
                route = BottomNavScreen.Decks.route
            ) {
                composable(BottomNavScreen.Decks.startDestination,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) { backStackEntry ->
                    val decksViewModel: DecksViewModel = hiltViewModel(backStackEntry)
                    if (cardsState !is CardsSyncState.Loading) {
                        DecksScreen(
                            navigateToDeck = { deckId ->
                                navController.navigate(
                                    "deck/$deckId"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            decksViewModel = decksViewModel,
                            appViewModel = appViewModel,
                            contentPadding = innerPadding
                        )
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Decks.label)
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    "${BottomNavScreen.Decks.route}/creation"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                            modifier = Modifier.size(32.dp),
                            enabled = cardsState !is CardsSyncState.Loading
                        ) {
                            Icon(
                                painterResource(id = R.drawable.add_32dp),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    switch = null
                }
                composable(BottomNavScreen.Decks.route + "/creation") {
                    val user by appViewModel.userUiState.collectAsState()

                    DeckCreationScreen(
                        onCancel = navController::navigateUp,
                        onCreate = { deckId ->
                            navController.navigate(
                                "deck/$deckId"
                            ) {
                                popUpTo(BottomNavScreen.Decks.startDestination) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        emitError = appViewModel::emitError,
                        user = user,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.new_deck)
                    actions = null
                    switch = null
                }
            }
            val deckIdArgument = "deckId"
            navigation(
                startDestination = "deck/{$deckIdArgument}",
                route = "deck",
            ) {
                composable(
                    route = "deck/{$deckIdArgument}",
                    enterTransition = {
                        if (!initialState.destination.route.orEmpty().startsWith("deck/")) {
                            fadeIn(
                                animationSpec = tween(300, easing = LinearEasing)
                            ) + slideIntoContainer(
                                animationSpec = tween(300, easing = EaseIn),
                                towards = AnimatedContentTransitionScope.SlideDirection.Up
                            )
                        } else {
                            EnterTransition.None
                        }
                    },
                    exitTransition = {
                        if (!targetState.destination.route.orEmpty().startsWith("deck/")) {
                            fadeOut(
                                animationSpec = tween(400, easing = LinearEasing)
                            ) + slideOutOfContainer(
                                animationSpec = tween(400, easing = EaseOut),
                                towards = AnimatedContentTransitionScope.SlideDirection.Down
                            )
                        } else {
                            ExitTransition.None
                        }
                    },
                    arguments = listOf(navArgument(deckIdArgument) { type = NavType.StringType })
                ) { backStackEntry ->
                    val deckViewModel: DeckViewModel = hiltViewModel(backStackEntry)
                    val deckId = backStackEntry.arguments?.getString(deckIdArgument)
                    val user by appViewModel.userUiState.collectAsState()
                    if (deckId != null) {
                        DeckScreen(
                            emitError = appViewModel::emitError,
                            showMessage = snackbarHostState::showSnackbar,
                            navController = navController,
                            deckViewModel = deckViewModel,
                            userInfo = user.userInfo,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding
                        )
                        title = ""
                        actions = null
                        switch = null
                    } else {
                        appViewModel.emitError(IllegalStateException("deckIdArgument cannot be null"))
                        navController.navigateUp()
                    }
                }
                composable("deck/roleChanging") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/{$deckIdArgument}")
                    }
                    val deckViewModel: DeckViewModel = hiltViewModel(parentEntry)
                    val deck by deckViewModel.deck.collectAsState()
                    if (deck != null) {
                        val user by appViewModel.userUiState.collectAsState()
                        DeckChangingRole(
                            onBack = navController::navigateUp,
                            deckViewModel = deckViewModel,
                            deck = deck!!,
                            userSettings = user.settings,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding
                        )
                        title = ""
                        actions = null
                        switch = null
                    } else navController.navigateUp()
                }
                val cardIdArgument = "cardId"
                composable(
                    route = "deck/card/{$cardIdArgument}",
                    arguments = listOf(navArgument(cardIdArgument) { type = NavType.StringType })
                ) { backStackEntry ->
                    val cardCode = backStackEntry.arguments?.getString(cardIdArgument)
                    if (cardCode != null) {
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("deck/{$deckIdArgument}")
                        }
                        val deckViewModel: DeckViewModel = hiltViewModel(parentEntry)
                        DeckFullCardScreen(
                            navigateUp = navController::navigateUp,
                            deckViewModel = deckViewModel,
                            cardCode = cardCode,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding,
                        )
                    } else {
                        appViewModel.emitError(IllegalStateException("cardIdArgument cannot be null"))
                        navController.navigateUp()
                    }
                }
                composable(route = "deck/charts") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/{$deckIdArgument}")
                    }
                    val deckViewModel: DeckViewModel = hiltViewModel(parentEntry)
                    DeckChartsScreen(
                        navigateUp = navController::navigateUp,
                        deckViewModel = deckViewModel,
                        contentPadding = innerPadding,
                    )
                }
                composable(route = "deck/mulligan") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/{$deckIdArgument}")
                    }
                    val deckViewModel: DeckViewModel = hiltViewModel(parentEntry)
                    val deckMulliganViewModel: DeckMulliganViewModel = hiltViewModel(backStackEntry)
                    DeckMulliganScreen(
                        navigateUp = navController::navigateUp,
                        navigateToCard = { cardId ->
                            navController.navigate("deck/card/$cardId") { launchSingleTop = true }
                        },
                        deckViewModel = deckViewModel,
                        deckMulliganViewModel = deckMulliganViewModel,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding,
                    )
                }
                composable(
                    route = "deck/{$deckIdArgument}/history",
                    arguments = listOf(navArgument(deckIdArgument) { type = NavType.StringType })
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/{$deckIdArgument}")
                    }
                    DeckVersionsScreen(
                        navigateUp = navController::navigateUp,
                        navigateToDeck = { deckId -> navController.navigate("deck/$deckId") {
                            popUpTo(parentEntry.destination.route.orEmpty()) { inclusive = true }
                            launchSingleTop = true
                        } },
                        contentPadding = innerPadding,
                    )
                }
                val typeIndexArgument = "typeIndexArgument"
                composable(route = "deck/cardsList/{$typeIndexArgument}",
                    arguments = listOf(navArgument(typeIndexArgument) { type = NavType.IntType }),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) { backStackEntry ->
                    val typeIndex = backStackEntry.arguments?.getInt(typeIndexArgument)
                    if (typeIndex != null) {
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("deck/{$deckIdArgument}")
                        }
                        val deckViewModel: DeckViewModel = hiltViewModel(parentEntry)
                        val deck by deckViewModel.deck.collectAsState()
                        if (deck != null) {
                            val deckCardsViewModel: DeckCardsViewModel = hiltViewModel(backStackEntry)
                            val user by appViewModel.userUiState.collectAsState()
                            LaunchedEffect(Unit) {
                                deckCardsViewModel.setPackIds(user.settings.collection)
                            }
                            DeckCardsSearchingListScreen(
                                navigateUp = navController::navigateUp,
                                deckViewModel = deckViewModel,
                                deckCardsViewModel = deckCardsViewModel,
                                isDarkTheme = isDarkTheme,
                                navigateToCard = { cardId ->
                                    navController.navigate(
                                        "deck/cardsList/{$typeIndexArgument}/card/$cardId"
                                    ) { launchSingleTop = true }
                                },
                                navigateToSort = {
                                    navController.navigate("deck/cardsList/{$typeIndexArgument}/sortOptions") {
                                        launchSingleTop = true
                                    }
                                },
                                navigateToFilters = {
                                    navController.navigate("deck/cardsList/{$typeIndexArgument}/filterOptions") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        } else navController.navigateUp()
                    } else {
                        appViewModel.emitError(IllegalStateException("typeIndexArgument cannot be null"))
                        navController.navigateUp()
                    }
                }
                composable(
                    route = "deck/cardsList/{$typeIndexArgument}/card/{$cardIdArgument}",
                    arguments = listOf(navArgument(cardIdArgument) { type = NavType.StringType })
                ) { backStackEntry ->
                    val cardId = backStackEntry.arguments?.getString(cardIdArgument)
                    if (cardId != null) {
                        val parentGraphEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("deck/{$deckIdArgument}")
                        }
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("deck/cardsList/{$typeIndexArgument}")
                        }
                        val deckViewModel: DeckViewModel = hiltViewModel(parentGraphEntry)
                        val deck by deckViewModel.deck.collectAsState()
                        if (deck != null) {
                            val deckCardsViewModel: DeckCardsViewModel = hiltViewModel(parentEntry)
                            DeckFullCardWithPagerScreen(
                                navigateUp = navController::navigateUp,
                                deckViewModel = deckViewModel,
                                deckCardsViewModel = deckCardsViewModel,
                                cardId = cardId,
                                isDarkTheme = isDarkTheme,
                                contentPadding = innerPadding,
                            )
                        } else navController.navigateUp()
                    } else {
                        appViewModel.emitError(IllegalStateException("cardIndexArgument cannot be null"))
                        navController.navigateUp()
                    }
                }
                composable(route = "deck/cardsList/{$typeIndexArgument}/sortOptions") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/cardsList/{$typeIndexArgument}")
                    }
                    val deckCardsViewModel: DeckCardsViewModel = hiltViewModel(parentEntry)
                    val filterOptions by deckCardsViewModel.filterOptions.collectAsState()
                    CardsSortScreen(
                        navigateUp = navController::navigateUp,
                        clearSortOptions = { deckCardsViewModel.clearSortOptions()
                            navController.navigateUp() },
                        sortOptions = filterOptions.sortOrder,
                        onApply = { newSortOptions ->
                            deckCardsViewModel.applyNewSortOptions(newSortOptions)
                            navController.navigateUp() },
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                }
                composable(route = "deck/cardsList/{$typeIndexArgument}/filterOptions") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/cardsList/{$typeIndexArgument}")
                    }
                    val deckCardsViewModel: DeckCardsViewModel = hiltViewModel(parentEntry)
                    val filterOptions by deckCardsViewModel.filterOptions.collectAsState()
                    CardsFilterScreen(
                        navigateUp = navController::navigateUp,
                        clearFilterOptions = { deckCardsViewModel.clearFilterOptions()
                            navController.navigateUp() },
                        filterOptions = filterOptions,
                        onApply = { newFilterOptions ->
                            deckCardsViewModel.applyNewFilterOptions(newFilterOptions)
                            navController.navigateUp() },
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                }
            }
            navigation(
                startDestination = BottomNavScreen.Campaigns.startDestination,
                route = BottomNavScreen.Campaigns.route
            ) {
                composable(BottomNavScreen.Campaigns.startDestination,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) { backStackEntry ->
                    val campaignsViewModel: CampaignsViewModel = hiltViewModel(backStackEntry)
                    if (cardsState !is CardsSyncState.Loading) {
                        CampaignsScreen(
                            navigateToCampaign = { campaignId ->
                                navController.navigate(
                                    "${BottomNavScreen.Campaigns.route}/campaign/$campaignId"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            campaignsViewModel = campaignsViewModel,
                            appViewModel = appViewModel,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding
                        )
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Campaigns.label)
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    "${BottomNavScreen.Campaigns.route}/creation"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                            modifier = Modifier.size(32.dp),
                            enabled = cardsState !is CardsSyncState.Loading
                        ) {
                            Icon(
                                painterResource(id = R.drawable.add_32dp),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    switch = null
                }
                composable(BottomNavScreen.Campaigns.route + "/creation") {
                    val user by appViewModel.userUiState.collectAsState()
                    CampaignCreationScreen(
                        onCancel = navController::navigateUp,
                        onCreate = { campaignId ->
                            navController.navigate(
                                "${BottomNavScreen.Campaigns.route}/campaign/$campaignId"
                            ) {
                                popUpTo(BottomNavScreen.Campaigns.startDestination) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        emitError = appViewModel::emitError,
                        userInfo = user.userInfo,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.new_campaign)
                    actions = null
                    switch = null
                }
                val campaignIdArgument = "campaignId"
                composable(
                    route = "${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}",
                    enterTransition = {
                        if (initialState.destination.route == BottomNavScreen.Campaigns.startDestination) {
                            fadeIn(
                                animationSpec = tween(300, easing = LinearEasing)
                            ) + slideIntoContainer(
                                animationSpec = tween(300, easing = EaseIn),
                                towards = AnimatedContentTransitionScope.SlideDirection.Up
                            )
                        } else {
                            EnterTransition.None
                        }
                    },
                    exitTransition = {
                        if (targetState.destination.route == BottomNavScreen.Campaigns.startDestination) {
                            fadeOut(
                                animationSpec = tween(400, easing = LinearEasing)
                            ) + slideOutOfContainer(
                                animationSpec = tween(400, easing = EaseOut),
                                towards = AnimatedContentTransitionScope.SlideDirection.Down
                            )
                        } else {
                            ExitTransition.None
                        }
                    },
                    arguments = listOf(navArgument(campaignIdArgument) { type = NavType.StringType }))
                    { backStackEntry ->
                        val campaignViewModel: CampaignViewModel = hiltViewModel(backStackEntry)
                        val user by appViewModel.userUiState.collectAsState()
                        val campaign by campaignViewModel.campaign.collectAsState()
                        val isViewOnly by campaignViewModel.isViewOnly.collectAsState()
                        if (cardsState !is CardsSyncState.Loading) {
                            CampaignScreen(
                                emitError = appViewModel::emitError,
                                campaignViewModel = campaignViewModel,
                                campaign = campaign,
                                user = user,
                                isDarkTheme = isDarkTheme,
                                isViewOnly = isViewOnly,
                                navController = navController,
                                contentPadding = innerPadding
                            )
                        } else {
                            CardsDownloadingCircularProgressIndicator()
                        }
                        title = if (campaign != null) stringResource(CampaignMaps.campaignCyclesMap[campaign!!.cycleId] ?: R.string.core_cycle)
                        else ""
                        actions = if (!isViewOnly) { {
                            IconButton(
                                onClick = {
                                    navController.navigate(
                                        "${BottomNavScreen.Campaigns.route}/campaign/undo"
                                    ) {
                                        launchSingleTop = true
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                                modifier = Modifier.size(32.dp),
                                enabled = cardsState !is CardsSyncState.Loading
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.undo_32dp),
                                    contentDescription = null,
                                    tint = CustomTheme.colors.m,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            //TODO:Add navigation to campaign guide screen
                        } } else null
                        switch = null
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/expansions") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val campaign by campaignViewModel.campaign.collectAsState()
                    if (campaign != null) {
                        CampaignExpansionsDialog(
                            campaign = campaign!!,
                            updateCampaignExpansions = campaignViewModel::updateCampaignExpansions,
                            isDarkTheme = isDarkTheme,
                            onBack = navController::navigateUp
                        )
                    } else navController.navigateUp()
                }
                val dayInfoIdArgument = "dayInfoId"
                dialog("${BottomNavScreen.Campaigns.route}/campaign/dayInfo/{$dayInfoIdArgument}",
                    arguments = listOf(navArgument(dayInfoIdArgument) { type = NavType.IntType }))
                { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val dayInfoId = backStackEntry.arguments?.getInt(dayInfoIdArgument)
                    if (dayInfoId != null) {
                        val campaign by campaignViewModel.campaign.collectAsState()
                        val isViewOnly by campaignViewModel.isViewOnly.collectAsState()
                        if (campaign != null) {
                            DayInfoDialog(
                                campaign = campaign!!,
                                groupDaysByWeather = campaignViewModel::groupDaysByWeather,
                                setCampaignCalendar = campaignViewModel::setCampaignCalendar,
                                dayId = dayInfoId,
                                isDarkTheme = isDarkTheme,
                                isViewOnly = isViewOnly,
                                onBack = navController::navigateUp,
                            )
                        } else navController.navigateUp()
                    } else {
                        appViewModel.emitError(IllegalStateException("dayInfoId cannot be null"))
                        navController.navigateUp()
                    }
                }
                composable(route = "${BottomNavScreen.Campaigns.route}/campaign/journey") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val campaign by campaignViewModel.campaign.collectAsState()
                    if (campaign != null) {
                        CampaignJourneyScreen(
                            campaign = campaign!!,
                            buildTravelHistory = campaignViewModel::buildTravelHistory,
                            getWeatherByDay = campaignViewModel::getWeatherByDay,
                            contentPadding = innerPadding
                        )
                        title = stringResource(R.string.journey_title)
                        actions = null
                        switch = null
                    } else navController.navigateUp()
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/endDay") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val campaign by campaignViewModel.campaign.collectAsState()
                    if (campaign != null) {
                        EndTheDayDialog(
                            campaign = campaign!!,
                            setCampaignDay = campaignViewModel::setCampaignDay,
                            isDarkTheme = isDarkTheme,
                            onBack = navController::navigateUp,
                        )
                    } else navController.navigateUp()
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/travel") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val campaign by campaignViewModel.campaign.collectAsState()
                    if (campaign != null) {
                        TravelDialog(
                            campaign = campaign!!,
                            campaignTravel = campaignViewModel::campaignTravel,
                            isDarkTheme = isDarkTheme,
                            onBack = navController::navigateUp,
                        )
                    } else navController.navigateUp()
                }
                composable(
                    route = "${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}/challengeDeck",
                    arguments = listOf(navArgument(campaignIdArgument) { type = NavType.StringType })
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val currentChallengeDeck by campaignViewModel.currentChallengeDeck.collectAsState()
                    if (currentChallengeDeck != null) {
                        CampaignChallengeDeckScreen(
                            challengeDeck = currentChallengeDeck!!,
                            discardScoutedCards = campaignViewModel::discardScoutedCards,
                            returnChallengeCardsInAnyOrder = campaignViewModel::returnChallengeCardsInAnyOrder,
                            reshuffleChallengeDeck = campaignViewModel::reshuffleChallengeDeck,
                            drawChallengeCard = campaignViewModel::drawChallengeCard,
                            scoutChallengeCard = campaignViewModel::scoutChallengeCard,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding
                        )
                        title = stringResource(R.string.challenge_deck_title)
                        actions = null
                        switch = null
                    } else navController.navigateUp()
                }
                composable(route = "${BottomNavScreen.Campaigns.route}/campaign/addRanger") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val campaign by campaignViewModel.campaign.collectAsState()
                    if (campaign != null) {
                        val campaignUiState by campaignViewModel.campaignUiState.collectAsState()
                        val user by appViewModel.userUiState.collectAsState()
                        AddDeckToCampaignScreen(
                            navigateBack = navController::navigateUp,
                            campaign = campaign!!,
                            campaignUiState = campaignUiState,
                            addDeck = campaignViewModel::addDeckCampaign,
                            getRole = campaignViewModel::getRole,
                            userInfo = user.userInfo,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding,
                        )
                        title = stringResource(R.string.add_ranger_button)
                        actions = null
                        switch = null
                    } else navController.navigateUp()
                }
                composable(route = "${BottomNavScreen.Campaigns.route}/campaign/addPlayer") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val campaign by campaignViewModel.campaign.collectAsState()
                    if (campaign != null) {
                        val campaignUiState by campaignViewModel.campaignUiState.collectAsState()
                        val user by appViewModel.userUiState.collectAsState()
                        AddPlayersToCampaign(
                            campaign = campaign!!,
                            campaignUiState = campaignUiState,
                            addFriend = campaignViewModel::addFriendToCampaign,
                            removeFriend = campaignViewModel::removeFriendFromCampaign,
                            user = user,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding,
                        )
                        title = stringResource(R.string.your_friends)
                        actions = null
                        switch = null
                    } else navController.navigateUp()
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/undo") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    UndoTravelDialog(
                        checkIfCanUndo = campaignViewModel::checkIfCanUndo,
                        undoTravel = campaignViewModel::undoTravel,
                        isDarkTheme = isDarkTheme,
                        onBack = navController::navigateUp,
                    )
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/removeCard") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    AddRemovedDialog(
                        campaignViewModel = campaignViewModel,
                        isDarkTheme = isDarkTheme,
                        onBack = navController::navigateUp,
                    )
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/recordEvent") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val campaign by campaignViewModel.campaign.collectAsState()
                    if (campaign != null) {
                        RecordEventDialog(
                            recordCampaignEvent = campaignViewModel::recordCampaignEvent,
                            isDarkTheme = isDarkTheme,
                            onBack = navController::navigateUp,
                        )
                    } else navController.navigateUp()
                }
                val eventNameArgument = "eventNameArgument"
                dialog("${BottomNavScreen.Campaigns.route}/campaign/event/{$eventNameArgument}",
                    arguments = listOf(navArgument(eventNameArgument) { type = NavType.StringType }))
                { backStackEntry ->
                    val eventName = backStackEntry.arguments?.getString(eventNameArgument)
                    if (eventName != null) {
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                        }
                        val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                        val campaign by campaignViewModel.campaign.collectAsState()
                        val event = campaign?.events?.firstOrNull { it.name == eventName }
                        if (event != null) CampaignEventDialog(
                            event = event,
                            updateCampaignEvents = campaignViewModel::updateCampaignEvents,
                            isDarkTheme = isDarkTheme,
                            onBack = navController::navigateUp,
                        ) else {
                            appViewModel.emitError(NoSuchCampaignEventException())
                            navController.navigateUp()
                        }
                    } else {
                        appViewModel.emitError(IllegalStateException("eventNameArgument cannot be null"))
                        navController.navigateUp()
                    }
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/addNote") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val campaign by campaignViewModel.campaign.collectAsState()
                    if (campaign != null) {
                        AddNoteDialog(
                            addCampaignNote = campaignViewModel::addCampaignNote,
                            currentDay = campaign!!.currentDay,
                            isDarkTheme = isDarkTheme,
                            onBack = navController::navigateUp,
                        )
                    } else navController.navigateUp()
                }
                val noteIndexArgument = "noteIndexArgument"
                dialog("${BottomNavScreen.Campaigns.route}/campaign/note/{$noteIndexArgument}",
                    arguments = listOf(navArgument(noteIndexArgument) { type = NavType.IntType }))
                { backStackEntry ->
                    val noteIndex = backStackEntry.arguments?.getInt(noteIndexArgument)
                    if (noteIndex != null) {
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                        }
                        val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                        val campaign by campaignViewModel.campaign.collectAsState()
                        val note = campaign?.notes[noteIndex]
                        if (note != null) CampaignNoteDialog(
                            index = noteIndex,
                            note = note,
                            updateCampaignNotes = campaignViewModel::updateCampaignNotes,
                            isDarkTheme = isDarkTheme,
                            onBack = navController::navigateUp,
                        ) else {
                            appViewModel.emitError(NoSuchCampaignNoteException())
                            navController.navigateUp()
                        }
                    } else {
                        appViewModel.emitError(IllegalStateException("noteIndexArgument cannot be null"))
                        navController.navigateUp()
                    }
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/addMission") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val campaign by campaignViewModel.campaign.collectAsState()
                    if (campaign != null) {
                        AddMissionDialog(
                            addCampaignMission = campaignViewModel::addCampaignMission,
                            currentDay = campaign!!.currentDay,
                            isDarkTheme = isDarkTheme,
                            onBack = navController::navigateUp,
                        )
                    } else navController.navigateUp()
                }
                val missionNameArgument = "missionNameArgument"
                dialog("${BottomNavScreen.Campaigns.route}/campaign/mission/{$missionNameArgument}",
                    arguments = listOf(navArgument(missionNameArgument) { type = NavType.StringType })
                ) { backStackEntry ->
                    val missionName = backStackEntry.arguments?.getString(missionNameArgument)
                    if (missionName != null) {
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                        }
                        val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                        val campaign by campaignViewModel.campaign.collectAsState()
                        val mission = campaign?.missions?.firstOrNull { it.name == missionName }
                        if (mission != null) CampaignMissionDialog(
                            campaignMission = mission,
                            deleteCampaignMission = campaignViewModel::deleteCampaignMission,
                            setCampaignMissions = campaignViewModel::setCampaignMissions,
                            isDarkTheme = isDarkTheme,
                            onBack = navController::navigateUp,
                        ) else {
                            appViewModel.emitError(NoSuchCampaignMissionException())
                            navController.navigateUp()
                        }
                    } else {
                        appViewModel.emitError(IllegalStateException("missionNameArgument cannot be null"))
                        navController.navigateUp()
                    }
                }
                val cardIndexArgument = "cardIndex"
                composable(
                    route = "${BottomNavScreen.Campaigns.route}/campaign/reward/{$cardIndexArgument}",
                    arguments = listOf(navArgument(cardIndexArgument) { type = NavType.IntType })
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = hiltViewModel(parentEntry)
                    val cardIndex = backStackEntry.arguments?.getInt(cardIndexArgument)
                    if (cardIndex != null) {
                        CampaignRewardFullScreen(
                            campaignViewModel = campaignViewModel,
                            cardIndex = cardIndex,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding,
                        )
                        actions = null
                    } else {
                        appViewModel.emitError(IllegalStateException("cardIndexArgument cannot be null"))
                        navController.navigateUp()
                    }
                }
            }
        }
    }
}

@Composable
fun CardsDownloadingCircularProgressIndicator() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CustomTheme.colors.l30
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.cards_updating),
                color = CustomTheme.colors.d30,
                style = CustomTheme.typography.headline
            )
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = CustomTheme.colors.m)
        }
    }
}