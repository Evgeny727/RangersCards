package com.rangerscards.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rangerscards.R
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun SettingsCard(
    isDarkTheme: Boolean,
    themeInt: Int,
    englishResults: Boolean,
    language: String,
    onSelectTheme: (Int) -> Unit,
    onSetEnglishSearchResults: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var openThemeDialog by rememberSaveable { mutableStateOf(false) }
    val currentThemeText = when (isDarkTheme) {
        false -> stringResource(id = R.string.light_theme)
        else -> stringResource(id = R.string.dark_theme)
    }
    val systemThemeText = when (isSystemInDarkTheme()) {
        false -> stringResource(id = R.string.light_theme)
        else -> stringResource(id = R.string.dark_theme)
    }
    if (openThemeDialog) RangersDialogWithContent(
        headerId = R.string.theme_header,
        isDarkTheme = isDarkTheme,
        onBack = { openThemeDialog = false }
    ) {
        RangersRadioButtonRow(
            text = stringResource(R.string.system_theme, systemThemeText),
            onValueChange = { openThemeDialog = false
                if (themeInt != 2) onSelectTheme(2) },
            isSelected = themeInt == 2,
            isSingleValue = true
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = CustomTheme.colors.l10
        )
        RangersRadioButtonRow(
            text = stringResource(R.string.light_theme),
            onValueChange = { openThemeDialog = false
                if (themeInt != 0) onSelectTheme(0) },
            isSelected = themeInt == 0,
            isSingleValue = true
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = CustomTheme.colors.l10
        )
        RangersRadioButtonRow(
            text = stringResource(R.string.dark_theme),
            onValueChange = { openThemeDialog = false
                if (themeInt != 1) onSelectTheme(1) },
            isSelected = themeInt == 1,
            isSingleValue = true
        )
    }
    RangersBaseCard(
        isDarkTheme = isDarkTheme,
        labelIdRes = R.string.settings_title,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.background(
                if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.l20,
                CustomTheme.shapes.large
            ),
        ) {
            SettingsClickableSurface(
                leadingIcon = R.drawable.theme_32dp,
                trailingIcon = R.drawable.edit_32dp,
                headerId = R.string.theme_header,
                text = currentThemeText,
                { openThemeDialog = true }
            )
        }
        if (language != "en") RangersRadioButtonRow(
            text = stringResource(id = R.string.english_search_results_radio_button),
            onValueChange = onSetEnglishSearchResults,
            leadingIcon = R.drawable.search_32dp,
            isSelected = englishResults
        )
    }
}