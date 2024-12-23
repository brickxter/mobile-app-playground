package com.brickxter.brickxtersmartersilo.ui.elements

import android.bluetooth.le.ScanResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


// Composable function for the main UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BLEScannerApp(
    devices: List<ScanResult>,
    isScanning: Boolean,
    onScanClick: () -> Unit,
    onConnectClick: (ScanResult) -> Unit  // Add this parameter
) {
    // Material3 theme wrapper
    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("BLE Scanner") }
                )
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues) // Account for the TopAppBar's padding
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp) // Reapply side padding
                    ) {
                        // List of discovered devices
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 72.dp) // Reserve space for the button
                        ) {
                            items(devices) { result ->
                                DeviceItem(
                                    result = result,
                                    onConnectClick = onConnectClick  // Pass to DeviceItem
                                )
                            }
                        }

                        // Scan button fixed at the bottom
                        Button(
                            onClick = onScanClick,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(if (isScanning) "Stop Scan" else "Start Scan")
                        }
                    }
                }
            }
        )
    }
}