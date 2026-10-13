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

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id AND isUnlocked = 0")
    suspend fun unlockAchievement(id: String, timestamp: Long): Int

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getAchievementById(id: String): AchievementEntity?
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressOnce(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProgress(progress: UserProgressEntity)
}

@Dao
interface TopicDao {
    @Query("SELECT * FROM topic_progress ORDER BY masteryPercentage DESC, totalAnswered DESC")
    fun getAllTopicProgress(): Flow<List<TopicProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTopics(topics: List<TopicProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTopic(topic: TopicProgressEntity)

    @Query("SELECT * FROM topic_progress WHERE topicId = :topicId LIMIT 1")
    suspend fun getTopicById(topicId: String): TopicProgressEntity?

    @Query("SELECT COUNT(*) FROM topic_progress")
    suspend fun getTopicCount(): Int

    @Query("DELETE FROM topic_progress")
    suspend fun clearAllTopicProgress()
}
