package com.rangerscards.data.mapper

import com.rangerscards.GetAllCardsQuery
import com.rangerscards.data.local.card.Card
import com.rangerscards.data.remote.patches.CardMulligan
import com.rangerscards.fragment.Card as RemoteCard

/**
 * Extension function to convert [RemoteCard] to [Card]
 */
fun RemoteCard.toDbCard(locale: String): Card? {
    return id?.let {
        Card(
            id = it,
            code = code.toString(),
            name = name,
            realName = if (locale == "en") null else real_name,
            realTraits = real_traits,
            traits = traits,
            equip = equip,
            presence = presence,
            tabooId = taboo_id,
            tokenId = token_id,
            tokenName = token_name,
            tokenPlurals = token_plurals,
            tokenCount = token_count,
            harm = harm,
            approachConflict = approach_conflict,
            approachReason = approach_reason,
            approachExploration = approach_exploration,
            approachConnection = approach_connection,
            text = text,
            realText = if (locale == "en") null else real_text,
            setId = set_id,
            setName = set_name,
            setTypeId = set_type_id,
            setSize = set_size,
            setTypeName = set_type_name,
            setPosition = set_position,
            quantity = quantity,
            level = level,
            flavor = flavor,
            realFlavor = if (locale == "en") null else real_flavor,
            typeId = type_id,
            typeName = type_name,
            cost = cost,
            aspectId = aspect_id,
            aspectName = aspect_name,
            aspectShortName = aspect_short_name,
            progress = progress,
            imageSrc = imagesrc ?: real_imagesrc,
            realImageSrc = real_imagesrc,
            position = position,
            deckLimit = deck_limit,
            spoiler = spoiler,
            sunChallenge = sun_challenge,
            mountainChallenge = mountain_challenge,
            crestChallenge = crest_challenge,
            packId = pack_id,
            packName = pack_name,
            packShortName = pack_short_name,
            packPosition = pack_position,
            setup = CardMulligan.setupField.contains(code),
            subsetId = subset_id,
            subsetName = set_name,
            subsetPosition = subset_position,
            subsetSize = subset_size,
            composite = listOfNotNull(
                name, traits, text, type_name,
                sun_challenge, mountain_challenge, crest_challenge
            ).joinToString(" "),
            realComposite = if (locale == "en") null else listOfNotNull(
                name, real_name, traits, real_traits,
                type_name, type_id, text, real_text,
                sun_challenge, mountain_challenge, crest_challenge
            ).joinToString(" "),
        )
    }
}

/**
 * Extension function to convert list of [GetAllCardsQuery.Card] to list of [Card]
 */
fun List<GetAllCardsQuery.Card>.toDbCards(locale: String): List<Card> {
    return mapNotNull { it.card.toDbCard(locale) }
}