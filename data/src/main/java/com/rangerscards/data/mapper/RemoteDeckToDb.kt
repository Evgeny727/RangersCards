package com.rangerscards.data.mapper

import com.rangerscards.GetMyDecksQuery
import com.rangerscards.data.local.deck.Deck
import com.rangerscards.domain.TimestampNormilizer
import com.rangerscards.fragment.Deck as RemoteDeck

/**
 * Extension function to convert [RemoteDeck] to [Deck]
 */
fun RemoteDeck.toDbDeck(): Deck =
    Deck(
        id = id.toString(),
        uploaded = true,
        userId = user_id,
        tabooSetId = taboo_set_id,
        userHandle = user.userInfo.handle,
        slots = slots,
        sideSlots = side_slots,
        extraSlots = extra_slots,
        version = version,
        name = name,
        description = description,
        awa = awa,
        spi = spi,
        fit = fit,
        foc = foc,
        createdAt = TimestampNormilizer.fixFraction(created_at),
        updatedAt = TimestampNormilizer.fixFraction(updated_at),
        meta = meta,
        campaignId = campaign?.id?.toString(),
        campaignName = campaign?.name,
        campaignRewards = campaign?.rewards,
        previousId = previous_deck?.id?.toString(),
        previousSlots = previous_deck?.slots,
        previousSideSlots = previous_deck?.side_slots,
        nextId = next_deck?.id?.toString()
    )
/**
 * Extension function to convert list of [GetMyDecksQuery.Deck] to list of [Deck]
 */
fun List<GetMyDecksQuery.Deck>.toDbDecks(): List<Deck> {
    return map { it.deck.toDbDeck() }
}