package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSky
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Advanced Iron Man Arc Reactor with movie-accurate copper solenoid induction coils,
 * glowing palladium energy torus, counter-rotating tachyon rings, quantum orbital nanites,
 * holographic radar sweep laser, and dynamic audio-reactive Canvas pulse waves.
 */
@Composable
fun ArcReactorCore(
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    rmsDb: Float,
    powerPercent: Int,
    brightnessMultiplier: Float = 1.0f,
    glowIntensity: Float = 1.0f,
    animationSpeedMultiplier: Float = 1.0f,
    particleIntensity: Float = 1.0f,
    holographicSweepEnabled: Boolean = true,
    primaryColor: Color = JarvisCyan,
    secondaryColor: Color = JarvisSky,
    accentColor: Color = JarvisGold,
    ambientLux: Float = 120f,
    onCoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Smooth interpolation between Dimmed Idle (0f) and Active Mic Surge (1f)
    val targetActivity = when {
        isListening -> 1.0f
        isSpeaking -> 0.85f
        isThinking -> 0.65f
        else -> 0.0f
    }

    val activityLevel by animateFloatAsState(
        targetValue = targetActivity,
        animationSpec = tween(durationMillis = (450 / animationSpeedMultiplier).toInt().coerceAtLeast(100), easing = FastOutSlowInEasing),
        label = "arcActivityLevel"
    )

    // Smooth real-time microphone audio amplitude decibels (0..1)
    val targetAudioNorm = if (isListening) (rmsDb / 11f).coerceIn(0f, 1f) else 0f
    val smoothAudio by animateFloatAsState(
        targetValue = targetAudioNorm,
        animationSpec = tween(durationMillis = 70, easing = LinearEasing),
        label = "smoothAudio"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorInfinite")

    val speed = animationSpeedMultiplier.coerceIn(0.2f, 3.0f)

    // Outer gyro rotation
    val baseOuterDuration = if (isListening) 3500 else if (isSpeaking) 4800 else 20000
    val outerDuration = (baseOuterDuration / speed).toInt().coerceAtLeast(400)
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = outerDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRotation"
    )

    // Inner tachyon counter-rotation
    val baseInnerDuration = if (isListening) 2600 else if (isSpeaking) 3600 else 15000
    val innerDuration = (baseInnerDuration / speed).toInt().coerceAtLeast(350)
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = innerDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRotation"
    )

    // Dynamic energy pulse oscillation
    val basePulseDuration = if (isListening) 400 else if (isSpeaking) 650 else 1900
    val pulseDuration = (basePulseDuration / speed).toInt().coerceAtLeast(150)
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseProgress"
    )

    // Radiating shockwave loop for active mic
    val baseShockDuration = (1000 / speed).toInt().coerceAtLeast(250)
    val shockwaveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = baseShockDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shockwaveProgress"
    )

    val shockwaveProgress2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = baseShockDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shockwaveProgress2"
    )

    // Holographic radar sweep angle
    val sweepDuration = (4000 / speed).toInt().coerceAtLeast(500)
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = sweepDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    // Quantum orbital nanites revolution
    val orbitalDuration = (3200 / speed).toInt().coerceAtLeast(400)
    val orbitalAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = orbitalDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalAngle"
    )

    // Color interpolation with suit theme
    val activeBaseColor = when {
        powerPercent > 100 -> accentColor
        isListening -> primaryColor
        isSpeaking -> primaryColor
        isThinking -> secondaryColor
        else -> primaryColor
    }

    val dynamicBaseColor = when {
        ambientLux > 1200f -> primaryColor.copy(alpha = 0.95f)
        ambientLux < 15f -> primaryColor
        else -> activeBaseColor
    }

    val idleDormantColor = primaryColor.copy(alpha = 0.35f)
    val effectiveColor = androidx.compose.ui.graphics.lerp(idleDormantColor, dynamicBaseColor, activityLevel)

    val coreGlowAlpha = (0.22f + 0.65f * activityLevel + 0.17f * smoothAudio).coerceIn(0.15f, 1.0f) * brightnessMultiplier
    val pulseScaleFactor = 1.0f + (0.02f * (1f - activityLevel)) + (0.07f * activityLevel * pulseProgress) + (0.09f * smoothAudio)

    Box(
        modifier = modifier
            .size(260.dp)
            .testTag("arc_reactor_box"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) - 14f

            // 1. OUTLINE HOUSING & PERIMETER BEZEL
            drawArcReactorChassis(
                center = center,
                baseRadius = baseRadius,
                activityLevel = activityLevel,
                effectiveColor = effectiveColor,
                brightnessMultiplier = brightnessMultiplier
            )

            // 2. RADIAL AMBIENT ENERGY CORONA
            val coronaRadius = (baseRadius + 16f * pulseScaleFactor + 24f * smoothAudio).coerceAtLeast(30f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        effectiveColor.copy(alpha = coreGlowAlpha * 0.60f),
                        effectiveColor.copy(alpha = coreGlowAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coronaRadius
                ),
                radius = coronaRadius,
                center = center
            )

            // 3. DYNAMIC SHOCKWAVE PULSES
            if (activityLevel > 0.05f) {
                drawActiveShockwaves(
                    center = center,
                    baseRadius = baseRadius,
                    shockwaveProgress1 = shockwaveProgress,
                    shockwaveProgress2 = (shockwaveProgress2 % 1f),
                    activityLevel = activityLevel,
                    smoothAudio = smoothAudio,
                    color = effectiveColor
                )
            }

            // 4. HOLOGRAPHIC RADAR SCANNER SWEEP BEAM
            if (holographicSweepEnabled) {
                drawRadarSweepBeam(
                    center = center,
                    radius = baseRadius - 6f,
                    sweepAngle = sweepAngle,
                    color = effectiveColor,
                    activityLevel = activityLevel
                )
            }

            // 5. THE GLOWING PALLADIUM ENERGY TORUS
            val torusRadius = baseRadius - 22f
            drawPalladiumEnergyTorus(
                center = center,
                torusRadius = torusRadius,
                activityLevel = activityLevel,
                pulseProgress = pulseProgress,
                smoothAudio = smoothAudio,
                effectiveColor = effectiveColor,
                brightnessMultiplier = brightnessMultiplier,
                glowIntensity = glowIntensity
            )

            // 6. THE 10 COPPER SOLENOID INDUCTION COILS
            rotate(outerRotation, pivot = center) {
                drawSolenoidCopperCoils(
                    center = center,
                    coilRadius = torusRadius,
                    activityLevel = activityLevel,
                    effectiveColor = effectiveColor,
                    glowIntensity = glowIntensity,
                    brightnessMultiplier = brightnessMultiplier
                )
            }

            // 7. COUNTER-ROTATING TACHYON INNER RING & STABILIZERS
            rotate(innerRotation, pivot = center) {
                drawInnerTachyonRing(
                    center = center,
                    radius = baseRadius - 48f,
                    activityLevel = activityLevel,
                    pulseProgress = pulseProgress,
                    effectiveColor = effectiveColor,
                    brightnessMultiplier = brightnessMultiplier
                )
            }

            // 8. QUANTUM ORBITAL NANITE PARTICLES
            if (particleIntensity > 0.1f) {
                drawQuantumOrbitalParticles(
                    center = center,
                    baseRadius = baseRadius,
                    orbitalAngle = orbitalAngle,
                    activityLevel = activityLevel,
                    effectiveColor = effectiveColor,
                    accentColor = accentColor,
                    smoothAudio = smoothAudio,
                    intensity = particleIntensity
                )
            }

            // 9. DYNAMIC AUDIO-REACTIVE PERIMETER FLUX RING
            if (isListening || smoothAudio > 0.05f) {
                drawAudioReactiveFluxRing(
                    center = center,
                    radius = baseRadius - 35f,
                    smoothAudio = smoothAudio,
                    color = effectiveColor
                )
            }

            // 10. INVERTED STARK TRIANGLE & CONFINEMENT FLUX STRUTS
            drawStarkCoreTriangle(
                center = center,
                triRadius = 38f * pulseScaleFactor,
                activityLevel = activityLevel,
                effectiveColor = effectiveColor,
                smoothAudio = smoothAudio,
                brightnessMultiplier = brightnessMultiplier
            )

            // 11. CENTRAL PLASMA BLOOM & HIGH-ENERGY APERTURE
            val plasmaRadius = (20f + 14f * activityLevel + 16f * smoothAudio) * glowIntensity
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        effectiveColor,
                        effectiveColor.copy(alpha = 0.45f * activityLevel),
                        Color.Transparent
                    ),
                    center = center,
                    radius = plasmaRadius
                ),
                radius = plasmaRadius,
                center = center
            )
        }

        // CENTER INTERACTIVE LENS / VOCAL MICROPHONE DOMED BUTTON
        val centerSize = 76.dp
        val centerBorderColor = if (activityLevel > 0.3f) effectiveColor else effectiveColor.copy(alpha = 0.5f)
        val buttonGlowElevation = ((7f + 18f * activityLevel + 14f * smoothAudio) * glowIntensity).dp

        Box(
            modifier = Modifier
                .size(centerSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            JarvisDarkSurface.copy(alpha = 0.92f),
                            Color(0xFF030D16).copy(alpha = 0.98f)
                        )
                    )
                )
                .border(2.dp, centerBorderColor, CircleShape)
                .shadow(
                    elevation = buttonGlowElevation,
                    shape = CircleShape,
                    spotColor = effectiveColor,
                    ambientColor = effectiveColor.copy(alpha = 0.55f)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCoreClick
                )
                .testTag("reactor_center_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isListening -> Icons.Default.GraphicEq
                    isSpeaking -> Icons.AutoMirrored.Filled.VolumeUp
                    else -> Icons.Default.Mic
                },
                contentDescription = if (isListening) "Listening" else "Mic",
                tint = if (activityLevel > 0.2f) effectiveColor else effectiveColor.copy(alpha = 0.75f),
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

