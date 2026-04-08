package com.paschaltimoth.batteryhealth

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform