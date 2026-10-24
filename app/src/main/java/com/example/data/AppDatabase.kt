package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SavedProblemEntity::class,
        QuizScoreEntity::class,
        AchievementEntity::class,
        UserProgressEntity::class,
        TopicProgressEntity::class,
        GlossaryTermEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun problemDao(): ProblemDao
    abstract fun quizDao(): QuizDao
    abstract fun achievementDao(): AchievementDao
    abstract fun progressDao(): ProgressDao
    abstract fun topicDao(): TopicDao
    abstract fun glossaryDao(): GlossaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `topic_progress` (
                        `topicId` TEXT NOT NULL,
                        `topicName` TEXT NOT NULL,
                        `grade` TEXT NOT NULL,
                        `iconEmoji` TEXT NOT NULL,
                        `correctCount` INTEGER NOT NULL DEFAULT 0,
                        `totalAnswered` INTEGER NOT NULL DEFAULT 0,
                        `masteryPercentage` INTEGER NOT NULL DEFAULT 0,
                        `lastUpdated` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`topicId`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `physics_glossary` (
                        `id` TEXT NOT NULL,
                        `termFa` TEXT NOT NULL,
                        `termEn` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `grade` TEXT NOT NULL,
                        `definition` TEXT NOT NULL,
                        `formula` TEXT,
                        `unit` TEXT,
                        `iconEmoji` TEXT NOT NULL DEFAULT '📖',
                        `isBookmarked` INTEGER NOT NULL DEFAULT 0,
                        `aiInsight` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_2_3.migrate(db)
                MIGRATION_3_4.migrate(db)
            }
        }

        val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
                MIGRATION_3_4.migrate(db)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "physics_lab_db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_1_3,
                        MIGRATION_3_4,
                        MIGRATION_2_4,
                        MIGRATION_1_4
                    )
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
