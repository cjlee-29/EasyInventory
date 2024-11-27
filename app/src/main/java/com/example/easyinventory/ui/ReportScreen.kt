package com.example.easyinventory.ui

import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easyinventory.model.Inventory
import com.example.easyinventory.viewmodel.InventoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.easyinventory.R
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.easyinventory.ui.theme.GreenButtonColor
import com.example.easyinventory.viewmodel.AuthViewModel

@Composable
fun ReportScreen(navController: NavController, viewModel: InventoryViewModel = viewModel(), authViewModel: AuthViewModel = viewModel()) {
    AppScaffold(
        navController = navController,
        title = "Report",
        showBackButton = false
    ) { paddingValues ->

        val context = LocalContext.current

        // Retrieve the username from the AuthViewModel
        val username by authViewModel.username.collectAsState()

        // Call to load inventory items for the current user
        LaunchedEffect(Unit) {
            viewModel.loadInventoryItems()
        }

        // State to hold the total count and total price of all items
        var totalItems by remember { mutableStateOf(0) }
        var totalPrice by remember { mutableStateOf(0.0) }

        // Collect the list of inventory items from ViewModel
        val inventoryItems by viewModel.inventoryItems.collectAsState()

        // Calculate the total quantity and price of all inventory items
        LaunchedEffect(inventoryItems) {
            totalItems = inventoryItems.sumOf { it.quantity }
            totalPrice = inventoryItems.sumOf { it.price * it.quantity }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total Items: $totalItems",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total Price: \$${String.format("%.2f", totalPrice)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (inventoryItems.isEmpty()) {
                    item {
                        Text("No inventory items found.")
                    }
                } else {
                    items(inventoryItems) { item ->
                        InventoryItemReportRow(item = item)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to generate PDF report
            Button(
                onClick = {
                    // Generate and download PDF report with username
                    generatePdfReport(context, inventoryItems, totalItems, totalPrice, username ?: "Unknown User")
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Download PDF Report")
            }
        }
    }
}

@Composable
fun InventoryItemReportRow(item: Inventory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically // Aligns the text and image
        ) {
            // Inventory information on the left side
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Item: ${item.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Quantity: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Price: \$${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: \$${String.format("%.2f", item.price * item.quantity)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Spacer between text and image
            Spacer(modifier = Modifier.width(16.dp))

            // Image of the inventory item
            if (item.photo.isNotEmpty()) {
                AsyncImage(
                    model = item.photo,
                    contentDescription = "Inventory Image",
                    modifier = Modifier
                        .size(64.dp) // Adjust the size of the image
                        .aspectRatio(1f) // Ensures the image has a square shape
                )
            } else {
                Text(
                    text = "No Image",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

// Function to generate PDF report
fun generatePdfReport(
    context: Context,
    inventoryItems: List<Inventory>,
    totalItems: Int,
    totalPrice: Double,
    username: String // Pass the username here
) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()

    // Log the username to ensure it's correctly retrieved
    android.util.Log.d("PDF Report", "Username: $username")

    // Application Icon: Load the icon and resize it
    val originalLogoBitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher) // App's icon
    val resizedLogoBitmap = Bitmap.createScaledBitmap(originalLogoBitmap, 50, 50, true) // Resize to 50x50

    val iconWidth = resizedLogoBitmap.width
    val iconHeight = resizedLogoBitmap.height
    val pageWidth = pageInfo.pageWidth

    // Draw the resized icon at the top-right corner
    canvas.drawBitmap(resizedLogoBitmap, (pageWidth - iconWidth - 20f), 20f, paint)

    // Title: Start content below the icon
    paint.textSize = 24f
    canvas.drawText("Inventory Report", 20f, (iconHeight + 40f), paint)  // Adjusted to avoid overlap

    // Date and Time
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val currentDateAndTime = dateFormat.format(Date())
    paint.textSize = 14f
    canvas.drawText("Date: $currentDateAndTime", 20f, (iconHeight + 70f), paint)

    // Username
    canvas.drawText("Generated by: $username", 20f, (iconHeight + 90f), paint)

    // Column headers: Reserve more space below the icon and the previous content
    paint.textSize = 16f
    canvas.drawText("Item Name", 20f, (iconHeight + 140f), paint)
    canvas.drawText("Quantity", 200f, (iconHeight + 140f), paint)
    canvas.drawText("Price", 350f, (iconHeight + 140f), paint)
    canvas.drawText("Total", 450f, (iconHeight + 140f), paint)

    // Inventory Items (formatted into a table)
    var yPosition = (iconHeight + 180f)  // Start items further down to avoid the icon area
    for (item in inventoryItems) {
        canvas.drawText(item.name, 20f, yPosition, paint)
        canvas.drawText("${item.quantity}", 200f, yPosition, paint)
        canvas.drawText("\$${String.format("%.2f", item.price)}", 350f, yPosition, paint)
        canvas.drawText("\$${String.format("%.2f", item.price * item.quantity)}", 450f, yPosition, paint)
        yPosition += 40f
    }

    // Total items and total price at the bottom-right
    paint.textSize = 16f
    canvas.drawText("Total Items: $totalItems", 400f, 800f, paint)
    canvas.drawText("Total Price: \$${String.format("%.2f", totalPrice)}", 400f, 820f, paint)

    pdfDocument.finishPage(page)

    // Save the PDF to external storage
    val fileName = "EasyInventory_${System.currentTimeMillis()}.pdf"
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        fileName
    )
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF saved to Downloads folder", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }

    pdfDocument.close()
}