package com.rangerscards.ui.deck

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rangerscards.R
import com.rangerscards.SUPPORTED_LANGUAGES
import com.rangerscards.domain.exceptions.DeckContainsErrorsException
import com.rangerscards.domain.exceptions.DeckContainsUpgradesException
import com.rangerscards.domain.exceptions.DeckInCampaignException
import com.rangerscards.domain.exceptions.NotAvailableWhileInEitModeException
import com.rangerscards.domain.model.UserInfo
import com.rangerscards.objects.CardTextParser
import com.rangerscards.objects.DeckMetaMaps
import com.rangerscards.ui.cards.components.CardListItem
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.RangersLoadingDialog
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.deck.components.DeckRightSideDrawer
import com.rangerscards.ui.deck.components.FullDeckProblemsItem
import com.rangerscards.ui.deck.components.FullDeckRoleItem
import com.rangerscards.ui.deck.components.FullDeckStatsItem
import com.rangerscards.ui.navigation.BottomNavScreen
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.settings.components.SettingsInputField
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import com.rangerscards.utils.openLink
import kotlinx.collections.immutable.persistentListOf
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

enum class DialogType {
    Save,
    Delete,
}

enum class DialogWithInputType {
    Name,
    Clone
}

const val deckLink = "rangersdb.com/decks/view"

