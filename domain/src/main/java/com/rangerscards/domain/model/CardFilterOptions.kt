package com.rangerscards.domain.model

data class CardFilterOptions(
    val searchQuery: String = "",
    val types: List<String> = emptyList(),
    val traits: List<String> = emptyList(),
    val sets: List<String> = emptyList(),
    val costRange: IntRange? = null,
    val approaches: Approaches = Approaches(),
    val packs: List<String> = emptyList(),
    val aspectRequirements: AspectRequirements = AspectRequirements(),
    val sortOrder: List<String> = listOf("set_type_id", "set_id", "set_position")
)

data class Approaches(
    val conflict: Boolean = false,
    val reason: Boolean = false,
    val exploration: Boolean = false,
    val connection: Boolean = false
)

data class AspectRequirements(
    val awa: Int? = null,
    val spi: Int? = null,
    val foc: Int? = null,
    val fit: Int? = null,
    val equalOrLower: Boolean = false
)
