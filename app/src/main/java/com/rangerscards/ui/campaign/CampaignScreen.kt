package com.rangerscards.ui.campaign

import android.net.Uri
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rangerscards.R
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.User
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.ui.campaign.components.CampaignCurrentPositionCard
import com.rangerscards.ui.campaign.components.CampaignEvents
import com.rangerscards.ui.campaign.components.CampaignMissions
import com.rangerscards.ui.campaign.components.CampaignNotes
import com.rangerscards.ui.campaign.components.CampaignRemovedCards
import com.rangerscards.ui.campaign.components.CampaignSettingsSection
import com.rangerscards.ui.campaign.components.CampaignTitleRow
import com.rangerscards.ui.campaign.components.TimeLineLazyRow
import com.rangerscards.ui.cards.components.CardListItem
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.RangersLoadingDialog
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.components.ScrollableRangersTabs
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.decks.components.DeckListItem
import com.rangerscards.ui.navigation.BottomNavScreen
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.settings.components.SettingsInputField
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.drop

@Composable
fun CampaignScreen(
    emitError: (Throwable) -> Unit,
    campaignViewModel: CampaignViewModel,
    campaign: Campaign?,
    user: User,
    isDarkTheme: Boolean,
    navController: NavHostController,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val campaignUiState by campaignViewModel.campaignUiState.collectAsState()
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var campaignNameEditing by rememberSaveable { mutableStateOf("") }
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    val isOwner = remember(campaign?.userId, user.userInfo) {
        campaign?.userId == user.userInfo?.id || campaign?.userId?.isEmpty() == true
    }
    var isCampaignLogExpanded by rememberSaveable { mutableStateOf(false) }
    var campaignLogTypeIndex by rememberSaveable { mutableIntStateOf(0) }
    var isCampaignMissionsOnlyActive by rememberSaveable { mutableStateOf(false) }
    // one state + connection per tab:
    val innerStates = List(5) { rememberLazyListState() }
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
    val rewardsQuery by campaignViewModel.rewardsQuery.collectAsState()
    val isShowAllRewards by campaignViewModel.showAllRewards.collectAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { rewardsQuery }
            .drop(1)
            .collect {
                // Scroll to the first item
                innerStates[1].animateScrollToItem(0)
            }
    }
    LaunchedEffect(user.settings, campaign?.cycleId) {
        campaignViewModel.setUserSettings(user.settings)
        campaignViewModel.setPackId(campaign?.cycleId ?: "core")
    }
    LaunchedEffect(Unit) {
        campaignViewModel.events.collect {
            emitError(it.exception)
        }
    }
    LaunchedEffect(campaignUiState) {
        when (val state = campaignUiState) {
            CampaignUiState.Deleted -> navController.navigateUp()
            is CampaignUiState.FriendDeckDownloaded -> navController.navigate(
                "deck/${state.deckId}"
            ) { launchSingleTop = true }
            is CampaignUiState.CampaignUploaded -> navController.navigate(
                "${BottomNavScreen.Campaigns.route}/campaign/${state.campaignId}"
            ) {
                popUpTo(BottomNavScreen.Campaigns.startDestination) { inclusive = false }
                launchSingleTop = true
            }
            else -> Unit
        }
    }

    if (campaignUiState is CampaignUiState.Loading) RangersLoadingDialog(isDarkTheme = isDarkTheme)

    if (showNameDialog) RangersDialogWithContent(
        headerId = R.string.name_label,
        isDarkTheme = isDarkTheme,
        onBack = { showNameDialog = false },
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
                    showNameDialog = false
                    campaignViewModel.updateCampaignName(campaignNameEditing)
                    campaignNameEditing = ""
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
    if (showConfirmationDialog) RangersDialogWithContent(
        headerId = if (isOwner) R.string.delete_campaign_button else R.string.leave_campaign_button,
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
                onClick = {
                    showConfirmationDialog = false
                    campaignViewModel.deleteOrLeaveCampaign(isOwner)
                },
            )
        }
    }

    Column(
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
    ) {
        if (campaign == null) {
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item("campaign_title") {
                    CampaignTitleRow(
                        campaign.name,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) { campaignNameEditing = campaign.name; showNameDialog = true }
                }
                item("timeline") {
                    val groupedDays = remember(
                        campaign.calendar,
                        campaign.expansions,
                        campaign.extendedCalendar
                    ) { campaignViewModel.groupDaysByWeather() }
                    TimeLineLazyRow(
                        groupedDays,
                        campaign.currentDay,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) { navController.navigate(
                        "${BottomNavScreen.Campaigns.route}/campaign/dayInfo/$it"
                        ) { launchSingleTop = true }
                    }
                }
                if (campaign.currentDay >= 30 && !campaign.extendedCalendar) item("extend_button") {
                    SquareButton(
                        stringId = R.string.extend_campaign_button,
                        leadingIcon = R.drawable.add_32dp,
                        onClick = campaignViewModel::extendCampaign,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
                item("current_position") {
                    CampaignCurrentPositionCard(
                        campaign.cycleId,
                        campaign.currentLocation,
                        campaign.currentPathTerrain,
                        campaign.expansions,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) { navController.navigate(
                        "${BottomNavScreen.Campaigns.route}/campaign/journey"
                        ) { launchSingleTop = true }
                    }
                }
                item("travel_section") {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max).padding(bottom = 8.dp),
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
                        if ((campaign.currentDay != 30 || campaign.extendedCalendar)
                            && campaign.currentDay != 60) key("endDayButton") {
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
                item("challengeDeckButton") {
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
                                "${BottomNavScreen.Campaigns.route}/campaign/${campaign.id}/challengeDeck"
                            ) { launchSingleTop = true }
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
                item("campaign_log") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .padding(bottom = 8.dp),
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
                                        R.string.section_notes,
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
                                        missions = remember(campaign.missions, isCampaignMissionsOnlyActive) {
                                            campaign.missions.filter {
                                                mission -> !isCampaignMissionsOnlyActive || !mission.completed
                                            }.distinctBy { it.name }.sortedBy { it.day }.toImmutableList()
                                        },
                                        onClick = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/mission/${Uri.encode(it)}")
                                        {
                                            launchSingleTop = true
                                        } },
                                        isOnlyActive = isCampaignMissionsOnlyActive,
                                        onActiveClick = { value -> isCampaignMissionsOnlyActive = value },
                                        state = innerStates[campaignLogTypeIndex],
                                        modifier = Modifier.nestedScroll(innerConnections[campaignLogTypeIndex]),
                                    )
                                    1 -> {
                                        val innerState = innerStates[campaignLogTypeIndex]
                                        RangersSearchOutlinedField(
                                            query = rewardsQuery,
                                            R.string.search_for_card,
                                            onQueryChanged = campaignViewModel::onRewardsQueryChange,
                                            onClearClicked = campaignViewModel::onRewardsQueryClear,
                                        )
                                        RangersRadioButtonRow(
                                            text = stringResource(R.string.show_all_rewards_in_collection),
                                            onValueChange = campaignViewModel::setShowAllRewards,
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                            isSelected = isShowAllRewards
                                        )
                                        val rewards by campaignViewModel.rewards.collectAsState()
                                        LazyColumn(
                                            state = innerState,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .nestedScroll(innerConnections[campaignLogTypeIndex])
                                        ) {
                                            rewards.forEachIndexed { index, reward ->
                                                val isAdded = campaign.rewards.contains(reward.id)
                                                item(reward.id) {
                                                    CardListItem(
                                                        tabooId = reward.tabooId,
                                                        aspect = reward.aspect,
                                                        cost = reward.cost,
                                                        imageSrc = reward.realImageSrc,
                                                        approaches = reward.approaches,
                                                        name = reward.name.toString(),
                                                        typeName = reward.typeName,
                                                        traits = reward.traits,
                                                        level = reward.level,
                                                        isDarkTheme = isDarkTheme,
                                                        currentAmount = if (isAdded) 2 else 0,
                                                        onRemoveClick = {
                                                            campaignViewModel.removeCampaignReward(reward.id)
                                                        },
                                                        onRemoveEnabled = isAdded,
                                                        onAddClick = {
                                                            campaignViewModel.addCampaignReward(reward.id)
                                                        },
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
                                        events = remember(campaign.events) {
                                            campaign.events.distinctBy { it.name }.sortedBy { it.name }.toImmutableList()
                                        },
                                        onClick = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/event/${Uri.encode(it)}"
                                        ) {
                                            launchSingleTop = true
                                        } },
                                        state = innerStates[campaignLogTypeIndex],
                                        modifier = Modifier.nestedScroll(innerConnections[campaignLogTypeIndex]),
                                    )
                                    3 -> CampaignNotes(
                                        onAdd = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/addNote"
                                        ) {
                                            launchSingleTop = true
                                        } },
                                        notes = campaign.notes.toImmutableList(),
                                        onClick = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/note/$it"
                                        ) {
                                            launchSingleTop = true
                                        } },
                                        state = innerStates[campaignLogTypeIndex],
                                        modifier = Modifier.nestedScroll(innerConnections[campaignLogTypeIndex]),
                                    )
                                    4 -> CampaignRemovedCards(
                                        onAdd = { navController.navigate(
                                            "${BottomNavScreen.Campaigns.route}/campaign/removeCard"
                                        ) {
                                            launchSingleTop = true
                                        } },
                                        removedSets = campaignViewModel.getRemovedSetsInfo(),
                                        removed = remember(campaign.removed) {
                                            campaign.removed.distinctBy { it.name }.toImmutableList()
                                        },
                                        onRemove = { removedName ->
                                            campaignViewModel.updateCampaignRemoved(removedName)
                                        },
                                        state = innerStates[campaignLogTypeIndex],
                                        nestedConnectionModifier = Modifier.nestedScroll(innerConnections[campaignLogTypeIndex]),
                                    )
                                }
                            }
                        }
                    }
                }
                item("rangers_section") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.rangers_section_header),
                            color = CustomTheme.colors.d10,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }
                items(campaign.decks, { deck -> deck.id }) { deck ->
                    val role by campaignViewModel.getRole(deck.meta.roleId).collectAsState(null)
                    DeckListItem(
                        meta = deck.meta,
                        imageSrc = role?.realImageSrc,
                        name = deck.name,
                        roleName = role?.name,
                        onClick = {
                            if (!campaign.uploaded || user.userInfo?.id == deck.user.id)
                                navController.navigate("deck/${deck.id}") { launchSingleTop = true }
                            else campaignViewModel.downloadFriendDeck(deck.id)
                        },
                        isCampaign = false,
                        userName = if (deck.user.name == "null") "" else deck.user.name,
                        onRemoveDeck = if (!campaign.uploaded || user.userInfo?.id == deck.user.id) {
                            { campaignViewModel.removeDeckCampaign(deck.id) }
                        } else null
                    )
                }
                item("add_ranger_button") {
                    SquareButton(
                        stringId = R.string.add_ranger_button,
                        leadingIcon = R.drawable.add_32dp,
                        iconColor = CustomTheme.colors.m,
                        textColor = CustomTheme.colors.d30,
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            containerColor = CustomTheme.colors.l20
                        ),
                        onClick = {
                            navController.navigate(
                            "${BottomNavScreen.Campaigns.route}/campaign/addRanger"
                            ) { launchSingleTop = true }
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
                item("settings_section") {
                    CampaignSettingsSection(
                        onAddOrRemovePlayers = { navController.navigate(
                            "${BottomNavScreen.Campaigns.route}/campaign/addPlayer"
                        ) {
                            launchSingleTop = true
                        } },
                        onUploadCampaign = if (!campaign.uploaded && user.userInfo != null) { {
                            campaignViewModel.uploadCampaign()
                        } } else null,
                        onDeleteOrLeaveCampaign = { showConfirmationDialog = true },
                        onCampaignExpansions = if (CampaignMaps.campaignExpansionsMap[campaign.cycleId]?.isNotEmpty() == true) {
                            { navController.navigate(
                                "${BottomNavScreen.Campaigns.route}/campaign/expansions"
                            ) { launchSingleTop = true } }
                        } else null,
                        isOwner = isOwner,
                        isUploaded = campaign.uploaded
                    )
                }
            }
        }
    }
}