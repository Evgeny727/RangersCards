package com.rangerscards.ui.settings.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.User
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import java.util.Locale

@Composable
fun CardsCard(
    isDarkTheme: Boolean,
    userUIState: User,
    navigateToCollection: () -> Unit,
    updateLocale: (String) -> Unit,
    updateCards: () -> Unit,
    setTaboo: (String?, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var openLanguagePickerDialog by rememberSaveable { mutableStateOf(false) }
    val selectedLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
    var amount by rememberSaveable(userUIState.settings) {
        mutableIntStateOf(userUIState.settings.collection.size)
    }
    var taboo by rememberSaveable(userUIState.settings) {
        mutableStateOf(userUIState.settings.taboo)
    }

    LaunchedEffect(userUIState.settings) {
        amount = userUIState.settings.collection.size
        taboo = userUIState.settings.taboo
    }

    if (openLanguagePickerDialog) RangersDialogWithContent(
        headerId = R.string.language_header,
        isDarkTheme = isDarkTheme,
        onBack = { openLanguagePickerDialog = false },
    ) {
        Text(
            text = stringResource(id = R.string.info_text_about_locale_switching),
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.2.sp,
            modifier = modifier.padding(horizontal = 4.dp)
        )
        RangersRadioButtonRow(
            text = Locale.forLanguageTag("en").displayLanguage,
            onValueChange = { openLanguagePickerDialog = false
                if (selectedLocale.language != "en")
                    updateLocale("en") },
            isSelected = selectedLocale.language == "en",
            isSingleValue = true
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = CustomTheme.colors.l10
        )
        RangersRadioButtonRow(
            text = Locale.forLanguageTag("ru").displayLanguage,
            onValueChange = { openLanguagePickerDialog = false
                if (selectedLocale.language != "ru")
                    updateLocale("ru") },
            isSelected = selectedLocale.language == "ru",
            isSingleValue = true
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = CustomTheme.colors.l10
        )
        RangersRadioButtonRow(
            text = Locale.forLanguageTag("de").displayLanguage,
            onValueChange = { openLanguagePickerDialog = false
                if (selectedLocale.language != "de")
                    updateLocale("de") },
            isSelected = selectedLocale.language == "de",
            isSingleValue = true
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = CustomTheme.colors.l10
        )
        RangersRadioButtonRow(
            text = Locale.forLanguageTag("fr").displayLanguage,
            onValueChange = { openLanguagePickerDialog = false
                if (selectedLocale.language != "fr")
                    updateLocale("fr") },
            isSelected = selectedLocale.language == "fr",
            isSingleValue = true
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = CustomTheme.colors.l10
        )
        RangersRadioButtonRow(
            text = Locale.forLanguageTag("it").displayLanguage,
            onValueChange = { openLanguagePickerDialog = false
                if (selectedLocale.language != "it")
                    updateLocale("it") },
            isSelected = selectedLocale.language == "it",
            isSingleValue = true
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = CustomTheme.colors.l10
        )
        RangersRadioButtonRow(
            text = Locale.forLanguageTag("es").displayLanguage,
            onValueChange = { openLanguagePickerDialog = false
                if (selectedLocale.language != "es")
                    updateLocale("es") },
            isSelected = selectedLocale.language == "es",
            isSingleValue = true
        )
    }
    RangersBaseCard(
        isDarkTheme = isDarkTheme,
        labelIdRes = R.string.cards_title,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.background(
                if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.l20,
                CustomTheme.shapes.large
            ),
        ) {
            SettingsClickableSurface(
                leadingIcon = R.drawable.language_32dp,
                trailingIcon = R.drawable.edit_32dp,
                headerId = R.string.language_header,
                text = selectedLocale.displayLanguage,
                { openLanguagePickerDialog = true }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = CustomTheme.colors.l10
            )
            SettingsClickableSurface(
                leadingIcon = R.drawable.cards_32dp,
                trailingIcon = R.drawable.edit_32dp,
                headerId = R.string.collection_header,
                text = pluralStringResource(id = R.plurals.expansions_amount, count = amount, amount),
                onClick = navigateToCollection
            )
        }
        SquareButton(
            stringId = R.string.update_cards_button,
            leadingIcon = R.drawable.reshuffle,
            onClick = updateCards
        )
        //TODO:Implement rules
//        SquareButton(
//            stringId = R.string.rules_button,
//            leadingIcon = R.drawable.book_32dp,
//            onClick = {  }
//        )
        RangersRadioButtonRow(
            text = stringResource(R.string.use_taboo),
            onValueChange = { setTaboo(userUIState.userInfo?.id, it) },
            leadingIcon = R.drawable.uncommon_wisdom,
            isSelected = taboo
        )
    }
}