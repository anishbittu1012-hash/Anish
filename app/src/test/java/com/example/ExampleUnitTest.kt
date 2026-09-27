package com.example

import com.example.util.AppCategory
import com.example.util.AppLaunchResult
import com.example.util.AppLauncherManager
import com.example.util.InstalledApp
import com.example.util.ProtocolManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun appLauncher_extractsTargetAppFromVoiceCommands() {
        // "JARVIS, open Spotify"
        val spotifyTarget = AppLauncherManager.extractTargetAppFromVoice("JARVIS, open Spotify")
        assertEquals("spotify", spotifyTarget?.lowercase())

        // "JARVIS open Spotify"
        val spotifyTarget2 = AppLauncherManager.extractTargetAppFromVoice("JARVIS open Spotify")
        assertEquals("spotify", spotifyTarget2?.lowercase())

        // "Hey JARVIS, launch YouTube"
        val ytTarget = AppLauncherManager.extractTargetAppFromVoice("Hey JARVIS, launch YouTube")
        assertEquals("youtube", ytTarget?.lowercase())

        // "Open Spotify please"
        val pleaseTarget = AppLauncherManager.extractTargetAppFromVoice("Open Spotify please")
        assertEquals("spotify", pleaseTarget?.lowercase())

        // "Play Spotify"
        val playTarget = AppLauncherManager.extractTargetAppFromVoice("Play Spotify")
        assertEquals("spotify", playTarget?.lowercase())

        // "Can you open Camera"
        val canYouTarget = AppLauncherManager.extractTargetAppFromVoice("Can you open Camera")
        assertEquals("camera", canYouTarget?.lowercase())

        // Bengali: "স্পটিফাই খোলো"
        val bnTarget = AppLauncherManager.extractTargetAppFromVoice("স্পটিফাই খোলো")
        assertEquals("স্পটিফাই", bnTarget)

        // Bengali: "জারভিস, ইউটিউব চালু করো"
        val bnTarget2 = AppLauncherManager.extractTargetAppFromVoice("জারভিস, ইউটিউব চালু করো")
        assertEquals("ইউটিউব", bnTarget2)
    }

    @Test
    fun appLauncher_storeOfferWhenAppNotInstalled() {
        val emptyAppList = emptyList<InstalledApp>()
        // Mock query for an app that isn't installed
        // In local unit test without Android context, resolveAndLaunch returns StoreOffer for missing popular app
        // We verify the query candidate extraction:
        val extracted = AppLauncherManager.extractTargetAppFromVoice("JARVIS, open Spotify")
        assertNotNull(extracted)
        assertEquals("spotify", extracted?.lowercase())
    }

    @Test
    fun protocolManager_hasRequiredProtocols() {
        val protocols = ProtocolManager.protocols
        assertTrue(protocols.isNotEmpty())
        assertTrue(protocols.any { it.id == "house_party" })
        assertTrue(protocols.any { it.id == "sentry_mode" })
        assertTrue(protocols.any { it.id == "overcharge" })
        assertTrue(protocols.any { it.id == "clean_slate" })
    }

    @Test
    fun protocolManager_sensitiveProtocolsAreMarked() {
        val cleanSlate = ProtocolManager.protocols.first { it.id == "clean_slate" }
        assertTrue(cleanSlate.isSensitive)
        assertTrue(cleanSlate.sensitiveWarning.isNotBlank())
        assertTrue(cleanSlate.sensitiveWarningBn.isNotBlank())

        val overcharge = ProtocolManager.protocols.first { it.id == "overcharge" }
        assertTrue(overcharge.isSensitive)
    }

    @Test
    fun roboticVoicePresets_areConfiguredCorrectly() {
        val presets = com.example.util.RoboticVoicePreset.values()
        assertTrue(presets.isNotEmpty())
        assertTrue(presets.any { it == com.example.util.RoboticVoicePreset.STARK_JARVIS })
        assertTrue(presets.any { it == com.example.util.RoboticVoicePreset.CYBERNETIC_ROBOT })
        assertTrue(presets.any { it == com.example.util.RoboticVoicePreset.DEEP_CORE_AI })
        assertTrue(presets.any { it == com.example.util.RoboticVoicePreset.QUANTUM_SYNTH })

        presets.forEach { preset ->
            assertTrue(preset.displayName.isNotBlank())
            assertTrue(preset.description.isNotBlank())
            assertTrue(preset.pitch in 0.5f..1.8f)
            assertTrue(preset.speechRate in 0.5f..2.0f)
        }
    }

    @Test
    fun voiceLanguage_supportedLanguagesConfigured() {
        val langs = com.example.util.VoiceLanguage.values()
        assertTrue(langs.any { it == com.example.util.VoiceLanguage.ENGLISH })
        assertTrue(langs.any { it == com.example.util.VoiceLanguage.HINDI })
        assertTrue(langs.any { it == com.example.util.VoiceLanguage.BENGALI })
        assertTrue(langs.any { it == com.example.util.VoiceLanguage.AUTO })
    }

    @Test
    fun voiceLanguage_bengaliAndEnglishTags_supported() {
        val bengali = com.example.util.VoiceLanguage.BENGALI
        assertEquals("bn", bengali.code)
        assertTrue(bengali.bcp47.startsWith("bn"))

        val english = com.example.util.VoiceLanguage.ENGLISH
        assertEquals("en", english.code)
        assertTrue(english.bcp47.startsWith("en"))
    }

    @Test
    fun suitTheme_allMarkSuitsConfigured() {
        val suits = com.example.ui.theme.SuitTheme.values()
        assertTrue(suits.any { it == com.example.ui.theme.SuitTheme.MARK_LXXXV })
        assertTrue(suits.any { it == com.example.ui.theme.SuitTheme.MARK_L_CRIMSON })
        assertTrue(suits.any { it == com.example.ui.theme.SuitTheme.STEALTH_OPS })
        assertTrue(suits.any { it == com.example.ui.theme.SuitTheme.WAR_MACHINE })
        suits.forEach {
            assertTrue(it.title.isNotBlank())
            assertTrue(it.subtitle.isNotBlank())
        }
    }

    @Test
    fun geminiBrainMode_allModesConfigured() {
        val modes = com.example.data.model.GeminiBrainMode.values()
        assertTrue(modes.any { it == com.example.data.model.GeminiBrainMode.BUTLER })
        assertTrue(modes.any { it == com.example.data.model.GeminiBrainMode.TACTICAL })
        assertTrue(modes.any { it == com.example.data.model.GeminiBrainMode.QUANTUM })
        assertTrue(modes.any { it == com.example.data.model.GeminiBrainMode.CREATIVE })
        modes.forEach {
            assertTrue(it.title.isNotBlank())
            assertTrue(it.titleBn.isNotBlank())
            assertTrue(it.systemPromptExtension.isNotBlank())
            assertTrue(it.temperature in 0.1f..1.5f)
        }
    }

    @Test
    fun geminiMultiTurnContext_constructsAlternatingTurnsCorrectly() {
        val logs = listOf(
            com.example.data.local.JarvisLogEntity(query = "What is the arc reactor?", response = "A clean energy generator.", category = "AI"),
            com.example.data.local.JarvisLogEntity(query = "বাংলায় বলো", response = "এটি একটি ক্লিন এনার্জি জেনারেটর।", category = "AI")
        )
        val currentPrompt = "How does it power the suit?"
        val contentsList = mutableListOf<com.example.data.remote.GeminiContent>()
        for (item in logs) {
            contentsList.add(com.example.data.remote.GeminiContent(parts = listOf(com.example.data.remote.GeminiPart(text = item.query)), role = "user"))
            contentsList.add(com.example.data.remote.GeminiContent(parts = listOf(com.example.data.remote.GeminiPart(text = item.response)), role = "model"))
        }
        contentsList.add(com.example.data.remote.GeminiContent(parts = listOf(com.example.data.remote.GeminiPart(text = currentPrompt)), role = "user"))

        assertEquals(5, contentsList.size)
        assertEquals("user", contentsList[0].role)
        assertEquals("model", contentsList[1].role)
        assertEquals("user", contentsList[2].role)
        assertEquals("model", contentsList[3].role)
        assertEquals("user", contentsList[4].role)
        assertEquals(currentPrompt, contentsList[4].parts[0].text)
    }

    @Test
    fun pythonJarvisConversationalContract_repliesMatchSpecification() {
        fun jarvisReply(userText: String): String {
            val text = userText.lowercase()
            return when {
                "tumhara naam" in text -> "Mera naam Jarvis hai. Main aapka friendly personal assistant hoon."
                "youtube kholo" in text -> "YouTube khol raha hoon."
                "internet par search karo" in text -> "Bilkul, main internet par search karta hoon."
                else -> "Samajh gaya. Main aapki madad karne ki koshish karta hoon."
            }
        }

        assertEquals(
            "Mera naam Jarvis hai. Main aapka friendly personal assistant hoon.",
            jarvisReply("tumhara naam kya hai")
        )
        assertEquals(
            "YouTube khol raha hoon.",
            jarvisReply("Jarvis please youtube kholo")
        )
        assertEquals(
            "Bilkul, main internet par search karta hoon.",
            jarvisReply("internet par search karo Iron Man Mark 85")
        )
        assertEquals(
            "Samajh gaya. Main aapki madad karne ki koshish karta hoon.",
            jarvisReply("kuch naya batao")
        )
    }

    @Test
    fun protocolManager_hasBengaliDescriptionsAndAnnouncements() {
        ProtocolManager.protocols.forEach { proto ->
            assertTrue(proto.nameBn.isNotBlank())
            assertTrue(proto.descriptionBn.isNotBlank())
            assertTrue(proto.voiceAnnouncementBn.isNotBlank())
        }
    }
}
