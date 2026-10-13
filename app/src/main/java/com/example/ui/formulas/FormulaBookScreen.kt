package com.example.ui.formulas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhysicsData
import com.example.model.FormulaItem
import com.example.notification.NotificationSettingsDialog
import com.example.ui.theme.*
import com.example.util.LtrMathBox
import com.example.util.PersianUtils

@Composable
fun FormulaBookScreen() {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val grades = listOf("همه پایه‌ها", "پایه دهم", "پایه یازدهم", "پایه دوازدهم")
    var selectedGrade by rememberSaveable { mutableStateOf("همه پایه‌ها") }
    var expandedFormulaId by rememberSaveable { mutableStateOf<String?>(null) }
    var showNotificationSettings by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val filteredFormulas = remember(searchQuery, selectedGrade) {
        PhysicsData.formulas.filter { item ->
            val matchGrade = selectedGrade == "همه پایه‌ها" || item.grade == selectedGrade
            val matchQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.formula.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchGrade && matchQuery
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = 80.dp)
        ) {
            // Header
            item {
                Text(
                    "بانک فرمول‌ها و مفاهیم فیزیک",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PhysicsCyan
                )
                Text(
                    "مجموعه جامع روابط، یکاهای SI و نکات کنکوری پایه‌های دهم، یازدهم و دوازدهم",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Daily Reminder Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PhysicsCyan.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔔", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "یادآور روزانه فرمول‌ها و مسائل",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PhysicsCyan
                                )
                                Text(
                                    "ارسال خودکار یک فرمول یا چالش کوتاه در روز",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { showNotificationSettings = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PhysicsCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("تنظیم یادآور", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("جستجو در عناوین، فرمول‌ها یا نمادها...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PhysicsCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Grade Filter Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grades) { grade ->
                    val isSelected = selectedGrade == grade
                    FilterChip(
                        modifier = Modifier.wrapContentWidth(),
                        selected = isSelected,
                        onClick = { selectedGrade = grade },
                        label = {
                            Text(
                                grade,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhysicsCyan.copy(alpha = 0.25f),
                            selectedLabelColor = PhysicsCyan
                        )
                    )
                }
            }
        }

        // Formula Count & Results
        item {
            Text(
                "تعداد موارد یافت شده: ${PersianUtils.formatInt(filteredFormulas.size)} فرمول",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Formula Cards
        items(filteredFormulas) { item ->
            val isExpanded = expandedFormulaId == item.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedFormulaId = if (isExpanded) null else item.id
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isExpanded) PhysicsCyan.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Title and Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PhysicsCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        item.grade,
                                        fontSize = 10.sp,
                                        color = PhysicsCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PhysicsPurple.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        item.category,
                                        fontSize = 10.sp,
                                        color = PhysicsPurple,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = PhysicsCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Formula Box (Strictly LTR isolated container)
                    LtrMathBox(formula = item.formula, fontSize = 15)

                    // Expanded Details
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Variable Breakdown
                            Text(
                                "نمادها و یکاهای استاندارد SI:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item.variables.forEach { variable ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF0F172A),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.3f))
                                            ) {
                                                Text(
                                                    variable.symbol,
                                                    color = PhysicsCyan,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                variable.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            variable.unit,
                                            fontSize = 11.sp,
                                            color = PhysicsGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Key Exam Tip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PhysicsAmber.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsAmber.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("💡", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.keyTip,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNotificationSettings) {
        NotificationSettingsDialog(onDismissRequest = { showNotificationSettings = false })
    }
}
}
