package com.example.ui.simulations

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.PersianUtils
import kotlinx.coroutines.delay
import kotlin.math.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun PendulumSim() {
    var lengthMeters by remember { mutableFloatStateOf(1.2f) } // 0.5m to 2.5m
    var massKg by remember { mutableFloatStateOf(1.0f) } // 0.2kg to 3.0kg
    var initialAngleDeg by remember { mutableFloatStateOf(35f) } // degrees
    var withDamping by remember { mutableStateOf(false) }

    val g = 9.8f
    val period = (2.0 * Math.PI * sqrt(lengthMeters / g)).toFloat()
    val frequency = 1f / period

    var isRunning by remember { mutableStateOf(true) }
    var timeElapsed by remember { mutableFloatStateOf(0f) }

    val omega = sqrt(g / lengthMeters)
    val theta0Rad = Math.toRadians(initialAngleDeg.toDouble()).toFloat()

    // Realtime animation loop
    LaunchedEffect(isRunning, lengthMeters, massKg, initialAngleDeg, withDamping) {
        val dt = 0.03f
        while (true) {
            if (isRunning) {
                timeElapsed += dt
            }
            delay(30)
        }
    }

    // Current angle with optional damping
    val dampingFactor = if (withDamping) exp(-0.06f * timeElapsed) else 1f
    val currentAngleRad = theta0Rad * cos(omega * timeElapsed) * dampingFactor
    val currentAngleDeg = Math.toDegrees(currentAngleRad.toDouble()).toFloat()

    // Energy calculations
    val totalEnergy = (massKg * g * lengthMeters * (1.0 - cos(theta0Rad.toDouble()))).toFloat()
    val potentialEnergy = (massKg * g * lengthMeters * (1.0 - cos(currentAngleRad.toDouble()))).toFloat()
    val kineticEnergy = max(0f, totalEnergy * (dampingFactor * dampingFactor) - potentialEnergy)
    val currentVelocity = sqrt(max(0f, 2f * kineticEnergy / massKg)) * (if (sin(omega * timeElapsed) > 0) -1f else 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Canvas of swinging pendulum
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF0B132B), RoundedCornerShape(16.dp))
                .border(1.dp, PhysicsPurple.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                val pivotX = canvasW / 2f
                val pivotY = 30.dp.toPx()

                // Ceiling mount
                drawRect(
                    color = Color(0xFF334155),
                    topLeft = Offset(pivotX - 50.dp.toPx(), 10.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(100.dp.toPx(), 20.dp.toPx())
                )
                drawCircle(
                    color = PhysicsCyan,
                    radius = 5.dp.toPx(),
                    center = Offset(pivotX, pivotY)
                )

                // Scaling string length to canvas
                val visualLength = (canvasH - 70.dp.toPx()) * (lengthMeters / 2.5f).coerceIn(0.4f, 1.0f)
                val bobX = pivotX + visualLength * sin(currentAngleRad)
                val bobY = pivotY + visualLength * cos(currentAngleRad)

                // Arc of motion (dotted reference path)
                val arcRadius = visualLength
                val pathColor = Color(0xFF334155).copy(alpha = 0.5f)
                drawLine(
                    color = pathColor,
                    start = Offset(pivotX, pivotY),
                    end = Offset(pivotX, pivotY + visualLength),
                    strokeWidth = 1.5f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )

                // Pendulum String
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(pivotX, pivotY),
                    end = Offset(bobX, bobY),
                    strokeWidth = 2.5f
                )

                // Bob Radius based on mass
                val bobRadius = (12f + massKg * 5f).dp.toPx()

                // Bob Radial Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PhysicsPurple, Color.Transparent),
                        center = Offset(bobX, bobY),
                        radius = bobRadius * 1.8f
                    ),
                    radius = bobRadius * 1.8f,
                    center = Offset(bobX, bobY)
                )

                // Bob Solid Sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PhysicsCyan, PhysicsPurpleDark),
                        center = Offset(bobX - bobRadius * 0.3f, bobY - bobRadius * 0.3f),
                        radius = bobRadius
                    ),
                    radius = bobRadius,
                    center = Offset(bobX, bobY)
                )

                // Tangential Velocity Vector
                val velVectorScale = 15f
                val vecVx = currentVelocity * cos(currentAngleRad) * velVectorScale
                val vecVy = -currentVelocity * sin(currentAngleRad) * velVectorScale
                if (abs(currentVelocity) > 0.05f) {
                    drawLine(
                        color = PhysicsGreen,
                        start = Offset(bobX, bobY),
                        end = Offset(bobX + vecVx, bobY + vecVy),
                        strokeWidth = 3f
                    )
                    drawCircle(PhysicsGreen, 3.dp.toPx(), Offset(bobX + vecVx, bobY + vecVy))
                }
            }

            // Real-time metrics chip
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.85f)
            ) {
                Text(
                    text = "زاویه: ${PersianUtils.formatNumber(currentAngleDeg, 1)}°",
                    color = PhysicsCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Energy Conservation Bars
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "نمودار پایستگی و تبدیل انرژی (ژول):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Kinetic Energy Bar
                val maxBarE = max(totalEnergy, 0.001f)
                val kineticFraction = (kineticEnergy / maxBarE).coerceIn(0f, 1f)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("انرژی جنبشی (K):", fontSize = 12.sp)
                        Text("${PersianUtils.formatNumber(kineticEnergy, 2)} ژول", color = PhysicsGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    LinearProgressIndicator(
                        progress = { kineticFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = PhysicsGreen,
                        trackColor = Color(0xFF1E293B)
                    )
                }

                // Potential Energy Bar
                val potentialFraction = (potentialEnergy / maxBarE).coerceIn(0f, 1f)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("انرژی پتانسیل گرانشی (U):", fontSize = 12.sp)
                        Text("${PersianUtils.formatNumber(potentialEnergy, 2)} ژول", color = PhysicsAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    LinearProgressIndicator(
                        progress = { potentialFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = PhysicsAmber,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Action controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) PhysicsAmber else PhysicsPurple
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isRunning) "مکث نوسان" else "ادامه نوسان", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    timeElapsed = 0f
                },
                modifier = Modifier.width(100.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("از ابتدا", fontSize = 12.sp)
            }
        }

        // Metrics: Period, Frequency, Velocity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "دوره تناوب (T)",
                value = "${PersianUtils.formatNumber(period, 2)} ثانیه",
                formula = "T = 2π√(L/g)",
                color = PhysicsCyan,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "بسامد (f)",
                value = "${PersianUtils.formatNumber(frequency, 2)} هرتز",
                formula = "f = 1 / T",
                color = PhysicsPurple,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "تندی لحظه‌ای",
                value = "${PersianUtils.formatNumber(abs(currentVelocity), 2)} m/s",
                formula = "v = √(2g·Δh)",
                color = PhysicsGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Interactive Sliders
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Length Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("طول نخ (L):", fontSize = 13.sp)
                    Text("${PersianUtils.formatNumber(lengthMeters, 2)} متر", color = PhysicsCyan, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = lengthMeters,
                    onValueChange = { lengthMeters = it },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = PhysicsCyan, activeTrackColor = PhysicsCyan)
                )

                // Mass Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("جرم گلوله (m):", fontSize = 13.sp)
                    Text("${PersianUtils.formatNumber(massKg, 1)} کیلوگرم", color = PhysicsPurple, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = massKg,
                    onValueChange = { massKg = it },
                    valueRange = 0.2f..3.0f,
                    colors = SliderDefaults.colors(thumbColor = PhysicsPurple, activeTrackColor = PhysicsPurple)
                )

                // Damping Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("شبیه‌سازی اصطکاک هوا (میرا)", fontSize = 13.sp)
                    Switch(
                        checked = withDamping,
                        onCheckedChange = { withDamping = it }
                    )
                }
            }
        }
    }
}
