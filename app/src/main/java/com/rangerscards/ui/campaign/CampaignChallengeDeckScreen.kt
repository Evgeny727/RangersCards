package com.rangerscards.ui.campaign

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.CurrentChallengeDeck
import com.rangerscards.R
import com.rangerscards.objects.ChallengeDeck
import com.rangerscards.ui.campaign.components.AspectsRowCharts
import com.rangerscards.ui.campaign.components.ChallengeCard
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings

enum class ReturnChallengeCard {
    Bottom, Top
}

@Composable
fun CampaignChallengeDeckScreen(
    challengeDeck: CurrentChallengeDeck,
    discardScoutedCards: () -> Unit,
    returnChallengeCardsInAnyOrder: (List<Int>, List<Int>) -> Unit,
    reshuffleChallengeDeck: () -> Unit,
    drawChallengeCard: () -> Int?,
    scoutChallengeCard: () -> Int?,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val challengeDeckSize by challengeDeck.size.collectAsState()
    val challengeDeckScoutPosition by challengeDeck.scoutPosition.collectAsState()
    val challengeDeckIds by challengeDeck.challengeDeckIdsFlow.collectAsState()
    val revealedCardIds = rememberSaveable(saver = listSaver(
        save = { stateList -> stateList.toList() },
        restore = { restored -> restored.toMutableStateList() }
    )) { mutableStateListOf<Int>() }
    var returnInOrderMode by remember { mutableStateOf<ReturnChallengeCard?>(null) }
    val topList = rememberSaveable(saver = listSaver(
        save = { stateList -> stateList.toList() },
        restore = { restored -> restored.toMutableStateList() }
    )) { mutableStateListOf<Int>() }
    val bottomList = rememberSaveable(saver = listSaver(
        save = { stateList -> stateList.toList() },
        restore = { restored -> restored.toMutableStateList() }
    )) { mutableStateListOf<Int>() }
    val isScoutAvailable = remember { derivedStateOf { (challengeDeckScoutPosition < challengeDeckSize) &&
            returnInOrderMode == null && ((revealedCardIds.isNotEmpty() && challengeDeckScoutPosition != 0) ||
            revealedCardIds.isEmpty())
    } }
    val showRow by remember { derivedStateOf { revealedCardIds.size > 1 } }
    Column(
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding)
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(returnInOrderMode != null) {
            val iconId = "info"
            BasicText(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = buildAnnotatedString {
                    appendInlineContent(iconId, "[$iconId]")
                    append(
                        " ${
                            stringResource(if (returnInOrderMode == ReturnChallengeCard.Bottom)
                                R.string.return_scouted_cards_ordered_bottom_info
                            else R.string.return_scouted_cards_ordered_top_info)
                        } "
                    )
                },
                inlineContent = mapOf(
                    "info" to InlineTextContent(
                        Placeholder(
                            width = 18.sp,
                            height = 18.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.info_32dp),
                            contentDescription = "Info Icon",
                            tint = CustomTheme.colors.m
                        )
                    },
                ),
                style = TextStyle(
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                ),
            )
        }
        AnimatedVisibility(showRow) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(revealedCardIds.dropLast(1), { id -> id }) { id ->
                    ChallengeCard(id, isDarkTheme, true) { id ->
                        when(returnInOrderMode) {
                            ReturnChallengeCard.Bottom -> {
                                bottomList.add(id)
                                revealedCardIds.remove(id)
                            }
                            ReturnChallengeCard.Top -> {
                                topList.add(0, id)
                                revealedCardIds.remove(id)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (revealedCardIds.isEmpty()) Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(288.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.draw_challenge_card_placeholder),
                    color = CustomTheme.colors.d15,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 22.sp
                )
            } else ChallengeCard(revealedCardIds.last(), isDarkTheme) { id ->
                when(returnInOrderMode) {
                    ReturnChallengeCard.Bottom -> {
                        bottomList.add(id)
                        revealedCardIds.remove(id)
                    }
                    ReturnChallengeCard.Top -> {
                        topList.add(0, id)
                        revealedCardIds.remove(id)
                    }
                    else -> {}
                }
            }
        }
        AnimatedVisibility(challengeDeckScoutPosition > 0 && returnInOrderMode == null) {
            SquareButton(
                stringId = R.string.return_scouted_cards_button,
                leadingIcon = R.drawable.close_32dp,
                iconColor = CustomTheme.colors.warn,
                textColor = CustomTheme.colors.l30,
                buttonColor = ButtonDefaults.buttonColors().copy(
                    containerColor = CustomTheme.colors.d30
                ),
                onClick = { discardScoutedCards(); revealedCardIds.clear() },
                modifier = Modifier.fillMaxWidth()
            )
        }
        AnimatedVisibility(challengeDeckScoutPosition > 0) {
            SquareButton(
                stringId = when(returnInOrderMode) {
                    null -> R.string.return_scouted_cards_ordered_button
                    ReturnChallengeCard.Bottom -> R.string.apply_bottom_button
                    ReturnChallengeCard.Top -> R.string.apply_top_button
                },
                leadingIcon = R.drawable.swap_vert_32dp,
                iconColor = CustomTheme.colors.warn,
                textColor = CustomTheme.colors.l30,
                buttonColor = ButtonDefaults.buttonColors().copy(
                    containerColor = CustomTheme.colors.d30,
                    disabledContainerColor = CustomTheme.colors.d30.copy(alpha = 0.25f)
                ),
                onClick = { when(returnInOrderMode) {
                    null -> returnInOrderMode = ReturnChallengeCard.Bottom
                    ReturnChallengeCard.Bottom -> returnInOrderMode = ReturnChallengeCard.Top
                    ReturnChallengeCard.Top -> {
                        returnInOrderMode = null
                        returnChallengeCardsInAnyOrder(topList, bottomList)
                        topList.clear()
                        bottomList.clear()
                    }
                } },
                isEnabled = when(returnInOrderMode) {
                    null -> true
                    ReturnChallengeCard.Bottom -> true
                    ReturnChallengeCard.Top -> revealedCardIds.isEmpty()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        SquareButton(
            stringId = R.string.draw_challenge_card_button,
            leadingIcon = R.drawable.card,
            iconColor = CustomTheme.colors.l15,
            textColor = CustomTheme.colors.l30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.d10,
                disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.25f)
            ),
            onClick = {
                if (revealedCardIds.isNotEmpty()) {
                    if (challengeDeckScoutPosition == 0) {
                        val card = ChallengeDeck.challengeDeck[revealedCardIds.first()]
                        if (card!!.reshuffle) reshuffleChallengeDeck()
                    } else discardScoutedCards()
                    revealedCardIds.clear()
                } else {
                    drawChallengeCard()?.let { id ->
                        revealedCardIds.add(id)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isEnabled = challengeDeckSize > 0 && returnInOrderMode == null
        )
        SquareButton(
            stringId = R.string.scout_challenge_card_button,
            leadingIcon = R.drawable.scout_32dp,
            iconColor = CustomTheme.colors.l15,
            textColor = CustomTheme.colors.l30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.d10,
                disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.25f)
            ),
            onClick = { scoutChallengeCard()?.let { id -> revealedCardIds.add(id) } },
            modifier = Modifier.fillMaxWidth(),
            isEnabled = isScoutAvailable.value
        )
        AspectsRowCharts(challengeDeckIds)
        Spacer(modifier = Modifier.height(16.dp))
    }
}