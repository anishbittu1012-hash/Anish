package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

enum class AppCategory(val displayName: String, val displayNameBn: String) {
    ALL("ALL", "সকল"),
    USER("USER APPS", "ইনস্টল করা অ্যাপ"),
    SYSTEM("SYSTEM", "সিস্টেম"),
    MEDIA("MEDIA & MUSIC", "মিডিয়া ও গান"),
    COMMUNICATION("COMMUNICATION", "যোগাযোগ"),
    TOOLS("TOOLS & UTILITIES", "টুলস ও ইউটিলিটি")
}

data class InstalledApp(
    val name: String,
    val packageName: String,
    val isSystem: Boolean,
    val category: AppCategory = AppCategory.TOOLS,
    val iconBitmap: ImageBitmap? = null
)

sealed class AppLaunchResult {
    data class Success(val appName: String, val packageName: String) : AppLaunchResult()
    data class StoreOffer(val queryName: String, val storeIntent: Intent, val suggestedPackage: String?) : AppLaunchResult()
    data class Failed(val appName: String) : AppLaunchResult()
    object NotAnAppCommand : AppLaunchResult()
}

/**
 * High-performance App Launcher & Vocal Command Dispatcher for JARVIS.
 * Fetches, caches, categorizes installed applications and accurately extracts
 * target app names from voice commands like "JARVIS, open Spotify".
 */
object AppLauncherManager {

    private val knownAppPackages = mapOf(
        "spotify" to "com.spotify.music",
        "youtube" to "com.google.android.youtube",
        "youtube music" to "com.google.android.apps.youtube.music",
        "whatsapp" to "com.whatsapp",
        "facebook" to "com.facebook.katana",
        "instagram" to "com.instagram.android",
        "twitter" to "com.twitter.android",
        "x" to "com.twitter.android",
        "telegram" to "org.telegram.messenger",
        "chrome" to "com.android.chrome",
        "gmail" to "com.google.android.gm",
        "netflix" to "com.netflix.mediaclient",
        "tiktok" to "com.zhiliaoapp.musically",
        "google maps" to "com.google.android.apps.maps",
        "maps" to "com.google.android.apps.maps",
        "google drive" to "com.google.android.apps.docs",
        "play store" to "com.android.vending",
        "calculator" to "com.google.android.calculator",
        "camera" to "com.google.android.GoogleCamera",
        "clock" to "com.google.android.deskclock"
    )

    private val bengaliAppAliasMap = mapOf(
        "স্পটিফাই" to "spotify",
        "ইউটিউব" to "youtube",
        "ইউটিউব মিউজিক" to "youtube music",
        "গান" to "spotify",
        "মিউজিক" to "spotify",
        "মিউজিক প্লেয়ার" to "spotify",
        "হোয়াটসঅ্যাপ" to "whatsapp",
        "ফেসবুক" to "facebook",
        "ইনস্টাগ্রাম" to "instagram",
        "ইন্সটাগ্রাম" to "instagram",
        "টুইটার" to "twitter",
        "এক্স" to "twitter",
        "টেলিগ্রাম" to "telegram",
        "টিকটক" to "tiktok",
        "নেটফ্লিক্স" to "netflix",
        "ক্যামেরা" to "camera",
        "ক্রোম" to "chrome",
        "গুগল" to "google",
        "গ্যালারি" to "photos",
        "ফটোস" to "photos",
        "ছবি" to "photos",
        "ক্যালকুলেটর" to "calculator",
        "ম্যাপস" to "maps",
        "ম্যাপ" to "maps",
        "জিমেইল" to "gmail",
        "মেইল" to "gmail",
        "ইমেইল" to "gmail",
        "সেটিংস" to "settings",
        "ঘড়ি" to "clock",
        "অ্যালার্ম" to "clock",
        "প্লে স্টোর" to "play store",
        "ড্রাইভ" to "drive",
        "মেসেজ" to "messages",
        "ফোন" to "phone",
        "কন্টাক্টস" to "contacts",
        "নোট" to "notes",
        "নোটস" to "notes"
    )

    private val englishAcronymMap = mapOf(
        "yt" to "youtube",
        "yt music" to "youtube music",
        "ytmusic" to "youtube music",
        "wa" to "whatsapp",
        "ig" to "instagram",
        "insta" to "instagram",
        "fb" to "facebook",
        "tg" to "telegram",
        "calc" to "calculator",
        "cam" to "camera",
        "gmaps" to "maps",
        "browser" to "chrome",
        "mail" to "gmail",
        "email" to "gmail",
        "music" to "spotify",
        "songs" to "spotify"
    )

