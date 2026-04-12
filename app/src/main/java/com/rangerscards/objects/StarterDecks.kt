package com.rangerscards.objects

import com.rangerscards.domain.model.DeckMeta
import com.rangerscards.domain.model.DeckSlot
import com.rangerscards.domain.model.StarterDeck
import kotlinx.collections.immutable.persistentListOf

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
                slots = persistentListOf(
                    DeckSlot("01001", 2),
                    DeckSlot("01039", 2),
                    DeckSlot("01056", 2),
                    DeckSlot("01099", 2),
                    DeckSlot("01005", 2),
                    DeckSlot("01101", 2),
                    DeckSlot("01105", 2),
                    DeckSlot("01003", 2),
                    DeckSlot("01044", 2),
                    DeckSlot("01006", 2),
                    DeckSlot("01093", 2),
                    DeckSlot("01008", 2),
                    DeckSlot("01048", 2),
                    DeckSlot("01042", 2),
                    DeckSlot("01043", 2)
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
                slots = persistentListOf(
                    DeckSlot("01023", 2),
                    DeckSlot("01073", 2),
                    DeckSlot("01104", 2),
                    DeckSlot("01078", 2),
                    DeckSlot("01026", 2),
                    DeckSlot("01107", 2),
                    DeckSlot("01095", 2),
                    DeckSlot("01067", 2),
                    DeckSlot("01097", 2),
                    DeckSlot("01022", 2),
                    DeckSlot("01070", 2),
                    DeckSlot("01025", 2),
                    DeckSlot("01027", 2),
                    DeckSlot("01077", 2),
                    DeckSlot("01018", 2),
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
                slots = persistentListOf(
                    DeckSlot("01102", 2),
                    DeckSlot("01084", 2),
                    DeckSlot("01081", 2),
                    DeckSlot("01085", 2),
                    DeckSlot("01034", 2),
                    DeckSlot("01029", 2),
                    DeckSlot("01090", 2),
                    DeckSlot("01031", 2),
                    DeckSlot("01100", 2),
                    DeckSlot("01094", 2),
                    DeckSlot("01083", 2),
                    DeckSlot("01082", 2),
                    DeckSlot("01028", 2),
                    DeckSlot("01106", 2),
                    DeckSlot("01035", 2),
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
                slots = persistentListOf(
                    DeckSlot("01062", 2),
                    DeckSlot("01061", 2),
                    DeckSlot("01012", 2),
                    DeckSlot("01059", 2),
                    DeckSlot("01060", 2),
                    DeckSlot("01096", 2),
                    DeckSlot("01011", 2),
                    DeckSlot("01007", 2),
                    DeckSlot("01017", 2),
                    DeckSlot("01013", 2),
                    DeckSlot("01108", 2),
                    DeckSlot("01015", 2),
                    DeckSlot("01098", 2),
                    DeckSlot("01103", 2),
                    DeckSlot("01053", 2),
                )
            ),
        )
    }
}