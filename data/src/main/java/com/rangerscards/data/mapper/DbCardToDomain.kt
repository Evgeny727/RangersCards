package com.rangerscards.data.mapper

import com.rangerscards.data.local.card.CardDeckListItemProjection
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.card.FullCardProjection
import com.rangerscards.data.local.deck.RoleCardProjection
import com.rangerscards.domain.model.CardApproaches
import com.rangerscards.domain.model.CardAspect
import com.rangerscards.domain.model.CardChallenges
import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.domain.model.CardImage
import com.rangerscards.domain.model.CardListItem
import com.rangerscards.domain.model.CardSet
import com.rangerscards.domain.model.CardTokens
import com.rangerscards.domain.model.CardType
import com.rangerscards.domain.model.FullCard
import com.rangerscards.domain.model.RoleCard

/**
 * Extension function to convert [FullCardProjection] to [FullCard]
 */
fun FullCardProjection.toDomain(): FullCard =
    FullCard(
        tabooId = tabooId,
        aspect = aspectId?.let { id ->
            aspectShortName?.let { shortName ->
                CardAspect(id, shortName)
            }
        },
        cost = cost,
        image = CardImage(imageSrc.toString(), realImageSrc.toString()),
        name = name,
        presence = presence,
        approaches = CardApproaches(
            connection = approachConnection,
            reason = approachReason,
            conflict = approachConflict,
            exploration = approachExploration
        ),
        type = CardType(typeId.toString(), typeName.toString()),
        traits = traits,
        equip = equip,
        harm = harm,
        progress = presence,
        tokens = tokenPlurals?.let { plurals ->
            tokenCount?.let { count ->
                CardTokens(plurals, count)
            }
        },
        text = text,
        flavor = flavor,
        level = level,
        set = CardSet(
            setName,
            setSize,
            setPosition
        ),
        packShortName = packShortName,
        subset = subsetName?.let { name ->
            subsetSize?.let { size ->
                subsetPosition?.let { position ->
                    CardSet(
                        name,
                        size,
                        position
                    )
                }
            }
        },
        challenges = CardChallenges(
            sunChallenge,
            mountainChallenge,
            crestChallenge
        )
    )

/**
 * Extension function to convert [CardListItemProjection] to [CardListItem]
 */
fun CardListItemProjection.toDomain(): CardListItem =
    CardListItem(
        id = id,
        code = code,
        tabooId = tabooId,
        name = name,
        approaches = CardApproaches(
            connection = approachConnection,
            reason = approachReason,
            conflict = approachConflict,
            exploration = approachExploration
        ),
        traits = traits,
        equip = equip,
        setName = setName,
        level = level,
        typeName = typeName,
        cost = cost,
        aspect = aspectId?.let { id ->
            aspectShortName?.let { shortName ->
                CardAspect(id, shortName)
            }
        },
        realImageSrc = realImageSrc,
    )

/**
 * Extension function to convert [RoleCardProjection] to [RoleCard]
 */
fun RoleCardProjection.toDomain(): RoleCard =
    RoleCard(
        id = id,
        code = code,
        name = name,
        text = text,
        realImageSrc = realImageSrc,
        tabooId = tabooId
    )

/**
 * Extension function to convert [CardDeckListItemProjection] to [CardDeckListItem]
 */
fun CardDeckListItemProjection.toDomain(): CardDeckListItem =
    CardDeckListItem(
        id = id,
        code = code,
        tabooId = tabooId,
        name = name,
        approaches = approachConnection?.let { connection ->
            approachReason?.let { reason ->
                approachConflict?.let { conflict ->
                    approachExploration?.let { exploration ->
                        CardApproaches(
                            connection = connection,
                            reason = reason,
                            conflict = conflict,
                            exploration = exploration
                        )
                    }
                }
            }
        },
        traits = traits,
        equip = equip,
        realTraits = realTraits,
        setName = setName,
        level = level,
        typeName = typeName,
        cost = cost,
        aspect = aspectId?.let { id ->
            aspectShortName?.let { shortName ->
                CardAspect(id, shortName)
            }
        },
        realImageSrc = realImageSrc,
        setId = setId,
        setTypeId = setTypeId,
        deckLimit = deckLimit
    )