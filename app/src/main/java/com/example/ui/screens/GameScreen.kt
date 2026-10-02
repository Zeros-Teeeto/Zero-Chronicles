package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameSession
import com.example.ui.components.GameHeader
import com.example.ui.components.StoryLogView
import com.example.ui.dialogs.*
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.session.collectAsState()
    val config by viewModel.config.collectAsState()
    val currentKeyIdx by viewModel.currentKeyIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var actionInput by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("⚔️ Действие") }

    val showKeys by viewModel.showKeysDialog.collectAsState()
    val showAbilities by viewModel.showAbilitiesDialog.collectAsState()
    val showMap by viewModel.showMapDialog.collectAsState()
    val showMemoryVault by viewModel.showMemoryVaultDialog.collectAsState()
    val showCompression by viewModel.showTurnCompressionDialog.collectAsState()
    val showGm by viewModel.showGmCorrectionDialog.collectAsState()
    val showDialogue by viewModel.showNpcDialogueDialog.collectAsState()
    val dialogueTarget by viewModel.npcDialogueDefaultTarget.collectAsState()
    val showCodex by viewModel.showCodexDialog.collectAsState()
    val finishedWorld by viewModel.showFinishedWorldDialog.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(8.dp)
    ) {
        // Top Header
        GameHeader(
            session = session,
            keysCount = config.apiKeys.size,
            currentKeyIdx = currentKeyIdx,
            onOpenKeys = { viewModel.showKeysDialog.value = true },
            onOpenAbilities = { viewModel.showAbilitiesDialog.value = true },
            onOpenMap = { viewModel.showMapDialog.value = true },
            onOpenMemoryVault = { viewModel.showMemoryVaultDialog.value = true },
            onOpenCompression = { viewModel.showTurnCompressionDialog.value = true },
            onOpenGm = { viewModel.showGmCorrectionDialog.value = true },
            onOpenDialogue = { target ->
                viewModel.npcDialogueDefaultTarget.value = target
                viewModel.showNpcDialogueDialog.value = true
            },
            onOpenCodex = { viewModel.showCodexDialog.value = true },
            onExportStory = {
                val fullStory = viewModel.getExportStoryText()
                viewModel.infoMessage.value = "Хроника скопирована в буфер (экспортировано ${fullStory.length} символов)!"
            },
            onTriggerEndGame = { viewModel.triggerEndGame() },
            onGoToMenu = { viewModel.navigateTo(com.example.viewmodel.Screen.MainMenu) },
            onAdjustFontSize = { delta -> viewModel.adjustFontSize(delta) }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pure Story Log View (100% focused text RPG)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
        ) {
            StoryLogView(
                storyText = session.storyText,
                fontSize = session.fontSize
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Status indicator banner if busy
        if (isLoading && statusMessage.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1716), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⏳ $statusMessage",
                    color = GoldLight,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Action Input Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkPanel),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Mode selector pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf("⚔️ Действие", "💬 Диалог", "👥 Окликнуть толпу")
                    modes.forEach { mode ->
                        val isSelected = selectedMode == mode
                        Button(
                            onClick = { selectedMode = mode },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) CrimsonPrimary else Color(0xFF1B1E29)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = mode,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldLight else FantasyTextDim
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Input field + Send button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isLocked = session.isGameEnded || session.isHeroDead
                    val placeholderText = when {
                        isLocked -> "Путь завершён. Мир сохранён в летописях."
                        selectedMode.contains("Толпу") -> "Обратитесь громко к толпе..."
                        selectedMode.contains("Диалог") -> "Произнесите фразу..."
                        else -> "Опишите ваше действие, решение или тактику..."
                    }

                    val onPerformSend: () -> Unit = {
                        val txt = actionInput.trim()
                        if (txt.isNotBlank() && !isLoading && !isLocked) {
                            actionInput = ""
                            viewModel.sendPlayerAction(txt, selectedMode)
                        }
                    }

                    OutlinedTextField(
                        value = actionInput,
                        onValueChange = { actionInput = it },
                        placeholder = { Text(placeholderText, fontSize = 12.sp) },
                        singleLine = true,
                        enabled = !isLoading && !isLocked,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = { onPerformSend() }
                        ),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkCardBorder
                        )
                    )

                    Button(
                        onClick = onPerformSend,
                        enabled = !isLoading && !isLocked && actionInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonPrimary,
                            disabledContainerColor = Color(0xFF2A2A2A)
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = GoldPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Ход", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ХОД", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    if (showKeys) {
        ApiKeysDialog(
            initialKeys = config.apiKeys,
            currentKeyIdx = currentKeyIdx,
            onSaveKeys = { viewModel.updateKeysPool(it) },
            onDismiss = { viewModel.showKeysDialog.value = false }
        )
    }

    if (showAbilities) {
        AbilitiesDialog(
            heroName = session.heroName,
            zeroGifts = session.zeroGifts,
            abilities = session.acquiredAbilities,
            onDeduplicate = { viewModel.deduplicateAbilities() },
            onRemoveAbility = { viewModel.removeAbility(it) },
            onDismiss = { viewModel.showAbilitiesDialog.value = false }
        )
    }

    if (showMap) {
        InteractiveMapDialog(
            worldName = session.worldName,
            markers = session.mapMarkers,
            onAddMarker = { name, reg, type, x, y ->
                viewModel.addMapMarker(name, reg, type, x, y)
            },
            onDeleteMarker = { viewModel.deleteMapMarker(it) },
            onDismiss = { viewModel.showMapDialog.value = false }
        )
    }

    if (showMemoryVault) {
        MemoryVaultDialog(
            memories = session.storyMemories,
            onToggleActive = { viewModel.toggleMemoryActive(it) },
            onDeleteMemory = { viewModel.deleteMemory(it) },
            onSaveLastTurn = { viewModel.saveLastTurnAsMemory() },
            onAddCustomMemory = { title, body -> viewModel.addCustomMemory(title, body) },
            onDismiss = { viewModel.showMemoryVaultDialog.value = false }
        )
    }

    if (showCompression) {
        TurnCompressionDialog(
            chatHistory = session.chatHistory,
            onCompress = { viewModel.compressSelectedTurns(it) },
            onDismiss = { viewModel.showTurnCompressionDialog.value = false }
        )
    }

    if (showGm) {
        GmCorrectionDialog(
            onRewriteTurn = { viewModel.executeGmRewrite(it) },
            onSendQuery = { viewModel.executeGmOocQuery(it) },
            onDismiss = { viewModel.showGmCorrectionDialog.value = false }
        )
    }

    if (showDialogue) {
        NpcDialogueDialog(
            heroName = session.heroName,
            currentDay = session.currentDay,
            currentTimeOfDay = session.currentTimeOfDay,
            nearbyNpcs = session.codexData.npcsNearby,
            deadNpcs = session.codexData.npcsDead,
            initialTarget = dialogueTarget,
            onSendMessage = { target, msg, onResp ->
                viewModel.sendNpcDialogue(target, msg, onResp)
            },
            onDismiss = { viewModel.showNpcDialogueDialog.value = false }
        )
    }

    if (showCodex) {
        CodexDialog(
            worldName = session.worldName,
            worldLore = session.worldLore,
            zeroGifts = session.zeroGifts,
            abilities = session.acquiredAbilities,
            codex = session.codexData,
            onDismiss = { viewModel.showCodexDialog.value = false }
        )
    }

    finishedWorld?.let { finishedSession ->
        FinishedWorldDialog(
            session = finishedSession,
            onDismiss = { viewModel.showFinishedWorldDialog.value = null }
        )
    }
}
