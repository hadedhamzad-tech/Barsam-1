package com.example.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PhysicsData
import com.example.data.PhysicsRepository
import com.example.data.QuizScoreEntity
import com.example.data.SavedProblemEntity
import com.example.model.QuizOption
import com.example.ui.achievements.AchievementsView
import com.example.ui.theme.*
import com.example.util.LtrMathBox
import com.example.util.PersianUtils
import com.example.util.QuizSoundHelper
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun QuizScreen(repository: PhysicsRepository) {
    val coroutineScope = rememberCoroutineScope()
    val savedProblems by repository.savedProblems.collectAsStateWithLifecycle(initialValue = emptyList())
    val pastScores by repository.quizScores.collectAsStateWithLifecycle(initialValue = emptyList())
    val achievements by repository.achievements.collectAsStateWithLifecycle(initialValue = emptyList())
    val userProgress by repository.userProgress.collectAsStateWithLifecycle(initialValue = null)

    var selectedTab by remember { mutableIntStateOf(0) } // 0: آزمون فیزیک, 1: کارنامه و تاریخچه, 2: مدال‌ها و امتیازات, 3: مسائل ذخیره شده

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Tab Selector Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = PhysicsCyan,
            edgePadding = 8.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("آزمون فیزیک", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("کارنامه و روند", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("مدال‌ها و افتخارات 🏅", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp), tint = PhysicsAmber) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    Text(
                        "مسائل ذخیره (${PersianUtils.formatInt(savedProblems.size)})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> QuizRunner(
                onQuizCompleted = { correct, total, diffLevel, diffTitle ->
                    coroutineScope.launch {
                        // Accurate rounding: e.g. 1 of 8 -> 12.5% rounds up to 13%
                        val scorePct = kotlin.math.round((correct.toFloat() / total) * 100f).toInt()
                        repository.recordQuizScore(
                            QuizScoreEntity(
                                topic = "آزمون $diffTitle فیزیک",
                                correctAnswers = correct,
                                totalQuestions = total,
                                scorePercentage = scorePct
                            ),
                            difficultyLevel = diffLevel
                        )
                    }
                }
            )
            1 -> PastScoresView(scores = pastScores)
            2 -> AchievementsView(
                achievements = achievements,
                userProgress = userProgress
            )
            3 -> SavedProblemsView(
                problems = savedProblems,
                onDelete = { id ->
                    coroutineScope.launch { repository.deleteProblem(id) }
                }
            )
        }
    }
}

