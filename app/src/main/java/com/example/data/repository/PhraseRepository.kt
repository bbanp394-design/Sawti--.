package com.example.data.repository

import com.example.data.db.PhraseDao
import com.example.data.model.Phrase
import kotlinx.coroutines.flow.Flow

class PhraseRepository(private val phraseDao: PhraseDao) {

    fun getPhrases(language: String): Flow<List<Phrase>> =
        phraseDao.getPhrasesByLanguage(language)

    suspend fun checkAndSeedDefaults(language: String) {
        val count = phraseDao.getCountByLanguage(language)
        if (count == 0) {
            val defaults = getDefaultPhrases(language)
            phraseDao.insertAll(defaults)
        }
    }

    suspend fun addPhrase(text: String, language: String, category: String = "general"): Long {
        val newPhrase = Phrase(
            text = text.trim(),
            language = language,
            category = category,
            sortOrder = 999,
            isCustom = true
        )
        return phraseDao.insertPhrase(newPhrase)
    }

    suspend fun updatePhrase(phrase: Phrase) {
        phraseDao.updatePhrase(phrase)
    }

    suspend fun deletePhrase(phrase: Phrase) {
        phraseDao.deletePhrase(phrase)
    }

    suspend fun swapOrder(first: Phrase, second: Phrase) {
        val updatedFirst = first.copy(sortOrder = second.sortOrder)
        val updatedSecond = second.copy(sortOrder = first.sortOrder)
        phraseDao.updatePhrase(updatedFirst)
        phraseDao.updatePhrase(updatedSecond)
    }

    private fun getDefaultPhrases(language: String): List<Phrase> {
        return when (language) {
            "ar" -> listOf(
                Phrase(text = "نعم", language = "ar", sortOrder = 0),
                Phrase(text = "لا", language = "ar", sortOrder = 1),
                Phrase(text = "أريد ماء", language = "ar", sortOrder = 2),
                Phrase(text = "أريد الطعام", language = "ar", sortOrder = 3),
                Phrase(text = "أشعر بالألم", language = "ar", sortOrder = 4),
                Phrase(text = "أحتاج مساعدة", language = "ar", sortOrder = 5),
                Phrase(text = "اتصل بأهلي", language = "ar", sortOrder = 6),
                Phrase(text = "أنا بخير", language = "ar", sortOrder = 7),
                Phrase(text = "أريد الذهاب للطبيب", language = "ar", sortOrder = 8)
            )
            "zh" -> listOf(
                Phrase(text = "是的", language = "zh", sortOrder = 0),
                Phrase(text = "不是", language = "zh", sortOrder = 1),
                Phrase(text = "我需要水", language = "zh", sortOrder = 2),
                Phrase(text = "我要吃东西", language = "zh", sortOrder = 3),
                Phrase(text = "我觉得痛", language = "zh", sortOrder = 4),
                Phrase(text = "我需要帮助", language = "zh", sortOrder = 5),
                Phrase(text = "联系我的家人", language = "zh", sortOrder = 6),
                Phrase(text = "我很好", language = "zh", sortOrder = 7),
                Phrase(text = "我想看医生", language = "zh", sortOrder = 8)
            )
            else -> listOf(
                Phrase(text = "Yes", language = "en", sortOrder = 0),
                Phrase(text = "No", language = "en", sortOrder = 1),
                Phrase(text = "I need water", language = "en", sortOrder = 2),
                Phrase(text = "I want food", language = "en", sortOrder = 3),
                Phrase(text = "I feel pain", language = "en", sortOrder = 4),
                Phrase(text = "I need help", language = "en", sortOrder = 5),
                Phrase(text = "Call my family", language = "en", sortOrder = 6),
                Phrase(text = "I am fine", language = "en", sortOrder = 7),
                Phrase(text = "I want to see a doctor", language = "en", sortOrder = 8)
            )
        }
    }
}
