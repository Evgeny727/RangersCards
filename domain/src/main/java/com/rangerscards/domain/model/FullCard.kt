package com.rangerscards.domain.model

data class FullCard(
    val tabooId: String?,
    val aspect: CardAspect?,
    val cost: Int?,
    val image: CardImage,
    val name: String,
    val presence: Int?,
    val approaches: CardApproaches?,
    val type: CardType,
    val traits: String?,
    val equip: Int?,
    val harm: Int?,
    val progress: Int?,
    val tokens: CardTokens?,
    val text: String?,
    val flavor: String?,
    val level: Int?,
    val set: CardSet,
    val packShortName: String?,
    val subset: CardSet?,
    val challenges: CardChallenges?,
)

data class CardAspect(
    val id: String,
    val shortName: String,
)

data class CardImage(
    val src: String,
    val realSrc: String,
)

data class CardApproaches(
    val conflict: Int?,
    val reason: Int?,
    val exploration: Int?,
    val connection: Int?
)

data class CardType(
    val id: String,
    val name: String,
)

data class CardTokens(
    val plurals: String,
    val count: Int,
)

data class CardSet(
    val name: String,
    val size: Int,
    val position: Int,
)

data class CardChallenges(
    val sunChallenge: String?,
    val mountainChallenge: String?,
    val crestChallenge: String?,
)
