package com.example.easyinventory.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.easyinventory.model.Inventory
import com.example.easyinventory.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

@Composable
fun InventoryDetailScreen(
    navController: NavController,
    itemId: String,
    viewModel: InventoryViewModel = viewModel()
) {
    AppScaffold(
        navController = navController,
        title = "Inventory Detail",
        showBackButton = false
    ) { paddingValues ->
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var item by remember { mutableStateOf<Inventory?>(null) }

        // State to manage the visibility of the confirmation dialog
        var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

        // State to manage the visibility of the fullscreen image dialog
        var showImageFullscreenDialog by remember { mutableStateOf(false) }

        LaunchedEffect(itemId) {
            item = viewModel.getInventoryItemById(itemId)
        }

        if (item != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = item!!.name, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Fixed image with click event to show fullscreen
                    if (item!!.photo.isNotEmpty()) {
                        AsyncImage(
                            model = item!!.photo,
                            contentDescription = "Inventory Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(16.dp)
                                .clickable {
                                    showImageFullscreenDialog =
                                        true  // Show fullscreen image dialog when clicked
                                },
                            alignment = Alignment.Center
                        )
                    } else {
                        Text("No image available", modifier = Modifier.padding(16.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Inventory Details in Card UI for better presentation
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Quantity UI
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Quantity:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${item!!.quantity}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Price UI
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Price:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "\$${item!!.price}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // Place buttons at the bottom of the screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            // Navigate to EditInventoryScreen
                            navController.navigate("edit_inventory/${item!!.id}")
                        },
                        modifier = Modifier.weight(1f).padding(8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Edit")
                    }

                    Button(
                        onClick = {
                            showDeleteConfirmationDialog = true  // Show the confirmation dialog
                        },
                        modifier = Modifier.weight(1f).padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }

            // Show the confirmation dialog if showDeleteConfirmationDialog is true
            if (showDeleteConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    title = { Text(text = "Confirm Deletion") },
                    text = { Text("Are you sure you want to delete this item?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val success = viewModel.deleteInventoryItem(item!!.id)
                                    if (success) {
                                        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT)
                                            .show()
                                        navController.popBackStack()  // Go back after deletion
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to delete item",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    showDeleteConfirmationDialog =
                                        false  // Close the dialog after deletion
                                }
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Show the fullscreen image dialog if showImageFullscreenDialog is true
            if (showImageFullscreenDialog) {
                fullScreenImageViewer(
                    imageUrl = item!!.photo,
                    onDismiss = { showImageFullscreenDialog = false })
            }
        } else {
            // If item is null, show loading or error message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Item not found or you don't have access to this item.")
            }
        }
    }
}