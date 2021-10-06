package com.sivakasi.papco.jobflow.util

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class JobFlowAuth @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance()
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun logIn(email: String, password: String): AuthResult =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(
                email,
                password
            ).addOnSuccessListener {
                continuation.resume(it)
            }.addOnFailureListener {
                continuation.resumeWithException(it)
            }
        }

    fun logout() = auth.signOut()


    suspend fun fetchUserClaim(user: FirebaseUser?, forceRefresh: Boolean = false): String =
        suspendCancellableCoroutine { continuation ->

            if (user == null) {
                continuation.resume("none")
            } else {
                auth.currentUser?.getIdToken(forceRefresh)
                    ?.addOnSuccessListener {
                        val claim = it.claims["role"] as String
                        continuation.resume(claim)
                    }?.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }


    suspend fun registerUser(email:String,password:String,displayName:String): HttpsCallableResult? =
        suspendCancellableCoroutine { continuation ->

            val data = hashMapOf(
                "email" to email,
                "password" to password,
                "displayName" to displayName
            )

            functions.getHttpsCallable("createNewUser")
                .call(data)
                .addOnSuccessListener {
                    continuation.resume(it)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }

        }

    suspend fun updateUserClaim(email: String, role: String): HttpsCallableResult? =
        suspendCancellableCoroutine { continuation ->

            val data = hashMapOf(
                "email" to email,
                "role" to role
            )

            functions.getHttpsCallable("updateUserClaim")
                .call(data)
                .addOnSuccessListener {
                    continuation.resume(it)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

}