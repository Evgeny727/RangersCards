package com.rangerscards.ui.campaign.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun CampaignTitleRow(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(R.drawable.guide),
            contentDescription = null,
            tint = CustomTheme.colors.m,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = title,
            color = CustomTheme.colors.d20,
            fontFamily = Jost,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painterResource(id = R.drawable.edit_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}