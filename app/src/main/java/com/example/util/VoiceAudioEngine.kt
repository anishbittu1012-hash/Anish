package com.example.util

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.sin

enum class VoiceLanguage(val code: String, val displayName: String, val bcp47: String) {
    AUTO("auto", "Auto (EN/HI/BN)", "en-US"),
    ENGLISH("en", "English", "en-US"),
    HINDI("hi", "हिंदी / Hinglish", "hi-IN"),
    BENGALI("bn", "বাংলা (Bengali)", "bn-BD")
}

class SpeechRecognizerHelper(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _liveHypothesis = MutableStateFlow("")
    val liveHypothesis: StateFlow<String> = _liveHypothesis.asStateFlow()

    private val _activeLanguage = MutableStateFlow(VoiceLanguage.AUTO)
    val activeLanguage: StateFlow<VoiceLanguage> = _activeLanguage.asStateFlow()

    var onSpeechResult: ((String) -> Unit)? = null
    var onPartialResult: ((String) -> Unit)? = null
    var onListeningStarted: (() -> Unit)? = null
    var onSpeechError: ((String, String) -> Unit)? = null // message, error code

    fun setLanguage(language: VoiceLanguage) {
        _activeLanguage.value = language
    }

    private fun destroyRecognizerInternal() {
        try {
            speechRecognizer?.setRecognitionListener(null)
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignore cleanup exception
        } finally {
            speechRecognizer = null
        }
    }

    fun startListening() {
        // Reset states immediately so UI reflects listening mode
        _isListening.value = true
        _rmsDb.value = 0f
        _liveHypothesis.value = ""

        val action = Runnable {
            try {
                destroyRecognizerInternal()

                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    val isBn = _activeLanguage.value == VoiceLanguage.BENGALI
                    val msg = if (isBn)
                        "স্পিচ রিকগনিশন সার্ভিস এই ডিভাইসে উপলব্ধ নেই। অনুগ্রহ করে গুগল স্পিচ সার্ভিস ব্যবহার করুন।"
                    else
                        "Speech recognition service unavailable on this device. Please install Google Speech Services."
                    _isListening.value = false
                    _rmsDb.value = 0f
                    onSpeechError?.invoke(msg, "SERVICE_UNAVAILABLE")
                    return@Runnable
                }

                val recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
                    try {
                        SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                    } catch (e: Exception) {
                        SpeechRecognizer.createSpeechRecognizer(context)
                    }
                } else {
                    SpeechRecognizer.createSpeechRecognizer(context)
                }
                speechRecognizer = recognizer

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _rmsDb.value = 0f
                        onListeningStarted?.invoke()
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _rmsDb.value = rmsdB.coerceIn(0f, 12f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        // Keep listening state active until onResults or onError
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _rmsDb.value = 0f
                        destroyRecognizerInternal()

                        val isBengali = _activeLanguage.value == VoiceLanguage.BENGALI

                        val (message, code) = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> {
                                if (isBengali) "কোনো বক্তব্য শোনা যায়নি। পুনরায় বলতে ট্যাপ করুন।" to "NO_MATCH"
                                else "No speech detected. Tap to speak again." to "NO_MATCH"
                            }
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                                if (isBengali) "সময়সীমা অতিক্রান্ত হয়েছে। পুনরায় বলতে ট্যাপ করুন।" to "TIMEOUT"
                                else "Listening timed out. Tap to speak again." to "TIMEOUT"
                            }
                            SpeechRecognizer.ERROR_AUDIO -> {
                                if (isBengali) "অডিও রেকর্ডিংয়ে ত্রুটি দেখা দিয়েছে।" to "AUDIO_ERROR"
                                else "Audio recording error. Check mic hardware or tap to try again." to "AUDIO_ERROR"
                            }
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                                if (isBengali) "মাইক্রোফোন ব্যবহারের অনুমতি প্রয়োজন।" to "PERMISSION_DENIED"
                                else "Microphone permission required. Tap to grant permission." to "PERMISSION_DENIED"
                            }
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                                if (isBengali) "নেটওয়ার্ক যোগাযোগে সমস্যা হয়েছে।" to "NETWORK_ERROR"
                                else "Network uplink interrupted. Tap to speak again." to "NETWORK_ERROR"
                            }
                            SpeechRecognizer.ERROR_CLIENT -> {
                                if (isBengali) "ভয়েস সেন্সর রিসেট হয়েছে। পুনরায় ট্যাপ করুন।" to "CLIENT_ERROR"
                                else "Voice sensor reset. Tap to speak again." to "CLIENT_ERROR"
                            }
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                if (isBengali) "ভয়েস সার্ভিস ব্যস্ত আছে। পুনরায় ট্যাপ করুন।" to "RECOGNIZER_BUSY"
                                else "Speech engine busy. Tap to speak again." to "RECOGNIZER_BUSY"
                            }
                            SpeechRecognizer.ERROR_SERVER -> {
                                if (isBengali) "স্পিচ সার্ভার যোগাযোগে বিঘ্ন ঘটেছে।" to "SERVER_ERROR"
                                else "Speech recognition server error. Tap to speak again." to "SERVER_ERROR"
                            }
                            else -> {
                                if (isBengali) "ভয়েস যোগাযোগে বিঘ্ন ঘটেছে ($error)। পুনরায় ট্যাপ করুন।" to "UNKNOWN"
                                else "Speech recognition error ($error). Tap to speak again." to "UNKNOWN"
                            }
                        }
                        onSpeechError?.invoke(message, code)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _rmsDb.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val recognized = matches?.firstOrNull()?.trim() ?: ""
                        _liveHypothesis.value = recognized
                        destroyRecognizerInternal()

                        if (recognized.isNotBlank()) {
                            onSpeechResult?.invoke(recognized)
                        } else {
                            val isBengali = _activeLanguage.value == VoiceLanguage.BENGALI
                            val noSpeechMsg = if (isBengali)
                                "কোনো বক্তব্য শোনা যায়নি। পুনরায় বলতে ট্যাপ করুন।"
                            else
                                "No speech detected. Tap to speak again."
                            onSpeechError?.invoke(noSpeechMsg, "NO_MATCH")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                        if (!partial.isNullOrBlank()) {
                            _liveHypothesis.value = partial
                            onPartialResult?.invoke(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val currentLang = _activeLanguage.value
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS is listening... Speak now")
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L)

                    when (currentLang) {
                        VoiceLanguage.HINDI -> {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "en-US", "bn-IN"))
                        }
                        VoiceLanguage.BENGALI -> {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN")
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-IN")
                            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-IN", "bn-BD", "en-US", "en-GB"))
                        }
                        VoiceLanguage.ENGLISH -> {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "en-GB", "en-IN", "bn-IN", "bn-BD"))
                        }
                        VoiceLanguage.AUTO -> {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-IN", "bn-BD", "en-US", "en-GB", "hi-IN", "en-IN"))
                        }
                    }
                }

                recognizer.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                _rmsDb.value = 0f
                destroyRecognizerInternal()
                val isBn = _activeLanguage.value == VoiceLanguage.BENGALI
                val err = if (isBn) {
                    "ভয়েস রিকগনিশন প্রস্তুত করা যায়নি: ${e.message ?: "সেবা অনুপস্থিত"}। পুনরায় চেষ্টা করুন।"
                } else {
                    "Could not initialize speech recognition: ${e.message ?: "Service unavailable"}. Tap to try again."
                }
                onSpeechError?.invoke(err, "INIT_FAILED")
            }
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run()
        } else {
            mainHandler.post(action)
        }
    }

    fun createSystemVoiceDialogIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS is listening... Speak your command")
            when (_activeLanguage.value) {
                VoiceLanguage.HINDI -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                }
                VoiceLanguage.BENGALI -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                }
                VoiceLanguage.ENGLISH -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                }
                VoiceLanguage.AUTO -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                }
            }
        }
    }

    fun stopListening() {
        _isListening.value = false
        _rmsDb.value = 0f
        if (Looper.myLooper() == Looper.getMainLooper()) {
            destroyRecognizerInternal()
        } else {
            mainHandler.post { destroyRecognizerInternal() }
        }
    }
}

