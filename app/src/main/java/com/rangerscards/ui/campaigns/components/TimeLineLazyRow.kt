package com.rangerscards.ui.campaigns.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.objects.Weather
import com.rangerscards.ui.campaigns.DayInfo
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun TimeLineLazyRow(
    groupedDays: Map<Weather, Map<Int, DayInfo>>,
    currentDay: Int,
    onClick: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val localDensity = LocalDensity.current
    var guideSectionHeightDp: Dp by remember { mutableStateOf(24.dp + with(localDensity) { (12.sp).toDp() }) }
    LaunchedEffect(currentDay) {
        val weatherList = groupedDays.toList()
        val currentIndex = weatherList.indexOfFirst { it.second.containsKey(currentDay) }
        if (currentIndex >= 0) {
            val itemWidthPx = 40.dp
            val daysList = weatherList[currentIndex].second.toList()
            val dayIndex = daysList.indexOfFirst { it.first == currentDay }
            val offset = (4.dp + itemWidthPx) * dayIndex
            listState.animateScrollToItem(index = currentIndex, scrollOffset = with(localDensity) { offset.roundToPx() })
        }
    }
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groupedDays.forEach { (weather, day) ->
            item {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        day.forEach { (day, dayInfo) ->
                            key(day) {
                                Column(
                                    modifier = Modifier.sizeIn(maxWidth = 40.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    if (dayInfo.guides.isNotEmpty()) Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .onGloballyPositioned { layoutCoordinates ->
                                                val dpValue = with(localDensity) {
                                                    layoutCoordinates.size.height.toDp()
                                                }
                                                if (guideSectionHeightDp != dpValue) guideSectionHeightDp = dpValue
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.guide),
                                            contentDescription = null,
                                            tint = CustomTheme.colors.m,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = dayInfo.guides.joinToString(separator = ", "),
                                            color = CustomTheme.colors.d30,
                                            fontFamily = Jost,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    else Spacer(Modifier.height(guideSectionHeightDp))
                                    DayIcon(currentDay, day, dayInfo.moonIconId, onClick)
                                }
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RoundedRectWithoutTopLine(CustomTheme.colors.m)
                        Text(
                            text = stringResource(weather.nameResId),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (weather.secondNameResId != null) {
                            HorizontalDivider(color = CustomTheme.colors.m, modifier = Modifier.width(24.dp))
                            Text(
                                text = stringResource(weather.secondNameResId),
                                color = CustomTheme.colors.d30.copy(alpha = 0.7f),
                                fontFamily = Jost,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoundedRectWithoutTopLine(
    color: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(14.dp)
    ) {
        val strokeWidth = 2f
        val rectWidth = size.width
        val rectHeight = size.height
        val cornerRadius = 12f // Adjust this for rounder corners
        val path = Path().apply {
            // Start at the top-left without drawing a top line
            moveTo(0f, cornerRadius)
            // Left vertical line
            lineTo(0f, rectHeight - cornerRadius)
            // Bottom-left rounded corner
            quadraticTo(0f, rectHeight, cornerRadius, rectHeight)
            // Bottom horizontal line
            lineTo(rectWidth - cornerRadius, rectHeight)
            // Bottom-right rounded corner
            quadraticTo(rectWidth, rectHeight, rectWidth, rectHeight - cornerRadius)
            // Right vertical line
            lineTo(rectWidth, cornerRadius)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun DayIcon(currentDay: Int, day: Int, @DrawableRes iconResId: Int, onClick: (Int) -> Unit) {
    val isPassed = currentDay > day
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            onClick = { onClick.invoke(day) },
            enabled = !isPassed,
            color = CustomTheme.colors.l30,
            shape = CustomTheme.shapes.circle,
            border = BorderStroke(
                if (currentDay == day) 3.dp else 1.dp,
                CustomTheme.colors.d30
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Day icon (positioned at the bottom)
                Icon(
                    painterResource(iconResId),
                    contentDescription = "Day icon",
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(40.dp)
                )
                // Day number text – if passed, show as struck through.
                Text(
                    text = day.toString(),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    textDecoration = if (isPassed) TextDecoration.LineThrough else TextDecoration.None,
                )
            }
        }
        // If the day is passed, overlay a semi-transparent layer.
        if (isPassed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CustomTheme.shapes.circle)
                    .background(CustomTheme.colors.m.copy(alpha = 0.5f))
            )
        }
    }
}