package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
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
import com.example.data.StoryMemory
import com.example.ui.theme.*

@Composable
fun MemoryVaultDialog(
    memories: List<StoryMemory>,
    onToggleActive: (Int) -> Unit,
    onDeleteMemory: (Int) -> Unit,
    onSaveLastTurn: () -> Unit,
    onAddCustomMemory: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customTitle by remember { mutableStateOf("") }
    var customText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .border(1.dp, Color(0xFF818CF8), RoundedCornerShape(12.dp)),
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
                            text = "🧠 ХРАНИЛИЩЕ ВЕЧНОЙ ПАМЯТИ",
                            color = Color(0xFFA5B4FC),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val active = memories.count { it.isActive }
                        Text(
                            text = "Активно в контексте ИИ: $active / ${memories.size}",
                            color = FantasyTextDim,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = FantasyTextDim)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onSaveLastTurn,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF182218),
                            contentColor = StoryPositive
                        )
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сохранить ход", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = { showCustomInput = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFF38BDF8)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Вручную", fontSize = 11.sp)
                    }
                }

                if (showCustomInput) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0F17)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Новое воспоминание", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customTitle,
                                onValueChange = { customTitle = it },
                                label = { Text("Заголовок") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customText,
                                onValueChange = { customText = it },
                                label = { Text("Полный текст воспоминания") },
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showCustomInput = false }) {
                                    Text("Отмена", color = FantasyTextDim)
                                }
                                Button(
                                    onClick = {
                                        if (customText.isNotBlank()) {
                                            onAddCustomMemory(customTitle, customText)
                                            customTitle = ""
                                            customText = ""
                                            showCustomInput = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                                ) {
                                    Text("Сохранить")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (memories.isEmpty()) {
                        item {
                            Text(
                                text = "В хранилище пока нет воспоминаний.\nНажмите «Сохранить ход» или добавьте вручную!",
                                color = FantasyTextDim,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 30.dp)
                            )
                        }
                    } else {
                        itemsIndexed(memories) { idx, mem ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0F17)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📌 ${mem.title}",
                                            color = GoldPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (mem.isActive) "В ИИ" else "Архив",
                                                color = if (mem.isActive) StoryPositive else FantasyTextDim,
                                                fontSize = 11.sp
                                            )
                                            Switch(
                                                checked = mem.isActive,
                                                onCheckedChange = { onToggleActive(idx) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color(0xFF818CF8),
                                                    checkedTrackColor = Color(0xFF3730A3)
                                                )
                                            )
                                            IconButton(
                                                onClick = { onDeleteMemory(idx) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Удалить",
                                                    tint = CrimsonLight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mem.fullText,
                                        color = FantasyText,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        maxLines = 4
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))
                ) {
                    Text("Закрыть", color = FantasyText)
                }
            }
        }
    }
}
