package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun EmergencyButton(
    language: String,
    onRequestEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onRequestEmergency,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
            .testTag("emergency_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Emergency",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = when (language) {
                    "ar" -> "🆘 أحتاج مساعدة (طوارئ)"
                    "zh" -> "🆘 我需要帮助（紧急模式）"
                    else -> "🆘 I NEED HELP (EMERGENCY)"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun EmergencyConfirmationDialog(
    isOpen: Boolean,
    language: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        "ar" -> "تأكيد وضع الطوارئ"
                        "zh" -> "确认进入紧急模式"
                        else -> "Confirm Emergency Mode"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Text(
                text = when (language) {
                    "ar" -> "هل أنت متأكد أنك بحاجة إلى مساعدة عاجلة؟ سيتم عرض عبارات الطوارئ المكبرة وتشغيل الصوت المسموع لتنبيه من حولك."
                    "zh" -> "您确定需要紧急帮助吗？将显示特大号紧急求助短语并播放声音提醒周围的人。"
                    else -> "Are you sure you need urgent assistance? This will show extra-large emergency phrases and sound alerts for nearby caregivers."
                },
                fontSize = 17.sp,
                lineHeight = 24.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("confirm_emergency_button")
            ) {
                Text(
                    text = when (language) {
                        "ar" -> "نعم، أحتاج مساعدة! 🆘"
                        "zh" -> "是的，需要帮助！ 🆘"
                        else -> "Yes, I Need Help! 🆘"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = when (language) {
                        "ar" -> "إلغاء"
                        "zh" -> "取消"
                        else -> "Cancel"
                    },
                    fontSize = 16.sp
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EmergencyScreen(
    isOpen: Boolean,
    language: String,
    onSpeakEmergencyPhrase: (String) -> Unit,
    onCloseEmergency: () -> Unit
) {
    if (!isOpen) return

    val emergencyPhrases = when (language) {
        "ar" -> listOf(
            "أحتاج إلى مساعدة عاجلة!",
            "أشعر بألم شديد في الصدر!",
            "لا أستطيع التنفس جيداً!",
            "لقد سقطت ولا أستطيع النهوض!",
            "أحتاج إلى دوائي فوراً!",
            "اتصل بالإسعاف أو الطبيب من فضلك!",
            "اتصل بعائلتي الآن!"
        )
        "zh" -> listOf(
            "我需要紧急帮助！",
            "我胸口剧烈疼痛！",
            "我呼吸非常困难！",
            "我摔倒了无法站起来！",
            "我现在需要吃药！",
            "请帮我叫救护车或医生！",
            "请立即联系我的家人！"
        )
        else -> listOf(
            "I need urgent help!",
            "I have severe chest pain!",
            "I cannot breathe properly!",
            "I fell down and cannot get up!",
            "I need my medication immediately!",
            "Please call for medical assistance!",
            "Call my family right now!"
        )
    }

    Dialog(
        onDismissRequest = onCloseEmergency,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF7F0000) // Deep urgent red emergency theme
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Emergency Top Bar with Exit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (language) {
                                "ar" -> "🆘 وضع الطوارئ العاجل"
                                "zh" -> "🆘 紧急求助模式"
                                else -> "🆘 EMERGENCY MODE"
                            },
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = onCloseEmergency,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("close_emergency_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (language) {
                                "ar" -> "إغلاق"
                                "zh" -> "退出"
                                else -> "Exit"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (language) {
                        "ar" -> "اضغط على أي عبارة لنطقها فوراً بأعلى صوت:"
                        "zh" -> "点击任意短语立即大声朗读："
                        else -> "Tap any phrase to announce immediately:"
                    },
                    color = Color.Yellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Emergency Phrases List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(emergencyPhrases) { phrase ->
                        Button(
                            onClick = { onSpeakEmergencyPhrase(phrase) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF900000)
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .shadow(8.dp, shape = RoundedCornerShape(18.dp))
                        ) {
                            Text(
                                text = phrase,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Safety Note
                Surface(
                    color = Color(0x44000000),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (language) {
                            "ar" -> "ملاحظة أمان: هذا الوضع ينطق العبارات لتنبيه الحاضرين ولا يقوم بالاتصال المباشر بخدمات الطوارئ."
                            "zh" -> "安全说明：此模式大声朗读以提醒身边的人，不会自动拨打急救电话。"
                            else -> "Safety Note: This mode speaks aloud to alert people around you and does not automatically dial emergency services."
                        },
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
