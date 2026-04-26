package com.rangerscards.ui.campaign.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.objects.ChallengeDeck
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun ChallengeCard(
    cardId: Int,
    isDarkTheme: Boolean,
    isSmall: Boolean = false,
    onClick: ((Int) -> Unit)?
) {
    val revealedCard = ChallengeDeck.challengeDeck.getValue(cardId)
    val width = if (isSmall) 110.dp else 220.dp
    Surface(
        modifier = Modifier.width(width).clickable(onClick != null) { onClick?.invoke(cardId) },
        shape = if (isSmall) CustomTheme.shapes.small  else CustomTheme.shapes.large,
        color = Color.Transparent,
        shadowElevation = if (isSmall) 2.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(width / 2),
                    color = CustomTheme.colors.green
                ) {
                    val value = revealedCard.awa
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.awa_chakra),
                            contentDescription = null,
                            tint = (if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30)
                                .copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxSize(0.9f)
                        )
                        Text(
                            text = (if (value > 0) "+" else "") + "$value",
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSmall) 42.sp else 84.sp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                                .padding(bottom = if (isSmall) 2.dp else 4.dp)
                                .background(CustomTheme.colors.green.copy(alpha = 0.5f)),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.awa_styled_card_text),
                                color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = if (isSmall) 12.sp else 22.sp,
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier.size(width / 2),
                    color = CustomTheme.colors.orange
                ) {
                    val value = revealedCard.spi
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.spi_chakra),
                            contentDescription = null,
                            tint = (if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30)
                                .copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxSize(0.9f)
                        )
                        Text(
                            text = (if (value > 0) "+" else "") + "$value",
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSmall) 42.sp else 84.sp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                                .padding(bottom = if (isSmall) 2.dp else 4.dp)
                                .background(CustomTheme.colors.orange.copy(alpha = 0.5f)),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.spi_styled_card_text),
                                color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = if (isSmall) 12.sp else 22.sp,
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = if (isDarkTheme) CustomTheme.colors.l30
            else CustomTheme.colors.d30, thickness = if (isSmall) 1.dp else 2.dp)
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(width / 2),
                    color = CustomTheme.colors.red
                ) {
                    val value = revealedCard.fit
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.fit_chakra),
                            contentDescription = null,
                            tint = (if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30)
                                .copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxSize(0.9f)
                        )
                        Text(
                            text = (if (value > 0) "+" else "") + "$value",
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSmall) 42.sp else 84.sp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                                .padding(bottom = if (isSmall) 2.dp else 4.dp)
                                .background(CustomTheme.colors.red.copy(alpha = 0.5f)),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.fit_styled_card_text),
                                color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = if (isSmall) 12.sp else 22.sp,
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier.size(width / 2),
                    color = CustomTheme.colors.blue
                ) {
                    val value = revealedCard.foc
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.foc_chakra),
                            contentDescription = null,
                            tint = (if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30)
                                .copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxSize(0.9f)
                        )
                        Text(
                            text = (if (value > 0) "+" else "") + "$value",
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSmall) 42.sp else 84.sp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                                .padding(bottom = if (isSmall) 2.dp else 4.dp)
                                .background(CustomTheme.colors.blue.copy(alpha = 0.5f)),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.foc_styled_card_text),
                                color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = if (isSmall) 12.sp else 22.sp,
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = if (isDarkTheme) CustomTheme.colors.l30
            else CustomTheme.colors.d30, thickness = if (isSmall) 1.dp else 2.dp)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = when(revealedCard.challengeIcon) {
                    R.drawable.sun -> CustomTheme.colors.orange.copy(alpha = 0.9f)
                    R.drawable.mountain -> CustomTheme.colors.blue.copy(alpha = 0.9f)
                    else -> CustomTheme.colors.red.copy(alpha = 0.9f)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(if (isSmall) 4.dp else 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(revealedCard.challengeIcon),
                        contentDescription = null,
                        tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        modifier = Modifier.size(if (isSmall) 24.dp else 48.dp)
                    )
                    if (revealedCard.reshuffle) Icon(
                        painterResource(R.drawable.reshuffle),
                        contentDescription = null,
                        tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        modifier = Modifier.size(if (isSmall) 16.dp else 32.dp).align(Alignment.CenterEnd)
                            .offset(x = (if (isSmall) -8 else -16).dp)
                    )
                }
            }
        }
    }
}