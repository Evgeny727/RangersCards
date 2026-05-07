package com.rangerscards.objects

import androidx.annotation.DrawableRes
import com.rangerscards.R

data class ChallengeCard(
    val awa: Int,
    val spi: Int,
    val fit: Int,
    val foc: Int,
    @DrawableRes val challengeIcon: Int,
    val reshuffle: Boolean = false
)

object ChallengeDeck {
    val challengeDeck by lazy {
        mapOf(
            1 to ChallengeCard(0,0,-1,1, R.drawable.mountain),
            2 to ChallengeCard(0,-1,0,1, R.drawable.crest),
            3 to ChallengeCard(0,0,-1,-1, R.drawable.sun),
            4 to ChallengeCard(1,-1,-1,1, R.drawable.mountain),
            5 to ChallengeCard(0,0,1,-1, R.drawable.crest),
            6 to ChallengeCard(0,1,-1,0, R.drawable.crest),
            7 to ChallengeCard(0,-1,-1,0, R.drawable.mountain),
            8 to ChallengeCard(0,1,-2,1, R.drawable.sun, true),
            9 to ChallengeCard(1,0,0,-1, R.drawable.sun),
            10 to ChallengeCard(-1,1,1,-1, R.drawable.mountain),
            11 to ChallengeCard(1,-1,0,0, R.drawable.mountain),
            12 to ChallengeCard(-1,0,0,-1, R.drawable.mountain),
            13 to ChallengeCard(0,-1,1,0, R.drawable.sun),
            14 to ChallengeCard(-1,0,1,0, R.drawable.sun),
            15 to ChallengeCard(-1,1,0,0, R.drawable.sun),
            16 to ChallengeCard(1,1,0,-2, R.drawable.crest, true),
            17 to ChallengeCard(1,0,-1,0, R.drawable.mountain),
            18 to ChallengeCard(-1,0,-1,0, R.drawable.crest),
            19 to ChallengeCard(-1,-1,0,0, R.drawable.crest),
            20 to ChallengeCard(1,-2,1,0, R.drawable.crest, true),
            21 to ChallengeCard(-2,0,1,1, R.drawable.sun, true),
            22 to ChallengeCard(0,-1,0,-1, R.drawable.sun),
            23 to ChallengeCard(-1,0,0,1, R.drawable.crest),
            24 to ChallengeCard(0,1,0,-1, R.drawable.mountain),
        )
    }
}