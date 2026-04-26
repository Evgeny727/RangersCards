package com.rangerscards.ui.campaigns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.R
import com.rangerscards.data.local.deck.DeckListItemProjection
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.decks.components.DeckListItem
import com.rangerscards.ui.settings.components.RangersBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun AddDeckToCampaignScreen(
    navController: NavHostController,
    campaignViewModel: CampaignViewModel,
    campaignDecksViewModel: CampaignDecksViewModel,
    user: FirebaseUser?,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val campaign = campaignViewModel.campaign.collectAsState()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()
    val decksLazyItems = campaignDecksViewModel.searchResults.collectAsLazyPagingItems()

    val searchQuery by campaignDecksViewModel.searchQuery.collectAsState()
    // Remember a LazyListState to control and observe scroll position.
    val listState = rememberLazyListState()
    // Whenever the search query changes, scroll the list back to the top.
    LaunchedEffect(Unit) {
        snapshotFlow { searchQuery }
            .drop(1)
            .collect {
                // Scroll to the first item
                listState.animateScrollToItem(0)
            }
    }

    LaunchedEffect(campaign) {
        campaignDecksViewModel.setUploaded(campaign.value?.uploaded ?: false)
    }

    Column(
        modifier = Modifier
            .background(CustomTheme.colors.l20)
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
            RangersBaseCard(
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
        RangersSearchOutlinedField(
            query = searchQuery,
            placeholder = R.string.search_decks,
            onQueryChanged = campaignDecksViewModel::onSearchQueryChanged,
            onClearClicked = campaignDecksViewModel::clearSearchQuery
        )
        LazyColumn(
            modifier = Modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            state = listState
        ) {
            if (decksLazyItems.itemCount == 0 && decksLazyItems.loadState.isIdle) item {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = if (searchQuery.isEmpty())
                            stringResource(R.string.no_decks_for_add)
                        else stringResource(id = R.string.no_matching_decks, searchQuery),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.2.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(
                count = decksLazyItems.itemCount,
                key = decksLazyItems.itemKey(DeckListItemProjection::id),
                contentType = decksLazyItems.itemContentType { it }
            ) { index ->
                val item = decksLazyItems[index] ?: return@items
                val role by campaignViewModel.getRole(
                    item.meta.jsonObject["role"]?.jsonPrimitive?.content.toString()
                ).collectAsState(null)
                DeckListItem(
                    meta = item.meta,
                    imageSrc = role?.realImageSrc,
                    name = item.name,
                    roleName = role?.name,
                    onClick = { coroutine.launch { showLoadingDialog = true
                        campaignViewModel.addDeckCampaign(item.id, user)
                    }.invokeOnCompletion { showLoadingDialog = false
                        navController.navigateUp() } },
                    isCampaign = if (item.campaignName != null) true else null,
                    campaignName = item.campaignName,
                )
            }

            // Handle load states: initial load and pagination load errors/loading.
            decksLazyItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = CustomTheme.colors.m)
                            }
                        }
                    }

                    loadState.append is LoadState.Loading -> {
                        item {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = CustomTheme.colors.m)
                            }
                        }
                    }
                }
            }
        }
    }
}