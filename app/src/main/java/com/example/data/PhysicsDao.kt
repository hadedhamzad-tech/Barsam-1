package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {
    @Query("SELECT * FROM saved_problems ORDER BY timestamp DESC")
    fun getAllSavedProblems(): Flow<List<SavedProblemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: SavedProblemEntity): Long

    @Query("DELETE FROM saved_problems WHERE id = :id")
    suspend fun deleteProblemById(id: Int)

    @Query("DELETE FROM saved_problems")
    suspend fun clearAllProblems()
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<QuizScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: QuizScoreEntity): Long

    @Query("DELETE FROM quiz_scores")
    suspend fun clearAllScores()
}
