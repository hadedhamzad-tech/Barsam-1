package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhysicsRepository
import com.example.ui.formulas.FormulaBookScreen
import com.example.ui.quiz.QuizScreen
import com.example.ui.simulations.SimulationsScreen
import com.example.ui.solver.SolverScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PhysicsAmber
import com.example.ui.theme.PhysicsCyan
import com.example.ui.theme.PhysicsGreen
import com.example.ui.theme.PhysicsPurple

sealed class NavigationTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Simulations : NavigationTab(
        "simulations",
        "شبیه‌سازی‌ها",
        Icons.Filled.Science,
        Icons.Outlined.Science
    )
    object Solver : NavigationTab(
        "solver",
        "حل مسائل",
        Icons.Filled.Calculate,
        Icons.Outlined.Calculate
    )
    object Formulas : NavigationTab(
        "formulas",
        "فرمول‌نامه",
        Icons.Filled.MenuBook,
        Icons.Outlined.MenuBook
    )
    object Quiz : NavigationTab(
        "quiz",
        "آزمون و نتایج",
        Icons.Filled.Quiz,
        Icons.Outlined.Quiz
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: PhysicsRepository,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var selectedTab by remember { mutableStateOf<NavigationTab>(NavigationTab.Simulations) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCompletionBanner by remember { mutableStateOf(true) }

    // Enforce RTL layout for Persian language
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PhysicsCyan.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.5f))
                            ) {
                                Box(modifier = Modifier.padding(5.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.AllInclusive,
                                        contentDescription = null,
                                        tint = PhysicsCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "برسام و فیزیک",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PhysicsAmber.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "سازنده: برسام",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PhysicsAmber,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    "آزمایشگاه و حل‌المسائل تعاملی فیزیک دبیرستان",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "تغییر پوسته",
                                tint = PhysicsCyan
                            )
                        }
                        IconButton(onClick = { showAboutDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "تقدیر از برسام و درباره برنامه",
                                tint = PhysicsAmber
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val tabs = listOf(
                        NavigationTab.Simulations,
                        NavigationTab.Solver,
                        NavigationTab.Formulas,
                        NavigationTab.Quiz
                    )

                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                indicatorColor = PhysicsCyan,
                                selectedTextColor = PhysicsCyan
                            )
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Celebratory Completion Status Banner
                if (showCompletionBanner) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = PhysicsGreen.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, PhysicsGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PhysicsGreen,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "علامت پایان ساخت برنامه",
                                        tint = DarkBackground,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "✓ فرآیند ساخت برنامه با موفقیت به پایان رسید!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhysicsGreen
                                )
                                Text(
                                    "«برسام و فیزیک» کامپایل شده و آماده بهره‌برداری کامل است.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { showCompletionBanner = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن",
                                    tint = PhysicsGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Crossfade(targetState = selectedTab, label = "tabTransition") { tab ->
                        when (tab) {
                            NavigationTab.Simulations -> SimulationsScreen()
                            NavigationTab.Solver -> SolverScreen(
                                repository = repository,
                                snackbarHostState = snackbarHostState
                            )
                            NavigationTab.Formulas -> FormulaBookScreen()
                            NavigationTab.Quiz -> QuizScreen(repository = repository)
                        }
                    }
                }
            }
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = PhysicsAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تقدیر و شناسنامه اثر", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Special Honor Card for Barsam
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PhysicsAmber.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsAmber.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🌟", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "تقدیر ویژه از برسام",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PhysicsAmber
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "این اثر ارزشمند علمی و آموزشی به نام «برسام و فیزیک» توسط «برسام» با هدف تسهیل و لذت‌بخش کردن یادگیری فیزیک برای دانش‌آموزان دبیرستان خلق و توسعه یافته است. با سپاس فراوان از همت و نوآوری برسام عزیز.",
                                    fontSize = 11.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Text("✨ قابلیت‌های برنامه «برسام و فیزیک»:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PhysicsCyan)
                        Text("• شبیه‌سازی‌های تعاملی پرتابه، آونگ و پایستگی انرژی، شکست نور، مدارهای الکتریکی و قوانین نیوتن", fontSize = 11.sp)
                        Text("• حل‌المسائل گام‌به‌گام با تحلیل داده‌ها، فرمول‌ها و مراحل محاسباتی کامل", fontSize = 11.sp)
                        Text("• فرمول‌نامه جامع پایه‌های دهم، یازدهم و دوازدهم همراه با یکاهای SI و نکات طلایی کنکور", fontSize = 11.sp)
                        Text("• آزمون‌های تستی مفهومی با کارنامه هوشمند و ذخیره‌سازی داده‌ها", fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PhysicsCyan)
                    ) {
                        Text("بسیار عالی", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
