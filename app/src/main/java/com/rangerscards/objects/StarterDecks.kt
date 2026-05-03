package com.rangerscards.objects

import com.rangerscards.domain.model.DeckMeta
import com.rangerscards.domain.model.StarterDeck
import kotlinx.collections.immutable.persistentMapOf

object StarterDecks {
    fun starterDecks(): List<StarterDeck> {
        return listOf(
            StarterDeck(
                meta = DeckMeta(
                    "01037",
                    "traveler",
                    "explorer"
                ),
                foc = 1,
                spi = 2,
                awa = 2,
                fit = 3,
                slots = persistentMapOf(
                    "01001" to 2,
                    "01039" to 2,
                    "01056" to 2,
                    "01099" to 2,
                    "01005" to 2,
                    "01101" to 2,
                    "01105" to 2,
                    "01003" to 2,
                    "01044" to 2,
                    "01006" to 2,
                    "01093" to 2,
                    "01008" to 2,
                    "01048" to 2,
                    "01042" to 2,
                    "01043" to 2
                )
            ),
            StarterDeck(
                meta = DeckMeta (
                    "01066",
                    "shepherd",
                    "conciliator"
                ),
                foc = 2,
                spi = 3,
                awa = 1,
                fit = 2,
                slots = persistentMapOf(
                    "01023" to 2,
                    "01073" to 2,
                    "01104" to 2,
                    "01078" to 2,
                    "01026" to 2,
                    "01107" to 2,
                    "01095" to 2,
                    "01067" to 2,
                    "01097" to 2,
                    "01022" to 2,
                    "01070" to 2,
                    "01025" to 2,
                    "01027" to 2,
                    "01077" to 2,
                    "01018" to 2,
                )
            ),
            StarterDeck(
                meta = DeckMeta (
                    "01079",
                    "forager",
                    "shaper"
                ),
                foc = 2,
                spi = 1,
                awa = 3,
                fit = 2,
                slots = persistentMapOf(
                    "01102" to 2,
                    "01084" to 2,
                    "01081" to 2,
                    "01085" to 2,
                    "01034" to 2,
                    "01029" to 2,
                    "01090" to 2,
                    "01031" to 2,
                    "01100" to 2,
                    "01094" to 2,
                    "01083" to 2,
                    "01082" to 2,
                    "01028" to 2,
                    "01106" to 2,
                    "01035" to 2,
                )
            ),
            StarterDeck(
                meta = DeckMeta (
                    "01051",
                    "artisan",
                    "artificer"
                ),
                foc = 3,
                spi = 2,
                awa = 2,
                fit = 1,
                slots = persistentMapOf(
                    "01062" to 2,
                    "01061" to 2,
                    "01012" to 2,
                    "01059" to 2,
                    "01060" to 2,
                    "01096" to 2,
                    "01011" to 2,
                    "01007" to 2,
                    "01017" to 2,
                    "01013" to 2,
                    "01108" to 2,
                    "01015" to 2,
                    "01098" to 2,
                    "01103" to 2,
                    "01053" to 2,
                )
            ),
        )
    }
}