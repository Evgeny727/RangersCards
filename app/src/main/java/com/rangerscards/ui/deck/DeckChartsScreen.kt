package com.rangerscards.ui.deck

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.CardWithCount
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun DeckChartsScreen(
    navigateUp: () -> Unit,
    deckViewModel: DeckViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val slots by deckViewModel.orderedSlotsCards.collectAsState()
    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = modifier.applyScaffoldPaddings(contentPadding),
        topBar = {
            RangersTopAppBar(
                title = stringResource(R.string.tools_section_charts),
                canNavigateBack = true,
                navigateUp = navigateUp,
                actions = null,
                switch = null
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .applyScaffoldPaddings(innerPadding),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val flattenedCards = slots.values.flatten().toImmutableList()
            item("equip_chart") {
                EquipChart(flattenedCards)
            }
            item("approaches_chart") {
                ApproachesChart(flattenedCards)
            }
            item("cost_chart") {
                CostChart(flattenedCards)
            }
        }
    }
}

@Composable
fun EquipChart(cards: ImmutableList<CardWithCount>) {
    val barsColor = CustomTheme.colors.d15
    val cardsMap = cards.filter { it.card.equip != null }.groupBy { it.card.equip!! }.mapValues {
        it.value.fold(0) { acc, item -> acc + item.count  }
    }
    val chartData = remember(cards) {
        (1..5).map { equip ->
            val count = cardsMap[equip] ?: 0

            Bars(
                label = equip.toString(), // bottom labels: 1..5
                values = listOf(
                    Bars.Data(
                        value = count.toDouble(), // left scale uses these counts
                        color = SolidColor(barsColor)
                    )
                )
            )
        }
    }
    val maxCount = (cardsMap.values.maxOrNull() ?: 0).toDouble()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.equip_card_divider_header),
                color = CustomTheme.colors.d20,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 22.sp,
            )
        }
        ColumnChart(
            modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.CenterHorizontally)
                .heightIn(max = 400.dp).padding(horizontal = 8.dp),
            data = chartData,
            maxValue = maxCount,
            minValue = 0.0,
            barProperties = BarProperties(
                spacing = 8.dp,
                thickness = 24.dp,
                cornerRadius = Bars.Data.Radius.Rectangle(
                    topLeft = 6.dp,
                    topRight = 6.dp
                )
            ),
            labelProperties = LabelProperties(
                enabled = true,
                padding = 4.dp,
                builder = { modifier, label, shouldRotate, index ->
                    // 'modifier' is already sized/padded by the chart; you can chain more modifiers
                    Box(modifier = modifier) {
                        Text(
                            text = label,
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            modifier = Modifier.then(if (shouldRotate)
                                Modifier.rotate(-45f) else Modifier)
                        )
                    }
                },
                rotation = LabelProperties.Rotation()
            ),
            labelHelperProperties = LabelHelperProperties(enabled = false),
            indicatorProperties = HorizontalIndicatorProperties(
                textStyle = TextStyle(
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
            ),
            gridProperties = GridProperties(
                xAxisProperties = GridProperties.AxisProperties(color = SolidColor(CustomTheme.colors.d30)),
                yAxisProperties = GridProperties.AxisProperties(color = SolidColor(CustomTheme.colors.d30))
            ),
            popupProperties = PopupProperties(
                textStyle = TextStyle(
                    color = CustomTheme.colors.l30,
                    fontFamily = Jost,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                containerColor = CustomTheme.colors.d20,
                cornerRadius = 4.dp,
                contentVerticalPadding = 4.dp,
                contentHorizontalPadding = 4.dp
            ),
            animationMode = AnimationMode.Together { it -> it * 100L },
            animationSpec = tween(300),
        )
        HorizontalDivider(color = CustomTheme.colors.l10)
    }
}

@Composable
fun ApproachesChart(cards: ImmutableList<CardWithCount>) {
    val barsColor = CustomTheme.colors.d15
    val conflictAmount = cards.sumOf { (it.card.approaches.conflict ?: 0) * it.count }
    val reasonAmount = cards.sumOf { (it.card.approaches.reason ?: 0) * it.count }
    val explorationAmount = cards.sumOf { (it.card.approaches.exploration ?: 0) * it.count }
    val connectionAmount = cards.sumOf { (it.card.approaches.connection ?: 0) * it.count }
    val chartData = remember(cards) {
        listOf(
            Bars(
                label = "",
                values = listOf(Bars.Data(value = conflictAmount.toDouble(), color = SolidColor(barsColor)))
            ),
            Bars(
                label = "",
                values = listOf(Bars.Data(value = reasonAmount.toDouble(), color = SolidColor(barsColor)))
            ),
            Bars(
                label = "",
                values = listOf(Bars.Data(value = explorationAmount.toDouble(), color = SolidColor(barsColor)))
            ),
            Bars(
                label = "",
                values = listOf(Bars.Data(value = connectionAmount.toDouble(), color = SolidColor(barsColor)))
            ),
        )
    }
    val maxCount = maxOf(conflictAmount, reasonAmount, explorationAmount, connectionAmount).toDouble()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.approaches_filter_header),
                color = CustomTheme.colors.d20,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 22.sp,
            )
        }
        ColumnChart(
            modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.CenterHorizontally)
                .heightIn(max = 400.dp).padding(horizontal = 8.dp),
            data = chartData,
            maxValue = maxCount,
            minValue = 0.0,
            barProperties = BarProperties(
                spacing = 8.dp,
                thickness = 24.dp,
                cornerRadius = Bars.Data.Radius.Rectangle(
                    topLeft = 6.dp,
                    topRight = 6.dp
                )
            ),
            labelProperties = LabelProperties(
                enabled = true,
                padding = 4.dp,
                builder = { modifier, label, shouldRotate, index ->
                    // 'modifier' is already sized/padded by the chart; you can chain more modifiers
                    Box(modifier = modifier) {
                        Icon(
                            painter = painterResource(when(index) {
                                0 -> R.drawable.conflict
                                1 -> R.drawable.reason
                                2 -> R.drawable.exploration
                                3 -> R.drawable.connection
                                else -> R.drawable.broken_image_32dp
                            }),
                            contentDescription = null,
                            tint = CustomTheme.colors.d30,
                            modifier = Modifier.size(24.dp)        // scale icon if needed
                                .then(if (shouldRotate) Modifier.rotate(-45f) else Modifier)
                        )
                    }
                },
                rotation = LabelProperties.Rotation()
            ),
            labelHelperProperties = LabelHelperProperties(enabled = false),
            indicatorProperties = HorizontalIndicatorProperties(
                textStyle = TextStyle(
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
            ),
            gridProperties = GridProperties(
                xAxisProperties = GridProperties.AxisProperties(color = SolidColor(CustomTheme.colors.d30)),
                yAxisProperties = GridProperties.AxisProperties(color = SolidColor(CustomTheme.colors.d30))
            ),
            popupProperties = PopupProperties(
                textStyle = TextStyle(
                    color = CustomTheme.colors.l30,
                    fontFamily = Jost,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                containerColor = CustomTheme.colors.d20,
                cornerRadius = 4.dp,
                contentVerticalPadding = 4.dp,
                contentHorizontalPadding = 4.dp
            ),
            animationMode = AnimationMode.Together { it -> it * 100L },
            animationSpec = tween(300),
        )
        HorizontalDivider(color = CustomTheme.colors.l10)
    }
}

