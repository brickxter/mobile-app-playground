package com.brickxter.brickxtersmartersilo.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanResult

interface BluetoothListener {

    fun onScanResult(result: ScanResult)
    fun onScanningStateChanged(isScanning: Boolean)
    fun onDeviceConnected(device: BluetoothDevice)
    fun onDeviceDisconnected()
    fun onServicesDiscovered(services: List<BluetoothGattService>)
    fun requestPermissions()
    fun onCharacteristicRead(value: ByteArray)
}