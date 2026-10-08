package com.example

import com.example.data.PhysicsData
import com.example.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizAcceptanceTest {

    @Test
    fun testRawQuestionBankBalancedDistributionAndLevels() {
        val questions = PhysicsData.quizQuestions
        assertEquals(120, questions.size)

        // Check each difficulty level has at least 30 questions
        for (level in 1..4) {
            val levelQuestions = questions.filter { it.difficulty == level }
            assertTrue("Level $level must have at least 30 questions", levelQuestions.size >= 30)
            println("Level $level question count: ${levelQuestions.size}")

            // Check balanced position in raw bank for each level
            val levelCounts = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)
            levelQuestions.forEach { q ->
                levelCounts[q.correctIndex] = (levelCounts[q.correctIndex] ?: 0) + 1
            }
            levelCounts.forEach { (pos, count) ->
                val pct = (count.toFloat() / levelQuestions.size) * 100f
                println("Level $level position ${pos + 1}: $count (${String.format("%.1f", pct)}%)")
                assertTrue("Level $level pos ${pos + 1} percentage ($pct%) must be <= 40%", pct <= 40f)
                assertTrue("Level $level pos ${pos + 1} percentage ($pct%) must be >= 15%", pct >= 15f)
            }
        }

        // Overall raw bank distribution check
        val distribution = PhysicsData.validateAnswerDistribution()
        println("Overall Raw Question Bank Distribution:")
        distribution.forEach { (pos, pct) ->
            println("Option position $pos: $pct% (${pos + 1})")
            assertTrue("Position $pos percentage ($pct%) must be <= 40%", pct <= 40f)
            assertTrue("Position $pos percentage ($pct%) should be >= 15%", pct >= 15f)
        }
    }

    @Test
    fun testTwoHundredExamsDistributionBiasCheck() {
        println("==================================================")
        println("Running 200 Simulated 10-Question Exams (2000 Questions Total)")
        println("==================================================")

        val positionCounts = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)
        var totalQuestionsTested = 0
        val rnd = Random(12345)

        for (examIndex in 1..200) {
            val examQuestions = PhysicsData.quizQuestions.shuffled(rnd).take(10)

            for (q in examQuestions) {
                // Pure Fisher-Yates shuffle without anti-streak mechanism (v1.5 requirement)
                val shuffled = q.getShuffledOptions(random = rnd)
                val correctPos = shuffled.indexOfFirst { it.isCorrect }
                assertTrue("Must have a correct option in shuffled options", correctPos != -1)

                positionCounts[correctPos] = (positionCounts[correctPos] ?: 0) + 1
                totalQuestionsTested++
            }
        }

        assertEquals(2000, totalQuestionsTested)

        println("نتایج عددی توزیع گزینه‌های درست در ۲۰۰ آزمون (۲۰۰۰ سوال):")
        positionCounts.forEach { (pos, count) ->
            val percentage = (count.toFloat() / totalQuestionsTested) * 100f
            println("گزینه ${pos + 1}: $count بار (${String.format("%.2f", percentage)}٪)")
            // Each position must be close to 25% (between 20% and 30%)
            assertTrue("Position ${pos + 1} percentage ($percentage%) must be between 20% and 30%", percentage in 20.0f..30.0f)
        }
    }

    @Test
    fun testUniqueOptionsAcrossAllQuestions() {
        // Verification: confirm that in every single question, all 4 options are distinct
        PhysicsData.quizQuestions.forEach { q ->
            val uniqueOptions = q.options.toSet()
            assertEquals("Question ID ${q.id} has duplicate options: ${q.options}", 4, uniqueOptions.size)
            assertTrue("Question ID ${q.id} correctIndex out of bounds", q.correctIndex in 0..3)
            assertTrue("Question ID ${q.id} explanation must not be empty", q.explanation.isNotBlank())
        }
        println("All ${PhysicsData.quizQuestions.size} questions verified with 4 unique options each!")
    }

    @Test
    fun testFullTenQuestionExamSimulation() {
        val questions = PhysicsData.quizQuestions.shuffled(Random(42)).take(10)
        println("Simulating 10-Question Exam:")

        var scoreIfAllCorrect = 0
        var scoreIfAllWrong = 0

        questions.forEachIndexed { idx, q ->
            val shuffled = q.getShuffledOptions(Random(100 + idx))
            val correctIdx = shuffled.indexOfFirst { it.isCorrect }
            val wrongIdx = shuffled.indexOfFirst { !it.isCorrect }

            println("Q${idx + 1} (سطح ${q.difficulty} - ${q.grade} - ${q.topic}): Correct option at position ${correctIdx + 1}")

            if (shuffled[correctIdx].isCorrect) {
                scoreIfAllCorrect++
            }
            if (shuffled[wrongIdx].isCorrect) {
                scoreIfAllWrong++
            }
        }

        val percentage100 = (scoreIfAllCorrect.toFloat() / questions.size) * 100f
        val percentage0 = (scoreIfAllWrong.toFloat() / questions.size) * 100f

        println("Score when selecting correct options: $percentage100%")
        println("Score when selecting wrong options: $percentage0%")

        assertEquals(100f, percentage100, 0.01f)
        assertEquals(0f, percentage0, 0.01f)
    }
}
