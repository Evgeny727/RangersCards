package com.rangerscards.data.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.interceptor.RetryOnErrorInterceptor
import com.apollographql.apollo.network.NetworkMonitor
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain
import com.apollographql.apollo.network.websocket.GraphQLWsProtocol
import com.apollographql.apollo.network.websocket.WebSocketNetworkTransport
import com.google.firebase.auth.FirebaseAuth
import com.rangerscards.data.objects.JsonElementAdapter
import com.rangerscards.data.remote.AuthTokenProvider
import com.rangerscards.type.Jsonb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

const val SERVER_URL = "gapi.rangersdb.com/v1/graphql"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @OptIn(ApolloExperimental::class)
    @Provides
    @Singleton
    fun provideApolloClient(
        authHttpInterceptor: AuthHttpInterceptor,
        authTokenProvider: AuthTokenProvider,
        @ApplicationContext context: Context
    ): ApolloClient = ApolloClient.Builder()
        .serverUrl("https://$SERVER_URL")
        .subscriptionNetworkTransport(
            WebSocketNetworkTransport.Builder()
                .serverUrl("wss://$SERVER_URL")
                .wsProtocol(GraphQLWsProtocol(connectionPayload = suspend {
                    val token = authTokenProvider.getToken(true)
                    mapOf("headers" to mapOf("Authorization" to "Bearer $token"))
                }))
                .build()
        )
        .addHttpInterceptor(authHttpInterceptor)
        .retryOnErrorInterceptor(RetryOnErrorInterceptor(NetworkMonitor(context)))
        .failFastIfOffline(true)
        .addCustomScalarAdapter(Jsonb.type, JsonElementAdapter)
        .build()

}

@Singleton
class AuthHttpInterceptor @Inject constructor(
    private val authTokenProvider: AuthTokenProvider
) : HttpInterceptor {

    private val refreshMutex = Mutex()

    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        val token = authTokenProvider.getToken()
        val initialRequest = request.withBearerToken(token)

        val response = chain.proceed(initialRequest)
        if (response.statusCode != 401) return response

        val refreshedToken = refreshMutex.withLock {
            authTokenProvider.getToken(true)
        }

        if (refreshedToken.isNullOrBlank()) {
            return response
        }

        return chain.proceed(request.withBearerToken(refreshedToken))
    }

    private fun HttpRequest.withBearerToken(token: String?): HttpRequest {
        return if (token.isNullOrBlank()) {
            this
        } else {
            newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
    }
}