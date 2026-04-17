package com.rangerscards.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.rangerscards.AddFriendToCampaignMutation
import com.rangerscards.CreateCampaignMutation
import com.rangerscards.DeleteCampaignMutation
import com.rangerscards.ExtendCampaignMutation
import com.rangerscards.GetMyCampaignsQuery
import com.rangerscards.LeaveCampaignMutation
import com.rangerscards.RemoveFriendFromCampaignMutation
import com.rangerscards.SetCampaignCalendarMutation
import com.rangerscards.SetCampaignDayMutation
import com.rangerscards.SetCampaignMissionsMutation
import com.rangerscards.SetCampaignNotesMutation
import com.rangerscards.SetCampaignTitleMutation
import com.rangerscards.SetCampaignTravelMutation
import com.rangerscards.TransferCampaignMutation
import com.rangerscards.UpdateCampaignEventsMutation
import com.rangerscards.UpdateCampaignExpansionsMutation
import com.rangerscards.UpdateCampaignRemovedMutation
import com.rangerscards.UpdateCampaignRewardsMutation
import com.rangerscards.UpdateUploadedMutation
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class CampaignsRemoteDataSource @Inject constructor(
    private val apolloClient: ApolloClient
) {

    suspend fun fetchAllCampaigns(userId: String) = apolloClient
        .query(GetMyCampaignsQuery(userId))
        .execute()

    suspend fun createCampaign(
        name: String,
        cycleId: String,
        currentLocation: String,
        expansions: JsonElement,
        calendar: JsonElement
    ) = apolloClient.mutation(CreateCampaignMutation(
            name = name,
            cycleId = cycleId,
            currentLocation = currentLocation,
            expansions = expansions,
            calendar = calendar
        ))
        .execute()

    suspend fun updateUploadedCampaign(
        campaignId: Int,
        currentPathTerrain: String?,
        day: Int,
        extendedCalendar: Boolean?,
        missions: JsonElement,
        events: JsonElement,
        rewards: JsonElement,
        removed: JsonElement,
        history: JsonElement
    ) = apolloClient.mutation(UpdateUploadedMutation(
            campaignId = campaignId,
            currentPathTerrain = Optional.present(currentPathTerrain),
            day = day,
            extendedCalendar = Optional.present(extendedCalendar),
            missions = missions,
            events = events,
            rewards = rewards,
            removed = removed,
            history = history
        ))
        .execute()

    suspend fun transferCampaign(campaignId: Int, cycleId: String, currentLocation: String) = apolloClient
        .mutation(TransferCampaignMutation(campaignId, cycleId, currentLocation))
        .execute()

    suspend fun updateCampaignExpansions(campaignId: Int, expansions: JsonElement) = apolloClient
        .mutation(UpdateCampaignExpansionsMutation(campaignId, expansions))
        .execute()

    suspend fun setCampaignTitle(campaignId: Int, title: String) = apolloClient
        .mutation(SetCampaignTitleMutation(title, campaignId))
        .execute()

    suspend fun addFriendToCampaign(campaignId: Int, userId: String) = apolloClient
        .mutation(AddFriendToCampaignMutation(campaignId, userId))
        .execute()

    suspend fun removeFriendFromCampaign(campaignId: Int, userId: String) = apolloClient
        .mutation(RemoveFriendFromCampaignMutation(campaignId, userId))
        .execute()

    suspend fun setCampaignNotes(campaignId: Int, notes: JsonElement) = apolloClient
        .mutation(SetCampaignNotesMutation(campaignId, notes))
        .execute()

    suspend fun updateCampaignRewards(campaignId: Int, rewards: JsonElement) = apolloClient
        .mutation(UpdateCampaignRewardsMutation(campaignId, rewards))
        .execute()

    suspend fun updateCampaignEvents(campaignId: Int, events: JsonElement) = apolloClient
        .mutation(UpdateCampaignEventsMutation(campaignId, events))
        .execute()

    suspend fun updateCampaignRemoved(campaignId: Int, removed: JsonElement) = apolloClient
        .mutation(UpdateCampaignRemovedMutation(campaignId, removed))
        .execute()

    suspend fun extendCampaign(campaignId: Int) = apolloClient
        .mutation(ExtendCampaignMutation(campaignId))
        .execute()

    suspend fun setCampaignMissions(campaignId: Int, missions: JsonElement) = apolloClient
        .mutation(SetCampaignMissionsMutation(campaignId, missions))
        .execute()

    suspend fun setCampaignCalendar(campaignId: Int, calendar: JsonElement) = apolloClient
        .mutation(SetCampaignCalendarMutation(campaignId, calendar))
        .execute()

    suspend fun setCampaignDay(campaignId: Int, day: Int) = apolloClient
        .mutation(SetCampaignDayMutation(campaignId, day))
        .execute()

    suspend fun campaignTravel(
        campaignId: Int,
        day: Int,
        location: String,
        pathTerrain: String?,
        history: JsonElement
    ) = apolloClient.mutation(SetCampaignTravelMutation(
            campaignId,
            history,
            day,
            location,
            if (pathTerrain != null) Optional.present(pathTerrain)
                            else Optional.absent(),
        ))
        .execute()

    suspend fun leaveCampaign(campaignId: Int, userId: String) = apolloClient
        .mutation(LeaveCampaignMutation(campaignId, userId))
        .execute()

    suspend fun deleteCampaign(campaignId: Int) = apolloClient
        .mutation(DeleteCampaignMutation(campaignId))
        .execute()

}