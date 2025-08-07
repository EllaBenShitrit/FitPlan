package com.ellabs.fitplan.managers

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

data class UserProfile(
    val username: String = "",
    val bio: String = "",
    val status: String = "",
    val imageUrl: String? = null
)

object ProfileManager {

    fun loadProfile(
        uid: String,
        onSuccess: (UserProfile) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val profile = UserProfile(
                        username = document.getString("username") ?: "",
                        bio = document.getString("bio") ?: "",
                        status = document.getString("status") ?: "",
                        imageUrl = document.getString("imageUrl")
                    )
                    onSuccess(profile)
                } else {
                    onFailure(Exception("Profile not found"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }
}

