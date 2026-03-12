package com.rangerscards.ui.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun DeckVersionsScreen(
    navigateUp: () -> Unit,
    navigateToDeck: (String) -> Unit,
    deckViewModel: DeckViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = modifier.padding(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding()
        ),
        topBar = {
            RangersTopAppBar(
                title = stringResource(R.string.campaign_section_deck_history),
                canNavigateBack = true,
                navigateUp = navigateUp,
                actions = null,
                switch = null
            )
        },
    ) { innerPadding ->
        val currentDeck by deckViewModel.originalDeck.collectAsState()
        val versions by deckViewModel.deckVersionIds.collectAsState()

        LaunchedEffect(currentDeck?.id) {
            if (currentDeck != null) {
                deckViewModel.getAllDeckVersions(currentDeck!!.id)
            } else navigateUp()
        }
        if (versions.isEmpty()) Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = 8.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = CustomTheme.colors.m
            )
        }
        else LazyColumn(
            modifier = modifier
                .background(CustomTheme.colors.l20)
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
           items(versions, key = { it }) { id ->
               val isCurrentDeck = currentDeck?.id == id

               Button(
                   onClick = { deckViewModel.clearDeckVersions()
                       navigateToDeck(id) },
                   modifier = Modifier.fillMaxWidth(),
                   enabled = !isCurrentDeck,
                   shape = CustomTheme.shapes.small,
                   colors = ButtonDefaults.buttonColors().copy(
                       containerColor = CustomTheme.colors.d10,
                       disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.7F)
                   ),
                   contentPadding = PaddingValues(8.dp),
               ) {
                   Icon(
                       painterResource(R.drawable.cards_32dp),
                       contentDescription = null,
                       tint = CustomTheme.colors.l30,
                       modifier = Modifier.size(24.dp)
                   )
                   Spacer(modifier = Modifier.width(8.dp))
                   Text(
                       text = if (isCurrentDeck) stringResource(R.string.deck_history_current_deck)
                       else stringResource(R.string.deck_section_deck_id, id),
                       modifier = Modifier.fillMaxWidth(),
                       color = CustomTheme.colors.l30,
                       fontFamily = Jost,
                       fontWeight = FontWeight.Medium,
                       fontSize = 18.sp,
                       letterSpacing = 0.1.sp
                   )
               }
           }
        }
    }
}