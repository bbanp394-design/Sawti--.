package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Phrase
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases WHERE language = :language ORDER BY sortOrder ASC, id ASC")
    fun getPhrasesByLanguage(language: String): Flow<List<Phrase>>

    @Query("SELECT COUNT(*) FROM phrases WHERE language = :language")
    suspend fun getCountByLanguage(language: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: Phrase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phrases: List<Phrase>)

    @Update
    suspend fun updatePhrase(phrase: Phrase)

    @Delete
    suspend fun deletePhrase(phrase: Phrase)

    @Query("DELETE FROM phrases WHERE id = :id")
    suspend fun deleteById(id: Long)
}
