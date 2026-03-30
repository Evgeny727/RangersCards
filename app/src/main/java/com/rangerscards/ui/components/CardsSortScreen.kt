package com.rangerscards.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.objects.CardFilters
import com.rangerscards.objects.SortOption
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun CardsSortScreen(
    navigateUp: () -> Unit,
    clearSortOptions: () -> Unit,
    sortOptions: List<String>,
    onApply: (List<String>) -> Unit,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val sortOptionsMap = remember { CardFilters.getSortOptions() }
    val localSortOptionsList = remember {
        (sortOptions.map {
            SortOption(it, sortOptionsMap.getValue(it), true)
        } + sortOptionsMap.filter { !sortOptions.contains(it.key) }.map {
            SortOption(it.key, it.value)
        }).toMutableStateList()
    }
    val lazyState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyState) { from, to ->
        val moved = localSortOptionsList.removeAt(from.index)
        localSortOptionsList.add(to.index.coerceIn(0, localSortOptionsList.size), moved)
    }
    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = Modifier.padding(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding()
        ),
        topBar = {
            RangersTopAppBar(
                title = stringResource(R.string.sort_screen_header),
                canNavigateBack = true,
                navigateUp = navigateUp,
                actions = {
                    IconButton(
                        onClick = clearSortOptions,
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
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                ),
        ) {
            LazyColumn(
                state = lazyState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(localSortOptionsList, { _, it -> it.id }) { index, sortOption ->
                    ReorderableItem(reorderableState, sortOption.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Transparent,
                            tonalElevation = elevation,
                            shape = CustomTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {},
                                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                                        modifier = Modifier.size(32.dp).draggableHandle(),
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.drag_handle_32dp),
                                            contentDescription = null,
                                            tint = CustomTheme.colors.m,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Text(
                                        text = stringResource(sortOption.resId),
                                        color = CustomTheme.colors.d30,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 18.sp,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    RangersRadioButton(
                                        selected = sortOption.isActive,
                                        onClick = {
                                            localSortOptionsList[index] = sortOption.copy(isActive = !sortOption.isActive)
                                        },
                                        modifier = Modifier.size(32.dp),
                                    )
                                }
                                if (index != localSortOptionsList.lastIndex) HorizontalDivider(color = CustomTheme.colors.l10)
                            }
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
                    onClick = navigateUp,
                    buttonColor = ButtonDefaults.buttonColors()
                        .copy(CustomTheme.colors.warn),
                    iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                SquareButton(
                    stringId = R.string.apply_button,
                    leadingIcon = R.drawable.done_32dp,
                    onClick = { onApply(localSortOptionsList.filter { it.isActive }.map { it.id }) },
                    buttonColor = ButtonDefaults.buttonColors().copy(
                        containerColor = CustomTheme.colors.d10,
                    ),
                    iconColor = CustomTheme.colors.m,
                    textColor = CustomTheme.colors.l30,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

