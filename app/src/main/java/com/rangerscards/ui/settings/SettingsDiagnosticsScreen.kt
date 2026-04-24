package com.rangerscards.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings

@Composable
fun SettingsDiagnosticsScreen(
    clearLocalData: (String) -> Unit,
    clearImageCache: (Context, String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val localMessage = stringResource(R.string.diagnostics_clear_local_data_cleared)
        Column(modifier = Modifier.fillMaxWidth().clickable {
            clearLocalData(localMessage)
        }) {
            Text(
                text = stringResource(R.string.diagnostics_clear_local_data),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(8.dp)
            )
            HorizontalDivider(color = CustomTheme.colors.l10)
        }
        val imageMessage = stringResource(R.string.diagnostics_clear_coil_cache_cleared)
        Column(modifier = Modifier.fillMaxWidth().clickable {
            clearImageCache(context, imageMessage)
        }) {
            Text(
                text = stringResource(R.string.diagnostics_clear_coil_cache),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(8.dp)
            )
            HorizontalDivider(color = CustomTheme.colors.l10)
        }
    }
}