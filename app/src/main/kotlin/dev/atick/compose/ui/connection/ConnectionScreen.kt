package dev.atick.compose.ui.connection

import ai.atick.material.MaterialColor
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.animation.ValueAnimator
import android.bluetooth.BluetoothDevice
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.LottieAnimationView
import dev.atick.compose.R
import dev.atick.compose.ui.connection.data.ConnectionUiState
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource


@Composable
fun ConnectionScreen(
    onConnectClick: (String) -> Unit,
    onRescanClick: () -> Unit,
    viewModel: ConnectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Remember selected device address
    var selectedDeviceAddress by remember { mutableStateOf<String?>(null) }

    // Check BLUETOOTH_CONNECT permission (required for device.name on Android 12+)
    val hasBluetoothConnectPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Connect",
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colors.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            modifier = Modifier.width(200.dp),
            painter = painterResource(id = R.drawable.movesense_logo),
            contentDescription = "Movesense Logo"
        )

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            // Removed white circle here as requested

            Image(
                modifier = Modifier.size(80.dp),
                painter = painterResource(id = R.drawable.ic_movesense),
                contentDescription = "Movesense Icon",
                contentScale = ContentScale.FillBounds
            )

            if (uiState.scanning) {
                AndroidView(
                    factory = { ctx ->
                        LottieAnimationView(ctx).apply {
                            setAnimation(R.raw.circular_lines)
                            repeatCount = ValueAnimator.INFINITE
                            playAnimation()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                uiState.scanning -> "Scanning..."
                uiState.devices.isEmpty() -> "No devices found"
                else -> "Select a device"
            },
            color = MaterialTheme.colors.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // List of discovered devices
        if (uiState.devices.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(horizontal = 16.dp)
            ) {
                items(uiState.devices) { device ->
                    val isSelected = device.address == selectedDeviceAddress
                    val backgroundColor =
                        if (isSelected) MaterialColor.Green50 else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(backgroundColor, shape = CircleShape)
                            .clickable {
                                selectedDeviceAddress = device.address
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasBluetoothConnectPermission) device.name ?: "Unknown device" else "Unknown device",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colors.onBackground,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                        Text(
                            text = device.address,
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(visible = !uiState.scanning) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        onRescanClick()
                        selectedDeviceAddress = null
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan Again"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rescan")
                }

                Button(
                    enabled = selectedDeviceAddress != null,
                    onClick = {
                        selectedDeviceAddress?.let { onConnectClick(it) }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Connect"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect")
                }
            }
        }
    }
}
