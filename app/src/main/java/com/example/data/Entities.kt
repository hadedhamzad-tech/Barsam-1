package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_problems")
data class SavedProblemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val inputSummary: String,
    val resultSummary: String,
    val stepsList: String, // newline-separated steps
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_scores")
data class QuizScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val topic: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val scorePercentage: Int,
    val timestamp: Long = System.currentTimeMillis()
)
