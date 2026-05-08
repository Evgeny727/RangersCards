package com.rangerscards.ui.campaign.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.CampaignEvent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CampaignEvents(
    onAdd: () -> Unit,
    events: ImmutableList<CampaignEvent>,
    onClick: (String) -> Unit,
    isViewOnly: Boolean,
    state: LazyListState,
    modifier: Modifier
) {
    Column {
        if (!isViewOnly) SquareButton(
            stringId = R.string.record_event_button,
            leadingIcon = R.drawable.add_circle_32dp,
            iconColor = CustomTheme.colors.m,
            textColor = CustomTheme.colors.d30,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.l20
            ),
            onClick = onAdd,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(
            state = state,
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            events.forEach { event ->
                item(event.name) {
                    val checks = "\u2713".repeat(event.marks)
                    Column(
                        modifier = Modifier.fillMaxWidth().clickable(!isViewOnly) { onClick.invoke(event.name) }
                    ) {
                        Text(
                            text = if (checks.isNotEmpty()) "${event.name} $checks" else event.name,
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            textDecoration = if (event.crossedOut) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
            }
        }
    }
}