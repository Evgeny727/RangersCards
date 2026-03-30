package com.rangerscards.objects

import androidx.annotation.StringRes
import com.rangerscards.R

data class SortOption(
    val id: String,
    @StringRes val resId: Int,
    val isActive: Boolean = false
)

object CardFilters {

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