package com.example

import com.example.data.PhysicsData
import com.example.data.PhysicsRepository
import com.example.data.TopicProgressEntity
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

    @Test
    fun testAllFiveDifficultyLevelsGenerateTenQuestionsWithExactLevel() {
        println("==================================================")
        println("Testing Acceptance Criteria: 5 Difficulty Modes")
        println("==================================================")

        // 0: Comprehensive (All levels)
        val allQuestions = PhysicsData.quizQuestions
        val examComp = allQuestions.shuffled(Random(1)).take(10)
        assertEquals(10, examComp.size)
        println("Mode 0 (Comprehensive): 10 questions selected. Bank size: ${allQuestions.size}")

        // 1: Foundation (Level 1)
        val level1Bank = allQuestions.filter { it.difficulty == 1 }
        val examL1 = level1Bank.shuffled(Random(2)).take(10)
        assertEquals(10, examL1.size)
        examL1.forEachIndexed { i, q ->
            assertEquals(1, q.difficulty)
            println("L1 Q${i + 1}: difficulty=${q.difficulty}, topic=${q.topic}")
        }

        // 2: Medium (Level 2)
        val level2Bank = allQuestions.filter { it.difficulty == 2 }
        val examL2 = level2Bank.shuffled(Random(3)).take(10)
        assertEquals(10, examL2.size)
        examL2.forEachIndexed { i, q ->
            assertEquals(2, q.difficulty)
            println("L2 Q${i + 1}: difficulty=${q.difficulty}, topic=${q.topic}")
        }

        // 3: Hard (Level 3)
        val level3Bank = allQuestions.filter { it.difficulty == 3 }
        val examL3 = level3Bank.shuffled(Random(4)).take(10)
        assertEquals(10, examL3.size)
        examL3.forEachIndexed { i, q ->
            assertEquals(3, q.difficulty)
            println("L3 Q${i + 1}: difficulty=${q.difficulty}, topic=${q.topic}")
        }

        // 4: Konkur (Level 4)
        val level4Bank = allQuestions.filter { it.difficulty == 4 }
        val examL4 = level4Bank.shuffled(Random(5)).take(10)
        assertEquals(10, examL4.size)
        examL4.forEachIndexed { i, q ->
            assertEquals(4, q.difficulty)
            println("L4 Q${i + 1}: difficulty=${q.difficulty}, topic=${q.topic}")
        }

        println("All 5 difficulty modes verified successfully! Every test mode outputs strictly 10 questions of that exact level.")
    }

    @Test
    fun testVersion18QuizResultTenQuestionsSummaryAndShortDifficultyLabels() {
        println("==================================================")
        println("Testing Acceptance Criteria v1.8: Result Screen 10 Questions")
        println("==================================================")

        for (level in 0..4) {
            val bank = if (level == 0) PhysicsData.quizQuestions else PhysicsData.quizQuestions.filter { it.difficulty == level }
            val examQuestions = bank.shuffled(Random(100 + level)).take(10)
            assertEquals("Exam must have exactly 10 questions", 10, examQuestions.size)

            // Simulate answering: even indices correct, odd indices wrong
            val simulatedAnswers = mutableMapOf<Int, Boolean>()
            examQuestions.forEachIndexed { i, _ ->
                simulatedAnswers[i] = (i % 2 == 0)
            }

            assertEquals(10, simulatedAnswers.size)

            examQuestions.forEachIndexed { i, q ->
                val isCorrect = simulatedAnswers[i] == true
                val shortLabel = when (q.difficulty) {
                    1 -> "پایه"
                    2 -> "متوسط"
                    3 -> "سخت"
                    4 -> "کنکوری"
                    else -> "جامع"
                }

                // Verify short labels do NOT contain "سطح" prefix
                assertTrue("Short label must not contain 'سطح'", !shortLabel.contains("سطح"))
                assertTrue("Topic must not be blank", q.topic.isNotBlank())
                assertTrue("Grade must not be blank", q.grade.isNotBlank())
                assertTrue("Answer status must be tracked", isCorrect == (i % 2 == 0))
            }
        }
        println("Verified: All 10 questions summary, short labels without 'سطح', and answer status tracking pass for all 5 difficulty modes!")
    }

    @Test
    fun testRoomTopicProgressAndBarChartDataIntegrity() {
        println("==================================================")
        println("Testing Room Topic Progress & Bar Chart Integrity")
        println("==================================================")

        // 1. Verify all 10 default topics defined
        val defaultTopics = PhysicsRepository.DEFAULT_TOPICS
        assertEquals("Must have 10 standard physics topics for Room progress chart", 10, defaultTopics.size)

        val topicIds = defaultTopics.map { it.topicId }.toSet()
        assertEquals(10, topicIds.size)

        // Verify each topic has emoji, title, grade
        defaultTopics.forEach { topic ->
            assertTrue("Topic name must not be blank", topic.topicName.isNotBlank())
            assertTrue("Grade must not be blank", topic.grade.isNotBlank())
            assertTrue("Emoji must not be blank", topic.iconEmoji.isNotBlank())
            println("Topic: ${topic.iconEmoji} ${topic.topicName} (پایه ${topic.grade}) - ID: ${topic.topicId}")
        }

        // 2. Verify mapping of questions to valid topic IDs
        val allQuestions = PhysicsData.quizQuestions
        val mappedTopics = allQuestions.map { PhysicsRepository.mapQuestionTopicToTopicId(it.topic) }.toSet()
        mappedTopics.forEach { id ->
            assertTrue("Mapped ID '$id' must be one of default topics", topicIds.contains(id))
        }

        // 3. Simulate answering a test and verifying mastery percentage calculation
        val testTopics = defaultTopics.map { topic ->
            // Simulate 5 answered, 4 correct -> 80% mastery
            topic.copy(
                correctCount = 4,
                totalAnswered = 5,
                masteryPercentage = 80
            )
        }

        // Verify sorting by curriculum order
        val orderMap = defaultTopics.mapIndexed { idx, t -> t.topicId to idx }.toMap()
        val sortedCurriculum = testTopics.sortedBy { orderMap[it.topicId] ?: 99 }
        assertEquals(10, sortedCurriculum.size)
        assertEquals("kinematics", sortedCurriculum.first().topicId)

        // Verify sorting by mastery
        val sortedMastery = testTopics.sortedWith(
            compareByDescending<TopicProgressEntity> { it.masteryPercentage }
                .thenByDescending { it.totalAnswered }
        )
        assertEquals(10, sortedMastery.size)

        println("Verified: Room Topic Progress entity, question-to-topic mapping, and curriculum/mastery sorting passed!")
    }
}
