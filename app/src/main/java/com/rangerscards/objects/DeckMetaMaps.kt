package com.rangerscards.objects

import com.rangerscards.R

object DeckMetaMaps {
    val background by lazy {
        mapOf(
            "traveler" to R.string.traveler_background,
            "shepherd" to R.string.shepherd_background,
            "forager" to R.string.forager_background,
            "artisan" to R.string.artisan_background,
            "talespinner" to R.string.talespinner_background
        )
    }
    val specialty by lazy {
        mapOf(
            "explorer" to R.string.explorer_specialty,
            "shaper" to R.string.shaper_specialty,
            "conciliator" to R.string.conciliator_specialty,
            "artificer" to R.string.artificer_specialty,
            "spirit_speaker" to R.string.spirit_speaker_specialty
        )
    }
}