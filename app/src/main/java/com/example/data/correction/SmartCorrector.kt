package com.example.data.correction

data class SmartCorrection(
    val originalText: String,
    val suggestedText: String,
    val explanation: String
)

object SmartCorrector {

    private val arabicReplacements = mapOf(
        "مء" to "ماء",
        "ماءء" to "ماء",
        "مرء" to "ماء",
        "مويه" to "ماء",
        "مرحبه" to "مرحبا",
        "مرحبان" to "مرحباً",
        "مرحب" to "مرحبا",
        "طعم" to "طعام",
        "طعمة" to "طعام",
        "اكل" to "أريد الطعام",
        "مسعده" to "مساعدة",
        "مسعادة" to "مساعدة",
        "الم" to "ألم",
        "الالم" to "الألم",
        "وجع" to "أشعر بالألم",
        "اهلي" to "أهلي",
        "عائلتي" to "اتصل بأهلي",
        "طبيب" to "أريد الذهاب للطبيب",
        "دكتور" to "أريد الذهاب للطبيب",
        "دوء" to "دواء",
        "دوا" to "دواء",
        "علاج" to "دواء",
        "لأ" to "لا",
        "نعمم" to "نعم",
        "شكرا" to "شكراً",
        "عفو" to "عفواً",
        "اسعاف" to "إسعاف"
    )

    private val englishReplacements = mapOf(
        "watr" to "water",
        "wtr" to "water",
        "wter" to "water",
        "ned" to "need",
        "hlp" to "help",
        "teh" to "the",
        "helo" to "hello",
        "hallo" to "hello",
        "fod" to "food",
        "fud" to "food",
        "pan" to "pain",
        "doc" to "doctor",
        "doktor" to "doctor",
        "plz" to "please",
        "pls" to "please",
        "thx" to "thanks",
        "thnk" to "thanks",
        "famly" to "family",
        "im ok" to "I am fine",
        "i ned watr" to "I need water",
        "i need watr" to "I need water",
        "need watr" to "I need water"
    )

    private val chineseReplacements = mapOf(
        "需水" to "我需要水",
        "要水" to "我需要水",
        "喝水" to "我想喝水",
        "水水" to "水",
        "吃食" to "我要吃东西",
        "要吃" to "我要吃东西",
        "疼" to "我觉得痛",
        "痛痛" to "我觉得痛",
        "帮我" to "我需要帮助",
        "看病" to "我想看医生",
        "医生" to "我想看医生",
        "谢谢" to "谢谢你",
        "家属" to "联系我的家人"
    )

    fun checkCorrection(text: String, language: String): SmartCorrection? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        // 1. Direct whole phrase replacement
        val replacementMap = when (language) {
            "ar" -> arabicReplacements
            "zh" -> chineseReplacements
            else -> englishReplacements
        }

        replacementMap[trimmed.lowercase()]?.let { corrected ->
            if (!corrected.equals(trimmed, ignoreCase = true)) {
                return SmartCorrection(
                    originalText = trimmed,
                    suggestedText = corrected,
                    explanation = getExplanation(language, corrected)
                )
            }
        }

        // 2. Word-by-word token replacement
        val words = trimmed.split("\\s+".toRegex())
        var modified = false
        val correctedWords = words.map { word ->
            val cleanWord = word.trim('.', ',', '!', '؟')
            val match = replacementMap[cleanWord.lowercase()]
            if (match != null) {
                modified = true
                match
            } else {
                word
            }
        }

        if (modified) {
            val correctedText = correctedWords.joinToString(" ")
            return SmartCorrection(
                originalText = trimmed,
                suggestedText = correctedText,
                explanation = getExplanation(language, correctedText)
            )
        }

        // 3. Common spelling heuristic (e.g. "مء" inside Arabic or "watr" inside English)
        if (language == "ar") {
            if (trimmed.contains("مء")) {
                val corrected = trimmed.replace("مء", "ماء")
                return SmartCorrection(trimmed, corrected, "تصحيح: ماء")
            }
            if (trimmed.endsWith("ه") && (trimmed.startsWith("مرحب") || trimmed.startsWith("مساعد"))) {
                val corrected = trimmed.dropLast(1) + "ة"
                if (trimmed.startsWith("مرحب")) {
                    return SmartCorrection(trimmed, "مرحبا", "تصحيح: مرحبا")
                }
                return SmartCorrection(trimmed, corrected, "تصحيح الإملاء")
            }
        }

        return null
    }

    private fun getExplanation(language: String, corrected: String): String {
        return when (language) {
            "ar" -> "اقتراح تصحيح: $corrected"
            "zh" -> "建议修正: $corrected"
            else -> "Suggested correction: $corrected"
        }
    }
}
