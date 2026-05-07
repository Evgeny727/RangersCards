package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.rangerscards.R
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun AddNoteDialog(
    addCampaignNote: (Int, String) -> Unit,
    currentDay: Int,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
) {
    var day by rememberSaveable(currentDay) { mutableStateOf("$currentDay") }
    var text by rememberSaveable { mutableStateOf("") }
    val isLegitAdding by remember { derivedStateOf {
        day.isNotEmpty() && text.isNotEmpty()
    } }
    RangersDialogWithContent(
        headerId = R.string.add_note_button,
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
                value = text,
                onValueChange = { text = it },
                label = {
                    Text(text = buildAnnotatedString {
                        append(stringResource(R.string.note_text))
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
            stringId = R.string.add_note_button,
            leadingIcon = R.drawable.add_circle_32dp,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.d10,
                disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.3f)
            ),
            onClick = { addCampaignNote(day.toInt(), text); onBack() },
            isEnabled = isLegitAdding,
            modifier = Modifier.padding(8.dp)
        )
    }
}