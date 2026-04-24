package com.rangerscards.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rangerscards.ui.settings.components.RangersBaseCard

@Composable
fun RangersDialogWithContent(
    @StringRes headerId: Int,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        RangersBaseCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = headerId,
            modifier = Modifier,
            content = content
        )
    }
}