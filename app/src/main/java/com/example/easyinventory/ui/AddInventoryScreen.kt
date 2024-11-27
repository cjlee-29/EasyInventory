package com.example.easyinventory.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.easyinventory.model.Inventory
import com.example.easyinventory.ui.theme.GreenButtonColor
import com.example.easyinventory.viewmodel.InventoryViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AddInventoryScreen(navController: NavController, viewModel: InventoryViewModel = viewModel()) {
    AppScaffold(
        navController = navController,
        title = "Add Inventory",
        showBackButton = false
    ) { paddingValues ->
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var name by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
        var isUploading by remember { mutableStateOf(false) }
        var showDialog by remember { mutableStateOf(false) }

        val storageReference = FirebaseStorage.getInstance().reference

        // Function to create a URI for the captured image
        fun createImageUri(): Uri? {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        }

        // Image picker launcher
        val imagePickerLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            selectedImageUri = uri
            capturedImageUri = null // Reset captured image if gallery image is selected
        }

        // Camera capture launcher
        val cameraLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success: Boolean ->
            if (success) {
                Toast.makeText(context, "Image captured successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }

        // Permission request launchers
        val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                capturedImageUri = createImageUri()
                capturedImageUri?.let { uri -> cameraLauncher.launch(uri) }
            } else {
                Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }

        val requestStoragePermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                imagePickerLauncher.launch("image/*")
            } else {
                Toast.makeText(context, "Storage permission is required.", Toast.LENGTH_SHORT).show()
            }
        }

        // Use a Box to position the button at the bottom center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Button to open dialog for selecting or capturing image
                Button(onClick = { showDialog = true }) {
                    Text("Select or Capture Image")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display the selected or captured image
                when {
                    capturedImageUri != null -> {
                        AsyncImage(
                            model = capturedImageUri,
                            contentDescription = "Captured Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                    selectedImageUri != null -> {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Place "Add Inventory" button at the bottom center
            Button(
                onClick = {
                    if (name.isEmpty() || quantity.isEmpty() || price.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Please fill all required fields",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    isUploading = true
                    val quantityInt = quantity.toIntOrNull()
                    val priceDouble = price.toDoubleOrNull()
                    if (quantityInt == null || priceDouble == null || quantityInt < 0 || priceDouble < 0.0) {
                        Toast.makeText(
                            context,
                            "Please enter valid numbers",
                            Toast.LENGTH_SHORT
                        ).show()
                        isUploading = false
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            val item: Inventory
                            if (selectedImageUri != null || capturedImageUri != null) {
                                val selectedUri = selectedImageUri ?: capturedImageUri
                                val inputStream = context.contentResolver.openInputStream(selectedUri!!)
                                val imageData = inputStream?.readBytes()
                                inputStream?.close()

                                if (imageData != null) {
                                    val imageRef =
                                        storageReference.child("images/${System.currentTimeMillis()}.jpg")
                                    imageRef.putBytes(imageData).await()
                                    val uri = imageRef.downloadUrl.await()

                                    item = Inventory(
                                        id = "",
                                        name = name,
                                        quantity = quantityInt,
                                        price = priceDouble,
                                        photo = uri.toString(),
                                        userId = ""
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to read image data",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    isUploading = false
                                    return@launch
                                }
                            } else {
                                item = Inventory(
                                    id = "",
                                    name = name,
                                    quantity = quantityInt,
                                    price = priceDouble,
                                    photo = "",
                                    userId = ""
                                )
                            }

                            val success = viewModel.addInventoryItem(item)
                            if (success) {
                                Toast.makeText(context, "Item Added", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Failed to add item", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to add item: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                Text("Add Inventory")
            }

            // Show the photo options dialog
            if (showDialog) {
                showPhotoOptionsDialog(
                    onSelectImage = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                            requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        } else {
                            imagePickerLauncher.launch("image/*")
                        }
                        showDialog = false
                    },
                    onCaptureImage = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            capturedImageUri = createImageUri()
                            capturedImageUri?.let { uri -> cameraLauncher.launch(uri) }
                        }
                        showDialog = false
                    },
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}