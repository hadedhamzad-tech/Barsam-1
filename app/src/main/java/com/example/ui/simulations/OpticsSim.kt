package com.example.ui.simulations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.PersianUtils
import kotlin.math.*

data class OpticalMedium(val name: String, val n: Float, val color: Color)

@Composable
fun OpticsSim() {
    val media = remember {
        listOf(
            OpticalMedium("هوا", 1.00f, Color(0xFF1E293B)),
            OpticalMedium("آب", 1.33f, Color(0xFF0284C7)),
            OpticalMedium("شیشه", 1.50f, Color(0xFF0D9488)),
            OpticalMedium("الماس", 2.42f, Color(0xFF7C3AED))
        )
    }

    var medium1 by remember { mutableStateOf(media[0]) } // Air default
    var medium2 by remember { mutableStateOf(media[1]) } // Water default
    var theta1Deg by remember { mutableFloatStateOf(45f) } // Incident angle

    val n1 = medium1.n
    val n2 = medium2.n

    // Critical Angle calculation
    val hasCriticalAngle = n1 > n2
    val criticalAngleDeg = if (hasCriticalAngle && n1 > 0f) {
        val ratio = (n2 / n1.toDouble()).coerceIn(0.0, 1.0)
        Math.toDegrees(asin(ratio)).toFloat()
    } else null

    // Snell's Law with robust float clamping to prevent NaN
    val theta1Rad = Math.toRadians(theta1Deg.toDouble())
    val rawSinTheta2 = (n1 * sin(theta1Rad)) / max(n2, 0.001f)
    val isTotalInternalReflection = rawSinTheta2 > 1.00001
    val clampedSin = rawSinTheta2.coerceIn(0.0, 1.0)

    val theta2Deg = if (!isTotalInternalReflection) {
        Math.toDegrees(asin(clampedSin)).toFloat()
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Media Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Medium 1 Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("محیط اول تابش (n₁):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(media) { med ->
                            FilterChip(
                                modifier = Modifier.wrapContentWidth(),
                                selected = medium1 == med,
                                onClick = { medium1 = med },
                                label = {
                                    Text(
                                        text = "${med.name} (${PersianUtils.formatNumber(med.n, 2)})",
                                        fontSize = 11.sp,
                                        fontWeight = if (medium1 == med) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PhysicsCyan.copy(alpha = 0.3f),
                                    selectedLabelColor = PhysicsCyan
                                )
                            )
                        }
                    }
                }

                // Medium 2 Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("محیط دوم شکست (n₂):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(media) { med ->
                            FilterChip(
                                modifier = Modifier.wrapContentWidth(),
                                selected = medium2 == med,
                                onClick = { medium2 = med },
                                label = {
                                    Text(
                                        text = "${med.name} (${PersianUtils.formatNumber(med.n, 2)})",
                                        fontSize = 11.sp,
                                        fontWeight = if (medium2 == med) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PhysicsPurple.copy(alpha = 0.3f),
                                    selectedLabelColor = PhysicsPurple
                                )
                            )
                        }
                    }
                }
            }
        }

        // Live Optics Simulation Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                .border(1.dp, PhysicsCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                val interfaceY = canvasH / 2f
                val centerX = canvasW / 2f

                // Draw Medium 1 Top Half
                drawRect(
                    color = medium1.color.copy(alpha = 0.25f),
                    topLeft = Offset(0f, 0f),
                    size = Size(canvasW, interfaceY)
                )

                // Draw Medium 2 Bottom Half
                drawRect(
                    color = medium2.color.copy(alpha = 0.4f),
                    topLeft = Offset(0f, interfaceY),
                    size = Size(canvasW, interfaceY)
                )

                // Interface Boundary line
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(0f, interfaceY),
                    end = Offset(canvasW, interfaceY),
                    strokeWidth = 2.5f
                )

                // Normal line (Dashed)
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(centerX, 20.dp.toPx()),
                    end = Offset(centerX, canvasH - 20.dp.toPx()),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )

                // Ray Length
                val rayLength = min(centerX, interfaceY) * 0.88f

                // 1. Incident Ray (Top-Left towards Center)
                val incAngleRad = Math.toRadians(theta1Deg.toDouble())
                val incStartX = centerX - rayLength * sin(incAngleRad).toFloat()
                val incStartY = interfaceY - rayLength * cos(incAngleRad).toFloat()

                // Glow laser beam
                drawLine(
                    color = PhysicsCyan.copy(alpha = 0.35f),
                    start = Offset(incStartX, incStartY),
                    end = Offset(centerX, interfaceY),
                    strokeWidth = 7f
                )
                drawLine(
                    color = PhysicsCyan,
                    start = Offset(incStartX, incStartY),
                    end = Offset(centerX, interfaceY),
                    strokeWidth = 3f
                )
                // Laser Emitter Box
                drawCircle(PhysicsCyan, 6.dp.toPx(), Offset(incStartX, incStartY))

                // 2. Reflected Ray (Center towards Top-Right)
                val refEndX = centerX + rayLength * sin(incAngleRad).toFloat()
                val refEndY = interfaceY - rayLength * cos(incAngleRad).toFloat()
                val refColor = if (isTotalInternalReflection) PhysicsAmber else PhysicsAmber.copy(alpha = 0.6f)
                drawLine(
                    color = refColor,
                    start = Offset(centerX, interfaceY),
                    end = Offset(refEndX, refEndY),
                    strokeWidth = if (isTotalInternalReflection) 4f else 2f
                )

                // 3. Refracted Ray (Center towards Bottom-Right)
                if (!isTotalInternalReflection) {
                    val theta2Rad = Math.toRadians(theta2Deg.toDouble())
                    val refrEndX = centerX + rayLength * sin(theta2Rad).toFloat()
                    val refrEndY = interfaceY + rayLength * cos(theta2Rad).toFloat()

                    drawLine(
                        color = PhysicsGreen.copy(alpha = 0.35f),
                        start = Offset(centerX, interfaceY),
                        end = Offset(refrEndX, refrEndY),
                        strokeWidth = 7f
                    )
                    drawLine(
                        color = PhysicsGreen,
                        start = Offset(centerX, interfaceY),
                        end = Offset(refrEndX, refrEndY),
                        strokeWidth = 3f
                    )
                    drawCircle(PhysicsGreen, 4.dp.toPx(), Offset(refrEndX, refrEndY))
                }

                // Center Point of Incidence
                drawCircle(Color.White, 4.dp.toPx(), Offset(centerX, interfaceY))
            }

            // Labels on Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "محیط ۱: ${medium1.name} (n₁=${PersianUtils.formatNumber(medium1.n, 2)})",
                    fontSize = 11.sp,
                    color = PhysicsCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تابش: θ₁ = ${PersianUtils.formatInt(theta1Deg.toInt())} درجه",
                    fontSize = 11.sp,
                    color = PhysicsCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "محیط ۲: ${medium2.name} (n₂=${PersianUtils.formatNumber(medium2.n, 2)})",
                    fontSize = 11.sp,
                    color = PhysicsPurple,
                    fontWeight = FontWeight.Bold
                )
                if (!isTotalInternalReflection) {
                    Text(
                        text = "شکست: θ₂ = ${PersianUtils.formatNumber(theta2Deg, 1)} درجه",
                        fontSize = 11.sp,
                        color = PhysicsGreen,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "بازتاب کلی (بدون شکست)!",
                        fontSize = 11.sp,
                        color = PhysicsRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Total Internal Reflection Alert
        if (isTotalInternalReflection) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PhysicsRed.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsRed.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "بازتاب کلی رخ داده است! چون نور از محیط غلیظ به رقیق وارد می‌شود و زاویه تابش (${PersianUtils.formatInt(theta1Deg.toInt())} درجه) از زاویه حد (${PersianUtils.formatNumber(criticalAngleDeg ?: 0f, 1)} درجه) بزرگتر است، هیچ نوری وارد محیط دوم نمی‌شود.",
                        fontSize = 11.sp,
                        color = PhysicsRed,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Metrics: Snell Formula, Refraction Angle, Critical Angle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "زاویه شکست (θ₂)",
                value = if (isTotalInternalReflection) "---" else "${PersianUtils.formatNumber(theta2Deg, 1)} درجه",
                formula = "n₁·sin(θ₁) = n₂·sin(θ₂)",
                color = PhysicsGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "زاویه حد (θ_c)",
                value = if (criticalAngleDeg != null) "${PersianUtils.formatNumber(criticalAngleDeg, 1)} درجه" else "وجود ندارد",
                formula = "sin(θ_c) = n₂ / n₁",
                color = PhysicsAmber,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "نسبت شکست",
                value = PersianUtils.formatNumber(n1 / n2, 2),
                formula = "n₁ / n₂",
                color = PhysicsCyan,
                modifier = Modifier.weight(1f)
            )
        }

        // Incident Angle Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("زاویه تابش نور (θ₁):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("${PersianUtils.formatInt(theta1Deg.toInt())} درجه", color = PhysicsCyan, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = theta1Deg,
                    onValueChange = { theta1Deg = it },
                    valueRange = 0f..85f,
                    colors = SliderDefaults.colors(thumbColor = PhysicsCyan, activeTrackColor = PhysicsCyan)
                )
            }
        }
    }
}
