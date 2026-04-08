package com.paschaltimoth.batteryhealth

import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.round

data class BatteryInfo(
    val level: Int = 0,
    val voltage: Int = 0,
    val current: Int = 0,
    val temperature: Int = 0,
    val isCharging: Boolean = false,
    val chargingSource: String = "Unknown",
    val health: String = "Unknown",
    val technology: String = "Unknown",
    val chargeCounter: Int = 0
) {
    /** Voltage in Volts (from millivolts) */
    val voltageVolts: Double get() = voltage / 1000.0

    /** Current in Amps (from microamps) */
    val currentAmps: Double get() = current / 1_000_000.0

    /** Power in Watts: P = V × I */
    val powerWatts: Double get() = voltageVolts * currentAmps

    /** Temperature in °C (from tenths of °C) */
    val temperatureCelsius: Double get() = temperature / 10.0
}

fun Double.toFixed(decimals: Int): String {
    if (decimals <= 0) return round(this).toLong().toString()
    val factor = 10.0.pow(decimals)
    val rounded = round(this.absoluteValue * factor) / factor
    val sign = if (this < 0) "-" else ""
    val str = rounded.toString()
    val dot = str.indexOf('.')
    return if (dot < 0) {
        "$sign$str.${"0".repeat(decimals)}"
    } else {
        val dec = str.substring(dot + 1)
        if (dec.length >= decimals) "$sign${str.substring(0, dot + 1 + decimals)}"
        else "$sign$str${"0".repeat(decimals - dec.length)}"
    }
}
