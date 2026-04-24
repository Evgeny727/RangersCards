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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
import com.rangerscards.objects.DeckMetaMaps
import com.rangerscards.objects.StarterDecks
import com.rangerscards.ui.components.CustomTab
import com.rangerscards.ui.components.DataPicker
import com.rangerscards.ui.components.RangersRadioButton
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.decks.components.StarterDeck
import com.rangerscards.ui.settings.UserUIState
import com.rangerscards.ui.settings.components.RangersBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

enum class ActiveField {
    FieldOne,
    FieldTwo,
    FieldThree
}

@Composable
fun DeckCreationScreen(
    onCancel: () -> Unit,
    onCreate: (String) -> Unit,
    decksViewModel: DecksViewModel,
    user: UserUIState,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var isCreating by remember { mutableStateOf(false) }
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var isUploading by rememberSaveable { mutableStateOf(false) }
    var taboo by rememberSaveable { mutableStateOf(user.settings.taboo) }
    var packIds = remember { user.settings.collection.toMutableStateList() }
    var selectedStarterDeck by rememberSaveable { mutableIntStateOf(-1) }
    var background by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("") }
    var role by remember { mutableStateOf("" to "") }
    var showDialogPicker by rememberSaveable { mutableStateOf<ActiveField?>(null) }
    val isLegit by remember {
        derivedStateOf {
            selectedStarterDeck >= 0 ||
                    (background.isNotEmpty() && specialty.isNotEmpty() && role.first.isNotEmpty())
        }
    }
    val context = LocalContext.current
    LaunchedEffect(user.settings) {
        packIds = user.settings.collection.toMutableStateList()
    }
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
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
                TabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = tabIndex,
                    containerColor = CustomTheme.colors.l20,
                    contentColor = CustomTheme.colors.d30,
                    // Custom indicator
                    indicator = { tabPositions ->
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[tabIndex]),
                            color = CustomTheme.colors.d30,
                            shape = AbsoluteRoundedCornerShape(topLeft = 50.dp, topRight = 50.dp),
                            width = tabPositions[tabIndex].contentWidth
                        )
                    },
                    divider = {
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                ) {
                    listOf(
                        R.string.custom_deck_tab,
                        R.string.starter_deck_tab
                    ).forEachIndexed { index, titleResId ->
                        CustomTab(
                            titleResId = titleResId,
                            selected = tabIndex == index,
                            onClick = {
                                tabIndex = index
                                if (index == 0) selectedStarterDeck = -1
                            }
                        )
                    }
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            value = name,
                            onValueChange = { name = it },
                            label = {
                                Text(text = stringResource(R.string.deck_creation_name_label))
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
                        1 -> item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.starter_deck_title),
                                    color = CustomTheme.colors.d30,
                                    fontFamily = Jost,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 20.sp,
                                    lineHeight = 22.sp,
                                )
                                StarterDecks.starterDecks().forEachIndexed { index, starterDeck ->
                                    val starterRole by decksViewModel.getCard(
                                        starterDeck.meta.role
                                    ).collectAsState(null)
                                    if (starterRole != null) key("starterDeck - $index") {
                                        StarterDeck(
                                            onclick = { selectedStarterDeck = index },
                                            isSelected = selectedStarterDeck == index,
                                            imageSrc = starterRole!!.realImageSrc.toString(),
                                            name = starterRole!!.name.toString(),
                                            starterDeck = starterDeck,
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { taboo = !taboo },
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.use_taboo),
                                color = CustomTheme.colors.d30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                            RangersRadioButton(
                                selected = taboo,
                                onClick = { taboo = !taboo },
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    item {
                        if (user.currentUser != null && decksViewModel.isConnected(context.applicationContext)) Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { isUploading = !isUploading },
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
                                selected = isUploading,
                                onClick = { isUploading = !isUploading },
                                modifier = Modifier.size(32.dp)
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
                    onClick = onCancel,
                    buttonColor = ButtonDefaults.buttonColors()
                        .copy(CustomTheme.colors.warn),
                    iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                val postfix = stringResource(R.string.starter_deck_name_postfix)
                SquareButton(
                    stringId = R.string.create_deck_button,
                    leadingIcon = R.drawable.add_32dp,
                    onClick = {
                        coroutine.launch {
                            isCreating = true
                            decksViewModel.createDeck(
                                name = name,
                                background = background,
                                specialty = specialty,
                                role = role.first,
                                isUploading = isUploading,
                                starterDeckId = selectedStarterDeck,
                                postfix = postfix,
                                user = user,
                                taboo = taboo,
                                context = context
                            )
                        }.invokeOnCompletion {
                            onCreate.invoke(decksViewModel.deckIdToOpen.value)
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
