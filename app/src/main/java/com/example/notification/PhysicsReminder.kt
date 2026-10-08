package com.example.notification

data class PhysicsReminder(
    val id: Int,
    val title: String,
    val formulaOrProblem: String,
    val explanation: String,
    val category: String // فرمول‌نامه، چالش مسئله، نکته کنکور
)

object PhysicsReminderData {
    val dailyReminders = listOf(
        PhysicsReminder(
            id = 1,
            title = "⚛️ یادآوری فرمول‌نامه: رابطه مستقل از زمان",
            formulaOrProblem = "v² - v₀² = 2aΔx",
            explanation = "در حرکت با شتاب ثابت هرگاه زمان (t) در مسئله داده نشده باشد، از این رابطه طلایی استفاده کنید!",
            category = "حرکت‌شناسی دوازدهم"
        ),
        PhysicsReminder(
            id = 2,
            title = "💡 نکته روز مدار: قانون اهم و توان مصرفی",
            formulaOrProblem = "V = IR  |  P = V²/R = RI²",
            explanation = "با دو برابر شدن ولتاژ دو سر یک مقاومت اهمی ثابت، توان مصرفی آن ۴ برابر می‌شود!",
            category = "الکتریسیته جاری یازدهم"
        ),
        PhysicsReminder(
            id = 3,
            title = "🎯 چالش حل مسئله: زاویه پرتاب برای بیشترین برد",
            formulaOrProblem = "R = (v₀² sin 2θ) / g",
            explanation = "در سطح افقی بیشترین برد زمانی رخ می‌دهد که sin(2θ)=1 باشد؛ یعنی زاویه پرتاب باید ۴۵ درجه باشد.",
            category = "حرکت پرتابه‌ای دوازدهم"
        ),
        PhysicsReminder(
            id = 4,
            title = "🌈 یادآوری اپتیک: قانون اسنل و زاویه حد",
            formulaOrProblem = "n₁ sin θ₁ = n₂ sin θ₂  |  sin θ_c = n₂ / n₁",
            explanation = "پدیده بازتاب کلی فقط زمانی رخ می‌دهد که نور از محیط غلیظ‌تر (n بیشتر) وارد محیط رقیق‌تر (n کمتر) شود.",
            category = "نوسان و امواج دوازدهم"
        ),
        PhysicsReminder(
            id = 5,
            title = "⚖️ فرمول‌نامه پویا: نیروی اصطکاک جنبشی",
            formulaOrProblem = "f_k = μ_k · F_N",
            explanation = "نیروی اصطکاک جنبشی به مساحت سطح تماس بستگی ندارد و تنها به جنس سطوح (μ_k) و نیروی عمودی تکیه‌گاه وابسته است.",
            category = "دینامیک دوازدهم"
        ),
        PhysicsReminder(
            id = 6,
            title = "⏱️ پایستگی انرژی: نوسان آونگ ساده",
            formulaOrProblem = "E = K + U = ½mv² + mgh = ثابت",
            explanation = "در بیشترین انحراف، سرعت صفر و کل انرژی به شکل پتانسیل گرانشی است. در نقطه تعادل، سرعت و انرژی جنبشی بیشینه است.",
            category = "نوسان و انرژی دوازدهم"
        ),
        PhysicsReminder(
            id = 7,
            title = "⚡ مرور فرمول‌نامه: ظرفیت و انرژی خازن",
            formulaOrProblem = "C = κε₀(A/d)  |  U = ½CV² = ½Q²/C",
            explanation = "اگر دی‌الکتریکی با ضریب κ بین صفحات خازن قرار گیرد، ظرفیت آن κ برابر افزایش می‌یابد.",
            category = "الکتریسیته ساکن یازدهم"
        ),
        PhysicsReminder(
            id = 8,
            title = "🌊 چالش روز امواج: سرعت انتشار موج در طناب",
            formulaOrProblem = "v = √(F / μ)  که  μ = m / L",
            explanation = "با چهار برابر شدن کشش طناب (F)، تندی انتشار موج در طناب دو برابر می‌شود.",
            category = "موج و صوت دوازدهم"
        ),
        PhysicsReminder(
            id = 9,
            title = "🌡️ ترمودینامیک: معادله گازهای آرمانی",
            formulaOrProblem = "PV = nRT  |  (P₁V₁)/T₁ = (P₂V₂)/T₂",
            explanation = "توجه: در تمام فرمول‌های گاز آرمانی، دما (T) باید حتماً بر حسب کلوین (K = °C + 273) جایگذاری شود!",
            category = "ویژگی‌های ماده و دما دهم"
        ),
        PhysicsReminder(
            id = 10,
            title = "🧲 مغناطیس: نیروی لورنتس وارد بر ذره باردار",
            formulaOrProblem = "F_B = |q| v B sin θ",
            explanation = "اگر ذره باردار موازی خطوط میدان مغناطیسی حرکت کند (θ = 0 یا 180)، هیچ نیروی مغناطیسی به آن وارد نمی‌شود.",
            category = "مغناطیس یازدهم"
        ),
        PhysicsReminder(
            id = 11,
            title = "🚀 کار و انرژی: قضیه کار و انرژی جنبشی",
            formulaOrProblem = "W_total = ΔK = ½mv₂² - ½mv₁²",
            explanation = "کار کل انجام‌شده روی یک جسم برابر است با تغییر انرژی جنبشی آن، فارغ از نوع نیروهای وارد بر جسم.",
            category = "کار و انرژی دهم"
        ),
        PhysicsReminder(
            id = 12,
            title = "🔬 فیزیک اتمی: انرژی فوتون و فرکانس",
            formulaOrProblem = "E = hf = hc / λ",
            explanation = "انرژی هر کوانتوم نور با فرکانس آن رابطه مستقیم و با طول‌موجش در خلاء رابطه معکوس دارد.",
            category = "فیزیک اتمی دوازدهم"
        )
    )

    fun getRandomReminder(): PhysicsReminder {
        return dailyReminders.random()
    }

    fun getReminderForDay(dayOfYear: Int): PhysicsReminder {
        val index = (dayOfYear % dailyReminders.size).coerceIn(0, dailyReminders.size - 1)
        return dailyReminders[index]
    }
}
