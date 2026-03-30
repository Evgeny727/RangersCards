package com.rangerscards.ui.cards.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import java.util.Locale

@Composable
fun FullCardAdditionalContent(
    aspectId: String?,
    traits: String?,
    typeId: String?,
    typeName: String?,
    equip: Int?,
    harm: Int?,
    progress: Int?,
    tokenPlurals: String?,
    tokenCount: Int?,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                if (traits != null) {
                    append("$typeName ")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("/ $traits")
                    }
                } else append(typeName)
            },
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
        AdditionalElements(
            aspectId,
            equip,
            harm,
            typeId,
            progress,
            tokenPlurals,
            tokenCount,
            isDarkTheme
        )
    }
}

@Composable
fun AdditionalElements(
    aspectId: String?,
    equip: Int?,
    harm: Int?,
    typeId: String?,
    progress: Int?,
    tokenPlurals: String?,
    tokenCount: Int?,
    isDarkTheme: Boolean
) {
    if (equip != null) EquipRow(aspectId, equip)
    if (harm != null || typeId == "being") Surface(
        modifier = Modifier.size(36.dp),
        color = CustomTheme.colors.red,
        shape = CustomTheme.shapes.small
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painterResource(R.drawable.harm),
                contentDescription = null,
                tint = if (isDarkTheme) CustomTheme.colors.l10 else CustomTheme.colors.d10,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = harm?.toString() ?: "/",
                    color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    if (progress != null) Surface(
        modifier = Modifier.size(36.dp),
        color = CustomTheme.colors.blue,
        shape = CustomTheme.shapes.small
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painterResource(R.drawable.progress),
                contentDescription = null,
                tint = if (isDarkTheme) CustomTheme.colors.l10 else CustomTheme.colors.d10,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = progress.toString(),
                    color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    if (tokenCount != null) TokenContainer(aspectId, tokenPlurals, tokenCount, isDarkTheme)
}

@Composable
fun EquipRow(aspectId: String?, equip: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..5) {
            Surface(
                modifier = Modifier.size(16.dp),
                color = if (i <= equip) when (aspectId) {
                    "AWA" -> CustomTheme.colors.green
                    "FIT" -> CustomTheme.colors.red
                    "FOC" -> CustomTheme.colors.blue
                    "SPI" -> CustomTheme.colors.orange
                    else -> Color.Transparent
                } else CustomTheme.colors.l10,
                border = BorderStroke(1.dp, CustomTheme.colors.d10),
                shape = CustomTheme.shapes.tiny
            ) {}
        }
    }
}

@Composable
fun TokenContainer(
    aspectId: String?,
    tokenPlurals: String?,
    tokenCount: Int,
    isDarkTheme: Boolean
) {
    Surface(
        color = when (aspectId) {
            "AWA" -> CustomTheme.colors.green
            "FIT" -> CustomTheme.colors.red
            "FOC" -> CustomTheme.colors.blue
            "SPI" -> CustomTheme.colors.orange
            else -> Color.Transparent
        },
        shape = CustomTheme.shapes.small
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = if (tokenCount == -2) "X" else tokenCount.toString(),
                color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                fontFamily = Jost,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                modifier = Modifier.sizeIn(maxHeight = 22.dp)
            )
            Text(
                text = getPlural(
                    Locale.getDefault().language.substring(0..1),
                    tokenPlurals ?: "",
                    tokenCount
                ),
                color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

private fun getPlural(lang: String, plurals: String, count: Int): String {
    // Split the input string by commas and trim any extra spaces.
    val forms = plurals.split(",")

    return when (lang) {
        "ru" -> {
            // Ensure we have exactly three forms for Russian.
            if (forms.size != 3) {
                // Fallback: return the first form if the count of forms is not three.
                forms.firstOrNull() ?: ""
            } else {
                when {
                    (count % 10 == 1 && count % 100 != 11) -> forms[0]
                    (count % 10 in 2..4 && (count % 100 < 10 || count % 100 >= 20)) -> forms[1]
                    else -> forms[2]
                }
            }
        }
        // For languages like English, German, Italian, French, and as a default:
        else -> {
            // If count is not 1 and there is a plural form available, use it.
            if (count != 1 && forms.size > 1) forms[1] else forms.firstOrNull() ?: ""
        }
    }
}