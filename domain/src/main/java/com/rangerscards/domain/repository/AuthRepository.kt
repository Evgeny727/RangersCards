package com.rangerscards.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUserId: StateFlow<String?>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun createAccount(email: String, password: String): Result<Unit>
    suspend fun deleteAccount(email: String, password: String): Result<Unit>
    suspend fun signOut()
}