package com.rangerscards.ui.decks

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.domain.model.DeckMeta
import com.rangerscards.domain.model.RoleCard
import com.rangerscards.domain.model.User
import com.rangerscards.objects.DeckMetaMaps
import com.rangerscards.objects.StarterDecks
import com.rangerscards.ui.components.DataPicker
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.RangersLoadingDialog
import com.rangerscards.ui.components.ScrollableRangersTabs
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.decks.components.StarterDeck
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings

enum class ActiveField {
    FieldOne,
    FieldTwo,
    FieldThree
}

@Composable
fun DeckCreationScreen(
    onCancel: () -> Unit,
    onCreate: (String) -> Unit,
    emitError: (Throwable) -> Unit,
    user: User,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    deckCreationViewModel: DeckCreationViewModel = hiltViewModel(),
) {
    val deckCreationUiState by deckCreationViewModel.deckCreationUiState.collectAsState()
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var isUploading by rememberSaveable { mutableStateOf(false) }
    var taboo by rememberSaveable(user.settings.taboo) { mutableStateOf(user.settings.taboo) }
    val packIds = remember(user.settings.collection) { user.settings.collection.toMutableStateList() }
    var selectedStarterDeck by rememberSaveable { mutableIntStateOf(-1) }
    var background by remember { mutableStateOf("" to "") }
    var specialty by remember { mutableStateOf("" to "") }
    var role by remember { mutableStateOf("" to "") }
    var showDialogPicker by rememberSaveable { mutableStateOf<ActiveField?>(null) }
    val isLegit by remember {
        derivedStateOf {
            selectedStarterDeck >= 0 ||
                    (background.first.isNotEmpty() && specialty.first.isNotEmpty() && role.first.isNotEmpty())
        }
    }
    val roles = deckCreationViewModel.getRoles(
        specialty.first,
        taboo,
        listOf("core") + packIds
    ).collectAsLazyPagingItems()

    LaunchedEffect(deckCreationUiState) {
        when (val state = deckCreationUiState) {
            is DeckCreationUiState.Success -> onCreate(state.deckId)
            is DeckCreationUiState.Error -> onCancel()
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        deckCreationViewModel.events.collect {
            emitError(it.exception)
        }
    }

    if (deckCreationUiState is DeckCreationUiState.Loading) RangersLoadingDialog(isDarkTheme)

    if (showDialogPicker != null) RangersDialogWithContent(
        headerId = when (showDialogPicker) {
            ActiveField.FieldOne -> R.string.background
            ActiveField.FieldTwo -> R.string.specialty
            else -> R.string.role
        },
        isDarkTheme = isDarkTheme,
        onBack = { showDialogPicker = null },
    ) {
        LazyColumn(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
            when (showDialogPicker) {
                ActiveField.FieldOne -> DeckMetaMaps.background.forEach { (key, value) ->
                    item {
                        val localizedValue = stringResource(value)
                        Text(
                            text = localizedValue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    background = key to localizedValue
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
                        val localizedValue = stringResource(value)
                        Text(
                            text = localizedValue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    specialty = key to localizedValue
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

                else -> if (roles.itemCount <= 0) item("no_roles") {
                    Text(
                        text = stringResource(R.string.no_roles),
                        modifier = Modifier
                            .fillMaxWidth()
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
                } else items(
                    count = roles.itemCount,
                    key = roles.itemKey(RoleCard::id),
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
    }

    Column(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
    ) {
        Column(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScrollableRangersTabs(
                listOf(
                    R.string.custom_deck_tab,
                    R.string.starter_deck_tab
                ),
                tabIndex
            ) { index ->
                tabIndex = index
                if (index == 0) selectedStarterDeck = -1
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text(text = stringResource(R.string.name_label))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.deck_creation_name_placeholder))
                        },
                        textStyle = TextStyle(
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                        ),
                        singleLine = true,
                        shape = CustomTheme.shapes.small,
                        colors = TextFieldDefaults.colors().copy(
                            focusedIndicatorColor = CustomTheme.colors.m,
                            unfocusedIndicatorColor = CustomTheme.colors.m,
                            unfocusedLabelColor = CustomTheme.colors.d30,
                            focusedLabelColor = CustomTheme.colors.d30,
                            unfocusedPlaceholderColor = CustomTheme.colors.d30,
                            focusedPlaceholderColor = CustomTheme.colors.d30,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
                when (tabIndex) {
                    0 -> {
                        item("background_$background") {
                            DataPicker(
                                onClick = {
                                    showDialogPicker = ActiveField.FieldOne
                                },
                                type = R.string.background,
                            ) {
                                Text(
                                    text = background.second.ifEmpty { stringResource(R.string.background_placeholder) },
                                    color = CustomTheme.colors.d30,
                                    fontFamily = Jost,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        item("specialty_$specialty") {
                            DataPicker(
                                onClick = {
                                    showDialogPicker = ActiveField.FieldTwo
                                },
                                type = R.string.specialty
                            ) {
                                Text(
                                    text = specialty.second.ifEmpty { stringResource(R.string.specialty_placeholder) },
                                    color = CustomTheme.colors.d30,
                                    fontFamily = Jost,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        item("role_${role.first}") {
                            AnimatedVisibility(specialty.first.isNotEmpty()) {
                                DataPicker(
                                    onClick = {
                                        showDialogPicker = ActiveField.FieldThree
                                    },
                                    type = R.string.role
                                ) {
                                    Text(
                                        text = role.second.ifEmpty { stringResource(R.string.role_placeholder) },
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

                    1 -> {
                        item("starter_deck_header") {
                            Text(
                                text = stringResource(R.string.starter_deck_title),
                                color = CustomTheme.colors.d30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        itemsIndexed(
                            StarterDecks.starterDecks(),
                            key = { index, _ -> "starterDeck - $index" }
                        ) { index, starterDeck ->
                            val starterRole by deckCreationViewModel.getRoleCard(
                                starterDeck.meta.roleId, false
                            ).collectAsState(null)
                            StarterDeck(
                                onclick = { backgroundLocalized, specialtyLocalized ->
                                    background = background.first to backgroundLocalized
                                    specialty = specialty.first to specialtyLocalized
                                    selectedStarterDeck = index
                                },
                                isSelected = selectedStarterDeck == index,
                                imageSrc = starterRole?.realImageSrc,
                                name = starterRole?.name,
                                starterDeck = starterDeck,
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
                item("taboo_row") {
                    RangersRadioButtonRow(
                        text = stringResource(R.string.use_taboo),
                        isSelected = taboo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) { value ->
                        taboo = value
                    }
                }
                if (user.userInfo?.id != null) item("upload_row") {
                    RangersRadioButtonRow(
                        text = stringResource(R.string.upload_to_rangersdb),
                        isSelected = isUploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) { value ->
                        isUploading = value
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            val postfix = stringResource(R.string.starter_deck_name_postfix)
            SquareButton(
                stringId = R.string.create_deck_button,
                leadingIcon = R.drawable.add_32dp,
                onClick = {
                    val deckMeta = if (selectedStarterDeck >= 0) null else DeckMeta(
                        roleId = role.first,
                        background = background.first,
                        specialty = specialty.first
                    )
                    deckCreationViewModel.createDeck(
                        name = name,
                        deckMeta = deckMeta,
                        backgroundLocalized = background.second,
                        specialtyLocalized = specialty.second,
                        isUploading = isUploading,
                        starterDeckId = selectedStarterDeck,
                        postfix = postfix,
                        taboo = taboo,
                    )
                },
                buttonColor = ButtonDefaults.buttonColors().copy(
                    containerColor = CustomTheme.colors.d10,
                    disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.25f)
                ),
                iconColor = CustomTheme.colors.m,
                textColor = CustomTheme.colors.l30,
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                isEnabled = isLegit
            )
        }
    }
}
