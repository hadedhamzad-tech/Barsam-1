package com.example.model

data class VariableItem(
    val symbol: String,
    val name: String,
    val unit: String
)

data class FormulaItem(
    val id: String,
    val grade: String, // پایه دهم، پایه یازدهم، پایه دوازدهم
    val category: String,
    val title: String,
    val formula: String,
    val description: String,
    val variables: List<VariableItem>,
    val keyTip: String
)

data class SolverStep(
    val stepNumber: Int,
    val title: String,
    val explanation: String,
    val formula: String,
    val calculation: String
)

data class SolverResult(
    val title: String,
    val category: String,
    val inputSummary: String,
    val resultSummary: String,
    val steps: List<SolverStep>
)

data class QuizQuestion(
    val id: Int,
    val grade: String,
    val topic: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

enum class SimulationType(
    val title: String,
    val subtitle: String,
    val gradeTag: String
) {
    PROJECTILE(
        "حرکت پرتابه‌ای و سقوط آزاد",
        "مسیر سهمی، برد، اوج و زمان پرواز در کرات مختلف",
        "پایه دوازدهم"
    ),
    PENDULUM(
        "آونگ ساده و تبدیل انرژی",
        "نوسان هماهنگ، دوره تناوب و پایستگی انرژی جنبشی و پتانسیل",
        "پایه دوازدهم"
    ),
    OPTICS(
        "شکست نور و قانون اسنل",
        "انعکاس، ضریب شکست، زاویه حد و بازتاب کلی",
        "پایه دهم"
    ),
    CIRCUIT(
        "مدار الکتریکی و قانون اهم",
        "بررسی ولتاژ، مقاومت، شدت جریان و توان لامپ",
        "پایه یازدهم"
    ),
    NEWTON(
        "قوانین نیوتن و اصطکاک",
        "نیروهای محرک، اصطکاک ایستایی و جنبشی، نمودار نیرو و شتاب",
        "پایه دوازدهم"
    )
}
