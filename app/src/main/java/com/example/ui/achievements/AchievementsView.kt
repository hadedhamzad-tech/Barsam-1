package com.example.ui.achievements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AchievementEntity
import com.example.data.UserProgressEntity
import com.example.ui.theme.*
import com.example.util.PersianUtils

@Composable
fun AchievementsView(
    achievements: List<AchievementEntity>,
    userProgress: UserProgressEntity?,
    listState: LazyListState = rememberLazyListState()
) {
    val progress = userProgress ?: UserProgressEntity()
    var selectedCategory by remember { mutableStateOf("همه") }
    val categories = listOf("همه", "آزمایشگاه", "آزمون", "حل مسئله", "امتیاز")

    val filteredAchievements = remember(achievements, selectedCategory) {
        if (selectedCategory == "همه") achievements
        else achievements.filter { it.category == selectedCategory }
    }

    val unlockedCount = remember(achievements) { achievements.count { it.isUnlocked } }
    val totalPoints = progress.totalPoints

    // Level calculation
    val (nextLevelPoints, nextLevelName) = remember(totalPoints) {
        when {
            totalPoints < 100 -> Pair(100, "کاوشگر آزمایشگاه")
            totalPoints < 250 -> Pair(250, "حلّال برتر مسائل")
            totalPoints < 500 -> Pair(500, "نظریه‌پرداز جوان")
            totalPoints < 800 -> Pair(800, "استاد فیزیک و افتخار برسام")
            else -> Pair(1000, "بالاترین سطح علمی")
        }
    }

    val prevLevelPoints = when {
        totalPoints < 100 -> 0
        totalPoints < 250 -> 100
        totalPoints < 500 -> 250
        totalPoints < 800 -> 500
        else -> 800
    }

    val progressFraction = if (nextLevelPoints > prevLevelPoints) {
        ((totalPoints - prevLevelPoints).toFloat() / (nextLevelPoints - prevLevelPoints).toFloat()).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "levelProgress")

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
    ) {
        // Hero Student Level & Trophy Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PhysicsAmber.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar / Trophy Badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PhysicsAmber, Color(0xFFF59E0B), Color(0xFFD97706))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = progress.levelTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PhysicsAmber,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "مجموع امتیازات: ${PersianUtils.formatInt(totalPoints)} امتیاز فیزیک",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar to next level
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "سطح بعد: $nextLevelName",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${PersianUtils.formatInt(totalPoints)} از ${PersianUtils.formatInt(nextLevelPoints)} امتیاز",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PhysicsCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PhysicsAmber,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Key Stats Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = PhysicsCyan.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🧪 آزمایش‌ها", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    PersianUtils.formatInt(progress.simulationsCompleted),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhysicsCyan
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = PhysicsGreen.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsGreen.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎯 آزمون‌ها", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    PersianUtils.formatInt(progress.quizzesCompleted),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhysicsGreen
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = PhysicsPurple.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsPurple.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🏅 مدال‌ها", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "${PersianUtils.formatInt(unlockedCount)} از ${PersianUtils.formatInt(achievements.size)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhysicsPurple
                                )
                            }
                        }
                    }
                }
            }
        }

        // Motivational Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PhysicsCyan.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "با تکمیل شبیه‌سازی‌ها، پاسخ به سوالات آزمون و حل مسائل، مدال‌های افتخار کسب کنید و به عنوان «استاد فیزیک» دست یابید!",
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhysicsAmber.copy(alpha = 0.2f),
                            selectedLabelColor = PhysicsAmber
                        )
                    )
                }
            }
        }

        // Medals Header
        item {
            Text(
                text = "مدال‌های افتخار (${PersianUtils.formatInt(filteredAchievements.size)} مدال)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Medal Cards
        items(filteredAchievements) { achievement ->
            MedalCard(achievement = achievement)
        }
    }
}

@Composable
fun MedalCard(achievement: AchievementEntity) {
    val isUnlocked = achievement.isUnlocked
    val borderColor = if (isUnlocked) PhysicsAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val cardBackground = if (isUnlocked) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = androidx.compose.foundation.BorderStroke(if (isUnlocked) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medal Icon / Emoji
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) PhysicsAmber.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) PhysicsAmber.copy(alpha = 0.6f) else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(achievement.iconEmoji, fontSize = 26.sp)
                } else {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = "قفل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = achievement.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isUnlocked) PhysicsGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isUnlocked) "کسب شد ✓" else "قفل",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) PhysicsGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = achievement.description,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isUnlocked && achievement.unlockedAt > 0L) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "زمان کسب: ${PersianUtils.formatJalaliDateTime(achievement.unlockedAt)}",
                        fontSize = 10.sp,
                        color = PhysicsGreen.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Points Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isUnlocked) PhysicsAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUnlocked) PhysicsAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+${PersianUtils.formatInt(achievement.points)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) PhysicsAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "امتیاز",
                        fontSize = 9.sp,
                        color = if (isUnlocked) PhysicsAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
