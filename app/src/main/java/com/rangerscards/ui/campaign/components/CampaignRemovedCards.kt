package com.rangerscards.ui.campaign.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.campaigns.CampaignRemoved
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun CampaignRemovedCards(
    onAdd: () -> Unit,
    removedSets: Map<String, Pair<Int?, Int>>,
    removed: List<CampaignRemoved>,
    onRemove: (String) -> Unit,
    state: LazyListState,
    nestedConnectionModifier: Modifier
) {
    Column {
        Text(
            text = stringResource(R.string.removed_campaign_log_text),
            color = CustomTheme.colors.d10,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        )
        SquareButton(
            stringId = R.string.remove_card_button,
            leadingIcon = R.drawable.add_circle_32dp,
            iconColor = CustomTheme.colors.m,
            textColor = CustomTheme.colors.d30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.l20
            ),
            onClick = onAdd,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        LazyColumn(
            state = state,
            modifier = nestedConnectionModifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            removed.forEach { removed ->
                item(removed.name) {
                    val removedSetInfo = removedSets[removed.setId]!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = removed.name,
                                color = CustomTheme.colors.d30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                lineHeight = 18.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (removedSetInfo.first != null) Icon(
                                    painterResource(removedSetInfo.first!!),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = stringResource(removedSetInfo.second),
                                    color = CustomTheme.colors.d30,
                                    fontFamily = Jost,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    lineHeight = 18.sp,
                                )
                            }
                        }
                        IconButton(
                            onClick = { onRemove.invoke(removed.name) },
                            colors = IconButtonDefaults.iconButtonColors()
                                .copy(containerColor = Color.Transparent),
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                painterResource(id = R.drawable.cancel_32dp),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}