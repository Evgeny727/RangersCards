package com.rangerscards.ui.decks

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.rangerscards.CreateDeckMutation
import com.rangerscards.GetMyDecksQuery
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.deck.Deck
import com.rangerscards.data.local.deck.DeckListItemProjection
import com.rangerscards.domain.repository.DecksRepository
import com.rangerscards.objects.DeckMetaMaps
import com.rangerscards.objects.StarterDecks
import com.rangerscards.domain.TimestampNormilizer
import com.rangerscards.ui.settings.UserUIState
import com.rangerscards.ui.settings.performFirebaseOperationWithRetry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val CURRENT_TABOO_SET = "set_01"

class DecksViewModel(
    private val apolloClient: ApolloClient,
    private val decksRepository: DecksRepository
) : ViewModel() {

    private val _deckIdToOpen = MutableStateFlow("")
    val deckIdToOpen: StateFlow<String> = _deckIdToOpen.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Holds the current search term entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun getAllNetworkDecks(user: FirebaseUser?, context: Context) {
        viewModelScope.launch {
            _isRefreshing.update { true }
            if (isConnected(context) && user != null) {
                var token: String? = ""
                val result = performFirebaseOperationWithRetry {
                    token = user.getIdToken(true).await().token
                }
                if (result != null) {
                    val response = apolloClient.query(GetMyDecksQuery(user.uid))
                        .addHttpHeader("Authorization", "Bearer $token")
                        .fetchPolicy(FetchPolicy.NetworkOnly).execute()
                    if (response.data != null) {
                        if (response.data?.decks?.isEmpty() == true) decksRepository.syncDecks(emptyList())
                        else decksRepository.syncDecks(response.data!!.decks.toDecks(true))
                    }
                }
            } else decksRepository.deleteAllUploadedDecks()
        }.invokeOnCompletion { _isRefreshing.update { false } }
    }

    fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) != null
    }

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<DeckListItemProjection>> =
        _searchQuery.flatMapLatest { query ->
            // When the search query or include flag changes, perform a new search.
            val userId = Firebase.auth.currentUser?.uid ?: ""
            if (query.trim().isEmpty()) {
                decksRepository.getAllDecks(userId).catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            } else {
                decksRepository.searchDecks(query.trim(), userId).catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            }
        }.cachedIn(viewModelScope)

    fun getRoles(specialty: String, taboo: Boolean, packIds: List<String>): Flow<PagingData<CardListItemProjection>> =
        decksRepository.getRoles(specialty, taboo, packIds).catch { throwable ->
            // Log the error.
            throwable.printStackTrace()
            // Return an empty PagingData on error so that the flow continues.
            emit(PagingData.empty())
        }.cachedIn(viewModelScope)

    fun getCard(id: String): Flow<CardListItemProjection?> = decksRepository.getCard(id)

    /**
     * Called when the user enters a new search term.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.update {
            newQuery
        }
    }

    fun clearSearchQuery() {
        _searchQuery.update { "" }
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun createDeck(
        name: String,
        background: String,
        specialty: String,
        role: String,
        isUploading: Boolean,
        starterDeckId: Int,
        postfix: String,
        user: UserUIState,
        taboo: Boolean,
        context: Context
    ) {
        val activity = (context as? AppCompatActivity)!!
        if (isUploading) {
            val token = user.currentUser!!.getIdToken(true).await().token
            if (starterDeckId >= 0) {
                val starterDeck = StarterDecks.starterDecks()[starterDeckId]
                val backgroundLocalized = DeckMetaMaps
                    .background[starterDeck.meta.background]
                val specialtyLocalized = DeckMetaMaps
                    .specialty[starterDeck.meta.specialty]
                val newDeck = apolloClient.mutation(CreateDeckMutation(
                    name = name.ifEmpty { "${activity.getString(backgroundLocalized!!)} - " +
                            "${activity.getString(specialtyLocalized!!)} $postfix" },
                    foc = starterDeck.foc,
                    fit = starterDeck.fit,
                    awa = starterDeck.awa,
                    spi = starterDeck.spi,
                    meta = starterDeck.meta,
                    slots = starterDeck.slots,
                    extraSlots = JsonNull,
                    tabooSetId = if (taboo) Optional.present(CURRENT_TABOO_SET) else Optional.absent()
                )).addHttpHeader("Authorization", "Bearer $token").execute()
                if (newDeck.data != null) {
                    decksRepository.insertDeck(newDeck.data!!.deck!!.deck.toDeck(true))
                    _deckIdToOpen.update { newDeck.data!!.deck!!.deck.id.toString() }
                }
            } else {
                val backgroundLocalized = DeckMetaMaps.background[background]
                val specialtyLocalized = DeckMetaMaps.specialty[specialty]
                val newDeck = apolloClient.mutation(CreateDeckMutation(
                    name = name.ifEmpty { "${activity.getString(backgroundLocalized!!)} - " +
                            activity.getString(specialtyLocalized!!) },
                    foc = 3,
                    fit = 3,
                    awa = 3,
                    spi = 3,
                    meta = buildJsonObject {
                        put("role", role)
                        put("background", background)
                        put("specialty", specialty)
                    },
                    slots = JsonNull,
                    extraSlots = JsonNull,
                    tabooSetId = if (taboo) Optional.present(CURRENT_TABOO_SET) else Optional.absent()
                )).addHttpHeader("Authorization", "Bearer $token").execute()
                if (newDeck.data != null) {
                    decksRepository.insertDeck(newDeck.data!!.deck!!.deck.toDeck(true))
                    _deckIdToOpen.update { newDeck.data!!.deck!!.deck.id.toString() }
                }
            }
        } else {
            val uuid = Uuid.random().toString()
            if (starterDeckId >= 0) {
                val starterDeck = StarterDecks.starterDecks()[starterDeckId]
                val backgroundLocalized = DeckMetaMaps
                    .background[starterDeck.meta.jsonObject["background"]?.jsonPrimitive?.content]
                val specialtyLocalized = DeckMetaMaps
                    .specialty[starterDeck.meta.jsonObject["specialty"]?.jsonPrimitive?.content]
                decksRepository.insertDeck(
                    createLocalDeck(
                        id = uuid,
                        deckName = name.ifEmpty { "${activity.getString(backgroundLocalized!!)} - " +
                                "${activity.getString(specialtyLocalized!!)} $postfix" },
                        meta = starterDeck.meta,
                        slots = starterDeck.slots,
                        awa = starterDeck.awa,
                        spi = starterDeck.spi,
                        fit = starterDeck.fit,
                        foc = starterDeck.foc,
                        taboo = taboo
                    )
                )
                _deckIdToOpen.update { uuid }
            } else {
                val backgroundLocalized = DeckMetaMaps.background[background]
                val specialtyLocalized = DeckMetaMaps.specialty[specialty]
                decksRepository.insertDeck(createLocalDeck(
                    id = uuid,
                    deckName = name.ifEmpty { "${activity.getString(backgroundLocalized!!)} - " +
                            activity.getString(specialtyLocalized!!) },
                    meta = buildJsonObject {
                        put("role", role)
                        put("background", background)
                        put("specialty", specialty)
                    },
                    slots = null,
                    taboo = taboo
                ))
                _deckIdToOpen.update { uuid }
            }
        }
    }

    private fun createLocalDeck(
        id: String,
        deckName: String,
        slots: JsonElement?,
        meta: JsonElement,
        awa: Int? = null,
        spi: Int? = null,
        fit: Int? = null,
        foc: Int? = null,
        taboo: Boolean,
    ): Deck {
        return Deck(
            id = id,
            uploaded = false,
            userId = "",
            tabooSetId = if (taboo) CURRENT_TABOO_SET else null,
            userHandle = null,
            slots = slots ?: JsonObject(emptyMap()),
            sideSlots = JsonObject(emptyMap()),
            extraSlots = JsonObject(emptyMap()),
            version = 1,
            name = deckName,
            description = null,
            awa = awa ?: 3,
            spi = spi ?: 3,
            fit = fit ?: 3,
            foc = foc ?: 3,
            createdAt = getCurrentDateTime(),
            updatedAt = getCurrentDateTime(),
            meta = meta,
            campaignId = null,
            campaignName = null,
            campaignRewards = null,
            previousId = null,
            previousSlots = null,
            previousSideSlots = null,
            nextId = null,
        )
    }
}

fun getCurrentDateTime(): String {
    // Get the current time
    val now = Date()
    // Create a formatter using the pattern "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    // The "XXX" pattern is supported in API 24 and formats the timezone as +00:00
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val formatted = sdf.format(now)
    // Replace trailing "Z" with "+00:00" if necessary
    return if (formatted.endsWith("Z")) {
        formatted.substring(0, formatted.length - 1) + "+00:00"
    } else {
        formatted
    }
}

/**
 * Extension function to convert [com.rangerscards.fragment.Deck] to [Deck]
 */
