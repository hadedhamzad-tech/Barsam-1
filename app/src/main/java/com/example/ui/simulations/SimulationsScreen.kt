package com.example.ui.simulations

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.PhysicsRepository
import com.example.data.UserProgressEntity
import com.example.model.SimulationType
import com.example.ui.theme.*
import com.example.util.PersianUtils
import kotlinx.coroutines.launch

@Composable
fun SimulationsScreen(
    repository: PhysicsRepository,
    snackbarHostState: SnackbarHostState
) {
    var selectedSim by remember { mutableStateOf(SimulationType.PROJECTILE) }
    val userProgress by repository.userProgress.collectAsStateWithLifecycle(initialValue = null)
    val achievements by repository.achievements.collectAsStateWithLifecycle(initialValue = emptyList())
    val coroutineScope = rememberCoroutineScope()

    val currentSimAchievementId = when (selectedSim) {
        SimulationType.PROJECTILE -> "sim_projectile"
        SimulationType.PENDULUM -> "sim_pendulum"
        SimulationType.OPTICS -> "sim_optics"
        SimulationType.CIRCUIT -> "sim_circuit"
        SimulationType.NEWTON -> "sim_newton"
    }
    val isCurrentSimCompleted = achievements.find { it.id == currentSimAchievementId }?.isUnlocked == true

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 0.dp, bottom = 80.dp)
    ) {
        // Hero Banner Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Generated Hero Image
                Image(
                    painter = painterResource(id = R.drawable.hero_physics),
                    contentDescription = "آزمایشگاه مجازی فیزیک",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay - deep contrast scrim ensuring text is 100% crisp in both light & dark themes
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color(0xFF030712).copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                // Header Text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = Color(0xFF0284C7),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE0F2FE)),
                            shadowElevation = 3.dp
                        ) {
                            Text(
                                text = "آزمایشگاه تعاملی و زنده",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = Color(0xFFD97706),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFEF3C7)),
                            shadowElevation = 3.dp
                        ) {
                            Text(
                                text = "اثری از برسام",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "شبیه‌سازی‌های مفهومی برسام و فیزیک",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Student Progress & Points Ribbon
        item {
            val progress = userProgress ?: UserProgressEntity()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                progress.levelTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PhysicsAmber
                            )
                            Text(
                                "مجموع: ${PersianUtils.formatInt(progress.totalPoints)} امتیاز فیزیک",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PhysicsCyan.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "آزمایش‌ها: ${PersianUtils.formatInt(progress.simulationsCompleted)} از ۵",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PhysicsCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Horizontal Simulation Selector Tabs
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SimulationType.values()) { simType ->
                    val isSelected = selectedSim == simType
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSim = simType },
                        label = {
                            Text(
                                simType.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            val icon = when (simType) {
                                SimulationType.PROJECTILE -> Icons.Default.RocketLaunch
                                SimulationType.PENDULUM -> Icons.Default.Timeline
                                SimulationType.OPTICS -> Icons.Default.LightMode
                                SimulationType.CIRCUIT -> Icons.Default.Bolt
                                SimulationType.NEWTON -> Icons.Default.Speed
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSelected) PhysicsCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhysicsCyan.copy(alpha = 0.2f),
                            selectedLabelColor = PhysicsCyan
                        )
                    )
                }
            }
        }

        // Active Simulation Description Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedSim.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = selectedSim.gradeTag,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = selectedSim.subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Selected Simulation Composable
        item {
            when (selectedSim) {
                SimulationType.PROJECTILE -> ProjectileSim()
                SimulationType.PENDULUM -> PendulumSim()
                SimulationType.OPTICS -> OpticsSim()
                SimulationType.CIRCUIT -> CircuitSim()
                SimulationType.NEWTON -> NewtonSim()
            }
        }

        // Medal & Points Registration Card for this Simulation
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentSimCompleted) PhysicsGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isCurrentSimCompleted) PhysicsGreen.copy(alpha = 0.6f) else PhysicsAmber.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isCurrentSimCompleted) "🏅 مدال این آزمایش کسب شد!" else "🌟 پاداش یادگیری و انجام آزمایش",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isCurrentSimCompleted) PhysicsGreen else PhysicsAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            if (isCurrentSimCompleted) "تبریک! مدال افتخار این بخش و ۳۰ امتیاز در کارنامه شما ثبت شد."
                            else "پس از بررسی شبیه‌سازی، دکمه روبرو را بزنید تا مدال و ۳۰ امتیاز دریافت نمایید.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val (newlyUnlocked, title) = repository.recordSimulationCompleted(currentSimAchievementId)
                                if (newlyUnlocked) {
                                    snackbarHostState.showSnackbar("🎉 تبریک! مدال «${title ?: "افتخار"}» آزاد شد و ۳۰ امتیاز کسب کردید!")
                                } else {
                                    snackbarHostState.showSnackbar("این مدال قبلاً کسب شده است (+۳۰ امتیاز در کارنامه شما).")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrentSimCompleted) PhysicsGreen else PhysicsAmber
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrentSimCompleted) Icons.Default.Check else Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = DarkBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCurrentSimCompleted) "کسب شد ✓" else "ثبت مدال 🏅",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
