package dev.atick.compose.ui.connection.data

import android.bluetooth.BluetoothDevice

data class ConnectionUiState(
    val scanning: Boolean = false,
    val devices: List<BluetoothDevice> = emptyList()
)