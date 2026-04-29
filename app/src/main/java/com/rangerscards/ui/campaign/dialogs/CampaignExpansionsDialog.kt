package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.Campaign
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun CampaignExpansionsDialog(
    campaign: Campaign,
    updateCampaignExpansions: (List<String>) -> Unit,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
) {
    val expansions = rememberSaveable(saver = listSaver(
        save = { stateList -> stateList.toList() },
        restore = { restored -> restored.toMutableStateList() }
    )) { campaign.expansions.toMutableStateList() }
    val allExpansions = remember { (CampaignMaps.campaignExpansionsMap[campaign.cycleId] ?: emptyList()).toMutableStateList() }
    RangersDialogWithContent(
        headerId = R.string.campaign_expansions,
        isDarkTheme = isDarkTheme,
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            allExpansions.forEach { expansion ->
                item(expansion.id) {
                    val isAdded = remember(expansions) { expansions.contains(expansion.id) }
                    RangersRadioButtonRow(
                        text = stringResource(expansion.name),
                        isSelected = isAdded,
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) { value ->
                        if (value) expansions.add(expansion.id)
                        else expansions.remove(expansion.id)
                    }
                }
            }
        }
        SquareButton(
            stringId = R.string.done_button,
            leadingIcon = R.drawable.done_32dp,
            onClick = { updateCampaignExpansions(expansions); onBack() },
            buttonColor = ButtonDefaults.buttonColors().copy(
                CustomTheme.colors.d10,
                disabledContainerColor = CustomTheme.colors.m
            ),
            modifier = Modifier.padding(8.dp),
        )
    }
}