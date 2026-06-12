package com.rangerscards.ui.cards.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.rangerscards.R
import com.rangerscards.objects.ImageSrc
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FullCardImageContainer(imageSrc: String?) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { isExpanded = !isExpanded }
        ) {
            Text(
                text = stringResource(if (isExpanded) R.string.hide_card_image
                else R.string.show_card_image),
                color = CustomTheme.colors.d10,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f, false)
            )
            Icon(
                painterResource(if (isExpanded) R.drawable.arrow_drop_up_32dp
                else R.drawable.arrow_drop_down_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(32.dp).align(Alignment.Top)
            )
        }
        if (isExpanded) Surface(
            color = CustomTheme.colors.l30,
            shape = CustomTheme.shapes.large,
            modifier = Modifier.size(225.dp, 315.dp).clickable { isExpanded = !isExpanded }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ImageSrc.BASE_URL + imageSrc)
                    .build(),
                placeholder = painterResource(R.drawable.broken_image_32dp),
                error = painterResource(R.drawable.broken_image_32dp),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
        } else Surface(
            color = CustomTheme.colors.l30,
            shape = CustomTheme.shapes.large,
            modifier = Modifier.size(112.dp, 157.dp).clickable { isExpanded = !isExpanded }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ImageSrc.BASE_URL + imageSrc)
                    .build(),
                placeholder = painterResource(R.drawable.broken_image_32dp),
                error = painterResource(R.drawable.broken_image_32dp),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
        }
    }
}