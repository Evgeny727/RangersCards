package com.rangerscards.data.mapper


import com.rangerscards.domain.model.Deck
import com.rangerscards.domain.model.DeckMeta
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.rangerscards.data.local.deck.Deck as DbDeck

/**
 * Extension function to convert [Deck] to [DbDeck]
 */
fun Deck.toDbDeck(): DbDeck =
    DbDeck(
        id = id,
        uploaded = uploaded,
        userId = playerInfo.id,
        tabooSetId = tabooSetId,
        userHandle = playerInfo.name,
        slots = oftenUpdatableDeckValues.slots.toJsonDeckSlots(),
        sideSlots = oftenUpdatableDeckValues.sideSlots.toJsonDeckSlots(),
        extraSlots = oftenUpdatableDeckValues.extraSlots.toJsonDeckSlots(),
        version = version,
        name = name,
        description = description,
        awa = oftenUpdatableDeckValues.awa,
        spi = oftenUpdatableDeckValues.spi,
        fit = oftenUpdatableDeckValues.fit,
        foc = oftenUpdatableDeckValues.foc,
        createdAt = createdAt,
        updatedAt = updatedAt,
        meta = deckMeta.toJsonDeckMeta(),
        campaignId = campaignInfo?.campaignId,
        campaignName = campaignInfo?.campaignName,
        campaignRewards = buildJsonArray {
            campaignInfo?.campaignRewards?.forEach { add(it) }
        },
        previousId = previousDeck?.id,
        previousSlots = previousDeck?.slots?.toJsonDeckSlots(),
        previousSideSlots = previousDeck?.sideSlots?.toJsonDeckSlots(),
        nextId = nextId
    )

/**
 * Extension function to convert [DeckMeta] to [JsonElement]
 */
fun DeckMeta.toJsonDeckMeta(): JsonElement =
    buildJsonObject {
        put("role", roleId)
        problems?.let { problems ->
            put("problem", buildJsonArray {
                problems.forEach { add(it) }
            })
        }
        put("background", background)
        put("specialty", specialty)
    }


/**
 * Extension function to convert [ImmutableMap] of DeckSlots to [JsonElement]
 */
fun ImmutableMap<String, Int>.toJsonDeckSlots(): JsonElement = with(this) {
    buildJsonObject {
        this@with.forEach { put(it.key, it.value) }
    }
}
