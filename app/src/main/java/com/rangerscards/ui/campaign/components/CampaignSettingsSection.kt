package com.rangerscards.ui.campaign.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun CampaignSettingsSection(
    onAddOrRemovePlayers: () -> Unit,
    onUploadCampaign: (() -> Unit)?,
    onDeleteOrLeaveCampaign: () -> Unit,
    onCampaignExpansions: (() -> Unit)?,
    onPreviousCampaign: (() -> Unit)?,
    onNextCampaign: (() -> Unit)?,
    isOwner: Boolean,
    isUploaded: Boolean,
    isViewOnly: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.settings_section_header),
                color = CustomTheme.colors.d10,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 20.sp,
            )
        }
        if (isUploaded && !isViewOnly) SquareButton(
            stringId = R.string.add_remove_players_button,
            leadingIcon = R.drawable.add_circle_32dp,
            iconColor = CustomTheme.colors.m,
            textColor = CustomTheme.colors.d30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.l20
            ),
            onClick = onAddOrRemovePlayers,
        )
        if (onPreviousCampaign != null) SquareButton(
            stringId = R.string.campaign_previous,
            leadingIcon = R.drawable.arrow_back_32dp,
            iconColor = CustomTheme.colors.m,
            textColor = CustomTheme.colors.d30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.l20
            ),
            onClick = onPreviousCampaign,
        )
        if (onNextCampaign != null) SquareButton(
            stringId = R.string.campaign_next,
            leadingIcon = R.drawable.arrow_forward_32dp,
            iconColor = CustomTheme.colors.m,
            textColor = CustomTheme.colors.d30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.l20
            ),
            onClick = onNextCampaign,
        )
        if (onUploadCampaign != null && !isViewOnly) SquareButton(
            stringId = R.string.upload_to_rangersdb,
            leadingIcon = R.drawable.language_32dp,
            iconColor = CustomTheme.colors.m,
            textColor = CustomTheme.colors.d30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.l20
            ),
            onClick = onUploadCampaign,
        )
        if (onCampaignExpansions != null && !isViewOnly) SquareButton(
            stringId = R.string.campaign_expansions,
            leadingIcon = R.drawable.settings_32dp,
            iconColor = CustomTheme.colors.m,
            textColor = CustomTheme.colors.d30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.l20
            ),
            onClick = onCampaignExpansions,
        )
        if (!isViewOnly) SquareButton(
            stringId = if (isOwner) R.string.delete_campaign_button else R.string.leave_campaign_button,
            leadingIcon = R.drawable.delete_32dp,
            iconColor = CustomTheme.colors.warn,
            textColor = CustomTheme.colors.l30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.d30
            ),
            onClick = onDeleteOrLeaveCampaign,
        )
    }
}