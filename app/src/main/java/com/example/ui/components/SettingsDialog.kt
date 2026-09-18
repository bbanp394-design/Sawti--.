package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsDialog(
    isOpen: Boolean,
    language: String,
    autoSpeakEnabled: Boolean,
    isDarkMode: Boolean,
    vibrationEnabled: Boolean,
    fontScale: Float,
    onToggleAutoSpeak: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleVibration: () -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        "ar" -> "إعدادات إمكانية الوصول"
                        "zh" -> "无障碍与系统设置"
                        else -> "Accessibility Settings"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Auto Speak Setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                "ar" -> "النطق التلقائي"
                                "zh" -> "自动朗读"
                                else -> "Auto Speak"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = when (language) {
                                "ar" -> "نطق النص فور التعرف عليه وتصحيحه تلقائياً"
                                "zh" -> "手写识别并校正后自动发音"
                                else -> "Speak automatically after write & recognize"
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoSpeakEnabled,
                        onCheckedChange = { onToggleAutoSpeak() },
                        modifier = Modifier.testTag("settings_auto_speak_switch")
                    )
                }

                HorizontalDivider()

                // 2. Dark Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                "ar" -> "الوضع الداكن (مريح للعين)"
                                "zh" -> "深色模式（护眼舒适）"
                                else -> "Dark Mode (Eye Comfort)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = when (language) {
                                "ar" -> "تباين عالي وتقليل إجهاد النظر"
                                "zh" -> "高对比度深色背景"
                                else -> "High contrast dark canvas"
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode() },
                        modifier = Modifier.testTag("settings_dark_mode_switch")
                    )
                }

                HorizontalDivider()

                // 3. Vibration Feedback Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                "ar" -> "الاهتزاز عند اللمس"
                                "zh" -> "触摸震动反馈"
                                else -> "Vibration Feedback"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = when (language) {
                                "ar" -> "تأكيد بالاهتزاز لكل لمسة أو ضغطة زر"
                                "zh" -> "每次点击按钮时给予触觉震动反馈"
                                else -> "Haptic pulse for every button tap"
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { onToggleVibration() },
                        modifier = Modifier.testTag("settings_vibration_switch")
                    )
                }

                HorizontalDivider()

                // 4. Font Size Scale Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = when (language) {
                            "ar" -> "حجم الخطوط للكبار"
                            "zh" -> "老年人大字号设置"
                            else -> "Elderly Font Size"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sizes = listOf(
                            Triple(1.0f, "عادي", "Normal"),
                            Triple(1.15f, "كبير", "Large"),
                            Triple(1.35f, "كبير جداً", "Extra")
                        )
                        sizes.forEach { (scale, arLabel, enLabel) ->
                            val isSelected = fontScale == scale
                            Surface(
                                onClick = { onFontScaleChange(scale) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (language == "ar") arLabel else enLabel,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = when (language) {
                        "ar" -> "حفظ وإغلاق"
                        "zh" -> "完成"
                        else -> "Save & Close"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
