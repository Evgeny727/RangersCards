package com.rangerscards.domain.repository

import com.rangerscards.domain.model.UserInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun getUserProfile(userId: String)

    suspend fun updateHandle(userId: String, handle: String)

    suspend fun setCollection(userId: String, collection: List<String>)

    suspend fun setTaboo(userId: String, taboo: Boolean)

    fun searchUsersByHandle(handle: String): Flow<ImmutableList<UserInfo>>

    suspend fun friendRequestAction(friendAction: FriendAction, friendUserId: String)

}

enum class FriendAction {
    SENT, ACCEPT, REVOKE
}