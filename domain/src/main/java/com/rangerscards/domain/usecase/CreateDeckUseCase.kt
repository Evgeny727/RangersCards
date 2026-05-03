package com.rangerscards.domain.usecase

import com.rangerscards.domain.model.DeckMeta
import com.rangerscards.domain.model.StarterDeck
import com.rangerscards.domain.repository.DecksRepository
import kotlinx.collections.immutable.persistentMapOf

class CreateDeckUseCase(
    private val decksRepository: DecksRepository,
) {
    suspend operator fun invoke(
        starterDeck: StarterDeck?,
        deckMeta: DeckMeta?,
        backgroundLocalized: String,
        specialtyLocalized: String,
        postfix: String,
        isUploading: Boolean,
        name: String,
        tabooSetId: String?,
    ) = if (starterDeck != null) {
        decksRepository.createDeck(
            uploaded = isUploading,
            name = name.ifEmpty { "$backgroundLocalized - $specialtyLocalized - $postfix" },
            slots = starterDeck.slots,
            meta = starterDeck.meta,
            tabooSetId = tabooSetId,
            awa = starterDeck.awa,
            fit = starterDeck.fit,
            foc = starterDeck.foc,
            spi = starterDeck.spi,
        )
    } else {
        decksRepository.createDeck(
            uploaded = isUploading,
            name = name.ifEmpty { "$backgroundLocalized - $specialtyLocalized" },
            slots = persistentMapOf(),
            meta = deckMeta!!,
            tabooSetId = tabooSetId,
        )
    }
}