@Composable
fun QuizRunner(onQuizCompleted: (Int, Int, Int, String) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Difficulty filter state: 0=ترکیبی, 1=پایه (آسان), 2=متوسط, 3=سخت, 4=کنکوری
    var selectedDifficulty by remember { mutableIntStateOf(0) }
    var quizSeed by remember { mutableIntStateOf(0) }

    val filteredBank = remember(selectedDifficulty) {
        if (selectedDifficulty == 0) {
            PhysicsData.quizQuestions
        } else {
            PhysicsData.quizQuestions.filter { it.difficulty == selectedDifficulty }
        }
    }

    val questions = remember(quizSeed, filteredBank) {
        filteredBank.shuffled().take(10)
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isQuizFinished by remember { mutableStateOf(false) }

    // Sound toggle state
    var isSoundActive by remember { mutableStateOf(QuizSoundHelper.isSoundEnabled(context)) }

    // Difficulty metadata helper
    fun getDiffBadge(diff: Int): Triple<String, Color, Color> {
        return when (diff) {
            1 -> Triple("سطح ۱: پایه", Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f))
            2 -> Triple("سطح ۲: متوسط", Color(0xFF38BDF8), Color(0xFF38BDF8).copy(alpha = 0.15f))
            3 -> Triple("سطح ۳: سخت", Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f))
            4 -> Triple("سطح ۴: کنکوری", Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f))
            else -> Triple("ترکیبی همه سطوح", PhysicsCyan, PhysicsCyan.copy(alpha = 0.15f))
        }
    }

    val difficultyOptions = listOf(
        0 to "همه سطوح (ترکیبی)",
        1 to "پایه (آسان)",
        2 to "متوسط",
        3 to "سخت",
        4 to "کنکوری (پیشرفته)"
    )

    if (isQuizFinished) {
        // Final Score View
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val currentDiffInfo = getDiffBadge(selectedDifficulty)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("نتیجه آزمون جامع فیزیک", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PhysicsCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = currentDiffInfo.third,
                        border = androidx.compose.foundation.BorderStroke(1.dp, currentDiffInfo.second.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = currentDiffInfo.first,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentDiffInfo.second,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Accurate rounding formula: 1 of 8 = 13%
                val percentage = kotlin.math.round((correctCount.toFloat() / questions.size) * 100f).toInt()
                val isPassed = percentage >= 70
                val badgeColor = if (isPassed) PhysicsGreen else PhysicsAmber

                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, badgeColor),
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${PersianUtils.formatInt(percentage)}٪",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                            Text(
                                "${PersianUtils.formatInt(correctCount)} از ${PersianUtils.formatInt(questions.size)} سوال درست",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = when {
                        percentage >= 85 -> "فوق‌العاده! تسلط شما بر مفاهیم فیزیک عالی و کنکوری است. 🌟"
                        percentage >= 60 -> "بسیار خوب! با مرور فرمول‌نامه می‌توانید به درصد ۱۰۰٪ برسید. 👍"
                        else -> "نیاز به مرور بیشتر. حتماً بخش شبیه‌سازی‌ها و حل مسائل گام‌به‌گام را مرور فرمایید. 📚"
                    },
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Earned Points with Difficulty Multiplier
                val diffMultiplier = when (selectedDifficulty) {
                    1 -> 1.0f
                    2 -> 1.2f
                    3 -> 1.5f
                    4 -> 2.0f
                    else -> 1.4f
                }
                val earnedPoints = kotlin.math.round((percentage / 5f) * diffMultiplier).toInt().coerceAtLeast(0)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PhysicsAmber.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("✨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "امتیاز کسب‌شده: +${PersianUtils.formatInt(earnedPoints)} امتیاز (بر مبنای درصد و ضریب دشواری)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PhysicsAmber
                        )
                        if (percentage >= 70) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🏅 مدال آزمون باز شد!", fontSize = 11.sp, color = PhysicsGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Summary of questions and difficulty breakdown in result
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("📊 وضعیت سوالات این آزمون:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PhysicsCyan)
                        questions.forEachIndexed { i, qItem ->
                            val badge = getDiffBadge(qItem.difficulty)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "سوال ${PersianUtils.formatInt(i + 1)}: ${qItem.grade} - ${qItem.topic}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = badge.third
                                ) {
                                    Text(
                                        badge.first,
                                        fontSize = 9.sp,
                                        color = badge.second,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        quizSeed++
                        currentIndex = 0
                        selectedOptionIndex = null
                        isAnswerSubmitted = false
                        correctCount = 0
                        isQuizFinished = false
                        coroutineScope.launch {
                            listState.scrollToItem(0)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PhysicsCyan)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("شروع مجدد با سوالات تصادفی جدید", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        val q = questions[currentIndex]

        // Shuffled options using standard Fisher-Yates per question; purely balanced 25% distribution
        val shuffledOptions = remember(quizSeed, currentIndex, q.id) {
            q.getShuffledOptions()
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // DIFFICULTY SELECTOR CHIPS ROW (shown before starting answering or during exam)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "سطح دشواری آزمون:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "بانک فعال: ${PersianUtils.formatInt(filteredBank.size)} سوال",
                            fontSize = 9.sp,
                            color = PhysicsCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Horizontally scrollable difficulty selection chips
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(difficultyOptions) { (diffId, title) ->
                            val isSelected = selectedDifficulty == diffId
                            val badgeInfo = getDiffBadge(diffId)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) badgeInfo.second else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) badgeInfo.second else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.clickable {
                                    if (selectedDifficulty != diffId) {
                                        selectedDifficulty = diffId
                                        quizSeed++
                                        currentIndex = 0
                                        selectedOptionIndex = null
                                        isAnswerSubmitted = false
                                        correctCount = 0
                                    }
                                }
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) DarkBackground else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // FIXED HEADER: Always visible above question card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val currentQBadge = getDiffBadge(q.difficulty)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = currentQBadge.third,
                            border = androidx.compose.foundation.BorderStroke(1.dp, currentQBadge.second.copy(alpha = 0.5f))
                        ) {
                            Text(
                                currentQBadge.first,
                                fontSize = 10.sp,
                                color = currentQBadge.second,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PhysicsCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "${q.grade} • ${q.topic}",
                                fontSize = 10.sp,
                                color = PhysicsCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        // Sound FX toggle button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSoundActive) PhysicsAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSoundActive) PhysicsAmber.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.clickable {
                                val newState = !isSoundActive
                                isSoundActive = newState
                                QuizSoundHelper.setSoundEnabled(context, newState)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSoundActive) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "تنظیم صدای آزمون",
                                    tint = if (isSoundActive) PhysicsAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isSoundActive) "صدا" else "بی‌صدا",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSoundActive) PhysicsAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        "سوال ${PersianUtils.formatInt(currentIndex + 1)} از ${PersianUtils.formatInt(questions.size)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = PhysicsCyan,
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Scrollable Question Content (resets to top on next question)
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Question Text
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = q.question,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // 4 Shuffled Options (numbers 1-4 displayed by position after shuffle)
                items(shuffledOptions.indices.toList()) { index ->
                    val option = shuffledOptions[index]
                    val isSelected = selectedOptionIndex == index
                    val isCorrect = option.isCorrect

                    val borderColor = when {
                        !isAnswerSubmitted -> if (isSelected) PhysicsCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        isCorrect -> PhysicsGreen
                        isSelected -> PhysicsRed
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }

                    val bgColor = when {
                        !isAnswerSubmitted -> if (isSelected) PhysicsCyan.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                        isCorrect -> PhysicsGreen.copy(alpha = 0.15f)
                        isSelected -> PhysicsRed.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isAnswerSubmitted) {
                                selectedOptionIndex = index
                                isAnswerSubmitted = true

                                // Evaluate correctness by the option object itself
                                if (option.isCorrect) {
                                    correctCount++
                                    QuizSoundHelper.playCorrectSound(context)
                                } else {
                                    QuizSoundHelper.playIncorrectSound(context)
                                }

                                if (currentIndex == questions.size - 1) {
                                    val currentDiffTitle = difficultyOptions.firstOrNull { it.first == selectedDifficulty }?.second ?: "جامع"
                                    onQuizCompleted(correctCount, questions.size, selectedDifficulty, currentDiffTitle)
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = bgColor,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = borderColor.copy(alpha = 0.3f),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        PersianUtils.formatInt(index + 1),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = option.text,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || (isAnswerSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isAnswerSubmitted) {
                                if (isCorrect) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PhysicsGreen)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = PhysicsRed)
                                }
                            }
                        }
                    }
                }

                // Explanation box when answered
                if (isAnswerSubmitted) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💡", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "تحلیل و راهنمای حل تشریحی گام‌به‌گام:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PhysicsCyan
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))

                                // Split lines to keep math equations isolated in LTR
                                val lines = q.explanation.lines()
                                lines.forEach { line ->
                                    val trimmed = line.trim()
                                    val isEquationLine = trimmed.contains("=") && (trimmed.contains("+") || trimmed.contains("-") || trimmed.contains("×") || trimmed.contains("·") || trimmed.contains("/") || trimmed.any { it in 'a'..'z' || it in 'A'..'Z' })
                                    if (isEquationLine) {
                                        LtrMathBox(
                                            formula = trimmed,
                                            fontSize = 12,
                                            backgroundColor = Color(0xFF1E293B),
                                            textColor = PhysicsCyan
                                        )
                                    } else if (trimmed.isNotEmpty()) {
                                        Text(
                                            text = trimmed,
                                            fontSize = 12.sp,
                                            lineHeight = 19.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Next Button (scrolls to top of next question)
                    item {
                        Button(
                            onClick = {
                                if (currentIndex < questions.size - 1) {
                                    currentIndex++
                                    selectedOptionIndex = null
                                    isAnswerSubmitted = false
                                    coroutineScope.launch {
                                        listState.scrollToItem(0)
                                    }
                                } else {
                                    isQuizFinished = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PhysicsCyan)
                        ) {
                            Text(
                                if (currentIndex < questions.size - 1) "سوال بعدی" else "مشاهده کارنامه نهایی",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = DarkBackground)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual Progress Trend Chart for Quiz Scores
 */
@Composable
fun QuizProgressChart(scores: List<QuizScoreEntity>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PhysicsCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "نمودار روند پیشرفت آزمون‌ها",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                val avgScore = scores.map { it.scorePercentage }.average().roundToInt()
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PhysicsCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        "میانگین: ${PersianUtils.formatInt(avgScore)}٪",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PhysicsCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Show up to the last 8 scores chronologically
            val displayScores = remember(scores) { scores.takeLast(8) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasW = size.width
                    val canvasH = size.height
                    val bottomY = canvasH - 20.dp.toPx()
                    val chartH = bottomY - 14.dp.toPx()

                    // Passing threshold dashed line at 70%
                    val line70Y = bottomY - (chartH * 0.70f)
                    drawLine(
                        color = PhysicsGreen.copy(alpha = 0.35f),
                        start = Offset(0f, line70Y),
                        end = Offset(canvasW, line70Y),
                        strokeWidth = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )

                    val count = displayScores.size
                    val slotW = canvasW / count.coerceAtLeast(1)
                    val barW = (slotW * 0.45f).coerceIn(12.dp.toPx(), 26.dp.toPx())

                    for (i in displayScores.indices) {
                        val score = displayScores[i]
                        val pct = score.scorePercentage.coerceIn(0, 100)
                        val barH = (chartH * (pct / 100f)).coerceAtLeast(4.dp.toPx())
                        val centerX = (i + 0.5f) * slotW
                        val barLeft = centerX - barW / 2f
                        val barTop = bottomY - barH

                        val barColor = if (pct >= 70) PhysicsGreen else PhysicsAmber

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(barColor, barColor.copy(alpha = 0.45f)),
                                startY = barTop,
                                endY = bottomY
                            ),
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barW, barH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                // Labels below and above each bar
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    displayScores.forEachIndexed { idx, item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                text = "${PersianUtils.formatInt(item.scorePercentage)}٪",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.scorePercentage >= 70) PhysicsGreen else PhysicsAmber
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = PersianUtils.formatInt(idx + 1),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "شماره آزمون (از ۱ تا ${PersianUtils.formatInt(displayScores.size)})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "خط‌چین: حد قبولی ۷۰٪",
                    fontSize = 10.sp,
                    color = PhysicsGreen.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun PastScoresView(scores: List<QuizScoreEntity>) {
    if (scores.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(54.dp), tint = PhysicsCyan)
                Spacer(modifier = Modifier.height(12.dp))
                Text("هنوز در هیچ آزمونی شرکت نکرده‌اید.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("از تب اول آزمون دهید تا کارنامه شما اینجا ذخیره شود!", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Visual Progress Trend Chart Header
            item {
                QuizProgressChart(scores = scores)
            }

            item {
                Text(
                    "تاریخچه و کارنامه تفصیلی آزمون‌ها (${PersianUtils.formatInt(scores.size)} آزمون)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(scores.reversed()) { score ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(score.topic, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            // Solar Hijri / Jalali Date & Time (e.g. ۱۴۰۵/۰۷/۱۳ ۲۲:۲۲)
                            Text(
                                PersianUtils.formatJalaliDateTime(score.timestamp),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${PersianUtils.formatInt(score.correctAnswers)} از ${PersianUtils.formatInt(score.totalQuestions)} سوال درست",
                                fontSize = 11.sp,
                                color = if (score.scorePercentage >= 70) PhysicsGreen else PhysicsAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (score.scorePercentage >= 70) PhysicsGreen.copy(alpha = 0.2f) else PhysicsAmber.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (score.scorePercentage >= 70) PhysicsGreen else PhysicsAmber)
                        ) {
                            Text(
                                "${PersianUtils.formatInt(score.scorePercentage)}٪",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (score.scorePercentage >= 70) PhysicsGreen else PhysicsAmber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedProblemsView(
    problems: List<SavedProblemEntity>,
    onDelete: (Int) -> Unit
) {
    if (problems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(54.dp), tint = PhysicsCyan)
                Spacer(modifier = Modifier.height(12.dp))
                Text("هیچ مسئله‌ای ذخیره نشده است.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("در بخش حل‌المسائل می‌توانید مسائل حل شده را ذخیره نمایید.", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(problems) { problem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(problem.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                // Solar Hijri / Jalali Date & Time
                                Text(
                                    "${problem.category} • ${PersianUtils.formatJalaliDateTime(problem.timestamp)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDelete(problem.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = PhysicsRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ورودی‌ها: ${PersianUtils.toPersianDigits(problem.inputSummary)}",
                            fontSize = 11.sp,
                            color = PhysicsCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "نتیجه: ${PersianUtils.toPersianDigits(problem.resultSummary)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PhysicsGreen
                        )
                    }
                }
            }
        }
    }
}
