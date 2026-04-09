package com.rangerscards.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.rangerscards.CreateDeckMutation
import com.rangerscards.DeleteDeckMutation
import com.rangerscards.GetDeckQuery
import com.rangerscards.GetMyDecksQuery
import com.rangerscards.RemoveDeckCampaignMutation
import com.rangerscards.SaveDeckMutation
import com.rangerscards.SaveDeckTabooSetMutation
import com.rangerscards.SetDeckCampaignMutation
import com.rangerscards.UpgradeDeckMutation
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class DecksRemoteDataSource @Inject constructor(
    private val  apolloClient: ApolloClient
) {

    suspend fun fetchDecks(userId: String) = apolloClient
        .query(GetMyDecksQuery(userId))
        .execute()

    suspend fun fetchDeckById(userId: String, deckId: Int) = apolloClient
        .query(GetDeckQuery(deckId))
        .execute()

    suspend fun upgradeDeck(deckId: Int) = apolloClient
        .mutation(UpgradeDeckMutation(deckId))
        .execute()

    suspend fun setDeckCampaign(deckId: Int, campaignId: Int) = apolloClient
        .mutation(SetDeckCampaignMutation(deckId, campaignId))
        .execute()

    suspend fun removeDeckCampaign(deckId: Int, campaignId: Int) = apolloClient
        .mutation(RemoveDeckCampaignMutation(deckId, campaignId))
        .execute()

    suspend fun deleteDeckById(deckId: Int) = apolloClient
        .mutation(DeleteDeckMutation(deckId))
        .execute()

    suspend fun saveDeckTabooSet(deckId: Int, tabooSet: String?) = apolloClient
        .mutation(SaveDeckTabooSetMutation(
            deckId,
            if (tabooSet == null) Optional.absent() else Optional.present(tabooSet)
        ))
        .execute()

    suspend fun createDeck(
        name: String,
        foc: Int,
        fit: Int,
        awa: Int,
        spi: Int,
        meta: JsonElement,
        slots: JsonElement,
        extraSlots: JsonElement,
        description: String?,
        tabooSetId: String?
    ) = apolloClient
        .mutation(CreateDeckMutation(
            name = name,
            foc = foc,
            fit = fit,
            awa = awa,
            spi = spi,
            meta = meta,
            slots = slots,
            extraSlots = extraSlots,
            description = if (description == null) Optional.absent()
                else Optional.present(description),
            tabooSetId = if (tabooSetId == null) Optional.absent()
                else Optional.present(tabooSetId)
        ))
        .execute()

    suspend fun saveDeck(
        deckId: Int,
        name: String,
        foc: Int,
        fit: Int,
        awa: Int,
        spi: Int,
        meta: JsonElement,
        slots: JsonElement,
        sideSlots: JsonElement,
        extraSlots: JsonElement,
    ) = apolloClient
        .mutation(SaveDeckMutation(
            id = deckId,
            name = name,
            foc = foc,
            fit = fit,
            awa = awa,
            spi = spi,
            meta = meta,
            slots = slots,
            sideSlots = sideSlots,
            extraSlots = extraSlots,
        )).execute()

}