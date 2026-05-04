package com.rangerscards.ui.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.ui.cards.components.CardListItem
import com.rangerscards.ui.cards.components.CardsHeaderType
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.components.RowTypeDivider
import com.rangerscards.ui.components.ScrollableRangersTabs
import com.rangerscards.ui.deck.components.DeckCardListUiModel
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import kotlinx.coroutines.flow.drop

@Composable
fun DeckCardsSearchingListScreen(
    navigateUp: () -> Unit,
    deckViewModel: DeckViewModel,
    deckCardsViewModel: DeckCardsViewModel,
    isDarkTheme: Boolean,
    navigateToCard: (String) -> Unit,
    navigateToSort: () -> Unit,
    navigateToFilters: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val values by deckViewModel.updatableValues.collectAsState()
    val deck by deckViewModel.deck.collectAsState()
    val showAll by deckCardsViewModel.showAllSpoilers.collectAsState()
    val filterOptions by deckCardsViewModel.filterOptions.collectAsState()
    val typeIndex by deckCardsViewModel.typeIndex.collectAsState()
    val cardsLazyItems = deckCardsViewModel.searchResultsWithHeaders.collectAsLazyPagingItems()
    // Remember a LazyListState to control and observe scroll position.
    var restored by remember { mutableStateOf(false) }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = deckCardsViewModel.scrollIndex,
        initialFirstVisibleItemScrollOffset = deckCardsViewModel.scrollOffset
    )
    LaunchedEffect(cardsLazyItems.loadState.refresh) {
        if (!restored && cardsLazyItems.loadState.refresh is LoadState.NotLoading) {
            listState.animateScrollToItem(deckCardsViewModel.scrollIndex, deckCardsViewModel.scrollOffset)
            restored = true
        }
    }
    LaunchedEffect(listState, restored) {
        if (!restored) return@LaunchedEffect

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            deckCardsViewModel.scrollIndex = index
            deckCardsViewModel.scrollOffset = offset
        }
    }

    // Whenever the search query changes, scroll the list back to the top.
    LaunchedEffect(Unit) {
        snapshotFlow { filterOptions.searchQuery to typeIndex }
            .drop(1)
            .collect {
                // Scroll to the first item
                listState.animateScrollToItem(0)
            }
    }

    LaunchedEffect(values?.sideSlots) {
        deckCardsViewModel.updateDeckInfo(deck!!, values!!.sideSlots.keys.toList())
    }

    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = modifier.applyScaffoldPaddings(contentPadding),
        topBar = {
            RangersTopAppBar(
                title = stringResource(R.string.editing_deck_screen_header),
                canNavigateBack = true,
                navigateUp = navigateUp,
                actions = {
                    IconButton(
                        onClick = navigateToSort,
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.sort_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = navigateToFilters,
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.filter_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                switch = null
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .background(CustomTheme.colors.l20)
                .fillMaxSize()
                .applyScaffoldPaddings(innerPadding),
        ) {
            ScrollableRangersTabs(
                if (deck?.previousDeck != null) listOf(
                    R.string.rewards_search_tab,
                    R.string.maladies_search_tab,
                    R.string.collection_search_tab,
                    R.string.displaced_search_tab
                ) else listOf(
                    R.string.personality,
                    R.string.background,
                    R.string.specialty,
                    R.string.outside_interest
                ),
                typeIndex,
                deckCardsViewModel::onTypeIndexChanged
            )
            RangersSearchOutlinedField(
                query = filterOptions.searchQuery,
                R.string.search_for_card,
                onQueryChanged = deckCardsViewModel::onSearchQueryChanged,
                onClearClicked = deckCardsViewModel::clearSearchQuery
            )
            LazyColumn(
                modifier = modifier
                    .background(CustomTheme.colors.l30)
                    .fillMaxSize(),
                state = listState
            ) {
                if (cardsLazyItems.itemCount == 0 && cardsLazyItems.loadState.isIdle) item {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
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
                item {
                    if (deck?.previousDeck == null) Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(when(typeIndex) {
                                0 -> R.string.personality_text
                                1 -> R.string.background_text
                                2 -> R.string.specialty_text
                                else -> R.string.outside_interest_text
                            }),
                            color = CustomTheme.colors.d10,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    } else if (typeIndex == 0) Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        RangersRadioButtonRow(
                            text = stringResource(R.string.show_all_button),
                            onValueChange = deckCardsViewModel::updateShowAllSpoilers,
                            modifier = Modifier,
                            isSelected = showAll
                        )
                    } else if (typeIndex == 2) Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.collection_text),
                            color = CustomTheme.colors.d10,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
                items(
                    count = cardsLazyItems.itemCount,
                    key = cardsLazyItems.itemKey { item ->
                        when (item) {
                            is DeckCardListUiModel.CardItem -> item.card.id
                            is DeckCardListUiModel.CategoryHeader -> item.key
                        }
                    },
                    contentType = cardsLazyItems.itemContentType { when (it) {
                        is DeckCardListUiModel.CardItem -> it.card
                        is DeckCardListUiModel.CategoryHeader -> it.category
                    } }
                ) { index ->
                    val item = cardsLazyItems[index] ?: return@items
                    when (item) {
                        is DeckCardListUiModel.CategoryHeader -> RowTypeDivider(text = when(item.category) {
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
                        is DeckCardListUiModel.CardItem -> {
                            val amount = values?.slots?.get(item.card.code) ?: 0
                            CardListItem(
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
                                currentAmount = amount,
                                onRemoveClick = {
                                    deckViewModel.removeCard(item.card.code, item.card.setId)
                                },
                                onRemoveEnabled = amount > 0,
                                onAddClick = { deckViewModel.addCard(item.card.code) },
                                onAddEnabled = amount != item.card.deckLimit,
                                onClick = { navigateToCard(item.card.id) }
                            )
                        }
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
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = CustomTheme.colors.m
                                    )
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
                                        color = CustomTheme.colors.m
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}