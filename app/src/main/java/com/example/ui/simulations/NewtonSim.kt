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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun NewtonSim() {
    var appliedForce by remember { mutableFloatStateOf(50f) } // 0 to 120 N
    var mass by remember { mutableFloatStateOf(5f) } // 1 to 20 kg
    var muS by remember { mutableFloatStateOf(0.4f) } // 0.1 to 0.8
    var muK by remember { mutableFloatStateOf(0.25f) } // 0.05 to 0.6

    val g = 9.8f
    val normalForce = mass * g
    val maxStaticFriction = muS * normalForce
    val kineticFriction = muK * normalForce

    val isMoving = appliedForce > maxStaticFriction
    val netForce = if (isMoving) appliedForce - kineticFriction else 0f
    val acceleration = if (isMoving) netForce / mass else 0f

    var isRunning by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableFloatStateOf(0f) }
    var displacementX by remember { mutableFloatStateOf(0f) }

    // Motion loop
    LaunchedEffect(isRunning, acceleration, isMoving) {
        val dt = 0.03f
        while (isRunning) {
            delay(30)
            if (isMoving) {
                currentSpeed += acceleration * dt
                displacementX += currentSpeed * dt
            } else {
                currentSpeed = 0f
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Canvas of block and free-body forces
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                .border(1.dp, PhysicsGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                val groundY = canvasH - 45.dp.toPx()

                // Draw ground surface
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(0f, groundY),
                    end = Offset(canvasW, groundY),
                    strokeWidth = 3f
                )
                // Surface hatch lines
                for (x in 0..canvasW.toInt() step 20) {
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(x.toFloat(), groundY),
                        end = Offset(x.toFloat() - 8.dp.toPx(), groundY + 12.dp.toPx()),
                        strokeWidth = 1.5f
                    )
                }

                // Block sizing & position
                val blockW = 60.dp.toPx()
                val blockH = 50.dp.toPx()
                val visualX = (50.dp.toPx() + (displacementX * 12f) % (canvasW - blockW - 60.dp.toPx()))
                val blockTop = groundY - blockH

                // Draw Block
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(visualX, blockTop),
                    size = Size(blockW, blockH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawRoundRect(
                    color = PhysicsCyan,
                    topLeft = Offset(visualX, blockTop),
                    size = Size(blockW, blockH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(2f)
                )

                val blockCenter = Offset(visualX + blockW / 2f, blockTop + blockH / 2f)

                // Free-Body Diagram Vectors
                val scale = 0.8f

                // 1. Applied Force (Right)
                if (appliedForce > 0) {
                    val fLen = (appliedForce * scale).coerceIn(15f, 90.dp.toPx())
                    drawLine(PhysicsGreen, blockCenter, Offset(blockCenter.x + fLen, blockCenter.y), 3.5f)
                    drawCircle(PhysicsGreen, 3.dp.toPx(), Offset(blockCenter.x + fLen, blockCenter.y))
                }

                // 2. Friction Force (Left)
                val fFric = if (isMoving) kineticFriction else appliedForce.coerceAtMost(maxStaticFriction)
                if (fFric > 0) {
                    val fLen = (fFric * scale).coerceIn(10f, 75.dp.toPx())
                    drawLine(PhysicsRed, blockCenter, Offset(blockCenter.x - fLen, blockCenter.y), 3.5f)
                    drawCircle(PhysicsRed, 3.dp.toPx(), Offset(blockCenter.x - fLen, blockCenter.y))
                }

                // 3. Normal Force F_N (Up)
                val fnLen = 35.dp.toPx()
                drawLine(PhysicsCyan, blockCenter, Offset(blockCenter.x, blockCenter.y - fnLen), 3f)

                // 4. Weight W = mg (Down)
                val wLen = 35.dp.toPx()
                drawLine(PhysicsAmber, blockCenter, Offset(blockCenter.x, blockCenter.y + wLen), 3f)

                // Central Pivot dot
                drawCircle(Color.White, 3.dp.toPx(), blockCenter)
            }

            // Top Status Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
                shape = RoundedCornerShape(8.dp),
                color = (if (isMoving) PhysicsGreen else PhysicsAmber).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isMoving) PhysicsGreen else PhysicsAmber)
            ) {
                Text(
                    text = if (isMoving) "حرکت شتابدار (F > f_s,max)" else "ساکن (F ≤ f_s,max)",
                    color = if (isMoving) PhysicsGreen else PhysicsAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.85f)
            ) {
                Text(
                    text = "سرعت: ${"%.1f".format(currentSpeed)} m/s",
                    color = PhysicsCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Action Buttons: Run & Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) PhysicsAmber else PhysicsGreen
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isRunning) "مکث نیرو" else "اعمال نیرو و حرکت", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    isRunning = false
                    displacementX = 0f
                    currentSpeed = 0f
                },
                modifier = Modifier.width(100.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("شروع مجدد", fontSize = 12.sp)
            }
        }

        // Metrics: Acceleration, Net Force, Max Static Friction
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "شتاب حرکت (a)",
                value = "${"%.2f".format(acceleration)} m/s²",
                formula = "a = ΣF / m",
                color = PhysicsGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "نیروی برآیند (ΣF)",
                value = "${"%.1f".format(netForce)} N",
                formula = "F_app - f_k",
                color = PhysicsCyan,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "اصطکاک ایستایی بیشینه",
                value = "${"%.1f".format(maxStaticFriction)} N",
                formula = "μ_s · mg",
                color = PhysicsRed,
                modifier = Modifier.weight(1f)
            )
        }

        // Sliders: Force & Mass
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Force Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("نیروی وارد بر جسم (F):", fontSize = 13.sp)
                    Text("${appliedForce.toInt()} نیوتن (N)", color = PhysicsGreen, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = appliedForce,
                    onValueChange = { appliedForce = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = PhysicsGreen, activeTrackColor = PhysicsGreen)
                )

                // Mass Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("جرم جسم (m):", fontSize = 13.sp)
                    Text("${mass.toInt()} کیلوگرم (kg)", color = PhysicsCyan, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = mass,
                    onValueChange = { mass = it },
                    valueRange = 1f..15f,
                    colors = SliderDefaults.colors(thumbColor = PhysicsCyan, activeTrackColor = PhysicsCyan)
                )

                // Friction coefficient μ_s
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ضریب اصطکاک ایستایی (μ_s):", fontSize = 13.sp)
                    Text("${"%.2f".format(muS)}", color = PhysicsRed, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = muS,
                    onValueChange = {
                        muS = it
                        if (muK > it) muK = it
                    },
                    valueRange = 0.1f..0.8f,
                    colors = SliderDefaults.colors(thumbColor = PhysicsRed, activeTrackColor = PhysicsRed)
                )
            }
        }
    }
}
