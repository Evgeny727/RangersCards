package com.rangerscards.data.local.deck

import androidx.room.ColumnInfo

data class RoleCardProjection(
    val id: String,
    val code: String,
    val name: String?,
    val text: String?,
    @ColumnInfo(name = "real_image_src")
    val realImageSrc: String?,
    @ColumnInfo(name = "taboo_id")
    val tabooId: String?,
)
