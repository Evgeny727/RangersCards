package com.rangerscards.ui.settings.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.ui.theme.RangersCardsTheme

@Composable
fun RangersBaseCard(
    isDarkTheme: Boolean,
    @StringRes labelIdRes: Int,
    formatArgs: Any? = null,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit),
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = CustomTheme.shapes.large,
        color = CustomTheme.colors.l30,
        border = BorderStroke(1.dp, if (isDarkTheme) Color.Transparent else CustomTheme.colors.d20),
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(
                        if (isDarkTheme) CustomTheme.colors.l20 else CustomTheme.colors.d20,
                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
            ) {
                Text(
                    text = if (formatArgs == null) stringResource(id = labelIdRes)
                        else stringResource(id = labelIdRes, formatArgs),
                    color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    style = CustomTheme.typography.headline,
                )
            }
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsClickableSurface(
    @DrawableRes leadingIcon: Int,
    @DrawableRes trailingIcon: Int,
    @StringRes headerId: Int,
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CustomTheme.shapes.large,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(id = leadingIcon),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = stringResource(id = headerId),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                )
                Text(
                    text = text,
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                )
            }
            Icon(
                painterResource(id = trailingIcon),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable fun TextWhenNotLoggedIn(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.text_when_not_logged_in),
        color = CustomTheme.colors.d30,
        fontFamily = Jost,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp,
        modifier = modifier.padding(horizontal = 4.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    RangersCardsTheme {
        Column(
            modifier = Modifier
                .background(CustomTheme.colors.l10)
                .fillMaxSize()
        ) {
            RangersBaseCard(labelIdRes = R.string.account_title, isDarkTheme = false) {
                TextWhenNotLoggedIn()
                Column(
                    modifier = Modifier.background(
                        CustomTheme.colors.l20,
                        CustomTheme.shapes.large
                    ),
                ) {
                    SettingsClickableSurface(
                        leadingIcon = R.drawable.badge_32dp,
                        trailingIcon = R.drawable.edit_32dp,
                        headerId = R.string.account_name_header,
                        text = "Evgeny727",
                    ) {}
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = CustomTheme.colors.l10
                    )
                    SettingsClickableSurface(
                        leadingIcon = R.drawable.group_32dp,
                        trailingIcon = R.drawable.add_32dp,
                        headerId = R.string.friends_amount_header,
                        text = "3 friends",
                    ) {}
                }
                SquareButton(
                    R.string.sign_out_account_button,
                    R.drawable.logout_32dp,
                    onClick = {}
                )
            }
        }
    }
}