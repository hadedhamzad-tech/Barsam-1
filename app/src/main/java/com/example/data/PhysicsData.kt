package com.example.data

import com.example.model.FormulaItem
import com.example.model.QuizQuestion
import com.example.model.VariableItem

object PhysicsData {
    val formulas = listOf(
        // پایه دهم (Grade 10)
        FormulaItem(
            id = "g10_density",
            grade = "پایه دهم",
            category = "اندازه‌گیری و ماده",
            title = "چگالی (جرم حجمی)",
            formula = "ρ = m / V",
            description = "نسبت جرم ماده به حجم اشغال شده توسط آن ماده.",
            variables = listOf(
                VariableItem("ρ", "چگالی", "kg/m³ یا g/cm³"),
                VariableItem("m", "جرم ماده", "kg"),
                VariableItem("V", "حجم ماده", "m³")
            ),
            keyTip = "برای تبدیل g/cm³ به kg/m³ عدد را در ۱۰۰۰ ضرب کنید. چگالی آب خالص ۱۰۰۰ کیلوگرم بر متر مکعب است."
        ),
        FormulaItem(
            id = "g10_work",
            grade = "پایه دهم",
            category = "کار و انرژی",
            title = "کار نیروی ثابت",
            formula = "W = F · d · cos(θ)",
            description = "کار انجام شده توسط نیروی ثابت F در جابه‌جایی d با زاویه θ نسبت به جهت جابه‌جایی.",
            variables = listOf(
                VariableItem("W", "کار مکانیکی", "J (ژول)"),
                VariableItem("F", "بزرگی نیرو", "N (نیوتن)"),
                VariableItem("d", "جابه‌جایی", "m (متر)"),
                VariableItem("θ", "زاویه بین بردار نیرو و جابه‌جایی", "درجه یا رادیان")
            ),
            keyTip = "اگر نیرو عمود بر جابه‌جایی باشد (θ = 90°)، کار انجام شده صفر است (مانند نیروی عمودی سطح یا نیروی وزن در حرکت افقی)."
        ),
        FormulaItem(
            id = "g10_kinetic_energy",
            grade = "پایه دهم",
            category = "کار و انرژی",
            title = "انرژی جنبشی",
            formula = "K = ½ m v²",
            description = "انرژی ناشی از حرکت جسم به جرم m و تندی v.",
            variables = listOf(
                VariableItem("K", "انرژی جنبشی", "J (ژول)"),
                VariableItem("m", "جرم جسم", "kg"),
                VariableItem("v", "تندی جسم", "m/s")
            ),
            keyTip = "قضیه کار و انرژی جنبشی: W_total = ΔK = K₂ - K₁. کار کل برآیند نیروها برابر تغییر انرژی جنبشی است."
        ),
        FormulaItem(
            id = "g10_potential_energy",
            grade = "پایه دهم",
            category = "کار و انرژی",
            title = "انرژی پتانسیل گرانشی",
            formula = "U = m · g · h",
            description = "انرژی ذخیره شده در جسم به دلیل موقعیت ارتفاعی نسبت به مبدأ پتانسیل.",
            variables = listOf(
                VariableItem("U", "انرژی پتانسیل گرانشی", "J (ژول)"),
                VariableItem("m", "جرم", "kg"),
                VariableItem("g", "شتاب گرانش زمین (تقریباً ۹٫۸)", "m/s²"),
                VariableItem("h", "ارتفاع از مبدأ", "m")
            ),
            keyTip = "کار نیروی گرانش همیشه برابر قرینه تغییر انرژی پتانسیل گرانشی است: W_g = -ΔU."
        ),
        FormulaItem(
            id = "g10_heat_capacity",
            grade = "پایه دهم",
            category = "دما و گرما",
            title = "گرمای مبادله شده",
            formula = "Q = m · c · ΔT",
            description = "مقدار گرمای لازم برای تغییر دمای جسمی به جرم m با گرمای ویژه c.",
            variables = listOf(
                VariableItem("Q", "مقدار گرما", "J (ژول)"),
                VariableItem("m", "جرم ماده", "kg"),
                VariableItem("c", "ظرفیت گرمایی ویژه", "J/(kg·K)"),
                VariableItem("ΔT", "تغییرات دما (T₂ - T₁)", "K یا °C")
            ),
            keyTip = "گرمای ویژه آب بسیار بالاست (حدود ۴۲۰۰ ژول بر کیلوگرم درجه سلسیوس)، به همین دلیل آب کند گرم و کند سرد می‌شود."
        ),

        // پایه یازدهم (Grade 11)
        FormulaItem(
            id = "g11_coulomb",
            grade = "پایه یازدهم",
            category = "الکتریسیته ساکن",
            title = "قانون کولن",
            formula = "F = k · |q₁ · q₂| / r²",
            description = "نیروی الکترواستاتیکی بین دو بار نقطه‌ای ساکن در فاصله r از یکدیگر.",
            variables = listOf(
                VariableItem("F", "نیروی الکتریکی", "N (نیوتن)"),
                VariableItem("k", "ثابت کولن (۸٫۹۹ × ۱۰⁹)", "N·m²/C²"),
                VariableItem("q₁, q₂", "اندازه بارهای الکتریکی", "C (کولن)"),
                VariableItem("r", "فاصله دو بار", "m (متر)")
            ),
            keyTip = "بارهای همنام یکدیگر را دفع و بارهای ناهم‌نام یکدیگر را جذب می‌کنند. نیرو هم‌راستای خط واصل دو بار است."
        ),
        FormulaItem(
            id = "g11_electric_field",
            grade = "پایه یازدهم",
            category = "الکتریسیته ساکن",
            title = "میدان الکتریکی بار نقطه‌ای",
            formula = "E = k · |q| / r² = F / q₀",
            description = "شدت میدان الکتریکی ایجاد شده توسط بار q در فاصله r.",
            variables = listOf(
                VariableItem("E", "شدت میدان الکتریکی", "N/C یا V/m"),
                VariableItem("q", "بار چشمه میدان", "C"),
                VariableItem("r", "فاصله از بار", "m")
            ),
            keyTip = "جهت میدان الکتریکی از بار مثبت به سمت خارج و به سوی بار منفی به سمت داخل است."
        ),
        FormulaItem(
            id = "g11_ohm",
            grade = "پایه یازدهم",
            category = "الکتریسیته جاری",
            title = "قانون اهم",
            formula = "V = I · R",
            description = "رابطه بین اختلاف پتانسیل دو سر رسانا، شدت جریان عبوری و مقاومت الکتریکی.",
            variables = listOf(
                VariableItem("V", "اختلاف پتانسیل الکتریکی", "V (ولت)"),
                VariableItem("I", "شدت جریان الکتریکی", "A (آمپر)"),
                VariableItem("R", "مقاومت اهمی", "Ω (اهم)")
            ),
            keyTip = "توان الکتریکی مصرفی در مقاومت: P = V · I = I² · R = V² / R (بر حسب وات)."
        ),
        FormulaItem(
            id = "g11_resistors",
            grade = "پایه یازدهم",
            category = "الکتریسیته جاری",
            title = "مقاومت‌های متوالی و موازی",
            formula = "متوالی: R_eq = R₁ + R₂ | موازی: 1/R_eq = 1/R₁ + 1/R₂",
            description = "محاسبه مقاومت معادل مدارهای ترکیبی متوالی و موازی.",
            variables = listOf(
                VariableItem("R_eq", "مقاومت معادل کل", "Ω"),
                VariableItem("R₁, R₂", "مقاومت‌های تکی مدار", "Ω")
            ),
            keyTip = "در بستن متوالی، جریان عبوری از مقاومت‌ها یکسان است. در بستن موازی، اختلاف پتانسیل دو سر مقاومت‌ها برابر است."
        ),
        FormulaItem(
            id = "g11_lorentz",
            grade = "پایه یازدهم",
            category = "مغناطیس",
            title = "نیروی مغناطیسی وارد بر بار متحرک",
            formula = "F = |q| · v · B · sin(θ)",
            description = "نیروی وارد بر بار متحرک با تندی v در میدان مغناطیسی B.",
            variables = listOf(
                VariableItem("F", "نیروی مغناطیسی (لورنتس)", "N"),
                VariableItem("q", "بار الکتریکی", "C"),
                VariableItem("v", "تندی بار", "m/s"),
                VariableItem("B", "میدان مغناطیسی", "T (تسلا)"),
                VariableItem("θ", "زاویه بین بردار سرعت و میدان", "درجه")
            ),
            keyTip = "جهت نیرو از قاعده دست راست به دست می‌آید. اگر بار در جهت میدان حرکت کند (θ = 0)، نیروی مغناطیسی صفر است."
        ),

        // پایه دوازدهم (Grade 12)
        FormulaItem(
            id = "g12_kinematics_velocity",
            grade = "پایه دوازدهم",
            category = "حرکت‌شناسی",
            title = "معادله سرعت - زمان (حرکت شتاب ثابت)",
            formula = "v = a · t + v₀",
            description = "محاسبه سرعت نهایی متحرک با شتاب ثابت a در زمان t.",
            variables = listOf(
                VariableItem("v", "سرعت ثانویه", "m/s"),
                VariableItem("v₀", "سرعت اولیه", "m/s"),
                VariableItem("a", "شتاب ثابت", "m/s²"),
                VariableItem("t", "مدت زمان حرکت", "s")
            ),
            keyTip = "شیب نمودار سرعت - زمان بیانگر شتاب لحظه‌ای است. مساحت زیر نمودار سرعت - زمان برابر جابه‌جایی (Δx) است."
        ),
        FormulaItem(
            id = "g12_kinematics_position",
            grade = "پایه دوازدهم",
            category = "حرکت‌شناسی",
            title = "معادله مکان - زمان (حرکت شتاب ثابت)",
            formula = "Δx = ½ a · t² + v₀ · t",
            description = "محاسبه جابه‌جایی متحرک با شتاب ثابت طی زمان t.",
            variables = listOf(
                VariableItem("Δx", "جابه‌جایی متحرک", "m"),
                VariableItem("a", "شتاب ثابت", "m/s²"),
                VariableItem("v₀", "سرعت اولیه", "m/s"),
                VariableItem("t", "زمان سپری شده", "s")
            ),
            keyTip = "معادله مستقل از زمان: v² - v₀² = 2 · a · Δx. زمانی که زمان در مسئله معلوم نیست از این رابطه استفاده کنید."
        ),
        FormulaItem(
            id = "g12_newton_2",
            grade = "پایه دوازدهم",
            category = "دینامیک",
            title = "قانون دوم نیوتن",
            formula = "ΣF = m · a",
            description = "برآیند نیروهای وارد بر جسم متناسب با جرم و شتاب وارده است.",
            variables = listOf(
                VariableItem("ΣF", "نیروی برآیند خالص", "N"),
                VariableItem("m", "جرم جسم", "kg"),
                VariableItem("a", "بردار شتاب", "m/s²")
            ),
            keyTip = "اصطکاک جنبشی: f_k = μ_k · F_N. جهت اصطکاک همیشه در خلاف جهت لغزش یا تمایل به لغزش سطوح است."
        ),
        FormulaItem(
            id = "g12_pendulum_period",
            grade = "پایه دوازدهم",
            category = "نوسان و امواج",
            title = "دوره تناوب آونگ ساده",
            formula = "T = 2π · √(L / g)",
            description = "دوره تناوب نوسان‌های کوچک یک آونگ ساده به طول ریسمان L.",
            variables = listOf(
                VariableItem("T", "دوره تناوب نوسان", "s (ثانیه)"),
                VariableItem("L", "طول نخ آونگ", "m (متر)"),
                VariableItem("g", "شتاب گرانش محل", "m/s²")
            ),
            keyTip = "دوره تناوب آونگ ساده در نوسان‌های کوچک به جرم گلوله آونگ بستگی ندارد! فقط به طول نخ و شتاب گرانش وابسته است."
        ),
        FormulaItem(
            id = "g12_wave_speed",
            grade = "پایه دوازدهم",
            category = "نوسان و امواج",
            title = "رابطه بنیادین موج",
            formula = "v = λ · f = λ / T",
            description = "تندی انتشار موج متناسب با طول موج و بسامد آن است.",
            variables = listOf(
                VariableItem("v", "تندی انتشار موج", "m/s"),
                VariableItem("λ", "طول موج", "m"),
                VariableItem("f", "بسامد (فرکانس)", "Hz (هرتز)"),
                VariableItem("T", "دوره تناوب موج", "s")
            ),
            keyTip = "بسامد موج فقط به چشمه موج بستگی دارد و با تغییر محیط انتشار تغییر نمی‌کند. تندی موج فقط به جنس و ویژگی محیط وابسته است."
        )
    )

