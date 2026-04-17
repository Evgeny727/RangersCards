package com.rangerscards.ui.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.settings.openLink
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun SocialsCard(
    isDarkTheme: Boolean,
    settingsViewModel: SettingsViewModel,
    language: String,
    modifier: Modifier = Modifier
) {
    SettingsBaseCard(
        isDarkTheme = isDarkTheme,
        labelIdRes = R.string.socials_title,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.socials_text),
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.2.sp,
            modifier = modifier.padding(horizontal = 8.dp)
        )
        val context = LocalContext.current
        when(language) {
            "ru" -> SquareButton(
                stringId = R.string.telegram_button,
                leadingIcon = R.drawable.telegram,
                onClick = { context.openLink("https://t.me/rangersgameru") }
            )
            else -> SquareButton(
                stringId = R.string.discord_button,
                leadingIcon = R.drawable.discord,
                onClick = { context.openLink("https://discord.gg/pw3Cye8NQR") }
            )
        }
    }
}