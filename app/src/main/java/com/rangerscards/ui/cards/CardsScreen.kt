package com.rangerscards.ui.cards

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.domain.model.User
import com.rangerscards.ui.cards.components.CardListItem
import com.rangerscards.ui.cards.components.CardListUiModel
import com.rangerscards.ui.cards.components.CardsHeaderType
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.components.RowTypeDivider
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import kotlinx.coroutines.flow.drop

@Composable
fun CardsScreen(
    isDarkTheme: Boolean,
    userUIState: User,
    navigateToCard: (String) -> Unit,
    modifier: Modifier = Modifier,
    cardsViewModel: CardsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val filterOptions by cardsViewModel.filterOptions.collectAsState()
    val spoiler by cardsViewModel.spoiler.collectAsState()
    val cardsLazyItems = cardsViewModel.searchResultsWithHeaders.collectAsLazyPagingItems()

    var restored by remember { mutableStateOf(false) }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = cardsViewModel.scrollIndex,
        initialFirstVisibleItemScrollOffset = cardsViewModel.scrollOffset
    )
    LaunchedEffect(cardsLazyItems.loadState.refresh) {
        if (!restored && cardsLazyItems.loadState.refresh is LoadState.NotLoading) {
            listState.animateScrollToItem(cardsViewModel.scrollIndex, cardsViewModel.scrollOffset)
            restored = true
        }
    }
    LaunchedEffect(listState, restored) {
        if (!restored) return@LaunchedEffect

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            cardsViewModel.scrollIndex = index
            cardsViewModel.scrollOffset = offset
        }
    }

    val activity = LocalActivity.current

    // Whenever the search query changes, scroll the list back to the top.
    LaunchedEffect(Unit) {
        snapshotFlow { filterOptions.searchQuery to spoiler }
            .drop(1)
            .collect {
                // Scroll to the first item
                listState.animateScrollToItem(0)
            }
    }
    LaunchedEffect(userUIState.settings) {
        val settings = userUIState.settings
        cardsViewModel.setTabooId(settings.taboo)
        cardsViewModel.setPackIds(settings.collection)
    }

    BackHandler {
        activity?.finish()
    }

    // Search TextField: user enters the search query.
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l20)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
    ) {
        RangersSearchOutlinedField(
            query = filterOptions.searchQuery,
            R.string.search_for_card,
            onQueryChanged = cardsViewModel::onSearchQueryChanged,
            onClearClicked = cardsViewModel::clearSearchQuery
        )
        LazyColumn(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize(),
            state = listState
        ) {
            if (cardsLazyItems.itemCount == 0 && cardsLazyItems.loadState.isIdle) item("no_results") {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Text(
                        text = if (filterOptions.searchQuery.isEmpty())
                            stringResource(R.string.no_matching_cards_filtered)
                            else stringResource(id = R.string.no_matching_cards, filterOptions.searchQuery),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.2.sp,
                    )
                }
            }
            items(
                count = cardsLazyItems.itemCount,
                key = cardsLazyItems.itemKey { item ->
                    when (item) {
                        is CardListUiModel.CardItem -> item.card.id
                        is CardListUiModel.CategoryHeader -> item.key
                    }
                },
                contentType = cardsLazyItems.itemContentType { when (it) {
                    is CardListUiModel.CardItem -> it.card
                    is CardListUiModel.CategoryHeader -> it.category
                } }
            ) { index ->
                val item = cardsLazyItems[index] ?: return@items
                when (item) {
                    is CardListUiModel.CategoryHeader -> RowTypeDivider(text = when(item.category) {
                        CardsHeaderType.SET_ID -> item.value.toString()
                        CardsHeaderType.EQUIP -> "${stringResource(R.string.equip_card_divider_header)}: ${
                            item.value ?: stringResource(R.string.text_none)
                        }"
                        CardsHeaderType.TYPE_NAME -> item.value.toString()
                        CardsHeaderType.COST -> "${stringResource(R.string.cost_filter_header)}: ${
                            when (item.value) {
                                null -> stringResource(R.string.text_none)
                                "-2" -> "X"
                                else -> item.value
                            }
                        }"
                        CardsHeaderType.ASPECT_ID -> "${stringResource(R.string.aspect_card_divider_header)}: ${
                            item.value ?: stringResource(R.string.text_none)
                        }"
                        else -> ""
                    })
                    is CardListUiModel.CardItem -> CardListItem(
                        tabooId = item.card.tabooId,
                        aspect = item.card.aspect,
                        cost = item.card.cost,
                        imageSrc = item.card.realImageSrc,
                        approaches = item.card.approaches,
                        name = item.card.name.toString(),
                        typeName = item.card.typeName,
                        traits = item.card.traits,
                        level = item.card.level,
                        isDarkTheme = isDarkTheme,
                        onClick = { navigateToCard(item.card.id) }
                    )
                }
            }

            // Handle load states: initial load and pagination load errors/loading.
            cardsLazyItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp).fillMaxWidth()
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
                                modifier = Modifier.padding(16.dp).fillMaxWidth()
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