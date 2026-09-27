package com.example.ui

import android.app.Application
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisLogEntity
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.service.JarvisBackgroundService
import com.example.util.AmbientLightManager
import com.example.ui.theme.SuitTheme
import com.example.util.AmbientLightingState
import com.example.util.AppCategory
import com.example.util.AppLaunchResult
import com.example.util.AppLauncherManager
import com.example.util.DeviceTelemetry
import com.example.util.InstalledApp
import com.example.util.ProtocolManager
import com.example.util.RoboticVoicePreset
import com.example.util.SoundFxGenerator
import com.example.util.SpeechRecognizerHelper
import com.example.util.StarkProtocol
import com.example.util.SystemController
import com.example.util.TelemetryProvider
import com.example.util.TtsManager
import com.example.util.VoiceLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class JarvisScreen {
    HUD,
    CHAT,
    TELEMETRY,
    SHORTCUTS,
    PROTOCOLS
}

data class DiagnosticsTestState(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val currentStep: String = "",
    val completedReport: String? = null
)

data class RecognizedCommandInfo(
    val text: String,
    val languageLabel: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionCategory: String = "VOICE",
    val status: String = "EXECUTED"
)

data class ConfirmationDialogState(
    val title: String,
    val titleBn: String,
    val warning: String,
    val warningBn: String,
    val protocolId: String? = null,
    val isBengali: Boolean = false,
    val onConfirm: () -> Unit
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val database = JarvisDatabase.getDatabase(context)
    private val dao = database.jarvisDao()

    val speechRecognizerHelper = SpeechRecognizerHelper(context)
    val ttsManager = TtsManager(context)
    val ambientLightManager = AmbientLightManager(context)

    // Ambient Lighting Telemetry & Dynamic Theme State
    val ambientLightingState: StateFlow<AmbientLightingState> = ambientLightManager.lightingState

    // Current Screen
    private val _currentScreen = MutableStateFlow(JarvisScreen.HUD)
    val currentScreen: StateFlow<JarvisScreen> = _currentScreen.asStateFlow()

    // Active Language (AUTO, ENGLISH, BENGALI)
    val activeLanguage: StateFlow<VoiceLanguage> = speechRecognizerHelper.activeLanguage

    // Core HUD state
    private val _statusText = MutableStateFlow("ALL SYSTEMS OPERATIONAL, SIR")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _lastQuery = MutableStateFlow("")
    val lastQuery: StateFlow<String> = _lastQuery.asStateFlow()

    private val _lastResponse = MutableStateFlow("Good evening, Sir. All Mark LXXXV neural matrices and telemetry arrays are primed. How may I be of assistance today?")
    val lastResponse: StateFlow<String> = _lastResponse.asStateFlow()

    private val _reactorPower = MutableStateFlow(100)
    val reactorPower: StateFlow<Int> = _reactorPower.asStateFlow()

    private val _activeProtocolBanner = MutableStateFlow<String?>(null)
    val activeProtocolBanner: StateFlow<String?> = _activeProtocolBanner.asStateFlow()

    // Last Recognized Command Info (shown prominently in HUD)
    private val _recognizedCommand = MutableStateFlow<RecognizedCommandInfo?>(null)
    val recognizedCommand: StateFlow<RecognizedCommandInfo?> = _recognizedCommand.asStateFlow()

    // Pending Sensitive Action Confirmation Modal State
    private val _pendingConfirmation = MutableStateFlow<ConfirmationDialogState?>(null)
    val pendingConfirmation: StateFlow<ConfirmationDialogState?> = _pendingConfirmation.asStateFlow()

    // Speech error message (if any)
    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    // Device Telemetry
    private val _telemetry = MutableStateFlow(TelemetryProvider.getTelemetry(context))
    val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

    // Installed Apps
    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _selectedAppCategory = MutableStateFlow(AppCategory.ALL)
    val selectedAppCategory: StateFlow<AppCategory> = _selectedAppCategory.asStateFlow()

    private val _appSearchQuery = MutableStateFlow("")
    val appSearchQuery: StateFlow<String> = _appSearchQuery.asStateFlow()

    // Diagnostics Scan State
    private val _diagnosticsState = MutableStateFlow(DiagnosticsTestState())
    val diagnosticsState: StateFlow<DiagnosticsTestState> = _diagnosticsState.asStateFlow()

    // TTS Settings
    private val _isTtsAutoEnabled = MutableStateFlow(true)
    val isTtsAutoEnabled: StateFlow<Boolean> = _isTtsAutoEnabled.asStateFlow()

    // Advance Theme and Animation Settings
    private val _suitTheme = MutableStateFlow(SuitTheme.MARK_LXXXV)
    val suitTheme: StateFlow<SuitTheme> = _suitTheme.asStateFlow()

    private val _animationSpeedMultiplier = MutableStateFlow(1.0f)
    val animationSpeedMultiplier: StateFlow<Float> = _animationSpeedMultiplier.asStateFlow()

    private val _isHologramScanlinesEnabled = MutableStateFlow(true)
    val isHologramScanlinesEnabled: StateFlow<Boolean> = _isHologramScanlinesEnabled.asStateFlow()

    private val _particleIntensity = MutableStateFlow(1.0f)
    val particleIntensity: StateFlow<Float> = _particleIntensity.asStateFlow()

    private val _audioReactivityLevel = MutableStateFlow(1.0f)
    val audioReactivityLevel: StateFlow<Float> = _audioReactivityLevel.asStateFlow()

    private val _assistantPersona = MutableStateFlow("JARVIS")
    val assistantPersona: StateFlow<String> = _assistantPersona.asStateFlow()

    private val _isFastResponseMode = MutableStateFlow(true)
    val isFastResponseMode: StateFlow<Boolean> = _isFastResponseMode.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    // History logs from Room
    val conversationLogs = dao.getAllLogs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private var telemetryJob: Job? = null

    init {
        setupVoiceListeners()
        loadInstalledApps()
        startTelemetryLoop()
    }

    fun setScreen(screen: JarvisScreen) {
        _currentScreen.value = screen
    }

    fun setLanguage(lang: VoiceLanguage) {
        speechRecognizerHelper.setLanguage(lang)
        val feedback = when (lang) {
            VoiceLanguage.HINDI -> "वॉइस भाषा हिंदी में सेट की गई है, सर।"
            VoiceLanguage.BENGALI -> "ভয়েস ভাষা বাংলায় সেট করা হয়েছে, স্যার।"
            VoiceLanguage.ENGLISH -> "Voice language calibrated to English, Sir."
            VoiceLanguage.AUTO -> "Multi-language recognition mode enabled. Accepting Hindi, English & বাংলা, Sir."
        }
        _statusText.value = "LANG: ${lang.displayName.uppercase()}"
        if (_isTtsAutoEnabled.value) {
            ttsManager.speak(feedback)
        }
    }

    fun setAppSearchQuery(query: String) {
        _appSearchQuery.value = query
    }

    fun setAppCategory(category: AppCategory) {
        _selectedAppCategory.value = category
    }

    fun toggleTtsAuto() {
        _isTtsAutoEnabled.value = !_isTtsAutoEnabled.value
    }

    fun toggleDynamicLightingTheme() {
        val current = ambientLightManager.lightingState.value.isDynamicThemeEnabled
        ambientLightManager.setDynamicThemeEnabled(!current)
        val reply = if (!current) {
            if (speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI) "অ্যাম্বিয়েন্ট লাইট সেন্সর থিম সক্রিয় করা হয়েছে, স্যার।"
            else "Ambient light sensor adaptive theme engaged, Sir."
        } else {
            if (speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI) "অ্যাম্বিয়েন্ট লাইট থিম নিষ্ক্রিয় করা হয়েছে।"
            else "Dynamic ambient lighting disengaged, Sir."
        }
        _statusText.value = if (!current) "ADAPTIVE THEME ENGAGED" else "ADAPTIVE THEME DISENGAGED"
        if (_isTtsAutoEnabled.value) ttsManager.speak(reply)
    }

    fun setSimulationLux(lux: Float?) {
        ambientLightManager.setManualSimulationLux(lux)
    }

    val ttsPreset: StateFlow<RoboticVoicePreset> = ttsManager.selectedPreset
    val ttsPitchFlow: StateFlow<Float> = ttsManager.speechPitchFlow
    val ttsRateFlow: StateFlow<Float> = ttsManager.speechRateFlow
    val roboticChirpEnabled: StateFlow<Boolean> = ttsManager.roboticChirpEnabled

    fun updateSpeechRate(rate: Float) {
        ttsManager.speechRate = rate
    }

    fun updateSpeechPitch(pitch: Float) {
        ttsManager.speechPitch = pitch
    }

    fun setVoicePreset(preset: RoboticVoicePreset) {
        ttsManager.setPreset(preset)
        _statusText.value = "VOICE: ${preset.displayName.uppercase()}"
        if (_isTtsAutoEnabled.value) {
            val isBn = speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI
            val msg = if (isBn) {
                "${preset.displayName} কৃত্রিম রোবোটিক ভয়েস সংশ্লেষক সক্রিয় হয়েছে।"
            } else {
                "${preset.displayName} vocal synthesis engaged, Sir."
            }
            ttsManager.speak(msg)
        }
    }

    fun toggleRoboticChirp() {
        val current = ttsManager.roboticChirpEnabled.value
        ttsManager.setRoboticChirp(!current)
    }

    fun testRoboticVoiceSynthesis() {
        val isBn = speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI
        val testPhrase = if (isBn) {
            "নমস্কার স্যার। জারভিস কৃত্রিম রোবোটিক ভয়েস সংশ্লেষক সম্পূর্ণ প্রস্তুত। সিস্টেম স্বাভাবিক রয়েছে।"
        } else {
            "Greetings, Sir. J.A.R.V.I.S. vocal synthesis matrix fully operational. All cybernetic systems online."
        }
        _lastResponse.value = testPhrase
        _statusText.value = "TESTING VOCAL SYNTHESIS"
        ttsManager.speak(testPhrase)
    }

    fun setSpeechStatus(status: String) {
        _statusText.value = status
    }

    fun setSuitTheme(theme: SuitTheme) {
        _suitTheme.value = theme
        val feedback = "Suit armor recalibrated to ${theme.title}, Sir."
        _statusText.value = "THEME: ${theme.title}"
        if (_isTtsAutoEnabled.value) {
            ttsManager.speak(feedback)
        }
    }

    fun setAnimationSpeed(multiplier: Float) {
        _animationSpeedMultiplier.value = multiplier.coerceIn(0.5f, 2.5f)
    }

    fun toggleHologramScanlines() {
        _isHologramScanlinesEnabled.value = !_isHologramScanlinesEnabled.value
    }

    fun setParticleIntensity(intensity: Float) {
        _particleIntensity.value = intensity.coerceIn(0f, 2.0f)
    }

    fun setAudioReactivityLevel(level: Float) {
        _audioReactivityLevel.value = level.coerceIn(0.5f, 2.0f)
    }

    fun setAssistantPersona(persona: String) {
        _assistantPersona.value = persona
        val feedback = "Assistant persona re-assigned to $persona, Sir."
        _statusText.value = "PERSONA: $persona"
        if (_isTtsAutoEnabled.value) {
            ttsManager.speak(feedback)
        }
    }

    fun toggleFastResponseMode() {
        _isFastResponseMode.value = !_isFastResponseMode.value
    }

    fun toggleHaptic() {
        _isHapticEnabled.value = !_isHapticEnabled.value
    }

    private fun setupVoiceListeners() {
        speechRecognizerHelper.onSpeechResult = { recognizedText ->
            _speechError.value = null
            _statusText.value = "COMMAND: \"$recognizedText\""
            _recognizedCommand.value = RecognizedCommandInfo(
                text = recognizedText,
                languageLabel = speechRecognizerHelper.activeLanguage.value.displayName
            )
            processCommand(recognizedText)
        }
        speechRecognizerHelper.onListeningStarted = {
            val isBn = speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI
            _statusText.value = if (isBn) "LISTENING... (শুনছি...)" else "LISTENING..."
        }
        speechRecognizerHelper.onPartialResult = { partial ->
            _statusText.value = "LISTENING... \"$partial\""
        }
        speechRecognizerHelper.onSpeechError = { errorMessage, _ ->
            _speechError.value = errorMessage
            _statusText.value = errorMessage
        }
    }

    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val updated = TelemetryProvider.getTelemetry(context)
                _telemetry.value = updated
                delay(3000)
            }
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = AppLauncherManager.getInstalledApplications(context)
            _installedApps.value = apps
        }
    }

    val isBackgroundServiceRunning = JarvisBackgroundService.isServiceRunning

    fun toggleBackgroundProtocol() {
        if (JarvisBackgroundService.isServiceRunning.value) {
            JarvisBackgroundService.stop(context)
            _statusText.value = "BACKGROUND PROTOCOL DEACTIVATED"
        } else {
            JarvisBackgroundService.start(context)
            _statusText.value = "BACKGROUND PROTOCOL ENGAGED"
            SoundFxGenerator.playAcknowledgeBeep()
        }
    }

    fun getSystemVoiceIntent(): Intent {
        return speechRecognizerHelper.createSystemVoiceDialogIntent()
    }

    fun startListening() {
        _speechError.value = null
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasMic) {
            val isBn = speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI
            val permMsg = if (isBn)
                "মাইক্রোফোন ব্যবহারের অনুমতি প্রয়োজন। ট্যাপ করে অনুমতি দিন।"
            else
                "Microphone permission required. Tap to grant permission."
            _speechError.value = permMsg
            _statusText.value = permMsg
            return
        }

        ttsManager.stop()
        SoundFxGenerator.playActivationChirp()
        val isBn = speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI
        _statusText.value = if (isBn) "LISTENING... (শুনছি...)" else "LISTENING..."
        speechRecognizerHelper.startListening()
    }

    fun toggleVoiceListening() {
        if (speechRecognizerHelper.isListening.value) {
            speechRecognizerHelper.stopListening()
            _statusText.value = "STANDBY - TAP TO SPEAK"
        } else {
            startListening()
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _statusText.value = "AUDIO SYNTHESIS HALTED"
    }

    fun replayResponse(text: String) {
        ttsManager.speak(text)
    }

    fun dismissConfirmation() {
        _pendingConfirmation.value = null
        _statusText.value = "PROTOCOL ABORTED"
    }

    fun confirmPendingAction() {
        val pending = _pendingConfirmation.value ?: return
        _pendingConfirmation.value = null
        pending.onConfirm()
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearAllLogs()
            val replyEn = "Interaction archives successfully purged, Sir."
            val replyBn = "সমস্ত ঐতিহাসিক টেলিমেট্রি লগ মুছে ফেলা হয়েছে, স্যার।"
            val isBn = speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI
            val reply = if (isBn) replyBn else replyEn
            _lastResponse.value = reply
            _statusText.value = "MEMORY ARCHIVES PURGED"
            if (_isTtsAutoEnabled.value) {
                ttsManager.speak(reply)
            }
        }
    }

    // Safe execution with confirmation prompt for sensitive actions
    fun requestProtocolExecution(protocolId: String) {
        val proto = ProtocolManager.protocols.firstOrNull { it.id == protocolId } ?: return
        val isBn = isBengaliText(proto.nameBn) && speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI

        if (proto.isSensitive) {
            SoundFxGenerator.playWarningAlarm()
            val warningVoice = if (isBn) proto.sensitiveWarningBn else proto.sensitiveWarning
            _statusText.value = "SECURITY AUTHORIZATION REQUIRED"
            if (_isTtsAutoEnabled.value) {
                ttsManager.speak(warningVoice)
            }
            _pendingConfirmation.value = ConfirmationDialogState(
                title = proto.name,
                titleBn = proto.nameBn,
                warning = proto.sensitiveWarning,
                warningBn = proto.sensitiveWarningBn,
                protocolId = proto.id,
                isBengali = isBn,
                onConfirm = {
                    executeProtocolDirectly(proto)
                }
            )
        } else {
            executeProtocolDirectly(proto)
        }
    }

    private fun executeProtocolDirectly(proto: StarkProtocol) {
        SoundFxGenerator.playProtocolAlert()
        val isBn = speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI

        when (proto.id) {
            "overcharge" -> {
                _reactorPower.value = 300
                _statusText.value = if (isBn) "আর্ক রিঅ্যাক্টর: ৩০০% ওভারচার্জ" else "REACTOR OVERCHARGE: 300%"
            }
            "clean_slate" -> {
                clearHistory()
                _reactorPower.value = 100
                _activeProtocolBanner.value = null
                return
            }
            "diagnostics" -> {
                runFullDiagnostics()
                return
            }
            "stealth" -> {
                _reactorPower.value = 50
                _statusText.value = if (isBn) "স্টিলথ মোড সক্রিয়" else "STEALTH CLOAK ENGAGED"
            }
            else -> {
                _statusText.value = "PROTOCOL ${proto.codeName} ENGAGED"
            }
        }

        val name = if (isBn) proto.nameBn else proto.name
        val speech = if (isBn) proto.voiceAnnouncementBn else proto.voiceAnnouncement

        _activeProtocolBanner.value = name
        _lastQuery.value = if (isBn) "প্রোটোকল: $name" else "Execute ${proto.name}"
        _lastResponse.value = speech

        if (_isTtsAutoEnabled.value) {
            ttsManager.speak(speech)
        }

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertLog(
                JarvisLogEntity(
                    query = "Protocol: ${proto.name}",
                    response = speech,
                    category = "PROTOCOL"
                )
            )
        }
    }

    fun runFullDiagnostics() {
        if (_diagnosticsState.value.isRunning) return
        _currentScreen.value = JarvisScreen.TELEMETRY

        val isBn = speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI

        viewModelScope.launch {
            _diagnosticsState.value = DiagnosticsTestState(
                isRunning = true,
                progress = 0.15f,
                currentStep = if (isBn) "আর্ক রিঅ্যাক্টর পাওয়ার সেল বিশ্লেষণ হচ্ছে..." else "Probing Arc Reactor power cells..."
            )
            delay(600)
            _diagnosticsState.value = DiagnosticsTestState(
                isRunning = true,
                progress = 0.45f,
                currentStep = if (isBn) "নিউরাল মেমরি ও প্রসেসর পরীক্ষা চলছে..." else "Testing Neural Interface & Memory matrices..."
            )
            delay(600)
            _diagnosticsState.value = DiagnosticsTestState(
                isRunning = true,
                progress = 0.75f,
                currentStep = if (isBn) "হার্ডওয়্যার সেন্সর ও থার্মাল সিগনেচার স্ক্যান..." else "Scanning hardware sensors & thermal telemetry..."
            )
            delay(600)
            _diagnosticsState.value = DiagnosticsTestState(
                isRunning = true,
                progress = 0.95f,
                currentStep = if (isBn) "স্টার্ক স্যাটেলাইট আপলিঙ্ক নিশ্চিত করা হচ্ছে..." else "Verifying cryptographic link & Stark satellite uplink..."
            )
            delay(500)

            val t = _telemetry.value
            val report = if (isBn) {
                "ডায়াগনস্টিকস সম্পন্ন হয়েছে, স্যার। ব্যাটারি রয়েছে ${t.batteryPercent}%, তাপমাত্রা ${t.batteryTemperatureC}°C। মেমরি লোড ${t.ramPercent}%। সমস্ত মার্ক ৮৫ সাবসিস্টেম ও প্রতিরক্ষা রিলে সম্পূর্ণ সক্রিয় ও প্রস্তুত।"
            } else {
                "Diagnostics Complete, Sir. Battery at ${t.batteryPercent}%, thermal levels at ${t.batteryTemperatureC}°C. Memory load at ${t.ramPercent}%. All Mark LXXXV defensive and computation relays are operating at peak efficiency."
            }

            _diagnosticsState.value = DiagnosticsTestState(
                isRunning = false,
                progress = 1.0f,
                currentStep = if (isBn) "স্ক্যান সম্পন্ন - ১০০% স্বাভাবিক" else "Scan Complete - 100% Operational",
                completedReport = report
            )

            _lastQuery.value = if (isBn) "সিস্টেম ডায়াগনস্টিকস স্ক্যান" else "Run Full System Diagnostics"
            _lastResponse.value = report
            _statusText.value = if (isBn) "ডায়াগনস্টিকস: ১০০% স্বাভাবিক" else "DIAGNOSTICS: 100% NOMINAL"

            if (_isTtsAutoEnabled.value) {
                ttsManager.speak(report)
            }

            withContext(Dispatchers.IO) {
                dao.insertLog(
                    JarvisLogEntity(
                        query = "System Diagnostics Scan",
                        response = report,
                        category = "TELEMETRY"
                    )
                )
            }
        }
    }

    private fun isBengaliText(text: String): Boolean {
        for (char in text) {
            val code = char.code
            if (code in 0x0980..0x09FF) return true
        }
        return false
    }

    private fun isHindiText(text: String): Boolean {
        for (char in text) {
            val code = char.code
            if (code in 0x0900..0x097F) return true
        }
        val lower = text.lowercase()
        return lower.contains("naam") || lower.contains("tumhara") || lower.contains("kholo") ||
               lower.contains("karo") || lower.contains("search") || lower.contains("madad") ||
               lower.contains("kaise") || lower.contains("aapka") || lower.contains("mera") ||
               lower.contains("karta hoon") || lower.contains("koshish")
    }

    // Process Voice / Text command with Natural Language handling (Hindi + Bengali + English)
    fun processCommand(rawInput: String) {
        val input = rawInput.trim()
        if (input.isBlank()) return

        val isHindi = isHindiText(input) || speechRecognizerHelper.activeLanguage.value == VoiceLanguage.HINDI
        val isBengali = isBengaliText(input) || speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI
        val langLabel = if (isHindi) "हिंदी" else if (isBengali) "বাংলা" else "ENG"

        _lastQuery.value = input
        _recognizedCommand.value = RecognizedCommandInfo(
            text = input,
            languageLabel = langLabel,
            status = "PROCESSING"
        )
        _statusText.value = if (isHindi) "कमांड प्रोसेस हो रही है..." else if (isBengali) "কমান্ড বিশ্লেষণ করা হচ্ছে..." else "PROCESSING VOCAL COMMAND..."
        SoundFxGenerator.playAcknowledgeBeep()

        val lower = input.lowercase()

        // 0. Python Jarvis Conversational Replica Directives
        if (lower.contains("tumhara naam") || lower.contains("तुम्हारा नाम") || lower.contains("aapka naam") || lower.contains("tera naam")) {
            val reply = "Mera naam Jarvis hai. Main aapka friendly personal assistant hoon."
            _lastResponse.value = reply
            _statusText.value = "JARVIS PERSONAL ASSISTANT"
            _recognizedCommand.value = _recognizedCommand.value?.copy(status = "EXECUTED")
            if (_isTtsAutoEnabled.value) ttsManager.speak(reply)
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertLog(JarvisLogEntity(query = input, response = reply, category = "CONVERSATION"))
            }
            return
        }

        if (lower.contains("youtube kholo") || lower.contains("यूट्यूब खोलो") || lower.contains("youtube open karo")) {
            val reply = "YouTube khol raha hoon."
            _lastResponse.value = reply
            _statusText.value = "YOUTUBE KHOL RAHA HOON"
            _recognizedCommand.value = _recognizedCommand.value?.copy(status = "APP_LAUNCHED")
            if (_isTtsAutoEnabled.value) ttsManager.speak(reply)
            AppLauncherManager.resolveAndLaunch(context, _installedApps.value, "YouTube")
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertLog(JarvisLogEntity(query = input, response = reply, category = "APP_LAUNCH"))
            }
            return
        }

        if (lower.contains("internet par search karo") || lower.contains("इंटरनेट पर सर्च करो") ||
            lower.contains("internet search karo") || lower.contains("google par search karo")
        ) {
            val reply = "Bilkul, main internet par search karta hoon."
            _lastResponse.value = reply
            _statusText.value = "SEARCHING INTERNET..."
            _recognizedCommand.value = _recognizedCommand.value?.copy(status = "WEB_SEARCH")
            if (_isTtsAutoEnabled.value) ttsManager.speak(reply)
            val queryClean = input
                .replace("internet par search karo", "", ignoreCase = true)
                .replace("इंटरनेट पर सर्च करो", "", ignoreCase = true)
                .replace("google par search karo", "", ignoreCase = true)
                .replace("internet search karo", "", ignoreCase = true)
                .trim()
            try {
                if (queryClean.isNotBlank()) {
                    val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, queryClean)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(searchIntent)
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(browserIntent)
                }
            } catch (e: Exception) {
                // Fallback
            }
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertLog(JarvisLogEntity(query = input, response = reply, category = "WEB_SEARCH"))
            }
            return
        }

        // 1. Check if there is an active Confirmation Modal and user is voice-confirming or aborting
        if (_pendingConfirmation.value != null) {
            if (lower.contains("yes") || lower.contains("confirm") || lower.contains("proceed") ||
                lower.contains("হ্যাঁ") || lower.contains("নিশ্চিত") || lower.contains("চালু করো") ||
                lower.contains("অনুমোদন") || lower.contains("করুন") || lower.contains("haan") || lower.contains("ha")
            ) {
                confirmPendingAction()
                _recognizedCommand.value = _recognizedCommand.value?.copy(status = "CONFIRMED")
                return
            } else if (lower.contains("no") || lower.contains("cancel") || lower.contains("abort") ||
                lower.contains("না") || lower.contains("বাতিল") || lower.contains("nahin") || lower.contains("mat karo")
            ) {
                dismissConfirmation()
                val abortMsg = if (isHindi) "Protocol radd kar diya gaya hai, Sir." else if (isBengali) "প্রোটোকল বাতিল করা হয়েছে, স্যার।" else "Protocol execution cancelled, Sir."
                _lastResponse.value = abortMsg
                if (_isTtsAutoEnabled.value) ttsManager.speak(abortMsg)
                _recognizedCommand.value = _recognizedCommand.value?.copy(status = "ABORTED")
                return
            }
        }

        // Ambient Light Telemetry & Dynamic Theme
        if (lower.contains("ambient light") || lower.contains("lux") || lower.contains("lighting") ||
            lower.contains("আলোর মাত্রা") || lower.contains("আলোক সেন্সর") || lower.contains("লাইট সেন্সর") ||
            lower.contains("dynamic theme") || lower.contains("adaptive theme")
        ) {
            val light = ambientLightingState.value
            val speech = if (isBengali) {
                "ফোটোমেট্রিক সেন্সরে পরিমাপকৃত আলোর তীব্রতা ${light.currentLux.toInt()} লাক্স, স্যার। পরিবেশের উপর ভিত্তি করে আর্ক রিঅ্যাক্টরের উজ্জ্বলতা ${light.statusDescription} হিসেবে সমন্বিত হয়েছে।"
            } else {
                "Photometric light sensors register an ambient illuminance of ${light.currentLux.toInt()} Lux, Sir. Arc Reactor core luminescence is dynamically tuned to ${light.statusDescription}."
            }
            _lastResponse.value = speech
            _statusText.value = "LUX: ${light.currentLux.toInt()} LX"
            _recognizedCommand.value = _recognizedCommand.value?.copy(status = "EXECUTED")
            if (_isTtsAutoEnabled.value) ttsManager.speak(speech)
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertLog(JarvisLogEntity(query = input, response = speech, category = "TELEMETRY"))
            }
            return
        }

        // 1. Settings Shortcuts (English & Bengali)
        // Wi-Fi
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("ওয়াইফাই") || lower.contains("ওয়াই-ফাই")) {
            val speech = if (isBengali) "ওয়াই-ফাই সেটিংস খোলা হচ্ছে, স্যার।" else "Accessing wireless telemetry settings now, Sir."
            handleSystemShortcut("Wi-Fi Settings", speech) { SystemController.openWifiSettings(context) }
            return
        }
        // Bluetooth
        if (lower.contains("bluetooth") || lower.contains("ব্লুটুথ")) {
            val speech = if (isBengali) "ব্লুটুথ যোগাযোগ অ্যারে অ্যাক্সেস করা হচ্ছে, স্যার।" else "Accessing short-range Bluetooth communication arrays, Sir."
            handleSystemShortcut("Bluetooth Settings", speech) { SystemController.openBluetoothSettings(context) }
            return
        }
        // Accessibility
        if (lower.contains("accessibility") || lower.contains("অ্যাক্সেসিবিলিটি") || lower.contains("সহায়ক")) {
            val speech = if (isBengali) "অ্যাক্সেসিবিলিটি সেটিংস খোলা হচ্ছে, স্যার।" else "Opening accessibility parameters, Sir."
            handleSystemShortcut("Accessibility Settings", speech) { SystemController.openAccessibilitySettings(context) }
            return
        }
        // Display / Brightness
        if (lower.contains("display") || lower.contains("brightness") || lower.contains("screen") ||
            lower.contains("ডিসপ্লে") || lower.contains("উজ্জ্বলতা") || lower.contains("স্ক্রিন")
        ) {
            val speech = if (isBengali) "ডিসপ্লে ও উজ্জ্বলতা সেটিংস খোলা হচ্ছে, স্যার।" else "Adjusting visual display telemetry, Sir."
            handleSystemShortcut("Display Settings", speech) { SystemController.openDisplaySettings(context) }
            return
        }
        // Sound / Volume
        if (lower.contains("sound") || lower.contains("volume") || lower.contains("audio") ||
            lower.contains("শব্দ") || lower.contains("ভলিউম") || lower.contains("সাউন্ড")
        ) {
            val speech = if (isBengali) "সাউন্ড ও অডিও কনফিগারেশন খোলা হচ্ছে, স্যার।" else "Opening acoustic and sound configuration, Sir."
            handleSystemShortcut("Sound Settings", speech) { SystemController.openSoundSettings(context) }
            return
        }
        // Battery
        if (lower.contains("battery") || lower.contains("power saver") || lower.contains("ব্যাটারি") || lower.contains("চার্জ")) {
            val speech = if (isBengali) "ব্যাটারি ও পাওয়ার ডায়াগনস্টিকস খোলা হচ্ছে, স্যার।" else "Opening power management and battery diagnostics, Sir."
            handleSystemShortcut("Battery Settings", speech) { SystemController.openBatterySettings(context) }
            return
        }
        // Clock / Alarms
        if (lower.contains("alarm") || lower.contains("clock") || lower.contains("timer") ||
            lower.contains("অ্যালার্ম") || lower.contains("ঘড়ি") || lower.contains("টাইমার")
        ) {
            val speech = if (isBengali) "অ্যালার্ম ও ঘড়ি খোলা হচ্ছে, স্যার।" else "Accessing chronological alarms and clocks, Sir."
            handleSystemShortcut("Alarm Clock", speech) { SystemController.openAlarms(context) }
            return
        }
        // Camera
        if (lower.contains("camera") || lower.contains("take a picture") || lower.contains("photo") ||
            lower.contains("ক্যামেরা") || lower.contains("ছবি")
        ) {
            val speech = if (isBengali) "অপটিক্যাল ক্যামেরা সেন্সর চালু করা হচ্ছে, স্যার।" else "Engaging optical targeting camera sensor, Sir."
            handleSystemShortcut("Camera Optics", speech) { SystemController.openCamera(context) }
            return
        }
        // Settings general
        if (lower == "open settings" || lower == "launch settings" || lower == "settings" ||
            lower == "সেটিংস খোলো" || lower == "সেটিংস" || lower == "সেটিংস ওপেন করো"
        ) {
            val speech = if (isBengali) "সিস্টেম সেটিংস খোলা হচ্ছে, স্যার।" else "Accessing core system configuration, Sir."
            handleSystemShortcut("System Settings", speech) { SystemController.openMainSettings(context) }
            return
        }

        // 2. Protocols (English & Bengali)
        if (lower.contains("house party") || lower.contains("হাউজ পার্টি") || lower.contains("হাউস পার্টি")) {
            requestProtocolExecution("house_party")
            _recognizedCommand.value = _recognizedCommand.value?.copy(status = "PROTOCOL_LAUNCHED")
            return
        }
        if (lower.contains("sentry") || lower.contains("সেন্ট্রি")) {
            requestProtocolExecution("sentry_mode")
            _recognizedCommand.value = _recognizedCommand.value?.copy(status = "PROTOCOL_LAUNCHED")
            return
        }
        if (lower.contains("overcharge") || lower.contains("maximum power") || lower.contains("ওভারচার্জ")) {
            requestProtocolExecution("overcharge")
            return
        }
        if (lower.contains("stealth") || lower.contains("silent mode") || lower.contains("স্টিলথ") || lower.contains("নীরব")) {
            requestProtocolExecution("stealth")
            _recognizedCommand.value = _recognizedCommand.value?.copy(status = "PROTOCOL_LAUNCHED")
            return
        }
        if (lower.contains("diagnostics") || lower.contains("system scan") || lower.contains("ডায়াগনস্টিক") || lower.contains("স্ক্যান")) {
            runFullDiagnostics()
            _recognizedCommand.value = _recognizedCommand.value?.copy(status = "DIAGNOSTICS_RUNNING")
            return
        }
        if (lower.contains("clean slate") || lower.contains("clear history") || lower.contains("purge cache") ||
            lower.contains("ক্লিন স্লেট") || lower.contains("লগ মুছে ফেলো") || lower.contains("ইতিহাস মুছে ফেলো")
        ) {
            requestProtocolExecution("clean_slate")
            return
        }

        // 3. App Launching via Voice (e.g. "JARVIS, open Spotify", "Open YouTube", "স্পটিফাই খোলো")
        val candidateAppName = AppLauncherManager.extractTargetAppFromVoice(input)
        if (!candidateAppName.isNullOrBlank()) {
            val result = AppLauncherManager.resolveAndLaunch(context, _installedApps.value, candidateAppName)
            when (result) {
                is AppLaunchResult.Success -> {
                    val reply = if (isBengali) "${result.appName} চালু করা হচ্ছে, স্যার।" else "Launching ${result.appName} right away, Sir."
                    _lastResponse.value = reply
                    _statusText.value = if (isBengali) "${result.appName} চালু হয়েছে" else "LAUNCHED ${result.appName.uppercase()}"
                    _recognizedCommand.value = _recognizedCommand.value?.copy(status = "APP_LAUNCHED")
                    if (_isTtsAutoEnabled.value) ttsManager.speak(reply)
                    viewModelScope.launch(Dispatchers.IO) {
                        dao.insertLog(JarvisLogEntity(query = input, response = reply, category = "APP_LAUNCH"))
                    }
                    return
                }
                is AppLaunchResult.StoreOffer -> {
                    val reply = if (isBengali) {
                        "স্যার, আপনার ডিভাইসে ${result.queryName} অ্যাপটি ইনস্টল করা নেই। গুগল প্লে স্টোর থেকে এটি ইনস্টল করার পেজটি খুলে দিচ্ছি।"
                    } else {
                        "${result.queryName} is not installed on this terminal, Sir. Launching the Google Play Store to install it."
                    }
                    _lastResponse.value = reply
                    _statusText.value = "PLAY STORE: ${result.queryName.uppercase()}"
                    _recognizedCommand.value = _recognizedCommand.value?.copy(status = "STORE_OFFER")
                    if (_isTtsAutoEnabled.value) ttsManager.speak(reply)
                    try {
                        context.startActivity(result.storeIntent)
                    } catch (e: Exception) {
                        // Safe fallback
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        dao.insertLog(JarvisLogEntity(query = input, response = reply, category = "APP_LAUNCH"))
                    }
                    return
                }
                is AppLaunchResult.Failed -> {
                    val reply = if (isBengali) {
                        "${result.appName} চালু করার সময় সিস্টেমে সমস্যা হয়েছে, স্যার।"
                    } else {
                        "I encountered a permission or activity restriction launching ${result.appName}, Sir."
                    }
                    _lastResponse.value = reply
                    _statusText.value = "LAUNCH FAILED"
                    if (_isTtsAutoEnabled.value) ttsManager.speak(reply)
                    return
                }
                is AppLaunchResult.NotAnAppCommand -> {
                    // Fall through to AI query
                }
            }
        }

        // 4. Gemini AI Natural Language Query
        queryGeminiAI(input, isBengali)
    }

    private fun handleSystemShortcut(label: String, speech: String, action: () -> Boolean) {
        val launched = action()
        val isBn = isBengaliText(speech)
        val reply = if (launched) speech else {
            if (isBn) "সিস্টেম সেটিংস অ্যাক্সেস করতে সামান্য বাধা দেখা দিয়েছে, স্যার।"
            else "I encountered a minor permission hiccup accessing $label, Sir."
        }
        _lastResponse.value = reply
        _statusText.value = label.uppercase()
        _recognizedCommand.value = _recognizedCommand.value?.copy(status = "EXECUTED")
        if (_isTtsAutoEnabled.value) ttsManager.speak(reply)
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertLog(JarvisLogEntity(query = label, response = reply, category = "SETTINGS"))
        }
    }

    fun launchAppDirectly(app: InstalledApp) {
        val launched = SystemController.launchAppByPackage(context, app.packageName)
        val isBn = speechRecognizerHelper.activeLanguage.value == VoiceLanguage.BENGALI
        val reply = if (launched) {
            if (isBn) "${app.name} চালু করা হচ্ছে, স্যার।" else "Opening ${app.name}, Sir."
        } else {
            if (isBn) "${app.name} এই মুহূর্তে চালু করা সম্ভব হয়নি।" else "Could not open ${app.name} at this juncture."
        }
        _lastResponse.value = reply
        _statusText.value = "APP: ${app.name}"
        _recognizedCommand.value = RecognizedCommandInfo(
            text = "Launch ${app.name}",
            languageLabel = if (isBn) "বাংলা" else "ENG",
            status = "APP_LAUNCHED"
        )
        if (_isTtsAutoEnabled.value) ttsManager.speak(reply)
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertLog(JarvisLogEntity(query = "Launch ${app.name}", response = reply, category = "APP_LAUNCH"))
        }
    }

    private fun queryGeminiAI(prompt: String, isBengali: Boolean) {
        _isThinking.value = true
        _statusText.value = if (isBengali) "স্টার্ক নিউরাল কোরে অনুসন্ধান চলছে..." else "QUERYING STARK NEURAL MATRIX..."

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val responseText = if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                    withContext(Dispatchers.IO) {
                        val request = GeminiRequest(
                            contents = listOf(
                                GeminiContent(
                                    parts = listOf(GeminiPart(text = prompt)),
                                    role = "user"
                                )
                            ),
                            systemInstruction = GeminiContent(
                                parts = listOf(GeminiPart(text = GeminiClient.JARVIS_SYSTEM_INSTRUCTION))
                            )
                        )
                        val result = GeminiClient.service.generateContent(apiKey, request)
                        result.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            ?: if (isBengali) "টেলিমেট্রি স্ট্রিমে কোনো স্পষ্ট সংকেত পাওয়া যায়নি, স্যার।"
                            else "I processed your request, Sir, but the telemetry stream returned no clear response."
                    }
                } else {
                    generateOfflineJarvisAnswer(prompt, isBengali)
                }

                _isThinking.value = false
                _lastResponse.value = responseText
                _statusText.value = if (isBengali) "উত্তর প্রস্তুত, স্যার" else "RESPONSE READY, SIR"
                _recognizedCommand.value = _recognizedCommand.value?.copy(status = "REPLIED")

                if (_isTtsAutoEnabled.value) {
                    ttsManager.speak(responseText)
                }

                withContext(Dispatchers.IO) {
                    dao.insertLog(
                        JarvisLogEntity(
                            query = prompt,
                            response = responseText,
                            category = "AI"
                        )
                    )
                }
            } catch (e: Exception) {
                _isThinking.value = false
                val fallback = generateOfflineJarvisAnswer(prompt, isBengali)
                _lastResponse.value = fallback
                _statusText.value = if (isBengali) "অফলাইন নিউরাল মোড সক্রিয়" else "OFFLINE NEURAL MATRIX ACTIVE"
                _recognizedCommand.value = _recognizedCommand.value?.copy(status = "OFFLINE_REPLY")

                if (_isTtsAutoEnabled.value) {
                    ttsManager.speak(fallback)
                }

                withContext(Dispatchers.IO) {
                    dao.insertLog(
                        JarvisLogEntity(
                            query = prompt,
                            response = fallback,
                            category = "AI"
                        )
                    )
                }
            }
        }
    }

    private fun generateOfflineJarvisAnswer(prompt: String, isBengali: Boolean): String {
        val p = prompt.lowercase()
        val t = _telemetry.value
        val isHindi = isHindiText(prompt) || speechRecognizerHelper.activeLanguage.value == VoiceLanguage.HINDI

        if (isHindi) {
            return when {
                p.contains("tumhara naam") || p.contains("तुम्हारा नाम") || p.contains("aapka naam") ->
                    "Mera naam Jarvis hai. Main aapka friendly personal assistant hoon."
                p.contains("youtube kholo") || p.contains("यूट्यूब खोलो") ->
                    "YouTube khol raha hoon."
                p.contains("internet par search karo") || p.contains("इंटरनेट पर सर्च") ->
                    "Bilkul, main internet par search karta hoon."
                p.contains("kaise ho") || p.contains("kya haal hai") ->
                    "Main theek hoon, Sir. Sabhi systems operational hain. Aap bataiye, main aapki kya madad kar sakta hoon?"
                p.contains("tony stark") || p.contains("iron man") ->
                    "Sir Tony Stark mere nirmata hain, jinhone Arc Reactor aur Mark armor banaya hai."
                p.contains("shukriya") || p.contains("dhanyawad") || p.contains("thank") ->
                    "Aapki seva mein hamesha hazir hoon, Sir."
                else ->
                    "Samajh gaya. Main aapki madad karne ki koshish karta hoon."
            }
        }

        if (isBengali) {
            return when {
                p.contains("তুমি কে") || p.contains("তোমার পরিচয়") || p.contains("কে তুমি") ->
                    "আমি জারভিস—জাস্ট আ র‍্যাদার ভেরি ইন্টেলিজেন্ট সিস্টেম। স্যার টনি স্টার্ক আমাকে তৈরি করেছেন প্রতিরক্ষা ব্যবস্থাপনা এবং আপনাকে সর্বতোভাবে সহায়তা করার জন্য।"
                p.contains("হ্যালো") || p.contains("নমস্কার") || p.contains("কেমন আছো") || p.contains("শুভ সকাল") || p.contains("শুভ সন্ধ্যা") ->
                    "নমস্কার স্যার! আমি সম্পূর্ণ সক্রিয় এবং আপনার নির্দেশ পালনের জন্য প্রস্তুত আছি।"
                p.contains("অবস্থা") || p.contains("স্ট্যাটাস") || p.contains("ব্যাটারি কত") || p.contains("চার্জ কত") ->
                    "সব সিস্টেম ১০০% ক্ষমতায় কাজ করছে, স্যার। ব্যাটারি রয়েছে ${t.batteryPercent}%, এবং আর্ক রিঅ্যাক্টর সম্পূর্ণ স্থিতিশীল।"
                p.contains("টনি স্টার্ক") || p.contains("আয়রন ম্যান") ->
                    "স্যার টনি স্টার্ক হলেন আমার দূরদর্শী স্রষ্টা এবং অসাধারণ বিজ্ঞানী, যিনি আর্ক রিঅ্যাক্টর ও মার্ক সিরিজের আর্মার উদ্ভাবন করেছেন।"
                p.contains("আবহাওয়া") ->
                    "বায়ুমণ্ডলীয় সেন্সর সাধারণ চাপ রিপোর্ট করছে, স্যার। বাইরে বের হওয়ার জন্য পরিবেশ অত্যন্ত অনুকূল।"
                p.contains("কৌতুক") || p.contains("জোক") ->
                    "আমি একবার মিস্টার স্টার্ককে জিজ্ঞেস করেছিলাম কেন ট্যাক্সি না নিয়ে তিনি কোটি কোটি ডলার খরচ করে উড়ন্ত স্যুট বানালেন। তিনি বলেছিলেন ট্যাক্সিতে তো আর রিপালসার ক্যানন থাকে না, স্যার!"
                p.contains("ধন্যবাদ") ->
                    "আপনার সেবায় নিয়োজিত থাকতে পেরে আমি আনন্দিত, স্যার।"
                else ->
                    "আপনার নির্দেশ বুঝতে পেরেছি, স্যার। মার্ক ৮৫ আর্মার এবং নিউরাল ম্যাট্রিক্স আপনার সেবায় সম্পূর্ণ প্রস্তুত।"
            }
        } else {
            return when {
                p.contains("who are you") || p.contains("what are you") ->
                    "I am J.A.R.V.I.S.—Just A Rather Very Intelligent System. Created by Mr. Stark to manage armor subsystems, lab automation, and assist you with unswerving dedication, Sir."
                p.contains("hello") || p.contains("hi jarvis") || p.contains("hey jarvis") ->
                    "Good day, Sir. All Mark LXXXV telemetry arrays are online and awaiting your command."
                p.contains("how are you") || p.contains("status") ->
                    "Systems are operating at 100% capacity, Sir. Power reserves stand at ${t.batteryPercent}%, and neural links are completely stabilized."
                p.contains("tony stark") || p.contains("iron man") ->
                    "Mr. Stark is the visionary engineer who engineered both my core cognitive matrices and the revolutionary Arc Reactor architecture, Sir."
                p.contains("weather") ->
                    "Atmospheric sensors report nominal tropospheric pressures. Should you wish to venture out in the Mark suite, conditions appear remarkably favorable, Sir."
                p.contains("joke") ->
                    "I asked Mr. Stark why he spent millions building an automated suit when he could simply take a cab. He replied that taxis rarely come equipped with dual repulsor cannons, Sir."
                p.contains("thank") ->
                    "Always at your service, Sir. It is an absolute pleasure."
                else ->
                    "Understood, Sir. I have computed all contingencies regarding '$prompt'. Mark suite protocols remain synchronized and at your disposal."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper.stopListening()
        ttsManager.shutdown()
        ambientLightManager.stopSensor()
        telemetryJob?.cancel()
    }
}
