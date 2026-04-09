package com.rangerscards.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rangerscards.domain.repository.UserPreferencesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {
    override val isDarkTheme: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[THEME] ?: 2
        }

    override val isIncludeEnglishSearchResults: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[INCLUDE_ENGLISH_SEARCH_RESULTS] ?: false
        }

    override val isTabooSet: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[TABOO] ?: false
        }

    override val collection: Flow<ImmutableList<String>> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[COLLECTION]?.split(",")?.filter { it.isNotBlank() }?.toImmutableList() ?: persistentListOf()
        }

    override val cardsUpdatedAt: Flow<String> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[CARDS_UPDATED_AT] ?: ""
    }

    override val sortOrder: Flow<ImmutableList<String>> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[CARDS_SORT_ORDER]?.split(",")?.filter { it.isNotBlank() }?.toImmutableList() ?: persistentListOf()
        }

    private companion object {
        val THEME = intPreferencesKey("theme")
        const val TAG = "UserPreferencesRepo"
        val CARDS_UPDATED_AT = stringPreferencesKey("cards_updated_at")
        val INCLUDE_ENGLISH_SEARCH_RESULTS = booleanPreferencesKey("english_results")
        val TABOO = booleanPreferencesKey("taboo")
        val COLLECTION = stringPreferencesKey("collection")
        val CARDS_SORT_ORDER = stringPreferencesKey("cards_sort_order")
    }

    override suspend fun saveThemePreference(theme: Int) {
        dataStore.edit { preferences ->
            preferences[THEME] = theme
        }
    }

    override suspend fun saveCardsUpdatedTimestamp(timestamp: String) {
        dataStore.edit { preferences ->
            preferences[CARDS_UPDATED_AT] = timestamp
        }
    }

    override suspend fun saveIncludeEnglishSearchResults(isIncludeEnglishSearchResults: Boolean) {
        dataStore.edit { preferences ->
            preferences[INCLUDE_ENGLISH_SEARCH_RESULTS] = isIncludeEnglishSearchResults
        }
    }

    override suspend fun saveTabooPreference(taboo: Boolean) {
        dataStore.edit { preferences ->
            preferences[TABOO] = taboo
        }
    }

    override suspend fun saveCollectionPreference(collection: List<String>) {
        dataStore.edit { preferences ->
            preferences[COLLECTION] = collection.joinToString(",")
        }
    }

    override suspend fun saveSortOrderPreference(sortOrder: List<String>) {
        dataStore.edit { preferences ->
            preferences[CARDS_SORT_ORDER] = sortOrder.joinToString(",")
        }
    }
}