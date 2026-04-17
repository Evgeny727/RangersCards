package com.rangerscards.ui.settings.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.rangerscards.R
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.settings.email
import com.rangerscards.ui.settings.openEmail
import com.rangerscards.ui.settings.openLink
import com.rangerscards.ui.theme.CustomTheme

const val boostyLink = "https://boosty.to/rangerscards/"
const val patreonLink = "https://patreon.com/rangerscards"

@Composable
fun SupportCard(
    isDarkTheme: Boolean,
    language: String,
    navigateToAbout: () -> Unit,
    navigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    SettingsBaseCard(
        isDarkTheme = isDarkTheme,
        labelIdRes = R.string.support_title,
        modifier = modifier
    ) {
        SquareButton(
            stringId = if (language == "ru") R.string.support_text_ru else R.string.support_text,
            leadingIcon = if (language == "ru") R.drawable.boosty else R.drawable.patreon_logo,
            onClick = { context.openLink(if (language == "ru") boostyLink else patreonLink) }
        )
        SquareButton(
            stringId = R.string.about_button,
            leadingIcon = R.drawable.info_32dp,
            onClick = navigateToAbout
        )
        //TODO:Implement data export and import for backup purposes
//        SquareButton(
//            stringId = R.string.backup_data_button,
//            leadingIcon = R.drawable.book_32dp,
//            onClick = { /*TODO: Implement backup*/ }
//        )
        //TODO:Implement data export for diagnostics
        SquareButton(
            stringId = R.string.diagnostics_button,
            leadingIcon = R.drawable.build_32dp,
            onClick = navigateToDiagnostics
        )
        SquareButton(
            stringId = R.string.contact_us_button,
            leadingIcon = R.drawable.mail_32dp,
            buttonColor = ButtonDefaults.buttonColors().copy(CustomTheme.colors.gold),
            iconColor = if (isDarkTheme) CustomTheme.colors.l20 else CustomTheme.colors.d20,
            textColor = if (isDarkTheme) CustomTheme.colors.l30 else CustomTheme.colors.d30,
            onClick = { context.openEmail(email) }
        )
    }
}