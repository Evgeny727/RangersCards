package com.rangerscards.ui.components

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.CardFilterOptions
import com.rangerscards.objects.CardFilters
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings

enum class Section {
    TYPES,
    TRAITS,
    SETS,
    COST,
    APPROACHES,
    PACKS,
    ASPECTS
}

@Composable
fun CardsFilterScreen(
    navigateUp: () -> Unit,
    clearFilterOptions: () -> Unit,
    filterOptions: CardFilterOptions,
    onApply: (CardFilterOptions) -> Unit,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var localFilterOptions by remember { mutableStateOf(filterOptions) }
    val expandedMap = remember {
        mutableStateMapOf<Section, Boolean>().apply {
            // initialize all sections as collapsed (false)
            Section.entries.forEach { this[it] = false }
        }
    }
    val innerStates = List(4) { rememberLazyListState() }
    val innerConnections = innerStates.map { _ ->
        remember {
            object : NestedScrollConnection {
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    // let the inner list scroll first, then eat all leftover
                    return available
                }
                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    return available
                }
            }
        }
    }
    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = Modifier.applyScaffoldPaddings(contentPadding),
        topBar = {
            RangersTopAppBar(
                title = stringResource(R.string.filters_screen_header),
                canNavigateBack = true,
                navigateUp = navigateUp,
                actions = {
                    IconButton(
                        onClick = clearFilterOptions,
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.delete_32dp),
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
        Column(
            modifier = Modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize()
                .applyScaffoldPaddings(innerPadding),
        ) {
            val context = LocalContext.current
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        val isExpanded = expandedMap[Section.TYPES] == true
                        FilterHeader(
                            onClick = {
                                expandedMap[Section.TYPES] = !(expandedMap[Section.TYPES] ?: false)
                            },
                            headerResId = R.string.types_filter_header,
                            isExpanded = isExpanded
                        )
                        if (isExpanded) {
                            val selectedTypes = remember(localFilterOptions.types) {
                                localFilterOptions.types.toSet()
                            }
                            CardsFilterCheckList(
                                optionsMap = context.transformMapOfFiltersToSortedExtractedString(
                                    CardFilters.getTypesFilters()
                                ),
                                selectedOptions = selectedTypes,
                                state = innerStates[0],
                                modifier = Modifier.nestedScroll(innerConnections[0])
                            ) { isSelected, key ->
                                localFilterOptions = localFilterOptions.copy(
                                    types = if (isSelected) {
                                        localFilterOptions.types + key
                                    } else {
                                        localFilterOptions.types - key
                                    }
                                )
                            }
                        }
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        val isExpanded = expandedMap[Section.TRAITS] == true
                        FilterHeader(
                            onClick = {
                                expandedMap[Section.TRAITS] =
                                    !(expandedMap[Section.TRAITS] ?: false)
                            },
                            headerResId = R.string.traits_filter_header,
                            isExpanded = isExpanded
                        )
                        if (isExpanded) {
                            val selectedTraits = remember(localFilterOptions.traits) {
                                localFilterOptions.traits.toSet()
                            }
                            CardsFilterCheckList(
                                optionsMap = context.transformMapOfFiltersToSortedExtractedString(
                                    CardFilters.getTraitsFilters()
                                ),
                                selectedOptions = selectedTraits,
                                state = innerStates[1],
                                modifier = Modifier.nestedScroll(innerConnections[1])
                            ) { isSelected, key ->
                                localFilterOptions = localFilterOptions.copy(
                                    traits = if (isSelected) {
                                        localFilterOptions.traits + key
                                    } else {
                                        localFilterOptions.traits - key
                                    }
                                )
                            }
                        }
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        val isExpanded = expandedMap[Section.SETS] == true
                        FilterHeader(
                            onClick = {
                                expandedMap[Section.SETS] = !(expandedMap[Section.SETS] ?: false)
                            },
                            headerResId = R.string.sets_filter_header,
                            isExpanded = isExpanded
                        )
                        if (isExpanded) {
                            val selectedSets = remember(localFilterOptions.sets) {
                                localFilterOptions.sets.toSet()
                            }
                            CardsFilterCheckList(
                                optionsMap = context.transformMapOfFiltersToSortedExtractedString(
                                    CardFilters.getSetsFilters()
                                ),
                                selectedOptions = selectedSets,
                                state = innerStates[2],
                                modifier = Modifier.nestedScroll(innerConnections[2])
                            ) { isSelected, key ->
                                localFilterOptions = localFilterOptions.copy(
                                    sets = if (isSelected) {
                                        localFilterOptions.sets + key
                                    } else {
                                        localFilterOptions.sets - key
                                    }
                                )
                            }
                        }
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        val isExpanded = expandedMap[Section.COST] == true
                        FilterHeader(
                            onClick = {
                                expandedMap[Section.COST] = !(expandedMap[Section.COST] ?: false)
                            },
                            headerResId = R.string.cost_filter_header,
                            isExpanded = isExpanded
                        )
                        if (isExpanded) {
                            CardsFilterCost(localFilterOptions.costRange) { newRange ->
                                localFilterOptions = localFilterOptions.copy(costRange = newRange)
                            }
                        }
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        val isExpanded = expandedMap[Section.APPROACHES] == true
                        FilterHeader(
                            onClick = {
                                expandedMap[Section.APPROACHES] =
                                    !(expandedMap[Section.APPROACHES] ?: false)
                            },
                            headerResId = R.string.approaches_filter_header,
                            isExpanded = isExpanded
                        )
                        if (isExpanded) {
                            val selectedApproaches = remember(localFilterOptions.approaches) {
                                localFilterOptions.approaches
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                            ) {
                                RangersRadioButtonRow(
                                    text = stringResource(R.string.approaches_filter_conflict),
                                    leadingIcon = R.drawable.conflict,
                                    iconTint = CustomTheme.colors.d30,
                                    isSelected = selectedApproaches.conflict,
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 18.sp,
                                        lineHeight = 20.sp,
                                    ),
                                ) { value ->
                                    localFilterOptions = localFilterOptions.copy(
                                        approaches = selectedApproaches.copy(conflict = value)
                                    )
                                }
                                HorizontalDivider(color = CustomTheme.colors.l10)
                                RangersRadioButtonRow(
                                    text = stringResource(R.string.approaches_filter_connection),
                                    leadingIcon = R.drawable.connection,
                                    iconTint = CustomTheme.colors.d30,
                                    isSelected = selectedApproaches.connection,
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 18.sp,
                                        lineHeight = 20.sp,
                                    ),
                                ) { value ->
                                    localFilterOptions = localFilterOptions.copy(
                                        approaches = selectedApproaches.copy(connection = value)
                                    )
                                }
                                HorizontalDivider(color = CustomTheme.colors.l10)
                                RangersRadioButtonRow(
                                    text = stringResource(R.string.approaches_filter_exploration),
                                    leadingIcon = R.drawable.exploration,
                                    iconTint = CustomTheme.colors.d30,
                                    isSelected = selectedApproaches.exploration,
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 18.sp,
                                        lineHeight = 20.sp,
                                    ),
                                ) { value ->
                                    localFilterOptions = localFilterOptions.copy(
                                        approaches = selectedApproaches.copy(exploration = value)
                                    )
                                }
                                HorizontalDivider(color = CustomTheme.colors.l10)
                                RangersRadioButtonRow(
                                    text = stringResource(R.string.approaches_filter_reason),
                                    leadingIcon = R.drawable.reason,
                                    iconTint = CustomTheme.colors.d30,
                                    isSelected = selectedApproaches.reason,
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 18.sp,
                                        lineHeight = 20.sp,
                                    ),
                                ) { value ->
                                    localFilterOptions = localFilterOptions.copy(
                                        approaches = selectedApproaches.copy(reason = value)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        val isExpanded = expandedMap[Section.PACKS] == true
                        FilterHeader(
                            onClick = {
                                expandedMap[Section.PACKS] = !(expandedMap[Section.PACKS] ?: false)
                            },
                            headerResId = R.string.packs_filter_header,
                            isExpanded = isExpanded
                        )
                        if (isExpanded) {
                            val selectedPacks = remember(localFilterOptions.packs) {
                                localFilterOptions.packs.toSet()
                            }
                            CardsFilterCheckList(
                                optionsMap = context.transformMapOfFiltersToSortedExtractedString(
                                    mapOf(
                                        "core" to R.string.core_cycle,
                                        "loa" to R.string.loa_expansion,
                                        "sotv" to R.string.sotv_expansion,
                                        "sib" to R.string.spire_in_bloom,
                                        "sos" to R.string.shadow_of_the_storm,
                                        "itm" to R.string.into_the_maw,
                                    ),
                                    false),
                                selectedOptions = selectedPacks,
                                state = innerStates[3],
                                modifier = Modifier.nestedScroll(innerConnections[3])
                            ) { isSelected, key ->
                                localFilterOptions = localFilterOptions.copy(
                                    packs = if (isSelected) {
                                        localFilterOptions.packs + key
                                    } else {
                                        localFilterOptions.packs - key
                                    }
                                )
                            }
                        }
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        val isExpanded = expandedMap[Section.ASPECTS] == true
                        FilterHeader(
                            onClick = {
                                expandedMap[Section.ASPECTS] =
                                    !(expandedMap[Section.ASPECTS] ?: false)
                            },
                            headerResId = R.string.aspects_filter_header,
                            isExpanded = isExpanded
                        )
                        if (isExpanded) {
                            val awa = remember(localFilterOptions.aspectRequirements) {
                                localFilterOptions.aspectRequirements.awa ?: 0
                            }
                            val spi = remember(localFilterOptions.aspectRequirements) {
                                localFilterOptions.aspectRequirements.spi ?: 0
                            }
                            val foc = remember(localFilterOptions.aspectRequirements) {
                                localFilterOptions.aspectRequirements.foc ?: 0
                            }
                            val fit = remember(localFilterOptions.aspectRequirements) {
                                localFilterOptions.aspectRequirements.fit ?: 0
                            }
                            val equalOrLess = remember(localFilterOptions.aspectRequirements) {
                                localFilterOptions.aspectRequirements.equalOrLower
                            }
                            Column(
                                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp),
                            ) {
                                RowWithEditableNumericValue(
                                    textResId = R.string.awa_styled_card_text,
                                    onMinusClicked = {
                                        localFilterOptions = localFilterOptions.copy(
                                            aspectRequirements = localFilterOptions.aspectRequirements.copy(
                                                awa = if (awa > 1) awa - 1 else null
                                            )
                                        )
                                    },
                                    isMinusEnabled = awa > 0,
                                    onPlusClicked = {
                                        localFilterOptions = localFilterOptions.copy(
                                            aspectRequirements = localFilterOptions.aspectRequirements.copy(
                                                awa = awa + 1
                                            )
                                        )
                                    },
                                    isPlusEnabled = awa < 9,
                                    numericValue = awa
                                )
                                RowWithEditableNumericValue(
                                    textResId = R.string.spi_styled_card_text,
                                    onMinusClicked = {
                                        localFilterOptions = localFilterOptions.copy(
                                            aspectRequirements = localFilterOptions.aspectRequirements.copy(
                                                spi = if (spi > 1) spi - 1 else null
                                            )
                                        )
                                    },
                                    isMinusEnabled = spi > 0,
                                    onPlusClicked = {
                                        localFilterOptions = localFilterOptions.copy(
                                            aspectRequirements = localFilterOptions.aspectRequirements.copy(
                                                spi = spi + 1
                                            )
                                        )
                                    },
                                    isPlusEnabled = spi < 9,
                                    numericValue = spi
                                )
                                RowWithEditableNumericValue(
                                    textResId = R.string.foc_styled_card_text,
                                    onMinusClicked = {
                                        localFilterOptions = localFilterOptions.copy(
                                            aspectRequirements = localFilterOptions.aspectRequirements.copy(
                                                foc = if (foc > 1) foc - 1 else null
                                            )
                                        )
                                    },
                                    isMinusEnabled = foc > 0,
                                    onPlusClicked = {
                                        localFilterOptions = localFilterOptions.copy(
                                            aspectRequirements = localFilterOptions.aspectRequirements.copy(
                                                foc = foc + 1
                                            )
                                        )
                                    },
                                    isPlusEnabled = foc < 9,
                                    numericValue = foc
                                )
                                RowWithEditableNumericValue(
                                    textResId = R.string.fit_styled_card_text,
                                    onMinusClicked = {
                                        localFilterOptions = localFilterOptions.copy(
                                            aspectRequirements = localFilterOptions.aspectRequirements.copy(
                                                fit = if (fit > 1) fit - 1 else null
                                            )
                                        )
                                    },
                                    isMinusEnabled = fit > 0,
                                    onPlusClicked = {
                                        localFilterOptions = localFilterOptions.copy(
                                            aspectRequirements = localFilterOptions.aspectRequirements.copy(
                                                fit = fit + 1
                                            )
                                        )
                                    },
                                    isPlusEnabled = fit < 9,
                                    numericValue = fit
                                )
                                RangersRadioButtonRow(
                                    text = stringResource(R.string.aspect_requirements_equal_or_lower),
                                    isSelected = equalOrLess,
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 18.sp,
                                        lineHeight = 22.sp,
                                    ),
                                ) { value ->
                                    localFilterOptions = localFilterOptions.copy(
                                        aspectRequirements = localFilterOptions.aspectRequirements
                                            .copy(equalOrLower = value)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = CustomTheme.colors.l10)
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
                    onClick = navigateUp,
                    buttonColor = ButtonDefaults.buttonColors()
                        .copy(CustomTheme.colors.warn),
                    iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                SquareButton(
                    stringId = R.string.apply_button,
                    leadingIcon = R.drawable.done_32dp,
                    onClick = { onApply(localFilterOptions) },
                    buttonColor = ButtonDefaults.buttonColors().copy(
                        containerColor = CustomTheme.colors.d10,
                    ),
                    iconColor = CustomTheme.colors.m,
                    textColor = CustomTheme.colors.l30,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
fun FilterHeader(
    onClick: () -> Unit,
    @StringRes headerResId: Int,
    isExpanded: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clickable { onClick() }
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = stringResource(headerResId),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 28.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Icon(
                painterResource(if (isExpanded) R.drawable.arrow_drop_up_32dp
                else R.drawable.arrow_drop_down_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.l10,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CardsFilterCheckList(
    optionsMap: Map<String, String>,
    selectedOptions: Set<String>,
    state: LazyListState,
    modifier: Modifier,
    onClick: (Boolean, String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredOptionsMap = (if (searchQuery.length >= 2)
        optionsMap.filter { it.value.contains(searchQuery, true) }
    else optionsMap)

    Row(
        modifier = Modifier.background(CustomTheme.colors.l20)
    ) {
        RangersSearchOutlinedField(
            query = searchQuery,
            placeholder = R.string.check_list_search_query_placeholder,
            onQueryChanged = { newQuery -> searchQuery = newQuery },
            onClearClicked = { searchQuery = "" }
        )
    }
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .sizeIn(maxHeight = 320.dp),
        state = state,
    ) {
        val entries = filteredOptionsMap.toList()
        itemsIndexed(entries, {_, it -> it.first}) { index, (key, value) ->
            val isSelected = key in selectedOptions
            val isLast = index == entries.lastIndex
            RangersRadioButtonRow(
                text = value,
                isSelected = isSelected,
                textStyle = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                ),
            ) { value ->
                onClick(value, key)
            }
            if (!isLast) HorizontalDivider(color = CustomTheme.colors.l10)
        }
    }
}

@Composable
fun CardsFilterCost(
    costRange: IntRange?,
    onRangeChanged: (IntRange) -> Unit
) {
    val startRange = remember(costRange) {
        (costRange?.start ?: 0).coerceAtLeast(0)
    }
    val endRange = remember(costRange) {
        costRange?.endInclusive ?: 9
    }
    val isXCostSelected = remember(costRange) {
        costRange == null || costRange.start == -2
    }
    Column(
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        RowWithEditableNumericValue(
            textResId = R.string.cost_filter_start_value,
            onMinusClicked = { onRangeChanged((startRange - 1)..endRange) },
            isMinusEnabled = startRange > 0,
            onPlusClicked = { onRangeChanged((startRange + 1)..endRange) },
            isPlusEnabled = startRange < endRange,
            numericValue = startRange
        )
        RowWithEditableNumericValue(
            textResId = R.string.cost_filter_end_value,
            onMinusClicked = { onRangeChanged((if (isXCostSelected) -2 else startRange)..(endRange - 1)) },
            isMinusEnabled = endRange > startRange,
            onPlusClicked = { onRangeChanged((if (isXCostSelected) -2 else startRange)..(endRange + 1)) },
            isPlusEnabled = endRange < 9,
            numericValue = endRange
        )
        RangersRadioButtonRow(
            text = stringResource(R.string.cost_filter_include_x_cost),
            isSelected = isXCostSelected,
            textStyle = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 22.sp,
            ),
        ) { value ->
            onRangeChanged((if (value) -2 else startRange)..endRange)
        }
    }
}

private fun Context.transformMapOfFiltersToSortedExtractedString(
    originalMap: Map<String, Int>,
    needSort: Boolean = true
): Map<String, String> {

    // Convert resource ids -> actual strings
    val converted: Map<String, String> = originalMap.mapValues { (_, resId) ->
        getString(resId)
    }

    // Sort by value (case-insensitive) and put into a LinkedHashMap to preserve order
    if (needSort) {
        val sortedList: List<Pair<String, String>> =
            converted.entries.map { it.key to it.value }.sortedBy { it.second.lowercase() }

        val result = LinkedHashMap<String, String>(sortedList.size)
        sortedList.forEach { (k, v) -> result[k] = v }
        return result
    }
    else return converted
}

