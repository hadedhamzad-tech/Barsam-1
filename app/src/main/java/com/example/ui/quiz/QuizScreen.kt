package com.example.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PhysicsData
import com.example.data.PhysicsRepository
import com.example.data.QuizScoreEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QuizScreen(repository: PhysicsRepository) {
    val coroutineScope = rememberCoroutineScope()
    val savedProblems by repository.savedProblems.collectAsStateWithLifecycle(initialValue = emptyList())
    val pastScores by repository.quizScores.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0: آزمون فیزیک, 1: کارنامه و تاریخچه, 2: مسائل ذخیره شده

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Tab Selector Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = PhysicsCyan
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("آزمون فیزیک", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("کارنامه آزمون‌ها", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("مسائل ذخیره‌شده (${savedProblems.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> QuizRunner(
                onQuizCompleted = { correct, total ->
                    coroutineScope.launch {
                        val scorePct = ((correct.toFloat() / total) * 100).toInt()
                        repository.recordQuizScore(
                            QuizScoreEntity(
                                topic = "آزمون جامع فیزیک دبیرستان",
                                correctAnswers = correct,
                                totalQuestions = total,
                                scorePercentage = scorePct
                            )
                        )
                    }
                }
            )
            1 -> PastScoresView(scores = pastScores)
            2 -> SavedProblemsView(
                problems = savedProblems,
                onDelete = { id ->
                    coroutineScope.launch { repository.deleteProblem(id) }
                }
            )
        }
    }
}

@Composable
fun QuizRunner(onQuizCompleted: (Int, Int) -> Unit) {
    val questions = remember { PhysicsData.quizQuestions.shuffled() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isQuizFinished by remember { mutableStateOf(false) }

    if (isQuizFinished) {
        // Final Score View
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("نتیجه آزمون جامع فیزیک", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PhysicsCyan)

                val percentage = ((correctCount.toFloat() / questions.size) * 100).toInt()
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (percentage >= 70) PhysicsGreen.copy(alpha = 0.2f) else PhysicsAmber.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (percentage >= 70) PhysicsGreen else PhysicsAmber
                    ),
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$percentage%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (percentage >= 70) PhysicsGreen else PhysicsAmber
                            )
                            Text(
                                "$correctCount از ${questions.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = when {
                        percentage >= 85 -> "فوق‌العاده! تسلط شما بر مفاهیم فیزیک عالی است. 🌟"
                        percentage >= 60 -> "خوب بود! با مرور نکات فرمول‌نامه می‌توانید درصد بالاتری کسب کنید. 👍"
                        else -> "نیاز به تمرین بیشتر. حتماً بخش شبیه‌سازی‌ها و حل مسائل را مرور کنید. 📚"
                    },
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = {
                        currentIndex = 0
                        selectedOptionIndex = null
                        isAnswerSubmitted = false
                        correctCount = 0
                        isQuizFinished = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PhysicsCyan)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("شروع مجدد آزمون", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        val q = questions[currentIndex]

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Question Progress & Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PhysicsCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "${q.grade} • ${q.topic}",
                            fontSize = 11.sp,
                            color = PhysicsCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        "سوال ${currentIndex + 1} از ${questions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier.fillMaxWidth(),
                    color = PhysicsCyan,
                    trackColor = Color(0xFF1E293B)
                )
            }

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

            // 4 Options
            items(q.options.indices.toList()) { index ->
                val optionText = q.options[index]
                val isSelected = selectedOptionIndex == index
                val isCorrect = index == q.correctIndex

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
                            if (index == q.correctIndex) {
                                correctCount++
                            }
                            if (currentIndex == questions.size - 1) {
                                onQuizCompleted(correctCount + if (index == q.correctIndex) 0 else 0, questions.size)
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
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    (index + 1).toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = optionText,
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
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "تحلیل و راهنمای حل:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhysicsCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = q.explanation,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Next Button
                item {
                    Button(
                        onClick = {
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                                selectedOptionIndex = null
                                isAnswerSubmitted = false
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
                Text("از تب اول آزمون بدهید تا کارنامه شما اینجا ذخیره شود!", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(scores) { score ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(score.topic, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                dateFormat.format(Date(score.timestamp)),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${score.correctAnswers} از ${score.totalQuestions} سوال درست",
                                fontSize = 11.sp,
                                color = PhysicsGreen
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (score.scorePercentage >= 70) PhysicsGreen.copy(alpha = 0.2f) else PhysicsAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "${score.scorePercentage}%",
                                fontSize = 15.sp,
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
    problems: List<com.example.data.SavedProblemEntity>,
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
        val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(problems) { problem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(problem.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "${problem.category} • ${dateFormat.format(Date(problem.timestamp))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDelete(problem.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = PhysicsRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ورودی‌ها: ${problem.inputSummary}",
                            fontSize = 11.sp,
                            color = PhysicsCyan
                        )
                        Text(
                            text = "نتیجه: ${problem.resultSummary}",
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
