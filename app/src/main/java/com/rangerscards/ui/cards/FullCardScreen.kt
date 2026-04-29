package com.rangerscards.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.rangerscards.ui.cards.components.FullCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.utils.applyScaffoldPaddings

@Composable
fun FullCardScreen(
    cardsViewModel: CardsViewModel,
    cardIndex: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val cardsLazyItems = cardsViewModel.searchResults.collectAsLazyPagingItems()
    val pagerState = rememberPagerState(initialPage = cardIndex) { cardsLazyItems.itemCount }
    HorizontalPager(
        state = pagerState,
        key = { page -> cardsLazyItems[page]?.id ?: page },
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize(),
    ) { page ->
        val item = cardsLazyItems[page] ?: return@HorizontalPager
        val fullCard by cardsViewModel.getCardById(item.code).collectAsState(null)
        Column (
            modifier = Modifier.fillMaxSize()
                .applyScaffoldPaddings(contentPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (fullCard == null) Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp).fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = CustomTheme.colors.m)
            } else FullCard(
                tabooId = fullCard!!.tabooId,
                aspect = fullCard?.aspect,
                cost = fullCard!!.cost,
                image = fullCard!!.image,
                name = fullCard!!.name,
                presence = fullCard!!.presence,
                approaches = fullCard!!.approaches,
                type = fullCard!!.type,
                traits = fullCard!!.traits,
                equip = fullCard!!.equip,
                harm = fullCard!!.harm,
                progress = fullCard!!.progress,
                tokens = fullCard!!.tokens,
                text = fullCard!!.text,
                flavor = fullCard!!.flavor,
                level = fullCard!!.level,
                set = fullCard!!.set,
                subset = fullCard!!.subset,
                packShortName = fullCard!!.packShortName,
                challenges = fullCard!!.challenges,
                isDarkTheme = isDarkTheme
            )
        }
    }
}