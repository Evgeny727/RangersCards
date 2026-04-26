package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.R
import com.rangerscards.ui.campaign.CampaignViewModel
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.RangersBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun AddMissionDialog(
    campaignViewModel: CampaignViewModel,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    user: FirebaseUser?
) {
    val campaign by campaignViewModel.campaign.collectAsState()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    var day by rememberSaveable { mutableStateOf("${campaign!!.currentDay}") }
    var name by rememberSaveable { mutableStateOf("") }
    val coroutine = rememberCoroutineScope()
    val isLegitAdding by remember { derivedStateOf {
        day.isNotEmpty() && name.isNotEmpty()
    } }
    RangersDialogWithContent(
        header = stringResource(id = R.string.add_mission_button),
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
                        append(stringResource(R.string.deck_creation_name_label))
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
        }
        SquareButton(
            stringId = R.string.add_mission_button,
            leadingIcon = R.drawable.add_circle_32dp,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.d10,
                disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.3f)
            ),
            onClick = { coroutine.launch { showLoadingDialog = true
                campaignViewModel.addCampaignMission(day.toInt(), name, user)
            }.invokeOnCompletion { showLoadingDialog = false
                onBack.invoke()
            } },
            isEnabled = isLegitAdding,
            modifier = Modifier.padding(8.dp)
        )
    }
    if (showLoadingDialog) Dialog(
        onDismissRequest = { showLoadingDialog = false },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        RangersBaseCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = R.string.saving_changes_header
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = CustomTheme.colors.m)
            }
        }
    }
}