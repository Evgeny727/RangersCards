package com.rangerscards.ui.campaign

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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.RoleCard
import com.rangerscards.domain.model.UserInfo
import com.rangerscards.ui.components.RangersLoadingDialog
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.decks.components.DeckListItem
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import com.rangerscards.domain.model.DeckListItem as DeckListItemModel

@Composable
fun AddDeckToCampaignScreen(
    navigateBack: () -> Unit,
    campaign: Campaign,
    campaignUiState: CampaignUiState,
    addDeck: (String) -> Unit,
    getRole: (String) -> Flow<RoleCard>,
    userInfo: UserInfo?,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    campaignDecksViewModel: CampaignDecksViewModel = hiltViewModel(),
) {
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
    LaunchedEffect(campaign.uploaded) {
        campaignDecksViewModel.setUploaded(campaign.uploaded)
    }
    LaunchedEffect(userInfo?.id) {
        campaignDecksViewModel.setUserId(userInfo?.id ?: "")
    }

    if (campaignUiState is CampaignUiState.Loading) RangersLoadingDialog(isDarkTheme = isDarkTheme)

    Column(
        modifier = Modifier
            .background(CustomTheme.colors.l20)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
    ) {
        RangersSearchOutlinedField(
            query = searchQuery,
            placeholder = if (campaign.uploaded) R.string.search_uploaded_decks else R.string.search_local_decks,
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
                        text = if (searchQuery.isEmpty()) stringResource(
                            if (campaign.uploaded) R.string.no_uploaded_decks_for_add
                            else R.string.no_local_decks_for_add
                        ) else stringResource(id = R.string.no_matching_decks, searchQuery),
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
                val role by getRole(item.meta.roleId).collectAsState(null)
                DeckListItem(
                    meta = item.meta,
                    imageSrc = role?.realImageSrc,
                    name = item.name,
                    roleName = role?.name,
                    onClick = { addDeck(item.id); navigateBack() },
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