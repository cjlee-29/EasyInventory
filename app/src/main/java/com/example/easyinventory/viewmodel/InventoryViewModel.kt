package com.example.easyinventory.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.easyinventory.model.Inventory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InventoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _inventoryItems = MutableStateFlow<List<Inventory>>(emptyList())
    val inventoryItems: StateFlow<List<Inventory>> = _inventoryItems

    // Function to add inventory item
    suspend fun addInventoryItem(item: Inventory): Boolean {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return false

        val inventoryRef = db.collection("inventory")
        val documentRef = inventoryRef.document() // Auto-generated ID
        val itemWithId = item.copy(id = documentRef.id, userId = currentUserUid)

        return try {
            documentRef.set(itemWithId).await()
            Log.d("InventoryViewModel", "Inventory item added successfully: $itemWithId")
            true
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error adding inventory item", e)
            false
        }
    }

    // Function to load inventory items
    fun loadInventoryItems() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("inventory")
            .whereEqualTo("userId", currentUserUid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("InventoryViewModel", "Error loading inventory items", e)
                    return@addSnapshotListener
                }
                val items = snapshots?.documents?.mapNotNull { it.toObject(Inventory::class.java) }
                _inventoryItems.value = items ?: emptyList()
            }
    }

    // Function to get inventory item by ID
    suspend fun getInventoryItemById(itemId: String): Inventory? {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return try {
            val document = db.collection("inventory").document(itemId).get().await()
            val item = document.toObject(Inventory::class.java)
            if (item != null && item.userId == currentUserUid) {
                item  // Item found and the current user is the owner
            } else {
                null  // Either item is null or the user doesn't own this item
            }
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error fetching inventory item: ${e.localizedMessage}", e)
            null
        }
    }

    // Function to update inventory item
    suspend fun updateInventoryItem(item: Inventory): Boolean {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        if (item.userId != currentUserUid) {
            // The user is not allowed to update this item
            return false
        }
        return try {
            db.collection("inventory").document(item.id).set(item).await()
            Log.d("InventoryViewModel", "Inventory item updated successfully: ${item.id}")
            true
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error updating inventory item", e)
            false
        }
    }

    // Function to delete inventory item
    suspend fun deleteInventoryItem(itemId: String): Boolean {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        return try {
            // First, fetch the inventory item to get the image URL if it exists
            val document = db.collection("inventory").document(itemId).get().await()
            val item = document.toObject(Inventory::class.java)

            // Check if the item exists and if the user has permission to delete it
            if (item != null && item.userId == currentUserUid) {
                // If the item has a photo URL, delete the image from Firebase Storage
                if (item.photo.isNotEmpty()) {
                    val imageRef = storage.getReferenceFromUrl(item.photo)
                    imageRef.delete().await()  // Await image deletion
                    Log.d("InventoryViewModel", "Image deleted successfully: ${item.photo}")
                }

                // Now delete the inventory document from Firestore
                db.collection("inventory").document(itemId).delete().await()
                Log.d("InventoryViewModel", "Inventory item deleted successfully: $itemId")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error deleting inventory item", e)
            false
        }
    }


}