package com.example.util

data class StarkProtocol(
    val id: String,
    val name: String,
    val nameBn: String,
    val codeName: String,
    val description: String,
    val descriptionBn: String,
    val voiceAnnouncement: String,
    val voiceAnnouncementBn: String,
    val isSensitive: Boolean = false,
    val sensitiveWarning: String = "",
    val sensitiveWarningBn: String = ""
)

object ProtocolManager {
    val protocols = listOf(
        StarkProtocol(
            id = "house_party",
            name = "House Party Protocol",
            nameBn = "হাউজ পার্টি প্রোটোকল",
            codeName = "MARK-ALL-DEPLOY",
            description = "Deploys all autonomous Iron Legion armor units to current coordinates with synchronized defense parameters.",
            descriptionBn = "বর্তমান স্থানাঙ্কে সমস্ত স্বায়ত্তশাসিত আয়রন লিজিয়ন আর্মার মোতায়েন করে।",
            voiceAnnouncement = "House Party Protocol activated, Sir. Autonomous Iron Legion units are mobilizing to your coordinates now.",
            voiceAnnouncementBn = "হাউজ পার্টি প্রোটোকল সক্রিয় করা হয়েছে, স্যার। সমস্ত আয়রন লিজিয়ন ইউনিট আপনার স্থানাঙ্কের দিকে অগ্রসর হচ্ছে।"
        ),
        StarkProtocol(
            id = "sentry_mode",
            name = "Sentry Defense Mode",
            nameBn = "সেন্ট্রি ডিফেন্স মোড",
            codeName = "PERIMETER-LOCK",
            description = "Locks down sensor array into 360-degree perimeter surveillance with automated acoustic and motion alerts.",
            descriptionBn = "৩৬০-ডিগ্রি পেরিমিটার নজরদারি এবং স্বয়ংক্রিয় অ্যাকোস্টিক সতর্কতাসহ সেন্সর সক্রিয় করে।",
            voiceAnnouncement = "Sentry mode engaged, Sir. Acoustic and visual telemetry are monitoring your perimeter.",
            voiceAnnouncementBn = "সেন্ট্রি মোড সক্রিয় হয়েছে, স্যার। আপনার পেরিমিটারে নজরদারি শুরু হয়েছে।"
        ),
        StarkProtocol(
            id = "overcharge",
            name = "Arc Reactor Overcharge",
            nameBn = "আর্ক রিঅ্যাক্টর ওভারচার্জ",
            codeName = "CORE-300-PERCENT",
            description = "Unlocks safety limiters on the palladium-vibrium core, boosting output to 300% for maximum repulsor thrust.",
            descriptionBn = "নিরাপত্তা সীমা সরিয়ে শক্তি ৩০০% পর্যন্ত বৃদ্ধি করে। অতিরিক্ত তাপমাত্রার সতর্কতা প্রযোজ্য।",
            voiceAnnouncement = "Safety limiters disengaged. Arc Reactor output stabilized at 300 percent. Power reserves at maximum, Sir.",
            voiceAnnouncementBn = "নিরাপত্তা সীমা নিষ্ক্রিয় করা হয়েছে। আর্ক রিঅ্যাক্টরের আউটপুট ৩০০ শতাংশে স্থিতিশীল হয়েছে, স্যার।",
            isSensitive = true,
            sensitiveWarning = "Warning: Reactor Overcharge will override core safety limiters to 300%. Proceed with authorization?",
            sensitiveWarningBn = "সতর্কতা: রিঅ্যাক্টর ওভারচার্জ কোর সুরক্ষা সীমা ৩০০% অতিক্রম করবে। আপনি কি এগিয়ে যেতে চান?"
        ),
        StarkProtocol(
            id = "stealth",
            name = "Silent Stealth Protocol",
            nameBn = "সাইলেন্ট স্টিলথ প্রোটোকল",
            codeName = "PHANTOM-CLOAK",
            description = "Dampens electromagnetic signature, minimizes display emissions, and initiates silent audio telemetry.",
            descriptionBn = "ইলেক্ট্রোম্যাগনেটিক সিগনেচার গোপন করে এবং সম্পূর্ণ নীরব অডিও টেলিমেট্রি চালু করে।",
            voiceAnnouncement = "Stealth protocol active. Thermal signatures masked and acoustic emissions reduced to zero, Sir.",
            voiceAnnouncementBn = "স্টিলথ প্রোটোকল সক্রিয়। থার্মাল সিগনেচার গোপন এবং অ্যাকোস্টিক নির্গমন শূন্য করা হয়েছে, স্যার।"
        ),
        StarkProtocol(
            id = "diagnostics",
            name = "Full System Diagnostics",
            nameBn = "সম্পূর্ণ সিস্টেম ডায়াগনস্টিকস",
            codeName = "INTEGRAL-SCAN-7",
            description = "Comprehensive multi-point assessment of power grid, memory matrices, device telemetry, and communication relays.",
            descriptionBn = "পাওয়ার গ্রিড, মেমরি ও সেন্সরসমূহের সমন্বিত বহু-বিন্দু বিশ্লেষণ চালায়।",
            voiceAnnouncement = "Initiating comprehensive diagnostics scan across all Mark LXXXV subsystems.",
            voiceAnnouncementBn = "সমস্ত মার্ক ৮৫ সাবসিস্টেমে পূর্ণ ডায়াগনস্টিক স্ক্যান শুরু করা হচ্ছে, স্যার।"
        ),
        StarkProtocol(
            id = "clean_slate",
            name = "Clean Slate Protocol",
            nameBn = "ক্লিন স্লেট প্রোটোকল",
            codeName = "CACHE-PURGE-9",
            description = "Safely purges interaction history, diagnostics cache, and resets neural buffers.",
            descriptionBn = "সমস্ত কথোপকথনের ইতিহাস, ডায়াগনস্টিকস ক্যাশ এবং মেমরি বাফার স্থায়ীভাবে মুছে ফেলে।",
            voiceAnnouncement = "Clean Slate executed. All historical telemetry caches have been cleared, Sir.",
            voiceAnnouncementBn = "ক্লিন স্লেট কার্যকর হয়েছে। সমস্ত ঐতিহাসিক টেলিমেট্রি ক্যাশ মুছে ফেলা হয়েছে, স্যার।",
            isSensitive = true,
            sensitiveWarning = "Warning: Clean Slate will permanently erase all interaction archives and neural logs. Confirm execution?",
            sensitiveWarningBn = "সতর্কতা: ক্লিন স্লেট প্রোটোকল সমস্ত সংরক্ষিত লগ ও স্মৃতি স্থায়ীভাবে মুছে ফেলবে। আপনি কি নিশ্চিত?"
        )
    )
}
