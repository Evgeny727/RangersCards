package com.rangerscards.ui.cards.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rangerscards.domain.model.CardApproaches
import com.rangerscards.domain.model.CardAspect
import com.rangerscards.domain.model.CardChallenges
import com.rangerscards.domain.model.CardImage
import com.rangerscards.domain.model.CardSet
import com.rangerscards.domain.model.CardTokens
import com.rangerscards.domain.model.CardType
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.RangersCardsTheme

@Composable
fun FullCard(
    tabooId: String?,
    aspect: CardAspect?,
    cost: Int?,
    image: CardImage,
    presence: Int?,
    approaches: CardApproaches,
    type: CardType,
    traits: String?,
    equip: Int?,
    harm: Int?,
    progress: Int?,
    tokens: CardTokens?,
    text: String?,
    flavor: String?,
    level: Int?,
    set: CardSet,
    subset: CardSet?,
    packShortName: String?,
    challenges: CardChallenges,
    isDarkTheme: Boolean,
    name: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(CustomTheme.shapes.large),
        shape = CustomTheme.shapes.large,
        color = CustomTheme.colors.l30,
        border = BorderStroke(1.dp, when (aspect?.id) {
            "AWA" -> CustomTheme.colors.green
            "FIT" -> CustomTheme.colors.red
            "FOC" -> CustomTheme.colors.blue
            "SPI" -> CustomTheme.colors.orange
            else -> CustomTheme.colors.m
        }),
        shadowElevation = 4.dp
    ) {
        Column {
            //Header
            FullCardHeader(
                aspect,
                cost,
                image.realSrc,
                name,
                presence,
                approaches,
                isDarkTheme,
            )
            //Content block
            FullCardContent(
                aspect?.id,
                type,
                traits,
                equip,
                harm,
                progress,
                tokens,
                text,
                flavor,
                challenges,
                image.src,
                isDarkTheme
            )
            //Set info
            FullCardSetInfo(
                tabooId,
                aspect,
                level,
                set,
                subset,
                packShortName,
                isDarkTheme
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FullCardScreenPreview() {
    RangersCardsTheme {
        Column(
            modifier = Modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize()
        ) {
            FullCard(
                tabooId = "",
                aspect = CardAspect("AWA", "AWA"),
                cost = 2,
                image = CardImage("null", "null"),
                presence = 1,
                approaches = CardApproaches(
                    connection = 1,
                    reason = 1,
                    conflict = 1,
                    exploration = 1
                ),
                type = CardType("null", "null"),
                traits = "Being / Companion / Mammal",
                equip = 2,
                harm = 1,
                progress = 1,
                tokens = CardTokens(
                    plurals = "Запись,Записи,Записей",
                    count = 1
                ),
                text = "Some text\nAnd [[new]] g line",
                flavor = "Some flavor",
                level = 2,
                set = CardSet(
                    name = "Reward",
                    size = 31,
                    position = 2
                ),
                subset = null,
                packShortName = null,
                isDarkTheme = isSystemInDarkTheme(),
                name = "Scuttler g Tunnel\nnew g line an some more",
                challenges = CardChallenges(
                    sun = null,
                    mountain = null,
                    crest = null
                )
            )
        }
    }
}
