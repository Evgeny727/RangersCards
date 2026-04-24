package com.rangerscards.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rangerscards.R
import com.rangerscards.domain.model.User
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.utils.applyScaffoldPaddings

@Composable
fun SettingsCollectionScreen(
    user: User,
    setCollection: (String?, List<String>) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val userCollection = user.settings.collection
    LazyColumn(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item("loa") {
            val selected = remember(userCollection) { userCollection.contains("loa") }
            Column {
                RangersRadioButtonRow(
                    textId = R.string.loa_expansion,
                    isSelected = selected,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                ) { value ->
                    setCollection(
                        user.userInfo?.id,
                        if (value) userCollection + "loa" else userCollection.filterNot { it == "loa" }
                    )
                }
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
        }
        item("sotv") {
            val selected = remember(userCollection) { userCollection.contains("sotv") }
            Column {
                RangersRadioButtonRow(
                    textId = R.string.sotv_expansion,
                    isSelected = selected,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                ) { value ->
                    setCollection(
                        user.userInfo?.id,
                        if (value) userCollection + "sotv" else userCollection.filterNot { it == "sotv" }
                    )
                }
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
        }
        item("sib") {
            val selected = remember(userCollection) { userCollection.contains("sib") }
            Column {
                RangersRadioButtonRow(
                    textId = R.string.spire_in_bloom,
                    isSelected = selected,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                ) { value ->
                    setCollection(
                        user.userInfo?.id,
                        if (value) userCollection + "sib" else userCollection.filterNot { it == "sib" }
                    )
                }
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
        }
        item("sos") {
            val selected = remember(userCollection) { userCollection.contains("sos") }
            Column {
                RangersRadioButtonRow(
                    textId = R.string.shadow_of_the_storm,
                    isSelected = selected,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                ) { value ->
                    setCollection(
                        user.userInfo?.id,
                        if (value) userCollection + "sos" else userCollection.filterNot { it == "sos" }
                    )
                }
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
        }
    }
}