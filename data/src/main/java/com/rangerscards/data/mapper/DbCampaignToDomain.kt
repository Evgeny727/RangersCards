package com.rangerscards.data.mapper

import com.rangerscards.data.local.campaign.CampaignListItemProjection
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.CampaignCalendar
import com.rangerscards.domain.model.CampaignDeck
import com.rangerscards.domain.model.CampaignEvent
import com.rangerscards.domain.model.CampaignHistory
import com.rangerscards.domain.model.CampaignListItem
import com.rangerscards.domain.model.CampaignMission
import com.rangerscards.domain.model.CampaignNote
import com.rangerscards.domain.model.CampaignRemoved
import com.rangerscards.domain.model.PlayerInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.rangerscards.data.local.campaign.Campaign as DbCampaign

/**
 * Extension function to convert [DbCampaign] to [Campaign]
 */
fun DbCampaign.toDomain(): Campaign =
    Campaign(
        id = id,
        uploaded = uploaded,
        userId = userId,
        name = name,
        currentDay = day,
        extendedCalendar = extendedCalendar ?: false,
        cycleId = cycleId,
        currentLocation = currentLocation,
        currentPathTerrain = currentPathTerrain,
        createdAt = createdAt,
        updatedAt = updatedAt,
        notes = notes.toNotes(),
        missions = missions.toMissions(),
        events = events.toEvents(),
        rewards = rewards.jsonArray.map { it.jsonPrimitive.content }.toImmutableList(),
        removed = removed.toRemoved(),
        history = history.toHistory(),
        calendar = calendar.toCalendar(),
        expansions = expansions.jsonArray.map { it.jsonPrimitive.content }.toImmutableList(),
        decks = latestDecks.toCampaignDecks(),
        access = access.toPlayerInfo(),
        previousCampaignId = previousCampaignId,
        nextCampaignId = nextCampaignId
    )

/**
 * Extension function to convert [CampaignListItemProjection] to [CampaignListItem]
 */
fun CampaignListItemProjection.toDomain(): CampaignListItem =
    CampaignListItem(
        id = id,
        cycleId = cycleId,
        name = name,
        day = day,
        currentLocation = currentLocation,
        latestDecksRoles = latestDecks.jsonObject.map {
            val value = it.value.jsonArray
            val meta = value[1].jsonObject
            meta["role"]?.jsonPrimitive?.content ?: ""
        }.toImmutableList(),
        players = access.jsonObject.mapValues {
            it.value.jsonPrimitive.content
        }.values.toImmutableList()
    )

internal fun JsonElement.toNotes(): ImmutableList<CampaignNote> =
    jsonArray.map { note ->
        val value = note.jsonObject
        CampaignNote(
            value["day"]!!.jsonPrimitive.content.toInt(),
            value["note"]!!.jsonPrimitive.content,
            value["crossed_out"]?.jsonPrimitive?.content.toBoolean(),
        )
    }.toImmutableList()

internal fun JsonElement.toMissions(): ImmutableList<CampaignMission> =
    jsonArray.map { mission ->
        val value = mission.jsonObject
        CampaignMission(
            value["day"]!!.jsonPrimitive.content.toInt(),
            value["name"]!!.jsonPrimitive.content,
            value["checks"]?.jsonArray?.map { it.jsonPrimitive.content.toBoolean() }
                ?: listOf(false, false, false),
            value["completed"]?.jsonPrimitive?.content.toBoolean()
        )
    }.toImmutableList()

internal fun JsonElement.toEvents(): ImmutableList<CampaignEvent> =
    jsonArray.map { event ->
        val value = event.jsonObject
        CampaignEvent(
            value["event"]!!.jsonPrimitive.content,
            value["crossed_out"]?.jsonPrimitive?.content.toBoolean(),
            value["marks"]?.jsonPrimitive?.content?.toInt() ?: 0
        )
    }.toImmutableList()

internal fun JsonElement.toRemoved(): ImmutableList<CampaignRemoved> =
    jsonArray.map { removed ->
        val value = removed.jsonObject
        CampaignRemoved(
            value["name"]!!.jsonPrimitive.content,
            value["set_id"]?.jsonPrimitive?.content ?: ""
        )
    }.toImmutableList()

internal fun JsonElement.toHistory(): ImmutableList<CampaignHistory> =
    jsonArray.map { history ->
        val value = history.jsonObject
        CampaignHistory(
            value["day"]!!.jsonPrimitive.content.toInt(),
            value["camped"]!!.jsonPrimitive.content.toBoolean(),
            value["location"]!!.jsonPrimitive.content,
            value["path_terrain"]!!.jsonPrimitive.content
        )
    }.toImmutableList()

internal fun JsonElement.toCalendar(): ImmutableList<CampaignCalendar> =
    jsonArray.map { calendarEntry ->
        val value = calendarEntry.jsonObject
        CampaignCalendar(
            value["day"]!!.jsonPrimitive.content.toInt(),
            value["guides"]!!.jsonArray.map { it.jsonPrimitive.content }.toImmutableList()
        )
    }.toImmutableList()

internal fun JsonElement.toCampaignDecks(): ImmutableList<CampaignDeck> =
    jsonObject.map {
        val value = it.value.jsonArray
        val user = value[2].jsonObject
        CampaignDeck(
            it.key,
            value[0].jsonPrimitive.content,
            value[1].toDeckMeta(),
            PlayerInfo(
                user.keys.first(),
                user.values.first().jsonPrimitive.content

            )
        )
    }.toImmutableList()

internal fun JsonElement.toPlayerInfo(): ImmutableList<PlayerInfo> =
    jsonObject.map { user ->
        PlayerInfo(
            user.key,
            user.value.jsonPrimitive.content
        )
    }.toImmutableList()