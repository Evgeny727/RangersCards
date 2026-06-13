package com.rangerscards.ui.deck

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.rangerscards.R
import com.rangerscards.domain.model.CardDeckMulliganItem
import com.rangerscards.objects.ImageSrc
import com.rangerscards.ui.cards.components.CardListItem
import com.rangerscards.ui.components.DataPicker
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.RangersRadioButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DeckMulliganScreen(
    navigateUp: () -> Unit,
    navigateToCard: (String) -> Unit,
    deckViewModel: DeckViewModel,
    deckMulliganViewModel: DeckMulliganViewModel,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val slots by deckViewModel.orderedSlotsCards.collectAsState()
    //If true - images mode, if false - list mode
    var viewMode by rememberSaveable { mutableStateOf(false) }
    val cardInPlay by deckMulliganViewModel.cardInPlay.collectAsState()
    val cardsInDeck by deckMulliganViewModel.cardsInDeck.collectAsState()
    var showDialogPicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(slots) {
        withContext(Dispatchers.Default) {
            deckMulliganViewModel.setSlots(slots.values.flatten())
        }
    }

    if (showDialogPicker) RangersDialogWithContent(
        headerId = R.string.card_in_play,
        isDarkTheme = isDarkTheme,
        onBack = { showDialogPicker = false },
    ) {
        val setupCards = cardsInDeck.filter { it.setup }
        LazyColumn(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
            item("no_start_in_play", contentType = { String }) {
                Text(
                    text = stringResource(R.string.text_none),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            deckMulliganViewModel.setCardInPlay(null)
                            showDialogPicker = false
                        }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                )
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
            items(items = setupCards, key = { it.id }, contentType = { String }) { card ->
                Text(
                    text = card.name.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            deckMulliganViewModel.setCardInPlay(card)
                            showDialogPicker = false
                        }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                )
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
        }
    }

    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = modifier.fillMaxSize().applyScaffoldPaddings(contentPadding),
        topBar = {
            RangersTopAppBar(
                title = stringResource(R.string.draw_simulator),
                canNavigateBack = true,
                navigateUp = navigateUp,
                actions = {
                    IconButton(
                        onClick = { viewMode = !viewMode },
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painterResource(id = if (viewMode) R.drawable.imagesmode_32dp
                                else R.drawable.list_32dp),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .applyScaffoldPaddings(innerPadding),
            contentPadding = PaddingValues(8.dp),
        ) {
            if (cardsInDeck.firstOrNull { it.setup } != null) item("setup_card_picker") {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)) {
                    DataPicker(
                        onClick = { showDialogPicker = true },
                        type = R.string.card_in_play,
                        isRequired = false
                    ) {
                        Text(
                            text = cardInPlay?.name ?: stringResource(R.string.text_none),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            item("draw_section") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    SquareButton(
                        stringId = R.string.draw_1,
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            containerColor = CustomTheme.colors.d10,
                            disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.3f)
                        ),
                        onClick = { deckMulliganViewModel.drawCards(1) },
                        isEnabled = deckMulliganViewModel.allCards.isNotEmpty(),
                        modifier = Modifier.weight(0.3f)
                    )
                    SquareButton(
                        stringId = R.string.draw_6,
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            containerColor = CustomTheme.colors.d10,
                            disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.3f)
                        ),
                        onClick = { deckMulliganViewModel.drawCards(6) },
                        isEnabled = deckMulliganViewModel.allCards.isNotEmpty(),
                        modifier = Modifier.weight(0.3f)
                    )
                    SquareButton(
                        stringId = R.string.reset,
                        leadingIcon = R.drawable.close_32dp,
                        iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            containerColor = CustomTheme.colors.warn,
                            disabledContainerColor = CustomTheme.colors.warn.copy(alpha = 0.3f)
                        ),
                        isEnabled = deckMulliganViewModel.drawedCards.isNotEmpty(),
                        onClick = { deckMulliganViewModel.resetCards() },
                        modifier = Modifier.weight(0.37f)
                    )
                }
            }
            item("selection_section") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    SquareButton(
                        stringId = R.string.redraw_selected,
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            containerColor = CustomTheme.colors.d10,
                            disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.3f)
                        ),
                        onClick = { deckMulliganViewModel.redrawSelected() },
                        isEnabled = deckMulliganViewModel.selectedCards.isNotEmpty(),
                        modifier = Modifier.weight(0.5f)
                    )
                    SquareButton(
                        stringId = R.string.reshuffle_selected,
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            containerColor = CustomTheme.colors.d10,
                            disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.3f)
                        ),
                        onClick = { deckMulliganViewModel.reshuffleSelected() },
                        isEnabled = deckMulliganViewModel.selectedCards.isNotEmpty(),
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
            item("drawn_cards_header") {
                Text(
                    text = stringResource(R.string.drawn_cards),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            if (viewMode) item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .verticalScroll(rememberScrollState())
                        .animateContentSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 6,
                ) {
                    deckMulliganViewModel.drawedCards.forEach { card ->
                        val isSelected = deckMulliganViewModel.selectedCards.contains(card)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            RangersRadioButton(
                                selected = isSelected,
                                onClick = { selected ->
                                    if (selected) deckMulliganViewModel.selectedCards.add(card)
                                    else deckMulliganViewModel.selectedCards.remove(card)
                                },
                                modifier = Modifier.size(48.dp)
                            )
                            CardImage(card.imageSrc) { navigateToCard(card.code) }
                        }
                    }
                }
            } else items(
                items = deckMulliganViewModel.drawedCards,
                key = { it.id },
                contentType = { CardDeckMulliganItem::class }
            ) { card ->
                val isSelected = deckMulliganViewModel.selectedCards.contains(card)
                CardListItem(
                    tabooId = card.tabooId,
                    aspect = card.aspect,
                    cost = card.cost,
                    imageSrc = card.imageSrc,
                    approaches = card.approaches,
                    name = card.name.toString(),
                    typeName = card.typeName,
                    traits = card.traits,
                    level = card.level,
                    isDarkTheme = isDarkTheme,
                    onClick = { navigateToCard(card.code) },
                    onSelectedChange = { selected ->
                        if (selected) deckMulliganViewModel.selectedCards.add(card)
                        else deckMulliganViewModel.selectedCards.remove(card)
                    },
                    isSelected = isSelected,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun CardImage(imageSrc: String?, onClick: () -> Unit) {
    Surface(
        color = CustomTheme.colors.l30,
        shape = CustomTheme.shapes.large,
        modifier = Modifier
            .size(114.dp, 160.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(ImageSrc.BASE_URL + imageSrc)
                .build(),
            placeholder = painterResource(R.drawable.broken_image_32dp),
            error = painterResource(R.drawable.broken_image_32dp),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
    }
}
