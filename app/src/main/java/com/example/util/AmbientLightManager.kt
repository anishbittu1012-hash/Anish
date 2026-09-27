package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.log10

data class AmbientLightingState(
    val currentLux: Float = 120f,
    val isHardwareSensorPresent: Boolean = false,
    val isDynamicThemeEnabled: Boolean = true,
    val brightnessMultiplier: Float = 1.0f,
    val glowIntensity: Float = 1.0f,
    val lightingCategory: String = "INDOOR_NOMINAL",
    val statusDescription: String = "Nominal Indoor (120 lx)"
)

class AmbientLightManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _lightingState = MutableStateFlow(
        AmbientLightingState(
            currentLux = 150f,
            isHardwareSensorPresent = lightSensor != null,
            isDynamicThemeEnabled = true
        )
    )
    val lightingState: StateFlow<AmbientLightingState> = _lightingState.asStateFlow()

    private var simulatedLux: Float? = null

    init {
        startSensor()
        updateLightingMetrics(150f)
    }

    private var lastRecordedLux = -1f

    fun startSensor() {
        if (lightSensor != null) {
            sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopSensor() {
        sensorManager?.unregisterListener(this)
    }

    fun setDynamicThemeEnabled(enabled: Boolean) {
        val current = _lightingState.value
        _lightingState.value = current.copy(isDynamicThemeEnabled = enabled)
        updateLightingMetrics(current.currentLux)
    }

    fun setManualSimulationLux(lux: Float?) {
        simulatedLux = lux
        val effectiveLux = lux ?: _lightingState.value.currentLux
        updateLightingMetrics(effectiveLux)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val measuredLux = event.values.firstOrNull() ?: return
            if (simulatedLux == null) {
                if (lastRecordedLux < 0 || kotlin.math.abs(measuredLux - lastRecordedLux) >= 5f) {
                    lastRecordedLux = measuredLux
                    updateLightingMetrics(measuredLux)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateLightingMetrics(lux: Float) {
        val clampedLux = lux.coerceAtLeast(0f)
        val isEnabled = _lightingState.value.isDynamicThemeEnabled

        if (!isEnabled) {
            _lightingState.value = _lightingState.value.copy(
                currentLux = clampedLux,
                brightnessMultiplier = 1.0f,
                glowIntensity = 1.0f,
                lightingCategory = "STANDARD_FIXED",
                statusDescription = "Adaptive Mode Disengaged"
            )
            return
        }

        // Categorize lighting levels according to standard photometric illuminance:
        // - Pitch Dark / Stealth: < 10 lux
        // - Dim Room / Evening: 10 - 80 lux
        // - Nominal Indoors: 80 - 400 lux
        // - Bright Office / Overcast: 400 - 1500 lux
        // - Direct Sunlight: > 1500 lux
        val (category, desc, brightness, glow) = when {
            clampedLux < 10f -> {
                Quad(
                    "PITCH_DARK_STEALTH",
                    "Deep Night Stealth (${clampedLux.toInt()} lx)",
                    0.70f, // softer, high-contrast night vision
                    0.65f
                )
            }
            clampedLux < 80f -> {
                Quad(
                    "DIM_INDOORS",
                    "Subdued Ambient (${clampedLux.toInt()} lx)",
                    0.85f,
                    0.85f
                )
            }
            clampedLux < 400f -> {
                Quad(
                    "NOMINAL_INDOOR",
                    "Nominal Indoor (${clampedLux.toInt()} lx)",
                    1.0f,
                    1.0f
                )
            }
            clampedLux < 1500f -> {
                Quad(
                    "BRIGHT_ENVIRONMENT",
                    "High Ambient Uplink (${clampedLux.toInt()} lx)",
                    1.20f,
                    1.25f
                )
            }
            else -> {
                Quad(
                    "DIRECT_SOLAR_OVERDRIVE",
                    "Direct Solar Overdrive (${clampedLux.toInt()} lx)",
                    1.40f, // boost intensity for outdoor readability
                    1.45f
                )
            }
        }

        _lightingState.value = _lightingState.value.copy(
            currentLux = clampedLux,
            brightnessMultiplier = brightness,
            glowIntensity = glow,
            lightingCategory = category,
            statusDescription = desc
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
