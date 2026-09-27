package com.example.ui.simulations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

data class CelestialBody(val name: String, val g: Float, val color: Color)

@Composable
fun ProjectileSim() {
    var velocity by remember { mutableFloatStateOf(30f) } // m/s
    var angleDeg by remember { mutableFloatStateOf(45f) } // degrees
    val celestialBodies = remember {
        listOf(
            CelestialBody("زمین", 9.8f, Color(0xFF4FC3F7)),
            CelestialBody("ماه", 1.62f, Color(0xFFE0E0E0)),
            CelestialBody("مریخ", 3.71f, Color(0xFFFF8A65)),
            CelestialBody("مشتری", 24.79f, Color(0xFFFFD54F))
        )
    }
    var selectedBody by remember { mutableStateOf(celestialBodies[0]) }

    val angleRad = Math.toRadians(angleDeg.toDouble())
    val vx = velocity * cos(angleRad).toFloat()
    val vy = velocity * sin(angleRad).toFloat()
    val g = selectedBody.g

    // Theoretical maximums
    val flightTime = (2f * vy) / g
    val maxHeight = (vy * vy) / (2f * g)
    val maxRange = (velocity * velocity * sin(2 * angleRad)).toFloat() / g

    var isRunning by remember { mutableStateOf(false) }
    var simTime by remember { mutableFloatStateOf(0f) }

    // Animation ticker
    LaunchedEffect(isRunning, velocity, angleDeg, selectedBody) {
        if (isRunning) {
            simTime = 0f
            val dt = 0.03f
            while (isRunning && simTime < flightTime) {
                delay(30)
                simTime += dt
            }
            if (simTime >= flightTime) {
                simTime = flightTime
                isRunning = false
            }
        }
    }

    // Current positions at simTime
    val currentX = vx * simTime
    val currentY = max(0f, vy * simTime - 0.5f * g * simTime * simTime)
    val currentVx = vx
    val currentVy = vy - g * simTime

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Celestial Presets Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            celestialBodies.forEach { body ->
                FilterChip(
                    selected = selectedBody == body,
                    onClick = {
                        selectedBody = body
                        simTime = 0f
                        isRunning = false
                    },
                    label = {
                        Text(
                            "${body.name} (${body.g})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedBody == body) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = body.color.copy(alpha = 0.3f),
                        selectedLabelColor = body.color
                    )
                )
            }
        }

        // Live Simulation Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                .border(1.dp, PhysicsCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                val groundY = canvasH - 30.dp.toPx()
                val originX = 40.dp.toPx()

                // Coordinate grid background lines
                val gridColor = Color(0xFF1E293B)
                for (x in 0..canvasW.toInt() step 60) {
                    drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), canvasH), 1f)
                }
                for (y in 0..canvasH.toInt() step 40) {
                    drawLine(gridColor, Offset(0f, y.toFloat()), Offset(canvasW, y.toFloat()), 1f)
                }

                // Draw ground
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(0f, groundY),
                    end = Offset(canvasW, groundY),
                    strokeWidth = 3f
                )

                // Scale factor so trajectory fits canvas nicely
                val scaleX = if (maxRange > 0) (canvasW - originX - 40.dp.toPx()) / max(maxRange, 10f) else 5f
                val scaleY = if (maxHeight > 0) (groundY - 40.dp.toPx()) / max(maxHeight, 10f) else 5f
                val scale = min(scaleX, scaleY).coerceIn(1.2f, 15f)

                // Full parabolic trajectory path
                val path = Path()
                path.moveTo(originX, groundY)
                val steps = 60
                for (i in 0..steps) {
                    val t = (flightTime * i) / steps
                    val px = originX + (vx * t) * scale
                    val py = groundY - (vy * t - 0.5f * g * t * t) * scale
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                drawPath(
                    path = path,
                    color = PhysicsCyan.copy(alpha = 0.45f),
                    style = Stroke(width = 2.5f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                )

                // Trajectory traversed so far
                if (simTime > 0) {
                    val traversedPath = Path()
                    traversedPath.moveTo(originX, groundY)
                    val currentSteps = (steps * (simTime / max(flightTime, 0.01f))).toInt()
                    for (i in 0..currentSteps) {
                        val t = (flightTime * i) / steps
                        val px = originX + (vx * t) * scale
                        val py = groundY - (vy * t - 0.5f * g * t * t) * scale
                        if (i == 0) traversedPath.moveTo(px, py) else traversedPath.lineTo(px, py)
                    }
                    drawPath(
                        path = traversedPath,
                        color = PhysicsCyan,
                        style = Stroke(width = 3.5f)
                    )
                }

                // Cannon/Launcher tube
                val launcherLength = 30.dp.toPx()
                val muzzleX = originX + launcherLength * cos(angleRad).toFloat()
                val muzzleY = groundY - launcherLength * sin(angleRad).toFloat()
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(originX, groundY),
                    end = Offset(muzzleX, muzzleY),
                    strokeWidth = 6f
                )

                // Projectile position
                val projPx = originX + currentX * scale
                val projPy = groundY - currentY * scale

                // Projectile Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PhysicsCyan, Color.Transparent),
                        center = Offset(projPx, projPy),
                        radius = 16.dp.toPx()
                    ),
                    radius = 16.dp.toPx(),
                    center = Offset(projPx, projPy)
                )
                // Projectile Ball
                drawCircle(
                    color = PhysicsCyan,
                    radius = 6.dp.toPx(),
                    center = Offset(projPx, projPy)
                )

                // Velocity vector arrow from projectile
                if (simTime < flightTime) {
                    val vecScale = 0.8f
                    val arrowEndX = projPx + currentVx * vecScale
                    val arrowEndY = projPy - currentVy * vecScale
                    drawLine(
                        color = PhysicsGreen,
                        start = Offset(projPx, projPy),
                        end = Offset(arrowEndX, arrowEndY),
                        strokeWidth = 2.5f
                    )
                    drawCircle(PhysicsGreen, 3.dp.toPx(), Offset(arrowEndX, arrowEndY))
                }
            }

            // Realtime time badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.85f)
            ) {
                Text(
                    text = "زمان: ${"%.2f".format(simTime)} ثانیه",
                    color = PhysicsCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Action Buttons: Play/Pause/Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (simTime >= flightTime) simTime = 0f
                    isRunning = !isRunning
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) PhysicsAmber else PhysicsCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "مکث" else "پرتاب"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (isRunning) "توقف شبیه‌سازی" else "شلیک پرتابه",
                    color = DarkBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {
                    isRunning = false
                    simTime = 0f
                },
                modifier = Modifier.width(100.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "تنظیم مجدد")
                Spacer(modifier = Modifier.width(4.dp))
                Text("شروع مجدد", fontSize = 12.sp)
            }
        }

        // Metrics Dashboard Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "برد نهایی (R)",
                value = "${"%.1f".format(maxRange)} متر",
                formula = "v₀²·sin(2θ)/g",
                color = PhysicsCyan,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "ارتفاع اوج (H)",
                value = "${"%.1f".format(maxHeight)} متر",
                formula = "v₀y² / 2g",
                color = PhysicsAmber,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "زمان پرواز (t)",
                value = "${"%.2f".format(flightTime)} ثانیه",
                formula = "2v₀y / g",
                color = PhysicsGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Controls: Angle & Velocity Sliders
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Angle Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("زاویه پرتاب (θ):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("${angleDeg.toInt()}° درجه", color = PhysicsCyan, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = angleDeg,
                    onValueChange = {
                        angleDeg = it
                        simTime = 0f
                        isRunning = false
                    },
                    valueRange = 5f..85f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = PhysicsCyan,
                        activeTrackColor = PhysicsCyan
                    )
                )

                // Velocity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("تندی اولیه (v₀):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("${velocity.toInt()} m/s", color = PhysicsAmber, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = velocity,
                    onValueChange = {
                        velocity = it
                        simTime = 0f
                        isRunning = false
                    },
                    valueRange = 10f..60f,
                    colors = SliderDefaults.colors(
                        thumbColor = PhysicsAmber,
                        activeTrackColor = PhysicsAmber
                    )
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    formula: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
            Text(formula, fontSize = 9.sp, color = color.copy(alpha = 0.75f))
        }
    }
}
