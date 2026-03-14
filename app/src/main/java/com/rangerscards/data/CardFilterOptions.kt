package com.rangerscards.data

import androidx.annotation.StringRes
import com.rangerscards.R
import com.rangerscards.data.objects.DeckMetaMaps

data class CardFilterOptions(
    val searchQuery: String = "",
    val types: List<String> = emptyList(),
    val traits:  List<String> = emptyList(),
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

data class SortOption(
    val id: String,
    @StringRes val resId: Int,
    val isActive: Boolean = false
)

object CardFilters {
    fun buildFiltersClause(filterOptions: CardFilterOptions): String {
        val clauses = mutableListOf<String>()

        // 1) Filter by exact type_id (IN (...))
        if (filterOptions.types.isNotEmpty()) {
            // e.g. type_id IN ('gear', 'role')
            val escaped = filterOptions.types.joinToString(", ") { "'${it.replace("'", "''")}'" }
            clauses += "type_id IN ($escaped)"
        }

        // 2) Filter by traits (check if the traits column CONTAINS any of the user’s selected traits)
        //    e.g. if traits = ["Wisdom", "Trail"]: (real_traits LIKE '%Wisdom%' OR real_traits LIKE '%Trail%')
        if (filterOptions.traits.isNotEmpty()) {
            val sub = filterOptions.traits.joinToString(" OR ") {
                "real_traits LIKE '%${it.replace("'", "''")}%'"
            }
            clauses += "($sub)"
        }

        // 3) Filter by set_id (IN (...))
        if (filterOptions.sets.isNotEmpty()) {
            val escaped = filterOptions.sets.joinToString(", ") { "'${it.replace("'", "''")}'" }
            clauses += "set_id IN ($escaped)"
        }

        // 4) Filter by costRange (INTEGER BETWEEN min AND max), only if range is set
        if (filterOptions.costRange != null) {
            val minCost = filterOptions.costRange.first
            val maxCost = filterOptions.costRange.last
            clauses += "cost BETWEEN $minCost AND $maxCost"
        }

        // 5) Filter by Approaches.
        //    If any of the four is true, build a single "( ... OR ... )" clause:
        val approachChecks = mutableListOf<String>()
        if (filterOptions.approaches.conflict) {
            approachChecks += "approach_conflict >= 1"
        }
        if (filterOptions.approaches.reason) {
            approachChecks += "approach_reason >= 1"
        }
        if (filterOptions.approaches.exploration) {
            approachChecks += "approach_exploration >= 1"
        }
        if (filterOptions.approaches.connection) {
            approachChecks += "approach_connection >= 1"
        }
        if (approachChecks.isNotEmpty()) {
            // Combine all selected approach‐checks with OR
            val combined = approachChecks.joinToString(" OR ")
            clauses += "($combined)"
        }

        // 6) Filter by AspectRequirements: combine into one "(… OR …)" clause
        val aspectChecks = mutableListOf<String>()
        val checkSign = if (filterOptions.aspectRequirements.equalOrLower) "<=" else "="
        filterOptions.aspectRequirements.awa?.let { awaValue ->
            aspectChecks += "(aspect_id = 'AWA' AND level $checkSign $awaValue AND level IS NOT NULL)"
        }
        filterOptions.aspectRequirements.spi?.let { spiValue ->
            aspectChecks += "(aspect_id = 'SPI' AND level $checkSign $spiValue AND level IS NOT NULL)"
        }
        filterOptions.aspectRequirements.foc?.let { focValue ->
            aspectChecks += "(aspect_id = 'FOC' AND level $checkSign $focValue AND level IS NOT NULL)"
        }
        filterOptions.aspectRequirements.fit?.let { fitValue ->
            aspectChecks += "(aspect_id = 'FIT' AND level $checkSign $fitValue AND level IS NOT NULL)"
        }
        if (aspectChecks.isNotEmpty()) {
            val combined = aspectChecks.joinToString(" OR ")
            clauses += "($combined)"
        }

        // If collected zero clauses, return an empty string
        if (clauses.isEmpty()) return ""

        // Otherwise join all clauses with " AND " and prefix with " AND "
        return clauses.joinToString(separator = " AND ")
    }

    fun buildSortClause(filterOptions: CardFilterOptions): String {
        val sortOptions = filterOptions.sortOrder
        if (sortOptions.isEmpty()) return "(set_type_id IS NULL), set_type_id, set_id, set_position"
        return sortOptions.joinToString(", ") {
            val col = when(it) {
                "set_type_id" -> "(set_type_id IS NULL), $it"
                "cost" -> "(cost IS NOT NULL), $it"
                "aspect_id" -> "(aspect_id IS NOT NULL), $it"
                else -> it
            }
            col
        }
    }

    fun getTypesFilters(): Map<String, Int> {
        return mapOf(
            "attachment" to R.string.types_filter_attachment,
            "attribute" to R.string.types_filter_attribute,
            "being" to R.string.types_filter_being,
            "feature" to R.string.types_filter_feature,
            "gear" to R.string.types_filter_gear,
            "moment" to R.string.types_filter_moment,
            "role" to R.string.role,
        )
    }

    fun getTraitsFilters(): Map<String, Int> {
        return mapOf(
            "acquired" to R.string.traits_filter_acquired,
            "aid" to R.string.traits_filter_aid,
            "avian" to R.string.traits_filter_avian,
            "book" to R.string.traits_filter_book,
            "clothing" to R.string.traits_filter_clothing,
            "companion" to R.string.traits_filter_companion,
            "conduit" to R.string.traits_filter_conduit,
            "experience" to R.string.traits_filter_experience,
            "expert" to R.string.traits_filter_expert,
            "food" to R.string.traits_filter_food,
            "innate" to R.string.traits_filter_innate,
            "instrument" to R.string.traits_filter_instrument,
            "knowledge" to R.string.traits_filter_knowledge,
            "major" to R.string.traits_filter_major,
            "mammal" to R.string.traits_filter_mammal,
            "manifestation" to R.string.traits_filter_manifestation,
            "map" to R.string.traits_filter_map,
            "mod" to R.string.traits_filter_mod,
            "nature" to R.string.traits_filter_nature,
            "skill" to R.string.traits_filter_skill,
            "spirit" to R.string.traits_filter_spirit,
            "spiritual" to R.string.traits_filter_spiritual,
            "tale" to R.string.traits_filter_tale,
            "tech" to R.string.traits_filter_tech,
            "tool" to R.string.traits_filter_tool,
            "trail" to R.string.traits_filter_trail,
            "weapon" to R.string.traits_filter_weapon,
            "wisdom" to R.string.traits_filter_wisdom
        )
    }

    fun getSetsFilters(): Map<String, Int> {
        return (DeckMetaMaps.background + DeckMetaMaps.specialty +
                mapOf("personality" to R.string.personality))
    }

    fun getSortOptions(): Map<String, Int> {
        return mapOf(
            "name" to R.string.deck_creation_name_label,
            "traits" to R.string.traits_sort_header,
            "equip" to R.string.equip_sort_header,
            "set_id" to R.string.sets_sort_header,
            "set_type_id" to R.string.set_type_sort_header,
            "set_position" to R.string.set_position_sort_header,
            "type_name" to R.string.types_sort_header,
            "cost" to R.string.cost_filter_header,
            "aspect_id" to R.string.aspects_sort_header,
            "pack_id" to R.string.packs_sort_header,
        )
    }
}