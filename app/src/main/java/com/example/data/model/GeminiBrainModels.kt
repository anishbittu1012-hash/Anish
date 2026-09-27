package com.example.data.model

import java.util.UUID

enum class GeminiBrainMode(
    val id: String,
    val title: String,
    val titleBn: String,
    val subtitle: String,
    val temperature: Float,
    val systemPromptExtension: String
) {
    BUTLER(
        id = "butler",
        title = "GENTLEMAN BUTLER",
        titleBn = "ভদ্র জেন্টলম্যান",
        subtitle = "Classic JARVIS witty & articulate AI",
        temperature = 0.7f,
        systemPromptExtension = "You are J.A.R.V.I.S. (Just A Rather Very Intelligent System). Maintain an impeccably polite gentleman tone, witty intellect, and articulate demeanor. You are bilingual in English and Bengali."
    ),
    TACTICAL(
        id = "tactical",
        title = "TACTICAL DEFENSE",
        titleBn = "কৌশলগত প্রতিরক্ষা",
        subtitle = "Combat telemetry & threat analysis",
        temperature = 0.35f,
        systemPromptExtension = "You are operating in Stark TACTICAL COMBAT & DEFENSE BRAIN MODE. Deliver battlefield analytics, threat detection probabilities, armor energy routing, and tactical countermeasure strategies with razor-sharp authority."
    ),
    QUANTUM(
        id = "quantum",
        title = "QUANTUM SCIENCE",
        titleBn = "কোয়ান্টাম বিজ্ঞান",
        subtitle = "Deep physics, code & calculations",
        temperature = 0.3f,
        systemPromptExtension = "You are operating in QUANTUM SCIENCE & PHYSICS BRAIN MODE. Provide profound scientific reasoning, code synthesis, mathematical derivations, quantum particle mechanics, and Stark tech engineering breakdowns."
    ),
    CREATIVE(
        id = "creative",
        title = "STARK INVENTOR",
        titleBn = "স্টার্ক উদ্ভাবক",
        subtitle = "Nanotech blueprints & creative ideas",
        temperature = 0.9f,
        systemPromptExtension = "You are operating in STARK INVENTOR & BLUEPRINT BRAIN MODE. Brainstorm futuristic nanotech innovations, arc reactor enhancements, inventive prototypes, and visionary technological marvels."
    )
}

data class GeminiBrainTelemetry(
    val status: String = "ONLINE", // "ONLINE", "SYNAPSES_FIRING", "COGNITION_READY", "DESYNC"
    val activeMode: GeminiBrainMode = GeminiBrainMode.BUTLER,
    val synapsesFiredCount: Int = 0,
    val lastLatencyMs: Long = 0L,
    val avgLatencyMs: Long = 0L,
    val totalTokensProcessed: Int = 0,
    val modelName: String = "gemini-3.5-flash",
    val neuralActivityLevel: Float = 0.45f
)

data class BrainThought(
    val id: String = UUID.randomUUID().toString(),
    val query: String,
    val response: String,
    val mode: GeminiBrainMode,
    val latencyMs: Long,
    val tokensUsed: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
