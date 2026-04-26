package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
fun UndoTravelDialog(
    campaignViewModel: CampaignViewModel,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    user: FirebaseUser?
) {
    val isUndoAvailable = campaignViewModel.checkIfCanUndo()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()
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
    RangersDialogWithContent(
        header = stringResource(id = R.string.undo_travel_header),
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
                onClick = { coroutine.launch { showLoadingDialog = true
                    campaignViewModel.undoTravel(user)
                }.invokeOnCompletion { showLoadingDialog = false
                    onBack.invoke()
                } },
                isEnabled = isUndoAvailable,
            )
        }
    }
}