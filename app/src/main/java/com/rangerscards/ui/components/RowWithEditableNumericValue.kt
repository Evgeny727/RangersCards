package com.rangerscards.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun RowWithEditableNumericValue(
    @StringRes textResId: Int,
    onMinusClicked: () -> Unit,
    isMinusEnabled: Boolean,
    onPlusClicked: () -> Unit,
    isPlusEnabled: Boolean,
    numericValue: Int
) {
    Row(
        modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(textResId),
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMinusClicked,
                colors = IconButtonDefaults.iconButtonColors()
                    .copy(containerColor = Color.Transparent),
                modifier = Modifier.size(32.dp),
                enabled = isMinusEnabled
            ) {
                Icon(
                    painterResource(id = R.drawable.remove_32dp),
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(32.dp)
                )
            }
            Surface(
                color = Color.Transparent,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = numericValue.toString(),
                        color = CustomTheme.colors.d10,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                    )
                }
            }
            IconButton(
                onClick = onPlusClicked,
                colors = IconButtonDefaults.iconButtonColors()
                    .copy(containerColor = Color.Transparent),
                modifier = Modifier.size(32.dp),
                enabled = isPlusEnabled
            ) {
                Icon(
                    painterResource(id = R.drawable.add_32dp),
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}