package com.rangerscards.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class User(
    val userInfo: UserInfo,
    val friends: ImmutableList<UserInfo>,
    val sentRequests: ImmutableList<UserInfo>,
    val receivedRequests: ImmutableList<UserInfo>
)

data class UserInfo(
    val id: String,
    val handle: String?,
)

data class UserSettings(
    val taboo: Boolean = false,
    val collection: ImmutableList<String> = persistentListOf()
)
