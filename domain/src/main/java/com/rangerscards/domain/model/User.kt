package com.rangerscards.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Locale

data class User(
    val userInfo: UserInfo? = null,
    val friends: ImmutableList<UserInfo> = persistentListOf(),
    val sentRequests: ImmutableList<UserInfo> = persistentListOf(),
    val receivedRequests: ImmutableList<UserInfo> = persistentListOf(),
    val language: String = Locale.getDefault().language.substring(0..1),
    val settings: UserSettings = UserSettings()
)

data class UserInfo(
    val id: String,
    val handle: String?,
)

data class UserSettings(
    val taboo: Boolean = false,
    val collection: ImmutableList<String> = persistentListOf()
)
