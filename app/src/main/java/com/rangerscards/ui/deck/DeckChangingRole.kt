package com.rangerscards.ui.deck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.domain.model.User
import com.rangerscards.objects.DeckMetaMaps
import com.rangerscards.ui.components.DataPicker
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.decks.ActiveField
import com.rangerscards.ui.decks.DecksViewModel
import com.rangerscards.ui.settings.components.RangersBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import kotlinx.coroutines.launch

@Composable
fun DeckChangingRole(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    deckViewModel: DeckViewModel,
    user: User,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val deck by deckViewModel.originalDeck.collectAsState()
    val deckRole by deckViewModel.role.collectAsState()
    var isCreating by remember { mutableStateOf(false) }
    val taboo by rememberSaveable { mutableStateOf(deck!!.tabooSetId != null) }
    val packIds = remember(user.settings.collection) { user.settings.collection.toMutableStateList() }
    var background by rememberSaveable { mutableStateOf(deck!!.background) }
    var specialty by rememberSaveable { mutableStateOf(deck!!.specialty) }
    var role by remember { mutableStateOf((if (deck?.roleId.toString() == "null") "" else deck!!.roleId)
            to (deckRole?.name ?: "")) }
    var showDialogPicker by rememberSaveable { mutableStateOf<ActiveField?>(null) }
    val isLegit by remember {
        derivedStateOf {
            (background.isNotEmpty() && specialty.isNotEmpty() && role.first.isNotEmpty())
        }
    }
    LaunchedEffect(deck?.id) {
        if (deck == null) onCancel.invoke()
    }
    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = modifier.applyScaffoldPaddings(contentPadding),
        topBar = {
            RangersTopAppBar(
                title = "",
                canNavigateBack = true,
                navigateUp = onCancel,
                actions = null,
                switch = null
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding()
                ),
        ) {
            val coroutine = rememberCoroutineScope()
            if (!isCreating) {
                Column(
                    modifier = modifier
                        .background(CustomTheme.colors.l30)
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val roles = decksViewModel.getRoles(specialty, taboo, listOf("core") + packIds).collectAsLazyPagingItems()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                key(background) {
                                    DataPicker(
                                        onClick = { showDialogPicker = ActiveField.FieldOne },
                                        type = R.string.background
                                    ) {
                                        Text(
                                            text = stringResource(if (background.isEmpty())
                                                R.string.background_placeholder
                                            else DeckMetaMaps.background[background]!!),
                                            color = CustomTheme.colors.d30,
                                            fontFamily = Jost,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                key(specialty) {
                                    DataPicker(
                                        onClick = { showDialogPicker = ActiveField.FieldTwo },
                                        type = R.string.specialty
                                    ) {
                                        Text(
                                            text = stringResource(if (specialty.isEmpty())
                                                R.string.specialty_placeholder
                                            else DeckMetaMaps.specialty[specialty]!!),
                                            color = CustomTheme.colors.d30,
                                            fontFamily = Jost,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                key(role.first) {
                                    AnimatedVisibility(specialty.isNotEmpty()) {
                                        DataPicker(
                                            onClick = { showDialogPicker = ActiveField.FieldThree },
                                            type = R.string.role
                                        ) {
                                            Text(
                                                text = if (role.first.isEmpty())
                                                    stringResource(R.string.role_placeholder)
                                                else role.second,
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
                                if (showDialogPicker != null) Dialog(
                                    onDismissRequest = { showDialogPicker = null },
                                    properties = DialogProperties(
                                        dismissOnBackPress = true,
                                        dismissOnClickOutside = true,
                                        usePlatformDefaultWidth = false
                                    )
                                ) {
                                    RangersBaseCard(
                                        isDarkTheme = isDarkTheme,
                                        labelIdRes = when(showDialogPicker) {
                                            ActiveField.FieldOne -> R.string.background
                                            ActiveField.FieldTwo -> R.string.specialty
                                            else -> R.string.role
                                        }
                                    ) {
                                        LazyColumn(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
                                            when(showDialogPicker) {
                                                ActiveField.FieldOne -> DeckMetaMaps.background.forEach { (key, value) ->
                                                    item {
                                                        Text(
                                                            text = stringResource(value),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    background = key
                                                                    showDialogPicker = null
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
                                                ActiveField.FieldTwo -> DeckMetaMaps.specialty.forEach { (key, value) ->
                                                    item {
                                                        Text(
                                                            text = stringResource(value),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    specialty = key
                                                                    showDialogPicker = null
                                                                    role = "" to ""
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
                                                else -> if (roles.itemCount <= 0) item("no_roles") {
                                                    Text(
                                                        text = stringResource(R.string.no_roles),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                                        color = CustomTheme.colors.d30,
                                                        fontFamily = Jost,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 18.sp,
                                                        lineHeight = 22.sp,
                                                    )
                                                } else items(
                                                    count = roles.itemCount,
                                                    key = roles.itemKey(CardListItemProjection::id),
                                                    contentType = roles.itemContentType { it }
                                                ) { index ->
                                                    val item = roles[index] ?: return@items
                                                    Text(
                                                        text = item.name.toString(),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                role = item.code to item.name.toString()
                                                                showDialogPicker = null
                                                            }
                                                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SquareButton(
                        stringId = R.string.cancel_button,
                        leadingIcon = R.drawable.close_32dp,
                        onClick = onCancel,
                        buttonColor = ButtonDefaults.buttonColors()
                            .copy(CustomTheme.colors.warn),
                        iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    SquareButton(
                        stringId = R.string.done_button,
                        leadingIcon = R.drawable.done_32dp,
                        onClick = { if (deck!!.roleId == role.first && deck!!.background == background
                            && deck!!.specialty == specialty) onCancel.invoke()
                        else coroutine.launch {
                            isCreating = true
                            deckViewModel.changeRole(
                                background = background,
                                specialty = specialty,
                                role = role.first,
                                user = user.currentUser,
                                problems = deck!!.problems
                            )
                            deckViewModel.loadDeck(deck!!.id)
                        }.invokeOnCompletion {
                            onSave.invoke()
                        }
                        },
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            containerColor = CustomTheme.colors.d10,
                            disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.25f)
                        ),
                        iconColor = CustomTheme.colors.m,
                        textColor = CustomTheme.colors.l30,
                        modifier = Modifier.weight(1.1f).fillMaxHeight(),
                        isEnabled = isLegit
                    )
                }
            }
            else Column(
                modifier = modifier
                    .background(CustomTheme.colors.l30)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = CustomTheme.colors.m)
            }
        }
    }
}