/**
 * 1. Draws the heavy metal reactor outer rim, 60 fine graduation ticks, and mechanical rivets.
 */
private fun DrawScope.drawArcReactorChassis(
    center: Offset,
    baseRadius: Float,
    activityLevel: Float,
    effectiveColor: Color,
    brightnessMultiplier: Float
) {
    val rimColor = Color(0xFF07141E)
    drawCircle(
        color = rimColor,
        radius = baseRadius + 3f,
        center = center,
        style = Stroke(width = 4.5f)
    )

    val hairlineAlpha = (0.25f + 0.35f * activityLevel).coerceIn(0.15f, 0.75f) * brightnessMultiplier
    drawCircle(
        color = effectiveColor.copy(alpha = hairlineAlpha),
        radius = baseRadius + 1f,
        center = center,
        style = Stroke(width = 1.3f)
    )

    val tickAlpha = (0.35f + 0.45f * activityLevel).coerceIn(0.2f, 0.85f) * brightnessMultiplier
    for (i in 0 until 60) {
        val angleDeg = i * 6.0
        val isMajor = (i % 5 == 0)
        val rad = Math.toRadians(angleDeg)
        val tickLength = if (isMajor) 7.5f else 3.5f
        val tickWidth = if (isMajor) 1.8f else 1.0f

        val startR = baseRadius - 1f
        val endR = baseRadius - 1f - tickLength

        val pStart = Offset(center.x + startR * cos(rad).toFloat(), center.y + startR * sin(rad).toFloat())
        val pEnd = Offset(center.x + endR * cos(rad).toFloat(), center.y + endR * sin(rad).toFloat())

        drawLine(
            color = if (isMajor) effectiveColor.copy(alpha = tickAlpha) else effectiveColor.copy(alpha = tickAlpha * 0.6f),
            start = pStart,
            end = pEnd,
            strokeWidth = tickWidth,
            cap = StrokeCap.Round
        )
    }

    for (i in 0 until 10) {
        val angleDeg = i * 36.0 + 18.0
        val rad = Math.toRadians(angleDeg)
        val boltR = baseRadius + 1.5f
        val boltPos = Offset(center.x + boltR * cos(rad).toFloat(), center.y + boltR * sin(rad).toFloat())
        drawCircle(
            color = Color(0xFF1B2F3D),
            radius = 2.4f,
            center = boltPos
        )
        drawCircle(
            color = effectiveColor.copy(alpha = (0.45f * activityLevel).coerceIn(0.1f, 0.7f)),
            radius = 1.0f,
            center = boltPos
        )
    }
}

