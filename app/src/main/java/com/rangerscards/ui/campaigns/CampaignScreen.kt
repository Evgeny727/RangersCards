package com.rangerscards.ui.campaigns

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.rangerscards.R
import com.rangerscards.data.local.campaign.Campaign
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.ui.campaigns.components.CampaignCurrentPositionCard
import com.rangerscards.ui.campaigns.components.CampaignDialog
import com.rangerscards.ui.campaigns.components.CampaignEvents
import com.rangerscards.ui.campaigns.components.CampaignMissions
import com.rangerscards.ui.campaigns.components.CampaignRemovedCards
import com.rangerscards.ui.campaigns.components.CampaignSettingsSection
import com.rangerscards.ui.campaigns.components.CampaignTitleRow
import com.rangerscards.ui.campaigns.components.TimeLineLazyRow
import com.rangerscards.ui.cards.components.CardListItem
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.components.ScrollableRangersTabs
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.decks.components.DeckListItem
import com.rangerscards.ui.navigation.BottomNavScreen
import com.rangerscards.ui.settings.UserUIState
import com.rangerscards.ui.settings.components.SettingsBaseCard
import com.rangerscards.ui.settings.components.SettingsInputField
import com.rangerscards.ui.settings.components.SettingsRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray

@Composable
fun CampaignScreen(
    campaignViewModel: CampaignViewModel,
    campaign: Campaign?,
    challengeDeck: JsonElement?,
    userUIState: UserUIState,
    isDarkTheme: Boolean,
    navController: NavHostController,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val isSubscriptionStarted by campaignViewModel.isSubscriptionStarted.collectAsState()
    val campaignState by campaignViewModel.campaign.collectAsState()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var campaignNameEditing by rememberSaveable { mutableStateOf("") }
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    val isOwner by remember { derivedStateOf {
        campaignState!!.userId == userUIState.currentUser?.uid || campaignState!!.userId.isEmpty()
    } }
    var isCampaignLogExpanded by rememberSaveable { mutableStateOf(false) }
    var campaignLogTypeIndex by rememberSaveable { mutableIntStateOf(0) }
    var isCampaignMissionsOnlyActive by rememberSaveable { mutableStateOf(false) }
    // one state + connection per tab:
    val innerStates = List(4) { rememberLazyListState() }
    val innerConnections = innerStates.map { _ ->
        remember {
            object : NestedScrollConnection {
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    // let the inner list scroll first, then eat all leftover
                    return available
                }
                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    return available
                }
            }
        }
    }
    var rewardsQuery by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        snapshotFlow { rewardsQuery }
            .drop(1)
            .collect {
                // Scroll to the first item
                innerStates[1].animateScrollToItem(0)
            }
    }
    LaunchedEffect(campaign, campaign?.id != campaignState?.id) {
        if (userUIState.currentUser != null && !isSubscriptionStarted && campaign?.uploaded == true)
            campaignViewModel.startSubscription(campaign.id)
        if (campaign != null) campaignViewModel.parseCampaign(campaign)
    }
    LaunchedEffect(challengeDeck, campaignViewModel.currentChallengeDeck) {
        if (campaignViewModel.currentChallengeDeck == null && challengeDeck?.jsonArray?.isNotEmpty() ?: true) {
            campaignViewModel.setChallengeDeck(challengeDeck ?: JsonArray(emptyList()))
        }
    }
    LaunchedEffect(userUIState.userInfo, campaignState) {
        val settings = userUIState.settings
        campaignViewModel.setTaboo(settings.taboo)
        campaignViewModel.setPackId(campaignState?.cycleId ?: "core")
        campaignViewModel.setCollection(settings.collection)
    }
    Column(
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
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
        if (showNameDialog) Dialog(
            onDismissRequest = { showNameDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            SettingsBaseCard(
                isDarkTheme = isDarkTheme,
                labelIdRes = R.string.deck_creation_name_label
            ) {
                SettingsInputField(
                    leadingIcon = R.drawable.badge_32dp,
                    placeholder = null,
                    textValue = campaignNameEditing,
                    onValueChange = { campaignNameEditing = it },
                    KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SquareButton(
                        stringId = R.string.cancel_button,
                        leadingIcon = R.drawable.close_32dp,
                        onClick = { showNameDialog = false
                            campaignNameEditing = ""
                        },
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            CustomTheme.colors.d30,
                            disabledContainerColor = CustomTheme.colors.m
                        ),
                        iconColor = CustomTheme.colors.warn,
                        textColor = CustomTheme.colors.l30,
                        modifier = Modifier.weight(0.5f),
                    )
                    SquareButton(
                        stringId = R.string.done_button,
                        leadingIcon = R.drawable.done_32dp,
                        onClick = {
                            if (campaignNameEditing != campaignState!!.name) coroutine.launch {
                                showNameDialog = false
                                showLoadingDialog = true
                                campaignViewModel.updateCampaignName(
                                    campaignState!!.id,
                                    campaignNameEditing,
                                    campaignState!!.uploaded,
                                    userUIState.currentUser
                                )
                            }.invokeOnCompletion {
                                campaignNameEditing = ""
                                showLoadingDialog = false
                            } else {
                                campaignNameEditing = ""
                                showNameDialog = false
                            }
                        },
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            CustomTheme.colors.d10,
                            disabledContainerColor = CustomTheme.colors.m
                        ),
                        iconColor = CustomTheme.colors.l15,
                        textColor = CustomTheme.colors.l30,
                        modifier = Modifier.weight(0.5f),
                    )
                }
            }
        }
        if (showConfirmationDialog) CampaignDialog(
            header = stringResource(id = R.string.delete_campaign_button),
            isDarkTheme = isDarkTheme,
            onBack = { showConfirmationDialog = false }
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(if (isOwner) R.string.delete_campaign_confirmation
                    else R.string.leave_campaign_confirmation),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                SquareButton(
                    stringId = R.string.cancel_button,
                    leadingIcon = R.drawable.close_32dp,
                    iconColor = CustomTheme.colors.warn,
                    buttonColor = ButtonDefaults.buttonColors().copy(
                        containerColor = CustomTheme.colors.d30
                    ),
                    onClick = { showConfirmationDialog = false },
                )
                SquareButton(
                    stringId = if (isOwner) R.string.delete_campaign_button else R.string.leave_campaign_button,
                    leadingIcon = R.drawable.delete_32dp,
                    iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    buttonColor = ButtonDefaults.buttonColors().copy(
                        containerColor = CustomTheme.colors.warn
                    ),
                    onClick = if (isOwner) { { coroutine.launch {
                        showLoadingDialog = true
                        showConfirmationDialog = false
                        campaignViewModel.deleteCampaign(userUIState.currentUser)
                    }.invokeOnCompletion {
                        showLoadingDialog = false
                        navController.navigateUp()
                    } } } else { { coroutine.launch { showLoadingDialog = true
                        showConfirmationDialog = false
                        campaignViewModel.leaveCampaign(userUIState.currentUser)
                    }.invokeOnCompletion { showLoadingDialog = false
                        navController.navigateUp()
                    } } },
                )
            }
        }
        if (campaignState == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = CustomTheme.colors.m
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    CampaignTitleRow(campaignState!!.name) { campaignNameEditing = campaignState?.name ?: ""
                        showNameDialog = true
                    }
                }
                item {
                    TimeLineLazyRow(
                        campaignViewModel.groupDaysByWeather(),
                        campaignState!!.currentDay
                    ) { navController.navigate(
                        "${BottomNavScreen.Campaigns.route}/campaign/dayInfo/$it"
                    ) {
                        launchSingleTop = true
                    } }
                }
                if (campaignState!!.currentDay >= 30 && !campaignState!!.extendedCalendar) item {
                    SquareButton(
                        stringId = R.string.extend_campaign_button,
                        leadingIcon = R.drawable.add_32dp,
                        onClick = { coroutine.launch { campaignViewModel.extendCampaign(userUIState.currentUser) } },
                        modifier = Modifier.padding(8.dp)
                    )
                }
                item {
                    CampaignCurrentPositionCard(
                        campaignState!!.cycleId,
                        campaignState!!.currentLocation,
                        campaignState!!.currentPathTerrain,
                        campaignState!!.expansions
                    ) { navController.navigate(
                        "${BottomNavScreen.Campaigns.route}/campaign/journey"
                    ) {
                        launchSingleTop = true
                    } }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        key("travelButton") {
                            SquareButton(
                                stringId = R.string.travel_button,
                                leadingIcon = R.drawable.travel_32dp,
                                iconColor = CustomTheme.colors.m,
                                textColor = CustomTheme.colors.d30,
                                buttonColor = ButtonDefaults.buttonColors().copy(
                                    containerColor = CustomTheme.colors.l15
                                ),
                                onClick = { navController.navigate(
                                    "${BottomNavScreen.Campaigns.route}/campaign/travel"
                                ) {
                                    launchSingleTop = true
                                } },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                        if ((campaignState!!.currentDay != 30 || campaignState!!.extendedCalendar)
                            && campaignState!!.currentDay != 60) key("endDayButton") {
                            SquareButton(
                                stringId = R.string.end_the_day,
                                leadingIcon = R.drawable.camp_32dp,
                                iconColor = CustomTheme.colors.d20,
                                textColor = CustomTheme.colors.d30,
                                buttonColor = ButtonDefaults.buttonColors().copy(
                                    containerColor = CustomTheme.colors.l10
                                ),
                                onClick = { navController.navigate(
                                    "${BottomNavScreen.Campaigns.route}/campaign/endDay"
                                ) {
                                    launchSingleTop = true
                                } },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        key("challengeDeckButton") {
                            SquareButton(
                                stringId = R.string.challenge_deck_title,
                                leadingIcon = R.drawable.cards_32dp,
                                iconColor = CustomTheme.colors.m,
                                textColor = CustomTheme.colors.d30,
                                buttonColor = ButtonDefaults.buttonColors().copy(
                                    containerColor = CustomTheme.colors.l20
                                ),
                                onClick = {
                                    navController.navigate(
                                        "${BottomNavScreen.Campaigns.route}/campaign/challengeDeck"
                                    ) {
                                        launchSingleTop = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        border = BorderStroke(1.dp, CustomTheme.colors.d15),
                        color = CustomTheme.colors.l30,
                        shape = CustomTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .background(
                                        CustomTheme.colors.d15,
                                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    )
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                    .clickable { isCampaignLogExpanded = !isCampaignLogExpanded }
                            ) {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                    Text(
                                        text = stringResource(R.string.campaign_log_header),
                                        color = CustomTheme.colors.l30,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 20.sp,
                                        lineHeight = 22.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 28.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Icon(
                                        painterResource(if (isCampaignLogExpanded) R.drawable.arrow_drop_up_32dp
                                        else R.drawable.arrow_drop_down_32dp),
                                        contentDescription = null,
                                        tint = CustomTheme.colors.l10,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            if (isCampaignLogExpanded) Column(modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(maxHeight = 400.dp)) {
                                ScrollableRangersTabs(
                                    listOf(
                                        R.string.missions_campaign_log_tab,
                                        R.string.rewards_search_tab,
                                        R.string.events_campaign_log_tab,
                                        R.string.removed_campaign_log_tab
                                    ),
                                    campaignLogTypeIndex,
                                ) { campaignLogTypeIndex = it }
                                when(campaignLogTypeIndex) {
                                    0 -> CampaignMissions(
                                        onAdd = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/addMission"
                                        ) {
                                            launchSingleTop = true
                                        } },
                                        missions = campaignState!!.missions.distinctBy { it.name },
                                        onClick = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/mission/${Uri.encode(it)}")
                                        {
                                            launchSingleTop = true
                                        } },
                                        isOnlyActive = isCampaignMissionsOnlyActive,
                                        onActiveClick = { isCampaignMissionsOnlyActive = !isCampaignMissionsOnlyActive },
                                        state = innerStates[campaignLogTypeIndex],
                                        nestedConnectionModifier = Modifier.nestedScroll(innerConnections[campaignLogTypeIndex]),
                                    )
                                    1 -> {
                                        val innerState = innerStates[campaignLogTypeIndex]
                                        val isShowAllRewards by campaignViewModel.showAllRewards.collectAsState()
                                        RangersSearchOutlinedField(
                                            query = rewardsQuery,
                                            R.string.search_for_card,
                                            onQueryChanged = { newQuery -> rewardsQuery = newQuery },
                                            onClearClicked = { rewardsQuery = "" }
                                        )
                                        SettingsRadioButtonRow(
                                            text = stringResource(R.string.show_all_rewards_in_collection),
                                            onClick = campaignViewModel::setShowAllRewards,
                                            modifier = Modifier,
                                            isSelected = isShowAllRewards
                                        )
                                        val rewards = campaignViewModel.getRewardsCards().collectAsState(emptyList())
                                        LazyColumn(
                                            state = innerState,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .nestedScroll(innerConnections[campaignLogTypeIndex])
                                        ) {
                                            rewards.value.filter { it.name!!.contains(rewardsQuery, true) }
                                                .forEachIndexed { index, reward ->
                                                val isAdded = campaignState!!.rewards.contains(reward.id)
                                                item(reward.id) {
                                                    CardListItem(
                                                        tabooId = reward.tabooId,
                                                        aspectId = reward.aspectId,
                                                        aspectShortName = reward.aspectShortName,
                                                        cost = reward.cost,
                                                        imageSrc = reward.realImageSrc,
                                                        approachConflict = reward.approachConflict,
                                                        approachConnection = reward.approachConnection,
                                                        approachReason = reward.approachReason,
                                                        approachExploration = reward.approachExploration,
                                                        name = reward.name.toString(),
                                                        typeName = reward.typeName,
                                                        traits = reward.traits,
                                                        level = reward.level,
                                                        isDarkTheme = isDarkTheme,
                                                        currentAmount = if (isAdded) 2 else 0,
                                                        onRemoveClick = { coroutine.launch { showLoadingDialog = true
                                                            campaignViewModel.removeCampaignReward(reward.id, userUIState.currentUser)
                                                        }.invokeOnCompletion { showLoadingDialog = false }  },
                                                        onRemoveEnabled = isAdded,
                                                        onAddClick = { coroutine.launch { showLoadingDialog = true
                                                            campaignViewModel.addCampaignReward(reward.id, userUIState.currentUser)
                                                        }.invokeOnCompletion { showLoadingDialog = false }  },
                                                        onAddEnabled = !isAdded,
                                                        onClick = {
                                                            navController.navigate(
                                                                "${BottomNavScreen.Campaigns.route}/campaign/reward/$index"
                                                            ) {
                                                                launchSingleTop = true
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    2 -> CampaignEvents(
                                        onAdd = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/recordEvent"
                                        ) {
                                            launchSingleTop = true
                                        } },
                                        events = campaignState!!.events.distinctBy { it.name }.sortedBy { it.name },
                                        onClick = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/event/${Uri.encode(it)}"
                                        ) {
                                            launchSingleTop = true
                                        } },
                                        state = innerStates[campaignLogTypeIndex],
                                        modifier = Modifier.nestedScroll(innerConnections[campaignLogTypeIndex]),
                                    )
                                    3 -> CampaignRemovedCards(
                                        onAdd = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/removeCard"
                                        ) {
                                            launchSingleTop = true
                                        } },
                                        removedSets = campaignViewModel.getRemovedSetsInfo(),
                                        removed = campaignState!!.removed.distinctBy { it.name },
                                        onRemove = { removedName -> coroutine.launch { showLoadingDialog = true
                                            campaignViewModel.updateCampaignRemoved(removedName, userUIState.currentUser)
                                        }.invokeOnCompletion { showLoadingDialog = false }},
                                        state = innerStates[campaignLogTypeIndex],
                                        nestedConnectionModifier = Modifier.nestedScroll(innerConnections[campaignLogTypeIndex]),
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = stringResource(R.string.rangers_section_header),
                                color = CustomTheme.colors.d10,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                lineHeight = 20.sp,
                            )
                        }
                        if (campaignState!!.decks.isNotEmpty()) campaignState!!.decks.forEach { deck ->
                            val role = campaignViewModel.getRole(deck.role).collectAsState(null).value
                            DeckListItem(
                                meta = deck.meta,
                                imageSrc = role?.realImageSrc,
                                name = deck.name,
                                role = role?.name,
                                onClick = { if (!campaignState!!.uploaded || userUIState.currentUser?.uid == deck.userId)
                                    navController.navigate(
                                        "deck/${deck.id}"
                                    ) {
                                        launchSingleTop = true
                                    }
                                    else coroutine.launch { showLoadingDialog = true
                                        campaignViewModel.downloadFriendDeck(deck.id, userUIState.currentUser)
                                    }.invokeOnCompletion { showLoadingDialog = false
                                        if (campaignViewModel.friendDeckIdToOpen.value != null)
                                            navController.navigate(
                                                "deck/${campaignViewModel.friendDeckIdToOpen.value}"
                                            ) { launchSingleTop = true }
                                        else Toast.makeText(
                                            context,
                                            context.getString(R.string.something_went_wrong),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                } ,
                                isCampaign = false,
                                userName = if (deck.userName == "null") "" else deck.userName,
                                onRemoveDeck = if (!campaignState!!.uploaded || userUIState.currentUser?.uid == deck.userId) {
                                    { coroutine.launch { showLoadingDialog = true
                                        campaignViewModel.removeDeckCampaign(deck.id, userUIState.currentUser)
                                    }.invokeOnCompletion { showLoadingDialog = false } }
                                } else null
                            )
                        }
                        SquareButton(
                            stringId = R.string.add_ranger_button,
                            leadingIcon = R.drawable.add_32dp,
                            iconColor = CustomTheme.colors.m,
                            textColor = CustomTheme.colors.d30,
                            buttonColor = ButtonDefaults.buttonColors().copy(
                                containerColor = CustomTheme.colors.l20
                            ),
                            onClick = { navController.navigate(
                                "${BottomNavScreen.Campaigns.route}/campaign/addRanger"
                            ) {
                                launchSingleTop = true
                            } },
                        )
                    }
                }
                item {
                    CampaignSettingsSection(
                        onAddOrRemovePlayers = { navController.navigate(
                            "${BottomNavScreen.Campaigns.route}/campaign/addPlayer"
                        ) {
                            launchSingleTop = true
                        } },
                        onUploadCampaign = if (!campaignState!!.uploaded && userUIState.currentUser != null)
                        { { if (campaignState!!.decks.isNotEmpty())
                            Toast.makeText(
                                context,
                                context.getString(R.string.upload_campaign_warning),
                                Toast.LENGTH_SHORT,
                            ).show()
                            else coroutine.launch {
                            showLoadingDialog = true
                            campaignViewModel.uploadCampaign(userUIState.currentUser)
                        }.invokeOnCompletion { showLoadingDialog = false
                            if (campaignViewModel.uploadedCampaignIdToOpen.value != null) navController.navigate(
                                "${BottomNavScreen.Campaigns.route}/campaign/${campaignViewModel.uploadedCampaignIdToOpen.value}"
                            ) {
                                popUpTo(BottomNavScreen.Campaigns.startDestination) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            } else navController.navigateUp()
                        } } } else null,
                        onDeleteOrLeaveCampaign = { showConfirmationDialog = true },
                        onCampaignExpansions = if (CampaignMaps.campaignExpansionsMap[campaignState!!.cycleId]?.isNotEmpty() == true) {
                            { navController.navigate(
                                "${BottomNavScreen.Campaigns.route}/campaign/expansions"
                            ) { launchSingleTop = true } }
                        } else null,
                        isOwner = isOwner,
                        isUploaded = campaignState!!.uploaded
                    )
                }
            }
        }
    }
}