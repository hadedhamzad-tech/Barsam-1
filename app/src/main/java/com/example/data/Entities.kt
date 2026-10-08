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

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String, // آزمایشگاه، آزمون، حل مسئله
    val iconEmoji: String,
    val points: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    val totalPoints: Int = 0,
    val levelTitle: String = "دانش‌آموز کنجکاو",
    val simulationsCompleted: Int = 0,
    val quizzesCompleted: Int = 0,
    val problemsSaved: Int = 0
)
