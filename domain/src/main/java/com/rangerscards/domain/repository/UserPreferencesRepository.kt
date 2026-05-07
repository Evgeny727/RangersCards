package com.rangerscards.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserPreferencesRepository {
    val isDarkTheme: Flow<Int>
    val isIncludeEnglishSearchResults: StateFlow<Boolean>
    val isTabooSet: Flow<Boolean>
    val collection: Flow<List<String>>
    val cardsUpdatedAt: Flow<String>
    val sortOrder: Flow<List<String>>

    suspend fun saveThemePreference(theme: Int)
    suspend fun saveCardsUpdatedTimestamp(timestamp: String)
    suspend fun saveIncludeEnglishSearchResults(isIncludeEnglishSearchResults: Boolean)
    suspend fun saveTabooPreference(taboo: Boolean)
    suspend fun saveCollectionPreference(collection: List<String>)
    suspend fun saveTabooAndCollectionPreference(taboo: Boolean, collection: List<String>)
    suspend fun saveSortOrderPreference(sortOrder: List<String>)
}