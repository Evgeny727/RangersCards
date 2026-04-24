package com.rangerscards.ui.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun RangersRadioButtonRow(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    @DrawableRes leadingIcon: Int? = null,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    isSingleValue: Boolean = false,
    textStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 20.sp,
    ),
    onValueChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .toggleable(isSelected, enabled, onValueChange = onValueChange)
            .background(Color.Transparent, CustomTheme.shapes.large)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) Icon(
            painterResource(id = leadingIcon),
            contentDescription = null,
            tint = CustomTheme.colors.m,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = text,
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = textStyle.fontWeight,
            fontSize = textStyle.fontSize,
            lineHeight = textStyle.lineHeight,
            modifier = Modifier.weight(1f)
        )
        if (!isSingleValue) RangersRadioButton(
            selected = isSelected,
            enabled = enabled,
            onClick = onValueChange,
            modifier = Modifier.size(32.dp)
        ) else RadioButton(
            selected = isSelected,
            enabled = enabled,
            onClick = null,
            colors = RadioButtonDefaults.colors().copy(
                selectedColor = CustomTheme.colors.m,
                unselectedColor = CustomTheme.colors.m,
            ),
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun RangersRadioButton(
    selected: Boolean,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconToggleButton(
        checked = selected,
        onCheckedChange = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        val asset = if (selected)
            painterResource(R.drawable.radio_button_checked_32dp)
        else
            painterResource(R.drawable.radio_button_unchecked_32dp)
        Icon(
            painter = asset,
            contentDescription = if (selected) "Selected" else "Not selected",
            tint = CustomTheme.colors.m,
            modifier = Modifier.size(24.dp)
        )
    }
}