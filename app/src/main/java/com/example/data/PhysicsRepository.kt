package com.example.data

import kotlinx.coroutines.flow.Flow

class PhysicsRepository(
    private val problemDao: ProblemDao,
    private val quizDao: QuizDao
) {
    val savedProblems: Flow<List<SavedProblemEntity>> = problemDao.getAllSavedProblems()
    val quizScores: Flow<List<QuizScoreEntity>> = quizDao.getAllScores()

    suspend fun saveProblem(problem: SavedProblemEntity) {
        problemDao.insertProblem(problem)
    }

    suspend fun deleteProblem(id: Int) {
        problemDao.deleteProblemById(id)
    }

    suspend fun recordQuizScore(score: QuizScoreEntity) {
        quizDao.insertScore(score)
    }
}
