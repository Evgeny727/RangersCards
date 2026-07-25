package com.rangerscards.data.local.card

import androidx.room.ColumnInfo

data class CardDeckMulliganProjection(
    val id: String,
    val code: String,
    @ColumnInfo(name = "taboo_id")
    val tabooId: String?,
    val name: String?,
    @ColumnInfo(name = "approach_conflict")
    val approachConflict: Int?,
    @ColumnInfo(name = "approach_reason")
    val approachReason: Int?,
    @ColumnInfo(name = "approach_exploration")
    val approachExploration: Int?,
    @ColumnInfo(name = "approach_connection")
    val approachConnection: Int?,
    val traits: String?,
    val level: Int?,
    @ColumnInfo(name = "type_name")
    val typeName: String?,
    val cost: Int?,
    @ColumnInfo(name = "aspect_id")
    val aspectId: String?,
    @ColumnInfo(name = "aspect_short_name")
    val aspectShortName: String?,
    val setup: Boolean,
    @ColumnInfo(name = "image_src")
    val imageSrc: String?,
    @ColumnInfo(name = "real_image_src")
    val realImageSrc: String?,
)