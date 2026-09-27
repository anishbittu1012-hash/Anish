package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings

object SystemController {

    fun getInstalledApps(context: Context): List<InstalledApp> {
        return AppLauncherManager.getInstalledApplications(context)
    }

    fun launchAppByPackage(context: Context, packageName: String): Boolean {
        return AppLauncherManager.launchPackage(context, packageName)
    }

    fun findAndLaunchAppByName(context: Context, rawQuery: String): Pair<Boolean, String> {
        val apps = getInstalledApps(context)
        return when (val res = AppLauncherManager.resolveAndLaunch(context, apps, rawQuery)) {
            is AppLaunchResult.Success -> Pair(true, res.appName)
            is AppLaunchResult.StoreOffer -> {
                try {
                    context.startActivity(res.storeIntent)
                } catch (e: Exception) {
                    // Ignore
                }
                Pair(true, res.queryName)
            }
            is AppLaunchResult.Failed -> Pair(false, res.appName)
            is AppLaunchResult.NotAnAppCommand -> Pair(false, "")
        }
    }

    // Settings & System Shortcuts
    fun openWifiSettings(context: Context): Boolean {
        return startSystemActivity(context, Settings.ACTION_WIFI_SETTINGS)
    }

    fun openBluetoothSettings(context: Context): Boolean {
        return startSystemActivity(context, Settings.ACTION_BLUETOOTH_SETTINGS)
    }

    fun openAccessibilitySettings(context: Context): Boolean {
        return startSystemActivity(context, Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    fun openDisplaySettings(context: Context): Boolean {
        return startSystemActivity(context, Settings.ACTION_DISPLAY_SETTINGS)
    }

    fun openSoundSettings(context: Context): Boolean {
        return startSystemActivity(context, Settings.ACTION_SOUND_SETTINGS)
    }

    fun openBatterySettings(context: Context): Boolean {
        return startSystemActivity(context, Settings.ACTION_BATTERY_SAVER_SETTINGS)
            || startSystemActivity(context, Intent.ACTION_POWER_USAGE_SUMMARY)
    }

    fun openMainSettings(context: Context): Boolean {
        return startSystemActivity(context, Settings.ACTION_SETTINGS)
    }

    fun openAppsSettings(context: Context): Boolean {
        return startSystemActivity(context, Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
    }

    fun openCamera(context: Context): Boolean {
        return startSystemActivity(context, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
    }

    fun openAlarms(context: Context): Boolean {
        return startSystemActivity(context, AlarmClock.ACTION_SHOW_ALARMS)
    }

    private fun startSystemActivity(context: Context, action: String): Boolean {
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }
}
