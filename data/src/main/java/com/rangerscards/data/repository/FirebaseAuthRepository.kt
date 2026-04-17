package com.rangerscards.data.repository

import android.util.Patterns
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.rangerscards.domain.exceptions.InvalidEmailException
import com.rangerscards.domain.exceptions.InvalidPasswordException
import com.rangerscards.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            if (validateEmail(email)) {
                if (validatePassword(password)) {
                    auth.signInWithEmailAndPassword(email, password).await()
                    Unit
                } else throw InvalidPasswordException()
            } else throw InvalidEmailException()
        }

    override suspend fun createAccount(email: String, password: String): Result<Unit> =
        runCatching {
            if (validateEmail(email)) {
                if (validatePassword(password)) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    Unit
                } else throw InvalidPasswordException()
            } else throw InvalidEmailException()
        }

    override suspend fun deleteAccount(email: String, password: String): Result<Unit> =
        runCatching {
            if (validateEmail(email)) {
                if (validatePassword(password)) {
                    auth.currentUser?.reauthenticate(EmailAuthProvider.getCredential(email, password))?.await()
                    auth.currentUser?.delete()?.await()
                    Unit
                } else throw InvalidPasswordException()
            } else throw InvalidEmailException()
        }

    override suspend fun signOut() {
        auth.signOut()
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validatePassword(password: String): Boolean {
        return password.length in 6..4096
    }
}