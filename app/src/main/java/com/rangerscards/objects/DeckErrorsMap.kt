package com.rangerscards.objects

import com.rangerscards.R

object DeckErrorsMap {
    fun deckErrorsMap(): Map<String, Int> {
        return mapOf(
            "invalid_aspects" to R.string.invalid_aspects,
            "too_many_duplicates" to R.string.too_many_duplicates,
            "need_two_cards" to R.string.need_two_cards,
            "personality" to R.string.personality_error,
            "too_many_awa_personality" to R.string.too_many_awa_personality,
            "too_many_spi_personality" to R.string.too_many_spi_personality,
            "too_many_foc_personality" to R.string.too_many_foc_personality,
            "too_many_fit_personality" to R.string.too_many_fit_personality,
            "background" to R.string.background_error,
            "too_many_background" to R.string.too_many_background,
            "specialty" to R.string.specialty_error,
            "too_many_specialty" to R.string.too_many_specialty,
            "role" to R.string.role_error,
            "outside_interest" to R.string.outside_interest_error,
            "invalid_background" to R.string.invalid_background,
            "invalid_specialty" to R.string.invalid_specialty,
            "invalid_role" to R.string.invalid_role,
            "invalid_aspect_levels" to R.string.invalid_aspect_levels,
            "invalid_outside_interest" to R.string.invalid_outside_interest,
            "too_many_outside_interest" to R.string.too_many_outside_interest,
            "too_many_cards" to R.string.too_many_cards,
            "too_few_cards" to R.string.too_few_cards,
        )
    }
}