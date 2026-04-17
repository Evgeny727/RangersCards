package com.rangerscards.data.mapper

import com.rangerscards.data.local.deck.DeckListItemProjection
import com.rangerscards.domain.model.Deck
import com.rangerscards.domain.model.DeckCampaignInfo
import com.rangerscards.domain.model.DeckListItem
import com.rangerscards.domain.model.DeckMeta
import com.rangerscards.domain.model.DeckSlot
import com.rangerscards.domain.model.OftenUpdatableDeckValues
import com.rangerscards.domain.model.PlayerInfo
import com.rangerscards.domain.model.PreviousDeck
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.rangerscards.data.local.deck.Deck as DbDeck

/**
 * Extension function to convert [DbDeck] to [Deck]
 */
fun DbDeck.toDomain(): Deck =
    Deck(
        id = id,
        uploaded = uploaded,
        playerInfo = PlayerInfo(
            userId = userId,
            userName = userHandle ?: ""
        ),
        tabooSetId = tabooSetId,
        version = version,
        name = name,
        description = description,
        deckMeta = meta.toDeckMeta(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        campaignInfo = campaignId?.let { id ->
            campaignName?.let { name ->
                campaignRewards?.let { rewards ->
                    DeckCampaignInfo(
                        campaignId = id,
                        campaignName = name,
                        campaignRewards = rewards.jsonArray.map { it.jsonPrimitive.content }.toImmutableList()
                    )
                }
            }
        },
        previousDeck = previousId?.let { id ->
            previousSlots?.let { slots ->
                previousSideSlots?.let { sideSlots ->
                    PreviousDeck(
                        id = id,
                        slots = slots.toDeckSlots(),
                        sideSlots = sideSlots.toDeckSlots()
                    )
                }
            }
        },
        nextId = nextId,
        oftenUpdatableDeckValues = OftenUpdatableDeckValues(
            slots = slots.toDeckSlots(),
            sideSlots = sideSlots.toDeckSlots(),
            extraSlots = extraSlots.toDeckSlots(),
            awa = awa,
            spi = spi,
            fit = fit,
            foc = foc
        )
    )

/**
 * Extension function to convert [DeckListItemProjection] to [DeckListItem]
 */
fun DeckListItemProjection.toDomain(): DeckListItem =
    DeckListItem(
        id = id,
        userHandle = userHandle,
        name = name,
        meta = meta.toDeckMeta(),
        campaignName = campaignName
    )

internal fun JsonElement.toDeckMeta(): DeckMeta =
    DeckMeta(
        roleId = jsonObject["role"]?.jsonPrimitive?.content.toString(),
        background = jsonObject["background"]?.jsonPrimitive?.content ?: "",
        specialty = jsonObject["specialty"]?.jsonPrimitive?.content ?: "",
        problems = jsonObject["problem"]?.jsonArray?.map { it.jsonPrimitive.content }?.toImmutableList()
    )

internal fun JsonElement.toDeckSlots(): PersistentList<DeckSlot> =
    jsonObject.map {
        DeckSlot(
            id = it.key,
            count = it.value.jsonPrimitive.int
        )
    }.toPersistentList()