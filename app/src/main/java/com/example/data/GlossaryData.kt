package com.example.data

object GlossaryData {
    val defaultTerms: List<GlossaryTermEntity> = listOf(
        // ================== پایه دهم ==================
        GlossaryTermEntity(
            id = "work",
            termFa = "کار مکانیکی",
            termEn = "Mechanical Work",
            category = "کار و انرژی",
            grade = "دهم",
            definition = "حاصل‌ضرب داخلی نیروی وارد بر جسم در بردار جابه‌جایی آن؛ نشان‌دهنده مقدار انرژی منتقل‌شده به جسم توسط یک نیرو.",
            formula = "W = F \\cdot d \\cdot \\cos(\\theta)",
            unit = "ژول (J) = N·m",
            iconEmoji = "⚡"
        ),
        GlossaryTermEntity(
            id = "kinetic_energy",
            termFa = "انرژی جنبشی",
            termEn = "Kinetic Energy",
            category = "کار و انرژی",
            grade = "دهم",
            definition = "انرژی که یک جسم به دلیل حرکت و سرعت خود دارد. متناسب با جرم و مجذور تندی جسم است.",
            formula = "K = \\frac{1}{2} m v^2",
            unit = "ژول (J)",
            iconEmoji = "🏃"
        ),
        GlossaryTermEntity(
            id = "potential_energy",
            termFa = "انرژی پتانسیل گرانشی",
            termEn = "Gravitational Potential Energy",
            category = "کار و انرژی",
            grade = "دهم",
            definition = "انرژی ذخیره‌شده در جسم به دلیل موقعیت آن نسبت به سطح مبنای گرانش زمین.",
            formula = "U = m g h",
            unit = "ژول (J)",
            iconEmoji = "🏔️"
        ),
        GlossaryTermEntity(
            id = "mechanical_energy_conservation",
            termFa = "پایستگی انرژی مکانیکی",
            termEn = "Conservation of Mechanical Energy",
            category = "کار و انرژی",
            grade = "دهم",
            definition = "در غیاب نیروهای اتلافی (مانند اصطکاک و مقاومت هوا)، مجموع انرژی جنبشی و پتانسیل یک سامانه مقداری ثابت باقی می‌ماند.",
            formula = "E = K + U = \\text{ثابت}",
            unit = "ژول (J)",
            iconEmoji = "🔄"
        ),
        GlossaryTermEntity(
            id = "power",
            termFa = "توان متوسط و لحظه‌ای",
            termEn = "Power",
            category = "کار و انرژی",
            grade = "دهم",
            definition = "آهنگ انجام کار یا آهنگ مصرف و انتقال انرژی در واحد زمان.",
            formula = "P = \\frac{W}{\\Delta t} = F \\cdot v",
            unit = "وات (W) = J/s",
            iconEmoji = "💡"
        ),
        GlossaryTermEntity(
            id = "density",
            termFa = "چگالی (جرم حجمی)",
            termEn = "Density",
            category = "ویژگی‌های ماده",
            grade = "دهم",
            definition = "نسبت جرم یک ماده به حجم آن؛ معیاری از فشردگی ذرات ماده درون یک فضا.",
            formula = "\\rho = \\frac{m}{V}",
            unit = "kg/m³ یا g/cm³",
            iconEmoji = "⚖️"
        ),
        GlossaryTermEntity(
            id = "pressure",
            termFa = "فشار در شاره‌ها",
            termEn = "Hydrostatic Pressure",
            category = "ویژگی‌های ماده",
            grade = "دهم",
            definition = "اندازه نیروی عمودی وارد بر واحد سطح یک شاره در حالت سکون ناشی از وزن ستون مایع بالای آن.",
            formula = "P = P_0 + \\rho g h",
            unit = "پاسکال (Pa) = N/m²",
            iconEmoji = "🌊"
        ),
        GlossaryTermEntity(
            id = "bernoulli_principle",
            termFa = "اصل برنولی",
            termEn = "Bernoulli's Principle",
            category = "ویژگی‌های ماده",
            grade = "دهم",
            definition = "در یک جریان افقی و پایای شاره تراکم‌ناپذیر، با افزایش تندی شاره، فشار درونی آن کاهش می‌یابد.",
            formula = "P + \\frac{1}{2} \\rho v^2 = \\text{ثابت}",
            unit = "پاسکال (Pa)",
            iconEmoji = "✈️"
        ),
        GlossaryTermEntity(
            id = "heat_capacity",
            termFa = "ظرفیت گرمایی ویژه",
            termEn = "Specific Heat Capacity",
            category = "دما و گرما",
            grade = "دهم",
            definition = "مقدار گرمایی که باید به یک کیلوگرم از یک ماده داده شود تا دمای آن یک کلوین (یا یک درجه سلسیوس) افزایش یابد.",
            formula = "Q = m c \\Delta T",
            unit = "J / (kg·K)",
            iconEmoji = "🔥"
        ),
        GlossaryTermEntity(
            id = "latent_heat",
            termFa = "گرمای نهان تغییر حالت",
            termEn = "Latent Heat",
            category = "دما و گرما",
            grade = "دهم",
            definition = "گرمای مبادله‌شده هنگام تغییر فاز ماده (ذوب، انجماد، تبخیر یا میعان) بدون تغییر در دمای آن.",
            formula = "Q = \\pm m L_f \\text{ یا } m L_v",
            unit = "J/kg",
            iconEmoji = "🧊"
        ),
        GlossaryTermEntity(
            id = "ideal_gas_law",
            termFa = "قانون گازهای کامل",
            termEn = "Ideal Gas Law",
            category = "دما و گرما",
            grade = "دهم",
            definition = "معادله حالتی که رابطه بین فشار، حجم، دما و مقدار مول یک گاز رقیق ایده‌آل را بیان می‌کند.",
            formula = "P V = n R T",
            unit = "فشار (Pa)، حجم (m³)، دما (K)",
            iconEmoji = "🎈"
        ),

        // ================== پایه یازدهم ==================
        GlossaryTermEntity(
            id = "coulomb_law",
            termFa = "قانون کولن",
            termEn = "Coulomb's Law",
            category = "الکتریسیته ساکن",
            grade = "یازدهم",
            definition = "نیروی جاذبه یا دافعه الکتروستاتیکی بین دو بار نقطه‌ای مستقیماً متناسب با حاصل‌ضرب بارها و معکوساً متناسب با مجذور فاصله آنهاست.",
            formula = "F = k \\frac{|q_1 q_2|}{r^2}",
            unit = "نیوتون (N)",
            iconEmoji = "⚡"
        ),
        GlossaryTermEntity(
            id = "electric_field",
            termFa = "میدان الکتریکی",
            termEn = "Electric Field",
            category = "الکتریسیته ساکن",
            grade = "یازدهم",
            definition = "خاصیتی در فضای اطراف یک بار الکتریکی که به هر بار دیگر قرار گرفته در آن نقطه، نیروی الکتریکی وارد می‌کند.",
            formula = "E = \\frac{F}{q_0} = k \\frac{|q|}{r^2}",
            unit = "N/C یا V/m",
            iconEmoji = "🌐"
        ),
        GlossaryTermEntity(
            id = "electric_potential",
            termFa = "پتانسیل الکتریکی",
            termEn = "Electric Potential",
            category = "الکتریسیته ساکن",
            grade = "یازدهم",
            definition = "انرژی پتانسیل الکتریکی بر واحد بار در یک نقطه از میدان الکتریکی؛ مشخص‌کننده جهت جریان بارهای مثبت از پتانسیل بیشتر به کمتر.",
            formula = "V = \\frac{U}{q} = - \\int E \\cdot dr",
            unit = "ولت (V) = J/C",
            iconEmoji = "🔋"
        ),
        GlossaryTermEntity(
            id = "capacitance",
            termFa = "ظرفیت خازن",
            termEn = "Capacitance",
            category = "الکتریسیته ساکن",
            grade = "یازدهم",
            definition = "توانایی یک خازن در ذخیره بار الکتریکی به ازای یک اختلاف پتانسیل مشخص؛ وابسته به ابعاد هندسی صفحات و جنس دی‌الکتریک.",
            formula = "C = \\frac{q}{V} = \\kappa \\varepsilon_0 \\frac{A}{d}",
            unit = "فاراد (F) = C/V",
            iconEmoji = "📦"
        ),
        GlossaryTermEntity(
            id = "electric_current",
            termFa = "جریان الکتریکی",
            termEn = "Electric Current",
            category = "جریان الکتریکی و مدارها",
            grade = "یازدهم",
            definition = "آهنگ عبور بار الکتریکی خالص از سطح مقطع یک رسانا در واحد زمان.",
            formula = "I = \\frac{\\Delta q}{\\Delta t}",
            unit = "آمپر (A) = C/s",
            iconEmoji = "🔌"
        ),
        GlossaryTermEntity(
            id = "ohm_law",
            termFa = "قانون اهم",
            termEn = "Ohm's Law",
            category = "جریان الکتریکی و مدارها",
            grade = "یازدهم",
            definition = "در دمای ثابت، نسبت اختلاف پتانسیل دو سر یک رسانای اهمی به شدت جریانی که از آن می‌گذرد مقداری ثابت به نام مقاومت الکتریکی است.",
            formula = "V = I R",
            unit = "اهم (\\Omega) = V/A",
            iconEmoji = "📏"
        ),
        GlossaryTermEntity(
            id = "resistivity",
            termFa = "مقاومت ویژه الکتریکی",
            termEn = "Electrical Resistivity",
            category = "جریان الکتریکی و مدارها",
            grade = "یازدهم",
            definition = "خاصیت ذاتی جنس ماده در مقابله با عبور جریان الکتریکی، وابسته به دما و ساختار میکروسکوپی اتم‌های رسانا.",
            formula = "R = \\rho \\frac{L}{A}",
            unit = "اهم-متر (\\Omega·m)",
            iconEmoji = "🧱"
        ),
        GlossaryTermEntity(
            id = "lorentz_force",
            termFa = "نیروی مغناطیسی وارد بر بار (نیروی لورنتس)",
            termEn = "Magnetic Force on Moving Charge",
            category = "مغناطیس و القا",
            grade = "یازدهم",
            definition = "نیرویی که از طرف یک میدان مغناطیسی یکنواخت به یک ذره باردار متحرک وارد می‌شود و بر بردار سرعت و میدان عمود است.",
            formula = "F = |q| v B \\sin(\\theta)",
            unit = "نیوتون (N)",
            iconEmoji = "🧲"
        ),
        GlossaryTermEntity(
            id = "magnetic_field",
            termFa = "میدان مغناطیسی (چگالی شار)",
            termEn = "Magnetic Field",
            category = "مغناطیس و القا",
            grade = "یازدهم",
            definition = "میدانی برداری که توسط بارهای الکتریکی متحرک یا مواد مغناطیسی ایجاد شده و بر سایر بارهای متحرک نیرو وارد می‌کند.",
            formula = "B = \\frac{\\mu_0 I}{2 \\pi r} \\text{ (سیم راست)}",
            unit = "تسلا (T) = N/(A·m)",
            iconEmoji = "🧭"
        ),
        GlossaryTermEntity(
            id = "magnetic_flux",
            termFa = "شار مغناطیسی",
            termEn = "Magnetic Flux",
            category = "مغناطیس و القا",
            grade = "یازدهم",
            definition = "تعداد خطوط میدان مغناطیسی گذرنده از یک سطح معین؛ حاصل‌ضرب اندازه میدان در مساحت و کسینوس زاویه خط عمود بر سطح.",
            formula = "\\Phi = B A \\cos(\\theta)",
            unit = "وبر (Wb) = T·m²",
            iconEmoji = "📐"
        ),
        GlossaryTermEntity(
            id = "faraday_induction_law",
            termFa = "قانون القای فارادی",
            termEn = "Faraday's Law of Induction",
            category = "مغناطیس و القا",
            grade = "یازدهم",
            definition = "نیروی محرکه الکتریکی القا شده در یک پیچه مستقیماً متناسب با منفی آهنگ تغییرات شار مغناطیسی گذرنده از آن پیچه است.",
            formula = "\\varepsilon = - N \\frac{\\Delta \\Phi}{\\Delta t}",
            unit = "ولت (V)",
            iconEmoji = "⚡"
        ),
        GlossaryTermEntity(
            id = "lenz_law",
            termFa = "قانون لنز",
            termEn = "Lenz's Law",
            category = "مغناطیس و القا",
            grade = "یازدهم",
            definition = "جهت جریان القایی در یک مدار بسته همواره به‌گونه‌ای است که میدان مغناطیسی ناشی از آن با عامل تغییردهنده شار مغناطیسی اولیه مخالفت کند.",
            formula = "\\text{علامت منفی در رابطه فارادی: } -\\frac{d\\Phi}{dt}",
            unit = "قانون جهت‌گیری",
            iconEmoji = "🛑"
        ),

        // ================== پایه دوازدهم ==================
        GlossaryTermEntity(
            id = "acceleration",
            termFa = "شتاب لحظه‌ای و متوسط",
            termEn = "Acceleration",
            category = "سینماتیک و حرکت‌شناسی",
            grade = "دوازدهم",
            definition = "آهنگ تغییرات بردار سرعت متحرک در واحد زمان. تغییر جهت یا اندازه سرعت سبب ایجاد شتاب می‌شود.",
            formula = "a = \\frac{\\Delta v}{\\Delta t} = \\frac{dv}{dt}",
            unit = "متر بر مجذور ثانیه (m/s²)",
            iconEmoji = "🚀"
        ),
        GlossaryTermEntity(
            id = "newton_second_law",
            termFa = "قانون دوم نیوتون",
            termEn = "Newton's Second Law",
            category = "دینامیک",
            grade = "دوازدهم",
            definition = "برآیند نیروهای وارد بر جسم برابر با حاصل‌ضرب جرم جسم در شتاب آن است و شتاب هم‌جهت با برآیند نیروها است.",
            formula = "\\sum F = m a = \\frac{\\Delta p}{\\Delta t}",
            unit = "نیوتون (N) = kg·m/s²",
            iconEmoji = "⚙️"
        ),
        GlossaryTermEntity(
            id = "momentum",
            termFa = "تکانه (اندازه حرکت)",
            termEn = "Linear Momentum",
            category = "دینامیک",
            grade = "دوازدهم",
            definition = "کمیتی برداری برابر با حاصل‌ضرب جرم جسم در سرعت آن؛ معیاری از دشواری متوقف کردن یک جسم متحرک.",
            formula = "p = m v",
            unit = "کیلوگرم متر بر ثانیه (kg·m/s)",
            iconEmoji = "🎯"
        ),
        GlossaryTermEntity(
            id = "centripetal_acceleration",
            termFa = "شتاب و نیروی مرکزگرا",
            termEn = "Centripetal Acceleration & Force",
            category = "دینامیک",
            grade = "دوازدهم",
            definition = "شتابی که در حرکت دایره‌ای یکنواخت به سوی مرکز دایره جهت‌گیری دارد و مسئول تغییر جهت بردار سرعت است نه اندازه آن.",
            formula = "a_c = \\frac{v^2}{r} = r \\omega^2",
            unit = "m/s²",
            iconEmoji = "🎡"
        ),
        GlossaryTermEntity(
            id = "hooke_law",
            termFa = "قانون هوک",
            termEn = "Hooke's Law",
            category = "نوسان و امواج",
            grade = "دوازدهم",
            definition = "نیروی کشسانی فنر با تغییر طول فنر نسبت مستقیم و در خلاف جهت تغییر طول (نیروی بازگرداننده) است.",
            formula = "F_s = - k x",
            unit = "نیوتون (N)، ثابت فنر (N/m)",
            iconEmoji = "🌀"
        ),
        GlossaryTermEntity(
            id = "simple_harmonic_motion",
            termFa = "حرکت هماهنگ ساده (SHM)",
            termEn = "Simple Harmonic Motion",
            category = "نوسان و امواج",
            grade = "دوازدهم",
            definition = "حرکت تناوبی که در آن نیروی بازگرداننده متناسب با جابه‌جایی و در خلاف جهت آن است؛ معادله مکان-زمان آن به صورت سینوسی یا کسینوسی بیان می‌شود.",
            formula = "x(t) = A \\cos(\\omega t + \\phi_0)",
            unit = "متر (m)",
            iconEmoji = "〰️"
        ),
        GlossaryTermEntity(
            id = "wave_velocity",
            termFa = "تندی انتشار موج",
            termEn = "Wave Propagation Speed",
            category = "نوسان و امواج",
            grade = "دوازدهم",
            definition = "تندی انتشار آشفتگی در یک محیط؛ به ویژگی‌های کشسانی و لختی محیط بستگی دارد و برابر با حاصل‌ضرب طول موج در بسامد است.",
            formula = "v = \\lambda f = \\frac{\\lambda}{T}",
            unit = "متر بر ثانیه (m/s)",
            iconEmoji = "🌊"
        ),
        GlossaryTermEntity(
            id = "doppler_effect",
            termFa = "اثر دوپلر",
            termEn = "Doppler Effect",
            category = "نوسان و امواج",
            grade = "دوازدهم",
            definition = "تغییر ظاهری در بسامد دریافتی موج به دلیل حرکت نسبی بین چشمه موج و شنونده یا ناظر.",
            formula = "f' = f \\left( \\frac{v \\pm v_o}{v \\mp v_s} \\right)",
            unit = "هرتز (Hz)",
            iconEmoji = "🚑"
        ),
        GlossaryTermEntity(
            id = "snell_law",
            termFa = "قانون شکست نور (اسنل)",
            termEn = "Snell's Law of Refraction",
            category = "برهم‌کنش‌های موج",
            grade = "دوازدهم",
            definition = "رابطه بین زاویه تابش و زاویه شکست هنگام عبور نور از یک محیط شفاف به محیط شفاف دیگر با ضریب شکست متفاوت.",
            formula = "n_1 \\sin(\\theta_1) = n_2 \\sin(\\theta_2)",
            unit = "بدون یکا",
            iconEmoji = "💎"
        ),
        GlossaryTermEntity(
            id = "photon_energy",
            termFa = "انرژی فوتون (پلانک)",
            termEn = "Photon Energy",
            category = "فیزیک اتمی",
            grade = "دوازدهم",
            definition = "بر اساس نظریه کوانتومی، نور از بسته‌های گسسته انرژی به نام فوتون تشکیل شده که انرژی هر فوتون متناسب با بسامد تابش است.",
            formula = "E = h f = \\frac{h c}{\\lambda}",
            unit = "ژول (J) یا الکترون‌ولت (eV)",
            iconEmoji = "☀️"
        ),
        GlossaryTermEntity(
            id = "photoelectric_effect",
            termFa = "پدیده فوتوالکتریک",
            termEn = "Photoelectric Effect",
            category = "فیزیک اتمی",
            grade = "دوازدهم",
            definition = "کنده شدن الکترون‌ها از سطح یک فلز در اثر تابش نور با بسامد بالاتر از بسامد آستانه؛ اثباتی بر ماهیت ذره‌ای نور که اینشتین به خاطر آن نوبل گرفت.",
            formula = "K_{\\max} = h f - W_0",
            unit = "ژول (J) یا eV",
            iconEmoji = "🔬"
        ),
        GlossaryTermEntity(
            id = "bohr_model",
            termFa = "مدل اتمی رادرفورد - بور",
            termEn = "Bohr Atomic Model",
            category = "فیزیک اتمی",
            grade = "دوازدهم",
            definition = "مدلی که در آن الکترون‌ها فقط در مدارهای معین و پایدار با اندازه حرکت زاویه‌ای کوانتیده حرکت می‌کنند و هنگام جهش بین ترازها فوتون گسیل یا جذب می‌نمایند.",
            formula = "E_n = -\\frac{13.6\\text{ eV}}{n^2}",
            unit = "الکترون‌ولت (eV)",
            iconEmoji = "⚛️"
        ),
        GlossaryTermEntity(
            id = "nuclear_binding_energy",
            termFa = "انرژی بستگی هسته",
            termEn = "Nuclear Binding Energy",
            category = "فیزیک هسته‌ای",
            grade = "دوازدهم",
            definition = "انرژی مورد نیاز برای تجزیه کامل یک هسته به نوکلئون‌های سازنده آن (پروتون‌ها و نوترون‌ها) که ناشی از کاستی جرم هسته است.",
            formula = "E_b = \\Delta m \\cdot c^2",
            unit = "مگا الکترون‌ولت (MeV)",
            iconEmoji = "☢️"
        ),
        GlossaryTermEntity(
            id = "radioactive_half_life",
            termFa = "نیمه‌عمر پرتوزایی",
            termEn = "Radioactive Half-Life",
            category = "فیزیک هسته‌ای",
            grade = "دوازدهم",
            definition = "مدت زمانی که طول می‌کشد تا نیمی از هسته‌های پرتوزای اولیه یک نمونه در اثر واپاشی هسته‌ای تجزیه شوند.",
            formula = "N(t) = N_0 \\left(\\frac{1}{2}\\right)^{\\frac{t}{T_{1/2}}}",
            unit = "ثانیه (s) یا سال",
            iconEmoji = "⏳"
        )
    )
}