/**
 * Draws dynamic concentric shockwaves radiating outwards from the core.
 */
private fun DrawScope.drawActiveShockwaves(
    center: Offset,
    baseRadius: Float,
    shockwaveProgress1: Float,
    shockwaveProgress2: Float,
    activityLevel: Float,
    smoothAudio: Float,
    color: Color
) {
    val minR = 36f
    val maxR = baseRadius - 20f

    val r1 = minR + (maxR - minR) * shockwaveProgress1
    val alpha1 = ((1f - shockwaveProgress1) * 0.45f * activityLevel * (0.6f + 0.4f * smoothAudio)).coerceIn(0f, 1f)
    drawCircle(
        color = color.copy(alpha = alpha1),
        radius = r1,
        center = center,
        style = Stroke(width = (2.5f * (1f - shockwaveProgress1) + 1.2f), cap = StrokeCap.Round)
    )

    val r2 = minR + (maxR - minR) * shockwaveProgress2
    val alpha2 = ((1f - shockwaveProgress2) * 0.45f * activityLevel * (0.6f + 0.4f * smoothAudio)).coerceIn(0f, 1f)
    drawCircle(
        color = color.copy(alpha = alpha2),
        radius = r2,
        center = center,
        style = Stroke(width = (2.5f * (1f - shockwaveProgress2) + 1.2f), cap = StrokeCap.Round)
    )
}

