package com.example.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PhysicsData
import com.example.data.PhysicsRepository
import com.example.data.QuizScoreEntity
import com.example.data.SavedProblemEntity
import com.example.data.TopicProgressEntity
import com.example.model.QuizOption
import com.example.model.QuizQuestion
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
    val topicProgress by repository.topicProgress.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(Unit) {
        repository.ensureDefaultTopics()
    }
    val achievements by repository.achievements.collectAsStateWithLifecycle(initialValue = emptyList())
    val userProgress by repository.userProgress.collectAsStateWithLifecycle(initialValue = null)

    // Preserved across configuration changes, scroll, and tab transitions
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: آزمون فیزیک, 1: کارنامه و تاریخچه, 2: مدال‌ها و امتیازات, 3: مسائل ذخیره شده
    var sortByMastery by rememberSaveable { mutableStateOf(false) }
    var selectedTopicId by rememberSaveable { mutableStateOf<String?>(null) }

    // Preserved scroll positions for each tab
    val quizLevelListState = rememberLazyListState()
    val quizResultListState = rememberLazyListState()
    val pastScoresListState = rememberLazyListState()
    val achievementsListState = rememberLazyListState()
    val savedProblemsListState = rememberLazyListState()

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
                onQuizCompleted = { correct, total, diffLevel, diffTitle, qList, answersMap ->
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
                        repository.recordQuestionResults(qList, answersMap)
                    }
                },
                levelListState = quizLevelListState,
                resultListState = quizResultListState
            )
            1 -> PastScoresView(
                scores = pastScores,
                topicProgress = topicProgress,
                sortByMastery = sortByMastery,
                onSortByMasteryChange = { sortByMastery = it },
                selectedTopicId = selectedTopicId,
                onSelectedTopicIdChange = { selectedTopicId = it },
                listState = pastScoresListState
            )
            2 -> AchievementsView(
                achievements = achievements,
                userProgress = userProgress,
                listState = achievementsListState
            )
            3 -> SavedProblemsView(
                problems = savedProblems,
                onDelete = { id ->
                    coroutineScope.launch { repository.deleteProblem(id) }
                },
                listState = savedProblemsListState
            )
        }
    }
}

