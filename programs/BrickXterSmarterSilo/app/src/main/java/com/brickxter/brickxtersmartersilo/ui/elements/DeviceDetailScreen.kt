package com.brickxter.brickxtersmartersilo.ui.elements

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun DeviceDetailsScreen(
    connectedDevice: BluetoothDevice,
    services: List<BluetoothGattService>,
    onBackClick: () -> Unit,
    onReadCharacteristic: (BluetoothGattCharacteristic) -> Unit,
    readValue: ByteArray?
) {
    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Device Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    //Device name
                    Text(
                        text = connectedDevice.name ?: "Unknown Device",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    // Device address
                    Text(
                        text = "Address: ${connectedDevice.address}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.padding(8.dp))

                    readValue?.let{
                        Text(
                            text = "Notification Value: ${it.joinToString("") { "%02x".format(it) }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                    }

                    Text(
                        text = "Services:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.padding(4.dp))

                    // Display services and their characteristics
                    LazyColumn {
                        items(services) { service ->
                            ServiceItem(
                                service = service,
                                onReadCharacteristic = onReadCharacteristic
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ServiceItem(
    service: BluetoothGattService,
    onReadCharacteristic: (BluetoothGattCharacteristic) -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)){
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {

            Text(
                text = when(service.uuid.toString()){
                    "0cc40389-b2e5-47a5-8b05-895179b22ab0" -> "Custom BrickXter Service"
                    "6e400001-b5a3-f393-e0a9-e50e24dcca9e" -> "Nordic UART Service"
                    else -> "Unknown Service"
                },
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.padding(4.dp))

            Text(
                text = "Service UUID: ${service.uuid}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Characteristics:",
                style = MaterialTheme.typography.bodyMedium
            )

            service.characteristics.forEach{
                CharacteristicItem(
                    characteristic = it,
                    onReadCharacteristic = onReadCharacteristic
                )
            }
        }
    }
}

@Composable
fun CharacteristicItem(
    characteristic: BluetoothGattCharacteristic,
    onReadCharacteristic: (BluetoothGattCharacteristic) -> Unit
) {
    Column {
        Text(
            text = "UUID: ${characteristic.uuid}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Properties: ${characteristic.properties}",
            style = MaterialTheme.typography.bodySmall
        )
        // Read button (only enable if the characteristic has read property)
        if(characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0){
            Button(
                onClick = { onReadCharacteristic(characteristic) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Read")
            }
        }
    }
}