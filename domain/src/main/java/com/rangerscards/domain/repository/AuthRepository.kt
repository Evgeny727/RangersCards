package com.rangerscards.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<String?>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun createAccount(email: String, password: String): Result<Unit>
    suspend fun deleteAccount(email: String, password: String): Result<Unit>
    suspend fun signOut()
}