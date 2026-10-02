package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ChatMessage
import com.example.data.GameEngine
import com.example.ui.theme.*

@Composable
fun TurnCompressionDialog(
    chatHistory: List<ChatMessage>,
    onCompress: (List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedIndices = remember { mutableStateMapOf<Int, Boolean>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .border(1.dp, Color(0xFFEAB308), RoundedCornerShape(12.dp)),
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
                            text = "🗜️ СЖАТИЕ СТАРЫХ ХОДОВ",
                            color = Color(0xFFFDE047),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Сжимает ходы в летопись по 7–10 слов для экономии памяти ИИ",
                            color = FantasyTextDim,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = FantasyTextDim)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FilledTonalButton(
                    onClick = {
                        val cutoff = maxOf(1, chatHistory.size - 10)
                        for (i in 1 until chatHistory.size) {
                            selectedIndices[i] = i < cutoff
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = GoldLight
                    )
                ) {
                    Text("Выбрать старые кроме последних 10")
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Skip index 0 (initial system prompt)
                    if (chatHistory.size <= 1) {
                        item {
                            Text(
                                text = "Ходов пока слишком мало для сжатия.",
                                color = FantasyTextDim,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                    } else {
                        itemsIndexed(chatHistory) { idx, msg ->
                            if (idx > 0) {
                                val role = if (msg.role == "user") "👤 Герой" else "🌍 Мир"
                                val preview = GameEngine.distillTextToShortFact(msg.text, maxWords = 10)
                                val isChecked = selectedIndices[idx] == true

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0E0F17), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedIndices[idx] = checked
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = GoldPrimary,
                                            checkmarkColor = DarkBg
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "[$role | Шаг #$idx]",
                                            color = GoldLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = preview.ifBlank { "..." },
                                            color = FantasyText,
                                            fontSize = 11.sp,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val chosen = selectedIndices.filter { it.value }.keys.sorted()
                        if (chosen.isNotEmpty()) {
                            onCompress(chosen)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    val count = selectedIndices.count { it.value }
                    Text("⚡ Сжать выбранные ходы ($count)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
