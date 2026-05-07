package com.rangerscards.ui.campaigns.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.ui.decks.components.DeckListItemImageContainer
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CampaignListItem(
    cycleId: String,
    name: String,
    day: Int,
    currentLocation: String,
    rolesImages: ImmutableList<String>,
    players: ImmutableList<String>,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = CustomTheme.colors.l20,
        shape = CustomTheme.shapes.large,
        border = BorderStroke(1.dp, CustomTheme.colors.campaignBlue),
        shadowElevation = 4.dp
    ) {
        Column(Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CustomTheme.colors.campaignBlue,
                shape = CustomTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(R.drawable.guide),
                            contentDescription = null,
                            tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = name,
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rolesImages.isNotEmpty()) FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rolesImages.forEach {
                            DeckListItemImageContainer(it, Modifier)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val location = CampaignMaps.getMapLocations(false, cycleId, ignoreExpansions = true)[currentLocation]
                        if (location != null) {
                            Icon(
                                painterResource(location.iconResId),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = stringResource(location.nameResId),
                                color = if (isDarkTheme) CustomTheme.colors.d20 else CustomTheme.colors.l20,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                            )
                            Text(
                                text = "\u2022",  // Unicode for bullet
                                color = if (isDarkTheme) CustomTheme.colors.d20 else CustomTheme.colors.l20,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                modifier = Modifier.offset(y = 2.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.campaigns_current_day, day),
                            color = if (isDarkTheme) CustomTheme.colors.d20 else CustomTheme.colors.l20,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
            if (players.isNotEmpty()) Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ranger),
                    contentDescription = null,
                    tint = CustomTheme.colors.d15,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = players.joinToString(", "),
                    color = CustomTheme.colors.d10,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}