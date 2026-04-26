package com.rangerscards.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun DataPicker(
    onClick: () -> Unit,
    @StringRes type: Int,
    modifier: Modifier = Modifier,
    isRequired: Boolean = true,
    text: @Composable (RowScope.() -> Unit),
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CustomTheme.shapes.small,
        color = Color.Transparent,
        border = BorderStroke(1.dp, CustomTheme.colors.m)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp)) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(type))
                        if (isRequired) withStyle(
                            style = SpanStyle(color = CustomTheme.colors.warn)
                        ) {
                            append("*")
                        }
                        append(" - ")
                    },
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
                text()
            }
            Icon(
                painterResource(R.drawable.edit_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}