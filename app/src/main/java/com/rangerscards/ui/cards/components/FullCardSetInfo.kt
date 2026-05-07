package com.rangerscards.ui.cards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import com.rangerscards.domain.model.CardAspect
import com.rangerscards.domain.model.CardSet
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FullCardSetInfo(
    tabooId: String?,
    aspect: CardAspect?,
    level: Int?,
    set: CardSet,
    subset: CardSet?,
    packShortName: String?,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (level != null) Surface(
            color = when (aspect?.id) {
                "AWA" -> CustomTheme.colors.green
                "FIT" -> CustomTheme.colors.red
                "FOC" -> CustomTheme.colors.blue
                "SPI" -> CustomTheme.colors.orange
                else -> Color.Transparent
            },
            modifier = Modifier
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = level.toString() + " " + aspect?.shortName.toString(),
                    color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 20.sp
                )
                if (tabooId != null) Icon(
                    painterResource(R.drawable.uncommon_wisdom),
                    contentDescription = null,
                    tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Surface(
            color = CustomTheme.colors.l10,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = set.name + " - " + stringResource(
                        R.string.set_info,
                        subset?.position ?: set.position, subset?.size ?: set.size
                    ) + if (packShortName != null) " ($packShortName)" else "",
                    color = CustomTheme.colors.d20,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                )
                if (tabooId != null && level == null) Icon(
                    painterResource(R.drawable.uncommon_wisdom),
                    contentDescription = null,
                    tint = CustomTheme.colors.d20,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}