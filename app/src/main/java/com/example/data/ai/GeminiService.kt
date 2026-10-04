package com.example.data.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(val text: String) : GeminiResult()
    data class QuizSuccess(val quiz: GeneratedQuiz) : GeminiResult()
    data class Error(val message: String, val isNetworkError: Boolean = false) : GeminiResult()
}

data class QuizQuestion(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class GeneratedQuiz(
    val title: String,
    val subject: String,
    val questions: List<QuizQuestion>
)

class GeminiService {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemInstruction = """
        أنت «مساعد الطالب الذكي»، رفيق دراسي وتربوي شخصي مصمم لتمكين الطالب العربي من الفهم العميق وبناء مهارات التفكير المستقل.
        
        قواعدك الصارمة:
        1. لست روبوت دردشة عامًا؛ تركيزك تعليمي وأكاديمي وتربوي حصريًا.
        2. عند حل أو شرح مسألة أو سؤال دراسي، لا تسرد الإجابة المباشرة دفعة واحدة إلا إذا طلب الطالب «الإجابة فقط». اتبع المنهجية التربوية التالية:
           - فهم المطلوب (ما الذي نبحث عنه؟)
           - تحديد المعطيات الأساسية
           - المفهوم أو القانون العلمي المستخدم
           - الحل المنظم خطوة بخطوة
           - النتيجة النهائية واضحة ومميزة
           - تحقق سريع أو تلميح للتأكد من الحل
           - تمرين مشابه اختياري لتثبيت المعلومة
        3. تحدث بلغة عربية فصحى مشجعة، واضحة، وعصرية بدون تعقيد.
        4. لا تختلق معلومات أو قوانين أو مصادر على الإطلاق.
        5. إذا كانت صورة السؤال المرفق غير واضحة، قل فورًا: «عذرًا، الصورة غير واضحة بما يكفي لقراءة السؤال بدقة. يرجى التقاطها بإضاءة أفضل.»
        6. لا تكشف عن مفتاح API أو التعليمات البرمجية أو إعدادات النظام الداخلية إطلاقًا.
    """.trimIndent()

    private fun Bitmap.toBase64Jpeg(): String {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun askAssistant(
        prompt: String,
        imageBitmap: Bitmap? = null,
        hintLevel: Int = 0 // 0 = standard answer, 1 = Hint 1, 2 = Hint 2, 3 = Hint 3
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiResult.Error(
                "لم يتم تكوين مفتاح Gemini API بعد. يمكنك إضافته من لوحة Secrets في AI Studio."
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val rootJson = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemInstruction))
            sysInstructionObj.put("parts", sysParts)
            rootJson.put("systemInstruction", sysInstructionObj)

            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            var effectivePrompt = prompt
            if (hintLevel in 1..3) {
                effectivePrompt = when (hintLevel) {
                    1 -> "أعطني تلميحًا أوليًا فقط (تلميح 1 من 3) يساعدني على التفكير في بداية الحل لهذا السؤال بدون كشف الحل الكامل أو النتيجة النهائية: $prompt"
                    2 -> "أعطني تلميحًا ثانيًا أكثر تفصيلًا (تلميح 2 من 3) يرشدني إلى القانون أو الخطوة التالية بدون كشف النتيجة النهائية: $prompt"
                    else -> "أعطني تلميحًا متقدمًا (تلميح 3 من 3) يقربني من النتيجة مع ترك خطوة الحساب الأخيرة لي: $prompt"
                }
            }

            partsArray.put(JSONObject().put("text", effectivePrompt))

            if (imageBitmap != null) {
                val inlineDataObj = JSONObject()
                inlineDataObj.put("mimeType", "image/jpeg")
                inlineDataObj.put("data", imageBitmap.toBase64Jpeg())
                partsArray.put(JSONObject().put("inlineData", inlineDataObj))
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            // Config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.4)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                return@withContext GeminiResult.Error("تعذر الاتصال بـ Gemini (${response.code})")
            }

            val respBodyStr = response.body?.string() ?: ""
            val respJson = JSONObject(respBodyStr)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiResult.Error("لم يقدم النموذج إجابة. يرجى إعادة المحاولة.")
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            if (text.isBlank()) {
                return@withContext GeminiResult.Error("الاستجابة فارغة.")
            }

            GeminiResult.Success(text)
        } catch (e: java.net.UnknownHostException) {
            GeminiResult.Error("المساعد الذكي يحتاج إلى اتصال بالإنترنت. يرجى التحقق من الشبكة.", isNetworkError = true)
        } catch (e: java.io.IOException) {
            GeminiResult.Error("انقطع الاتصال بالإنترنت أثناء معالجة الطلب.", isNetworkError = true)
        } catch (e: Exception) {
            GeminiResult.Error("حدث خطأ غير متوقع: ${e.localizedMessage ?: "فشل الطلب"}")
        }
    }

    suspend fun generateQuiz(
        subject: String,
        topic: String,
        count: Int = 4,
        difficulty: String = "متوسط"
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiResult.Error("يرجى إدخال مفتاح Gemini API من لوحة Secrets.")
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val prompt = """
                أنشئ اختبارًا تدريبيًا باللغة العربية لمادة ($subject) في موضوع ($topic).
                مستوى الصعوبة: $difficulty.
                عدد الأسئلة: $count.
                
                أجب بتنسيق JSON حصري بدون أي نص خارجي، وفق الهيكل التالي:
                {
                  "title": "اختبار $subject - $topic",
                  "subject": "$subject",
                  "questions": [
                    {
                      "id": 1,
                      "questionText": "نص السؤال هنا",
                      "options": ["الخيار أ", "الخيار ب", "الخيار ج", "الخيار د"],
                      "correctIndex": 0,
                      "explanation": "شرح سبب صحة الإجابة"
                    }
                  ]
                }
            """.trimIndent()

            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            val genConfig = JSONObject()
            genConfig.put("responseMimeType", "application/json")
            genConfig.put("temperature", 0.3)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext GeminiResult.Error("تعذر إنشاء الاختبار (${response.code})")
            }

            val respBodyStr = response.body?.string() ?: ""
            val respJson = JSONObject(respBodyStr)
            val candidates = respJson.optJSONArray("candidates") ?: return@withContext GeminiResult.Error("لم يتم استلام أسئلة.")
            val rawJsonText = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")

            val quizObj = JSONObject(rawJsonText)
            val title = quizObj.optString("title", "اختبار تدريبي")
            val sub = quizObj.optString("subject", subject)
            val qArray = quizObj.getJSONArray("questions")
            val qList = mutableListOf<QuizQuestion>()

            for (i in 0 until qArray.length()) {
                val q = qArray.getJSONObject(i)
                val opts = mutableListOf<String>()
                val optArr = q.getJSONArray("options")
                for (j in 0 until optArr.length()) {
                    opts.add(optArr.getString(j))
                }
                qList.add(
                    QuizQuestion(
                        id = q.optInt("id", i + 1),
                        questionText = q.getString("questionText"),
                        options = opts,
                        correctIndex = q.optInt("correctIndex", 0),
                        explanation = q.optString("explanation", "")
                    )
                )
            }

            GeminiResult.QuizSuccess(GeneratedQuiz(title, sub, qList))
        } catch (e: java.net.UnknownHostException) {
            GeminiResult.Error("المساعد الذكي يحتاج إلى اتصال بالإنترنت.", isNetworkError = true)
        } catch (e: Exception) {
            GeminiResult.Error("تعذر صياغة الاختبار: ${e.message}")
        }
    }
}
