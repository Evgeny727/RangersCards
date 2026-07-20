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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.domain.model.Deck
import com.rangerscards.domain.model.RoleCard
import com.rangerscards.domain.model.UserSettings
import com.rangerscards.objects.DeckMetaMaps
import com.rangerscards.ui.components.DataPicker
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.decks.ActiveField
import com.rangerscards.ui.decks.DeckCreationViewModel
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings

@Composable
fun DeckChangingRole(
    onBack: () -> Unit,
    deckViewModel: DeckViewModel,
    deck: Deck,
    userSettings: UserSettings,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    deckCreationViewModel: DeckCreationViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val deckRole by deckViewModel.deckRole.collectAsState()
    val taboo by rememberSaveable { mutableStateOf(deck.tabooSetId != null) }
    val packIds = remember(userSettings.collection) { userSettings.collection }
    var background by rememberSaveable { mutableStateOf(deck.deckMeta.background) }
    var specialty by rememberSaveable { mutableStateOf(deck.deckMeta.specialty) }
    var role by remember { mutableStateOf(deck.deckMeta.roleId to (deckRole?.name ?: "")) }
    var showDialogPicker by rememberSaveable { mutableStateOf<ActiveField?>(null) }
    val isLegit by remember {
        derivedStateOf {
            (background.isNotEmpty() && specialty.isNotEmpty() && role.first.isNotEmpty())
        }
    }
    val roles = deckCreationViewModel.getRoles(specialty, taboo, listOf("core") + packIds).collectAsLazyPagingItems()

    if (showDialogPicker != null) RangersDialogWithContent(
        headerId = when(showDialogPicker) {
            ActiveField.FieldOne -> R.string.background
            ActiveField.FieldTwo -> R.string.specialty
            else -> R.string.role
        },
        isDarkTheme = isDarkTheme,
        onBack = { showDialogPicker = null },
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
                    key = roles.itemKey(RoleCard::id),
                    contentType = roles.itemContentType { it::class }
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

    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = modifier.applyScaffoldPaddings(contentPadding),
        topBar = {
            RangersTopAppBar(
                title = "",
                canNavigateBack = true,
                navigateUp = onBack,
                actions = null,
                switch = null
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize()
                .applyScaffoldPaddings(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(CustomTheme.colors.l30)
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(background) {
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
                item(specialty) {
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
                item(role.first) {
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
                    onClick = onBack,
                    buttonColor = ButtonDefaults.buttonColors()
                        .copy(CustomTheme.colors.warn),
                    iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                SquareButton(
                    stringId = R.string.done_button,
                    leadingIcon = R.drawable.done_32dp,
                    onClick = {
                        if (deck.deckMeta.roleId != role.first || deck.deckMeta.background != background
                            || deck.deckMeta.specialty != specialty)
                                deckViewModel.changeRole(background, specialty, role.first)
                        onBack()
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
    }
}