package com.rangerscards.ui.cards.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.rangerscards.R
import com.rangerscards.domain.model.CardApproaches
import com.rangerscards.domain.model.CardAspect
import com.rangerscards.objects.ImageSrc
import com.rangerscards.ui.settings.components.RangersRadioButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun CardListItem(
    tabooId: String?,
    aspect: CardAspect?,
    cost: Int?,
    imageSrc: String?,
    approaches: CardApproaches,
    name: String,
    typeName: String?,
    traits: String?,
    level: Int?,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    charForAmount: String? = null,
    currentAmount: Int? = null,
    onAddClick: (() -> Unit)? = null,
    onAddEnabled: Boolean = false,
    onRemoveClick: (() -> Unit)? = null,
    onRemoveEnabled: Boolean = false,
    onSelectedChange: ((Boolean) -> Unit)? = null,
    isSelected: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Top
            ) {
                CardListItemImageContainer(
                    aspect,
                    cost,
                    imageSrc,
                    name,
                    isDarkTheme,
                    Modifier.align(Alignment.CenterVertically)
                )
                CardListItemTextContainer(
                    name,
                    typeName,
                    traits,
                    if (level == null) tabooId else null,
                    Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    CardListItemApproachContainer(
                        approaches,
                        isDarkTheme
                    )
                    if (level != null)
                        CardListItemLevelContainer(tabooId, aspect, level, isDarkTheme)
                }
                CardListItemDeckInfo(
                    charForAmount,
                    currentAmount,
                    onAddClick,
                    onAddEnabled,
                    onRemoveClick,
                    onRemoveEnabled,
                    onSelectedChange,
                    isSelected,
                )
            }
            HorizontalDivider(
                color = CustomTheme.colors.l10
            )
        }
    }
}

@Composable
fun CardListItemImageContainer(
    aspect: CardAspect?,
    cost: Int?,
    imageSrc: String?,
    name: String,
    isDarkTheme: Boolean,
    modifier: Modifier
) {
    Surface(
        modifier = modifier
            .sizeIn(maxHeight = 40.dp)
            .aspectRatio(1f),
        shape = CustomTheme.shapes.small,
        color = when (aspect?.id) {
            "AWA" -> CustomTheme.colors.green
            "FIT" -> CustomTheme.colors.red
            "FOC" -> CustomTheme.colors.blue
            "SPI" -> CustomTheme.colors.orange
            else -> Color.Transparent
        },
        border = BorderStroke(
            1.dp,
            if (aspect != null) Color.Transparent else CustomTheme.colors.d10),
    ) {
        if (aspect != null) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(when(aspect.id) {
                        "AWA" -> R.drawable.awa_chakra
                        "FIT" -> R.drawable.fit_chakra
                        "FOC" -> R.drawable.foc_chakra
                        "SPI" -> R.drawable.spi_chakra
                        else -> R.drawable.spi_chakra
                    }),
                    contentDescription = null,
                    tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.fillMaxSize(0.9f)
                )
                if (cost != null) {
                    Text(
                        text = when(cost) {
                            -2 -> "X"
                            else -> cost.toString()
                        },
                        color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                    )
                    Text(
                        text = aspect.shortName,
                        color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
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
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.graphicsLayer { translationY = 3F }
            )
        }
    }
}

@Composable
fun CardListItemTextContainer(
    name: String,
    typeName: String?,
    traits: String?,
    tabooId: String?,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = name,
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
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
                color = CustomTheme.colors.d10,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (tabooId != null) Icon(
                painterResource(R.drawable.uncommon_wisdom),
                contentDescription = null,
                tint = CustomTheme.colors.d10,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun CardListItemApproachContainer(
    approaches: CardApproaches,
    isDarkTheme: Boolean
) {
    val approachMap = mapOf(
        R.drawable.connection to approaches.connection,
        R.drawable.exploration to approaches.exploration,
        R.drawable.reason to approaches.reason,
        R.drawable.conflict to approaches.conflict,
    ).mapNotNull { (res, value) ->
        value?.let { res to it }
    }.toMap()
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        approachMap.forEach { approach ->
            repeat(approach.value) {
                Surface(
                    color = Color.Black,
                    shape = CustomTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(approach.key),
                            contentDescription = null,
                            tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardListItemLevelContainer(
    tabooId: String?,
    aspect: CardAspect?,
    level: Int?,
    isDarkTheme: Boolean
) {
    Surface(
        shape = CustomTheme.shapes.small,
        color = when (aspect?.id) {
            "AWA" -> CustomTheme.colors.green
            "FIT" -> CustomTheme.colors.red
            "FOC" -> CustomTheme.colors.blue
            "SPI" -> CustomTheme.colors.orange
            else -> Color.Transparent
        },
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = (level ?: 0).toString() + " " + aspect?.shortName.toString(),
                color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 16.sp,
            )
            if (tabooId != null) Icon(
                painterResource(R.drawable.uncommon_wisdom),
                contentDescription = null,
                tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun CardListItemDeckInfo(
    charForAmount: String?,
    currentAmount: Int?,
    onAddClick: (() -> Unit)?,
    onAddEnabled: Boolean,
    onRemoveClick: (() -> Unit)?,
    onRemoveEnabled: Boolean,
    onSelectedChange: ((Boolean) -> Unit)?,
    isSelected: Boolean,
) {
    val animatedAlpha by animateFloatAsState(targetValue = if ((currentAmount ?: 0) > 0) 1f else 0.5f)
    Row(
        modifier = Modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onRemoveClick != null) IconButton(
            onClick = onRemoveClick,
            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
            modifier = Modifier.size(24.dp),
            enabled = onRemoveEnabled
        ) {
            Icon(
                painterResource(id = R.drawable.remove_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(24.dp)
            )
        }
        if (currentAmount != null) Surface(
            modifier = Modifier.fillMaxHeight().alpha(animatedAlpha),
            color = CustomTheme.colors.l10,
            shape = CustomTheme.shapes.small,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 6.dp).sizeIn(minWidth = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${ if (currentAmount < 0) "" else (charForAmount ?: "×")}$currentAmount",
                    color = CustomTheme.colors.d10,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                )
            }
        }
        if (onAddClick != null) IconButton(
            onClick = onAddClick,
            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
            modifier = Modifier.size(24.dp),
            enabled = onAddEnabled
        ) {
            Icon(
                painterResource(id = R.drawable.add_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(24.dp)
            )
        }
        if (onSelectedChange != null) RangersRadioButton(
            selected = isSelected,
            onClick = onSelectedChange,
            modifier = Modifier.size(24.dp)
        )
    }
}