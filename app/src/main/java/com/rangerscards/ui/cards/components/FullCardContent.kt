package com.rangerscards.ui.cards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rangerscards.domain.model.CardChallenges
import com.rangerscards.domain.model.CardTokens
import com.rangerscards.domain.model.CardType

@Composable
fun FullCardContent(
    aspectId: String?,
    type: CardType,
    traits: String?,
    equip: Int?,
    harm: Int?,
    progress: Int?,
    tokens: CardTokens?,
    text: String?,
    flavor: String?,
    challenges: CardChallenges,
    imageSrc: String?,
    isDarkTheme: Boolean
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        FullCardAdditionalContent(
            aspectId,
            traits,
            type,
            equip,
            harm,
            progress,
            tokens,
            isDarkTheme
        )
        FullCardTextContent(aspectId, text, flavor, challenges, isDarkTheme)
        FullCardImageContainer(imageSrc)
    }
}