package com.rangerscards.data.remote

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface AuthTokenProvider {
    suspend fun getToken(forceRefresh: Boolean? = null): String?
}

class FirebaseAuthTokenProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthTokenProvider {

    override suspend fun getToken(forceRefresh: Boolean?): String? = performFirebaseOperationWithRetry {
        firebaseAuth.currentUser?.getIdToken(forceRefresh ?: false)?.await()?.token
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
            } catch (e: FirebaseException) {
                Log.w("FirebaseOperation", "Attempt ${attempt + 1} failed: ${e.localizedMessage}")
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
        return null
    }
}