@Composable
fun QuizRunner(
    onQuizCompleted: (Int, Int, Int, String, List<QuizQuestion>, Map<Int, Boolean>) -> Unit,
    levelListState: LazyListState = rememberLazyListState(),
    resultListState: LazyListState = rememberLazyListState()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Screen state preserved across configuration changes and tab switches
    var selectedDifficulty by rememberSaveable { mutableStateOf<Int?>(null) }
    var quizSeed by rememberSaveable { mutableIntStateOf(0) }

    val activeDifficulty = selectedDifficulty ?: 0
    val filteredBank = remember(activeDifficulty) {
        if (activeDifficulty == 0) {
            PhysicsData.quizQuestions
        } else {
            PhysicsData.quizQuestions.filter { it.difficulty == activeDifficulty }
        }
    }

    val questions = remember(quizSeed, activeDifficulty) {
        val rnd = if (quizSeed != 0) kotlin.random.Random(quizSeed) else kotlin.random.Random.Default
        filteredBank.shuffled(rnd).take(10)
    }

    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedOptionIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by rememberSaveable { mutableStateOf(false) }
    var correctCount by rememberSaveable { mutableIntStateOf(0) }
    var isQuizFinished by rememberSaveable { mutableStateOf(false) }

    val answersSaver = remember {
        Saver<SnapshotStateMap<Int, Boolean>, String>(
            save = { map -> map.entries.joinToString(",") { "${it.key}:${it.value}" } },
            restore = { str ->
                mutableStateMapOf<Int, Boolean>().apply {
                    if (str.isNotBlank()) {
                        str.split(",").forEach { item ->
                            val parts = item.split(":")
                            if (parts.size == 2) {
                                put(parts[0].toInt(), parts[1].toBoolean())
                            }
                        }
                    }
                }
            }
        )
    }
    val userAnswers = rememberSaveable(saver = answersSaver) { mutableStateMapOf<Int, Boolean>() }

    // Sound toggle state
    var isSoundActive by rememberSaveable { mutableStateOf(QuizSoundHelper.isSoundEnabled(context)) }

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

    // Helper to start/restart quiz for a specific difficulty level
    fun startQuizForLevel(level: Int) {
        selectedDifficulty = level
        quizSeed = (System.currentTimeMillis() % 1000000).toInt() + 1
        currentIndex = 0
        selectedOptionIndex = null
        isAnswerSubmitted = false
        correctCount = 0
        userAnswers.clear()
        isQuizFinished = false
        coroutineScope.launch {
            resultListState.scrollToItem(0)
        }
    }

    if (selectedDifficulty == null) {
        // LEVEL SELECTION SCREEN (Shown before starting quiz)
        LazyColumn(
            state = levelListState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp, start = 2.dp, end = 2.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PhysicsCyan.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "انتخاب سطح دشواری آزمون فیزیک",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PhysicsCyan
                            )
                        }
                        Text(
                            "سطح مورد نظر خود را انتخاب فرمایید. هر آزمون شامل ۱۰ سوال تصادفی با توزیع استاندارد است. با انتخاب سطوح سخت‌تر، ضریب امتیاز کسب‌شده بیشتر خواهد بود.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val levelCards = listOf(
                LevelCardData(
                    level = 0,
                    title = "جامع (ترکیبی همه سطوح)",
                    subtitle = "ترکیب متعادل از تمامی پایه‌ها و سطوح دهم تا دوازدهم",
                    questionCount = PhysicsData.quizQuestions.size,
                    multiplier = "1.4x",
                    badgeText = "ضریب ۱٫۴",
                    badgeColor = PhysicsCyan,
                    containerColor = PhysicsCyan.copy(alpha = 0.12f),
                    borderColor = PhysicsCyan
                ),
                LevelCardData(
                    level = 1,
                    title = "سطح ۱: پایه (آسان)",
                    subtitle = "مفاهیم بنیادی، فرمول‌های مستقیم و محاسبات تک‌مرحله‌ای",
                    questionCount = PhysicsData.quizQuestions.count { it.difficulty == 1 },
                    multiplier = "1.0x",
                    badgeText = "ضریب ۱٫۰",
                    badgeColor = Color(0xFF10B981),
                    containerColor = Color(0xFF10B981).copy(alpha = 0.12f),
                    borderColor = Color(0xFF10B981)
                ),
                LevelCardData(
                    level = 2,
                    title = "سطح ۲: متوسط",
                    subtitle = "مسائل استاندارد امتحانات نهایی با تحلیل داده‌ها و نمودار",
                    questionCount = PhysicsData.quizQuestions.count { it.difficulty == 2 },
                    multiplier = "1.2x",
                    badgeText = "ضریب ۱٫۲",
                    badgeColor = Color(0xFF38BDF8),
                    containerColor = Color(0xFF38BDF8).copy(alpha = 0.12f),
                    borderColor = Color(0xFF38BDF8)
                ),
                LevelCardData(
                    level = 3,
                    title = "سطح ۳: سخت",
                    subtitle = "مسائل چندمرحله‌ای، تبدیل یکاها و تحلیل خطاهای متداول",
                    questionCount = PhysicsData.quizQuestions.count { it.difficulty == 3 },
                    multiplier = "1.5x",
                    badgeText = "ضریب ۱٫۵",
                    badgeColor = Color(0xFFF59E0B),
                    containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f),
                    borderColor = Color(0xFFF59E0B)
                ),
                LevelCardData(
                    level = 4,
                    title = "سطح ۴: کنکوری (پیشرفته)",
                    subtitle = "تست‌های چالشی و ترکیبی کنکور سراسری با راهنمای تشریحی کامل",
                    questionCount = PhysicsData.quizQuestions.count { it.difficulty == 4 },
                    multiplier = "2.0x",
                    badgeText = "ضریب ۲٫۰",
                    badgeColor = Color(0xFFEF4444),
                    containerColor = Color(0xFFEF4444).copy(alpha = 0.12f),
                    borderColor = Color(0xFFEF4444)
                )
            )

            items(levelCards) { card ->
                Surface(
                    onClick = { startQuizForLevel(card.level) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, card.borderColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = card.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = card.badgeColor
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = card.containerColor,
                                border = androidx.compose.foundation.BorderStroke(1.dp, card.borderColor.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = card.badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = card.badgeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = card.subtitle,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = card.badgeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تعداد سوالات بانک: ${PersianUtils.formatInt(card.questionCount)} سوال",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "شروع ۱۰ سوال",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = card.badgeColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = card.badgeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else if (isQuizFinished) {
        // Final Score View - fully scrollable with bottom padding to ensure no content is hidden behind navigation bar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val currentDiffInfo = getDiffBadge(activeDifficulty)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val currentTitleText = difficultyOptions.firstOrNull { it.first == activeDifficulty }?.second ?: "جامع"
                        Text("نتیجه آزمون $currentTitleText فیزیک", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PhysicsCyan)
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
                        modifier = Modifier.size(110.dp)
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
                    val diffMultiplier = when (activeDifficulty) {
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

                    // Two Buttons: Restart same level & Change level placed HIGH, right below earned points
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                startQuizForLevel(activeDifficulty)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PhysicsCyan)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("شروع مجدد همین سطح", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedDifficulty = null
                                currentIndex = 0
                                selectedOptionIndex = null
                                isAnswerSubmitted = false
                                correctCount = 0
                                userAnswers.clear()
                                isQuizFinished = false
                            },
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = PhysicsCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تغییر سطح دشواری", color = PhysicsCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Detailed summary of all 10 questions
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📊 وضعیت سوالات این آزمون (${PersianUtils.formatInt(questions.size)} سوال):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PhysicsCyan)
                            questions.forEachIndexed { i, qItem ->
                                val isCorrect = userAnswers[i] == true
                                val (shortDiffName, diffColor, diffBg) = when (qItem.difficulty) {
                                    1 -> Triple("پایه", Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f))
                                    2 -> Triple("متوسط", Color(0xFF38BDF8), Color(0xFF38BDF8).copy(alpha = 0.15f))
                                    3 -> Triple("سخت", Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f))
                                    4 -> Triple("کنکوری", Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f))
                                    else -> Triple("جامع", PhysicsCyan, PhysicsCyan.copy(alpha = 0.15f))
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (i % 2 == 0) MaterialTheme.colorScheme.surface.copy(alpha = 0.4f) else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "سوال ${PersianUtils.formatInt(i + 1)}: ${qItem.grade} - ${qItem.topic}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = diffBg
                                    ) {
                                        Text(
                                            text = shortDiffName,
                                            fontSize = 9.sp,
                                            color = diffColor,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isCorrect) PhysicsGreen.copy(alpha = 0.15f) else PhysicsRed.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                                contentDescription = if (isCorrect) "درست" else "غلط",
                                                tint = if (isCorrect) PhysicsGreen else PhysicsRed,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (isCorrect) "درست" else "غلط",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCorrect) PhysicsGreen else PhysicsRed,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
            // FIXED COMPACT HEADER (< 15% screen height, two distinct clean rows)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                // Row 1: Difficulty Badge (fixed width, single line) | Spacer | Sound Button (icon only, fixed 40dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentQBadge = getDiffBadge(q.difficulty)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = currentQBadge.third,
                        border = androidx.compose.foundation.BorderStroke(1.dp, currentQBadge.second.copy(alpha = 0.5f)),
                        modifier = Modifier.widthIn(min = 90.dp, max = 130.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentQBadge.first,
                                fontSize = 11.sp,
                                color = currentQBadge.second,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Sound FX toggle button: strictly fixed 40dp x 40dp, icon only
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSoundActive) PhysicsAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSoundActive) PhysicsAmber.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                val newState = !isSoundActive
                                isSoundActive = newState
                                QuizSoundHelper.setSoundEnabled(context, newState)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSoundActive) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "تنظیم صدای آزمون",
                                tint = if (isSoundActive) PhysicsAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Row 2: "سوال X از Y" single line, no wrapping
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سوال ${PersianUtils.formatInt(currentIndex + 1)} از ${PersianUtils.formatInt(questions.size)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = PhysicsCyan,
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Scrollable Question Content (resets to top on next question)
            LazyColumn(
                state = resultListState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
            ) {
                // Question Text & Topic Chip
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Topic & Grade badge chip inside/above question card with safe ellipsis
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PhysicsCyan.copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(
                                        text = "${q.grade} • ${q.topic}",
                                        fontSize = 11.sp,
                                        color = PhysicsCyan,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = q.question,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 4 Shuffled Options (compact minHeight = 48dp, fontSize = 13sp)
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
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable(enabled = !isAnswerSubmitted) {
                                selectedOptionIndex = index
                                isAnswerSubmitted = true
                                val isCorrectChoice = option.isCorrect
                                userAnswers[currentIndex] = isCorrectChoice

                                // Evaluate correctness by the option object itself
                                if (isCorrectChoice) {
                                    correctCount++
                                    QuizSoundHelper.playCorrectSound(context)
                                } else {
                                    QuizSoundHelper.playIncorrectSound(context)
                                }

                                if (currentIndex == questions.size - 1) {
                                    val currentDiffTitle = difficultyOptions.firstOrNull { it.first == activeDifficulty }?.second ?: "جامع"
                                    onQuizCompleted(
                                        correctCount,
                                        questions.size,
                                        activeDifficulty,
                                        currentDiffTitle,
                                        questions,
                                        userAnswers.toMap()
                                    )
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = bgColor,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = borderColor.copy(alpha = 0.3f),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = PersianUtils.formatInt(index + 1),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option.text,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || (isAnswerSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Fixed icon container (24dp) to avoid any layout jump on answer selection
                            Box(
                                modifier = Modifier.size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAnswerSubmitted) {
                                    if (isCorrect) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "پاسخ صحیح",
                                            tint = PhysicsGreen,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            Icons.Default.Cancel,
                                            contentDescription = "پاسخ غلط",
                                            tint = PhysicsRed,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
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
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💡", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "تحلیل و راهنمای حل تشریحی گام‌به‌گام:",
                                        fontSize = 12.sp,
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
                                        resultListState.scrollToItem(0)
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
 * Data holder for level selection cards
 */
data class LevelCardData(
    val level: Int,
    val title: String,
    val subtitle: String,
    val questionCount: Int,
    val multiplier: String,
    val badgeText: String,
    val badgeColor: Color,
    val containerColor: Color,
    val borderColor: Color
)

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

/**
 * Visual Bar Chart for Physics Topics Progress (Stored in Room Database)
 */
@Composable
fun TopicProgressBarChart(
    topicProgress: List<TopicProgressEntity>,
    sortByMastery: Boolean = false,
    onSortByMasteryChange: (Boolean) -> Unit = {},
    selectedTopicId: String? = null,
    onSelectedTopicIdChange: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rawTopics = remember(topicProgress) {
        if (topicProgress.isNotEmpty()) topicProgress else PhysicsRepository.DEFAULT_TOPICS
    }

    val displayTopics = remember(rawTopics, sortByMastery) {
        if (sortByMastery) {
            rawTopics.sortedWith(
                compareByDescending<TopicProgressEntity> { it.masteryPercentage }
                    .thenByDescending { it.totalAnswered }
            )
        } else {
            val orderMap = PhysicsRepository.DEFAULT_TOPICS.mapIndexed { idx, t -> t.topicId to idx }.toMap()
            rawTopics.sortedBy { orderMap[it.topicId] ?: 99 }
        }
    }

    val activeTopics = displayTopics.filter { it.totalAnswered > 0 }
    val avgMastery = if (activeTopics.isNotEmpty()) {
        activeTopics.map { it.masteryPercentage }.average().roundToInt()
    } else 0

    // Selected topic or topic with highest mastery
    val selectedTopic = remember(selectedTopicId, displayTopics) {
        displayTopics.find { it.topicId == selectedTopicId }
            ?: displayTopics.maxByOrNull { it.masteryPercentage }
            ?: displayTopics.firstOrNull()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        tint = PhysicsCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "نمودار میله‌ای پیشرفت مباحث فیزیک",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تحلیل تسلط در ۱۰ مبحث اصلی بر مبنای داده‌های Room",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PhysicsCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "میانگین: ${PersianUtils.formatInt(avgMastery)}٪",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PhysicsCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sort Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (sortByMastery) "مرتب‌شده بر مبنای بیشترین تسلط" else "ترتیب سرفصل‌های کتاب درسی (دهم تا دوازدهم)",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (!sortByMastery) PhysicsCyan else Color.Transparent,
                            onClick = { onSortByMasteryChange(false) }
                        ) {
                            Text(
                                "کتاب درسی",
                                fontSize = 9.sp,
                                fontWeight = if (!sortByMastery) FontWeight.Bold else FontWeight.Normal,
                                color = if (!sortByMastery) DarkBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (sortByMastery) PhysicsCyan else Color.Transparent,
                            onClick = { onSortByMasteryChange(true) }
                        ) {
                            Text(
                                "رتبه تسلط",
                                fontSize = 9.sp,
                                fontWeight = if (sortByMastery) FontWeight.Bold else FontWeight.Normal,
                                color = if (sortByMastery) DarkBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Bar Chart
            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasW = size.width
                    val canvasH = size.height
                    val bottomY = canvasH - 22.dp.toPx()
                    val chartH = bottomY - 16.dp.toPx()

                    // Reference line at 70% (Passing/Mastery threshold)
                    val line70Y = bottomY - (chartH * 0.70f)
                    drawLine(
                        color = PhysicsGreen.copy(alpha = 0.35f),
                        start = Offset(0f, line70Y),
                        end = Offset(canvasW, line70Y),
                        strokeWidth = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )

                    // Reference line at 50%
                    val line50Y = bottomY - (chartH * 0.50f)
                    drawLine(
                        color = PhysicsCyan.copy(alpha = 0.2f),
                        start = Offset(0f, line50Y),
                        end = Offset(canvasW, line50Y),
                        strokeWidth = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )

                    val count = displayTopics.size.coerceAtLeast(1)
                    val slotW = canvasW / count
                    val barW = (slotW * 0.55f).coerceIn(10.dp.toPx(), 22.dp.toPx())

                    for (i in displayTopics.indices) {
                        val topic = displayTopics[i]
                        val pct = topic.masteryPercentage.coerceIn(0, 100)
                        val isSelected = selectedTopic?.topicId == topic.topicId
                        val isUntested = topic.totalAnswered == 0

                        val barH = if (isUntested) 6.dp.toPx() else (chartH * (pct / 100f)).coerceAtLeast(6.dp.toPx())
                        val colIndex = if (isRtl) (count - 1 - i) else i
                        val centerX = (colIndex + 0.5f) * slotW
                        val barLeft = centerX - barW / 2f
                        val barTop = bottomY - barH

                        val barColor = when {
                            isUntested -> Color.Gray.copy(alpha = 0.35f)
                            pct >= 75 -> PhysicsGreen
                            pct >= 50 -> PhysicsCyan
                            pct >= 25 -> PhysicsAmber
                            else -> PhysicsRed
                        }

                        // Gradient fill
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = if (isSelected) {
                                    listOf(barColor, barColor.copy(alpha = 0.7f))
                                } else {
                                    listOf(barColor, barColor.copy(alpha = 0.35f))
                                },
                                startY = barTop,
                                endY = bottomY
                            ),
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barW, barH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Highlight border if selected
                        if (isSelected) {
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(barLeft - 1.dp.toPx(), barTop - 1.dp.toPx()),
                                size = Size(barW + 2.dp.toPx(), barH + 2.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }
                }

                // Percentage and Emoji labels
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    displayTopics.forEach { topic ->
                        val isSelected = selectedTopic?.topicId == topic.topicId
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onSelectedTopicIdChange(topic.topicId) }
                        ) {
                            Text(
                                text = if (topic.totalAnswered == 0) "۰٪" else "${PersianUtils.formatInt(topic.masteryPercentage)}٪",
                                fontSize = 8.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    topic.totalAnswered == 0 -> Color.Gray
                                    topic.masteryPercentage >= 75 -> PhysicsGreen
                                    topic.masteryPercentage >= 50 -> PhysicsCyan
                                    else -> PhysicsAmber
                                }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = topic.iconEmoji,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-chart guides
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "۱۰ مبحث جامع دهم تا دوازدهم",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(PhysicsGreen, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "حد تسلط: ۷۰٪",
                        fontSize = 10.sp,
                        color = PhysicsGreen.copy(alpha = 0.9f)
                    )
                }
            }

            // Selected topic highlight card
            if (selectedTopic != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedTopic.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    selectedTopic.topicName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhysicsCyan
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PhysicsCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "پایه ${selectedTopic.grade}",
                                    fontSize = 10.sp,
                                    color = PhysicsCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedTopic.totalAnswered > 0)
                                    "پاسخ درست: ${PersianUtils.formatInt(selectedTopic.correctCount)} از ${PersianUtils.formatInt(selectedTopic.totalAnswered)} سوال"
                                else "هنوز سوالی از این مبحث پاسخ داده نشده است",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val pctColor = when {
                                selectedTopic.totalAnswered == 0 -> Color.Gray
                                selectedTopic.masteryPercentage >= 75 -> PhysicsGreen
                                selectedTopic.masteryPercentage >= 50 -> PhysicsCyan
                                else -> PhysicsAmber
                            }

                            Text(
                                text = "تسلط: ${PersianUtils.formatInt(selectedTopic.masteryPercentage)}٪",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = pctColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Status message
                        Text(
                            text = when {
                                selectedTopic.totalAnswered == 0 -> "با آزمون دادن در سطوح مختلف، عملکرد شما در این مبحث ثبت می‌شود."
                                selectedTopic.masteryPercentage >= 80 -> "تسلط عالی و فوق‌العاده بر مفاهیم و مسائل این مبحث 🌟"
                                selectedTopic.masteryPercentage >= 60 -> "تسلط استاندارد و مناسب امتحانات نهایی و نهایی کشوری 👍"
                                selectedTopic.masteryPercentage >= 40 -> "نیازمند مرور فرمول‌ها و تمرین مسائل تکمیلی در حل‌المسائل 📚"
                                else -> "توصیه می‌شود شبیه‌سازها و گام‌های حل مسئله این مبحث را دوباره مرور کنید."
                            },
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detailed Horizontal Progress Bars for all 10 topics
            Text(
                text = "📊 جزئیات پیشرفت میله‌ای مباحث فیزیک:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PhysicsCyan
            )

            Spacer(modifier = Modifier.height(6.dp))

            displayTopics.forEach { item ->
                val isSelected = selectedTopic?.topicId == item.topicId
                val itemColor = when {
                    item.totalAnswered == 0 -> Color.Gray
                    item.masteryPercentage >= 75 -> PhysicsGreen
                    item.masteryPercentage >= 50 -> PhysicsCyan
                    else -> PhysicsAmber
                }

                Surface(
                    onClick = { onSelectedTopicIdChange(item.topicId) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.iconEmoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    item.topicName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.totalAnswered > 0) {
                                    Text(
                                        "${PersianUtils.formatInt(item.correctCount)}/${PersianUtils.formatInt(item.totalAnswered)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    "${PersianUtils.formatInt(item.masteryPercentage)}٪",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = itemColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // Styled Horizontal Progress Bar
                        LinearProgressIndicator(
                            progress = { if (item.totalAnswered > 0) item.masteryPercentage / 100f else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = itemColor,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PastScoresView(
    scores: List<QuizScoreEntity>,
    topicProgress: List<TopicProgressEntity> = emptyList(),
    sortByMastery: Boolean = false,
    onSortByMasteryChange: (Boolean) -> Unit = {},
    selectedTopicId: String? = null,
    onSelectedTopicIdChange: (String?) -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    if (scores.isEmpty() && topicProgress.all { it.totalAnswered == 0 }) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
        ) {
            item {
                TopicProgressBarChart(
                    topicProgress = topicProgress,
                    sortByMastery = sortByMastery,
                    onSortByMasteryChange = onSortByMasteryChange,
                    selectedTopicId = selectedTopicId,
                    onSelectedTopicIdChange = onSelectedTopicIdChange
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(54.dp), tint = PhysicsCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("هنوز در هیچ آزمونی شرکت نکرده‌اید.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("از تب اول آزمون دهید تا نمودار و کارنامه شما اینجا ذخیره شود!", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
        ) {
            // 1. Topic Bar Chart
            item {
                TopicProgressBarChart(
                    topicProgress = topicProgress,
                    sortByMastery = sortByMastery,
                    onSortByMasteryChange = onSortByMasteryChange,
                    selectedTopicId = selectedTopicId,
                    onSelectedTopicIdChange = onSelectedTopicIdChange
                )
            }

            // 2. Exam Progress Chart
            if (scores.isNotEmpty()) {
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
}

@Composable
fun SavedProblemsView(
    problems: List<SavedProblemEntity>,
    onDelete: (Int) -> Unit,
    listState: LazyListState = rememberLazyListState()
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
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
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
