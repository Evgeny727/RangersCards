package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.CampaignMission
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun CampaignMissionDialog(
    campaignMission: CampaignMission,
    deleteCampaignMission: (String) -> Unit,
    setCampaignMissions: (String, CampaignMission) -> Unit,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
) {
    var day by rememberSaveable { mutableStateOf(campaignMission.day.toString()) }
    var name by rememberSaveable { mutableStateOf(campaignMission.name) }
    val checks = remember { campaignMission.checks.toMutableStateList() }
    var completed by rememberSaveable { mutableStateOf(campaignMission.completed) }
    RangersDialogWithContent(
        headerId = R.string.mission_header,
        isDarkTheme = isDarkTheme,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                value = day,
                onValueChange = { newDay -> day = newDay.filter { it.isDigit() }.take(2) },
                label = {
                    Text(text = buildAnnotatedString {
                        append(stringResource(R.string.mission_day_input))
                        withStyle(style = SpanStyle(color = CustomTheme.colors.warn)) {
                            append("*")
                        }
                    })
                },
                textStyle = TextStyle(
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = CustomTheme.shapes.small,
                colors = TextFieldDefaults.colors().copy(
                    focusedIndicatorColor = CustomTheme.colors.m,
                    unfocusedIndicatorColor = CustomTheme.colors.m,
                    unfocusedLabelColor = CustomTheme.colors.d30,
                    focusedLabelColor = CustomTheme.colors.d30,
                    unfocusedPlaceholderColor = CustomTheme.colors.d30,
                    focusedPlaceholderColor = CustomTheme.colors.d30,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(text = buildAnnotatedString {
                        append(stringResource(R.string.name_label))
                        withStyle(style = SpanStyle(color = CustomTheme.colors.warn)) {
                            append("*")
                        }
                    })
                },
                textStyle = TextStyle(
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                ),
                singleLine = true,
                shape = CustomTheme.shapes.small,
                colors = TextFieldDefaults.colors().copy(
                    focusedIndicatorColor = CustomTheme.colors.m,
                    unfocusedIndicatorColor = CustomTheme.colors.m,
                    unfocusedLabelColor = CustomTheme.colors.d30,
                    focusedLabelColor = CustomTheme.colors.d30,
                    unfocusedPlaceholderColor = CustomTheme.colors.d30,
                    focusedPlaceholderColor = CustomTheme.colors.d30,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Text(
                text = stringResource(R.string.mission_progress),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 20.sp,
            )
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                checks.forEachIndexed { index, check ->
                    key(index) {
                        IconButton(
                            onClick = { checks[index] = !check },
                            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                painterResource(if (check) R.drawable.square_check_checked
                                else R.drawable.square_check_unchecked),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
            RangersRadioButtonRow(
                text = stringResource(R.string.mission_completed),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                isSelected = completed,
            ) { value ->
                completed = value
            }
        }
        Spacer(Modifier.height(8.dp))
        SquareButton(
            stringId = R.string.delete_mission_button,
            leadingIcon = R.drawable.delete_32dp,
            iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
            textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.warn,
            ),
            onClick = { deleteCampaignMission(campaignMission.name); onBack() },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        SquareButton(
            stringId = R.string.save_deck_changes_button,
            leadingIcon = R.drawable.done_32dp,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.d10,
            ),
            onClick = { setCampaignMissions(campaignMission.name, CampaignMission(
                day = day.toInt(),
                name = name,
                checks = checks,
                completed = completed
            )); onBack() },
            modifier = Modifier.padding(8.dp)
        )
    }
}