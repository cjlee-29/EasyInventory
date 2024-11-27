package com.example.easyinventory.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.easyinventory.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AuthState {
    IDLE,
    AUTHENTICATING,
    AUTHENTICATED,
    AUTHENTICATION_FAILED,
    REGISTERING,
    REGISTERED,
    REGISTRATION_FAILED
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    // Define _errorMessage to store error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState

    init {
        // Check if user is logged in and fetch username from Firestore
        auth.currentUser?.let { currentUser ->
            fetchUsernameFromFirestore(currentUser.uid)
        }
    }

    fun loginWithUsername(username: String, password: String) {
        _authState.value = AuthState.AUTHENTICATING
        _errorMessage.value = null

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0].toObject(User::class.java)
                    val email = user?.email ?: ""
                    if (email.isNotEmpty()) {
                        login(email, password)
                    } else {
                        _authState.value = AuthState.AUTHENTICATION_FAILED
                        _errorMessage.value = "Email not found for username."
                    }
                } else {
                    _authState.value = AuthState.AUTHENTICATION_FAILED
                    _errorMessage.value = "Username not found."
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.AUTHENTICATION_FAILED
                _errorMessage.value = e.message
                Log.e("AuthViewModel", "Error fetching user", e)
            }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.AUTHENTICATED
                } else {
                    _authState.value = AuthState.AUTHENTICATION_FAILED

                    // Handle specific exceptions
                    val exception = task.exception
                    _errorMessage.value = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Handle specific error for invalid credentials
                            "Incorrect password. Please try again."
                        }
                        is FirebaseAuthInvalidUserException -> {
                            // Handle error for no user found
                            "No account found with this email."
                        }
                        else -> {
                            // Default message for other types of errors
                            "Incorrect password. Please try again."
                        }
                    }
                    Log.e("AuthViewModel", "Error logging in", task.exception)
                }
            }
    }


    fun register(username: String, email: String, password: String) {
        _authState.value = AuthState.REGISTERING
        _errorMessage.value = null

        // Step 1: Check if the username is unique
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // If username is unique, proceed to create a Firebase user
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: ""

                                // Step 2: Create the user data model
                                val user = User(id = userId, username = username, email = email)

                                // Step 3: Store user data in Firestore
                                db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener {
                                        _authState.value = AuthState.REGISTERED
                                    }
                                    .addOnFailureListener { e ->
                                        _authState.value = AuthState.REGISTRATION_FAILED
                                        _errorMessage.value = e.message
                                        Log.e("AuthViewModel", "Error writing user to Firestore", e)
                                    }
                            } else {
                                _authState.value = AuthState.REGISTRATION_FAILED

                                // Handle specific Firebase exceptions
                                val exception = task.exception
                                _errorMessage.value = when (exception) {
                                    is FirebaseAuthWeakPasswordException -> "Weak password: ${exception.reason}"
                                    is FirebaseAuthInvalidCredentialsException -> "Invalid email address."
                                    else -> exception?.localizedMessage ?: "Registration failed."
                                }
                                Log.e("AuthViewModel", "Error creating user", task.exception)
                            }
                        }
                } else {
                    // Username is not unique
                    _authState.value = AuthState.REGISTRATION_FAILED
                    _errorMessage.value = "Username already exists."
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.REGISTRATION_FAILED
                _errorMessage.value = e.message
                Log.e("AuthViewModel", "Error checking username uniqueness", e)
            }
    }


    fun logout() {
        auth.signOut()
        _authState.value = AuthState.IDLE
    }

    fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Password reset email sent to $email")
                    _errorMessage.value = "Password reset email sent."
                } else {
                    val exception = task.exception
                    _errorMessage.value = when (exception) {
                        is FirebaseAuthInvalidUserException -> "No user found with this email."
                        else -> exception?.localizedMessage ?: "Failed to send password reset email."
                    }
                    Log.e("AuthViewModel", "Error sending password reset email", task.exception)
                }
            }
    }

    fun fetchUsernameFromFirestore(userId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _username.value = document.getString("username") ?: "Unknown User"
                } else {
                    _username.value = "Unknown User"
                }
            }
            .addOnFailureListener {
                _username.value = "Unknown User"
            }
    }

}