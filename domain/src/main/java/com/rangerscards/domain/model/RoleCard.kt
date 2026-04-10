package com.rangerscards.domain.model

data class RoleCard(
    val id: String,
    val code: String,
    val name: String?,
    val text: String?,
    val realImageSrc: String?,
    val tabooId: String?,
)
