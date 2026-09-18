package com.example.data.recognition

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset

data class StrokePoint(
    val x: Float,
    val y: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class HandwritingStroke(
    val points: List<StrokePoint>
) {
    val isSingleDot: Boolean
        get() = points.size <= 2 || boundingBoxDiagonal < 15f

    private val boundingBoxDiagonal: Float
        get() {
            if (points.isEmpty()) return 0f
            var minX = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var minY = Float.MAX_VALUE
            var maxY = Float.MIN_VALUE
            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
            val dx = maxX - minX
            val dy = maxY - minY
            return kotlin.math.sqrt(dx * dx + dy * dy)
        }
}

data class RecognitionResult(
    val recognizedText: String,
    val suggestedCorrection: String? = null,
    val source: RecognitionSource,
    val alternativeCandidates: List<String> = emptyList()
)

enum class RecognitionSource {
    GOOGLE_CLOUD_HANDWRITING,
    GEMINI_VISION,
    OFFLINE_STROKE_MATCHER
}
