package com.rangerscards.ui.campaign.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.objects.Path
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun CampaignCurrentPositionCard(
    cycleId: String,
    location: String,
    pathTerrain: String?,
    expansions: List<String>,
    onRecordedJourney: () -> Unit)
{
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CustomTheme.colors.l30,
        shape = CustomTheme.shapes.large,
        border = BorderStroke(1.dp, CustomTheme.colors.m),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                color = CustomTheme.colors.l20,
                shape = CustomTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.current_location),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                        )
                        val currentLocation = CampaignMaps.getMapLocations(false, cycleId, expansions)[location]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(currentLocation?.iconResId ?: R.drawable.broken_image_32dp),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = stringResource(currentLocation?.nameResId ?: R.string.text_none),
                                color = CustomTheme.colors.d30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.current_path_terrain),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                        )
                        val currentPathTerrain = Path
                            .fromValue(pathTerrain.toString())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(currentPathTerrain?.iconResId ?: R.drawable.broken_image_32dp),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = stringResource(currentPathTerrain?.nameResId
                                    ?: R.string.text_none),
                                color = CustomTheme.colors.d30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(CustomTheme.colors.l10)
                    .clickable { onRecordedJourney.invoke() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.calendar_32dp),
                        contentDescription = null,
                        tint = CustomTheme.colors.d10,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.recorded_journey_button),
                        color = CustomTheme.colors.d10,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }
}