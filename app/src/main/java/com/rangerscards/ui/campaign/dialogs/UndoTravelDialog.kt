package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun UndoTravelDialog(
    checkIfCanUndo: () -> Boolean,
    undoTravel: () -> Unit,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
) {
    val isUndoAvailable = checkIfCanUndo()
    RangersDialogWithContent(
        headerId = R.string.undo_travel_header,
        isDarkTheme = isDarkTheme,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(if (isUndoAvailable) R.string.undo_available_text
                else R.string.undo_unavailable_text),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(8.dp),
            )
            SquareButton(
                stringId = R.string.undo_travel_header,
                leadingIcon = R.drawable.undo_32dp,
                buttonColor = ButtonDefaults.buttonColors().copy(
                    containerColor = CustomTheme.colors.d10,
                    disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.3f)
                ),
                onClick = { undoTravel(); onBack() },
                isEnabled = isUndoAvailable,
            )
        }
    }
}