enum class RoboticVoicePreset(
    val id: String,
    val displayName: String,
    val pitch: Float,
    val speechRate: Float,
    val description: String
) {
    NATURAL_FRIENDLY(
        id = "natural_friendly",
        displayName = "Natural Friendly AI",
        pitch = 1.00f,
        speechRate = 1.00f,
        description = "Warm, welcoming, natural human-like cadence (non-robotic)"
    ),
    WARM_COMPANION(
        id = "warm_companion",
        displayName = "Warm British Assistant",
        pitch = 0.98f,
        speechRate = 1.00f,
        description = "Articulate, polite, natural gentleman cadence"
    ),
    CALM_GENTLE(
        id = "calm_gentle",
        displayName = "Calm & Gentle",
        pitch = 1.04f,
        speechRate = 0.95f,
        description = "Soft, soothing and attentive conversational tone"
    ),
    CRISP_TACTICAL(
        id = "crisp_tactical",
        displayName = "Dynamic & Expressive",
        pitch = 1.00f,
        speechRate = 1.08f,
        description = "Clear, brisk and energetic natural speech"
    ),
    STARK_JARVIS(
        id = "stark_jarvis",
        displayName = "J.A.R.V.I.S. Classic",
        pitch = 0.95f,
        speechRate = 1.00f,
        description = "Sophisticated British AI cadence with harmonic depth"
    ),
    CYBERNETIC_ROBOT(
        id = "cybernetic_robot",
        displayName = "Cybernetic Tone",
        pitch = 0.85f,
        speechRate = 1.05f,
        description = "Synthesized cadence for sci-fi atmosphere"
    ),
    DEEP_CORE_AI(
        id = "deep_core_ai",
        displayName = "Deep Resonant",
        pitch = 0.80f,
        speechRate = 0.96f,
        description = "Deliberate baritone acoustic presence"
    ),
    QUANTUM_SYNTH(
        id = "quantum_synth",
        displayName = "High Clarity",
        pitch = 1.10f,
        speechRate = 1.05f,
        description = "High-frequency articulated vocal tone"
    )
}

class TtsManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentLanguageName = MutableStateFlow("English")
    val currentLanguageName: StateFlow<String> = _currentLanguageName.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.00f)
    val speechPitchFlow: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _speechRate = MutableStateFlow(1.00f)
    val speechRateFlow: StateFlow<Float> = _speechRate.asStateFlow()

    private val _selectedPreset = MutableStateFlow(RoboticVoicePreset.NATURAL_FRIENDLY)
    val selectedPreset: StateFlow<RoboticVoicePreset> = _selectedPreset.asStateFlow()

    private val _roboticChirpEnabled = MutableStateFlow(false)
    val roboticChirpEnabled: StateFlow<Boolean> = _roboticChirpEnabled.asStateFlow()

    var speechRate: Float
        get() = _speechRate.value
        set(value) {
            _speechRate.value = value.coerceIn(0.6f, 1.8f)
            tts?.setSpeechRate(_speechRate.value)
        }

    var speechPitch: Float
        get() = _speechPitch.value
        set(value) {
            _speechPitch.value = value.coerceIn(0.6f, 1.8f)
            tts?.setPitch(_speechPitch.value)
        }

    fun setPreset(preset: RoboticVoicePreset) {
        _selectedPreset.value = preset
        speechPitch = preset.pitch
        speechRate = preset.speechRate
    }

    fun setRoboticChirp(enabled: Boolean) {
        _roboticChirpEnabled.value = enabled
    }

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true

                // Configure low-latency speech audio attributes
                try {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(audioAttributes)
                } catch (e: Exception) {
                    // Safe fallback
                }

                val ukResult = tts?.setLanguage(Locale.UK)
                if (ukResult == TextToSpeech.LANG_MISSING_DATA || ukResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }

                // Prefer natural friendly non-robotic voice
                try {
                    val voices = tts?.voices
                    val naturalVoice = voices?.firstOrNull { v ->
                        v.locale.language == "en" &&
                            !v.name.contains("robot", ignoreCase = true) &&
                            (v.name.contains("natural", ignoreCase = true) ||
                             v.name.contains("neural", ignoreCase = true) ||
                             v.name.contains("wavenet", ignoreCase = true) ||
                             v.name.contains("en-gb", ignoreCase = true))
                    } ?: voices?.firstOrNull { v ->
                        v.locale.language == "en" && !v.name.contains("robot", ignoreCase = true)
                    }

                    if (naturalVoice != null) {
                        tts?.voice = naturalVoice
                    }
                } catch (e: Exception) {
                    // Safe fallback
                }

                tts?.setSpeechRate(_speechRate.value)
                tts?.setPitch(_speechPitch.value)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    private fun containsHindi(text: String): Boolean {
        for (char in text) {
            val code = char.code
            if (code in 0x0900..0x097F) {
                return true
            }
        }
        return false
    }

    private fun isHinglish(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("mera naam") || lower.contains("tumhara naam") ||
               lower.contains("kholo") || lower.contains("karo") ||
               lower.contains("hoon") || lower.contains("koshish") ||
               lower.contains("madad") || lower.contains("samajh") ||
               lower.contains("aapka") || lower.contains("bataiye") ||
               lower.contains("theek") || lower.contains("karta hoon") ||
               lower.contains("bilkul") || lower.contains("khol raha")
    }

    private fun containsBengali(text: String): Boolean {
        for (char in text) {
            val code = char.code
            if (code in 0x0980..0x09FF) {
                return true
            }
        }
        return false
    }

    /**
     * Cleans markdown, formatting, code blocks, and symbols so TTS speaks in natural human speech
     */
    private fun formatNaturalSpeech(rawText: String): String {
        return rawText
            .replace(Regex("```[\\s\\S]*?```"), "Code block omitted.")
            .replace(Regex("`.*?`"), "")
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("#+\\s*"), "")
            .replace(Regex("!\\[.*?\\]\\(.*?\\)"), "")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .replace("•", ",")
            .replace("- ", ", ")
            .replace("%", " percent ")
            .replace("&", " and ")
            .replace("JARVIS", "Jarvis")
            .replace("AI", "A.I.")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized || tts == null) return

        val formattedText = formatNaturalSpeech(text)
        if (formattedText.isBlank()) return

        // Gentle notification chime if enabled
        if (_roboticChirpEnabled.value) {
            SoundFxGenerator.playResponseArrivalChime()
        }

        tts?.setSpeechRate(_speechRate.value)
        tts?.setPitch(_speechPitch.value)

        // Intelligently select Hindi, Bengali, or English TTS voice with natural preference
        if (containsHindi(formattedText) || isHinglish(formattedText)) {
            val hiLocale = Locale.forLanguageTag("hi-IN")
            val available = tts?.isLanguageAvailable(hiLocale)
            if (available != null && available >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = hiLocale
                _currentLanguageName.value = "हिंदी"
            } else {
                val inLocale = Locale.forLanguageTag("en-IN")
                if (tts?.isLanguageAvailable(inLocale) ?: -1 >= TextToSpeech.LANG_AVAILABLE) {
                    tts?.language = inLocale
                    _currentLanguageName.value = "Hinglish (IN)"
                } else {
                    _currentLanguageName.value = "हिंदी / Hinglish"
                }
            }
        } else if (containsBengali(formattedText)) {
            val bnLocale = Locale.forLanguageTag("bn-BD")
            val available = tts?.isLanguageAvailable(bnLocale)
            if (available != null && available >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = bnLocale
                _currentLanguageName.value = "বাংলা"
            } else {
                val bnIn = Locale.forLanguageTag("bn-IN")
                if (tts?.isLanguageAvailable(bnIn) ?: -1 >= TextToSpeech.LANG_AVAILABLE) {
                    tts?.language = bnIn
                    _currentLanguageName.value = "বাংলা"
                }
            }
        } else {
            val ukResult = tts?.setLanguage(Locale.UK)
            if (ukResult == TextToSpeech.LANG_MISSING_DATA || ukResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            _currentLanguageName.value = "English"
        }

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        tts?.speak(formattedText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

object SoundFxGenerator {

    @Volatile
    var isSoundEffectsEnabled: Boolean = true

    /**
     * Pleasant rising two-tone chime when microphone activates (523Hz C5 -> 784Hz G5)
     */
    fun playActivationChirp() {
        if (!isSoundEffectsEnabled) return
        Thread {
            try {
                val sampleRate = 44100
                val durationMs = 150
                val numSamples = (sampleRate * durationMs / 1000)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = if (progress < 0.5) 523.25 else 783.99
                    val segProgress = (progress * 2) % 1.0
                    val envelope = sin(Math.PI * segProgress)
                    val sample = sin(2.0 * Math.PI * freq * i / sampleRate) * envelope * 0.35
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                // Ignore audio generation issues
            }
        }.start()
    }

    /**
     * Sweet affirmative confirmation pop chime when command is captured (880Hz A5 with smooth decay)
     */
    fun playAcknowledgeBeep() {
        if (!isSoundEffectsEnabled) return
        Thread {
            try {
                val sampleRate = 44100
                val durationMs = 110
                val numSamples = (sampleRate * durationMs / 1000)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val envelope = kotlin.math.exp(-3.5 * progress) * sin(Math.PI * progress)
                    val sample = sin(2.0 * Math.PI * 880.0 * i / sampleRate) * envelope * 0.35
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                // Ignore audio generation issues
            }
        }.start()
    }

    /**
     * Warm, friendly 3-note arrival chime (C5 -> E5 -> G5)
     */
    fun playResponseArrivalChime() {
        if (!isSoundEffectsEnabled) return
        Thread {
            try {
                val sampleRate = 44100
                val durationMs = 210
                val numSamples = (sampleRate * durationMs / 1000)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = when {
                        progress < 0.33 -> 523.25 // C5
                        progress < 0.66 -> 659.25 // E5
                        else -> 783.99           // G5
                    }
                    val segProgress = (progress * 3.0) % 1.0
                    val envelope = sin(Math.PI * segProgress)
                    val sample = sin(2.0 * Math.PI * freq * i / sampleRate) * envelope * 0.3
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                // Ignore audio generation issues
            }
        }.start()
    }

    fun playRoboticVocoderChirp() {
        playResponseArrivalChime()
    }

    /**
     * Triumphant harmonic protocol chime
     */
    fun playProtocolAlert() {
        if (!isSoundEffectsEnabled) return
        Thread {
            try {
                val sampleRate = 44100
                val durationMs = 240
                val numSamples = (sampleRate * durationMs / 1000)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = if (progress < 0.5) 659.25 else 987.77
                    val envelope = sin(Math.PI * (progress * 2 % 1.0))
                    val sample = (sin(2.0 * Math.PI * freq * i / sampleRate) * 0.6 +
                                 sin(2.0 * Math.PI * (freq * 1.5) * i / sampleRate) * 0.4) * envelope * 0.35
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                // Ignore audio generation issues
            }
        }.start()
    }

    /**
     * Gentle warning tone (440Hz -> 330Hz)
     */
    fun playWarningAlarm() {
        if (!isSoundEffectsEnabled) return
        Thread {
            try {
                val sampleRate = 44100
                val durationMs = 240
                val numSamples = (sampleRate * durationMs / 1000)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = if (progress < 0.5) 440.0 else 330.0
                    val envelope = sin(Math.PI * (progress * 2 % 1.0))
                    val sample = sin(2.0 * Math.PI * freq * i / sampleRate) * envelope * 0.35
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                // Ignore audio generation issues
            }
        }.start()
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2.coerceAtLeast(minBufferSize))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        Thread.sleep(450)
        track.release()
    }
}
