package com.example.easyinventory.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easyinventory.R
import com.example.easyinventory.viewmodel.AuthViewModel

@Composable
fun DashboardScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    AppScaffold(
        navController = navController,
        title = "Dashboard",
        showBackButton = false
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            val appIcon: Painter = painterResource(id = R.mipmap.ic_launcher)

            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = appIcon,
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(40.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Button(
                    onClick = { navController.navigate("add_inventory") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.AddCircleOutline,
                            contentDescription = "Add Inventory",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            "Add Inventory",
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = { navController.navigate("view_inventory") },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.ViewInAr,
                            contentDescription = "View Inventory",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            "View Inventory",
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Report Button
                Button(
                    onClick = { navController.navigate("report") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.FileDownload,
                            contentDescription = "Report",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            "Report",
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        // Feature coming soon action
                    },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.QueryBuilder,
                            contentDescription = "Coming Soon",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            "Coming Soon",
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}