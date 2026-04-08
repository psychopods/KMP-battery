package com.paschaltimoth.batteryhealth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
actual fun rememberBatteryInfo(): State<BatteryInfo> {
    val context = LocalContext.current
    val batteryManager = remember {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }
    val batteryInfo = remember { mutableStateOf(BatteryInfo()) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

                val currentNow = batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
                ).let { if (it == Int.MIN_VALUE) 0 else it }

                val chargeCounter = batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER
                ).let { if (it == Int.MIN_VALUE) 0 else it }

                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val chargingSource = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> if (isCharging) "Unknown" else "Not Charging"
                }

                val healthStr = when (healthInt) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                    else -> "Unknown"
                }

                batteryInfo.value = BatteryInfo(
                    level = if (scale > 0) (level * 100) / scale else level,
                    voltage = voltage,
                    current = currentNow,
                    temperature = temperature,
                    isCharging = isCharging,
                    chargingSource = chargingSource,
                    health = healthStr,
                    technology = tech,
                    chargeCounter = chargeCounter
                )
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Poll current every 2s since it fluctuates faster than broadcast updates
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            val currentNow = batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
            ).let { if (it == Int.MIN_VALUE) 0 else it }
            val chargeCounter = batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER
            ).let { if (it == Int.MIN_VALUE) 0 else it }
            batteryInfo.value = batteryInfo.value.copy(
                current = currentNow,
                chargeCounter = chargeCounter
            )
        }
    }

    return batteryInfo
}
