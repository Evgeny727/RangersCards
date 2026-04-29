package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.CampaignEvent
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun CampaignEventDialog(
    event: CampaignEvent,
    updateCampaignEvents: (String, CampaignEvent) -> Unit,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(event.name) }
    var crossedOut by rememberSaveable { mutableStateOf(event.crossedOut) }
    var marks by rememberSaveable { mutableIntStateOf(event.marks) }
    RangersDialogWithContent(
        headerId = R.string.event_header,
        isDarkTheme = isDarkTheme,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(text = buildAnnotatedString {
                        append(stringResource(R.string.event_name_input))
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
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.event_marks),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (marks > 0) marks -= 1 },
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(32.dp),
                        enabled = marks > 0
                    ) {
                        Icon(
                            painterResource(id = R.drawable.remove_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Surface(
                        color = Color.Transparent,
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = marks.toString(),
                                color = CustomTheme.colors.d10,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                            )
                        }
                    }
                    IconButton(
                        onClick = { if (marks < 20) marks += 1 },
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(32.dp),
                        enabled = marks < 20
                    ) {
                        Icon(
                            painterResource(id = R.drawable.add_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            RangersRadioButtonRow(
                text = stringResource(R.string.event_crossed_out),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                isSelected = crossedOut,
            ) { value ->
                crossedOut = value
            }
        }
        SquareButton(
            stringId = R.string.save_deck_changes_button,
            leadingIcon = R.drawable.done_32dp,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.d10,
                disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.3f)
            ),
            onClick = { updateCampaignEvents(event.name, CampaignEvent(
                name = name,
                crossedOut = crossedOut,
                marks = marks
            )); onBack.invoke() },
            modifier = Modifier.padding(8.dp)
        )
    }
}