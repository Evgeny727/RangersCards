package com.rangerscards.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rangerscards.data.local.campaign.Campaign
import com.rangerscards.data.local.campaign.ChallengeDeck
import com.rangerscards.data.local.card.Card
import com.rangerscards.data.local.card.CardFts
import com.rangerscards.data.local.dao.CampaignDao
import com.rangerscards.data.local.dao.CardDao
import com.rangerscards.data.local.dao.DeckDao
import com.rangerscards.data.local.deck.Deck
import com.rangerscards.data.objects.JsonElementConverter

@Database(entities = [Card::class, CardFts::class, Deck::class, Campaign::class, ChallengeDeck::class],
    version = 5,
    exportSchema = false)
@TypeConverters(JsonElementConverter::class)
abstract class RangersDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun deckDao(): DeckDao
    abstract fun campaignDao(): CampaignDao
}