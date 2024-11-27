package com.example.easyinventory.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Inventory(
    val id: String = "",        // String ID (auto-generated or UUID)
    val name: String = "",
    val quantity: Int = 0,
    val photo: String = "",     // URL to the photo
    val price: Double = 0.0,    // price field
    val userId: String = ""     // User ID of the owner
)