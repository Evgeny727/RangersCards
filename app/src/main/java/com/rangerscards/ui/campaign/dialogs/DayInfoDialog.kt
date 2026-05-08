package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.Campaign
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.objects.Weather
import com.rangerscards.ui.campaign.DayInfo
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.SettingsInputField
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.collections.immutable.ImmutableMap

enum class DayInfoDialog {
    Edit,
    Add
}

@Composable
fun DayInfoDialog(
    campaign: Campaign,
    groupDaysByWeather: () -> ImmutableMap<Weather, ImmutableMap<Int, DayInfo>>,
    setCampaignCalendar: (Int, List<String>) -> Unit,
    dayId: Int,
    isDarkTheme: Boolean,
    isViewOnly: Boolean,
    onBack: () -> Unit,
) {
    val groupedDays = remember { groupDaysByWeather().values }
    val dayInfo = remember(dayId) { groupedDays.firstNotNullOfOrNull { it[dayId] } }
    var showInputDialog by rememberSaveable { mutableStateOf<DayInfoDialog?>(null) }
    var guideEntryEditing by rememberSaveable { mutableStateOf("") }
    var guideEntryPrevious by rememberSaveable { mutableStateOf("") }
    val fixedGuides = remember { CampaignMaps.fixedGuideEntries[campaign.cycleId] }
    RangersDialogWithContent(
        headerId = R.string.campaigns_current_day,
        formatArgs = dayId,
        isDarkTheme = isDarkTheme,
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.guide_entries),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                )
            }
            dayInfo?.guides?.forEach { guideEntry ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = guideEntry,
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (!isViewOnly && fixedGuides?.firstOrNull { it.day == dayId }?.guides?.contains(guideEntry) != true) {
                            IconButton(
                                onClick = { guideEntryEditing = guideEntry
                                    guideEntryPrevious = guideEntry
                                    showInputDialog = DayInfoDialog.Edit
                                },
                                colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    painterResource(R.drawable.edit_32dp),
                                    contentDescription = null,
                                    tint = CustomTheme.colors.m,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val newGuides = campaign.calendar.firstOrNull { it.day == dayId }?.guides
                                    setCampaignCalendar(
                                        dayId,
                                        newGuides?.filter { it != guideEntry } ?: emptyList(),
                                    )
                                    onBack()
                                },
                                colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    painterResource(R.drawable.delete_32dp),
                                    contentDescription = null,
                                    tint = CustomTheme.colors.warn,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        if (!isViewOnly) SquareButton(
            stringId = R.string.add_guide_entry_button,
            leadingIcon = R.drawable.add_32dp,
            onClick = { showInputDialog = DayInfoDialog.Add },
            modifier = Modifier.padding(8.dp)
        )
    }
    if (showInputDialog != null) RangersDialogWithContent(
        headerId = R.string.guide_entry,
        isDarkTheme = isDarkTheme,
        onBack = { showInputDialog = null },
    ) {
        SettingsInputField(
            leadingIcon = R.drawable.badge_32dp,
            placeholder = null,
            textValue = guideEntryEditing,
            onValueChange = { guideEntryEditing = it },
            KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SquareButton(
                stringId = R.string.cancel_button,
                leadingIcon = R.drawable.close_32dp,
                onClick = { showInputDialog = null
                    guideEntryEditing = ""
                    guideEntryPrevious = ""
                },
                buttonColor = ButtonDefaults.buttonColors().copy(
                    CustomTheme.colors.d30,
                    disabledContainerColor = CustomTheme.colors.m
                ),
                iconColor = CustomTheme.colors.warn,
                textColor = CustomTheme.colors.l30,
                modifier = Modifier.weight(0.5f),
            )
            SquareButton(
                stringId = R.string.done_button,
                leadingIcon = R.drawable.done_32dp,
                onClick = {
                    val newGuides = campaign.calendar.firstOrNull { it.day == dayId }?.guides ?: emptyList()
                    when(showInputDialog) {
                        DayInfoDialog.Add -> {
                            setCampaignCalendar(
                                dayId,
                                newGuides + guideEntryEditing,
                            )
                        }
                        else -> {
                            val index = newGuides.indexOf(guideEntryPrevious)
                            if (index != -1) {
                                setCampaignCalendar(
                                    dayId,
                                    newGuides.map { if (it == guideEntryPrevious) guideEntryEditing else it },
                                )
                            }
                        }
                    }
                    onBack()
                },
                buttonColor = ButtonDefaults.buttonColors().copy(
                    CustomTheme.colors.d10,
                    disabledContainerColor = CustomTheme.colors.m
                ),
                iconColor = CustomTheme.colors.l15,
                textColor = CustomTheme.colors.l30,
                isEnabled = guideEntryEditing.isNotEmpty(),
                modifier = Modifier.weight(0.5f),
            )
        }
    }
}