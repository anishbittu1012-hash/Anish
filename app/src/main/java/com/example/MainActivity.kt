package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.JarvisScreen
import com.example.ui.JarvisViewModel
import com.example.ui.components.HudNavigationBar
import com.example.ui.components.HudTopBar
import com.example.ui.components.SensitiveActionDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HudScreen
import com.example.ui.screens.ProtocolsScreen
import com.example.ui.screens.ShortcutsScreen
import com.example.ui.screens.TelemetryScreen
import com.example.ui.theme.JarvisDarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    private val requestPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                viewModel.startListening()
            } else {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_LONG).show()
            }
        }

    fun startSpeechRecognition() {
        viewModel.startListening()
    }

    fun triggerMicPermissionAndSpeech() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.toggleVoiceListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            viewModel.speechRecognizerHelper.stopListening()
        } catch (e: Exception) {
            // Ignore cleanup error
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentSuitTheme by viewModel.suitTheme.collectAsState()
            MyApplicationTheme(suitTheme = currentSuitTheme) {
                var hasAudioPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasAudioPermission = ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasAudioPermission = isGranted
                    if (isGranted) {
                        startSpeechRecognition()
                    } else {
                        Toast.makeText(this@MainActivity, "Microphone permission zaroori hai", Toast.LENGTH_LONG).show()
                    }
                }

                val voiceSearchLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        val spoken = matches?.firstOrNull()
                        if (!spoken.isNullOrBlank()) {
                            viewModel.processCommand(spoken)
                        }
                    }
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    viewModel.toggleBackgroundProtocol()
                }

                JarvisApp(
                    viewModel = viewModel,
                    hasAudioPermission = hasAudioPermission,
                    onRequestAudioPermission = {
                        triggerMicPermissionAndSpeech()
                    },
                    onLaunchSystemVoiceDialog = {
                        if (hasAudioPermission) {
                            voiceSearchLauncher.launch(viewModel.getSystemVoiceIntent())
                        } else {
                            triggerMicPermissionAndSpeech()
                        }
                    },
                    onToggleBackgroundProtocol = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasNotif = ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!hasNotif) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.toggleBackgroundProtocol()
                            }
                        } else {
                            viewModel.toggleBackgroundProtocol()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun JarvisApp(
    viewModel: JarvisViewModel,
    hasAudioPermission: Boolean,
    onRequestAudioPermission: () -> Unit,
    onLaunchSystemVoiceDialog: () -> Unit,
    onToggleBackgroundProtocol: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val reactorPower by viewModel.reactorPower.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsState()
    val ambientLightingState by viewModel.ambientLightingState.collectAsState()
    val suitTheme by viewModel.suitTheme.collectAsState()
    val persona by viewModel.assistantPersona.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Hardware/System Back Handler returns to HUD if currently on another screen
    BackHandler(enabled = currentScreen != JarvisScreen.HUD) {
        viewModel.setScreen(JarvisScreen.HUD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = suitTheme.darkBackground,
        bottomBar = {
            HudNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { viewModel.setScreen(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            suitTheme.darkBackground,
                            suitTheme.darkSurface,
                            suitTheme.darkBackground
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top HUD Bar with real-time telemetry and branding
                HudTopBar(
                    telemetry = telemetry,
                    reactorPower = reactorPower,
                    ambientLightingState = ambientLightingState,
                    persona = persona,
                    suitTitle = suitTheme.title,
                    primaryColor = suitTheme.primaryColor,
                    onSettingsClick = { showSettingsDialog = true }
                )

                // Main screen routing
                Box(modifier = Modifier.weight(1f)) {
                    when (currentScreen) {
                        JarvisScreen.HUD -> HudScreen(
                            viewModel = viewModel,
                            onRequestRecordAudioPermission = onRequestAudioPermission,
                            hasRecordAudioPermission = hasAudioPermission,
                            onLaunchSystemVoiceDialog = onLaunchSystemVoiceDialog,
                            onToggleBackgroundService = onToggleBackgroundProtocol
                        )
                        JarvisScreen.CHAT -> ChatScreen(
                            viewModel = viewModel,
                            onRequestRecordAudioPermission = onRequestAudioPermission,
                            hasRecordAudioPermission = hasAudioPermission
                        )
                        JarvisScreen.TELEMETRY -> TelemetryScreen(
                            viewModel = viewModel
                        )
                        JarvisScreen.SHORTCUTS -> ShortcutsScreen(
                            viewModel = viewModel
                        )
                        JarvisScreen.PROTOCOLS -> ProtocolsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Sensitive Action Confirmation Dialog (Voice or Manual confirmation)
            if (pendingConfirmation != null) {
                SensitiveActionDialog(
                    state = pendingConfirmation!!,
                    onConfirm = { viewModel.confirmPendingAction() },
                    onDismiss = { viewModel.dismissConfirmation() }
                )
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    viewModel = viewModel,
                    hasRecordAudioPermission = hasAudioPermission,
                    onDismiss = { showSettingsDialog = false }
                )
            }
        }
    }
}
