package com.example.easyinventory.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easyinventory.R
import com.example.easyinventory.viewmodel.AuthState
import com.example.easyinventory.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // State for Forgot Password dialog visibility
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Reacting to authentication state
    LaunchedEffect(authState) {
        if (authState == AuthState.AUTHENTICATED) {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        } else if (authState == AuthState.AUTHENTICATION_FAILED) {
            errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val appIcon: Painter = painterResource(id = R.mipmap.ic_launcher)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = appIcon,
                contentDescription = "App Icon",
                modifier = Modifier.size(128.dp).fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Login button
            Button(
                onClick = {
                    if (username.isBlank()) {
                        Toast.makeText(context, "Please enter your username", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password.isBlank()) {
                        Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.loginWithUsername(username, password)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }

        // "Forgot Password?" and "Register" at the bottom of the screen
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // "Forgot Password?" Text Button
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    Text("Forgot Password?")
                }

                // "Register" Text Button
                TextButton(onClick = { navController.navigate("register") }) {
                    Text("Register")
                }
            }
        }
    }

    // Show Forgot Password Dialog if triggered
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onPasswordReset = { email ->
                viewModel.sendPasswordResetEmail(email)
                Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                showForgotPasswordDialog = false
            }
        )
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onPasswordReset: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Forgot Password") },
        text = {
            Column {
                Text("Enter your email address to reset your password:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onPasswordReset(email)
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter your email address",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text("Reset Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
