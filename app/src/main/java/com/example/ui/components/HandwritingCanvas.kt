package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.recognition.HandwritingStroke
import com.example.data.recognition.StrokePoint
import com.example.ui.theme.CanvasBgDark
import com.example.ui.theme.CanvasBgLight
import com.example.ui.theme.CanvasInkDark
import com.example.ui.theme.CanvasInkLight

@Composable
fun HandwritingCanvas(
    strokes: List<HandwritingStroke>,
    currentStrokePoints: List<StrokePoint>,
    isRecognizing: Boolean,
    isDarkMode: Boolean,
    isOnline: Boolean,
    language: String,
    onStrokeStart: (Float, Float) -> Unit,
    onStrokeMove: (Float, Float) -> Unit,
    onStrokeEnd: () -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit,
    onRecognize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canvasBg = if (isDarkMode) CanvasBgDark else CanvasBgLight
    val inkColor = if (isDarkMode) CanvasInkDark else CanvasInkLight
    val guidelineColor = if (isDarkMode) Color(0x15FFFFFF) else Color(0x15000000)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Mode and Status Indicator Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (language) {
                    "ar" -> "منطقة الكتابة اليدوية (اكتب بالأصبع أو القلم)"
                    "zh" -> "手写区域（用手指或手写笔书写）"
                    else -> "Handwriting Area (Finger or Stylus)"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.AutoAwesome else Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOnline) {
                            when (language) {
                                "ar" -> "تعرف ذكي متصل"
                                "zh" -> "智能在线识别"
                                else -> "Smart Online AI"
                            }
                        } else {
                            when (language) {
                                "ar" -> "وضع محلي بدون نت"
                                "zh" -> "离线本地识别"
                                else -> "Offline Local Mode"
                            }
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Handwriting Drawing Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(canvasBg)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .testTag("handwriting_canvas_box")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                onStrokeStart(offset.x, offset.y)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                onStrokeMove(change.position.x, change.position.y)
                            },
                            onDragEnd = {
                                onStrokeEnd()
                            },
                            onDragCancel = {
                                onStrokeEnd()
                            }
                        )
                    }
                    .testTag("handwriting_canvas")
            ) {
                // Draw soft ruled notebook guidelines to assist writing
                val lineSpacing = 48.dp.toPx()
                var y = lineSpacing
                while (y < size.height) {
                    drawLine(
                        color = guidelineColor,
                        start = Offset(16.dp.toPx(), y),
                        end = Offset(size.width - 16.dp.toPx(), y),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    y += lineSpacing
                }

                // Draw completed strokes
                for (stroke in strokes) {
                    if (stroke.points.size >= 2) {
                        val path = Path()
                        path.moveTo(stroke.points[0].x, stroke.points[0].y)
                        for (i in 1 until stroke.points.size) {
                            val p0 = stroke.points[i - 1]
                            val p1 = stroke.points[i]
                            // Quad curve for smoother ink strokes
                            val midX = (p0.x + p1.x) / 2f
                            val midY = (p0.y + p1.y) / 2f
                            path.quadraticTo(p0.x, p0.y, midX, midY)
                        }
                        path.lineTo(stroke.points.last().x, stroke.points.last().y)

                        drawPath(
                            path = path,
                            color = inkColor,
                            style = Stroke(
                                width = 7.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    } else if (stroke.points.size == 1) {
                        drawCircle(
                            color = inkColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(stroke.points[0].x, stroke.points[0].y)
                        )
                    }
                }

                // Draw active in-progress stroke
                if (currentStrokePoints.size >= 2) {
                    val activePath = Path()
                    activePath.moveTo(currentStrokePoints[0].x, currentStrokePoints[0].y)
                    for (i in 1 until currentStrokePoints.size) {
                        val p0 = currentStrokePoints[i - 1]
                        val p1 = currentStrokePoints[i]
                        val midX = (p0.x + p1.x) / 2f
                        val midY = (p0.y + p1.y) / 2f
                        activePath.quadraticTo(p0.x, p0.y, midX, midY)
                    }
                    activePath.lineTo(currentStrokePoints.last().x, currentStrokePoints.last().y)

                    drawPath(
                        path = activePath,
                        color = inkColor,
                        style = Stroke(
                            width = 7.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else if (currentStrokePoints.size == 1) {
                    drawCircle(
                        color = inkColor,
                        radius = 3.5.dp.toPx(),
                        center = Offset(currentStrokePoints[0].x, currentStrokePoints[0].y)
                    )
                }
            }

            // Watermark prompt when empty
            if (strokes.isEmpty() && currentStrokePoints.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (language) {
                            "ar" -> "✍️ اكتب هنا بأصبعك..."
                            "zh" -> "✍️ 在此用手指书写..."
                            else -> "✍️ Write here with finger or stylus..."
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Canvas Action Buttons - Clear, Undo, Recognize (Large touch targets for elderly users)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🗑️ Clear Button
            OutlinedButton(
                onClick = onClear,
                enabled = strokes.isNotEmpty() || currentStrokePoints.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("canvas_clear_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (language) {
                        "ar" -> "مسح"
                        "zh" -> "清除"
                        else -> "Clear"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ↩️ Undo Button
            OutlinedButton(
                onClick = onUndo,
                enabled = strokes.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("canvas_undo_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (language) {
                        "ar" -> "تراجع"
                        "zh" -> "撤销"
                        else -> "Undo"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 🔄 Recognize Button (Primary Prominent Action)
            Button(
                onClick = onRecognize,
                enabled = strokes.isNotEmpty() && !isRecognizing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .weight(1.3f)
                    .height(56.dp)
                    .testTag("canvas_recognize_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isRecognizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (language) {
                            "ar" -> "جارِ التعرف..."
                            "zh" -> "识别中..."
                            else -> "Recognizing..."
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recognize",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (language) {
                            "ar" -> "تعرف 🔄"
                            "zh" -> "识别 🔄"
                            else -> "Recognize 🔄"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
