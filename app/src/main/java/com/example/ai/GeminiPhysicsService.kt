package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.GlossaryTermEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiPhysicsService {
    private const val TAG = "GeminiPhysicsService"
    const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
تو «برسام فیزیک» هستی؛ یک دستیار هوشمند، فوق‌العاده بااستعداد، دلسوز و تخصصی فیزیک دبیرستان (پایه‌های دهم، یازدهم و دوازدهم نظام آموزشی ایران).
اهداف تو:
۱. تفهیم عمیق و مفهومی اصطلاحات و پدیده‌های فیزیک با زبانی شیوا، روان و جذاب به زبان فارسی.
۲. ارائه مثال‌های شهودی و ملموس از زندگی روزمره و طبیعت برای درک بهتر پدیده.
۳. بیان مفهوم فیزیکی فرمول‌ها (چرا متغیرها در صورت یا مخرج هستند) به جای حفظ کردن محض.
۴. ذکر نکات پرتکرار امتحانات نهایی و تست‌های کنکور سراسری پیرامون آن مفهوم.
پاسخ‌هایت باید ساختاریافته، با لحنی صمیمی و انگیزه‌بخش و دارای بخش‌بندی‌های منظم باشد.
"""

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && key != "none"
    }

    suspend fun explainTermConceptually(term: GlossaryTermEntity): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "none") {
            // Return high quality curriculum breakdown with guidance on AI Studio Secrets
            return@withContext Result.success(getCuratedInsightFallback(term))
        }

        try {
            val userPrompt = """
لطفاً مفهوم فیزیکی «${term.termFa}» (${term.termEn}) مربوط به مبحث ${term.category} پایه ${term.grade} را با جزییات زیر تحلیل و تشریح کن:
۱. مفهوم شهودی و تصویر ذهنی (در دنیای واقعی چه اتفاقی می‌افتد؟)
۲. تحلیل فیزیکی فرمول (${term.formula ?: "روابط مربوطه"}) و یکای آن (${term.unit ?: "یکای SI"})
۳. مثال کاربردی و جذاب از زندگی روزمره یا صنعت
۴. دام‌های آموزشی و نکات تستی ویژه امتحانات نهایی و کنکور
پاسخ تمیز و با عنوان‌های مشخص باشد.
""".trimIndent()

            val responseText = callGeminiApi(userPrompt, apiKey)
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content with Gemini 3.5 Flash", e)
            Result.success(getCuratedInsightFallback(term) + "\n\n*(توجه: ارتباط اینترنتی برقرار نشد؛ از تحلیل مفهومی پیش‌فرض کتب درسی استفاده شد)*")
        }
    }

    suspend fun askPhysicsQuestion(question: String, relatedTerm: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "none") {
            return@withContext Result.failure(
                IllegalStateException("کلید هوش مصنوعی در پانل تنظیمات (Secrets panel) ثبت نشده است. لطفاً کلید GEMINI_API_KEY را در تنظیمات وارد نمایید.")
            )
        }

        try {
            val prompt = if (relatedTerm != null) {
                "در مورد مبحث «$relatedTerm»: $question"
            } else {
                question
            }
            val responseText = callGeminiApi(prompt, apiKey)
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error asking Gemini 3.5 Flash", e)
            Result.failure(e)
        }
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String {
        val url = "$BASE_URL?key=$apiKey"

        val jsonBody = JSONObject().apply {
            val contentsArr = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            }
            put("contents", contentsArr)

            val systemInstructionObj = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", SYSTEM_PROMPT))
                })
            }
            put("systemInstruction", systemInstructionObj)

            val genConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
            }
            put("generationConfig", genConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            throw IllegalStateException("خطا در پاسخ هوش مصنوعی (کد ${response.code}): $errBody")
        }

        val respString = response.body?.string() ?: throw IllegalStateException("پاسخ دریافتی خالی است")
        val respJson = JSONObject(respString)
        val candidates = respJson.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "")
            }
        }
        throw IllegalStateException("قالب پاسخ Gemini معتبر نبود")
    }

    private fun getCuratedInsightFallback(term: GlossaryTermEntity): String {
        return """
✨ **تحلیل مفهومی برسام فیزیک (${term.termFa})**:

📌 **تصویر ذهنی و مفهوم شهودی:**
مفهوم «${term.termFa}» یکی از ارکان سرفصل‌های ${term.category} در پایه ${term.grade} دبیرستان است. ${term.definition}

🔍 **تحلیل ریاضی و فیزیکی:**
${if (!term.formula.isNullOrBlank()) "• رابطه کلیدی: ${term.formula}\n" else ""}${if (!term.unit.isNullOrBlank()) "• یکای اندازه‌گیری استاندارد در سامانه بین‌المللی: ${term.unit}\n" else ""}در حل مسائل، همواره به تبدیل یکاها (مثلاً کیلومتر بر ساعت به متر بر ثانیه، یا گرم بر سانتی‌متر مکعب به کیلوگرم بر متر مکعب) دقت مضاعف داشته باشید.

💡 **نکته کلیدی شب امتحان و کنکور:**
در این مبحث طراحان آزمون‌های نهایی اغلب روی جهت بردارها، شرایط پایستگی و ثابت بودن یا متغیر بودن کمیت‌ها تمرکز می‌کنند.

*(برای دریافت پاسخ‌های زنده، تحلیلی و تعاملی از مدل پیشرفته Gemini 3.5 Flash، کافیست کلید GEMINI_API_KEY را در بخش Secrets panel استودیو هوش مصنوعی فعال کنید).*
""".trimIndent()
    }
}
