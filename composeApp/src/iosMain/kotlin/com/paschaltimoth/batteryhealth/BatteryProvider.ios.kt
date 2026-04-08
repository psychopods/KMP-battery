package com.paschaltimoth.batteryhealth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryStateCharging
import platform.UIKit.UIDeviceBatteryStateFull
import platform.UIKit.UIDeviceBatteryStateUnplugged

@Composable
actual fun rememberBatteryInfo(): State<BatteryInfo> {
    val batteryInfo = remember { mutableStateOf(BatteryInfo()) }

    LaunchedEffect(Unit) {
        UIDevice.currentDevice.batteryMonitoringEnabled = true
        while (true) {
            val level = (UIDevice.currentDevice.batteryLevel * 100).toInt()
            val state = UIDevice.currentDevice.batteryState
            val isCharging = state == UIDeviceBatteryStateCharging ||
                    state == UIDeviceBatteryStateFull
            batteryInfo.value = BatteryInfo(
                level = if (level < 0) 0 else level,
                isCharging = isCharging,
                chargingSource = when (state) {
                    UIDeviceBatteryStateCharging -> "Charging"
                    UIDeviceBatteryStateFull -> "Full"
                    UIDeviceBatteryStateUnplugged -> "Not Charging"
                    else -> "Unknown"
                },
                health = "N/A",
                technology = "Li-ion"
            )
            delay(5000)
        }
    }

    return batteryInfo
}
