package com.rangerscards.data.mapper

import com.rangerscards.GetMyCampaignsQuery
import com.rangerscards.data.local.campaign.Campaign
import com.rangerscards.domain.TimestampNormilizer.fixFraction
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.rangerscards.fragment.Campaign as RemoteCampaign

/**
 * Extension function to convert [RemoteCampaign] to [Campaign]
 */
fun RemoteCampaign.toDbCampaign(): Campaign =
    Campaign(
        id = id.toString(),
        uploaded = true,
        userId = user_id,
        name = name,
        notes = notes,
        day = day,
        extendedCalendar = extended_calendar,
        cycleId = cycle_id,
        currentLocation = current_location.toString(),
        currentPathTerrain = current_path_terrain,
        missions = missions,
        events = events,
        rewards = rewards,
        removed = removed,
        history = history,
        calendar = calendar,
        createdAt = fixFraction(created_at),
        updatedAt = fixFraction(updated_at),
        expansions = expansions ?: JsonArray(emptyList()),
        latestDecks = buildJsonObject { latest_decks.forEach {
            put(it.deck!!.id.toString(), buildJsonArray {
                add(it.deck.name)
                add(it.deck.meta)
                add(buildJsonObject {
                    put(it.deck.user.userInfo.id, it.deck.user.userInfo.handle)
                })
            })
        } },
        access = buildJsonObject { access.forEach {
            put(it.user!!.id, it.user.userInfo.handle)
        } },
        nextCampaignId = next_campaign_id?.toString(),
        previousCampaignId = previous_campaign?.toString()
    )
/**
 * Extension function to convert list of [GetMyCampaignsQuery.Campaign] to list of [Campaign]
 */
fun List<GetMyCampaignsQuery.Campaign>.toDbCampaigns(): List<Campaign> =
    mapNotNull { it.campaign?.campaign?.toDbCampaign() }