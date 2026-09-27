package com.example.ui.simulations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun CircuitSim() {
    var voltage by remember { mutableFloatStateOf(12f) } // 0V to 30V
    var resistance by remember { mutableFloatStateOf(20f) } // 5Ω to 150Ω
    var isSwitchClosed by remember { mutableStateOf(true) }

    val current = if (isSwitchClosed && resistance > 0) voltage / resistance else 0f
    val power = current * (if (isSwitchClosed) voltage else 0f)

    // Electron particle animation loop
    var electronOffset by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isSwitchClosed, current) {
        while (true) {
            if (isSwitchClosed && current > 0) {
                electronOffset = (electronOffset + current * 0.15f) % 1000f
            }
            delay(30)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Circuit Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF0D1424), RoundedCornerShape(16.dp))
                .border(1.dp, PhysicsCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                val padX = 40.dp.toPx()
                val padY = 30.dp.toPx()
                val rectW = canvasW - 2 * padX
                val rectH = canvasH - 2 * padY

                // Circuit Wire Loop (Top, Right, Bottom, Left)
                val wireColor = Color(0xFF64748B)
                val wireActiveColor = if (isSwitchClosed && current > 0) PhysicsCyan else wireColor

                // Draw rectangular wire
                drawRect(
                    color = wireActiveColor.copy(alpha = 0.6f),
                    topLeft = Offset(padX, padY),
                    size = Size(rectW, rectH),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                )

                // 1. Battery (Left Branch: X = padX, Y around center)
                val batY = padY + rectH / 2f
                val batH = 30.dp.toPx()
                // Clear wire for battery
                drawLine(Color(0xFF0D1424), Offset(padX, batY - batH), Offset(padX, batY + batH), 6f)
                // Long plate (+), Short plate (-)
                drawLine(PhysicsAmber, Offset(padX - 16.dp.toPx(), batY - 10.dp.toPx()), Offset(padX + 16.dp.toPx(), batY - 10.dp.toPx()), 4f)
                drawLine(PhysicsAmber, Offset(padX - 8.dp.toPx(), batY + 10.dp.toPx()), Offset(padX + 8.dp.toPx(), batY + 10.dp.toPx()), 6f)

                // 2. Switch (Top Branch: Y = padY, X around center)
                val switchX = padX + rectW / 2f
                val switchGap = 20.dp.toPx()
                drawLine(Color(0xFF0D1424), Offset(switchX - switchGap, padY), Offset(switchX + switchGap, padY), 6f)
                drawCircle(PhysicsCyan, 4.dp.toPx(), Offset(switchX - switchGap, padY))
                drawCircle(PhysicsCyan, 4.dp.toPx(), Offset(switchX + switchGap, padY))
                if (isSwitchClosed) {
                    drawLine(PhysicsCyan, Offset(switchX - switchGap, padY), Offset(switchX + switchGap, padY), 3f)
                } else {
                    // Open switch arm
                    drawLine(PhysicsAmber, Offset(switchX - switchGap, padY), Offset(switchX + switchGap * 0.8f, padY - 18.dp.toPx()), 3f)
                }

                // 3. Lightbulb & Resistor (Right Branch: X = padX + rectW, Y around center)
                val bulbX = padX + rectW
                val bulbY = batY
                val bulbRadius = 22.dp.toPx()

                // Glow radial gradient when active
                if (isSwitchClosed && power > 0) {
                    val glowIntensity = (power / 20f).coerceIn(0.2f, 1.8f)
                    val glowRadius = bulbRadius * (1f + glowIntensity)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(PhysicsAmber.copy(alpha = 0.8f), Color.Transparent),
                            center = Offset(bulbX, bulbY),
                            radius = glowRadius
                        ),
                        radius = glowRadius,
                        center = Offset(bulbX, bulbY)
                    )
                }

                // Bulb Glass Circle
                drawCircle(
                    color = if (isSwitchClosed && power > 0) PhysicsAmber else Color(0xFF334155),
                    radius = bulbRadius,
                    center = Offset(bulbX, bulbY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = bulbRadius,
                    center = Offset(bulbX, bulbY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(2f)
                )
                // Filament (Z-shape)
                drawLine(Color(0xFFFFD54F), Offset(bulbX - 8.dp.toPx(), bulbY - 6.dp.toPx()), Offset(bulbX, bulbY + 6.dp.toPx()), 2.5f)
                drawLine(Color(0xFFFFD54F), Offset(bulbX, bulbY + 6.dp.toPx()), Offset(bulbX + 8.dp.toPx(), bulbY - 6.dp.toPx()), 2.5f)

                // 4. Moving Electron particles (drifting through the wire)
                if (isSwitchClosed && current > 0) {
                    val perimeter = 2 * (rectW + rectH)
                    val numElectrons = 16
                    for (i in 0 until numElectrons) {
                        val pos = (electronOffset * 30f + (i * perimeter / numElectrons)) % perimeter
                        val (ex, ey) = when {
                            pos < rectW -> Offset(padX + pos, padY) // Top wire (L to R)
                            pos < rectW + rectH -> Offset(padX + rectW, padY + (pos - rectW)) // Right wire (T to B)
                            pos < 2 * rectW + rectH -> Offset(padX + rectW - (pos - rectW - rectH), padY + rectH) // Bottom (R to L)
                            else -> Offset(padX, padY + rectH - (pos - 2 * rectW - rectH)) // Left (B to T)
                        }
                        drawCircle(PhysicsCyan, 3.5.dp.toPx(), Offset(ex, ey))
                    }
                }
            }

            // Status Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("باتری (+ / -)", color = PhysicsAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isSwitchClosed) "کلید: وصل" else "کلید: قطع (مدار باز)",
                    color = if (isSwitchClosed) PhysicsGreen else PhysicsRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("مصرف‌کننده (لامپ)", color = PhysicsAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Switch Toggle Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isSwitchClosed = !isSwitchClosed },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSwitchClosed) PhysicsRed else PhysicsGreen
                )
            ) {
                Text(
                    if (isSwitchClosed) "قطع کردن کلید مدار" else "وصل کردن کلید مدار",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Metrics: Current (I), Power (P), Resistance (R)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "شدت جریان (I)",
                value = "${"%.2f".format(current)} A",
                formula = "I = V / R",
                color = PhysicsCyan,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "توان روشنایی (P)",
                value = "${"%.1f".format(power)} W",
                formula = "P = V · I",
                color = PhysicsAmber,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "اختلاف پتانسیل",
                value = "${voltage.toInt()} V",
                formula = "V = I · R",
                color = PhysicsGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Sliders: Voltage & Resistance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Voltage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ولتاژ منبع تغذیه (V):", fontSize = 13.sp)
                    Text("${voltage.toInt()} ولت", color = PhysicsCyan, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = voltage,
                    onValueChange = { voltage = it },
                    valueRange = 0f..24f,
                    colors = SliderDefaults.colors(thumbColor = PhysicsCyan, activeTrackColor = PhysicsCyan)
                )

                // Resistance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("مقاومت الکتریکی لامپ (R):", fontSize = 13.sp)
                    Text("${resistance.toInt()} اهم (Ω)", color = PhysicsAmber, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = resistance,
                    onValueChange = { resistance = it },
                    valueRange = 5f..100f,
                    colors = SliderDefaults.colors(thumbColor = PhysicsAmber, activeTrackColor = PhysicsAmber)
                )
            }
        }
    }
}
