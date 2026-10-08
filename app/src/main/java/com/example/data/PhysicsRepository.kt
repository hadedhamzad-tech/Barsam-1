package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PhysicsRepository(
    private val problemDao: ProblemDao,
    private val quizDao: QuizDao,
    private val achievementDao: AchievementDao,
    private val progressDao: ProgressDao
) {
    val savedProblems: Flow<List<SavedProblemEntity>> = problemDao.getAllSavedProblems()
    val quizScores: Flow<List<QuizScoreEntity>> = quizDao.getAllScores()
    val achievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()
    val userProgress: Flow<UserProgressEntity?> = progressDao.getUserProgress()

    companion object {
        val DEFAULT_ACHIEVEMENTS = listOf(
            AchievementEntity(
                id = "sim_projectile",
                title = "پرتابگر کیهانی",
                description = "آزمایش کامل حرکت پرتابه و شلیک در گرانش‌های مختلف",
                category = "آزمایشگاه",
                iconEmoji = "🚀",
                points = 30
            ),
            AchievementEntity(
                id = "sim_pendulum",
                title = "پایستگی نوسان و آونگ",
                description = "بررسی تبدیل انرژی جنبشی و پتانسیل در نوسان آونگ",
                category = "آزمایشگاه",
                iconEmoji = "⏱️",
                points = 30
            ),
            AchievementEntity(
                id = "sim_optics",
                title = "کاشف شکست و زاویه حد",
                description = "آزمایش ورود نور به محیط غلیظ و بازتاب کلی اسنل",
                category = "آزمایشگاه",
                iconEmoji = "🌈",
                points = 30
            ),
            AchievementEntity(
                id = "sim_circuit",
                title = "مهندس روشنایی مدار",
                description = "بستن کلید مدار و راه‌اندازی جریان الکتریکی و لامپ",
                category = "آزمایشگاه",
                iconEmoji = "💡",
                points = 30
            ),
            AchievementEntity(
                id = "sim_newton",
                title = "شتاب‌دهنده نیوتنی",
                description = "غلبه بر اصطکاک ایستایی و شتاب‌دهی به بلوک فیزیکی",
                category = "آزمایشگاه",
                iconEmoji = "⚖️",
                points = 30
            ),
            AchievementEntity(
                id = "sim_all",
                title = "پژوهشگر جامع آزمایشگاه",
                description = "تکمیل و ثبت آزمایش در تمامی ۵ بخش آزمایشگاه فیزیک",
                category = "آزمایشگاه",
                iconEmoji = "🔬",
                points = 70
            ),
            AchievementEntity(
                id = "quiz_pass",
                title = "پیروزی در آزمون جامع",
                description = "کسب نمره قبولی (۷۰٪ یا بیشتر) در آزمون فیزیک",
                category = "آزمون",
                iconEmoji = "🎯",
                points = 50
            ),
            AchievementEntity(
                id = "quiz_perfect",
                title = "مدال طلای نمره ۱۰۰٪",
                description = "پاسخ صحیح به تمام سوالات یک آزمون بدون خطا",
                category = "آزمون",
                iconEmoji = "🥇",
                points = 100
            ),
            AchievementEntity(
                id = "quiz_frequent",
                title = "اراده پولادین",
                description = "شرکت در حداقل ۳ آزمون جامع فیزیک",
                category = "آزمون",
                iconEmoji = "🏆",
                points = 60
            ),
            AchievementEntity(
                id = "problem_solver",
                title = "حلّال حرفه‌ای مسائل",
                description = "حل گام‌به‌گام و ذخیره‌سازی مسئله در تاریخچه",
                category = "حل مسئله",
                iconEmoji = "📚",
                points = 30
            ),
            AchievementEntity(
                id = "sim_explorer",
                title = "گام نخست در آزمایشگاه",
                description = "شروع و اجرای اولین آزمایش شبیه‌سازی فیزیک",
                category = "آزمایشگاه",
                iconEmoji = "✨",
                points = 20
            ),
            AchievementEntity(
                id = "points_master",
                title = "نظریه‌پرداز ممتاز",
                description = "کسب بیش از ۲۰۰ امتیاز در کارنامه کلی فیزیک",
                category = "امتیاز",
                iconEmoji = "👑",
                points = 50
            )
        )

        fun calculateLevelTitle(points: Int): String {
            return when {
                points >= 800 -> "استاد فیزیک و افتخار برسام"
                points >= 500 -> "نظریه‌پرداز جوان"
                points >= 250 -> "حلّال برتر مسائل"
                points >= 100 -> "کاوشگر آزمایشگاه"
                else -> "دانش‌آموز کنجکاو"
            }
        }
    }

    suspend fun recordSimulationCompleted(simId: String): Pair<Boolean, String?> {
        ensureDefaultAchievements()
        unlockAchievement("sim_explorer")
        val newlyUnlocked = unlockAchievement(simId)
        val ach = achievementDao.getAchievementById(simId)

        // Increment simulationsCompleted count
        val currentProgress = progressDao.getUserProgressOnce() ?: UserProgressEntity()
        progressDao.saveUserProgress(
            currentProgress.copy(
                simulationsCompleted = currentProgress.simulationsCompleted + 1
            )
        )

        return Pair(newlyUnlocked, ach?.title)
    }

    suspend fun ensureDefaultAchievements() {
        achievementDao.insertAchievements(DEFAULT_ACHIEVEMENTS)
        val current = progressDao.getUserProgressOnce()
        if (current == null) {
            progressDao.saveUserProgress(UserProgressEntity())
        }
    }

    suspend fun saveProblem(problem: SavedProblemEntity) {
        problemDao.insertProblem(problem)
        // Unlock problem solver achievement
        unlockAchievement("problem_solver")
        val currentProgress = progressDao.getUserProgressOnce() ?: UserProgressEntity()
        progressDao.saveUserProgress(
            currentProgress.copy(
                problemsSaved = currentProgress.problemsSaved + 1
            )
        )
    }

    suspend fun deleteProblem(id: Int) {
        problemDao.deleteProblemById(id)
    }

    suspend fun recordQuizScore(score: QuizScoreEntity, difficultyLevel: Int = 1) {
        quizDao.insertScore(score)
        val currentProgress = progressDao.getUserProgressOnce() ?: UserProgressEntity()
        val updatedQuizCount = currentProgress.quizzesCompleted + 1

        // Check quiz achievements
        if (score.scorePercentage >= 70) {
            unlockAchievement("quiz_pass")
        }
        if (score.scorePercentage == 100) {
            unlockAchievement("quiz_perfect")
        }
        if (updatedQuizCount >= 3) {
            unlockAchievement("quiz_frequent")
        }

        // Add performance points based on exam score percentage & difficulty level:
        // سطح ۱: ضریب ۱ (درصد ÷ ۵)
        // سطح ۲: ضریب ۱٫۲
        // سطح ۳: ضریب ۱٫۵
        // سطح ۴: ضریب ۲ (کنکوری)
        // ترکیبی (۰): ضریب ۱٫۴
        val diffMultiplier = when (difficultyLevel) {
            1 -> 1.0f
            2 -> 1.2f
            3 -> 1.5f
            4 -> 2.0f
            else -> 1.4f
        }
        val basePoints = (score.scorePercentage / 5f)
        val earnedScorePoints = kotlin.math.round(basePoints * diffMultiplier).toInt().coerceAtLeast(0)
        addPointsInternal(earnedScorePoints, currentProgress.copy(quizzesCompleted = updatedQuizCount))
    }

    suspend fun unlockAchievement(id: String): Boolean {
        ensureDefaultAchievements()
        val ach = achievementDao.getAchievementById(id) ?: return false
        if (ach.isUnlocked) return false

        val updatedRows = achievementDao.unlockAchievement(id, System.currentTimeMillis())
        if (updatedRows > 0) {
            val currentProgress = progressDao.getUserProgressOnce() ?: UserProgressEntity()
            val newTotal = currentProgress.totalPoints + ach.points
            progressDao.saveUserProgress(
                currentProgress.copy(
                    totalPoints = newTotal,
                    levelTitle = calculateLevelTitle(newTotal)
                )
            )

            // Check if all 5 simulations are unlocked
            if (id.startsWith("sim_") && id != "sim_all") {
                checkAllSimulationsUnlocked()
            }
            if (newTotal >= 200 && id != "points_master") {
                unlockAchievement("points_master")
            }
            return true
        }
        return false
    }

    private suspend fun checkAllSimulationsUnlocked() {
        val simIds = listOf("sim_projectile", "sim_pendulum", "sim_optics", "sim_circuit", "sim_newton")
        var allDone = true
        for (simId in simIds) {
            val item = achievementDao.getAchievementById(simId)
            if (item == null || !item.isUnlocked) {
                allDone = false
                break
            }
        }
        if (allDone) {
            unlockAchievement("sim_all")
        }
    }

    private suspend fun addPointsInternal(pointsToAdd: Int, current: UserProgressEntity) {
        val newTotal = current.totalPoints + pointsToAdd
        progressDao.saveUserProgress(
            current.copy(
                totalPoints = newTotal,
                levelTitle = calculateLevelTitle(newTotal)
            )
        )
        if (newTotal >= 200) {
            unlockAchievement("points_master")
        }
    }
}
