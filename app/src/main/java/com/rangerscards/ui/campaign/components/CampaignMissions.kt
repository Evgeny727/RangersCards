package com.rangerscards.ui.campaign.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.campaigns.CampaignMission
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun CampaignMissions(
    onAdd: () -> Unit,
    missions: List<CampaignMission>,
    onClick: (String) -> Unit,
    isOnlyActive: Boolean = false,
    onActiveClick: () -> Unit,
    state: LazyListState,
    nestedConnectionModifier: Modifier
) {
    Column {
        RangersRadioButtonRow(
            text = stringResource(R.string.show_only_active_missions),
            onValueChange = onActiveClick,
            modifier = Modifier,
            isSelected = isOnlyActive
        )
        SquareButton(
            stringId = R.string.add_mission_button,
            leadingIcon = R.drawable.add_circle_32dp,
            iconColor = CustomTheme.colors.m,
            textColor = CustomTheme.colors.d30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.l20
            ),
            onClick = onAdd,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(
            state = state,
            modifier = nestedConnectionModifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            missions.filter { mission -> !isOnlyActive || !mission.completed }.forEach { mission ->
                item(mission.name) {
                    Column(
                        modifier = Modifier.fillMaxWidth().clickable { onClick.invoke(mission.name) },
                    ) {
                        Text(
                            text = stringResource(R.string.campaigns_current_day, mission.day),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                        )
                        Text(
                            text = mission.name,
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            textDecoration = if (mission.completed) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Row {
                            Text(
                                text = stringResource(R.string.mission_progress),
                                color = CustomTheme.colors.d30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                            )
                            if (mission.completed) Text(
                                text = " - ${stringResource(R.string.mission_completed)}",
                                color = CustomTheme.colors.d30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                            ) else {
                                Spacer(Modifier.width(8.dp))
                                mission.checks.forEach { check ->
                                    Icon(
                                        painterResource(if (check) R.drawable.square_check_checked
                                        else R.drawable.square_check_unchecked),
                                        contentDescription = null,
                                        tint = CustomTheme.colors.m,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
            }
        }
    }
}