package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.CodexData
import com.example.ui.theme.*

@Composable
fun CodexDialog(
    worldName: String,
    worldLore: String,
    zeroGifts: String,
    abilities: List<String>,
    codex: CodexData,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("🏛️ Локации", "👥 Персонажи", "✨ Силы", "📜 Заметки")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
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
                    Text(
                        text = "📖 КОДЕКС: ${worldName.uppercase()}",
                        color = GoldPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = FantasyTextDim)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF090A0F),
                    contentColor = GoldPrimary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF07070B), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedTab) {
                        0 -> { // Locations
                            item {
                                Text("ОТКРЫТЫЕ ЛОКАЦИИ:", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            if (codex.locations.isEmpty()) {
                                item {
                                    Text("Исследованные земли пока пусты...", color = FantasyTextDim, fontSize = 12.sp)
                                }
                            } else {
                                codex.locations.forEach { loc ->
                                    item {
                                        Text("• $loc", color = FantasyText, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        1 -> { // NPCs
                            item {
                                Text("📍 РЯДОМ С ВАМИ СЕЙЧАС:", color = StoryPositive, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            if (codex.npcsNearby.isEmpty()) {
                                item { Text("  (Никого нет поблизости)", color = FantasyTextDim, fontSize = 12.sp) }
                            } else {
                                codex.npcsNearby.forEach { n ->
                                    item { Text("  🟢 $n", color = StoryPositive, fontSize = 12.sp) }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(8.dp)) }
                            item {
                                Text("💀 ПОГИБШИЕ ПЕРСОНАЖИ:", color = CrimsonPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            if (codex.npcsDead.isEmpty()) {
                                item { Text("  (Пока никто не погиб)", color = FantasyTextDim, fontSize = 12.sp) }
                            } else {
                                codex.npcsDead.forEach { d ->
                                    item { Text("  ⚰️ $d", color = CrimsonLight, fontSize = 12.sp) }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(8.dp)) }
                            item {
                                Text("📜 ВСЕ ВСТРЕЧЕННЫЕ СУЩЕСТВА:", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            if (codex.npcsAll.isEmpty()) {
                                item { Text("  (Пока никого)", color = FantasyTextDim, fontSize = 12.sp) }
                            } else {
                                codex.npcsAll.forEach { m ->
                                    item { Text("  • $m", color = FantasyText, fontSize = 12.sp) }
                                }
                            }
                        }

                        2 -> { // Powers
                            item {
                                Text("🌌 ИЗНАЧАЛЬНЫЕ ДАРЫ ЗЕРО:", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(zeroGifts, color = FantasyText, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFF2B261F))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("⚡ ПРИОБРЕТЁННЫЕ СПОСОБНОСТИ (${abilities.size}):", color = Color(0xFFC084FC), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            if (abilities.isEmpty()) {
                                item {
                                    Text("Новых способностей пока нет.", color = FantasyTextDim, fontSize = 12.sp)
                                }
                            } else {
                                abilities.forEachIndexed { i, ab ->
                                    item {
                                        Text("${i + 1}. $ab", color = FantasyText, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        3 -> { // Notes & Lore
                            item {
                                Text("🌍 ЛОР МИРА:", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(worldLore, color = FantasyText, fontSize = 12.sp, lineHeight = 16.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFF2B261F))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("📜 ЗАМЕТКИ СТРАНСТВИЯ:", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            if (codex.notes.isEmpty()) {
                                item {
                                    Text("Заметок пока нет.", color = FantasyTextDim, fontSize = 12.sp)
                                }
                            } else {
                                codex.notes.forEach { note ->
                                    item {
                                        Text("• $note", color = FantasyText, fontSize = 12.sp)
                                    }
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