    val quizQuestions = listOf(
        QuizQuestion(
            id = 1,
            grade = "پایه دوازدهم",
            topic = "حرکت‌شناسی",
            question = "اتومبیلی با سرعت اولیه ۱۰ متر بر ثانیه و شتاب ثابت ۲ متر بر مربع ثانیه حرکت می‌کند. جابه‌جایی آن پس از ۴ ثانیه چند متر است؟",
            options = listOf("۴۰ متر", "۵۶ متر", "۲۴ متر", "۴۸ متر"),
            correctIndex = 1,
            explanation = "با استفاده از رابطه مکان - زمان: Δx = v₀t + ½at² = (۱۰ × ۴) + ½ × ۲ × (۴)² = ۴۰ + ۱۶ = ۵۶ متر."
        ),
        QuizQuestion(
            id = 2,
            grade = "پایه دوازدهم",
            topic = "نوسان",
            question = "اگر طول نخ یک آونگ ساده را ۴ برابر کنیم، دوره تناوب نوسان آن چند برابر می‌شود؟",
            options = listOf("۲ برابر", "۴ برابر", "نصف می‌شود", "تغییری نمی‌کند"),
            correctIndex = 0,
            explanation = "رابطه دوره تناوب T = 2π√(L/g) است. چون L درون رادیکال قرار دارد، با ۴ برابر شدن L، مقدار T برابر √۴ یعنی ۲ برابر می‌شود."
        ),
        QuizQuestion(
            id = 3,
            grade = "پایه یازدهم",
            topic = "مدار الکتریکی",
            question = "اگر ولتاژ دو سر یک مقاومت اهمی را ۲ برابر کنیم، توان مصرفی در آن مقاومت چند برابر خواهد شد؟",
            options = listOf("۲ برابر", "۴ برابر", "نصف می‌شود", "ثابت می‌ماند"),
            correctIndex = 1,
            explanation = "با توجه به رابطه توان P = V² / R، چون مقاومت ثابت است، توان با مجذور ولتاژ نسبت مستقیم دارد: با دو برابر شدن ولتاژ، توان ۴ برابر می‌شود."
        ),
        QuizQuestion(
            id = 4,
            grade = "پایه دهم",
            topic = "کار و انرژی",
            question = "توپی به جرم ۲ کیلوگرم با تندی ۶ متر بر ثانیه حرکت می‌کند. انرژی جنبشی آن چند ژول است؟",
            options = listOf("۱۲ ژول", "۳۶ ژول", "۷۲ ژول", "۱۸ ژول"),
            correctIndex = 1,
            explanation = "انرژی جنبشی از رابطه K = ½mv² محاسبه می‌شود: K = ½ × ۲ × (۶)² = ۳۶ ژول."
        ),
        QuizQuestion(
            id = 5,
            grade = "پایه یازدهم",
            topic = "الکتریسیته ساکن",
            question = "اگر فاصله بین دو بار الکتریکی نقطه‌ای را به نصف کاهش دهیم، نیروی دافعه بین آنها چه تغییری می‌کند؟",
            options = listOf("۲ برابر می‌شود", "۴ برابر می‌شود", "نصف می‌شود", "یک چهارم می‌شود"),
            correctIndex = 1,
            explanation = "طبق قانون کولن F = k|q₁q₂| / r²، نیرو با عکس مجذور فاصله رابطه دارد. وقتی فاصله نصف شود (r/2)، نیرو ۴ برابر می‌شود."
        ),
        QuizQuestion(
            id = 6,
            grade = "پایه دهم",
            topic = "شکست نور",
            question = "پرتو نوری از محیطی با ضریب شکست غلیظ‌تر (مثلاً آب n=1.33) به محیط رقیق‌تر (هوا n=1.0) وارد می‌شود. زاویه شکست چه رابطه‌ای با زاویه تابش دارد؟",
            options = listOf("زاویه شکست بزرگتر از زاویه تابش است", "زاویه شکست کوچکتر از زاویه تابش است", "دو زاویه همواره برابرند", "پرتو نور بدون انحراف عبور می‌کند"),
            correctIndex = 0,
            explanation = "طبق قانون اسنل n₁ sin(θ₁) = n₂ sin(θ₂). چون n₁ > n₂ است، باید sin(θ₂) > sin(θ₁) باشد، در نتیجه زاویه شکست θ₂ بزرگتر از زاویه تابش θ₁ بوده و پرتو از خط عمود دور می‌شود."
        ),
        QuizQuestion(
            id = 7,
            grade = "پایه دوازدهم",
            topic = "دینامیک",
            question = "جسمی به جرم ۵ کیلوگرم روی سطح افقی بدون اصطکاک با نیروی افقی ۲۰ نیوتن کشیده می‌شود. شتاب حرکت جسم چند m/s² است؟",
            options = listOf("۱۰۰", "۴", "۰٫۲۵", "۱۵"),
            correctIndex = 1,
            explanation = "طبق قانون دوم نیوتن: a = F / m = ۲۰ / ۵ = ۴ متر بر مربع ثانیه."
        ),
        QuizQuestion(
            id = 8,
            grade = "پایه دوازدهم",
            topic = "امواج",
            question = "موج صوتی با بسامد ۵۰۰ هرتز در هوا با سرعت ۳۴۰ متر بر ثانیه منتشر می‌شود. طول موج این صوت چند سانتی‌متر است؟",
            options = listOf("۶۸ سانتی‌متر", "۱۷ سانتی‌متر", "۱۳۶ سانتی‌متر", "۵۰ سانتی‌متر"),
            correctIndex = 0,
            explanation = "از رابطه v = λ · f داریم: λ = v / f = ۳۴۰ / ۵۰۰ = ۰٫۶۸ متر که معادل ۶۸ سانتی‌متر است."
        )
    )
}
