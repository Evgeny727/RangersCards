package com.rangerscards.ui.decks

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.AppViewModel
import com.rangerscards.R
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.decks.components.DeckListItem
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import kotlinx.coroutines.flow.drop
import com.rangerscards.domain.model.DeckListItem as DeckListItemModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecksScreen(
    navigateToDeck: (String) -> Unit,
    decksViewModel: DecksViewModel,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val user by appViewModel.userUiState.collectAsState()
    val decksUiState by decksViewModel.decksUiState.collectAsState()
    val refreshState = rememberPullToRefreshState()
    var userId by rememberSaveable { mutableStateOf<String?>(null) }
    val decksLazyItems = decksViewModel.searchResults.collectAsLazyPagingItems()

    LaunchedEffect(user.userInfo?.id) {
        if (userId != user.userInfo?.id) {
            decksViewModel.getAllNetworkDecks(user.userInfo?.id)
            userId = user.userInfo?.id
        }
    }
    LaunchedEffect(Unit) {
        decksViewModel.events.collect {
            appViewModel.emitError(it.exception)
        }
    }

    val searchQuery by decksViewModel.searchQuery.collectAsState()
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
            .applyScaffoldPaddings(contentPadding),
    ) {
        RangersSearchOutlinedField(
            query = searchQuery,
            placeholder = R.string.search_decks,
            onQueryChanged = decksViewModel::onSearchQueryChanged,
            onClearClicked = decksViewModel::clearSearchQuery
        )
        PullToRefreshBox(
            isRefreshing = decksUiState is DecksUiState.Loading,
            state = refreshState,
            onRefresh = { decksViewModel.getAllNetworkDecks(user.userInfo?.id) },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = decksUiState is DecksUiState.Loading,
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
                contentPadding = PaddingValues(vertical = 8.dp),
                state = listState
            ) {
                if (decksLazyItems.itemCount == 0 && decksLazyItems.loadState.isIdle) item("no_results") {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty())
                                stringResource(R.string.no_decks)
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
                    key = decksLazyItems.itemKey(DeckListItemModel::id),
                    contentType = decksLazyItems.itemContentType { it }
                ) { index ->
                    val item = decksLazyItems[index] ?: return@items
                    val role by decksViewModel.getRoleCard(
                        item.meta.roleId,
                        user.settings.taboo
                    ).collectAsState(null)
                    DeckListItem(
                        meta = item.meta,
                        imageSrc = role?.realImageSrc,
                        name = item.name,
                        roleName = role?.name,
                        onClick = { navigateToDeck(item.id) },
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
}