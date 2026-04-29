package com.rangerscards.ui.campaign.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.Campaign
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun EndTheDayDialog(
    campaign: Campaign,
    setCampaignDay: () -> Unit,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
) {
    RangersDialogWithContent(
        headerId = R.string.end_the_day,
        isDarkTheme = isDarkTheme,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.ending_day_number, campaign.currentDay),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 18.sp,
            )
            Text(
                text = stringResource(R.string.ending_day_warning),
                color = CustomTheme.colors.d20,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                lineHeight = 18.sp,
            )
        }
        SquareButton(
            stringId = R.string.end_the_day,
            leadingIcon = R.drawable.camp_32dp,
            onClick = { setCampaignDay(); onBack() },
            modifier = Modifier.padding(8.dp)
        )
    }
}