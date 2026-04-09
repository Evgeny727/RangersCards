package com.rangerscards.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.rangerscards.AcceptFriendRequestMutation
import com.rangerscards.GetProfileQuery
import com.rangerscards.GetUserInfoByHandleQuery
import com.rangerscards.GetUsersInfoByHandleQuery
import com.rangerscards.RejectFriendRequestMutation
import com.rangerscards.SendFriendRequestMutation
import com.rangerscards.SetAdhereTaboosMutation
import com.rangerscards.SetPackCollectionMutation
import com.rangerscards.UpdateHandleMutation
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class UserSettingsRemoteDataSource @Inject constructor(
    private val apolloClient: ApolloClient
) {

    suspend fun updateHandle(userId: String, handle: String, normalizedHandle: String) = apolloClient
        .mutation(UpdateHandleMutation(userId, handle, normalizedHandle))
        .execute()

    fun getProfile(id: String) = apolloClient
        .query(GetProfileQuery(id))
        .fetchPolicy(FetchPolicy.CacheAndNetwork)
        .toFlow()

    suspend fun setPackCollection(userId: String, packCollection: JsonElement) = apolloClient
        .mutation(SetPackCollectionMutation(userId, packCollection))
        .execute()

    suspend fun setTaboo(userId: String, adhereTaboos: Boolean) = apolloClient
        .mutation(SetAdhereTaboosMutation(userId, adhereTaboos))
        .execute()

    suspend fun getUserByHandle(handle: String) = apolloClient
        .query(GetUserInfoByHandleQuery(handle))
        .execute()

    suspend fun getUsersByHandle(handle: String) = apolloClient
        .query(GetUsersInfoByHandleQuery(handle))
        .execute()

    suspend fun sendFriendRequest(toUserId: String) = apolloClient
        .mutation(SendFriendRequestMutation(toUserId))
        .execute()

    suspend fun acceptFriendRequest(toUserId: String) = apolloClient
        .mutation(AcceptFriendRequestMutation(toUserId))
        .execute()

    suspend fun rejectFriendRequest(toUserId: String) = apolloClient
        .mutation(RejectFriendRequestMutation(toUserId))
        .execute()

}