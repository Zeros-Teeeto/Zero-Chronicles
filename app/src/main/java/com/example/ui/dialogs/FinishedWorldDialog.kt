package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GameSession
import com.example.ui.theme.*

@Composable
fun FinishedWorldDialog(
    session: GameSession,
    onDismiss: () -> Unit
) {
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
                            text = "📜 ЛЕТОПИСЬ СУДЬБЫ",
                            color = GoldPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val deathSub = if (session.isHeroDead) " (💀 ПОГИБ)" else ""
                        Text(
                            text = "Мир: ${session.worldName} | Герой: ${session.heroName} | Дней: ${session.currentDay}$deathSub",
                            color = FantasyTextDim,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = FantasyTextDim)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Score card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181924)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val scoreStr = session.finalScore ?: "—"
                        Text(
                            text = "⭐ ОЦЕНКА ВЛИЯНИЯ НА МИР: $scoreStr / 10",
                            color = GoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chronicle details
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF08090E), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("👤 ГЕРОЙ: ${session.heroName}", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("📅 ВРЕМЯ В ПУТИ: ${session.currentDay} дней", color = FantasyText, fontSize = 12.sp)
                        Text("📍 ОТКРЫТО ТОЧЕК НА КАРТЕ: ${session.mapMarkers.size}", color = FantasyText, fontSize = 12.sp)
                        Text("✨ ДАРЫ ЗЕРО: ${session.zeroGifts.ifBlank { "—" }}", color = FantasyText, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFF26231C))
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (session.acquiredAbilities.isNotEmpty()) {
                        item {
                            Text("⚡ ПОЛУЧЕННЫЕ СПОСОБНОСТИ:", color = Color(0xFFC084FC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            session.acquiredAbilities.forEach { ab ->
                                Text("• $ab", color = FantasyText, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFF26231C))
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    if (session.storyMemories.isNotEmpty()) {
                        item {
                            Text("🧠 ВЕЧНАЯ ПАМЯТЬ И СОЮЗЫ:", color = Color(0xFFA5B4FC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            session.storyMemories.forEach { mem ->
                                Text("• [${mem.title}]:\n${mem.fullText}", color = FantasyText, fontSize = 11.sp, lineHeight = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFF26231C))
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    item {
                        Text("🌍 СОСТОЯНИЕ МИРА:", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(session.worldLore, color = FantasyText, fontSize = 11.sp, lineHeight = 15.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFF26231C))
                        Spacer(modifier = Modifier.height(6.dp))

                        Text("📜 КРАТКОЕ ИЗЛОЖЕНИЕ И НАСЛЕДИЕ:", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        val summaryText = session.finalSummary.ifBlank {
                            if (session.storyText.isNotBlank()) session.storyText.takeLast(400) else "История затерялась в веках..."
                        }
                        Text(summaryText, color = FantasyText, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🔒 Этот мир завершён. Летопись сохранена в хрониках истории.",
                    color = FantasyTextDim,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

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
