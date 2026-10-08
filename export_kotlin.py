# -*- coding: utf-8 -*-
import generate_dataset

qs = generate_dataset.questions

with open('app/src/main/java/com/example/data/PhysicsData.kt', 'r', encoding='utf-8') as f:
    orig = f.read()

# We need to preserve everything in PhysicsData before `val quizQuestions = listOf(`
prefix = orig.split("    val quizQuestions = listOf(")[0]

kotlin_lines = []
kotlin_lines.append(prefix + "    val quizQuestions = listOf(\n")

for i, q in enumerate(qs):
    opt_str = ", ".join(f'"{opt}"' for opt in q["options"])
    # escape explanation
    expl = q["explanation"].replace('"""', '\"\"\"')
    level_comment = f"        // --- سطح {q['difficulty']} | {q['grade']} | سوال {q['id']} ---"
    kotlin_lines.append(level_comment + "\n")
    kotlin_lines.append(f"""        QuizQuestion(
            id = {q['id']},
            grade = "{q['grade']}",
            topic = "{q['topic']}",
            difficulty = {q['difficulty']},
            question = "{q['question']}",
            options = listOf({opt_str}),
            correctIndex = {q['correctIndex']},
            explanation = \"\"\"{expl}\"\"\"
        )""")
    if i < len(qs) - 1:
        kotlin_lines.append(",\n")
    else:
        kotlin_lines.append("\n")

kotlin_lines.append("""    )

    /**
     * بررسی تعادل توزیع گزینه‌های درست در بانک سوالات خام.
     * اگر سهم هر جایگاه (۰ تا ۳) بیشتر از ۴۰٪ باشد، هشدار صادر می‌کند.
     */
    fun validateAnswerDistribution(questions: List<QuizQuestion> = quizQuestions): Map<Int, Float> {
        val counts = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)
        questions.forEach { q ->
            counts[q.correctIndex] = (counts[q.correctIndex] ?: 0) + 1
        }
        val total = questions.size.toFloat()
        val percentages = counts.mapValues { (_, count) -> (count / total) * 100f }
        percentages.forEach { (pos, pct) ->
            if (pct > 40f) {
                android.util.Log.w("QuizValidator", "هشدار: سهم جایگاه $pos برابر $pct٪ و بیشتر از حد مجاز ۴۰٪ است!")
            }
        }
        return percentages
    }
}
""")

with open('app/src/main/java/com/example/data/PhysicsData.kt', 'w', encoding='utf-8') as f:
    f.write("".join(kotlin_lines))

print("PhysicsData.kt successfully updated with 120 questions!")
