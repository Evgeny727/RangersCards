package com.rangerscards.data.repository

import com.rangerscards.data.mapper.toDomain
import com.rangerscards.data.remote.UserSettingsRemoteDataSource
import com.rangerscards.domain.repository.FriendAction
import com.rangerscards.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import java.util.Locale
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val userSettingsRemoteDataSource: UserSettingsRemoteDataSource
) : SettingsRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserProfile(userId: String) = userSettingsRemoteDataSource.getProfile(userId)
        .map { response ->
            runCatching {
                response.dataAssertNoErrors.profile!!.userProfile.toDomain()
            }
        }

    override suspend fun updateHandle(userId: String, handle: String) = runCatching {
        validateHandle(handle)

        userSettingsRemoteDataSource.updateHandle(
            userId,
            handle,
            normalizeHandle(handle)
        ).dataAssertNoErrors
        Unit
    }

    private suspend fun validateHandle(handle: String){
        if (handle.length !in 3..22)
            throw IllegalArgumentException("invalid_handle_size")

        val result = userSettingsRemoteDataSource.getUserByHandle(handle)
        if (result.data?.profile?.isEmpty() == false)
            throw IllegalArgumentException("invalid_handle_already_taken")
    }

    private fun normalizeHandle(handle: String): String {
        return handle.replace("[\\.\\$\\[\\]#/]".toRegex(),"_")
            .lowercase(Locale.ENGLISH).trim()
    }

    override suspend fun setTaboo(userId: String, adhereTaboos: Boolean) = runCatching {
        userSettingsRemoteDataSource.setTaboo(userId, adhereTaboos).dataAssertNoErrors
        Unit
    }

    override suspend fun setCollection(userId: String, packCollection: List<String>) = runCatching {
        val packCollectionJson = buildJsonArray { packCollection.forEach { add(it) } }
        userSettingsRemoteDataSource
            .setPackCollection(userId, packCollectionJson).dataAssertNoErrors
        Unit
    }

    override suspend fun searchUsersByHandle(handle: String) = runCatching {
        val usersData = userSettingsRemoteDataSource.getUsersByHandle(handle)
        usersData.dataAssertNoErrors.profile.map { it.userInfo.toDomain() }
    }

    override suspend fun friendRequestAction(friendAction: FriendAction, friendUserId: String) = runCatching {
        when (friendAction) {
            FriendAction.SENT -> userSettingsRemoteDataSource.sendFriendRequest(friendUserId)
            FriendAction.ACCEPT -> userSettingsRemoteDataSource.acceptFriendRequest(friendUserId)
            FriendAction.REVOKE -> userSettingsRemoteDataSource.rejectFriendRequest(friendUserId)
        }.dataAssertNoErrors
        Unit
    }
}