@Composable
fun DeckScreen(
    emitError: (Throwable) -> Unit,
    showMessage: suspend (String) -> Unit,
    deckViewModel: DeckViewModel,
    navController: NavHostController,
    userInfo: UserInfo?,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val deckUiState by deckViewModel.deckUiState.collectAsState()
    val deck by deckViewModel.deck.collectAsState()
    val values by deckViewModel.updatableValues.collectAsState()
    val role by deckViewModel.deckRole.collectAsState()
    val slots by deckViewModel.orderedSlotsCards.collectAsState()
    val extraSlots by deckViewModel.extraSlotsCards.collectAsState()
    val changedCards by deckViewModel.changedCards.collectAsState()
    val deckProblems by deckViewModel.deckProblems.collectAsState()
    var showActionDialog by rememberSaveable { mutableStateOf<DialogType?>(null) }
    var showInputDialog by rememberSaveable { mutableStateOf<DialogWithInputType?>(null) }
    var drawerOpen by remember { mutableStateOf(false) }
    var deckNameEditing by rememberSaveable(deck?.name) { mutableStateOf(deck?.name ?: "") }
    var isUploadClone by rememberSaveable(deck?.uploaded) { mutableStateOf(deck?.uploaded ?: false) }
    val isEditing = remember(deckUiState) { deckUiState is DeckUiState.Editing }

    BackHandler {
        if (deckViewModel.checkChanges()) { showActionDialog = DialogType.Save }
        else navController.navigateUp()
    }

    LaunchedEffect(Unit) {
        deckViewModel.events.collect { emitError(it.exception) }
    }

    LaunchedEffect(deckUiState) {
        when (val state = deckUiState) {
            is DeckUiState.DeckToOpen -> state.deckId?.let {
                navController.navigate("deck/${state.deckId}") {
                    popUpTo(navController.previousBackStackEntry?.destination?.id!!) { inclusive = false }
                    launchSingleTop = true
                }
            } ?: navController.navigateUp()
            is DeckUiState.DeckUploaded -> navController.navigate("deck/${state.deckId}") {
                popUpTo(BottomNavScreen.Decks.startDestination) { inclusive = false }
                launchSingleTop = true
            }
            else -> Unit
        }
    }

    if (showActionDialog != null) RangersDialogWithContent(
        headerId = if (showActionDialog == DialogType.Save) R.string.save_deck_changes_header
            else R.string.options_section_delete_deck,
        isDarkTheme = isDarkTheme,
        onBack = { showActionDialog = null }
    ) {
        Text(
            text = if (showActionDialog == DialogType.Save)
                stringResource(id = R.string.save_deck_changes_text)
            else stringResource(id = R.string.delete_deck_text, deck?.version ?: 1),
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        SquareButton(
            stringId = R.string.cancel_button,
            leadingIcon = R.drawable.close_32dp,
            onClick = { showActionDialog = null },
            buttonColor = ButtonDefaults.buttonColors()
                .copy(CustomTheme.colors.d30),
            iconColor = CustomTheme.colors.warn,
            textColor = CustomTheme.colors.l30
        )
        if (showActionDialog == DialogType.Save) {
            SquareButton(
                R.string.discard_deck_changes_button,
                R.drawable.delete_32dp,
                onClick = {
                    deckViewModel.discardChanges()
                    showActionDialog = null
                    navController.navigateUp()
                },
                buttonColor = ButtonDefaults.buttonColors()
                    .copy(CustomTheme.colors.warn),
                iconColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30,
                textColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30,
            )
            SquareButton(
                stringId = R.string.save_deck_changes_button,
                leadingIcon = R.drawable.done_32dp,
                onClick = {
                    showActionDialog = null
                    deckViewModel.saveChanges(true)?.invokeOnCompletion {
                        navController.navigateUp()
                    } ?: navController.navigateUp()
                },
            )
        } else {
            SquareButton(
                stringId = R.string.options_section_delete_current_deck,
                leadingIcon = R.drawable.delete_32dp,
                onClick = { showActionDialog = null; deckViewModel.deleteDeck() },
                buttonColor = ButtonDefaults.buttonColors()
                    .copy(CustomTheme.colors.warn),
                iconColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30,
                textColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30,
            )
            SquareButton(
                stringId = R.string.options_section_delete_deck_all_versions,
                leadingIcon = R.drawable.delete_32dp,
                onClick = { showActionDialog = null; deckViewModel.deleteAllVersionsOfDeck() },
                buttonColor = ButtonDefaults.buttonColors()
                    .copy(CustomTheme.colors.warn),
                iconColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30,
                textColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30,
            )
        }
    }
    if (deckUiState is DeckUiState.Loading) RangersLoadingDialog(isDarkTheme)
    if (showInputDialog != null) RangersDialogWithContent(
        headerId = R.string.name_label,
        isDarkTheme = isDarkTheme,
        onBack = { showInputDialog = null; deckNameEditing = deck?.name ?: "" }
    ) {
        SettingsInputField(
            leadingIcon = R.drawable.badge_32dp,
            placeholder = null,
            textValue = deckNameEditing,
            onValueChange = { deckNameEditing = it },
            KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            )
        )
        if (showInputDialog == DialogWithInputType.Clone)
            RangersRadioButtonRow(
                text = stringResource(R.string.upload_to_rangersdb),
                isSelected = isUploadClone,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) { value -> isUploadClone = value }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SquareButton(
                stringId = R.string.cancel_button,
                leadingIcon = R.drawable.close_32dp,
                onClick = { showInputDialog = null; deckNameEditing = deck?.name ?: "" },
                buttonColor = ButtonDefaults.buttonColors().copy(
                    CustomTheme.colors.d30,
                    disabledContainerColor = CustomTheme.colors.m
                ),
                iconColor = CustomTheme.colors.warn,
                textColor = CustomTheme.colors.l30,
                modifier = Modifier.weight(0.5f),
            )
            val postfix = stringResource(R.string.clone_deck_name_postfix)
            SquareButton(
                stringId = R.string.done_button,
                leadingIcon = R.drawable.done_32dp,
                onClick = {
                    when(showInputDialog) {
                        DialogWithInputType.Name -> {
                            deckViewModel.updateDeckName(deckNameEditing)
                            showInputDialog = null
                        }
                        else -> {
                            deckViewModel.cloneDeck(
                                isUploadClone,
                                deckNameEditing,
                                postfix
                            )
                            showInputDialog = null
                        }
                    }
                },
                buttonColor = ButtonDefaults.buttonColors().copy(
                    CustomTheme.colors.d10,
                    disabledContainerColor = CustomTheme.colors.m
                ),
                iconColor = CustomTheme.colors.l15,
                textColor = CustomTheme.colors.l30,
                isEnabled = deckNameEditing.isNotEmpty(),
                modifier = Modifier.weight(0.5f),
            )
        }
    }
    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = Modifier.applyScaffoldPaddings(contentPadding),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = CustomTheme.colors.l30,
                shadowElevation = 4.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            if (deckViewModel.checkChanges()) showActionDialog = DialogType.Save
                                else navController.navigateUp()
                        },
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.arrow_back_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        val background = remember(deck?.deckMeta?.background) {
                            DeckMetaMaps.background[deck?.deckMeta?.background]
                        }
                        val specialty = remember(deck?.deckMeta?.specialty) {
                            DeckMetaMaps.specialty[deck?.deckMeta?.specialty]
                        }
                        Text(
                            text = buildAnnotatedString {
                                if (background != null)
                                    append(stringResource(background) + " - ")
                                if (specialty != null)
                                    append(stringResource(specialty))
                            },
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = deck?.name ?: "",
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { if (deckUiState !is DeckUiState.Editing) drawerOpen = !drawerOpen
                                  else emitError(NotAvailableWhileInEitModeException()) },
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.menu_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        if (deck == null) {
            Column(
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
        } else {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())) {
                val isOwner = remember(userInfo, deck?.playerInfo?.id) {
                    userInfo?.id == deck!!.playerInfo.id || deck!!.playerInfo.id.isEmpty()
                }
                LazyColumn(contentPadding = PaddingValues(8.dp)) {
                    item(key = "role/${deck!!.deckMeta.roleId}") {
                        FullDeckRoleItem(
                            tabooId = role?.tabooId,
                            imageSrc = role?.realImageSrc,
                            name = role?.name,
                            text = CardTextParser.parseCustomText(role?.text, null),
                            campaignName = deck!!.campaignInfo?.campaignName,
                            onClick = if (role != null) {{
                                navController.navigate("deck/card/${deck!!.deckMeta.roleId}") {
                                    launchSingleTop = true
                                }
                            }} else {{ }},
                            onEdit = if (isOwner && deck!!.nextId == null) {{
                                navController.navigate("deck/roleChanging") {
                                    launchSingleTop = true
                                }
                            } } else null
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (values != null) item("deck_stats") {
                        val stats = remember(values) {
                            persistentListOf(values!!.awa, values!!.spi, values!!.fit, values!!.foc)
                        }
                        FullDeckStatsItem(
                            stats = stats,
                            isDarkTheme = isDarkTheme,
                            isEditing = deckUiState is DeckUiState.Editing,
                            isUpgrade = deck!!.previousDeck != null,
                            onStatChange = deckViewModel::changeStat
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (deckProblems.problems.isNotEmpty()) item(key = "deck_problems") {
                        FullDeckProblemsItem(deckProblems.problems)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (isOwner && deck!!.nextId == null) item(key = "edit_button") {
                        Button(
                            onClick = {
                                if (isEditing) {
                                    deckViewModel.saveChanges(); showActionDialog = null
                                } else deckViewModel.enterEditMode()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = CustomTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors().copy(CustomTheme.colors.d10),
                            contentPadding = PaddingValues(8.dp),
                        ) {
                            Icon(
                                painterResource(
                                    id = if (!isEditing) R.drawable.edit_32dp
                                    else R.drawable.done_32dp
                                ),
                                contentDescription = null,
                                tint = CustomTheme.colors.l30,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(
                                        id = if (!isEditing) R.string.edit_deck_button
                                        else R.string.save_deck_button
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = CustomTheme.colors.l30,
                                    fontFamily = Jost,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.1.sp
                                )
                                val (amount, maladyAmount) = remember(slots.values) {
                                    slots.values.flatten().fold(0 to 0) { (nonMalady, malady), (cardItem, value) ->
                                        if (cardItem.setId == "malady") nonMalady to (malady + value)
                                        else (nonMalady + value) to malady
                                    }
                                }
                                Text(
                                    text = buildAnnotatedString {
                                        append(
                                            stringResource(
                                                R.string.cards_amount_in_deck,
                                                amount
                                            )
                                        )
                                        if (maladyAmount > 0) append(
                                            " ${
                                                pluralStringResource(
                                                    R.plurals.maladies_amount,
                                                    maladyAmount,
                                                    maladyAmount
                                                )
                                            }"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = CustomTheme.colors.l10,
                                    fontFamily = Jost,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                    item("deck_section_header") {
                        DeckSectionHeader(R.string.deck_section_header)
                    }
                    slots.forEach { (key, cards) ->
                        when (key) {
                            "personality" -> item("deck_section_personality_header") {
                                DeckCardsTypeHeader(
                                    textId = R.string.personality,
                                    onClick = if (isOwner && deck!!.nextId == null) {{
                                        deckViewModel.enterEditMode()
                                        navController.navigate("deck/cardsList/0") {
                                            launchSingleTop = true
                                        }
                                    }} else null
                                )
                            }
                            "background" -> item("deck_section_background_header") {
                                val background = stringResource(
                                    DeckMetaMaps.background[deck!!.deckMeta.background] ?: R.string.text_none
                                )
                                DeckCardsTypeHeader(
                                    textId = R.string.background,
                                    additionalText = background,
                                    onClick = if (isOwner && deck!!.nextId == null) {{
                                        deckViewModel.enterEditMode()
                                        navController.navigate("deck/cardsList/${if (deck!!.previousDeck == null) 1 else 0}") {
                                            launchSingleTop = true
                                        }
                                    }} else null
                                )
                            }

                            "specialty" -> item("deck_section_specialty_header") {
                                val specialty = stringResource(
                                    DeckMetaMaps.specialty[deck!!.deckMeta.specialty] ?: R.string.text_none
                                )
                                DeckCardsTypeHeader(
                                    textId = R.string.specialty,
                                    additionalText = specialty,
                                    onClick = if (isOwner && deck!!.nextId == null) {{
                                        deckViewModel.enterEditMode()
                                        navController.navigate("deck/cardsList/${if (deck!!.previousDeck == null) 2 else 0}") {
                                            launchSingleTop = true
                                        }
                                    }} else null
                                )
                            }

                            "outsideInterest" -> item("deck_section_outsideInterest_header") {
                                Column {
                                    DeckCardsTypeHeader(
                                        textId = R.string.outside_interest,
                                        onClick = if (isOwner && deck!!.nextId == null) {{
                                            deckViewModel.enterEditMode()
                                            navController.navigate("deck/cardsList/${if (deck!!.previousDeck == null) 3 else 0}") {
                                                launchSingleTop = true
                                            }
                                        }} else null
                                    )
                                    if (deckProblems.splash != null) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            val iconId = "info"
                                            BasicText(
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                text = buildAnnotatedString {
                                                    appendInlineContent(iconId, "[$iconId]")
                                                    append(
                                                        " ${
                                                            stringResource(deckProblems.splash!!)
                                                        } "
                                                    )
                                                },
                                                inlineContent = mapOf(
                                                    "info" to InlineTextContent(
                                                        Placeholder(
                                                            width = 16.sp,
                                                            height = 16.sp,
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
                                                    fontSize = 16.sp,
                                                    lineHeight = 18.sp,
                                                ),
                                            )
                                            HorizontalDivider(color = CustomTheme.colors.l10)
                                        }
                                    }
                                }
                            }

                            "other" -> if (deck!!.previousDeck != null || cards.isNotEmpty()) item("deck_section_other_header") {
                                Column {
                                    DeckCardsTypeHeader(
                                        textId = R.string.rewards_and_maladies,
                                        onClick = if (isOwner && deck!!.nextId == null) {{
                                            deckViewModel.enterEditMode()
                                            navController.navigate("deck/cardsList/0") {
                                                launchSingleTop = true
                                            }
                                        }} else null
                                    )
                                    if (deck!!.previousDeck == null) Column(modifier = Modifier.fillMaxWidth()) {
                                        val iconId = "warn"
                                        BasicText(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            text = buildAnnotatedString {
                                                appendInlineContent(iconId, "[$iconId]")
                                                append(
                                                    " ${
                                                        stringResource(R.string.reward_or_malady_in_starting_deck)
                                                    } "
                                                )
                                            },
                                            inlineContent = mapOf(
                                                "warn" to InlineTextContent(
                                                    Placeholder(
                                                        width = 16.sp,
                                                        height = 16.sp,
                                                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                                                    )
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.error_32dp),
                                                        contentDescription = "Info Icon",
                                                        tint = CustomTheme.colors.warn
                                                    )
                                                },
                                            ),
                                            style = TextStyle(
                                                color = CustomTheme.colors.warn,
                                                fontFamily = Jost,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 16.sp,
                                                lineHeight = 18.sp,
                                            ),
                                        )
                                        HorizontalDivider(color = CustomTheme.colors.l10)
                                    }
                                }
                            }
                        }
                        items(cards, { it.card.id }, { it.card }) { item ->
                            val card = item.card
                            CardListItem(
                                tabooId = card.tabooId,
                                aspect = card.aspect,
                                cost = card.cost,
                                imageSrc = card.realImageSrc,
                                approaches = card.approaches,
                                name = card.name.toString(),
                                typeName = card.typeName,
                                traits = card.traits,
                                level = card.level,
                                isDarkTheme = isDarkTheme,
                                currentAmount = item.count,
                                onRemoveClick = if (isEditing) { {
                                    deckViewModel.removeCard(
                                        card.code,
                                        card.setId
                                    )
                                } } else null,
                                onRemoveEnabled = item.count > 0,
                                onAddClick = if (isEditing) { {
                                    deckViewModel.addCard(card.code)
                                } } else null,
                                onAddEnabled = item.count != card.deckLimit,
                                onClick = {
                                    navController.navigate("deck/card/${card.code}") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                    item("side_deck_section_header") {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            DeckSectionHeader(R.string.side_deck)
                        }
                    }
                    items(extraSlots, { "side_${it.card.id}" }, { it.card }) { item ->
                        val currentAmount = values?.slots?.get(item.card.id) ?: 0
                        val card = item.card
                        CardListItem(
                            tabooId = card.tabooId,
                            aspect = card.aspect,
                            cost = card.cost,
                            imageSrc = card.realImageSrc,
                            approaches = card.approaches,
                            name = card.name.toString(),
                            typeName = card.typeName,
                            traits = card.traits,
                            level = card.level,
                            isDarkTheme = isDarkTheme,
                            currentAmount = currentAmount,
                            onRemoveClick = if (isEditing) { {
                                deckViewModel.removeCard(card.code, card.setId)
                            } } else null,
                            onRemoveEnabled = currentAmount > 0,
                            onAddClick = if (isEditing) { {
                                deckViewModel.addCard(card.code)
                            } } else null,
                            onAddEnabled = currentAmount != card.deckLimit,
                            onClick = {
                                navController.navigate("deck/card/${card.code}") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    if (deck!!.previousDeck != null && changedCards.isNotEmpty()){
                        item("deck_changes_section_header") {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                DeckSectionHeader(R.string.deck_changes)
                            }
                        }
                        changedCards.forEach { (title, cards) ->
                            if (cards.isNotEmpty()) {
                                item("changes_header_$title") {
                                    DeckCardsTypeHeader(title)
                                }
                                items(cards, { "changes_${it.card.id}" }, { it.card }) { cardWithCount ->
                                    val card = cardWithCount.card
                                    CardListItem(
                                        tabooId = card.tabooId,
                                        aspect = card.aspect,
                                        cost = card.cost,
                                        imageSrc = card.realImageSrc,
                                        approaches = card.approaches,
                                        name = card.name.toString(),
                                        typeName = card.typeName,
                                        traits = card.traits,
                                        level = card.level,
                                        isDarkTheme = isDarkTheme,
                                        charForAmount = when(title){
                                            R.string.deck_changes_added -> "+"
                                            R.string.deck_changes_added_collection -> "+"
                                            else -> null
                                        },
                                        currentAmount = cardWithCount.count,
                                        onClick = {
                                            navController.navigate("deck/card/${card.code}") {
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                val isTabooSet = remember(deck?.tabooSetId) { deck?.tabooSetId != null }
                val locale = LocalLocale.current.platformLocale.language.take(2)
                val supportedLocale = if (SUPPORTED_LANGUAGES.contains(locale)) locale
                else ""
                val context = LocalContext.current
                DeckRightSideDrawer(
                    isOpen = drawerOpen,
                    onClick = { drawerOpen = !drawerOpen },
                    showMessage = showMessage,
                    isOwner = isOwner,
                    deckName = deck!!.name,
                    deckId = if (deck!!.uploaded) deck!!.id else null,
                    changeName = { showInputDialog = DialogWithInputType.Name; deckNameEditing = deck!!.name },
                    setTaboo = { deckViewModel.setDeckTaboo(!isTabooSet) },
                    isTabooSet = isTabooSet,
                    toNotes = { /*TODO:Implement notes*/ },
                    toCharts = { navController.navigate("deck/charts") {launchSingleTop = true } },
                    toMulligan = { navController.navigate("deck/mulligan") {launchSingleTop = true } },
                    camp = if (deck!!.nextId == null) { { if (deckProblems.problems.isNotEmpty())
                        emitError(DeckContainsErrorsException()) else deckViewModel.camp()
                    } } else null,
                    toPreviousDeck = if (deck!!.previousDeck != null) {{
                        navController.navigate("deck/${deck!!.previousDeck!!.id}") {
                            popUpTo(navController.previousBackStackEntry?.destination?.id!!) { inclusive = false }
                            launchSingleTop = true
                        }
                    }} else null,
                    toNextDeck = if (deck!!.nextId != null) {{
                        navController.navigate(
                            "deck/${deck!!.nextId}"
                        ) {
                            popUpTo(navController.previousBackStackEntry?.destination?.id!!) { inclusive = false }
                            launchSingleTop = true
                        }
                    }} else null,
                    toDeckHistory = if (deck!!.previousDeck != null || deck!!.nextId != null) { {
                        navController.navigate("deck/${deck!!.id}/history") { launchSingleTop = true }
                    } } else null,
                    cloneDeck = { showInputDialog = DialogWithInputType.Clone },
                    upload = if (userInfo == null) null
                    else { {
                        if (deck!!.uploaded) context.openLink(
                            if (supportedLocale.isNotEmpty() && supportedLocale != "en")
                                "https://$supportedLocale.$deckLink/${deck!!.id}"
                            else "https://$deckLink/${deck!!.id}"
                        ) else {
                            if (deck!!.nextId != null || deck!!.previousDeck != null)
                                emitError(DeckContainsUpgradesException())
                            else if (deck!!.campaignInfo != null) emitError(DeckInCampaignException())
                            else deckViewModel.uploadDeck()
                        }
                    } },
                    url = if (deck!!.uploaded) {
                        if (supportedLocale.isNotEmpty() && supportedLocale != "en")
                            "https://$supportedLocale.$deckLink/${deck!!.id}"
                        else "https://$deckLink/${deck!!.id}"
                    } else null,
                    deleteDeck = { showActionDialog = DialogType.Delete }
                )
            }
        }
    }
}

@Composable
fun DeckSectionHeader(@StringRes textId: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "— ${stringResource(textId)} —",
            color = CustomTheme.colors.d10,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 22.sp,
        )
    }
}

@Composable
fun DeckCardsTypeHeader(
    @StringRes textId: Int,
    additionalText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 16.dp, bottom = 4.dp)
                .clickable(onClick = onClick ?: {}),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(textId) + if (additionalText != null) ": $additionalText" else "",
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
            if (onClick != null) Icon(
                painterResource(R.drawable.edit_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(24.dp)
            )
        }
        HorizontalDivider(color = CustomTheme.colors.l10)
    }
}