    /**
     * Queries device package manager for all user-launchable applications,
     * extracts app icons, and categorizes each application.
     */
    fun getInstalledApplications(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val appList = mutableListOf<InstalledApp>()

        for (info in resolveInfos) {
            try {
                val pkgName = info.activityInfo.packageName
                if (pkgName == context.packageName) continue // Don't list JARVIS itself

                val appName = info.loadLabel(pm).toString()
                val isSys = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val category = categorizeApp(appName, pkgName, isSys)
                val iconDrawable = try {
                    info.loadIcon(pm)
                } catch (e: Exception) {
                    null
                }
                val iconBitmap = iconDrawable?.toSafeImageBitmap()

                appList.add(
                    InstalledApp(
                        name = appName,
                        packageName = pkgName,
                        isSystem = isSys,
                        category = category,
                        iconBitmap = iconBitmap
                    )
                )
            } catch (e: Exception) {
                // Ignore broken package
            }
        }

        return appList.sortedBy { it.name.lowercase() }
    }

    private fun categorizeApp(name: String, pkg: String, isSystem: Boolean): AppCategory {
        val combined = "$name $pkg".lowercase()

        return when {
            combined.contains("spotify") || combined.contains("youtube") || combined.contains("music") ||
            combined.contains("audio") || combined.contains("player") || combined.contains("sound") ||
            combined.contains("radio") || combined.contains("netflix") || combined.contains("video") ||
            combined.contains("podcast") || combined.contains("media") -> AppCategory.MEDIA

            combined.contains("whatsapp") || combined.contains("telegram") || combined.contains("mess") ||
            combined.contains("chat") || combined.contains("talk") || combined.contains("call") ||
            combined.contains("dialer") || combined.contains("contact") || combined.contains("gmail") ||
            combined.contains("mail") || combined.contains("discord") || combined.contains("phone") -> AppCategory.COMMUNICATION

            !isSystem -> AppCategory.USER

            combined.contains("setting") || combined.contains("camera") || combined.contains("clock") ||
            combined.contains("alarm") || combined.contains("calc") || combined.contains("file") ||
            combined.contains("browser") || combined.contains("chrome") || combined.contains("tool") -> AppCategory.TOOLS

            else -> AppCategory.SYSTEM
        }
    }