fun com.rangerscards.fragment.Deck.toDeck(uploaded: Boolean): Deck {
    return Deck(
        id = this.id.toString(),
        uploaded = uploaded,
        userId = this.user_id,
        tabooSetId = this.taboo_set_id,
        userHandle = this.user.userInfo.handle,
        slots = this.slots,
        sideSlots = this.side_slots,
        extraSlots = this.extra_slots,
        version = this.version,
        name = this.name,
        description = this.description,
        awa = this.awa,
        spi = this.spi,
        fit = this.fit,
        foc = this.foc,
        createdAt = TimestampNormilizer.fixFraction(this.created_at),
        updatedAt = TimestampNormilizer.fixFraction(this.updated_at),
        meta = this.meta,
        campaignId = if (this.campaign?.id != null) this.campaign.id.toString() else null,
        campaignName = this.campaign?.name,
        campaignRewards = this.campaign?.rewards,
        previousId = this.previous_deck?.id?.toString(),
        previousSlots = this.previous_deck?.slots,
        previousSideSlots = this.previous_deck?.side_slots,
        nextId = this.next_deck?.id?.toString(),
    )
}

/**
 * Extension function to convert list of [GetMyDecksQuery.Deck] to list of [Deck]
 */
fun List<GetMyDecksQuery.Deck>.toDecks(uploaded: Boolean): List<Deck> {
    return this.map {
        it.deck.toDeck(uploaded)
    }
}