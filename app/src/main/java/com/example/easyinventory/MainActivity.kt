package com.example.easyinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.easyinventory.ui.*
import com.example.easyinventory.ui.theme.EasyInventoryTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyInventoryTheme {
                EasyInventoryApp()
            }
        }
    }
}

@Composable
fun EasyInventoryApp() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Track whether to show the exit confirmation dialog
    var showExitDialog by remember { mutableStateOf(false) }

    // Access the current context to finish the activity
    val context = LocalContext.current

    // Handle the back button press to show the exit confirmation dialog
    androidx.activity.compose.BackHandler {
        showExitDialog = true
    }

    // Display the exit confirmation dialog if triggered
    if (showExitDialog) {
        ExitAppDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                // Schedule the finish() call after the composition completes
                (context as ComponentActivity).finish()
            }
        )
    }

    Surface(color = Color.White) {
        NavHost(navController, startDestination = if (currentUser != null) "dashboard" else "login") {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("dashboard") { DashboardScreen(navController) }
            composable("add_inventory") { AddInventoryScreen(navController) }
            composable("view_inventory") { ViewInventoryScreen(navController) }
            composable("report") { ReportScreen(navController) }
            composable(
                "edit_inventory/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) {
                val itemId = it.arguments?.getString("itemId") ?: ""
                EditInventoryScreen(navController, itemId)
            }

            composable(
                "inventory_detail/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) {
                val itemId = it.arguments?.getString("itemId") ?: ""
                InventoryDetailScreen(navController, itemId)
            }
        }
    }
}

@Composable
fun ExitAppDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Exit EasyInventory") },
        text = { Text("Are you sure you want to exit?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Exit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}