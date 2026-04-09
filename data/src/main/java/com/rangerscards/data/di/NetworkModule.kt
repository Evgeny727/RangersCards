package com.rangerscards.data.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain
import com.apollographql.apollo.network.ws.SubscriptionWsProtocol
import com.apollographql.apollo.network.ws.WebSocketNetworkTransport
import com.google.firebase.auth.FirebaseAuth
import com.rangerscards.data.objects.JsonElementAdapter
import com.rangerscards.data.remote.AuthTokenProvider
import com.rangerscards.data.remote.FirebaseAuthTokenProvider
import com.rangerscards.data.remote.NetworkConnectivityObserver
import com.rangerscards.type.Jsonb
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import javax.inject.Singleton

const val SERVER_URL = "gapi.rangersdb.com/v1/graphql"

@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideNetworkObserver(@ApplicationContext context: Context): NetworkConnectivityObserver =
        NetworkConnectivityObserver(context)

    @Provides
    @Singleton
    fun provideApolloClient(authTokenProvider: AuthTokenProvider): ApolloClient = ApolloClient.Builder()
        .serverUrl("https://$SERVER_URL")
        .subscriptionNetworkTransport(
            WebSocketNetworkTransport.Builder()
                .serverUrl("wss://$SERVER_URL")
                .protocol(SubscriptionWsProtocol.Factory(connectionPayload = suspend {
                    val token = authTokenProvider.getToken(true)
                    mapOf("headers" to mapOf("Authorization" to "Bearer $token"))
                }))
                .reopenWhen { _, attempt ->
                    delay(attempt * 1000)
                    attempt < 5
                }
                .build()
        )
        .addHttpInterceptor( object : HttpInterceptor {
            override suspend fun intercept(
                request: HttpRequest,
                chain: HttpInterceptorChain
            ): HttpResponse {
                val token = authTokenProvider.getToken()
                val newRequest = if (token.isNullOrBlank()) {
                    request
                } else {
                    request.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                }

                return chain.proceed(newRequest)
            }
        })
        .addCustomScalarAdapter(Jsonb.type, JsonElementAdapter)
        .normalizedCache(SqlNormalizedCacheFactory("apollo.db"))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .build()

    @Binds
    @Singleton
    fun bindAuthTokenProvider(impl: FirebaseAuthTokenProvider): AuthTokenProvider

}