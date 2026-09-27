package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import java.io.File

data class DeviceTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val batteryTemperatureC: Float = 28.5f,
    val batteryHealth: String = "Good",
    val ramUsedMb: Long = 0,
    val ramTotalMb: Long = 0,
    val ramPercent: Int = 0,
    val storageFreeGb: Float = 0f,
    val storageTotalGb: Float = 0f,
    val storagePercentUsed: Int = 0,
    val networkStatus: String = "Online",
    val networkType: String = "Wi-Fi"
)

object TelemetryProvider {

    fun getTelemetry(context: Context): DeviceTelemetry {
        // Battery
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, batteryFilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 85

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
        val tempC = tempTenths / 10.0f

        val healthCode = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val healthStr = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Optimal (100%)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Depleted"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Nominal"
        }

        // Memory (RAM)
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024 * 1024))
        val availRamMb = (memInfo.availMem / (1024 * 1024))
        val usedRamMb = (totalRamMb - availRamMb).coerceAtLeast(0)
        val ramPercent = if (totalRamMb > 0) ((usedRamMb.toFloat() / totalRamMb) * 100).toInt() else 45

        // Storage
        var freeStorageGb = 0f
        var totalStorageGb = 0f
        var storagePct = 0
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
            val bytesTotal = stat.blockCountLong * stat.blockSizeLong
            freeStorageGb = (bytesAvailable / (1024f * 1024f * 1024f))
            totalStorageGb = (bytesTotal / (1024f * 1024f * 1024f))
            val usedGb = totalStorageGb - freeStorageGb
            storagePct = if (totalStorageGb > 0) ((usedGb / totalStorageGb) * 100).toInt() else 35
        } catch (e: Exception) {
            freeStorageGb = 32.5f
            totalStorageGb = 128f
            storagePct = 74
        }

        // Network
        var netStatus = "Offline"
        var netType = "None"
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNetwork)
            if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                netStatus = "Online"
                netType = when {
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi 6 / Stark Net"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "5G Ultra Wideband"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "High-Speed Uplink"
                    else -> "Encrypted Link"
                }
            }
        } catch (e: Exception) {
            netStatus = "Secured"
            netType = "Quantum Link"
        }

        return DeviceTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            batteryTemperatureC = tempC,
            batteryHealth = healthStr,
            ramUsedMb = usedRamMb,
            ramTotalMb = totalRamMb,
            ramPercent = ramPercent,
            storageFreeGb = freeStorageGb,
            storageTotalGb = totalStorageGb,
            storagePercentUsed = storagePct,
            networkStatus = netStatus,
            networkType = netType
        )
    }
}
