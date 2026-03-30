package com.rangerscards.ui.campaigns

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.data.local.campaign.CampaignListItemProjection
import com.rangerscards.ui.campaigns.components.CampaignListItem
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.flow.drop
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen(
    navigateToCampaign: (String) -> Unit,
    campaignsViewModel: CampaignsViewModel,
    settingsViewModel: SettingsViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val user by settingsViewModel.userUiState.collectAsState()
    val isRefreshing by campaignsViewModel.isRefreshing.collectAsState()
    val refreshState = rememberPullToRefreshState()
    var userId by rememberSaveable { mutableStateOf("") }
    val campaignsLazyItems = campaignsViewModel.searchResults.collectAsLazyPagingItems()
    val context = LocalContext.current.applicationContext
    LaunchedEffect(user.currentUser) {
        if (userId != user.currentUser?.uid.toString()) {
            campaignsViewModel.getAllNetworkCampaigns(user.currentUser, context)
            userId = user.currentUser?.uid.toString()
        }
    }

    val searchQuery by campaignsViewModel.searchQuery.collectAsState()
    // Remember a LazyListState to control and observe scroll position.
    val listState = rememberLazyListState()
    val activity = LocalActivity.current
    // Whenever the search query changes, scroll the list back to the top.
    LaunchedEffect(Unit) {
        snapshotFlow { searchQuery }
            .drop(1)
            .collect {
                // Scroll to the first item
                listState.animateScrollToItem(0)
            }
    }
    BackHandler {
        activity?.finish()
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
        RangersSearchOutlinedField(
            query = searchQuery,
            placeholder = R.string.search_campaigns,
            onQueryChanged = campaignsViewModel::onSearchQueryChanged,
            onClearClicked = campaignsViewModel::clearSearchQuery
        )
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = refreshState,
            onRefresh = { campaignsViewModel.getAllNetworkCampaigns(user.currentUser, context) },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    containerColor = CustomTheme.colors.d10,
                    color = CustomTheme.colors.l30,
                    state = refreshState
                )
            }
        ) {
            LazyColumn(
                modifier = modifier
                    .background(CustomTheme.colors.l30)
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (campaignsLazyItems.itemCount == 0 && campaignsLazyItems.loadState.isIdle) item {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty())
                                stringResource(R.string.no_campaigns)
                            else stringResource(id = R.string.no_matching_campaigns, searchQuery),
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
                    count = campaignsLazyItems.itemCount,
                    key = campaignsLazyItems.itemKey(CampaignListItemProjection::id),
                    contentType = campaignsLazyItems.itemContentType { it }
                ) { index ->
                    val item = campaignsLazyItems[index] ?: return@items
                    val roleImages = campaignsViewModel.getRolesImages(
                        item.latestDecks.jsonObject.values.mapNotNull { jsonArray ->
                            jsonArray.jsonArray.getOrNull(1)?.jsonObject?.get("role")?.jsonPrimitive?.content
                        }
                    ).collectAsState(null)
                    CampaignListItem(
                        cycleId = item.cycleId,
                        name = item.name,
                        day = item.day,
                        currentLocation = item.currentLocation,
                        rolesImages = roleImages.value ?: emptyList(),
                        access = item.access,
                        onClick = { navigateToCampaign.invoke(item.id) },
                        isDarkTheme = isDarkTheme
                    )
                }

                // Handle load states: initial load and pagination load errors/loading.
                campaignsLazyItems.apply {
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
}