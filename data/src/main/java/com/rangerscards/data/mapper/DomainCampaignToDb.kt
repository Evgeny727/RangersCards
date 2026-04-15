package com.rangerscards.data.mapper

import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.CampaignCalendar
import com.rangerscards.domain.model.CampaignDeck
import com.rangerscards.domain.model.CampaignEvent
import com.rangerscards.domain.model.CampaignHistory
import com.rangerscards.domain.model.CampaignMission
import com.rangerscards.domain.model.CampaignNote
import com.rangerscards.domain.model.CampaignRemoved
import com.rangerscards.domain.model.PlayerInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.rangerscards.data.local.campaign.Campaign as DbCampaign

/**
 * Extension function to convert [Campaign] to [DbCampaign]
 */
fun Campaign.toDbCampaign(): DbCampaign =
    DbCampaign(
        id = id,
        uploaded = uploaded,
        userId = userId,
        name = name,
        notes = notes.toJsonNotes(),
        day = currentDay,
        extendedCalendar = extendedCalendar,
        cycleId = cycleId,
        currentLocation = currentLocation,
        currentPathTerrain = currentPathTerrain,
        missions = missions.toJsonMissions(),
        events = events.toJsonEvents(),
        rewards = buildJsonArray { rewards.forEach { add(it) } },
        removed = removed.toJsonRemoved(),
        history = history.toJsonHistory(),
        calendar = calendar.toJsonCalendar(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        expansions = buildJsonArray { expansions.forEach { add(it) } },
        latestDecks = decks.toJsonDecks(),
        access = access.toJsonAccess(),
        nextCampaignId = nextCampaignId,
        previousCampaignId = previousCampaignId
    )

/**
 * Extension function to convert [ImmutableList] of [CampaignNote] to [JsonElement]
 */
fun ImmutableList<CampaignNote>.toJsonNotes(): JsonElement =
    buildJsonArray {
        this@toJsonNotes.forEach {
            add(
                buildJsonObject {
                    put("day", it.day)
                    put("note", it.note)
                    put("crossed_out", it.crossedOut)
                }
            )
        }
    }

/**
 * Extension function to convert [ImmutableList] of [CampaignMission] to [JsonElement]
 */
fun ImmutableList<CampaignMission>.toJsonMissions(): JsonElement =
    buildJsonArray {
        this@toJsonMissions.forEach {
            add(
                buildJsonObject {
                    put("day", it.day)
                    put("name", it.name)
                    put("checks", buildJsonArray {
                        it.checks.forEach { check -> add(check) }
                    })
                    put("completed", it.completed)
                }
            )
        }
    }

/**
 * Extension function to convert [ImmutableList] of [CampaignEvent] to [JsonElement]
 */
fun ImmutableList<CampaignEvent>.toJsonEvents(): JsonElement =
    buildJsonArray {
        this@toJsonEvents.forEach {
            add(
                buildJsonObject {
                    put("event", it.name)
                    put("crossed_out", it.crossedOut)
                    if (it.marks > 0) put("marks", it.marks)
                }
            )
        }
    }

/**
 * Extension function to convert [ImmutableList] of [CampaignRemoved] to [JsonElement]
 */
fun ImmutableList<CampaignRemoved>.toJsonRemoved(): JsonElement =
    buildJsonArray {
        this@toJsonRemoved.forEach {
            add(
                buildJsonObject {
                    put("name", it.name)
                    if (it.setId.isNotBlank()) put("set_id", it.setId)
                }
            )
        }
    }

/**
 * Extension function to convert [ImmutableList] of [CampaignHistory] to [JsonElement]
 */
fun ImmutableList<CampaignHistory>.toJsonHistory(): JsonElement =
    buildJsonArray {
        this@toJsonHistory.forEach {
            add(
                buildJsonObject {
                    put("day", it.day)
                    put("camped", it.camped)
                    put("location", it.location)
                    put("path_terrain", it.pathTerrain)
                }
            )
        }
    }

/**
 * Extension function to convert [ImmutableList] of [CampaignCalendar] to [JsonElement]
 */
fun ImmutableList<CampaignCalendar>.toJsonCalendar(): JsonElement =
    buildJsonArray {
        this@toJsonCalendar.forEach {
            add(
                buildJsonObject {
                    put("day", it.day)
                    put("guides", buildJsonArray {
                        it.guides.forEach { guide -> add(guide) }
                    })
                }
            )
        }
    }

/**
 * Extension function to convert [ImmutableList] of [CampaignDeck] to [JsonElement]
 */
fun ImmutableList<CampaignDeck>.toJsonDecks(): JsonElement =
    buildJsonObject {
        this@toJsonDecks.forEach {
            put(it.id, buildJsonArray {
                add(it.name)
                add(it.meta.toJsonDeckMeta())
                add(buildJsonObject {
                    put(it.user.userId, it.user.userName)
                })
            })
        }
    }

/**
 * Extension function to convert [ImmutableList] of [PlayerInfo] to [JsonElement]
 */
fun ImmutableList<PlayerInfo>.toJsonAccess(): JsonElement =
    buildJsonObject {
        this@toJsonAccess.forEach {
            put(it.userId, it.userName)
        }
    }