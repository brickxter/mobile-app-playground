package com.brickxter.brickxtersmartersilo.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class BluetoothManager(
    private val context: Context,
    private val bluetoothListener: BluetoothListener
) {

    private var bluetoothAdapter: BluetoothAdapter
    private var isScanning = false
    private var connectedDevice: BluetoothDevice? = null
    private var services: List<BluetoothGattService> = emptyList()
    private val TARGET_SERVICE_UUID = UUID.fromString("0cc40389-b2e5-47a5-8b05-895179b22ab0")

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            bluetoothListener.onScanResult(result)
        }
    }

    fun toggleScan() {
        if (isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (hasRequiredPermissions()) {
            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(TARGET_SERVICE_UUID))
                .build()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            CoroutineScope(Dispatchers.IO).launch{
                bluetoothAdapter.bluetoothLeScanner?.startScan(
                    listOf(filter),
                    settings,
                    scanCallback
                )
                withContext(Dispatchers.Main){
                    isScanning = true
                    bluetoothListener.onScanningStateChanged(isScanning)
                }
            }

        }else{
            bluetoothListener.requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        CoroutineScope(Dispatchers.IO).launch{
            bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
            withContext(Dispatchers.Main){
                isScanning = false
                bluetoothListener.onScanningStateChanged(isScanning)
            }
        }

    }

    private fun hasRequiredPermissions(): Boolean {
        val permissions = getRequiredPermissions()
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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


    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = gatt.device
                    gatt.discoverServices()
                    bluetoothListener.onDeviceConnected(gatt.device)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    gatt.close()
                    connectedDevice = null
                    services = emptyList()
                    bluetoothListener.onDeviceDisconnected()
                }
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                services = gatt.services
                bluetoothListener.onServicesDiscovered(gatt.services)
                // Enable notifications for all characteristics with notify or indicate property
                enableNotifications(gatt)
            }
        }
        // Need to use deprecated function, otherwise it will not work on Android 12 and before
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                handleCharacteristicResponse(characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            handleCharacteristicResponse(characteristic)
        }

        private fun handleCharacteristicResponse(characteristic: BluetoothGattCharacteristic) {
            val value = characteristic.value
            if (value != null) {
                bluetoothListener.onCharacteristicRead(value)
                println("Characteristic read value: ${value.joinToString("") { "%02x".format(it) }}")
            } else {
                println("Value is null")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice() {
        connectedDevice?.let{
            it.connectGatt(context, false, gattCallback).disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt) {
        services.forEach { service ->
            service.characteristics.forEach { characteristic ->
                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 ||
                    characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0
                ) {
                    gatt.setCharacteristicNotification(characteristic, true)

                    val descriptor = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                    )
                    descriptor?.let {
                        it.value = if(characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        } else {
                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        }
                        gatt.writeDescriptor(it)
                    }
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        connectedDevice?.let { device ->
            CoroutineScope(Dispatchers.IO).launch {
                device.connectGatt(context, false, gattCallback).readCharacteristic(characteristic)
            }
        }
    }
}