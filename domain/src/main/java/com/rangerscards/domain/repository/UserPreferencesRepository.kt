package com.rangerscards.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isDarkTheme: Flow<Int>
    val isIncludeEnglishSearchResults: Flow<Boolean>
    val isTabooSet: Flow<Boolean>
    val collection: Flow<List<String>>
    val cardsUpdatedAt: Flow<String>
    val sortOrder: Flow<List<String>>

    suspend fun saveThemePreference(theme: Int)
    suspend fun saveCardsUpdatedTimestamp(timestamp: String)
    suspend fun saveIncludeEnglishSearchResults(isIncludeEnglishSearchResults: Boolean)
    suspend fun saveTabooPreference(taboo: Boolean)
    suspend fun saveCollectionPreference(collection: List<String>)
    suspend fun saveSortOrderPreference(sortOrder: List<String>)
}