package com.brickxter.brickxtersmartersilo.ui.elements

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Composable function for individual device items
@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(
    result: ScanResult,
    onConnectClick: (ScanResult) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                // Device name
                Text(
                    text = result.device.name ?: "Unknown Device",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = { onConnectClick(result) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Connect")
                }
            }
            // Detect device type
            Text(
                text = if(result.scanRecord?.serviceUuids.toString() == "[0cc40389-b2e5-47a5-8b05-895179b22ab0]"){
                    "Device: SmarterSilo"
                } else{
                    "Device: Unknown"
                },

                style = MaterialTheme.typography.bodyMedium
            )
            // Service UUIDs
            Text(
                text = "Service UUIDs: ${result.scanRecord?.serviceUuids}",
                style = MaterialTheme.typography.bodyMedium
            )
            // Device address
            Text(
                text = "Device Address: ${result.device.address}",
                style = MaterialTheme.typography.bodyMedium
            )
            // Signal strength
            Text(
                text = "Signal Strength: ${result.rssi} dBm",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}