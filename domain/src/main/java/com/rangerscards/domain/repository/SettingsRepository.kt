package com.rangerscards.domain.repository

import com.rangerscards.domain.model.User
import com.rangerscards.domain.model.UserInfo
import com.rangerscards.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun startUserSubscription(userId: String): Flow<Pair<Result<User>, Result<UserSettings>>>

    suspend fun updateHandle(userId: String, handle: String): Result<Unit>

    suspend fun setCollection(userId: String, collection: List<String>): Result<Unit>

    suspend fun setTaboo(userId: String, taboo: Boolean): Result<Unit>

    suspend fun searchUsersByHandle(handle: String): Result<List<UserInfo>>

    suspend fun friendRequestAction(friendAction: FriendAction, friendUserId: String): Result<Unit>

}

enum class FriendAction {
    SENT, ACCEPT, REVOKE
}