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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Phrase

@Composable
fun AddEditPhraseDialog(
    isOpen: Boolean,
    editingPhrase: Phrase?,
    language: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var text by remember(editingPhrase) { mutableStateOf(editingPhrase?.text ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (editingPhrase != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingPhrase != null) {
                        when (language) {
                            "ar" -> "تعديل العبارة"
                            "zh" -> "编辑常用短语"
                            else -> "Edit Phrase"
                        }
                    } else {
                        when (language) {
                            "ar" -> "إضافة عبارة سريعة جديدة"
                            "zh" -> "添加新短语"
                            else -> "Add New Quick Phrase"
                        }
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (language) {
                        "ar" -> "نص العبارة التي تريد حفظها ونطقها:"
                        "zh" -> "输入您想要保存并朗读的文本："
                        else -> "Enter phrase to save and speak:"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phrase_input_field"),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = {
                        Text(
                            text = when (language) {
                                "ar" -> "مثال: أريد الراحة"
                                "zh" -> "例如：我想休息"
                                else -> "e.g. I want to rest"
                            }
                        )
                    },
                    singleLine = false,
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onSave(text.trim())
                    }
                },
                enabled = text.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("save_phrase_confirm_button")
            ) {
                Text(
                    text = when (language) {
                        "ar" -> "حفظ"
                        "zh" -> "保存"
                        else -> "Save"
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
