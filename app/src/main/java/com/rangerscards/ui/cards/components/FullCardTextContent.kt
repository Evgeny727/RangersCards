package com.rangerscards.ui.cards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.objects.CardTextParser
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FullCardTextContent(
    aspectId: String?,
    text: String?,
    flavor: String?,
    sunChallenge: String?,
    mountainChallenge: String?,
    crestChallenge: String?,
    isDarkTheme: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 2.dp,
                color = when (aspectId) {
                    "AWA" -> CustomTheme.colors.green
                    "FIT" -> CustomTheme.colors.red
                    "FOC" -> CustomTheme.colors.blue
                    "SPI" -> CustomTheme.colors.orange
                    else -> CustomTheme.colors.m
                }
            )
            val texts: List<String> = text?.split("\n") ?: emptyList()
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (texts.isNotEmpty()) {
                    texts.forEachIndexed { index, text ->
                        val annotatedText = CardTextParser.parseCustomText(text, aspectId)
                        BasicText(
                            text = annotatedText,
                            inlineContent = CardTextParser.inlineIconsMap(CustomTheme.colors.d30),
                            style = TextStyle(
                                color = CustomTheme.colors.d30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                            )
                        )
                        if (index != texts.lastIndex) HorizontalDivider(color = CustomTheme.colors.l10)
                    }
                }
                if (flavor != null) {
                    if (texts.isNotEmpty()) HorizontalDivider(color = CustomTheme.colors.l10)
                    Text(
                        text = flavor,
                        color = CustomTheme.colors.d10,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
        if (sunChallenge != null) Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CustomTheme.colors.orange,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.sun),
                    contentDescription = null,
                    tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.size(32.dp)
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val annotatedText = CardTextParser.parseCustomText(sunChallenge, aspectId)
                    BasicText(
                        text = annotatedText,
                        inlineContent = CardTextParser.inlineIconsMap(if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30),
                        style = TextStyle(
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                        )
                    )
                }
            }
        }
        if (mountainChallenge != null) Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CustomTheme.colors.blue,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.mountain),
                    contentDescription = null,
                    tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.size(32.dp)
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val annotatedText = CardTextParser.parseCustomText(mountainChallenge, aspectId)
                    BasicText(
                        text = annotatedText,
                        inlineContent = CardTextParser.inlineIconsMap(if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30),
                        style = TextStyle(
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                        )
                    )
                }
            }
        }
        if (crestChallenge != null) Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CustomTheme.colors.red,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.crest),
                    contentDescription = null,
                    tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.size(32.dp)
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val annotatedText = CardTextParser.parseCustomText(crestChallenge, aspectId)
                    BasicText(
                        text = annotatedText,
                        inlineContent = CardTextParser.inlineIconsMap(if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30),
                        style = TextStyle(
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                        )
                    )
                }
            }
        }
    }
}