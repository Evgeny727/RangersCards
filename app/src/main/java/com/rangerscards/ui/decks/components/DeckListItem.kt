package com.rangerscards.ui.decks.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.rangerscards.R
import com.rangerscards.domain.model.DeckMeta
import com.rangerscards.objects.DeckMetaMaps
import com.rangerscards.objects.ImageSrc
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun DeckListItem(
    meta: DeckMeta,
    imageSrc: String?,
    name: String,
    roleName: String?,
    onClick: () -> Unit,
    isCampaign: Boolean? = null, //true - display campaign icon, false - display ranger icon
    campaignName: String? = null,
    userName: String? = null,
    onRemoveDeck: (() -> Unit)? = null,
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
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Top
            ) {
                DeckListItemImageContainer(
                    imageSrc,
                    Modifier.align(Alignment.CenterVertically)
                )
                DeckListItemTextContainer(
                    meta,
                    name,
                    roleName,
                    isCampaign,
                    if (isCampaign == true) campaignName else if (isCampaign == false) userName else null,
                    Modifier.weight(1f)
                )
                if (onRemoveDeck != null)
                    IconButton(
                        onClick = onRemoveDeck,
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(32.dp).align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painterResource(R.drawable.cancel_32dp),
                            contentDescription = "Remove deck",
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                if (!meta.problems.isNullOrEmpty()) Icon(
                    painterResource(R.drawable.error_32dp),
                    contentDescription = "Error",
                    tint = CustomTheme.colors.warn,
                    modifier = Modifier.size(24.dp)
                )
            }
            HorizontalDivider(
                color = CustomTheme.colors.l10
            )
        }
    }
}

@Composable
fun DeckListItemImageContainer(
    imageSrc: String?,
    modifier: Modifier
) {
    Surface(
        modifier = modifier
            .sizeIn(maxHeight = 64.dp)
            .aspectRatio(1f),
        shape = CustomTheme.shapes.medium,
        color = Color.Transparent,
        border = BorderStroke(1.dp, CustomTheme.colors.d10),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(ImageSrc.BASE_URL + imageSrc)
                .build(),
            placeholder = painterResource(id = R.drawable.broken_image_32dp),
            error = painterResource(id = R.drawable.broken_image_32dp),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.graphicsLayer {
                translationY = 6F
                val scaleFactor = (64.dp.toPx() + 6F) / 64.dp.toPx()

                // Apply uniform scaling.
                scaleX = scaleFactor
                scaleY = scaleFactor },
        )
    }
}

@Composable
fun DeckListItemTextContainer(
    meta: DeckMeta,
    name: String,
    roleName: String?,
    isCampaign: Boolean?,
    campaignOrUserName: String?,
    modifier: Modifier = Modifier
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
        val background = DeckMetaMaps.background[meta.background]
        val specialty = DeckMetaMaps.specialty[meta.specialty]
        Text(
            text = buildAnnotatedString {
                if (background != null)
                    append(stringResource(background) + " - ")
                if (specialty != null)
                    append(stringResource(specialty))
                if (roleName != null) append(" - $roleName")
            },
            color = CustomTheme.colors.d20,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            fontSize = 16.sp,
            lineHeight = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isCampaign != null) {
            val iconId = if (isCampaign) "campaign" else "ranger"
            BasicText(
                text = buildAnnotatedString {
                    appendInlineContent(
                        iconId,
                        "[$iconId]"
                    )
                    append(" $campaignOrUserName")
                },
                inlineContent = mapOf(
                    "campaign" to InlineTextContent(
                        Placeholder(
                            width = 20.sp,
                            height = 16.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.guide),
                            contentDescription = "Campaign Icon",
                            tint = CustomTheme.colors.d10
                        )
                    },
                    "ranger" to InlineTextContent(
                        Placeholder(
                            width = 16.sp,
                            height = 16.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ranger),
                            contentDescription = "Ranger Icon",
                            tint = CustomTheme.colors.d10
                        )
                    },
                ),
                style = TextStyle(
                    color = CustomTheme.colors.d10,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}