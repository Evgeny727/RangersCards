package com.rangerscards.ui.deck

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.R
import com.rangerscards.objects.CardTextParser
import com.rangerscards.objects.DeckMetaMaps
import com.rangerscards.ui.cards.components.CardListItem
import com.rangerscards.ui.components.RangersRadioButton
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.deck.components.DeckCardsTypeCard
import com.rangerscards.ui.deck.components.DeckRightSideDrawer
import com.rangerscards.ui.deck.components.FullDeckProblemsItem
import com.rangerscards.ui.deck.components.FullDeckRoleItem
import com.rangerscards.ui.deck.components.FullDeckStatsItem
import com.rangerscards.ui.navigation.BottomNavScreen
import com.rangerscards.ui.settings.SUPPORTED_LANGUAGES
import com.rangerscards.ui.settings.components.RangersBaseCard
import com.rangerscards.ui.settings.components.SettingsInputField
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

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
    navController: NavHostController,
    deckViewModel: DeckViewModel,
    deckId: String,
    user: FirebaseUser?,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val originalDeck by deckViewModel.originalDeck.collectAsState()
    val editableDeck by deckViewModel.editableDeck.collectAsState()
    val values by deckViewModel.updatableValues.collectAsState()
    val isEditing by deckViewModel.isEditing.collectAsState()
    val role by deckViewModel.role.collectAsState()
    val deck = if (isEditing) editableDeck else originalDeck
    var needLoadDeck by rememberSaveable { mutableStateOf(true) }
    val slots = deckViewModel.slotsCardsFlow.collectAsState(null).value?.groupBy {
        when {
            it.setId == "personality" -> "personality"
            it.setTypeId == "background" -> if (it.setId == deck!!.background) "background" else "outsideInterest"
            it.setTypeId == "specialty" -> if (it.setId == deck!!.specialty) "specialty" else "outsideInterest"
            else -> "other"
        }
    }?.mapValues { (_, cards) -> cards.associateWith { (values!!.slots[it.code] ?: 0) } }
    val orderedSlots = listOf("personality", "background", "specialty", "outsideInterest", "other")
        .associateWith { key -> slots?.get(key) }
    val extraSlots = deckViewModel.extraSlotsCardsFlow.collectAsState(null).value
        ?.associate { card -> card to (values!!.extraSlots[card.code] ?: 0) }
    val changedCards = deckViewModel.changedCards.collectAsState()
    val deckProblems = deckViewModel.deckProblemsFlow.collectAsState(deck?.problems to (0 to null))
    var showActionDialog by rememberSaveable { mutableStateOf<DialogType?>(null) }
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    var showInputDialog by rememberSaveable { mutableStateOf<DialogWithInputType?>(null) }
    val coroutine = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    var drawerOpen by remember { mutableStateOf(false) }
    var deckNameEditing by rememberSaveable { mutableStateOf("") }
    var isUploadClone by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(needLoadDeck) {
        delay(500L)
        if (needLoadDeck) {
            deckViewModel.loadDeck(deckId)
            needLoadDeck = false
        }
    }
    LaunchedEffect(deck) {
        if (deck != null) {
            if (deckNameEditing.isEmpty()) deckNameEditing = deck.name
            isUploadClone = deck.uploaded
        }
    }
    BackHandler {
        if (deckViewModel.checkChanges()) {
            showActionDialog = DialogType.Save
        } else navController.navigateUp()
    }
    if (showActionDialog != null) Dialog(
        onDismissRequest = { showActionDialog = null },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        RangersBaseCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = if (showActionDialog == DialogType.Save) R.string.save_deck_changes_header
            else R.string.options_section_delete_deck
        ) {
            Text(
                text = if (showActionDialog == DialogType.Save)
                    stringResource(id = R.string.save_deck_changes_text)
                else stringResource(id = R.string.delete_deck_text, deck?.version ?: 0),
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
                    isEnabled = !showLoadingDialog
                )
                SquareButton(
                    stringId = R.string.save_deck_changes_button,
                    leadingIcon = R.drawable.done_32dp,
                    onClick = {
                        coroutine.launch {
                            showLoadingDialog = true
                            showActionDialog = null
                            deckViewModel.saveChanges(user, deckProblems.value.first, context)
                        }.invokeOnCompletion {
                            showLoadingDialog = false
                            navController.navigateUp()
                        }
                    },
                )
            } else {
                SquareButton(
                    stringId = R.string.options_section_delete_current_deck,
                    leadingIcon = R.drawable.delete_32dp,
                    onClick = { coroutine.launch {
                        showLoadingDialog = true
                        showActionDialog = null
                        deckViewModel.deleteDeck(user)
                    }.invokeOnCompletion {
                        showLoadingDialog = false
                        if (deckViewModel.deckToOpen.value != null) navController.navigate(
                            "deck/${deckViewModel.deckToOpen.value}"
                        ) {
                            popUpTo(navController.previousBackStackEntry?.destination?.id!!) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                        else navController.navigateUp()
                    } },
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
                    onClick = { coroutine.launch {
                        showLoadingDialog = true
                        showActionDialog = null
                        deckViewModel.deleteAllVersionsOfDeck(user)
                    }.invokeOnCompletion {
                        showLoadingDialog = false
                        navController.navigateUp()
                    } },
                    buttonColor = ButtonDefaults.buttonColors()
                        .copy(CustomTheme.colors.warn),
                    iconColor = if (isDarkTheme)
                        CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    textColor = if (isDarkTheme)
                        CustomTheme.colors.d30 else CustomTheme.colors.l30,
                )
            }
        }
    }
    if (showLoadingDialog) Dialog(
        onDismissRequest = { showLoadingDialog = false },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        RangersBaseCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = R.string.saving_changes_header
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = CustomTheme.colors.m)
            }
        }
    }
    if (showInputDialog != null) Dialog(
        onDismissRequest = { showInputDialog = null
            deckNameEditing = deck?.name ?: ""
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        RangersBaseCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = R.string.deck_creation_name_label
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
            if (showInputDialog == DialogWithInputType.Clone && deckViewModel.isConnected(context)) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { isUploadClone = !isUploadClone },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.upload_to_rangersdb),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                    RangersRadioButton(
                        selected = isUploadClone,
                        onClick = { isUploadClone = !isUploadClone },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SquareButton(
                    stringId = R.string.cancel_button,
                    leadingIcon = R.drawable.close_32dp,
                    onClick = { showInputDialog = null
                        deckNameEditing = deck?.name ?: ""
                    },
                    buttonColor = ButtonDefaults.buttonColors().copy(
                        CustomTheme.colors.d30,
                        disabledContainerColor = CustomTheme.colors.m
                    ),
                    iconColor = CustomTheme.colors.warn,
                    textColor = CustomTheme.colors.l30,
                    modifier = Modifier.weight(0.5f),
                )
                SquareButton(
                    stringId = R.string.done_button,
                    leadingIcon = R.drawable.done_32dp,
                    onClick = when(showInputDialog) {
                        DialogWithInputType.Name -> {{ coroutine.launch {
                            showInputDialog = null
                            showLoadingDialog = true
                            deckViewModel.updateDeckName(user, deckProblems.value.first, deckNameEditing)
                        }.invokeOnCompletion {
                            deckNameEditing = ""
                            showLoadingDialog = false
                            deckViewModel.loadDeck(deckId) }
                        }}
                        else -> {{coroutine.launch { showLoadingDialog = true
                            deckViewModel.cloneDeck(
                                user, deckProblems.value.first, isUploadClone,
                                deckNameEditing, context)
                        }.invokeOnCompletion { showLoadingDialog = false
                            if (deckViewModel.deckToOpen.value != null) navController.navigate(
                                "deck/${deckViewModel.deckToOpen.value}"
                            ) {
                                popUpTo(BottomNavScreen.Decks.startDestination) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                            else navController.navigateUp() }
                        }}
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
    }
    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = Modifier.padding(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding()
        ),
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
                            if (deckViewModel.checkChanges()) {
                                showActionDialog = DialogType.Save
                            } else navController.navigateUp()
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
                        val background = DeckMetaMaps.background[deck?.background]
                        val specialty = DeckMetaMaps.specialty[deck?.specialty]
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
                        onClick = { drawerOpen = !drawerOpen },
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(24.dp),
                        enabled = !isEditing
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
                .padding(top = innerPadding.calculateTopPadding())
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "description/${deckId}") {
                        deckViewModel.getRole(deck.roleId, deck.tabooSetId != null)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FullDeckRoleItem(
                                tabooId = role?.tabooId,
                                imageSrc = role?.realImageSrc,
                                name = role?.name,
                                text = CardTextParser.parseCustomText(role?.text, null),
                                campaignName = deck.campaignName,
                                onClick = if (role != null) {{
                                    navController.navigate(
                                        "deck/card/${deck.roleId}"
                                    ) {
                                        launchSingleTop = true
                                    }
                                }} else { {} },
                                onEdit = {
                                    navController.navigate(
                                        "deck/roleChanging"
                                    ) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                            val stats =
                                listOf(values!!.awa, values!!.spi, values!!.fit, values!!.foc)
                            FullDeckStatsItem(
                                stats = stats,
                                isDarkTheme = isDarkTheme,
                                isEditing = isEditing,
                                isUpgrade = deck.previousId != null,
                                onStatChange = deckViewModel::changeStat
                            )
                        }
                    }
                    if (!deckProblems.value.first.isNullOrEmpty())
                        item(key = "problem") {
                            FullDeckProblemsItem(deckProblems.value.first!!)
                        }
                    if (deck.nextId == null &&
                        (user == null || user.uid == deck.userId || deck.userId.isEmpty()))
                        item(key = "edit_button") {
                        Button(
                            onClick = {
                                if (!isEditing) deckViewModel.enterEditMode()
                                else {
                                    coroutine.launch {
                                        showLoadingDialog = true
                                        deckViewModel.saveChanges(
                                            user,
                                            deckProblems.value.first,
                                            context
                                        )
                                    }.invokeOnCompletion {
                                        showLoadingDialog = false
                                        showActionDialog = null
                                    }
                                }
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
                                val (amount, maladyAmount) = slots?.values?.flatMap { it.entries }
                                    ?.fold(0 to 0) { (nonMalady, malady), (cardItem, value) ->
                                        if (cardItem.setId == "malady")
                                            nonMalady to (malady + value)
                                        else
                                            (nonMalady + value) to malady
                                    } ?: (0 to 0)
                                Text(
                                    text = buildAnnotatedString {
                                        append(
                                            stringResource(
                                                R.string.cards_amount_in_deck,
                                                amount
                                            )
                                        )
                                        if (maladyAmount > 0)
                                            append(
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
                    orderedSlots.forEach { (key, value) ->
                        when (key) {
                            "personality" -> item {
                                DeckCardsTypeCard(
                                    showIcon = deck.nextId == null && (user == null || user.uid == deck.userId
                                            || deck.userId.isEmpty()),
                                    label = stringResource(R.string.personality),
                                    onClick = {
                                        deckViewModel.enterEditMode()
                                        navController.navigate(
                                            "deck/cardsList/-1"
                                        ) {
                                            launchSingleTop = true
                                        }
                                    }
                                ) {
                                    value?.forEach { (card, amount) ->
                                        key(card.id) {
                                            CardListItem(
                                                tabooId = card.tabooId,
                                                aspectId = card.aspectId,
                                                aspectShortName = card.aspectShortName,
                                                cost = card.cost,
                                                imageSrc = card.realImageSrc,
                                                approachConflict = card.approachConflict,
                                                approachConnection = card.approachConnection,
                                                approachReason = card.approachReason,
                                                approachExploration = card.approachExploration,
                                                name = card.name.toString(),
                                                typeName = card.typeName,
                                                traits = card.traits,
                                                level = card.level,
                                                isDarkTheme = isDarkTheme,
                                                currentAmount = amount,
                                                onRemoveClick = if (isEditing) {
                                                    {
                                                        deckViewModel.removeCard(
                                                            card.code,
                                                            card.setId
                                                        )
                                                    }
                                                } else null,
                                                onRemoveEnabled = amount > 0,
                                                onAddClick = if (isEditing) {
                                                    { deckViewModel.addCard(card.code) }
                                                } else null,
                                                onAddEnabled = amount != card.deckLimit,
                                                onClick = {
                                                    navController.navigate(
                                                        "deck/card/${card.code}"
                                                    ) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            "background" -> item {
                                DeckCardsTypeCard(
                                    showIcon = deck.nextId == null && (user == null || user.uid == deck.userId
                                            || deck.userId.isEmpty()),
                                    label = stringResource(R.string.background) + ": " +
                                            if (DeckMetaMaps.background[deck.background] != null)
                                                stringResource(DeckMetaMaps.background[deck.background]!!)
                                            else "",
                                    onClick = {
                                        deckViewModel.enterEditMode()
                                        navController.navigate(
                                            "deck/cardsList/${if (deck.previousId == null) 1 else -1}"
                                        ) {
                                            launchSingleTop = true
                                        }
                                    }
                                ) {
                                    value?.forEach { (card, amount) ->
                                        key(card.id) {
                                            CardListItem(
                                                tabooId = card.tabooId,
                                                aspectId = card.aspectId,
                                                aspectShortName = card.aspectShortName,
                                                cost = card.cost,
                                                imageSrc = card.realImageSrc,
                                                approachConflict = card.approachConflict,
                                                approachConnection = card.approachConnection,
                                                approachReason = card.approachReason,
                                                approachExploration = card.approachExploration,
                                                name = card.name.toString(),
                                                typeName = card.typeName,
                                                traits = card.traits,
                                                level = card.level,
                                                isDarkTheme = isDarkTheme,
                                                currentAmount = amount,
                                                onRemoveClick = if (isEditing) {
                                                    {
                                                        deckViewModel.removeCard(
                                                            card.code,
                                                            card.setId
                                                        )
                                                    }
                                                } else null,
                                                onRemoveEnabled = amount > 0,
                                                onAddClick = if (isEditing) {
                                                    { deckViewModel.addCard(card.code) }
                                                } else null,
                                                onAddEnabled = amount != card.deckLimit,
                                                onClick = {
                                                    navController.navigate(
                                                        "deck/card/${card.code}"
                                                    ) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            "specialty" -> item {
                                DeckCardsTypeCard(
                                    showIcon = deck.nextId == null && (user == null || user.uid == deck.userId
                                            || deck.userId.isEmpty()),
                                    label = stringResource(R.string.specialty) + ": " +
                                            if (DeckMetaMaps.specialty[deck.specialty] != null)
                                                stringResource(DeckMetaMaps.specialty[deck.specialty]!!)
                                            else "",
                                    onClick = {
                                        deckViewModel.enterEditMode()
                                        navController.navigate(
                                            "deck/cardsList/${if (deck.previousId == null) 2 else -1}"
                                        ) {
                                            launchSingleTop = true
                                        }
                                    }
                                ) {
                                    value?.forEach { (card, amount) ->
                                        key(card.id) {
                                            CardListItem(
                                                tabooId = card.tabooId,
                                                aspectId = card.aspectId,
                                                aspectShortName = card.aspectShortName,
                                                cost = card.cost,
                                                imageSrc = card.realImageSrc,
                                                approachConflict = card.approachConflict,
                                                approachConnection = card.approachConnection,
                                                approachReason = card.approachReason,
                                                approachExploration = card.approachExploration,
                                                name = card.name.toString(),
                                                typeName = card.typeName,
                                                traits = card.traits,
                                                level = card.level,
                                                isDarkTheme = isDarkTheme,
                                                currentAmount = amount,
                                                onRemoveClick = if (isEditing) {
                                                    {
                                                        deckViewModel.removeCard(
                                                            card.code,
                                                            card.setId
                                                        )
                                                    }
                                                } else null,
                                                onRemoveEnabled = amount > 0,
                                                onAddClick = if (isEditing) {
                                                    { deckViewModel.addCard(card.code) }
                                                } else null,
                                                onAddEnabled = amount != card.deckLimit,
                                                onClick = {
                                                    navController.navigate(
                                                        "deck/card/${card.code}"
                                                    ) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            "outsideInterest" -> item {
                                DeckCardsTypeCard(
                                    showIcon = deck.nextId == null && (user == null || user.uid == deck.userId
                                            || deck.userId.isEmpty()),
                                    label = stringResource(R.string.outside_interest),
                                    onClick = {
                                        deckViewModel.enterEditMode()
                                        navController.navigate(
                                            "deck/cardsList/${if (deck.previousId == null) 3 else -1}"
                                        ) {
                                            launchSingleTop = true
                                        }
                                    }
                                ) {
                                    if (deckProblems.value.second.second != null) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            val iconId = "info"
                                            BasicText(
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                text = buildAnnotatedString {
                                                    appendInlineContent(iconId, "[$iconId]")
                                                    append(
                                                        " ${
                                                            stringResource(deckProblems.value.second.second!!)
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
                                    value?.forEach { (card, amount) ->
                                        key(card.id) {
                                            CardListItem(
                                                tabooId = card.tabooId,
                                                aspectId = card.aspectId,
                                                aspectShortName = card.aspectShortName,
                                                cost = card.cost,
                                                imageSrc = card.realImageSrc,
                                                approachConflict = card.approachConflict,
                                                approachConnection = card.approachConnection,
                                                approachReason = card.approachReason,
                                                approachExploration = card.approachExploration,
                                                name = card.name.toString(),
                                                typeName = card.typeName,
                                                traits = card.traits,
                                                level = card.level,
                                                isDarkTheme = isDarkTheme,
                                                currentAmount = amount,
                                                onRemoveClick = if (isEditing) {
                                                    {
                                                        deckViewModel.removeCard(
                                                            card.code,
                                                            card.setId
                                                        )
                                                    }
                                                } else null,
                                                onRemoveEnabled = amount > 0,
                                                onAddClick = if (isEditing) {
                                                    { deckViewModel.addCard(card.code) }
                                                } else null,
                                                onAddEnabled = amount != card.deckLimit,
                                                onClick = {
                                                    navController.navigate(
                                                        "deck/card/${card.code}"
                                                    ) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            "other" -> if (deck.previousId != null || value?.isNotEmpty() == true) item {
                                DeckCardsTypeCard(
                                    showIcon = deck.nextId == null && (user == null || user.uid == deck.userId
                                            || deck.userId.isEmpty()),
                                    label = stringResource(R.string.rewards_and_maladies),
                                    onClick = {
                                        deckViewModel.enterEditMode()
                                        navController.navigate(
                                            "deck/cardsList/-1"
                                        ) {
                                            launchSingleTop = true
                                        }
                                    }
                                ) {
                                    if (deck.previousId == null) Column(modifier = Modifier.fillMaxWidth()) {
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
                                    value?.forEach { (card, amount) ->
                                        key(card.id) {
                                            CardListItem(
                                                tabooId = card.tabooId,
                                                aspectId = card.aspectId,
                                                aspectShortName = card.aspectShortName,
                                                cost = card.cost,
                                                imageSrc = card.realImageSrc,
                                                approachConflict = card.approachConflict,
                                                approachConnection = card.approachConnection,
                                                approachReason = card.approachReason,
                                                approachExploration = card.approachExploration,
                                                name = card.name.toString(),
                                                typeName = card.typeName,
                                                traits = card.traits,
                                                level = card.level,
                                                isDarkTheme = isDarkTheme,
                                                currentAmount = amount,
                                                onRemoveClick = if (isEditing) {
                                                    {
                                                        deckViewModel.removeCard(
                                                            card.code,
                                                            card.setId
                                                        )
                                                    }
                                                } else null,
                                                onRemoveEnabled = amount > 0,
                                                onAddClick = if (isEditing) {
                                                    { deckViewModel.addCard(card.code) }
                                                } else null,
                                                onAddEnabled = if (deck.previousId != null) amount != card.deckLimit
                                                else false,
                                                onClick = {
                                                    navController.navigate(
                                                        "deck/card/${card.code}"
                                                    ) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        DeckCardsTypeCard(
                            showIcon = false,
                            label = stringResource(R.string.side_deck),
                        ) {
                            extraSlots?.forEach { (card, _) ->
                                key(card.id) {
                                    val currentAmount = values?.slots?.get(card.id) ?: 0
                                    CardListItem(
                                        tabooId = card.tabooId,
                                        aspectId = card.aspectId,
                                        aspectShortName = card.aspectShortName,
                                        cost = card.cost,
                                        imageSrc = card.realImageSrc,
                                        approachConflict = card.approachConflict,
                                        approachConnection = card.approachConnection,
                                        approachReason = card.approachReason,
                                        approachExploration = card.approachExploration,
                                        name = card.name.toString(),
                                        typeName = card.typeName,
                                        traits = card.traits,
                                        level = card.level,
                                        isDarkTheme = isDarkTheme,
                                        currentAmount = currentAmount,
                                        onRemoveClick = if (isEditing) {
                                            { deckViewModel.removeCard(card.code, card.setId) }
                                        } else null,
                                        onRemoveEnabled = currentAmount > 0,
                                        onAddClick = if (isEditing) {
                                            { deckViewModel.addCard(card.code) }
                                        } else null,
                                        onAddEnabled = currentAmount != card.deckLimit,
                                        onClick = {
                                            navController.navigate(
                                                "deck/card/${card.code}"
                                            ) {
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (deck.previousId != null && changedCards.value != null) item {
                        DeckCardsTypeCard(
                            showIcon = false,
                            label = stringResource(R.string.deck_changes),
                        ) {
                            changedCards.value!!.forEach { (textId, cards) ->
                                when (textId) {
                                    R.string.deck_changes_added -> if (cards.isNotEmpty()) {
                                        DeckChangesHeader(textId)
                                        cards.forEach { card ->
                                            key(card.id) {
                                                CardListItem(
                                                    tabooId = card.tabooId,
                                                    aspectId = card.aspectId,
                                                    aspectShortName = card.aspectShortName,
                                                    cost = card.cost,
                                                    imageSrc = card.realImageSrc,
                                                    approachConflict = card.approachConflict,
                                                    approachConnection = card.approachConnection,
                                                    approachReason = card.approachReason,
                                                    approachExploration = card.approachExploration,
                                                    name = card.name.toString(),
                                                    typeName = card.typeName,
                                                    traits = card.traits,
                                                    level = card.level,
                                                    isDarkTheme = isDarkTheme,
                                                    charForAmount = "+",
                                                    currentAmount = deck.addedCards[card.code],
                                                    onClick = {
                                                        navController.navigate(
                                                            "deck/card/${card.code}"
                                                        ) {
                                                            launchSingleTop = true
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    R.string.deck_changes_removed -> if (cards.isNotEmpty()) {
                                        DeckChangesHeader(textId)
                                        cards.forEach { card ->
                                            key(card.id) {
                                                CardListItem(
                                                    tabooId = card.tabooId,
                                                    aspectId = card.aspectId,
                                                    aspectShortName = card.aspectShortName,
                                                    cost = card.cost,
                                                    imageSrc = card.realImageSrc,
                                                    approachConflict = card.approachConflict,
                                                    approachConnection = card.approachConnection,
                                                    approachReason = card.approachReason,
                                                    approachExploration = card.approachExploration,
                                                    name = card.name.toString(),
                                                    typeName = card.typeName,
                                                    traits = card.traits,
                                                    level = card.level,
                                                    isDarkTheme = isDarkTheme,
                                                    currentAmount = deck.removedCards[card.code],
                                                    onClick = {
                                                        navController.navigate(
                                                            "deck/card/${card.code}"
                                                        ) {
                                                            launchSingleTop = true
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    R.string.deck_changes_added_collection -> if (cards.isNotEmpty()) {
                                        DeckChangesHeader(textId)
                                        cards.forEach { card ->
                                            key(card.id) {
                                                CardListItem(
                                                    tabooId = card.tabooId,
                                                    aspectId = card.aspectId,
                                                    aspectShortName = card.aspectShortName,
                                                    cost = card.cost,
                                                    imageSrc = card.realImageSrc,
                                                    approachConflict = card.approachConflict,
                                                    approachConnection = card.approachConnection,
                                                    approachReason = card.approachReason,
                                                    approachExploration = card.approachExploration,
                                                    name = card.name.toString(),
                                                    typeName = card.typeName,
                                                    traits = card.traits,
                                                    level = card.level,
                                                    isDarkTheme = isDarkTheme,
                                                    charForAmount = "+",
                                                    currentAmount = deck.addedCollectionCards[card.code],
                                                    onClick = {
                                                        navController.navigate(
                                                            "deck/card/${card.code}"
                                                        ) {
                                                            launchSingleTop = true
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    R.string.deck_changes_returned_collection -> if (cards.isNotEmpty()) {
                                        DeckChangesHeader(textId)
                                        cards.forEach { card ->
                                            key(card.id) {
                                                CardListItem(
                                                    tabooId = card.tabooId,
                                                    aspectId = card.aspectId,
                                                    aspectShortName = card.aspectShortName,
                                                    cost = card.cost,
                                                    imageSrc = card.realImageSrc,
                                                    approachConflict = card.approachConflict,
                                                    approachConnection = card.approachConnection,
                                                    approachReason = card.approachReason,
                                                    approachExploration = card.approachExploration,
                                                    name = card.name.toString(),
                                                    typeName = card.typeName,
                                                    traits = card.traits,
                                                    level = card.level,
                                                    isDarkTheme = isDarkTheme,
                                                    currentAmount = deck.returnedCollectionCards[card.code],
                                                    onClick = {
                                                        navController.navigate(
                                                            "deck/card/${card.code}"
                                                        ) {
                                                            launchSingleTop = true
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                val isTabooSet = deck.tabooSetId != null
                val locale = Locale.getDefault().language.substring(0..1)
                val supportedLocale = if (SUPPORTED_LANGUAGES.contains(locale)) locale
                else ""
                DeckRightSideDrawer(
                    isOpen = drawerOpen,
                    onClick = { drawerOpen = !drawerOpen },
                    isOwner = user == null || user.uid == deck.userId || deck.userId.isEmpty(),
                    deckName = deck.name,
                    deckId = if (deck.uploaded) deckId else null,
                    changeName = { showInputDialog = DialogWithInputType.Name },
                    setTaboo = { coroutine.launch { showLoadingDialog = true
                        deckViewModel.setDeckTaboo(!isTabooSet, user, deckProblems.value.first)
                    }.invokeOnCompletion { showLoadingDialog = false
                        deckViewModel.loadDeck(deckId)
                    } },
                    isTabooSet = isTabooSet,
                    toNotes = { /*TODO:Implement notes*/ },
                    toCharts = { /*TODO:Implement charts*/ },
                    camp = if (deck.nextId == null) {{ if (deckProblems.value.first.orEmpty().isNotEmpty()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.campaign_section_camp_warning),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else coroutine.launch { showLoadingDialog = true
                            deckViewModel.camp(user, deckProblems.value.first)
                        }.invokeOnCompletion { showLoadingDialog = false
                            if (deckViewModel.deckToOpen.value != null) navController.navigate(
                                "deck/${deckViewModel.deckToOpen.value}"
                            ) {
                                popUpTo(navController.previousBackStackEntry?.destination?.id!!) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                            else navController.navigateUp()
                        } }} else null,
                    toPreviousDeck = if (deck.previousId != null) {{ navController.navigate(
                        "deck/${deck.previousId}"
                    ) {
                        popUpTo(navController.previousBackStackEntry?.destination?.id!!) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    } }} else null,
                    toNextDeck = if (deck.nextId != null) {{ navController.navigate(
                        "deck/${deck.nextId}"
                    ) {
                        popUpTo(navController.previousBackStackEntry?.destination?.id!!) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    } }} else null,
                    toDeckHistory = if (deck.previousId != null || deck.nextId != null) {
                        { navController.navigate("deck/history") { launchSingleTop = true } }
                    } else null,
                    cloneDeck = { showInputDialog = DialogWithInputType.Clone },
                    upload = if (user == null) null
                    else if (deck.uploaded) {{
                        deckViewModel.openLink(if (supportedLocale.isNotEmpty() && supportedLocale != "en")
                            "https://$supportedLocale.$deckLink/$deckId"
                        else "https://$deckLink/$deckId", context
                        )
                    }}
                    else {{
                        if (deck.nextId != null || deck.previousId != null) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.options_section_upload_deck_warning),
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else if (deck.campaignId != null) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.options_section_delete_deck_warning),
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else coroutine.launch { showLoadingDialog = true
                            deckViewModel.uploadDeck(user)
                        }.invokeOnCompletion { showLoadingDialog = false
                            if (deckViewModel.deckToOpen.value != null) navController.navigate(
                                "deck/${deckViewModel.deckToOpen.value}"
                            ) {
                                popUpTo(BottomNavScreen.Decks.startDestination) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                            else navController.navigateUp() }
                    }},
                    url = if (deck.uploaded) {
                        if (supportedLocale.isNotEmpty() && supportedLocale != "en")
                            "https://$supportedLocale.$deckLink/$deckId"
                        else "https://$deckLink/$deckId"
                    } else null,
                    deleteDeck = { showActionDialog = DialogType.Delete }
                )
            }
        }
    }
}

@Composable
fun DeckChangesHeader(@StringRes textId: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(textId),
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        HorizontalDivider(color = CustomTheme.colors.l10)
    }
}