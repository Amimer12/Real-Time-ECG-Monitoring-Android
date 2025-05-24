package dev.atick.compose.ui.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atick.compose.ui.connection.data.ConnectionUiState
import dev.atick.movesense.data.ConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor() : ViewModel() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> get() = _uiState

    private val _connectionStatus = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionState> = _connectionStatus.asStateFlow()

    private val scanResults = mutableMapOf<String, BluetoothDevice>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.address !in scanResults) {
                scanResults[device.address] = device
                _uiState.update { currentState ->
                    currentState.copy(devices = scanResults.values.toList())
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _uiState.update { currentState ->
                currentState.copy(scanning = false)
            }
        }
    }

    /**
     * Start Bluetooth LE scan with permission check.
     * Context is required to check runtime permissions.
     */
    fun scan(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startScanWithTimeout()
            }
        } else {
            startScanWithTimeout()
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startScanWithTimeout() {
        scanResults.clear()
        _uiState.update { it.copy(scanning = true, devices = emptyList()) }
        scanner?.startScan(scanCallback)

        // Stop scan after 5 seconds using coroutine
        viewModelScope.launch {
            delay(5000)
            stopScan()
        }
    }


    /**
     * Stops ongoing Bluetooth LE scan safely.
     */
    fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (securityException: SecurityException) {
            securityException.printStackTrace()
        }
        _uiState.update { currentState -> currentState.copy(scanning = false) }
    }

    fun setConnecting() {
        _connectionStatus.update { ConnectionState.CONNECTING }
    }

    fun setConnected() {
        _connectionStatus.update { ConnectionState.CONNECTED }
        stopScan()
    }

    fun setConnectionFailed() {
        _connectionStatus.update { ConnectionState.CONNECTION_FAILED }
    }

    fun setDisconnected() {
        _connectionStatus.update { ConnectionState.DISCONNECTED }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}