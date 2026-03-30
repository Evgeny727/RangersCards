package com.rangerscards.ui.campaigns.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.objects.ChallengeDeck
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.LineProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.VerticalIndicatorProperties

@Composable
fun AspectsRowCharts(
    deckList: List<Int>
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val awaText = stringResource(R.string.awa_styled_card_text)
    val spiText = stringResource(R.string.spi_styled_card_text)
    val fitText = stringResource(R.string.fit_styled_card_text)
    val focText = stringResource(R.string.foc_styled_card_text)
    val awaColor = CustomTheme.colors.green
    val spiColor = CustomTheme.colors.orange
    val fitColor = CustomTheme.colors.red
    val focColor = CustomTheme.colors.blue
    val challengeCardsList = ChallengeDeck.challengeDeck.filter { deckList.contains(it.key) }.values.toList()
    val awaCounts = listOf(
        challengeCardsList.count { it.awa == -2 }.toDouble(),
        challengeCardsList.count { it.awa == -1 }.toDouble(),
        challengeCardsList.count { it.awa == 0 }.toDouble(),
        challengeCardsList.count { it.awa == 1 }.toDouble()
    )
    val spiCounts = listOf(
        challengeCardsList.count { it.spi == -2 }.toDouble(),
        challengeCardsList.count { it.spi == -1 }.toDouble(),
        challengeCardsList.count { it.spi == 0 }.toDouble(),
        challengeCardsList.count { it.spi == 1 }.toDouble()
    )
    val fitCounts = listOf(
        challengeCardsList.count { it.fit == -2 }.toDouble(),
        challengeCardsList.count { it.fit == -1 }.toDouble(),
        challengeCardsList.count { it.fit == 0 }.toDouble(),
        challengeCardsList.count { it.fit == 1 }.toDouble()
    )
    val focCounts = listOf(
        challengeCardsList.count { it.foc == -2 }.toDouble(),
        challengeCardsList.count { it.foc == -1 }.toDouble(),
        challengeCardsList.count { it.foc == 0 }.toDouble(),
        challengeCardsList.count { it.foc == 1 }.toDouble()
    )
    val icons = listOf(
        R.drawable.sun,
        R.drawable.mountain,
        R.drawable.crest
    )
    val effectCounts = listOf(
        challengeCardsList.count { it.challengeIcon == R.drawable.sun }.toDouble(),
        challengeCardsList.count { it.challengeIcon == R.drawable.mountain }.toDouble(),
        challengeCardsList.count { it.challengeIcon == R.drawable.crest }.toDouble()
    )
    Column(
        modifier = Modifier.fillMaxWidth().animateContentSize(tween(300)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp).clickable { isExpanded = !isExpanded }
        ) {
            Text(
                text = stringResource(if (isExpanded) R.string.hide_charts
                else R.string.show_charts),
                color = CustomTheme.colors.d15,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painterResource(if (isExpanded) R.drawable.arrow_drop_up_32dp
                else R.drawable.arrow_drop_down_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(32.dp)
            )
        }
        if (isExpanded) {
            RowChart(
                modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.CenterHorizontally)
                    .heightIn(max = 600.dp).padding(horizontal = 8.dp),
                labelProperties = LabelProperties(
                    enabled = true,
                    textStyle = TextStyle(
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    padding = 4.dp,
                    labels = listOf("-2", "-1", "0", "1"),
                    builder = null,
                    rotation = LabelProperties.Rotation()
                ),
                labelHelperProperties = LabelHelperProperties(
                    textStyle = TextStyle(
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    labelCountPerLine = 4
                ),
                dividerProperties = DividerProperties(
                    xAxisProperties = LineProperties(color = SolidColor(CustomTheme.colors.d30)),
                    yAxisProperties = LineProperties(color = SolidColor(CustomTheme.colors.d30))
                ),
                gridProperties = GridProperties(
                    xAxisProperties = GridProperties.AxisProperties(color = SolidColor(CustomTheme.colors.d30)),
                    yAxisProperties = GridProperties.AxisProperties(
                        color = SolidColor(CustomTheme.colors.d30),
                        lineCount = 6
                    )
                ),
                indicatorProperties = VerticalIndicatorProperties(
                    textStyle = TextStyle(
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    count = IndicatorCount.StepBased(2.0)
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
                data = remember(challengeCardsList.size) {
                    listOf(
                        Bars(
                            label = "-2",
                            values = listOf(
                                Bars.Data(label = awaText, value = awaCounts[0], color = SolidColor(awaColor)),
                                Bars.Data(label = spiText, value = spiCounts[0], color = SolidColor(spiColor)),
                                Bars.Data(label = fitText, value = fitCounts[0], color = SolidColor(fitColor)),
                                Bars.Data(label = focText, value = focCounts[0], color = SolidColor(focColor)),
                            ),
                        ),
                        Bars(
                            label = "-1",
                            values = listOf(
                                Bars.Data(label = awaText, value = awaCounts[1], color = SolidColor(awaColor)),
                                Bars.Data(label = spiText, value = spiCounts[1], color = SolidColor(spiColor)),
                                Bars.Data(label = fitText, value = fitCounts[1], color = SolidColor(fitColor)),
                                Bars.Data(label = focText, value = focCounts[1], color = SolidColor(focColor)),
                            ),
                        ),
                        Bars(
                            label = "0",
                            values = listOf(
                                Bars.Data(label = awaText, value = awaCounts[2], color = SolidColor(awaColor)),
                                Bars.Data(label = spiText, value = spiCounts[2], color = SolidColor(spiColor)),
                                Bars.Data(label = fitText, value = fitCounts[2], color = SolidColor(fitColor)),
                                Bars.Data(label = focText, value = focCounts[2], color = SolidColor(focColor)),
                            ),
                        ),
                        Bars(
                            label = "1",
                            values = listOf(
                                Bars.Data(label = awaText, value = awaCounts[3], color = SolidColor(awaColor)),
                                Bars.Data(label = spiText, value = spiCounts[3], color = SolidColor(spiColor)),
                                Bars.Data(label = fitText, value = fitCounts[3], color = SolidColor(fitColor)),
                                Bars.Data(label = focText, value = focCounts[3], color = SolidColor(focColor)),
                            ),
                        ),
                    )
                },
                barProperties = BarProperties(
                    cornerRadius = Bars.Data.Radius.Rectangle(bottomRight = 6.dp, topRight = 6.dp),
                    spacing = 8.dp,
                    thickness = 20.dp,
                ),
                animationMode = AnimationMode.Together { it -> it * 100L },
                animationSpec = tween(300),
                maxValue = 10.0,
                minValue = 0.0
            )
            HorizontalDivider(color = CustomTheme.colors.l10)
            RowChart(
                modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.CenterHorizontally)
                    .heightIn(max = 200.dp).padding(horizontal = 8.dp),
                labelProperties = LabelProperties(
                    enabled = true,
                    padding = 4.dp,
                    builder = { modifier, label, shouldRotate, index ->
                        // 'modifier' is already sized/padded by the chart; you can chain more modifiers
                        Box(modifier = modifier) {
                            Icon(
                                painter = painterResource(id = icons.getOrNull(index) ?: R.drawable.broken_image_32dp),
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
                dividerProperties = DividerProperties(
                    xAxisProperties = LineProperties(color = SolidColor(CustomTheme.colors.d30)),
                    yAxisProperties = LineProperties(color = SolidColor(CustomTheme.colors.d30))
                ),
                gridProperties = GridProperties(
                    xAxisProperties = GridProperties.AxisProperties(color = SolidColor(CustomTheme.colors.d30)),
                    yAxisProperties = GridProperties.AxisProperties(color = SolidColor(CustomTheme.colors.d30))
                ),
                indicatorProperties = VerticalIndicatorProperties(
                    textStyle = TextStyle(
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    count = IndicatorCount.StepBased(2.0)
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
                data = remember(challengeCardsList.size) {
                    listOf(
                        Bars(
                            label = "",
                            values = listOf(
                                Bars.Data(value = effectCounts[0], color = SolidColor(spiColor.copy(alpha = 0.9f))),
                            ),
                        ),
                        Bars(
                            label = "",
                            values = listOf(
                                Bars.Data(value = effectCounts[1], color = SolidColor(focColor.copy(alpha = 0.9f))),
                            ),
                        ),
                        Bars(
                            label = "",
                            values = listOf(
                                Bars.Data(value = effectCounts[2], color = SolidColor(fitColor.copy(alpha = 0.9f))),
                            ),
                        ),
                    )
                },
                barProperties = BarProperties(
                    cornerRadius = Bars.Data.Radius.Rectangle(bottomRight = 6.dp, topRight = 6.dp),
                    spacing = 8.dp,
                    thickness = 20.dp,
                ),
                animationMode = AnimationMode.Together { it -> it * 100L },
                animationSpec = tween(300),
                maxValue = 8.0,
                minValue = 0.0
            )
        }
    }
}