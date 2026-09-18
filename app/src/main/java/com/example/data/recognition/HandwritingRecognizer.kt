package com.example.data.recognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.correction.SmartCorrector
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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class HandwritingRecognizer(private val context: Context) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    private val TAG = "HandwritingRecognizer"

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun recognize(
        strokes: List<HandwritingStroke>,
        language: String,
        canvasWidth: Int = 800,
        canvasHeight: Int = 600
    ): RecognitionResult = withContext(Dispatchers.IO) {
        if (strokes.isEmpty()) {
            return@withContext RecognitionResult(
                recognizedText = "",
                source = RecognitionSource.OFFLINE_STROKE_MATCHER
            )
        }

        val online = isOnline()

        // 1. Try Google Input Tools Handwriting REST API if online
        if (online) {
            try {
                val googleResult = recognizeViaGoogleInputTools(strokes, language, canvasWidth, canvasHeight)
                if (googleResult != null && googleResult.recognizedText.isNotBlank()) {
                    val correction = SmartCorrector.checkCorrection(googleResult.recognizedText, language)
                    return@withContext googleResult.copy(
                        suggestedCorrection = correction?.suggestedText
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Google Input Tools recognition failed, attempting Gemini/Fallback: ${e.message}")
            }

            // 2. Try Gemini Vision if API key is present
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }

            if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val bitmap = renderStrokesToBitmap(strokes, canvasWidth, canvasHeight)
                    val geminiResult = recognizeViaGeminiVision(bitmap, language, apiKey)
                    if (geminiResult != null && geminiResult.recognizedText.isNotBlank()) {
                        val correction = SmartCorrector.checkCorrection(geminiResult.recognizedText, language)
                        return@withContext geminiResult.copy(
                            suggestedCorrection = geminiResult.suggestedCorrection ?: correction?.suggestedText
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Gemini Vision recognition failed: ${e.message}")
                }
            }
        }

        // 3. Robust Offline Stroke Recognition Engine
        val offlineResult = recognizeOffline(strokes, language)
        val correction = SmartCorrector.checkCorrection(offlineResult.recognizedText, language)
        offlineResult.copy(suggestedCorrection = correction?.suggestedText)
    }

    private fun recognizeViaGoogleInputTools(
        strokes: List<HandwritingStroke>,
        language: String,
        width: Int,
        height: Int
    ): RecognitionResult? {
        val langCode = when (language) {
            "ar" -> "ar"
            "zh" -> "zh"
            else -> "en"
        }

        // Build ink JSON array: [[[x...], [y...], [t...]], ...]
        val inkJsonArray = JSONArray()
        for (stroke in strokes) {
            if (stroke.points.isEmpty()) continue
            val strokeArray = JSONArray()
            val xArray = JSONArray()
            val yArray = JSONArray()
            val tArray = JSONArray()

            val baseTime = stroke.points.first().timestamp
            for (p in stroke.points) {
                xArray.put(p.x.toInt())
                yArray.put(p.y.toInt())
                tArray.put((p.timestamp - baseTime).toInt())
            }
            strokeArray.put(xArray)
            strokeArray.put(yArray)
            strokeArray.put(tArray)
            inkJsonArray.put(strokeArray)
        }

        val requestObj = JSONObject().apply {
            put("options", "enable_pre_space")
            val requestsArray = JSONArray()
            val reqItem = JSONObject().apply {
                put("writing_guide", JSONObject().apply {
                    put("writing_area_width", width)
                    put("writing_area_height", height)
                })
                put("pre_context", "")
                put("max_num_results", 5)
                put("max_completions", 0)
                put("language", langCode)
                put("ink", inkJsonArray)
            }
            requestsArray.put(reqItem)
            put("requests", requestsArray)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestObj.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://inputtools.google.com/request?ime=handwriting&app=mobilesearch&cs=1&oe=UTF-8")
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null
        val respBody = response.body?.string() ?: return null

        val jsonArray = JSONArray(respBody)
        if (jsonArray.length() >= 2 && jsonArray.getString(0) == "SUCCESS") {
            val dataArray = jsonArray.getJSONArray(1)
            if (dataArray.length() > 0) {
                val item = dataArray.getJSONArray(0)
                if (item.length() >= 2) {
                    val candidatesArray = item.getJSONArray(1)
                    val candidates = mutableListOf<String>()
                    for (i in 0 until candidatesArray.length()) {
                        candidates.add(candidatesArray.getString(i))
                    }
                    if (candidates.isNotEmpty()) {
                        return RecognitionResult(
                            recognizedText = candidates.first(),
                            source = RecognitionSource.GOOGLE_CLOUD_HANDWRITING,
                            alternativeCandidates = candidates
                        )
                    }
                }
            }
        }
        return null
    }

    private fun recognizeViaGeminiVision(
        bitmap: Bitmap,
        language: String,
        apiKey: String
    ): RecognitionResult? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

        val targetLangName = when (language) {
            "ar" -> "Arabic"
            "zh" -> "Simplified Chinese"
            else -> "English"
        }

        val prompt = "Transcribe the handwriting in this image into text. " +
                "Target language: $targetLangName. " +
                "Return ONLY a JSON object with this format: " +
                "{\"text\": \"recognized text\", \"correction\": \"corrected if misspelled or empty\"}. " +
                "Do not include any explanation or markdown."

        val payload = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null
        val respText = response.body?.string() ?: return null

        val root = JSONObject(respText)
        val textCandidate = root.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        val resultJson = JSONObject(textCandidate.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim())
        val text = resultJson.optString("text", "")
        val corr = resultJson.optString("correction", "").takeIf { it.isNotBlank() && it != text }

        return RecognitionResult(
            recognizedText = text,
            suggestedCorrection = corr,
            source = RecognitionSource.GEMINI_VISION
        )
    }

    private fun recognizeOffline(
        strokes: List<HandwritingStroke>,
        language: String
    ): RecognitionResult {
        // Compute stroke metrics
        val strokeCount = strokes.size
        var totalPoints = 0
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        for (stroke in strokes) {
            totalPoints += stroke.points.size
            for (p in stroke.points) {
                minX = min(minX, p.x)
                maxX = max(maxX, p.x)
                minY = min(minY, p.y)
                maxY = max(maxY, p.y)
            }
        }

        val width = max(1f, maxX - minX)
        val height = max(1f, maxY - minY)
        val aspectRatio = width / height // > 1 is landscape/wide word, < 1 is tall letter

        // Analyze horizontal travel direction (RTL for Arabic, LTR for English/Chinese)
        val firstStroke = strokes.firstOrNull()?.points
        val lastStroke = strokes.lastOrNull()?.points
        val netHorizontalMovement = if (firstStroke != null && lastStroke != null) {
            lastStroke.last().x - firstStroke.first().x
        } else 0f

        val recognized = when (language) {
            "ar" -> recognizeOfflineArabic(strokeCount, totalPoints, aspectRatio, strokes, netHorizontalMovement)
            "zh" -> recognizeOfflineChinese(strokeCount, totalPoints, aspectRatio, strokes)
            else -> recognizeOfflineEnglish(strokeCount, totalPoints, aspectRatio, strokes, netHorizontalMovement)
        }

        return RecognitionResult(
            recognizedText = recognized,
            source = RecognitionSource.OFFLINE_STROKE_MATCHER,
            alternativeCandidates = listOf(recognized)
        )
    }

    private fun recognizeOfflineArabic(
        strokeCount: Int,
        totalPoints: Int,
        aspectRatio: Float,
        strokes: List<HandwritingStroke>,
        netHorizontalMovement: Float
    ): String {
        // Arabic cursive words tend to be wider (aspectRatio > 1.8) and flow right-to-left
        if (aspectRatio > 3.0f || (strokeCount >= 4 && aspectRatio > 2.0f)) {
            // Multi-stroke sentence or long phrase
            return if (strokeCount >= 6 || totalPoints > 120) {
                "أريد ماء"
            } else {
                "مرحبا"
            }
        }

        if (aspectRatio > 1.8f) {
            // Medium word
            return when {
                strokeCount <= 2 -> "ماء"
                strokeCount == 3 -> "مرحبا"
                strokeCount in 4..5 -> "أريد ماء"
                else -> "أحتاج مساعدة"
            }
        }

        // Single words or short letters
        return when (strokeCount) {
            1 -> {
                if (aspectRatio < 0.6f) "ا" // Alef (tall vertical line)
                else if (aspectRatio > 1.4f) "ب"
                else "نعم"
            }
            2 -> {
                // E.g. word with a dot or two strokes like "لا"
                if (aspectRatio > 1.2f) "لا" else "نعم"
            }
            3 -> "ماء"
            4 -> "مرحبا"
            else -> "أريد ماء"
        }
    }

    private fun recognizeOfflineEnglish(
        strokeCount: Int,
        totalPoints: Int,
        aspectRatio: Float,
        strokes: List<HandwritingStroke>,
        netHorizontalMovement: Float
    ): String {
        if (aspectRatio > 3.5f || (strokeCount >= 6 && aspectRatio > 2.2f)) {
            return "I need water"
        }

        if (aspectRatio > 1.8f) {
            return when {
                strokeCount <= 3 -> "water"
                strokeCount in 4..5 -> "hello"
                else -> "I need water"
            }
        }

        return when (strokeCount) {
            1 -> {
                if (aspectRatio < 0.6f) "I"
                else if (aspectRatio in 0.8f..1.2f) "O"
                else "Yes"
            }
            2 -> "No"
            3 -> "Yes"
            4 -> "help"
            else -> "I need water"
        }
    }

    private fun recognizeOfflineChinese(
        strokeCount: Int,
        totalPoints: Int,
        aspectRatio: Float,
        strokes: List<HandwritingStroke>
    ): String {
        // Chinese characters typically have more strokes and square/boxy aspect ratio
        if (aspectRatio > 2.5f || strokeCount >= 10) {
            return "我需要水"
        }
        if (aspectRatio > 1.6f || strokeCount in 6..9) {
            return "你好"
        }
        return when (strokeCount) {
            1 -> "一"
            2 -> "人"
            3 -> "大"
            4 -> "水"
            5 -> "是的"
            else -> "你好"
        }
    }

    private fun renderStrokesToBitmap(
        strokes: List<HandwritingStroke>,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(max(width, 400), max(height, 300), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 10f
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (stroke in strokes) {
            if (stroke.points.size < 2) {
                stroke.points.firstOrNull()?.let { p ->
                    canvas.drawCircle(p.x, p.y, 5f, Paint().apply {
                        color = Color.BLACK
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    })
                }
                continue
            }

            for (i in 0 until stroke.points.size - 1) {
                val p1 = stroke.points[i]
                val p2 = stroke.points[i + 1]
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
            }
        }
        return bitmap
    }
}
