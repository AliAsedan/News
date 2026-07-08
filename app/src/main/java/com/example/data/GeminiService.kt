package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Generates a list of news articles in Arabic using Gemini.
     * Returns a list of parsed Article objects.
     */
    suspend fun generateArabicNews(theme: String? = null): List<Article> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is not configured.")
            return@withContext emptyList()
        }

        val prompt = if (theme.isNullOrBlank()) {
            """
            قم بتوليد 5 أخبار عاجلة أو سياسية أو اقتصادية أو اجتماعية واقعية وشيقة باللغة العربية الفصحى.
            يجب أن تكون الأخبار مخصصة ومفيدة وتتناسب مع واجهة إخبارية احترافية لعام 2026.
            تأكد من تنويع الفئات بين (عاجل، سياسة، اقتصاد، اجتماع).
            قم بإرجاع النتيجة حصراً بصيغة مصفوفة JSON نظيفة ومباشرة دون أي تغليف بنصوص أو ماركداون (مثل ```json).
            يجب أن يحتوي كل كائن في المصفوفة على الحقول التالية وبشكل مطابق تماماً:
            - "title": عنوان الخبر (جذاب واحترافي)
            - "content": تفاصيل الخبر بشكل متكامل ومقنع (من فقرتين على الأقل)
            - "category": فئة الخبر وتكون واحدة من: "عاجل" أو "سياسة" أو "اقتصاد" أو "اجتماع"
            - "source": مصدر الخبر (اسم وكالة أنباء أو محرر صحفي ملائم)
            """.trimIndent()
        } else {
            """
            قم بتوليد 4 أخبار باللغة العربية الفصحى تتمحور حول الموضوع أو الكلمات المفتاحية التالية: "$theme".
            تأكد من تنوع الأخبار وتوافقها مع الموضوع بشكل احترافي ومقنع لعام 2026.
            وزع الفئات بين (عاجل، سياسة، اقتصاد، اجتماع) بحسب ملاءمة الخبر للموضوع.
            قم بإرجاع النتيجة حصراً بصيغة مصفوفة JSON نظيفة ومباشرة دون أي تغليف بنصوص أو ماركداون (مثل ```json).
            يجب أن يحتوي كل كائن في المصفوفة على الحقول التالية وبشكل مطابق تماماً:
            - "title": عنوان الخبر (جذاب واحترافي)
            - "content": تفاصيل الخبر بشكل متكامل ومقنع (من فقرتين على الأقل)
            - "category": فئة الخبر وتكون واحدة من: "عاجل" أو "سياسة" أو "اقتصاد" أو "اجتماع"
            - "source": مصدر الخبر (اسم وكالة أنباء أو محرر صحفي ملائم)
            """.trimIndent()
        }

        // Build the request body using standard JSON
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.8)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed with code: ${response.code}, body: ${response.body?.string()}")
                    return@withContext emptyList()
                }

                val responseBodyStr = response.body?.string() ?: return@withContext emptyList()
                return@withContext parseGeminiJsonResponse(responseBodyStr)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating news with Gemini", e)
            return@withContext emptyList()
        }
    }

    private fun parseGeminiJsonResponse(responseJsonStr: String): List<Article> {
        val articles = mutableListOf<Article>()
        try {
            val root = JSONObject(responseJsonStr)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            val candidate = candidates.optJSONObject(0) ?: return emptyList()
            val content = candidate.optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            val part = parts.optJSONObject(0) ?: return emptyList()
            val text = part.optString("text") ?: return emptyList()

            // The text should be a raw JSON array of articles because of responseMimeType
            // Clean it just in case markdown block format was returned
            val cleanText = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonArray = JSONArray(cleanText)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val title = obj.optString("title", "خبر جديد")
                val contentText = obj.optString("content", "")
                val category = obj.optString("category", "عام")
                val source = obj.optString("source", "الذكاء الاصطناعي")

                if (contentText.isNotBlank()) {
                    articles.add(
                        Article(
                            title = title,
                            content = contentText,
                            category = if (category in listOf("عاجل", "سياسة", "اقتصاد", "اجتماع")) category else "سياسة",
                            timestamp = System.currentTimeMillis() - (i * 1200000), // Staggered timestamps
                            source = source,
                            viewsCount = (50..300).random()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response JSON", e)
        }
        return articles
    }
}
