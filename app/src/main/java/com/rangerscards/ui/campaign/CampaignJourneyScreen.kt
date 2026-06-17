package com.rangerscards.ui.campaign

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.CampaignHistory
import com.rangerscards.domain.model.CampaignTravelDay
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.objects.Path
import com.rangerscards.objects.Weather
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CampaignJourneyScreen(
    campaign: Campaign,
    buildTravelHistory: (List<CampaignHistory>) -> ImmutableList<CampaignTravelDay>,
    getWeatherByDay: (Int) -> Weather?,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val travelHistory by remember(campaign.history) {
        mutableStateOf(buildTravelHistory(campaign.history))
    }
    Column(
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(travelHistory, CampaignTravelDay::day) { travelDay ->
                // Header for the travel day row
                var isExpanded by rememberSaveable { mutableStateOf(false) }
                Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val weather = remember { getWeatherByDay(travelDay.day) }
                        val upperWeather = weather?.nameResId ?: R.string.text_none
                        val undergroundWeather = weather?.secondNameResId
                        Text(
                            text = stringResource(R.string.campaigns_current_day, travelDay.day) +
                                    " - ${stringResource(upperWeather)}" +
                                    undergroundWeather?.let { " (${stringResource(it)})" },
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 8.dp).weight(1f)
                        )
                        Icon(
                            painterResource(if (isExpanded) R.drawable.arrow_drop_up_32dp
                            else R.drawable.arrow_drop_down_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    HorizontalDivider(color = CustomTheme.colors.l10)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Render travel day details
                    TravelDayRow(
                        cycleId = campaign.cycleId,
                        travelDay = travelDay,
                        currentDay = campaign.currentDay,
                        isExpanded = isExpanded
                    )
                }
            }
        }
    }
}

@Composable
fun TravelDayRow(
    cycleId: String,
    travelDay: CampaignTravelDay,
    currentDay: Int,
    isExpanded: Boolean,
) {
    // Resolve starting location from locale
    val start = travelDay.startingLocation
    // Determine final location from the travel list (or fallback to startingLocation)
    val lastEntry = remember(travelDay.travel) { travelDay.travel.lastOrNull() }
    val finalLocation = lastEntry?.location ?: travelDay.startingLocation
    // Whether the last travel entry was camped
    val camped = lastEntry?.camped ?: false
    val locationsMap = remember {
        CampaignMaps.getMapLocations(false, cycleId, ignoreExpansions = true)
    }
    if (isExpanded) FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Starting location icon
        if (start != null) Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .sizeIn(maxWidth = 110.dp)
                .align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val location = locationsMap[start]
            Icon(
                painterResource(location?.iconResId ?: R.drawable.broken_image_32dp),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(location?.nameResId ?: R.string.text_none),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(hyphens = Hyphens.Auto)
            )
        }
        // Render each travel entry
        travelDay.travel.forEach { entry ->
            val path = Path.fromValue(entry.pathTerrain)
            if (path?.iconResId != null) {
                Icon(
                    painterResource(R.drawable.arrow_forward_32dp),
                    contentDescription = "Arrow",
                    tint = CustomTheme.colors.m,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
                Icon(
                    painter = painterResource(id = path.iconResId),
                    contentDescription = path.name,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            // If not camped, show an arrow and location icon.
            if (!entry.camped) {
                Icon(
                    painterResource(R.drawable.arrow_forward_32dp),
                    contentDescription = "Arrow",
                    tint = CustomTheme.colors.m,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
                val current = locationsMap[entry.location]
                if (current != null) {
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .sizeIn(maxWidth = 110.dp)
                            .align(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painterResource(current.iconResId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(current.nameResId),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center,
                            style = TextStyle(hyphens = Hyphens.Auto)
                        )
                    }
                }
            }
        }
        // At the end, if a final location exists and this is a past day, show a summary icon.
        if (finalLocation != null && travelDay.day < currentDay) {
            Icon(
                painterResource(R.drawable.arrow_forward_32dp),
                contentDescription = "Arrow",
                tint = CustomTheme.colors.m,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
            val finalLocationName = stringResource(locationsMap[finalLocation]?.nameResId ?: R.string.text_none)
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .sizeIn(maxWidth = 110.dp)
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painterResource(if (camped) R.drawable.camping_32dp else R.drawable.camp_32dp),
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = stringResource(if (camped) R.string.en_route_to else R.string.stayed_at, finalLocationName),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(hyphens = Hyphens.Auto)
                )
            }
        }
    }
    else LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Starting location icon
        if (start != null) item {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .sizeIn(maxWidth = 110.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val location = locationsMap[start]!!
                Icon(
                    painterResource(location.iconResId),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(location.nameResId),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(hyphens = Hyphens.Auto)
                )
            }
        }
        // Render each travel entry
        travelDay.travel.forEach { entry ->
            val path = Path.fromValue(entry.pathTerrain)
            if (path?.iconResId != null) {
                item {
                    Icon(
                        painterResource(R.drawable.arrow_forward_32dp),
                        contentDescription = "Arrow",
                        tint = CustomTheme.colors.m,
                        modifier = Modifier.size(24.dp)
                    )
                }
                item {
                    Icon(
                        painter = painterResource(id = path.iconResId),
                        contentDescription = path.name,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(48.dp)
                    )
                }
            }
            // If not camped, show an arrow and location icon.
            if (!entry.camped) {
                item {
                    Icon(
                        painterResource(R.drawable.arrow_forward_32dp),
                        contentDescription = "Arrow",
                        tint = CustomTheme.colors.m,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
                val current = locationsMap[entry.location]
                if (current != null) item {
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .sizeIn(maxWidth = 110.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painterResource(current.iconResId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(current.nameResId),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center,
                            style = TextStyle(hyphens = Hyphens.Auto)
                        )
                    }
                }
            }
        }
        // At the end, if a final location exists and this is a past day, show a summary icon.
        if (finalLocation != null && travelDay.day < currentDay) {
            item {
                Icon(
                    painterResource(R.drawable.arrow_forward_32dp),
                    contentDescription = "Arrow",
                    tint = CustomTheme.colors.m,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
            item {
                val finalLocationName = stringResource(locationsMap[finalLocation]?.nameResId ?: R.string.text_none)
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .sizeIn(maxWidth = 110.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painterResource(if (camped) R.drawable.camping_32dp else R.drawable.camp_32dp),
                        contentDescription = null,
                        tint = CustomTheme.colors.m,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = stringResource(if (camped) R.string.en_route_to else R.string.stayed_at, finalLocationName),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                        style = TextStyle(hyphens = Hyphens.Auto)
                    )
                }
            }
        }
    }
}