@Composable
fun CostChart(cards: ImmutableList<CardWithCount>) {
    val awaText = stringResource(R.string.awa_styled_card_text)
    val spiText = stringResource(R.string.spi_styled_card_text)
    val fitText = stringResource(R.string.fit_styled_card_text)
    val focText = stringResource(R.string.foc_styled_card_text)
    val barsColor = listOf(CustomTheme.colors.green, CustomTheme.colors.orange, CustomTheme.colors.red, CustomTheme.colors.blue)
    val filteredCards = cards.filter { it.card.aspect != null }.groupBy { it.card.aspect!!.id }
    val awaAmount = filteredCards["AWA"]?.sumOf { it.count } ?: 0
    val spiAmount = filteredCards["SPI"]?.sumOf { it.count } ?: 0
    val fitAmount = filteredCards["FIT"]?.sumOf { it.count } ?: 0
    val focAmount = filteredCards["FOC"]?.sumOf { it.count } ?: 0
    val chartData = remember(cards) {
        listOf(
            Bars(
                label = awaText,
                values = listOf(Bars.Data(value = awaAmount.toDouble(), color = SolidColor(barsColor[0])))
            ),
            Bars(
                label = spiText,
                values = listOf(Bars.Data(value = spiAmount.toDouble(), color = SolidColor(barsColor[1])))
            ),
            Bars(
                label = fitText,
                values = listOf(Bars.Data(value = fitAmount.toDouble(), color = SolidColor(barsColor[2])))
            ),
            Bars(
                label = focText,
                values = listOf(Bars.Data(value = focAmount.toDouble(), color = SolidColor(barsColor[3])))
            ),
        )
    }
    val maxCount = maxOf(awaAmount, spiAmount, fitAmount, focAmount).toDouble()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.aspect_card_divider_header),
                color = CustomTheme.colors.d20,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 22.sp,
            )
        }
        ColumnChart(
            modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.CenterHorizontally)
                .heightIn(max = 400.dp).padding(horizontal = 8.dp),
            data = chartData,
            maxValue = maxCount,
            minValue = 0.0,
            barProperties = BarProperties(
                spacing = 8.dp,
                thickness = 24.dp,
                cornerRadius = Bars.Data.Radius.Rectangle(
                    topLeft = 6.dp,
                    topRight = 6.dp
                )
            ),
            labelProperties = LabelProperties(
                enabled = true,
                padding = 4.dp,
                builder = { modifier, label, shouldRotate, index ->
                    // 'modifier' is already sized/padded by the chart; you can chain more modifiers
                    Box(modifier = modifier) {
                        Text(
                            text = label,
                            color = when(index) {
                                0 -> CustomTheme.colors.green
                                1 -> CustomTheme.colors.orange
                                2 -> CustomTheme.colors.red
                                3 -> CustomTheme.colors.blue
                                else -> CustomTheme.colors.d30
                            },
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                },
            ),
            labelHelperProperties = LabelHelperProperties(enabled = false),
            indicatorProperties = HorizontalIndicatorProperties(
                textStyle = TextStyle(
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
            ),
            gridProperties = GridProperties(
                xAxisProperties = GridProperties.AxisProperties(color = SolidColor(CustomTheme.colors.d30)),
                yAxisProperties = GridProperties.AxisProperties(color = SolidColor(CustomTheme.colors.d30))
            ),
            popupProperties = PopupProperties(
                textStyle = TextStyle(
                    color = CustomTheme.colors.l30,
                    fontFamily = Jost,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                containerColor = CustomTheme.colors.d20,
                cornerRadius = 4.dp,
                contentVerticalPadding = 4.dp,
                contentHorizontalPadding = 4.dp
            ),
            animationMode = AnimationMode.Together { it -> it * 100L },
            animationSpec = tween(300),
        )
    }
}