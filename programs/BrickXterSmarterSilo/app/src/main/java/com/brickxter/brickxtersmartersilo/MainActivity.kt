package com.brickxter.brickxtersmartersilo

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import com.brickxter.brickxtersmartersilo.ble.BluetoothListener
import com.brickxter.brickxtersmartersilo.ble.BluetoothManager
import com.brickxter.brickxtersmartersilo.ui.elements.BLEScannerApp
import com.brickxter.brickxtersmartersilo.ui.elements.DeviceDetailsScreen
import android.bluetooth.le.ScanResult



class MainActivity : ComponentActivity(), BluetoothListener {
    private lateinit var bluetoothManager: BluetoothManager
    private val PERMISSION_REQUEST_CODE = 1
    private val scannedDevices = mutableStateListOf<ScanResult>()
    private var isScanning by mutableStateOf(false)
    private var connectedDevice by mutableStateOf<BluetoothDevice?>(null)
    private var services by mutableStateOf<List<BluetoothGattService>>(emptyList())
    private var readValue by mutableStateOf<ByteArray?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bluetoothManager = BluetoothManager(this, this)
        setContent {
            if(connectedDevice == null){
                BLEScannerApp(
                    devices = scannedDevices,
                    isScanning = isScanning,
                    onScanClick = { bluetoothManager.toggleScan() },
                    onConnectClick = { result -> bluetoothManager.connectToDevice(result.device) }
                )
            } else{
                DeviceDetailsScreen(
                    connectedDevice = connectedDevice!!,
                    services = services,
                    onBackClick = {bluetoothManager.disconnectDevice()},
                    onReadCharacteristic = { characteristic -> bluetoothManager.readCharacteristic(characteristic)},
                    readValue = readValue
                )
            }
        }
    }

    override fun onScanResult(result: ScanResult) {
        val existingDevice = scannedDevices.find { it.device.address == result.device.address }
        if (existingDevice == null) {
            scannedDevices.add(result)
        }
    }

    override fun onScanningStateChanged(isScanning: Boolean) {
        this.isScanning = isScanning
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        connectedDevice = device
    }

    override fun onDeviceDisconnected() {
        connectedDevice = null
        services = emptyList()
    }

    override fun onServicesDiscovered(services: List<BluetoothGattService>) {
        this.services = services
    }

    override fun onCharacteristicRead(value: ByteArray) {
        readValue = value
    }


    @SuppressLint("MissingPermission")
    override fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            getRequiredPermissions(),
            PERMISSION_REQUEST_CODE
        )
    }
    private fun getRequiredPermissions(): Array<String> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }
}