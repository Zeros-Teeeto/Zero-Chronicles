package com.example.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun ApiKeysDialog(
    initialKeys: List<String>,
    currentKeyIdx: Int,
    onSaveKeys: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var keysList by remember {
        val nonBlank = initialKeys.filter { it.isNotBlank() }
        mutableStateOf(
            if (nonBlank.isEmpty()) mutableListOf("")
            else nonBlank.toMutableList()
        )
    }
    var visibleIndices by remember { mutableStateOf(setOf<Int>()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkPanel),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🔑 ПУЛ API-КЛЮЧЕЙ GEMINI",
                            color = GoldPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val currInfo = if (keysList.isNotEmpty()) "Текущий ключ: #${currentKeyIdx + 1}" else "Нет ключей"
                        Text(
                            text = "Все ключи опрашиваются по очереди без пропусков. [$currInfo]",
                            color = FantasyTextDim,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = FantasyTextDim)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        keysList = (keysList + "").toMutableList()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E281E))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = StoryPositive)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Добавить ещё ключ", color = StoryPositive)
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(keysList) { idx, keyVal ->
                        val isCurrent = idx == currentKeyIdx
                        val isVisible = visibleIndices.contains(idx)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isCurrent) "▶ #${idx + 1}" else "#${idx + 1}",
                                color = if (isCurrent) GoldPrimary else FantasyTextDim,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp)
                            )

                            OutlinedTextField(
                                value = keyVal,
                                onValueChange = { newText ->
                                    val updated = keysList.toMutableList()
                                    updated[idx] = newText
                                    keysList = updated
                                },
                                visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                placeholder = { Text("AIzaSy...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )

                            // Show/Hide
                            IconButton(
                                onClick = {
                                    visibleIndices = if (isVisible) {
                                        visibleIndices - idx
                                    } else {
                                        visibleIndices + idx
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Показать/Скрыть",
                                    tint = FantasyTextDim,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Paste
                            IconButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text.orEmpty().trim()
                                    if (clip.isNotBlank()) {
                                        val updated = keysList.toMutableList()
                                        updated[idx] = clip
                                        keysList = updated
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Вставить",
                                    tint = GoldLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Remove
                            IconButton(
                                onClick = {
                                    if (keysList.size > 1) {
                                        val updated = keysList.toMutableList()
                                        updated.removeAt(idx)
                                        keysList = updated
                                    } else {
                                        keysList = mutableListOf("")
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Удалить",
                                    tint = CrimsonLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val valid = keysList.map { it.trim() }.filter { it.isNotBlank() }
                        onSaveKeys(valid)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("💾 СОХРАНИТЬ ПУЛ КЛЮЧЕЙ", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