/**
 * Draws a rotating 360-degree holographic laser sweep / radar beam across the reactor.
 */
private fun DrawScope.drawRadarSweepBeam(
    center: Offset,
    radius: Float,
    sweepAngle: Float,
    color: Color,
    activityLevel: Float
) {
    val alpha = (0.12f + 0.25f * activityLevel).coerceIn(0.08f, 0.4f)
    val sweepSpan = 45f

    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color.Transparent,
                color.copy(alpha = alpha * 0.2f),
                color.copy(alpha = alpha),
                Color.White.copy(alpha = alpha * 1.2f)
            ),
            center = center
        ),
        startAngle = sweepAngle - sweepSpan,
        sweepAngle = sweepSpan,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f)
    )

    // Leading laser line
    val rad = Math.toRadians(sweepAngle.toDouble())
    val endPt = Offset(center.x + radius * cos(rad).toFloat(), center.y + radius * sin(rad).toFloat())
    drawLine(
        color = Color.White.copy(alpha = alpha * 1.5f),
        start = center,
        end = endPt,
        strokeWidth = 1.4f,
        cap = StrokeCap.Round
    )
}

/**
 * Draws the glowing circular palladium energy conduit (torus ring) underneath the coils.
 */
private fun DrawScope.drawPalladiumEnergyTorus(
    center: Offset,
    torusRadius: Float,
    activityLevel: Float,
    pulseProgress: Float,
    smoothAudio: Float,
    effectiveColor: Color,
    brightnessMultiplier: Float,
    glowIntensity: Float
) {
    val diffuseAlpha = (0.22f + 0.45f * activityLevel + 0.20f * pulseProgress + 0.15f * smoothAudio).coerceIn(0.12f, 0.95f) * brightnessMultiplier
    val diffuseStroke = (10f + 8f * activityLevel + 8f * smoothAudio) * glowIntensity

    drawCircle(
        color = effectiveColor.copy(alpha = diffuseAlpha * 0.4f),
        radius = torusRadius,
        center = center,
        style = Stroke(width = diffuseStroke)
    )

    val coreStroke = 3.4f + 2.0f * activityLevel
    val coreAlpha = (0.45f + 0.55f * activityLevel).coerceIn(0.35f, 1.0f) * brightnessMultiplier

    drawCircle(
        color = effectiveColor.copy(alpha = coreAlpha),
        radius = torusRadius,
        center = center,
        style = Stroke(width = coreStroke)
    )

    if (activityLevel > 0.15f) {
        val whiteAlpha = (0.35f * activityLevel + 0.5f * smoothAudio).coerceIn(0f, 0.9f)
        drawCircle(
            color = Color.White.copy(alpha = whiteAlpha),
            radius = torusRadius,
            center = center,
            style = Stroke(width = 1.4f)
        )
    }
}

/**
 * Draws the 10 copper-wound solenoid induction coils with metallic highlights.
 */
