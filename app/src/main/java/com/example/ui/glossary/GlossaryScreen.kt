package com.example.ui.glossary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.GeminiPhysicsService
import com.example.data.GlossaryTermEntity
import com.example.data.PhysicsRepository
import com.example.ui.theme.*
import com.example.util.LtrMathBox
import com.example.util.PersianUtils
import kotlinx.coroutines.launch

@Composable
fun GlossaryScreen(
    repository: PhysicsRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allTerms by repository.glossaryTerms.collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedGrade by rememberSaveable { mutableStateOf("همه پایه‌ها") }
    var selectedCategory by rememberSaveable { mutableStateOf("همه موضوعات") }
    var onlyBookmarked by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // State for Ask AI dialog
    var showAskAiDialog by remember { mutableStateOf(false) }
    var customQuestion by remember { mutableStateOf("") }
    var aiAnswer by remember { mutableStateOf<String?>(null) }
    var isAskingAi by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf<String?>(null) }

    val grades = listOf("همه پایه‌ها", "دهم", "یازدهم", "دوازدهم")
    val categories = listOf(
        "همه موضوعات",
        "کار و انرژی",
        "ویژگی‌های ماده",
        "دما و گرما",
        "الکتریسیته ساکن",
        "جریان الکتریکی و مدارها",
        "مغناطیس و القا",
        "سینماتیک و حرکت‌شناسی",
        "دینامیک",
        "نوسان و امواج",
        "برهم‌کنش‌های موج",
        "فیزیک اتمی",
        "فیزیک هسته‌ای"
    )

    // Filter terms in memory
    val filteredTerms = remember(allTerms, searchQuery, selectedGrade, selectedCategory, onlyBookmarked) {
        allTerms.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.termFa.contains(searchQuery.trim(), ignoreCase = true) ||
                    item.termEn.contains(searchQuery.trim(), ignoreCase = true) ||
                    item.definition.contains(searchQuery.trim(), ignoreCase = true) ||
                    item.category.contains(searchQuery.trim(), ignoreCase = true)

            val matchGrade = selectedGrade == "همه پایه‌ها" || item.grade == selectedGrade
            val matchCategory = selectedCategory == "همه موضوعات" || item.category == selectedCategory
            val matchBookmark = !onlyBookmarked || item.isBookmarked

            matchQuery && matchGrade && matchCategory && matchBookmark
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
        ) {
            // Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = PhysicsCyan.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        "📖",
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "واژه‌نامه تخصصی فیزیک",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "پایگاه داده اصطلاحات درسی Room + هوش مصنوعی Gemini 3.5 Flash",
                                        fontSize = 10.sp,
                                        color = PhysicsCyan
                                    )
                                }
                            }

                            // Ask AI button
                            FilledTonalButton(
                                onClick = { showAskAiDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = PhysicsPurple.copy(alpha = 0.2f),
                                    contentColor = PhysicsPurple
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("پرسش از AI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "مرجع سریع تعاریف، فرمول‌ها و یکاهای استاندارد کتب درسی دهم، یازدهم و دوازدهم با قابلیت تحلیل مفهومی عمیق توسط مدل Gemini 3.5 Flash.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "جستجوی واژه یا مفهوم (مثلاً تکانه، برنولی، کولن، لنز...)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "جستجو",
                            tint = PhysicsCyan
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PhysicsCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Grade Filters
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(grades) { grade ->
                        val isSelected = selectedGrade == grade
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PhysicsCyan,
                                selectedLabelColor = DarkBackground
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = onlyBookmarked,
                            onClick = { onlyBookmarked = !onlyBookmarked },
                            label = { Text("⭐ نشان‌شده‌ها", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PhysicsAmber,
                                selectedLabelColor = DarkBackground
                            )
                        )
                    }
                }
            }

            // Category Filters
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        SuggestionChip(
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) PhysicsCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (isSelected) PhysicsCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = if (isSelected) PhysicsCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }

            // Results Count & AI Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${PersianUtils.formatInt(filteredTerms.size)} اصطلاح تخصصی",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PhysicsPurple.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsPurple.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("🤖", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "مجهز به Gemini 3.5 Flash",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PhysicsPurple
                            )
                        }
                    }
                }
            }

            // Terms List
            if (filteredTerms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اصطلاحی با این مشخصات یافت نشد.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "عبارت دیگری را جستجو کنید یا فیلترها را تغییر دهید.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredTerms,
                    key = { it.id }
                ) { term ->
                    GlossaryTermCard(
                        term = term,
                        onToggleBookmark = {
                            coroutineScope.launch {
                                repository.toggleGlossaryBookmark(term.id, !term.isBookmarked)
                            }
                        },
                        onSaveAiInsight = { insight ->
                            coroutineScope.launch {
                                repository.updateGlossaryAiInsight(term.id, insight)
                            }
                        }
                    )
                }
            }
        }

        // Custom Ask AI Dialog
        if (showAskAiDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAskAiDialog = false
                    aiAnswer = null
                    aiError = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PhysicsPurple.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚡", fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("پرسش از هوش مصنوعی برسام", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("مبتنی بر مدل Gemini 3.5 Flash", fontSize = 10.sp, color = PhysicsPurple)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "هر سوال مفهومی یا مسئله‌ای از فیزیک دبیرستان دارید را بنویسید تا برسام با هوش مصنوعی پاسخ دهد:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = customQuestion,
                            onValueChange = { customQuestion = it },
                            placeholder = { Text("مثال: چرا در اتصال متوالی مقاومت‌ها جریان یکسان است؟", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp),
                            maxLines = 4,
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (isAskingAi) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = PhysicsPurple
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("در حال تفکر و پردازش با Gemini 3.5 Flash...", fontSize = 11.sp, color = PhysicsPurple)
                            }
                        }

                        if (aiError != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PhysicsRed.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsRed.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = aiError ?: "",
                                    fontSize = 11.sp,
                                    color = PhysicsRed,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        if (aiAnswer != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsPurple.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                            ) {
                                LazyColumn(modifier = Modifier.padding(10.dp)) {
                                    item {
                                        Text(
                                            text = aiAnswer ?: "",
                                            fontSize = 11.sp,
                                            lineHeight = 17.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customQuestion.isNotBlank()) {
                                isAskingAi = true
                                aiError = null
                                aiAnswer = null
                                coroutineScope.launch {
                                    val result = GeminiPhysicsService.askPhysicsQuestion(customQuestion.trim())
                                    isAskingAi = false
                                    result.onSuccess { text ->
                                        aiAnswer = text
                                    }.onFailure { err ->
                                        aiError = err.message ?: "خطا در ارتباط با هوش مصنوعی"
                                    }
                                }
                            }
                        },
                        enabled = !isAskingAi && customQuestion.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PhysicsPurple)
                    ) {
                        Text("ارسال به هوش مصنوعی", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAskAiDialog = false }) {
                        Text("بستن", fontSize = 11.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun GlossaryTermCard(
    term: GlossaryTermEntity,
    onToggleBookmark: () -> Unit,
    onSaveAiInsight: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(false) }
    var isLoadingAi by remember { mutableStateOf(false) }
    var showCopiedToast by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (term.isBookmarked) PhysicsAmber.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Emoji + Title + English + Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(term.iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = term.termFa,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = term.termEn,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = PhysicsCyan
                        )
                    }
                }

                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (term.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "نشان کردن",
                        tint = if (term.isBookmarked) PhysicsAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Badges: Grade + Category
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PhysicsCyan.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "پایه ${term.grade}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PhysicsCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = term.category,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (!term.unit.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PhysicsGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "یکا: ${term.unit}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PhysicsGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Definition Text
            Text(
                text = term.definition,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Formula Box if present
            if (!term.formula.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                LtrMathBox(
                    formula = term.formula,
                    fontSize = 13,
                    textColor = PhysicsCyan,
                    borderColor = PhysicsCyan.copy(alpha = 0.35f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions: Gemini 3.5 Flash Insight Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        isExpanded = !isExpanded
                        if (isExpanded && term.aiInsight.isNullOrBlank() && !isLoadingAi) {
                            isLoadingAi = true
                            coroutineScope.launch {
                                val result = GeminiPhysicsService.explainTermConceptually(term)
                                isLoadingAi = false
                                result.onSuccess { insight ->
                                    onSaveAiInsight(insight)
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PhysicsPurple
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isExpanded) PhysicsPurple else PhysicsPurple.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = PhysicsPurple
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isExpanded) "بستن تحلیل مفهومی" else "تحلیل مفهومی با Gemini 3.5 Flash ✨",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isExpanded && !term.aiInsight.isNullOrBlank()) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(term.aiInsight))
                            showCopiedToast = true
                        }
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "کپی تحلیل",
                            tint = PhysicsCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Animated AI Insight Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PhysicsPurple.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsPurple.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Psychology,
                                        contentDescription = null,
                                        tint = PhysicsPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "تحلیل عمیق هوش مصنوعی برسام",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PhysicsPurple
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PhysicsPurple.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "Gemini 3.5 Flash",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PhysicsPurple,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isLoadingAi) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = PhysicsPurple
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "در حال تحلیل با مدل Gemini 3.5 Flash...",
                                        fontSize = 11.sp,
                                        color = PhysicsPurple
                                    )
                                }
                            } else {
                                val content = term.aiInsight ?: ""
                                Text(
                                    text = content,
                                    fontSize = 11.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            isLoadingAi = true
                                            coroutineScope.launch {
                                                val result = GeminiPhysicsService.explainTermConceptually(term)
                                                isLoadingAi = false
                                                result.onSuccess { insight ->
                                                    onSaveAiInsight(insight)
                                                }
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = PhysicsPurple
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("به‌روزرسانی تحلیل", fontSize = 10.sp, color = PhysicsPurple)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
