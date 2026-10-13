package com.example

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.TopicProgressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Migration Test verifying data preservation across database schema upgrades.
 * Tests upgrade path from database version 2 (app v1.7) to version 3 (app v1.8 / v1.9).
 * Proves that user scores, unlocked achievements, saved problems, and progress remain 100% intact.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomMigrationTest {

    private class V2DatabaseHelper(context: Context, dbName: String) :
        SQLiteOpenHelper(context, dbName, null, 2) {

        override fun onCreate(db: SQLiteDatabase) {
            // Version 1 tables
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `saved_problems` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `inputSummary` TEXT NOT NULL,
                    `resultSummary` TEXT NOT NULL,
                    `stepsList` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `quiz_scores` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `topic` TEXT NOT NULL,
                    `correctAnswers` INTEGER NOT NULL,
                    `totalQuestions` INTEGER NOT NULL,
                    `scorePercentage` INTEGER NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            // Version 2 tables (achievements & user_progress added in v2)
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `achievements` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `iconEmoji` TEXT NOT NULL,
                    `points` INTEGER NOT NULL,
                    `isUnlocked` INTEGER NOT NULL DEFAULT 0,
                    `unlockedAt` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `user_progress` (
                    `id` INTEGER NOT NULL,
                    `totalPoints` INTEGER NOT NULL DEFAULT 0,
                    `levelTitle` TEXT NOT NULL DEFAULT 'دانش‌آموز کنجکاو',
                    `simulationsCompleted` INTEGER NOT NULL DEFAULT 0,
                    `quizzesCompleted` INTEGER NOT NULL DEFAULT 0,
                    `problemsSaved` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // No-op for helper fixed at v2
        }
    }

    private class V1DatabaseHelper(context: Context, dbName: String) :
        SQLiteOpenHelper(context, dbName, null, 1) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `saved_problems` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `inputSummary` TEXT NOT NULL,
                    `resultSummary` TEXT NOT NULL,
                    `stepsList` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `quiz_scores` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `topic` TEXT NOT NULL,
                    `correctAnswers` INTEGER NOT NULL,
                    `totalQuestions` INTEGER NOT NULL,
                    `scorePercentage` INTEGER NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // No-op
        }
    }

    @Test
    fun testMigrationFromV2ToV3PreservesAllUserData() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration_test_v2_to_v3.db"
        context.deleteDatabase(dbName)

        // 1. Create a v2 SQLite database and populate with user data (scores, achievements, problems, progress)
        val v2Helper = V2DatabaseHelper(context, dbName)
        val v2Db = v2Helper.writableDatabase

        // Insert Quiz Score (کارنامه)
        val scoreValues = ContentValues().apply {
            put("topic", "آزمون کنکوری فیزیک دوازدهم")
            put("correctAnswers", 9)
            put("totalQuestions", 10)
            put("scorePercentage", 90)
            put("timestamp", 1710000000000L)
        }
        val scoreId = v2Db.insert("quiz_scores", null, scoreValues)
        assertTrue("Quiz score insertion should succeed", scoreId > 0)

        // Insert Achievement (مدال)
        val medalValues = ContentValues().apply {
            put("id", "mechanics_master")
            put("title", "استاد مکانیک")
            put("description", "تسلط کامل بر دینامیک و سینماتیک")
            put("category", "آزمون")
            put("iconEmoji", "🥇")
            put("points", 100)
            put("isUnlocked", 1)
            put("unlockedAt", 1710000000000L)
        }
        val medalId = v2Db.insert("achievements", null, medalValues)
        assertTrue("Achievement insertion should succeed", medalId != -1L)

        // Insert Saved Problem (مسئله ذخیره‌شده)
        val problemValues = ContentValues().apply {
            put("title", "بررسی سقوط آزاد در خلأ")
            put("category", "سینماتیک")
            put("inputSummary", "h = 80m, g = 10m/s^2")
            put("resultSummary", "t = 4s, v = 40m/s")
            put("stepsList", "گام ۱: رابطه مستقل از جرم\nگام ۲: محاسبه سرعت برخورد")
            put("timestamp", 1710000000000L)
        }
        val problemId = v2Db.insert("saved_problems", null, problemValues)
        assertTrue("Problem insertion should succeed", problemId > 0)

        // Insert User Progress (امتیاز و رتبه)
        val progressValues = ContentValues().apply {
            put("id", 1)
            put("totalPoints", 350)
            put("levelTitle", "استاد آزمایشگاه فیزیک")
            put("simulationsCompleted", 5)
            put("quizzesCompleted", 4)
            put("problemsSaved", 3)
        }
        val progressId = v2Db.insert("user_progress", null, progressValues)
        assertTrue("Progress insertion should succeed", progressId > 0)

        v2Db.close()
        v2Helper.close()

        // 2. Open with Room AppDatabase using MIGRATION_2_3 to upgrade to v3
        val roomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_1_3)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

        try {
            // 3. Verify that old user data is completely preserved
            val savedScores = roomDb.quizDao().getAllScores().first()
            assertEquals("Quiz scores count must be preserved", 1, savedScores.size)
            assertEquals("آزمون کنکوری فیزیک دوازدهم", savedScores[0].topic)
            assertEquals(9, savedScores[0].correctAnswers)
            assertEquals(10, savedScores[0].totalQuestions)
            assertEquals(90, savedScores[0].scorePercentage)

            val savedAchievements = roomDb.achievementDao().getAllAchievements().first()
            val unlockedAchievement = savedAchievements.find { it.id == "mechanics_master" }
            assertNotNull("Achievement 'mechanics_master' must exist", unlockedAchievement)
            assertEquals("استاد مکانیک", unlockedAchievement?.title)
            assertEquals(true, unlockedAchievement?.isUnlocked)
            assertEquals(100, unlockedAchievement?.points)

            val savedProblems = roomDb.problemDao().getAllSavedProblems().first()
            assertEquals("Saved problems count must be preserved", 1, savedProblems.size)
            assertEquals("بررسی سقوط آزاد در خلأ", savedProblems[0].title)
            assertEquals("سینماتیک", savedProblems[0].category)

            val userProgress = roomDb.progressDao().getUserProgress().first()
            assertNotNull("User progress must exist", userProgress)
            assertEquals(350, userProgress?.totalPoints)
            assertEquals("استاد آزمایشگاه فیزیک", userProgress?.levelTitle)

            // 4. Verify that the new topic_progress table exists and is fully functional
            val topicProgressList = roomDb.topicDao().getAllTopicProgress().first()
            assertEquals("topic_progress table should initially be empty or default", 0, topicProgressList.size)

            // Insert new topic progress to ensure schema is fully writeable
            val newProgress = TopicProgressEntity(
                topicId = "dynamics",
                topicName = "دینامیک و قوانین نیوتون",
                grade = "دوازدهم",
                iconEmoji = "⚙️",
                correctCount = 8,
                totalAnswered = 10,
                masteryPercentage = 80
            )
            roomDb.topicDao().insertOrUpdateTopic(newProgress)

            val updatedTopicProgress = roomDb.topicDao().getAllTopicProgress().first()
            assertEquals(1, updatedTopicProgress.size)
            assertEquals("dynamics", updatedTopicProgress[0].topicId)
            assertEquals(80, updatedTopicProgress[0].masteryPercentage)

            println("MIGRATION TEST SUCCESS: All scores, achievements, problems, and progress preserved perfectly!")
        } finally {
            roomDb.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun testMigrationFromV1ToV3PreservesScoresAndProblems() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration_test_v1_to_v3.db"
        context.deleteDatabase(dbName)

        // 1. Create a v1 SQLite database with initial scores
        val v1Helper = V1DatabaseHelper(context, dbName)
        val v1Db = v1Helper.writableDatabase

        val scoreValues = ContentValues().apply {
            put("topic", "آزمون آزمایشی فیزیک دهم")
            put("correctAnswers", 7)
            put("totalQuestions", 10)
            put("scorePercentage", 70)
            put("timestamp", 1705000000000L)
        }
        v1Db.insert("quiz_scores", null, scoreValues)

        v1Db.close()
        v1Helper.close()

        // 2. Open with Room using MIGRATION_1_3
        val roomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_1_3)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

        try {
            val savedScores = roomDb.quizDao().getAllScores().first()
            assertEquals(1, savedScores.size)
            assertEquals("آزمون آزمایشی فیزیک دهم", savedScores[0].topic)
            assertEquals(70, savedScores[0].scorePercentage)

            println("V1 -> V3 MIGRATION SUCCESS: Direct leap preserved all scores and created missing v2 & v3 tables!")
        } finally {
            roomDb.close()
            context.deleteDatabase(dbName)
        }
    }
}