private fun DrawScope.drawSolenoidCopperCoils(
    center: Offset,
    coilRadius: Float,
    activityLevel: Float,
    effectiveColor: Color,
    glowIntensity: Float,
    brightnessMultiplier: Float
) {
    val numCoils = 10
    val angleStep = 360.0 / numCoils
    val coilAngularSpan = 22.0
    val coilRadialThickness = 15.0f

    val baseCopper = Color(0xFFB87333)
    val litCopper = Color(0xFFE59866)
    val darkClamp = Color(0xFF131920)

    for (i in 0 until numCoils) {
        val centerAngleDeg = i * angleStep
        val startAngleDeg = centerAngleDeg - coilAngularSpan / 2.0

        if (activityLevel > 0.05f) {
            val radCenter = Math.toRadians(centerAngleDeg)
            val glowPos = Offset(
                center.x + coilRadius * cos(radCenter).toFloat(),
                center.y + coilRadius * sin(radCenter).toFloat()
            )
            drawCircle(
                color = effectiveColor.copy(alpha = (0.30f * activityLevel * glowIntensity).coerceIn(0f, 0.55f)),
                radius = 12f * glowIntensity,
                center = glowPos
            )
        }

        val rInner = coilRadius - coilRadialThickness / 2f
        val rOuter = coilRadius + coilRadialThickness / 2f

        val wireLayers = 4
        for (w in 0 until wireLayers) {
            val wireRadius = rInner + (rOuter - rInner) * (w + 0.5f) / wireLayers
            val isHighlight = (w == 1 || w == 2)
            val copperColor = if (activityLevel > 0.4f && isHighlight) litCopper else baseCopper
            val wireAlpha = (0.65f + 0.35f * activityLevel).coerceIn(0.5f, 1.0f) * brightnessMultiplier

            drawArc(
                color = copperColor.copy(alpha = wireAlpha),
                startAngle = startAngleDeg.toFloat() + 1.5f,
                sweepAngle = (coilAngularSpan - 3.0).toFloat(),
                useCenter = false,
                topLeft = Offset(center.x - wireRadius, center.y - wireRadius),
                size = Size(wireRadius * 2f, wireRadius * 2f),
                style = Stroke(width = 2.4f, cap = StrokeCap.Butt)
            )
        }

        val radStart = Math.toRadians(startAngleDeg)
        val radEnd = Math.toRadians(startAngleDeg + coilAngularSpan)

        val clampWidth = 2.8f
        drawLine(
            color = darkClamp,
            start = Offset(center.x + (rInner - 1.5f) * cos(radStart).toFloat(), center.y + (rInner - 1.5f) * sin(radStart).toFloat()),
            end = Offset(center.x + (rOuter + 1.5f) * cos(radStart).toFloat(), center.y + (rOuter + 1.5f) * sin(radStart).toFloat()),
            strokeWidth = clampWidth,
            cap = StrokeCap.Square
        )
        drawLine(
            color = darkClamp,
            start = Offset(center.x + (rInner - 1.5f) * cos(radEnd).toFloat(), center.y + (rInner - 1.5f) * sin(radEnd).toFloat()),
            end = Offset(center.x + (rOuter + 1.5f) * cos(radEnd).toFloat(), center.y + (rOuter + 1.5f) * sin(radEnd).toFloat()),
            strokeWidth = clampWidth,
            cap = StrokeCap.Square
        )

        val ledR = rOuter + 3.5f
        val radMid = Math.toRadians(centerAngleDeg)
        val ledPos = Offset(center.x + ledR * cos(radMid).toFloat(), center.y + ledR * sin(radMid).toFloat())
        val ledAlpha = (0.25f + 0.75f * activityLevel).coerceIn(0.2f, 1.0f)
        drawCircle(
            color = effectiveColor.copy(alpha = ledAlpha),
            radius = 1.3f,
            center = ledPos
        )
    }
}

/**
 * Draws the inner counter-rotating tachyon ring with energy cooling vents and crosshairs.
 */
