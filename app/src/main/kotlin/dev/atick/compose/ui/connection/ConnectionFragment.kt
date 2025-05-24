package dev.atick.compose.ui.connection

import dev.atick.compose.ui.connection.ConnectionScreen
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.atick.compose.base.BaseLifecycleService
import dev.atick.compose.service.CardiacZoneService
import dev.atick.compose.ui.theme.ComposeTheme
import dev.atick.core.ui.BaseComposeFragment
import dev.atick.core.utils.extensions.collectWithLifecycle
import dev.atick.core.utils.extensions.showToast
import dev.atick.movesense.data.ConnectionState
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

@AndroidEntryPoint
class ConnectionFragment : BaseComposeFragment() {

    private val viewModel: ConnectionViewModel by viewModels()

    @Composable
    override fun ComposeUi() {
        ComposeTheme {
            ConnectionScreen(
                onConnectClick = ::startMovesenseService,
                onRescanClick = ::startScanIfPermitted
            )
        }
    }

    override fun observeStates() {
        // Fix type inference by explicitly specifying the type
        collectWithLifecycle(viewModel.connectionStatus) { status: ConnectionState ->
            requireContext().showToast(getString(status.description))
            when (status) {
                ConnectionState.CONNECTING -> {
                    // Handle connecting state
                }
                ConnectionState.CONNECTED -> {
                    navigateToDashboardFragment()
                }
                ConnectionState.CONNECTION_FAILED -> {
                    // Handle connection failed state
                }
                ConnectionState.DISCONNECTED -> {
                    // Handle disconnected state
                }
                ConnectionState.NOT_CONNECTED -> {
                    // Handle not connected state
                }
            }
        }
    }
    private fun startScanIfPermitted() {
        val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val notGranted = requiredPermissions.any {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted) {
            requestPermissions(requiredPermissions, 101)
        } else {
            viewModel.scan(requireContext())

        }
    }

    private fun startMovesenseService(address: String) {
        val intent = Intent(requireContext(), CardiacZoneService::class.java)
            .apply {
                action = BaseLifecycleService.ACTION_START_SERVICE
                putExtra(CardiacZoneService.BT_DEVICE_ADDRESS_KEY, address)
            }
        requireContext().startForegroundService(intent)
    }

    private fun navigateToDashboardFragment() {
        findNavController().navigate(
            ConnectionFragmentDirections.actionConnectionFragmentToDashboardFragment()
        )
    }
}