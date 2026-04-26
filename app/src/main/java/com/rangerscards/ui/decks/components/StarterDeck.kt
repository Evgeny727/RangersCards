package com.rangerscards.ui.decks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.StarterDeck
import com.rangerscards.objects.DeckMetaMaps
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun StarterDeck(
    onclick: (String, String) -> Unit,
    isSelected: Boolean,
    imageSrc: String?,
    name: String?,
    starterDeck: StarterDeck,
    isDarkTheme: Boolean,
) {
    val background = stringResource(DeckMetaMaps.background[starterDeck.meta.background]!!)
    val specialty = stringResource(DeckMetaMaps.specialty[starterDeck.meta.specialty]!!)
    Surface(
        onClick = { onclick(background, specialty) },
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeckListItemImageContainer(
                    imageSrc,
                    Modifier.align(Alignment.CenterVertically)
                )
                StatsGrid(
                    starterDeck.awa,
                    starterDeck.fit,
                    starterDeck.foc,
                    starterDeck.spi,
                    isDarkTheme
                )
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = name ?: "",
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = buildAnnotatedString {
                            append("$background - ")
                            append(specialty)
                        },
                        color = CustomTheme.colors.d20,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors().copy(
                        selectedColor = CustomTheme.colors.m,
                        unselectedColor = CustomTheme.colors.m
                    ),
                    modifier = Modifier.size(32.dp)
                )
            }
            HorizontalDivider(
                color = CustomTheme.colors.l10
            )
        }
    }
}

@Composable
fun StatsGrid(awa: Int, fit: Int, foc: Int, spi: Int, isDarkTheme: Boolean) {
    Column(
        modifier = Modifier.clip(CustomTheme.shapes.medium)
    ) {
        Row {
            Surface(
                modifier = Modifier.size(32.dp),
                color = CustomTheme.colors.green,
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.awa_chakra),
                        contentDescription = null,
                        tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = awa.toString(),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.sizeIn(maxHeight = 16.dp)
                        )
                        Text(
                            text = stringResource(R.string.awa_styled_card_text),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.sizeIn(maxHeight = 12.dp)
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier.size(32.dp),
                color = CustomTheme.colors.orange,
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.spi_chakra),
                        contentDescription = null,
                        tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = spi.toString(),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.sizeIn(maxHeight = 16.dp)
                        )
                        Text(
                            text = stringResource(R.string.spi_styled_card_text),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.sizeIn(maxHeight = 12.dp)
                        )
                    }
                }
            }
        }
        Row {
            Surface(
                modifier = Modifier.size(32.dp),
                color = CustomTheme.colors.red,
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.fit_chakra),
                        contentDescription = null,
                        tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = fit.toString(),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.sizeIn(maxHeight = 16.dp)
                        )
                        Text(
                            text = stringResource(R.string.fit_styled_card_text),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.sizeIn(maxHeight = 12.dp)
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier.size(32.dp),
                color = CustomTheme.colors.blue,
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.foc_chakra),
                        contentDescription = null,
                        tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = foc.toString(),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.sizeIn(maxHeight = 16.dp)
                        )
                        Text(
                            text = stringResource(R.string.foc_styled_card_text),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.sizeIn(maxHeight = 12.dp)
                        )
                    }
                }
            }
        }
    }
}