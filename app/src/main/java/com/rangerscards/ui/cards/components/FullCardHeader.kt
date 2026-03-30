package com.rangerscards.ui.cards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.rangerscards.R
import com.rangerscards.objects.ImageSrc
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FullCardHeader(
    aspectId: String?,
    aspectShortName: String?,
    cost: Int?,
    imageSrc: String?,
    name: String,
    presence: Int?,
    approachConflict: Int?,
    approachReason: Int?,
    approachExploration: Int?,
    approachConnection: Int?,
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
        ) {
            FullCardHeaderAspectContainer(
                aspectId,
                aspectShortName,
                cost,
                imageSrc,
                isDarkTheme,
            )
            Text(
                text = name,
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (presence != null) PresenceContainer(presence, isDarkTheme)
            ApproachContainer(
                mapOf(
                    R.drawable.connection to approachConnection,
                    R.drawable.exploration to approachExploration,
                    R.drawable.reason to approachReason,
                    R.drawable.conflict to approachConflict,
                ).mapNotNull { (res, value) ->
                    value?.let { res to it }
                }.toMap(),
                isDarkTheme
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = when(aspectId) {
                "AWA" -> CustomTheme.colors.green
                "FIT" -> CustomTheme.colors.red
                "FOC" -> CustomTheme.colors.blue
                "SPI" -> CustomTheme.colors.orange
                else -> CustomTheme.colors.m
            }
        )
    }
}

@Composable
fun FullCardHeaderAspectContainer(
    aspectId: String?,
    aspectShortName: String?,
    cost: Int?,
    imageSrc: String?,
    isDarkTheme: Boolean,
) {
    Surface(
        modifier = Modifier
            .size(56.dp),
        color = when (aspectId) {
            "AWA" -> CustomTheme.colors.green
            "FIT" -> CustomTheme.colors.red
            "FOC" -> CustomTheme.colors.blue
            "SPI" -> CustomTheme.colors.orange
            else -> Color.Transparent
        },
    ) {
        if (aspectId != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painterResource(when(aspectId) {
                        "AWA" -> R.drawable.awa_chakra
                        "FIT" -> R.drawable.fit_chakra
                        "FOC" -> R.drawable.foc_chakra
                        "SPI" -> R.drawable.spi_chakra
                        else -> R.drawable.spi_chakra
                    }),
                    contentDescription = null,
                    tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.size(48.dp)
                )
                if (cost != null) {
                    Text(
                        text = when (cost) {
                            -2 -> "X"
                            else -> cost.toString()
                        },
                        color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        lineHeight = 30.sp,
                    )
                    Text(
                        text = aspectShortName.toString(),
                        color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ImageSrc.BASE_URL + imageSrc)
                    .build(),
                placeholder = painterResource(id = R.drawable.per_ranger),
                error = painterResource(id = R.drawable.per_ranger),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.graphicsLayer {
                    translationY = 6F
                    val scaleFactor = (56.dp.toPx() + 6F) / 56.dp.toPx()

                    // Apply uniform scaling.
                    scaleX = scaleFactor
                    scaleY = scaleFactor },
            )
        }
    }
}

@Composable
fun PresenceContainer(presence: Int, isDarkTheme: Boolean) {
    Surface(
        modifier = Modifier.size(36.dp),
        color = CustomTheme.colors.purple,
        shape = CustomTheme.shapes.small
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painterResource(R.drawable.presence),
                contentDescription = null,
                tint = if (isDarkTheme) CustomTheme.colors.l10.copy(alpha = 0.5f) else CustomTheme.colors.d10.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = presence.toString(),
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
}

@Composable
fun ApproachContainer(
    approachMap: Map<Int, Int>,
    isDarkTheme: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        approachMap.forEach { approach ->
            for (i in 1..approach.value) {
                Surface(
                    color = Color.Black,
                    shape = CustomTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 1.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(approach.key),
                            contentDescription = null,
                            tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}