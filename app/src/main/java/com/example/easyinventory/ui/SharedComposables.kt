package com.example.easyinventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easyinventory.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.platform.LocalDensity

@Composable
fun showPhotoOptionsDialog(
    onSelectImage: () -> Unit,
    onCaptureImage: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose an option") },
        text = {
            Column {
                TextButton(onClick = onSelectImage) {
                    Text("Select from Gallery")
                }
                TextButton(onClick = onCaptureImage) {
                    Text("Capture with Camera")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun fullScreenImageViewer(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() }) // Dismiss on tap
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full-Screen Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    title: String,
    showBackButton: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    // State for controlling the drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Get the screen width and convert it to dp using LocalDensity
    val screenWidthPx = LocalContext.current.resources.displayMetrics.widthPixels
    val density = LocalDensity.current
    val drawerWidthDp = with(density) { (screenWidthPx * 0.7f).toDp() } // 70% of screen width in dp

    ModalNavigationDrawer(
        drawerContent = {
            // Set the drawer width to 70% of the screen width
            DrawerContent(
                navController = navController,
                drawerState = drawerState,
                scope = scope,
                modifier = Modifier.width(drawerWidthDp)
            )
        },
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.5f)  // Dim background when drawer is open
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, color = Color.White) },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        } else {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open() // Open the drawer when the menu button is clicked
                                }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            },
            content = { paddingValues ->
                // Pass the paddingValues to the content to handle internal padding properly
                Box(modifier = Modifier.padding(paddingValues)) {
                    content(paddingValues)
                }
            }
        )
    }
}

@Composable
fun DrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier // Add the modifier parameter
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Drawer Header
        Text(
            text = "EasyInventory",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        // Navigation options
        DrawerItem(
            icon = Icons.Filled.Dashboard,
            label = "Dashboard",
            onClick = {
                scope.launch { drawerState.close() }  // Close the drawer when clicked
                navController.navigate("dashboard")
            }
        )

        DrawerItem(
            icon = Icons.Filled.List,
            label = "View Inventory",
            onClick = {
                scope.launch { drawerState.close() }  // Close the drawer when clicked
                navController.navigate("view_inventory")
            }
        )

        DrawerItem(
            icon = Icons.Filled.Download,
            label = "Download Report",
            onClick = {
                scope.launch { drawerState.close() }  // Close the drawer when clicked
                navController.navigate("report")
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))

        // Logout action
        TextButton(
            onClick = {
                scope.launch { drawerState.close() }  // Close the drawer before logging out
                viewModel.logout()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }  // Remove all destinations from backstack
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun DrawerItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick() // Call the passed navigation function
            }
            .padding(12.dp)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}