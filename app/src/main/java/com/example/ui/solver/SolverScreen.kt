package com.example.ui.solver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhysicsRepository
import com.example.data.SavedProblemEntity
import com.example.model.SolverResult
import com.example.model.SolverStep
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

enum class SolverTopic(val title: String, val category: String) {
    KINEMATICS("حرکت با شتاب ثابت", "پایه دوازدهم"),
    DYNAMICS("دینامیک و اصطکاک", "پایه دوازدهم"),
    ENERGY("کار و انرژی مکانیکی", "پایه دهم"),
    HEAT("گرما و تعادل دمایی", "پایه دهم"),
    CIRCUITS("مقاومت معادل و مدار", "پایه یازدهم"),
    COULOMB("قانون کولن و بار", "پایه یازدهم")
}

@Composable
fun SolverScreen(
    repository: PhysicsRepository,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTopic by remember { mutableStateOf(SolverTopic.KINEMATICS) }
    var solverResult by remember { mutableStateOf<SolverResult?>(null) }

    // Inputs for Kinematics
    var kVo by remember { mutableStateOf("10") }
    var kA by remember { mutableStateOf("2.5") }
    var kT by remember { mutableStateOf("4") }

    // Inputs for Dynamics
    var dMass by remember { mutableStateOf("4") }
    var dForce by remember { mutableStateOf("30") }
    var dMuK by remember { mutableStateOf("0.2") }

    // Inputs for Energy
    var eMass by remember { mutableStateOf("2") }
    var eHeight by remember { mutableStateOf("5") }
    var eVel by remember { mutableStateOf("8") }

    // Inputs for Heat
    var hM1 by remember { mutableStateOf("0.5") }
    var hC1 by remember { mutableStateOf("4200") } // Water
    var hT1 by remember { mutableStateOf("20") }
    var hM2 by remember { mutableStateOf("1.0") }
    var hC2 by remember { mutableStateOf("900") } // Aluminum
    var hT2 by remember { mutableStateOf("80") }

    // Inputs for Circuits
    var cR1 by remember { mutableStateOf("10") }
    var cR2 by remember { mutableStateOf("20") }
    var cR3 by remember { mutableStateOf("30") }
    var cVoltage by remember { mutableStateOf("12") }
    var isSeries by remember { mutableStateOf(true) }

    // Inputs for Coulomb
    var q1Micro by remember { mutableStateOf("5") }
    var q2Micro by remember { mutableStateOf("-8") }
    var distCm by remember { mutableStateOf("30") }

    // Calculation logic
    fun solveCurrentTopic() {
        when (selectedTopic) {
            SolverTopic.KINEMATICS -> {
                val v0 = kVo.toFloatOrNull() ?: 0f
                val a = kA.toFloatOrNull() ?: 0f
                val t = kT.toFloatOrNull() ?: 0f

                val v = v0 + a * t
                val deltaX = v0 * t + 0.5f * a * t * t

                solverResult = SolverResult(
                    title = "حل گام‌به‌گام حرکت با شتاب ثابت",
                    category = "سینماتیک (پایه دوازدهم)",
                    inputSummary = "v₀ = $v0 m/s , a = $a m/s² , t = $t s",
                    resultSummary = "v = ${"%.2f".format(v)} m/s | Δx = ${"%.2f".format(deltaX)} m",
                    steps = listOf(
                        SolverStep(
                            1,
                            "مشخص کردن داده‌ها و یکاهای SI",
                            "سرعت اولیه: v₀ = $v0 m/s\nشتاب ثابت: a = $a m/s²\nزمان حرکت: t = $t s",
                            "داده‌های اولیه همگی در یکای استاندارد SI هستند.",
                            "نیازی به تبدیل یکا نیست."
                        ),
                        SolverStep(
                            2,
                            "محاسبه سرعت نهایی (v)",
                            "از رابطه سرعت - زمان برای حرکت با شتاب ثابت استفاده می‌کنیم:",
                            "v = v₀ + a · t",
                            "v = $v0 + ($a × $t) = $v0 + ${a * t} = ${"%.2f".format(v)} m/s"
                        ),
                        SolverStep(
                            3,
                            "محاسبه جابه‌جایی متحرک (Δx)",
                            "از رابطه مکان - زمان در حرکت با شتاب ثابت استفاده می‌کنیم:",
                            "Δx = v₀ · t + ½ a · t²",
                            "Δx = ($v0 × $t) + ½ × $a × ($t)²\nΔx = ${v0 * t} + ${0.5f * a * t * t} = ${"%.2f".format(deltaX)} متر"
                        ),
                        SolverStep(
                            4,
                            "بررسی با رابطه مستقل از زمان (تأیید پاسخ)",
                            "برای اطمینان، رابطه مستقل از زمان را بررسی می‌کنیم:",
                            "v² - v₀² = 2 · a · Δx",
                            "${"%.2f".format(v * v)} - ${"%.2f".format(v0 * v0)} = 2 × $a × ${"%.2f".format(deltaX)} ✓ تایید شد"
                        )
                    )
                )
            }

            SolverTopic.DYNAMICS -> {
                val m = dMass.toFloatOrNull() ?: 1f
                val f = dForce.toFloatOrNull() ?: 0f
                val mu = dMuK.toFloatOrNull() ?: 0.1f
                val g = 9.8f

                val fn = m * g
                val fk = mu * fn
                val netF = f - fk
                val acc = if (netF > 0) netF / m else 0f

                solverResult = SolverResult(
                    title = "حل دینامیک و قانون دوم نیوتن",
                    category = "دینامیک (پایه دوازدهم)",
                    inputSummary = "m = $m kg , F = $f N , μ_k = $mu",
                    resultSummary = "شتاب a = ${"%.2f".format(acc)} m/s² | اصطکاک f_k = ${"%.2f".format(fk)} N",
                    steps = listOf(
                        SolverStep(
                            1,
                            "رسم نیروها و محاسبه نیروی عمودی تکیه‌گاه (F_N)",
                            "چون جسم در راستای قائم حرکتی ندارد، برآیند نیروهای قائم صفر است: F_N - mg = 0",
                            "F_N = m · g",
                            "F_N = $m kg × 9.8 m/s² = ${"%.2f".format(fn)} N"
                        ),
                        SolverStep(
                            2,
                            "محاسبه نیروی اصطکاک جنبشی (f_k)",
                            "نیروی اصطکاک برابر حاصل‌ضرب ضریب اصطکاک در نیروی عمودی تکیه‌گاه است:",
                            "f_k = μ_k · F_N",
                            "f_k = $mu × ${"%.2f".format(fn)} = ${"%.2f".format(fk)} N"
                        ),
                        SolverStep(
                            3,
                            "محاسبه نیروی خالص برآیند (ΣF)",
                            "نیروی پیشران منهای اصطکاک بازدارنده:",
                            "ΣF = F - f_k",
                            "ΣF = $f - ${"%.2f".format(fk)} = ${"%.2f".format(netF)} N"
                        ),
                        SolverStep(
                            4,
                            "محاسبه شتاب بر اساس قانون دوم نیوتن",
                            "شتاب متناسب با نیروی برآیند و عکس جرم است:",
                            "a = ΣF / m",
                            "a = ${"%.2f".format(netF)} / $m = ${"%.2f".format(acc)} m/s²"
                        )
                    )
                )
            }

            SolverTopic.ENERGY -> {
                val m = eMass.toFloatOrNull() ?: 1f
                val h = eHeight.toFloatOrNull() ?: 0f
                val v = eVel.toFloatOrNull() ?: 0f
                val g = 9.8f

                val u = m * g * h
                val k = 0.5f * m * v * v
                val totalE = u + k

                solverResult = SolverResult(
                    title = "محاسبه کار و انرژی مکانیکی",
                    category = "کار و انرژی (پایه دهم)",
                    inputSummary = "m = $m kg , h = $h m , v = $v m/s",
                    resultSummary = "انرژی کل E = ${"%.2f".format(totalE)} J (K=${"%.2f".format(k)} J, U=${"%.2f".format(u)} J)",
                    steps = listOf(
                        SolverStep(
                            1,
                            "محاسبه انرژی پتانسیل گرانشی (U)",
                            "انرژی وابسته به ارتفاع از سطح زمین:",
                            "U = m · g · h",
                            "U = $m × 9.8 × $h = ${"%.2f".format(u)} ژول (J)"
                        ),
                        SolverStep(
                            2,
                            "محاسبه انرژی جنبشی (K)",
                            "انرژی ناشی از تندی جسم:",
                            "K = ½ m · v²",
                            "K = 0.5 × $m × ($v)² = ${"%.2f".format(k)} ژول (J)"
                        ),
                        SolverStep(
                            3,
                            "محاسبه انرژی مکانیکی کل (E)",
                            "طبق پایستگی انرژی مکانیکی، انرژی کل مجموع K و U است:",
                            "E_total = K + U",
                            "E_total = ${"%.2f".format(k)} + ${"%.2f".format(u)} = ${"%.2f".format(totalE)} ژول (J)"
                        )
                    )
                )
            }

            SolverTopic.HEAT -> {
                val m1 = hM1.toFloatOrNull() ?: 1f
                val c1 = hC1.toFloatOrNull() ?: 4200f
                val t1 = hT1.toFloatOrNull() ?: 20f
                val m2 = hM2.toFloatOrNull() ?: 1f
                val c2 = hC2.toFloatOrNull() ?: 900f
                val t2 = hT2.toFloatOrNull() ?: 80f

                val cTotal1 = m1 * c1
                val cTotal2 = m2 * c2
                val te = (cTotal1 * t1 + cTotal2 * t2) / (cTotal1 + cTotal2)

                solverResult = SolverResult(
                    title = "محاسبه تعادل گرمایی دو جسم",
                    category = "دما و گرما (پایه دهم)",
                    inputSummary = "ماده ۱: $m1 kg, $t1°C | ماده ۲: $m2 kg, $t2°C",
                    resultSummary = "دمای تعادل T_e = ${"%.2f".format(te)} °C",
                    steps = listOf(
                        SolverStep(
                            1,
                            "قانون پایستگی انرژی گرمایی",
                            "در یک ظرف عایق گرما، مجموع گرماهای مبادله شده برابر صفر است:",
                            "Q₁ + Q₂ = 0",
                            "m₁c₁(T_e - T₁) + m₂c₂(T_e - T₂) = 0"
                        ),
                        SolverStep(
                            2,
                            "محاسبه ظرفیت گرمایی اجسام (C = mc)",
                            "ظرفیت گرمایی هر جزء را محاسبه می‌کنیم:",
                            "C₁ = m₁c₁ , C₂ = m₂c₂",
                            "C₁ = $m1 × $c1 = $cTotal1 J/K\nC₂ = $m2 × $c2 = $cTotal2 J/K"
                        ),
                        SolverStep(
                            3,
                            "استخراج رابطه دمای تعادل (T_e)",
                            "با فاکتورگیری از T_e خواهیم داشت:",
                            "T_e = (C₁·T₁ + C₂·T₂) / (C₁ + C₂)",
                            "T_e = ($cTotal1 × $t1 + $cTotal2 × $t2) / ($cTotal1 + $cTotal2)\nT_e = ${cTotal1 * t1 + cTotal2 * t2} / ${cTotal1 + cTotal2} = ${"%.2f".format(te)} °C"
                        )
                    )
                )
            }

            SolverTopic.CIRCUITS -> {
                val r1 = cR1.toFloatOrNull() ?: 10f
                val r2 = cR2.toFloatOrNull() ?: 20f
                val r3 = cR3.toFloatOrNull() ?: 30f
                val v = cVoltage.toFloatOrNull() ?: 12f

                val req = if (isSeries) {
                    r1 + r2 + r3
                } else {
                    1f / (1f / r1 + 1f / r2 + 1f / r3)
                }
                val iTotal = if (req > 0) v / req else 0f
                val pTotal = v * iTotal

                solverResult = SolverResult(
                    title = if (isSeries) "تحلیل مدار متوالی مقاومت‌ها" else "تحلیل مدار موازی مقاومت‌ها",
                    category = "الکتریسیته جاری (پایه یازدهم)",
                    inputSummary = "R₁=${r1}Ω, R₂=${r2}Ω, R₃=${r3}Ω, V=${v} V",
                    resultSummary = "R_eq = ${"%.2f".format(req)} Ω | I = ${"%.2f".format(iTotal)} A | P = ${"%.2f".format(pTotal)} W",
                    steps = listOf(
                        SolverStep(
                            1,
                            if (isSeries) "محاسبه مقاومت معادل در اتصال متوالی" else "محاسبه مقاومت معادل در اتصال موازی",
                            if (isSeries) "در اتصال متوالی، مقاومت‌ها مستقیماً با یکدیگر جمع می‌شوند:" else "در اتصال موازی، معکوس مقاومت معادل برابر مجموع معکوس تک‌تک مقاومت‌ها است:",
                            if (isSeries) "R_eq = R₁ + R₂ + R₃" else "1/R_eq = 1/R₁ + 1/R₂ + 1/R₃",
                            if (isSeries) "R_eq = $r1 + $r2 + $r3 = ${"%.2f".format(req)} Ω" else "1/R_eq = 1/$r1 + 1/$r2 + 1/$r3 = ${"%.4f".format(1f / req)} → R_eq = ${"%.2f".format(req)} Ω"
                        ),
                        SolverStep(
                            2,
                            "محاسبه جریان کل عبوری از مدار با قانون اهم",
                            "جریان کل از نسبت اختلاف پتانسیل کل به مقاومت معادل کل به دست می‌آید:",
                            "I_total = V / R_eq",
                            "I_total = $v / ${"%.2f".format(req)} = ${"%.3f".format(iTotal)} آمپر (A)"
                        ),
                        SolverStep(
                            3,
                            "محاسبه توان الکتریکی کل مصرفی مدار",
                            "توان الکتریکی مصرف شده در کل مدار:",
                            "P = V · I_total",
                            "P = $v × ${"%.3f".format(iTotal)} = ${"%.2f".format(pTotal)} وات (W)"
                        )
                    )
                )
            }

            SolverTopic.COULOMB -> {
                val q1 = (q1Micro.toFloatOrNull() ?: 1f) * 1e-6f
                val q2 = (q2Micro.toFloatOrNull() ?: 1f) * 1e-6f
                val r = (distCm.toFloatOrNull() ?: 10f) * 0.01f
                val k = 8.99e9f

                val force = k * (abs(q1 * q2)) / (r * r)
                val isAttractive = (q1 * q2) < 0

                solverResult = SolverResult(
                    title = "محاسبه نیروی قانون کولن بین دو بار نقطه‌ای",
                    category = "الکتریسیته ساکن (پایه یازدهم)",
                    inputSummary = "q₁ = $q1Micro μC , q₂ = $q2Micro μC , r = $distCm cm",
                    resultSummary = "نیرو F = ${"%.2f".format(force)} N (${if (isAttractive) "ربایشی" else "رانشی"})",
                    steps = listOf(
                        SolverStep(
                            1,
                            "تبدیل یکاها به دستگاه بین‌المللی SI",
                            "میکروکولن (μC) به کولن (C) با ضرب در ۱۰⁻⁶ و سانتی‌متر (cm) به متر (m) با ضرب در ۱۰⁻² تبدیل می‌شوند.",
                            "q₁ = $q1Micro × 10⁻⁶ C\nq₂ = $q2Micro × 10⁻⁶ C\nr = $distCm × 10⁻² = $r m",
                            "یکاهای استاندارد آماده جایگذاری در فرمول هستند."
                        ),
                        SolverStep(
                            2,
                            "قانون کولن و جایگذاری مقادیر",
                            "ثابت کولن در خلأ: k = 8.99 × 10⁹ N·m²/C²",
                            "F = k · |q₁ · q₂| / r²",
                            "F = (8.99 × 10⁹ × |$q1Micro × $q2Micro| × 10⁻¹²) / ($r)²\nF = ${"%.3f".format(force)} نیوتن (N)"
                        ),
                        SolverStep(
                            3,
                            "تعیین نوع نیرو (جاذبه یا دافعه)",
                            "بررسی همنام یا ناهم‌نام بودن بارها:",
                            "علامت حاصل‌ضرب q₁ · q₂",
                            if (isAttractive) "بارهای الکتریکی ناهم‌نام هستند (+ و -)؛ بنابراین نیروی بین آن‌ها «ربایشی (جذب)» است." else "بارهای الکتریکی همنام هستند؛ بنابراین نیروی بین آن‌ها «رانشی (دفع)» است."
                        )
                    )
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "حل‌المسائل گام‌به‌گام فیزیک",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PhysicsCyan
            )
            Text(
                "موضوع مورد نظر خود را انتخاب کرده، داده‌ها را وارد کنید و راه‌حل تشریحی کامل را دریافت نمایید.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Horizontal topic selector
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SolverTopic.values()) { topic ->
                    val isSelected = selectedTopic == topic
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTopic = topic
                            solverResult = null
                        },
                        label = {
                            Text(
                                topic.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhysicsPurple.copy(alpha = 0.25f),
                            selectedLabelColor = PhysicsPurple
                        )
                    )
                }
            }
        }

        // Input Fields Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "داده‌های مسئله (${selectedTopic.title})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    when (selectedTopic) {
                        SolverTopic.KINEMATICS -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = kVo,
                                    onValueChange = { kVo = it },
                                    label = { Text("سرعت اولیه v₀ (m/s)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                OutlinedTextField(
                                    value = kA,
                                    onValueChange = { kA = it },
                                    label = { Text("شتاب a (m/s²)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                            OutlinedTextField(
                                value = kT,
                                onValueChange = { kT = it },
                                label = { Text("مدت زمان t (s)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        SolverTopic.DYNAMICS -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = dMass,
                                    onValueChange = { dMass = it },
                                    label = { Text("جرم m (kg)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                OutlinedTextField(
                                    value = dForce,
                                    onValueChange = { dForce = it },
                                    label = { Text("نیروی F (N)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                            OutlinedTextField(
                                value = dMuK,
                                onValueChange = { dMuK = it },
                                label = { Text("ضریب اصطکاک جنبشی (μ_k)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        SolverTopic.ENERGY -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = eMass,
                                    onValueChange = { eMass = it },
                                    label = { Text("جرم m (kg)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                OutlinedTextField(
                                    value = eHeight,
                                    onValueChange = { eHeight = it },
                                    label = { Text("ارتفاع h (m)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                            OutlinedTextField(
                                value = eVel,
                                onValueChange = { eVel = it },
                                label = { Text("تندی v (m/s)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        SolverTopic.HEAT -> {
                            Text("ماده اول:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = hM1,
                                    onValueChange = { hM1 = it },
                                    label = { Text("جرم m₁ (kg)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = hC1,
                                    onValueChange = { hC1 = it },
                                    label = { Text("گرمای ویژه c₁") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = hT1,
                                    onValueChange = { hT1 = it },
                                    label = { Text("دمای اولیه T₁ (°C)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text("ماده دوم:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = hM2,
                                    onValueChange = { hM2 = it },
                                    label = { Text("جرم m₂ (kg)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = hC2,
                                    onValueChange = { hC2 = it },
                                    label = { Text("گرمای ویژه c₂") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = hT2,
                                    onValueChange = { hT2 = it },
                                    label = { Text("دمای اولیه T₂ (°C)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        SolverTopic.CIRCUITS -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cR1,
                                    onValueChange = { cR1 = it },
                                    label = { Text("R₁ (Ω)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = cR2,
                                    onValueChange = { cR2 = it },
                                    label = { Text("R₂ (Ω)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = cR3,
                                    onValueChange = { cR3 = it },
                                    label = { Text("R₃ (Ω)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = cVoltage,
                                    onValueChange = { cVoltage = it },
                                    label = { Text("ولتاژ V (Volt)") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (isSeries) "بستن متوالی" else "بستن موازی", fontSize = 12.sp)
                                    Switch(checked = isSeries, onCheckedChange = { isSeries = it })
                                }
                            }
                        }

                        SolverTopic.COULOMB -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = q1Micro,
                                    onValueChange = { q1Micro = it },
                                    label = { Text("بار q₁ (μC)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = q2Micro,
                                    onValueChange = { q2Micro = it },
                                    label = { Text("بار q₂ (μC)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = distCm,
                                onValueChange = { distCm = it },
                                label = { Text("فاصله بین دو بار r (cm)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Button(
                        onClick = { solveCurrentTopic() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PhysicsCyan)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = DarkBackground)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("محاسبه و نمایش حل تشریحی گام‌به‌گام", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Display Step-by-Step Solver Results
        if (solverResult != null) {
            val result = solverResult!!

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PhysicsGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("نتیجه نهایی محاسبات:", fontSize = 11.sp, color = PhysicsGreen)
                            Text(
                                result.resultSummary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val entity = SavedProblemEntity(
                                        title = result.title,
                                        category = result.category,
                                        inputSummary = result.inputSummary,
                                        resultSummary = result.resultSummary,
                                        stepsList = result.steps.joinToString("\n") { "گام ${it.stepNumber}: ${it.title} | ${it.calculation}" }
                                    )
                                    repository.saveProblem(entity)
                                    snackbarHostState.showSnackbar("مسئله با موفقیت در تاریخچه ذخیره شد!")
                                }
                            }
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "ذخیره مسئله", tint = PhysicsGreen)
                        }
                    }
                }
            }

            // Steps items
            items(result.steps) { step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PhysicsPurple,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        step.stepNumber.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                step.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            step.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Formula chip box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = step.formula,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PhysicsCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Calculation result
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = step.calculation,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
