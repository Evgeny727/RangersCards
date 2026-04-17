package com.rangerscards.data.mapper

import com.rangerscards.ProfileSubscription
import com.rangerscards.domain.model.User
import com.rangerscards.domain.model.UserInfo
import com.rangerscards.domain.model.UserSettings
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import com.rangerscards.fragment.UserInfo as RemoteUserInfo


/**
 * Extension function to convert [RemoteUserInfo] to [UserInfo]
 */
fun RemoteUserInfo.toDomain(): UserInfo =
    UserInfo(
        this.id,
        this.handle
    )

/**
 * Extension function to convert [ProfileSubscription.Settings] to [UserInfo]
 */
fun ProfileSubscription.Settings.toDomain(): UserSettings =
    UserSettings(
        adhere_taboos ?: false,
        pack_collection?.jsonArray?.map { it.jsonPrimitive.content }?.toImmutableList() ?: persistentListOf()
    )

/**
 * Extension function to convert [ProfileSubscription.Data] to [User]
 */
fun ProfileSubscription.Data.toDomain(): User {
    val profile = profile!!.userProfile
    val settings = settings!!
    return User(
        userInfo = profile.userInfo.toDomain(),
        friends = profile.friends.mapNotNull { it.user?.userInfo?.toDomain() }.toImmutableList(),
        sentRequests = profile.sent_requests.mapNotNull { it.user?.userInfo?.toDomain() }.toImmutableList(),
        receivedRequests = profile.received_requests.mapNotNull { it.user?.userInfo?.toDomain() }.toImmutableList(),
        settings = settings.toDomain()
    )
}