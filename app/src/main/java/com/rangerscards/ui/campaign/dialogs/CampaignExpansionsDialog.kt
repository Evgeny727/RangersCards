package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.ui.campaign.CampaignViewModel
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.RangersRadioButton
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.RangersBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun CampaignExpansionsDialog(
    campaignViewModel: CampaignViewModel,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    user: FirebaseUser?
) {
    val campaign by campaignViewModel.campaign.collectAsState()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    val expansions = rememberSaveable(saver = listSaver(
        save = { stateList -> stateList.toList() },
        restore = { restored -> restored.toMutableStateList() }
    )) { campaign!!.expansions.toMutableStateList() }
    val allExpansions = remember { (CampaignMaps.campaignExpansionsMap[campaign!!.cycleId] ?: emptyList()).toMutableStateList() }
    val coroutine = rememberCoroutineScope()
    RangersDialogWithContent(
        header = stringResource(id = R.string.campaign_expansions),
        isDarkTheme = isDarkTheme,
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            allExpansions.forEach { expansion ->
                item {
                    val isAdded = expansions.contains(expansion.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isAdded) expansions.remove(expansion.id)
                                else expansions.add(expansion.id)
                            },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(expansion.name),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        RangersRadioButton(
                            selected = isAdded,
                            onClick = {
                                if (isAdded) expansions.remove(expansion.id)
                                else expansions.add(expansion.id)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                }
            }
        }
        SquareButton(
            stringId = R.string.done_button,
            leadingIcon = R.drawable.done_32dp,
            onClick = { coroutine.launch { showLoadingDialog = true
                campaignViewModel.updateCampaignExpansions(expansions, user)
            }.invokeOnCompletion { showLoadingDialog = false
                onBack.invoke() }
            },
            buttonColor = ButtonDefaults.buttonColors().copy(
                CustomTheme.colors.d10,
                disabledContainerColor = CustomTheme.colors.m
            ),
            isEnabled = expansions != campaign!!.expansions,
            modifier = Modifier.padding(8.dp),
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