    /**
     * Analyzes raw speech or text input for vocal app launching directives.
     * Supports:
     * - "JARVIS, open Spotify"
     * - "Hey JARVIS, launch YouTube"
     * - "JARVIS, start Spotify"
     * - "Open Spotify please"
     * - "Play Spotify"
     * - "জারভিস, স্পটিফাই খোলো"
     * - "স্পটিফাই ওপেন করো"
     */
    fun extractTargetAppFromVoice(rawInput: String): String? {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return null

        // 1. Remove common punctuation
        var cleaned = trimmed
            .replace(",", " ")
            .replace(".", " ")
            .replace("?", " ")
            .replace("!", " ")
            .replace(":", " ")
            .replace(";", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()

        val lower = cleaned.lowercase()

        // 2. Strip wake words
        val wakeWordRegex = "^(hey\\s+|hi\\s+|hello\\s+|ok\\s+|okay\\s+)?(jarvis|jarvees|jarviss|জারভিস|হে\\s+জারভিস)\\s*".toRegex(RegexOption.IGNORE_CASE)
        val strippedWake = lower.replace(wakeWordRegex, "").trim()

        // 3. Strip politeness / fillers
        val fillerRegex = "^(please|can you|could you|would you|would you kindly|kindly|doya kore|দয়া করে)\\s*".toRegex(RegexOption.IGNORE_CASE)
        val withoutFiller = strippedWake.replace(fillerRegex, "").trim()

        // 4. English Verb Matches: "open [app]", "launch [app]", "start [app]", "run [app]", "play [app]"
        val englishVerbRegex = "^(open|launch|start|run|play|bring up|switch to|access)\\s+(?:the\\s+)?(.+)".toRegex(RegexOption.IGNORE_CASE)
        val enMatch = englishVerbRegex.find(withoutFiller)
        if (enMatch != null) {
            val candidate = enMatch.groupValues[2].trim()
                .replace("\\s+(please|now|app)$".toRegex(RegexOption.IGNORE_CASE), "")
                .trim()
            if (candidate.isNotBlank()) return candidate
        }

        // 5. Bengali Prefix Matches: "খোলো [app]", "ওপেন করো [app]", "চালু করো [app]"
        val bnPrefixRegex = "^(খোলো|ওপেন করো|চালু করো|খুলুন|স্টার্ট করো|প্লে করো)\\s+(?:অ্যাপ\\s+)?(.+)".toRegex()
        val bnPreMatch = bnPrefixRegex.find(withoutFiller)
        if (bnPreMatch != null) {
            val candidate = bnPreMatch.groupValues[2].trim().replace("\\s+অ্যাপ$".toRegex(), "").trim()
            if (candidate.isNotBlank()) return candidate
        }

        // 6. Bengali Suffix Matches: "[app] খোলো", "[app] ওপেন করো", "[app] চালু করো"
        val bnSuffixRegex = "(.+?)\\s+(?:অ্যাপ\\s+)?(খোলো|ওপেন করো|চালু করো|খুলুন|প্লে করো|স্টার্ট করো)$".toRegex()
        val bnSuffMatch = bnSuffixRegex.find(withoutFiller)
        if (bnSuffMatch != null) {
            val candidate = bnSuffMatch.groupValues[1].trim().replace("^অ্যাপ\\s+".toRegex(), "").trim()
            if (candidate.isNotBlank()) return candidate
        }

        // 7. English Suffix Matches: "[app] open", "[app] app launch"
        val enSuffixRegex = "(.+?)\\s+(?:app\\s+)?(open|launch|start|run)$".toRegex(RegexOption.IGNORE_CASE)
        val enSuffMatch = enSuffixRegex.find(withoutFiller)
        if (enSuffMatch != null) {
            val candidate = enSuffMatch.groupValues[1].trim()
            if (candidate.isNotBlank()) return candidate
        }

        return null
    }

    /**
     * Resolves an app query string into an InstalledApp or Store fallback.
     */
    fun resolveAndLaunch(
        context: Context,
        installedApps: List<InstalledApp>,
        queryCandidate: String
    ): AppLaunchResult {
        var clean = queryCandidate.trim().lowercase()
        if (clean.isBlank()) return AppLaunchResult.NotAnAppCommand

        // Check Bengali transliteration map
        val bnMapped = bengaliAppAliasMap[clean] ?: bengaliAppAliasMap.entries.firstOrNull { clean.contains(it.key) }?.value
        if (bnMapped != null) {
            clean = bnMapped
        }

        // Check English acronyms
        val acronymMapped = englishAcronymMap[clean]
        if (acronymMapped != null) {
            clean = acronymMapped
        }

        // 1. Exact match on app name
        var matched = installedApps.firstOrNull { it.name.equals(clean, ignoreCase = true) }

        // 2. Starts with name
        if (matched == null) {
            matched = installedApps.firstOrNull { it.name.lowercase().startsWith(clean) }
        }

        // 3. Contains name
        if (matched == null) {
            matched = installedApps.firstOrNull { it.name.lowercase().contains(clean) }
        }

        // 4. Match package name
        if (matched == null) {
            matched = installedApps.firstOrNull { it.packageName.lowercase().contains(clean) }
        }

        // 5. Match normalized (spaces stripped)
        if (matched == null) {
            val noSpaceClean = clean.replace(" ", "")
            matched = installedApps.firstOrNull { it.name.lowercase().replace(" ", "").contains(noSpaceClean) }
        }

        // If found, launch it!
        if (matched != null) {
            val success = launchPackage(context, matched.packageName)
            return if (success) {
                AppLaunchResult.Success(matched.name, matched.packageName)
            } else {
                AppLaunchResult.Failed(matched.name)
            }
        }

        // App NOT installed on device. Check if it's a known popular application
        val suggestedPkg = knownAppPackages[clean] ?: knownAppPackages.entries.firstOrNull { clean.contains(it.key) }?.value
        val playStoreIntent = createPlayStoreIntent(suggestedPkg ?: clean)

        return AppLaunchResult.StoreOffer(
            queryName = queryCandidate.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            storeIntent = playStoreIntent,
            suggestedPackage = suggestedPkg
        )
    }

    fun launchPackage(context: Context, packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun createPlayStoreIntent(packageOrQuery: String): Intent {
        return try {
            val uri = if (packageOrQuery.contains(".")) {
                Uri.parse("market://details?id=$packageOrQuery")
            } else {
                Uri.parse("market://search?q=$packageOrQuery")
            }
            Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            val webUri = if (packageOrQuery.contains(".")) {
                Uri.parse("https://play.google.com/store/apps/details?id=$packageOrQuery")
            } else {
                Uri.parse("https://play.google.com/store/search?q=$packageOrQuery")
            }
            Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    private fun Drawable.toSafeImageBitmap(sizePx: Int = 96): ImageBitmap? {
        return try {
            if (this is BitmapDrawable && bitmap != null) {
                return bitmap.asImageBitmap()
            }
            val width = if (intrinsicWidth > 0) intrinsicWidth.coerceIn(32, sizePx) else sizePx
            val height = if (intrinsicHeight > 0) intrinsicHeight.coerceIn(32, sizePx) else sizePx
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bmp.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
