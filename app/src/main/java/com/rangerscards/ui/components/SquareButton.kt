package com.rangerscards.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun SquareButton(
    @StringRes stringId: Int,
    @DrawableRes leadingIcon: Int? = null,
    buttonColor: ButtonColors = ButtonDefaults.buttonColors().copy(CustomTheme.colors.d10),
    iconColor: Color = CustomTheme.colors.l20,
    textColor: Color = CustomTheme.colors.l30,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isEnabled: Boolean = true
) {
    Button(
        onClick = { onClick() },
        modifier = modifier,
        shape = CustomTheme.shapes.small,
        colors = buttonColor,
        contentPadding = PaddingValues(8.dp),
        enabled = isEnabled
    ) {
        leadingIcon?.let { iconId ->
            Icon(
                painterResource(id = iconId),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = stringResource(id = stringId),
            modifier = Modifier.fillMaxWidth(),
            color = textColor,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        )
    }
}