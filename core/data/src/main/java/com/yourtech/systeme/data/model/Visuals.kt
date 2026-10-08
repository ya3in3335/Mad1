package com.yourtech.systeme.data.model

/**
 * Built-in vector illustrations (drawn in the design system) used for service categories and as a
 * fallback for products without a company photo. Matched from product text when not chosen.
 */
object SecurityVisuals {
    data class Entry(val key: String, val label: String, val keywords: List<String>)

    /** Most specific first. */
    val all = listOf(
        Entry("recorder", "DVR / NVR", listOf("nvr", "dvr", "enregistreur", "recorder", "مسجل", "hdd", "disque")),
        Entry("dome", "Dôme", listOf("dome", "dôme", "قبة", "ptz")),
        Entry("camera", "Caméra", listOf("camera", "caméra", "cctv", "bullet", "كاميرا", "ip")),
        Entry("fingerprint", "Biométrie", listOf("biometric", "biométrique", "fingerprint", "empreinte", "بصمة", "facial")),
        Entry("card", "RFID / NFC", listOf("rfid", "nfc", "badge", "card", "carte", "بطاقة")),
        Entry("keypad", "Contrôle d'accès", listOf("access", "accès", "keypad", "clavier", "lock", "serrure", "قفل", "دخول")),
        Entry("alarm", "Alarme", listOf("alarm", "alarme", "إنذار", "انذار", "siren", "sirène", "sensor", "détecteur", "حساس")),
        Entry("intercom", "Interphone", listOf("intercom", "interphone", "visiophone", "doorbell", "إنتركم", "انتركم")),
        Entry("barrier", "Portail / barrière", listOf("gate", "portail", "barrier", "barrière", "بوابة", "حاجز", "moteur")),
        Entry("home", "Smart home", listOf("smart", "intelligent", "connecté", "ذكي", "wifi", "home")),
        Entry("cable", "Accessoires", listOf("cable", "câble", "كابل", "switch", "poe", "alimentation", "power", "connect")),
        Entry("tools", "Installation", listOf("installation", "تركيب", "maintenance", "repair", "réparation", "صيانة")),
        Entry("shield", "Sécurité", emptyList()),
    )

    private val byKey = all.associateBy { it.key }
    fun entry(key: String?) = key?.let(byKey::get)

    fun keyFor(vararg texts: String): String? {
        val hay = texts.joinToString(" ").lowercase()
        val words = hay.split(Regex("[^\\p{L}\\p{N}]+")).toSet()
        // Short keywords ("ip", "poe") must match a whole word, longer ones may match inside a word.
        return all.firstOrNull { e -> e.keywords.any { k -> if (k.length <= 3) k.lowercase() in words else hay.contains(k.lowercase()) } }?.key
    }
}
