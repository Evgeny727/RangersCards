package com.rangerscards.data.remote

import com.apollographql.apollo.ApolloClient
import com.rangerscards.GetAllCardsQuery
import com.rangerscards.GetCardsUpdatedAtQuery
import javax.inject.Inject

class CardsRemoteDataSource @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun fetchAllCards(locale: String) = apolloClient
        .query(GetAllCardsQuery(locale))
        .execute()

    suspend fun fetchCardsUpdatedAt(locale: String) = apolloClient
        .query(GetCardsUpdatedAtQuery(locale))
        .execute()
}