package com.rangerscards.data.repository

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.rangerscards.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(private val auth: FirebaseAuth) : AuthRepository {

    override val currentUserId: Flow<String?> =
        callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser?.uid)
            }

            auth.addAuthStateListener(listener)
            trySend(auth.currentUser?.uid)

            awaitClose {
                auth.removeAuthStateListener(listener)
            }
        }

    override val isLoggedIn: Flow<Boolean> =
        currentUserId.map { it != null }

    override suspend fun getToken(forceRefresh: Boolean): Result<String?> =
        runCatching {
            performFirebaseOperationWithRetry {
                auth.currentUser?.getIdToken(forceRefresh)?.await()?.token
            }
        }

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await()
            Unit
        }

    override suspend fun createAccount(email: String, password: String): Result<Unit> =
        runCatching {
            auth.createUserWithEmailAndPassword(email, password).await()
            Unit
        }

    override suspend fun deleteAccount(email: String, password: String): Result<Unit> =
        runCatching {
            auth.currentUser?.reauthenticate(EmailAuthProvider.getCredential(email, password))?.await()
            auth.currentUser?.delete()?.await()
            Unit
        }

    override suspend fun signOut() {
        auth.signOut()
    }

    private suspend fun <T> performFirebaseOperationWithRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T? {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                Log.w("FirebaseOperation", "Attempt ${attempt + 1} failed: ${e.localizedMessage}")
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
        return null
    }
}