private fun DrawScope.drawInnerTachyonRing(
    center: Offset,
    radius: Float,
    activityLevel: Float,
    pulseProgress: Float,
    effectiveColor: Color,
    brightnessMultiplier: Float
) {
    val segments = 8
    val sweep = 31f
    val gap = 14f

    val alpha = (0.35f + 0.65f * activityLevel).coerceIn(0.25f, 1.0f) * brightnessMultiplier
    val strokeWidth = 3.6f + 2.0f * activityLevel

    for (i in 0 until segments) {
        val angle = i * (sweep + gap)
        drawArc(
            color = effectiveColor.copy(alpha = alpha),
            startAngle = angle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }

    val crosshairAlpha = (0.15f + 0.35f * activityLevel).coerceIn(0.1f, 0.5f) * brightnessMultiplier
    drawLine(
        color = effectiveColor.copy(alpha = crosshairAlpha),
        start = Offset(center.x - radius + 4f, center.y),
        end = Offset(center.x + radius - 4f, center.y),
        strokeWidth = 1.0f
    )
    drawLine(
        color = effectiveColor.copy(alpha = crosshairAlpha),
        start = Offset(center.x, center.y - radius + 4f),
        end = Offset(center.x, center.y + radius - 4f),
        strokeWidth = 1.0f
    )
}

/**
 * Draws 8 quantum orbital nanite particles circulating the reactor tracks at distinct radii and speeds.
 */
private fun DrawScope.drawQuantumOrbitalParticles(
    center: Offset,
    baseRadius: Float,
    orbitalAngle: Float,
    activityLevel: Float,
    effectiveColor: Color,
    accentColor: Color,
    smoothAudio: Float,
    intensity: Float
) {
    val particleCount = (8 * intensity).toInt().coerceIn(4, 12)
    val baseTrackRadius = baseRadius - 10f

    for (p in 0 until particleCount) {
        val speedFactor = if (p % 2 == 0) 1.0f + (p * 0.15f) else -(0.8f + (p * 0.12f))
        val currentAngle = (orbitalAngle * speedFactor + (p * (360f / particleCount))) % 360f
        val rad = Math.toRadians(currentAngle.toDouble())

        val orbitR = baseTrackRadius - (p % 3) * 14f + (smoothAudio * 10f)
        val px = center.x + orbitR * cos(rad).toFloat()
        val py = center.y + orbitR * sin(rad).toFloat()

        val pColor = if (p % 3 == 0) accentColor else effectiveColor
        val pAlpha = (0.4f + 0.6f * activityLevel + 0.3f * smoothAudio).coerceIn(0.3f, 1.0f)
        val pRadius = 2.0f + (1.2f * smoothAudio) + if (p % 2 == 0) 0.8f else 0f

        // Glow halo
        drawCircle(
            color = pColor.copy(alpha = pAlpha * 0.35f),
            radius = pRadius * 2.5f,
            center = Offset(px, py)
        )
        // Solid core node
        drawCircle(
            color = Color.White.copy(alpha = pAlpha),
            radius = pRadius * 0.6f,
            center = Offset(px, py)
        )
    }
}

/**
 * Draws an oscillating audio frequency contour around the inner perimeter ring.
 */
private fun DrawScope.drawAudioReactiveFluxRing(
    center: Offset,
    radius: Float,
    smoothAudio: Float,
    color: Color
) {
    val points = 36
    val path = Path()

    for (i in 0 until points) {
        val angleDeg = i * (360f / points)
        val rad = Math.toRadians(angleDeg.toDouble())
        val waveMod = sin(angleDeg * 4 * (PI / 180.0)).toFloat() * (smoothAudio * 12f)
        val r = radius + waveMod
        val px = center.x + r * cos(rad).toFloat()
        val py = center.y + r * sin(rad).toFloat()

        if (i == 0) {
            path.moveTo(px, py)
        } else {
            path.lineTo(px, py)
        }
    }
    path.close()

    drawPath(
        path = path,
        color = color.copy(alpha = (0.35f + 0.55f * smoothAudio).coerceIn(0.2f, 0.9f)),
        style = Stroke(width = 1.6f, cap = StrokeCap.Round)
    )
}

/**
 * Draws the inverted Stark equilateral triangle and magnetic flux guides.
 */
private fun DrawScope.drawStarkCoreTriangle(
    center: Offset,
    triRadius: Float,
    activityLevel: Float,
    effectiveColor: Color,
    smoothAudio: Float,
    brightnessMultiplier: Float
) {
    val triPath = Path().apply {
        val p1 = Offset(center.x, center.y - triRadius)
        val p2 = Offset(center.x + triRadius * 0.866f, center.y + triRadius * 0.5f)
        val p3 = Offset(center.x - triRadius * 0.866f, center.y + triRadius * 0.5f)
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }

    val fillAlpha = (0.08f + 0.28f * activityLevel + 0.20f * smoothAudio).coerceIn(0.06f, 0.55f) * brightnessMultiplier
    drawPath(
        path = triPath,
        color = effectiveColor.copy(alpha = fillAlpha)
    )

    val strokeWidth = (2.0f + 1.8f * activityLevel + 1.5f * smoothAudio).coerceIn(1.8f, 5.0f)
    val strokeAlpha = (0.35f + 0.65f * activityLevel).coerceIn(0.3f, 1.0f) * brightnessMultiplier
    drawPath(
        path = triPath,
        color = effectiveColor.copy(alpha = strokeAlpha),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    val rad0 = -PI / 2.0
    val rad1 = rad0 + 2.0 * PI / 3.0
    val rad2 = rad0 + 4.0 * PI / 3.0
    val angles = listOf(rad0, rad1, rad2)

    for (rad in angles) {
        val vx = center.x + triRadius * cos(rad).toFloat()
        val vy = center.y + triRadius * sin(rad).toFloat()
        drawCircle(
            color = if (activityLevel > 0.2f) Color.White else effectiveColor,
            radius = 2.4f + 1.2f * activityLevel,
            center = Offset(vx, vy)
        )
    }
}
