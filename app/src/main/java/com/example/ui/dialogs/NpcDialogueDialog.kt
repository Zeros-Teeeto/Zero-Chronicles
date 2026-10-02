package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.ui.theme.*

@Composable
fun NpcDialogueDialog(
    heroName: String,
    currentDay: Int,
    currentTimeOfDay: String,
    nearbyNpcs: List<String>,
    deadNpcs: List<String>,
    initialTarget: String? = null,
    onSendMessage: (target: String, message: String, onResponse: (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val aliveNpcs = nearbyNpcs.filter { !deadNpcs.contains(it) }
    val options = remember(aliveNpcs) {
        listOf("👥 Толпа / Окружающие") + aliveNpcs
    }

    var selectedTarget by remember {
        mutableStateOf(initialTarget?.takeIf { options.contains(it) } ?: options.first())
    }

    var isDropdownOpen by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isWaitingResponse by remember { mutableStateOf(false) }

    data class DialogLine(val sender: String, val text: String)
    val conversationLines = remember { mutableStateListOf<DialogLine>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkPanel),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🗣️ ПРЯМОЙ ДИАЛОГ",
                            color = Color(0xFF38BDF8),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "День $currentDay ($currentTimeOfDay)  |  Тайна сделок и мыслей соблюдается",
                            color = FantasyTextDim,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = FantasyTextDim)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownOpen = true },
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0E0F17))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Собеседник: $selectedTarget",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GoldPrimary)
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownOpen,
                        onDismissRequest = { isDropdownOpen = false },
                        modifier = Modifier.background(DarkPanel)
                    ) {
                        options.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, color = FantasyText) },
                                onClick = {
                                    selectedTarget = opt
                                    conversationLines.clear()
                                    isDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dialogue Log
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF07070B), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (conversationLines.isEmpty()) {
                        item {
                            Text(
                                text = "Собеседник помнит сюжет, но знает ТОЛЬКО то, что видел или слышал сам!\n" +
                                        "Задайте вопрос или обратитесь с репликой.",
                                color = FantasyTextDim,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        items(conversationLines) { line ->
                            val isHero = line.sender == heroName
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isHero) Alignment.End else Alignment.Start
                            ) {
                                Text(
                                    text = if (isHero) "👤 $heroName" else "💬 ${line.sender}",
                                    color = if (isHero) StoryAction else GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isHero) Color(0xFF132035) else Color(0xFF1E1716),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = line.text,
                                        color = if (isHero) StorySpeech else FantasyText,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Сказать фразу или обратиться...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        enabled = !isWaitingResponse
                    )

                    Button(
                        onClick = {
                            val msg = inputText.trim()
                            if (msg.isNotBlank() && !isWaitingResponse) {
                                conversationLines.add(DialogLine(heroName, msg))
                                inputText = ""
                                isWaitingResponse = true

                                onSendMessage(selectedTarget, msg) { reply ->
                                    conversationLines.add(DialogLine(selectedTarget, reply))
                                    isWaitingResponse = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        enabled = !isWaitingResponse
                    ) {
                        if (isWaitingResponse) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = GoldPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Сказать", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
