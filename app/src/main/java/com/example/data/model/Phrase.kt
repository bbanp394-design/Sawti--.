package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrases")
data class Phrase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val language: String, // "ar", "en", "zh"
    val category: String = "general", // "needs", "feelings", "emergency", "general"
    val sortOrder: Int = 0,
    val isCustom: Boolean = false
)
