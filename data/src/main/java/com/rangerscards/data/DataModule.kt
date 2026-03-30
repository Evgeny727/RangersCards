package com.rangerscards.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.network.ws.SubscriptionWsProtocol
import com.apollographql.apollo.network.ws.WebSocketNetworkTransport
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.rangerscards.data.local.RangersDatabase
import com.rangerscards.data.local.dao.CampaignDao
import com.rangerscards.data.local.dao.CardDao
import com.rangerscards.data.local.dao.DeckDao
import com.rangerscards.data.local.migrations.MigrationCampaignChallengeDeck
import com.rangerscards.data.local.migrations.MigrationCampaignExpansions
import com.rangerscards.data.local.migrations.MigrationCampaignTransfer
import com.rangerscards.data.local.migrations.MigrationRemoveFlavorFromFTS
import com.rangerscards.data.objects.JsonElementAdapter
import com.rangerscards.data.remote.NetworkConnectivityObserver
import com.rangerscards.type.Jsonb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("settings_preferences")
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideNetworkObserver(@ApplicationContext context: Context): NetworkConnectivityObserver =
        NetworkConnectivityObserver(context)

    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://gapi.rangersdb.com/v1/graphql")
            .subscriptionNetworkTransport(
                WebSocketNetworkTransport.Builder()
                .serverUrl("wss://gapi.rangersdb.com/v1/graphql")
                .protocol(SubscriptionWsProtocol.Factory(connectionPayload = suspend {
                    val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                    mapOf("headers" to mapOf("Authorization" to "Bearer $token"))
                }))
                .reopenWhen { _, attempt ->
                    delay(attempt * 1000)
                    attempt < 5
                }
                .build()
            )
            .addCustomScalarAdapter(Jsonb.type, JsonElementAdapter)
            .normalizedCache(SqlNormalizedCacheFactory("apollo.db"))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RangersDatabase {
        return Room.databaseBuilder(
            context,
            RangersDatabase::class.java,
            "rangers_database"
        )
            .addMigrations(
                MigrationCampaignTransfer,
                MigrationCampaignChallengeDeck,
                MigrationRemoveFlavorFromFTS,
                MigrationCampaignExpansions,
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideCardDao(db: RangersDatabase): CardDao = db.cardDao()

    @Provides
    @Singleton
    fun provideDeckDao(db: RangersDatabase): DeckDao = db.deckDao()

    @Provides
    @Singleton
    fun provideCampaignDao(db: RangersDatabase): CampaignDao = db.campaignDao()

}