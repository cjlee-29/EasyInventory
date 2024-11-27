package com.example.easyinventory.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyinventory.viewmodel.InventoryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easyinventory.model.Inventory
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@Composable
fun ViewInventoryScreen(navController: NavController, viewModel: InventoryViewModel = viewModel()) {

    AppScaffold(navController = navController, title = "View Inventory", showBackButton = false) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp) // Padding from the scaffold
        ) {
            val inventoryItems by viewModel.inventoryItems.collectAsState()
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            var searchQuery by remember { mutableStateOf("") }
            var sortAttribute by remember { mutableStateOf("Name") } // Default sort by "Name"
            var sortOrder by remember { mutableStateOf("Ascending") } // Default sort order is "Ascending"

            LaunchedEffect(Unit) {
                viewModel.loadInventoryItems()
            }

            // Filtered and sorted inventory list
            val filteredItems = inventoryItems
                .filter { it.name.contains(searchQuery, ignoreCase = true) }
                .sortedWith(
                    when (sortAttribute) {
                        "Name" -> if (sortOrder == "Ascending") compareBy { it.name } else compareByDescending { it.name }
                        "Quantity" -> if (sortOrder == "Ascending") compareBy { it.quantity } else compareByDescending { it.quantity }
                        "Price" -> if (sortOrder == "Ascending") compareBy { it.price } else compareByDescending { it.price }
                        else -> compareBy { it.name }
                    }
                )

            Column(modifier = Modifier.fillMaxSize()) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Inventory") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 0.dp) // Remove any top padding to minimize space
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sort Attribute and Order Dropdowns in a Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, // Space between buttons
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sort Attribute Dropdown
                    var attributeExpanded by remember { mutableStateOf(false) }
                    Button(
                        onClick = { attributeExpanded = true },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(text = "Sort by: $sortAttribute", fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = attributeExpanded,
                        onDismissRequest = { attributeExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Name") },
                            onClick = {
                                sortAttribute = "Name"
                                attributeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Quantity") },
                            onClick = {
                                sortAttribute = "Quantity"
                                attributeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Price") },
                            onClick = {
                                sortAttribute = "Price"
                                attributeExpanded = false
                            }
                        )
                    }

                    // Sort Order Dropdown (Ascending/Descending)
                    var orderExpanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(Alignment.End) // Align the button to the right
                    ) {
                        Button(
                            onClick = { orderExpanded = true },
                        ) {
                            Text(text = sortOrder, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(Icons.Filled.Sort, contentDescription = "Order")
                        }

                        DropdownMenu(
                            expanded = orderExpanded,
                            onDismissRequest = { orderExpanded = false },
                            modifier = Modifier.wrapContentWidth(Alignment.End) // Aligns dropdown to the right
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ascending") },
                                onClick = {
                                    sortOrder = "Ascending"
                                    orderExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Descending") },
                                onClick = {
                                    sortOrder = "Descending"
                                    orderExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Inventory List
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredItems) { item ->
                        InventoryItemRow(
                            item = item,
                            onClick = {
                                // Navigate to InventoryDetailScreen when clicked
                                navController.navigate("inventory_detail/${item.id}")
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun InventoryItemRow(
    item: Inventory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.photo.isNotEmpty()) {
                AsyncImage(
                    model = item.photo,
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = "Quantity: ${item.quantity}")
                Text(text = "Price: \$${item.price}")
            }
        }
    }
}