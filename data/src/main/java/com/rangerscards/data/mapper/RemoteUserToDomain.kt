package com.rangerscards.data.mapper

import com.rangerscards.domain.model.User
import com.rangerscards.domain.model.UserInfo
import com.rangerscards.fragment.UserProfile
import kotlinx.collections.immutable.toImmutableList
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
 * Extension function to convert [UserProfile] to [User]
 */
fun UserProfile.toDomain(): User =
    User(
        userInfo.toDomain(),
        friends.mapNotNull { it.user?.userInfo?.toDomain() }.toImmutableList(),
        sent_requests.mapNotNull { it.user?.userInfo?.toDomain() }.toImmutableList(),
        received_requests.mapNotNull { it.user?.userInfo?.toDomain() }.toImmutableList()
    )