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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PhysicsRepository
import com.example.notification.NotificationSettingsDialog
import com.example.ui.formulas.FormulaBookScreen
import com.example.ui.quiz.QuizScreen
import com.example.ui.simulations.SimulationsScreen
import com.example.ui.solver.SolverScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PhysicsAmber
import com.example.ui.theme.PhysicsCyan
import com.example.ui.theme.PhysicsGreen
import com.example.ui.theme.PhysicsPurple
import com.example.util.PersianUtils

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
    var showNotificationDialog by remember { mutableStateOf(false) }

    val userProgress by repository.userProgress.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(Unit) {
        repository.ensureDefaultAchievements()
    }

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
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PhysicsCyan.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "نسخه ۱٫۵",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PhysicsCyan,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
                        // Interactive Score & Trophy Chip
                        val totalPts = userProgress?.totalPoints ?: 0
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PhysicsAmber.copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsAmber.copy(alpha = 0.45f)),
                            onClick = { selectedTab = NavigationTab.Quiz }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏆", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    "${PersianUtils.formatInt(totalPts)} امتیاز",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhysicsAmber
                                )
                            }
                        }

                        IconButton(onClick = { showNotificationDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "یادآور روزانه فیزیک",
                                tint = PhysicsCyan
                            )
                        }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(targetState = selectedTab, label = "tabTransition") { tab ->
                    when (tab) {
                        NavigationTab.Simulations -> SimulationsScreen(
                            repository = repository,
                            snackbarHostState = snackbarHostState
                        )
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
                                    Spacer(modifier = Modifier.weight(1f))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PhysicsCyan.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "نسخه ۱٫۵",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PhysicsCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
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

                        // Changelog section for v1.5
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PhysicsCyan.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎯", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "تازه‌های نسخه جدید ۱٫۵:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PhysicsCyan
                                    )
                                }
                                Text("• افزودن ۴ سطح دشواری به آزمون فیزیک: پایه (آسان)، متوسط، سخت و کنکوری با امکان انتخاب در شروع آزمون", fontSize = 10.sp, lineHeight = 15.sp)
                                Text("• گسترش بانک سوالات به ۱۲۰ سوال تخصصی (۳۰ سوال برای هر سطح) با پراکندگی کامل در پایه‌های دهم، یازدهم و دوازدهم", fontSize = 10.sp, lineHeight = 15.sp)
                                Text("• سوالات چندمرحله‌ای با تبدیل یکاها (g→kg, cm→m, km/h→m/s)، داده‌های اضافه و تحلیل خطاهای متداول کنکور", fontSize = 10.sp, lineHeight = 15.sp)
                                Text("• راه‌حل تشریحی گام‌به‌گام با تمام مراحل و فرمول‌ها برای همه سوالات سطوح سخت و کنکوری", fontSize = 10.sp, lineHeight = 15.sp)
                                Text("• راستی‌آزمایی ریاضی مستقل ۱۰۰٪ سوالات بدون هیچ‌گونه گزینه تکراری یا خطای محاسباتی", fontSize = 10.sp, lineHeight = 15.sp)
                                Text("• حذف سازوکار ضدتوالی و حفظ توزیع متعادل ۲۵٪ با الگوریتم استاندارد Fisher–Yates", fontSize = 10.sp, lineHeight = 15.sp)
                                Text("• سیستم امتیازدهی تصاعدی با ضرایب دشواری (سطح سخت‌تر = امتیاز بیشتر تا ۲ برابر)", fontSize = 10.sp, lineHeight = 15.sp)
                            }
                        }

                        // Changelog history v1.4
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PhysicsGreen.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsGreen.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text("پیشینه تغییرات نسخه ۱٫۴:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = PhysicsGreen)
                                Text("• ساعت دستی دلخواه یادآور روزانه (TimePicker)، پشتیبانی تا ۳ یادآور، پایداری با BOOT_COMPLETED و کلید صوتی آزمون", fontSize = 9.sp, lineHeight = 14.sp)
                            }
                        }

                        Text("✨ قابلیت‌های برنامه «برسام و فیزیک»:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PhysicsCyan)
                        Text("• شبیه‌سازی‌های تعاملی پرتابه، آونگ و پایستگی انرژی، شکست نور، مدارهای الکتریکی و قوانین نیوتن", fontSize = 11.sp)
                        Text("• حل‌المسائل گام‌به‌گام با تحلیل داده‌ها، فرمول‌ها و مراحل محاسباتی کامل", fontSize = 11.sp)
                        Text("• فرمول‌نامه جامع پایه‌های دهم، یازدهم و دوازدهم همراه با یکاهای SI و نکات طلایی کنکور", fontSize = 11.sp)
                        Text("• آزمون‌های تستی مفهومی با کارنامه هوشمند و ذخیره‌سازی داده‌ها", fontSize = 11.sp)
                        Text("• سیستم گیمیفیکیشن، مدال‌های افتخار و رتبه‌بندی دانش‌آموزی برای تقویت انگیزه یادگیری", fontSize = 11.sp)
                        Text("• سیستم یادآوری هوشمند روزانه (Local Notifications) برای مرور فرمول‌ها و چالش‌های حل مسئله", fontSize = 11.sp)
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

        if (showNotificationDialog) {
            NotificationSettingsDialog(onDismissRequest = { showNotificationDialog = false })
        }
    }
}
