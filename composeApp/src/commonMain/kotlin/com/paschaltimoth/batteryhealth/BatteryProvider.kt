package com.paschaltimoth.batteryhealth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
expect fun rememberBatteryInfo(): State<BatteryInfo>
