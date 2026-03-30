package com.rangerscards.ui.deck.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.rangerscards.R
import com.rangerscards.objects.CardTextParser
import com.rangerscards.objects.ImageSrc
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FullDeckRoleItem(
    tabooId: String?,
    imageSrc: String?,
    name: String?,
    text: AnnotatedString,
    campaignName: String?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .sizeIn(maxHeight = 88.dp)
                .aspectRatio(1f),
            shape = CustomTheme.shapes.medium,
            color = Color.Transparent,
            border = BorderStroke(2.dp, CustomTheme.colors.d10),
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
                    val scaleFactor = (88.dp.toPx() + 6F) / 88.dp.toPx()

                    // Apply uniform scaling.
                    scaleX = scaleFactor
                    scaleY = scaleFactor },
            )
        }
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name ?: "",
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (tabooId != null) Icon(
                    painterResource(R.drawable.uncommon_wisdom),
                    contentDescription = null,
                    tint = CustomTheme.colors.d30,
                    modifier = Modifier.size(24.dp)
                )
            }
            BasicText(
                text = text,
                inlineContent = CardTextParser.inlineIconsMap(CustomTheme.colors.d30),
                style = TextStyle(
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                )
            )
            if (campaignName != null) {
                val iconId = "campaign"
                BasicText(
                    text = buildAnnotatedString {
                        appendInlineContent(
                            iconId,
                            "[$iconId]"
                        )
                        append(" $campaignName")
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
        IconButton(
            onClick = onEdit,
            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painterResource(id = R.drawable.edit_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}