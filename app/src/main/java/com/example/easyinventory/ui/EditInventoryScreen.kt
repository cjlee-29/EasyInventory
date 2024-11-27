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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.easyinventory.model.Inventory
import com.example.easyinventory.viewmodel.InventoryViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun EditInventoryScreen(
    navController: NavController,
    itemId: String,
    viewModel: InventoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var item by remember { mutableStateOf<Inventory?>(null) }
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var initialPhotoUrl by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }  // State to show the dialog

    val storageReference = FirebaseStorage.getInstance().reference

    // Load the inventory item when screen is opened
    LaunchedEffect(itemId) {
        item = viewModel.getInventoryItemById(itemId)
        item?.let {
            name = it.name
            quantity = it.quantity.toString()
            price = it.price.toString()
            initialPhotoUrl = it.photo
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
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

    // Create a content resolver for the camera image
    fun createImageUri(): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    // Permission request launchers
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            capturedImageUri = createImageUri()
            capturedImageUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Edit Inventory", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Text field for inventory name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Text field for inventory quantity
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Text field for inventory price
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Button to open the dialog for selecting/capturing image
        Button(onClick = { showDialog = true }) {
            Text("Select or Capture Image")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Display the current or newly selected image
        if (capturedImageUri != null) {
            // Display the captured image
            AsyncImage(
                model = capturedImageUri,
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        } else if (photoUri != null) {
            // Display the selected image
            AsyncImage(
                model = photoUri,
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        } else if (initialPhotoUrl.isNotEmpty()) {
            // Display the current image from Firebase
            AsyncImage(
                model = initialPhotoUrl,
                contentDescription = "Current Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save button to update the inventory
        Button(
            onClick = {
                if (name.isEmpty() || quantity.isEmpty() || price.isEmpty()) {
                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val quantityInt = quantity.toIntOrNull()
                if (quantityInt == null || quantityInt < 0) {
                    Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val priceDouble = price.toDoubleOrNull()
                if (priceDouble == null || priceDouble < 0.00) {
                    Toast.makeText(context, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isUploading = true

                coroutineScope.launch {
                    try {
                        var updatedPhotoUrl = initialPhotoUrl
                        var isNewImageSelected = false

                        // If a new image was selected or captured, upload it to Firebase Storage
                        if (photoUri != null || capturedImageUri != null) {
                            val selectedUri = photoUri ?: capturedImageUri
                            val storageReference = FirebaseStorage.getInstance().reference
                            val imageRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")

                            val inputStream = context.contentResolver.openInputStream(selectedUri!!)
                            val imageData = inputStream?.readBytes()
                            inputStream?.close()

                            if (imageData != null) {
                                imageRef.putBytes(imageData).await()
                                updatedPhotoUrl = imageRef.downloadUrl.await().toString()
                                isNewImageSelected = true
                            }
                        }

                        // If the photo was updated, delete the old one from Firebase Storage
                        if (isNewImageSelected && initialPhotoUrl.isNotEmpty()) {
                            val oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(initialPhotoUrl)
                            oldImageRef.delete().await()  // Delete the old image
                        }

                        // Create the updated inventory object
                        val updatedItem = item!!.copy(
                            name = name,
                            quantity = quantityInt,
                            price = priceDouble,
                            photo = updatedPhotoUrl
                        )

                        // Update the inventory in Firestore
                        val success = viewModel.updateInventoryItem(updatedItem)
                        if (success) {
                            Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()  // Go back after updating
                        } else {
                            Toast.makeText(context, "Failed to update item", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isUploading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }

        // Show a progress indicator while uploading
        if (isUploading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
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
                        capturedImageUri?.let { uri ->
                            cameraLauncher.launch(uri)
                        }
                    }
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}