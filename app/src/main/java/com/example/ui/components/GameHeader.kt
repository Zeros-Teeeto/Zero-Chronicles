package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameSession
import com.example.ui.theme.*

@Composable
fun GameHeader(
    session: GameSession,
    keysCount: Int,
    currentKeyIdx: Int,
    onOpenKeys: () -> Unit,
    onOpenAbilities: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenMemoryVault: () -> Unit,
    onOpenCompression: () -> Unit,
    onOpenGm: () -> Unit,
    onOpenDialogue: (String?) -> Unit,
    onOpenCodex: () -> Unit,
    onExportStory: () -> Unit,
    onTriggerEndGame: () -> Unit,
    onGoToMenu: () -> Unit,
    onAdjustFontSize: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkPanel),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            // Row 1: World & Hero info + Status badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "🌍 ${session.worldName}",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "👤 ${session.heroName}  |  ${session.genre}",
                        color = FantasyTextDim,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Turn badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF261933), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🎲 #${session.totalTurnsCount}",
                            color = Color(0xFFC084FC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Calendar badge
                    val timeColor = when (session.currentTimeOfDay) {
                        "Утро" -> TimeMorning
                        "День" -> TimeDay
                        "Вечер" -> TimeEvening
                        else -> TimeNight
                    }
                    Box(
                        modifier = Modifier
                            .background(timeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, timeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "📅 Д${session.currentDay} (${session.currentTimeOfDay})",
                            color = timeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Status badge
                    val statusColor = when {
                        session.heroStatus.contains("невредим", true) || session.heroStatus.contains("здравии", true) -> StoryPositive
                        session.heroStatus.contains("воскрес", true) -> Color(0xFFCC66FF)
                        session.heroStatus.contains("легк", true) || session.heroStatus.contains("ушиб", true) -> GoldLight
                        session.heroStatus.contains("смерт", true) || session.heroStatus.contains("погиб", true) -> CrimsonPrimary
                        else -> Color(0xFFFF7733)
                    }
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "❤️ ${session.heroStatus.take(15)}",
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFF24211D), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Action control chips (horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Key badge
                HeaderChip(
                    text = "🔑 #${currentKeyIdx + 1} ($keysCount)",
                    color = Color(0xFF1C202B),
                    textColor = GoldLight,
                    onClick = onOpenKeys
                )

                // Powers
                HeaderChip(
                    text = "✨ Силы (${session.acquiredAbilities.size})",
                    color = Color(0xFF2C1A2D),
                    textColor = Color(0xFFF3E8FF),
                    borderColor = Color(0xFFA855F7),
                    onClick = onOpenAbilities
                )

                // Map
                HeaderChip(
                    text = "🗺️ Карта (${session.mapMarkers.size})",
                    color = Color(0xFF1E293B),
                    textColor = Color(0xFF38BDF8),
                    borderColor = Color(0xFF38BDF8),
                    onClick = onOpenMap
                )

                // Memory Vault
                val activeMem = session.storyMemories.count { it.isActive }
                HeaderChip(
                    text = "🧠 Память ($activeMem/${session.storyMemories.size})",
                    color = Color(0xFF201F2E),
                    textColor = Color(0xFFA5B4FC),
                    borderColor = Color(0xFF818CF8),
                    onClick = onOpenMemoryVault
                )

                // Compression
                HeaderChip(
                    text = "🗜️ Сжатие",
                    color = Color(0xFF27271E),
                    textColor = Color(0xFFFDE047),
                    borderColor = Color(0xFFEAB308),
                    onClick = onOpenCompression
                )

                // Master OOC
                HeaderChip(
                    text = "🎭 Мастер",
                    color = Color(0xFF3B2D18),
                    textColor = GoldPrimary,
                    borderColor = GoldPrimary,
                    onClick = onOpenGm
                )

                // NPC Dialogue
                HeaderChip(
                    text = "🗣️ Диалог",
                    color = Color(0xFF1E293B),
                    textColor = FantasyText,
                    onClick = { onOpenDialogue(null) }
                )

                // Crowd
                HeaderChip(
                    text = "👥 Толпа",
                    color = Color(0xFF27273A),
                    textColor = FantasyText,
                    onClick = { onOpenDialogue("👥 Толпа / Окружающие") }
                )

                // Codex
                HeaderChip(
                    text = "📖 Кодекс",
                    color = Color(0xFF202230),
                    textColor = FantasyText,
                    onClick = onOpenCodex
                )

                // Export
                HeaderChip(
                    text = "📜 Экспорт",
                    color = Color(0xFF1E281E),
                    textColor = StoryPositive,
                    borderColor = StoryPositive,
                    onClick = onExportStory
                )

                // Font size A- / A+
                Row(
                    modifier = Modifier
                        .background(Color(0xFF1F1F27), RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    TextButton(
                        onClick = { onAdjustFontSize(-1) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("A-", color = FantasyTextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { onAdjustFontSize(1) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("A+", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // End Game
                HeaderChip(
                    text = "⛔ Финал",
                    color = Color(0xFF451212),
                    textColor = Color(0xFFFF9999),
                    borderColor = CrimsonPrimary,
                    onClick = onTriggerEndGame
                )

                // Menu
                HeaderChip(
                    text = "🚪 В меню",
                    color = Color(0xFF1A1A1A),
                    textColor = FantasyTextDim,
                    onClick = onGoToMenu
                )
            }
        }
    }
}

@Composable
private fun HeaderChip(
    text: String,
    color: Color,
    textColor: Color,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = color,
        border = borderColor?.let { androidx.compose.foundation.BorderStroke(1.dp, it) },
        modifier = Modifier.height(28.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
