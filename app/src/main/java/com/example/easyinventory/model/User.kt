package com.example.easyinventory.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val id: String = "",        // User ID from Firebase Authentication
    val username: String = "",  // User's chosen username
    val email: String = ""      // User's email
)