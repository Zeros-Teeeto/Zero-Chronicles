package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun WorldCreationScreen(
    currentStep: Int,
    worldName: String,
    genre: String,
    worldLore: String,
    worldMap: String,
    keyNpcs: String,
    sapientRaces: String,
    beasts: String,
    aiNotes: String,
    heroName: String,
    zeroGifts: String,
    onWorldNameChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onWorldLoreChange: (String) -> Unit,
    onWorldMapChange: (String) -> Unit,
    onKeyNpcsChange: (String) -> Unit,
    onSapientRacesChange: (String) -> Unit,
    onBeastsChange: (String) -> Unit,
    onAiNotesChange: (String) -> Unit,
    onHeroNameChange: (String) -> Unit,
    onZeroGiftsChange: (String) -> Unit,
    onRandomizeWorld: () -> Unit,
    onRandomizeAiNotes: () -> Unit,
    onRandomizeHero: () -> Unit,
    onSaveTemplate: () -> Unit,
    onOpenTemplates: () -> Unit,
    onStepChange: (Int) -> Unit,
    onStartAdventure: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkPanel),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header with step indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val stepTitle = when (currentStep) {
                            1 -> "ШАГ 1: СОТВОРЕНИЕ ВСЕЛЕННОЙ"
                            2 -> "ШАГ 2: ПРИМЕЧАНИЯ ДЛЯ ИИ"
                            else -> "ШАГ 3: ГЕРОЙ И ДАРЫ ЗЕРО"
                        }
                        Text(
                            text = stepTitle,
                            color = GoldPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Шаг $currentStep из 3",
                            color = FantasyTextDim,
                            fontSize = 11.sp
                        )
                    }

                    if (currentStep == 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = onSaveTemplate, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Save, contentDescription = "Сохранить шаблон", tint = StoryPositive)
                            }
                            IconButton(onClick = onOpenTemplates, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Folder, contentDescription = "Шаблоны", tint = GoldLight)
                            }
                            IconButton(onClick = onRandomizeWorld, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Casino, contentDescription = "Случайный мир", tint = GoldPrimary)
                            }
                        }
                    } else if (currentStep == 2) {
                        IconButton(onClick = onRandomizeAiNotes, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Casino, contentDescription = "Случайные примечания", tint = GoldPrimary)
                        }
                    } else {
                        IconButton(onClick = onRandomizeHero, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Casino, contentDescription = "Случайный герой", tint = GoldPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Body content based on step
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentStep) {
                        1 -> Step1WorldCreation(
                            worldName = worldName,
                            genre = genre,
                            worldLore = worldLore,
                            worldMap = worldMap,
                            keyNpcs = keyNpcs,
                            sapientRaces = sapientRaces,
                            beasts = beasts,
                            onWorldNameChange = onWorldNameChange,
                            onGenreChange = onGenreChange,
                            onWorldLoreChange = onWorldLoreChange,
                            onWorldMapChange = onWorldMapChange,
                            onKeyNpcsChange = onKeyNpcsChange,
                            onSapientRacesChange = onSapientRacesChange,
                            onBeastsChange = onBeastsChange
                        )
                        2 -> Step2AiNotes(
                            aiNotes = aiNotes,
                            onAiNotesChange = onAiNotesChange
                        )
                        3 -> Step3HeroCreation(
                            heroName = heroName,
                            zeroGifts = zeroGifts,
                            onHeroNameChange = onHeroNameChange,
                            onZeroGiftsChange = onZeroGiftsChange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom navigation bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentStep > 1) onStepChange(currentStep - 1) else onBackToMenu()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF181A20))
                    ) {
                        Text(if (currentStep > 1) "⬅️ Назад" else "🚪 В меню", color = FantasyText)
                    }

                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                onStepChange(currentStep + 1)
                            } else {
                                onStartAdventure()
                            }
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        val label = when (currentStep) {
                            1 -> "ДАЛЕЕ: ПРАВИЛА ИИ ➔"
                            2 -> "ДАЛЕЕ: ГЕРОЙ И ДАРЫ ➔"
                            else -> "⚔️ НАЧАТЬ ПРИКЛЮЧЕНИЕ"
                        }
                        Text(label, fontWeight = FontWeight.Bold, color = GoldLight)
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1WorldCreation(
    worldName: String,
    genre: String,
    worldLore: String,
    worldMap: String,
    keyNpcs: String,
    sapientRaces: String,
    beasts: String,
    onWorldNameChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onWorldLoreChange: (String) -> Unit,
    onWorldMapChange: (String) -> Unit,
    onKeyNpcsChange: (String) -> Unit,
    onSapientRacesChange: (String) -> Unit,
    onBeastsChange: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📜 Лор", "🗺️ Карта", "👑 NPC", "🧝 Расы", "🐉 Монстры")

    Column(modifier = Modifier.fillMaxSize()) {
        // Genre toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isClassic = genre == "Классическое фэнтези"
            Button(
                onClick = { onGenreChange("Классическое фэнтези") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isClassic) CrimsonPrimary else Color(0xFF1D1E26)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Классическое фэнтези", fontSize = 11.sp, color = if (isClassic) GoldLight else FantasyTextDim)
            }

            Button(
                onClick = { onGenreChange("Тёмное фэнтези") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isClassic) CrimsonPrimary else Color(0xFF1D1E26)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Тёмное фэнтези", fontSize = 11.sp, color = if (!isClassic) GoldLight else FantasyTextDim)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = worldName,
            onValueChange = onWorldNameChange,
            label = { Text("Название мира") },
            placeholder = { Text("Например: Кольцевые Земли: Раскол Внутреннего Моря...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF090A0F),
            contentColor = GoldPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        when (selectedTab) {
            0 -> OutlinedTextField(
                value = worldLore,
                onValueChange = onWorldLoreChange,
                label = { Text("Общий лор и история мира") },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            )
            1 -> OutlinedTextField(
                value = worldMap,
                onValueChange = onWorldMapChange,
                label = { Text("Регионы и расстояния в днях пути") },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            )
            2 -> OutlinedTextField(
                value = keyNpcs,
                onValueChange = onKeyNpcsChange,
                label = { Text("Важные сюжетные NPC мира") },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            )
            3 -> OutlinedTextField(
                value = sapientRaces,
                onValueChange = onSapientRacesChange,
                label = { Text("Разумные расы и народы") },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            )
            4 -> OutlinedTextField(
                value = beasts,
                onValueChange = onBeastsChange,
                label = { Text("Бестиарий и монстры") },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun Step2AiNotes(
    aiNotes: String,
    onAiNotesChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "СКРЫТЫЕ ПРАВИЛА И ДИРЕКТИВЫ ДЛЯ МАСТЕРА (ИИ):",
            color = GoldPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "NPC не читают эти правила заранее. Задайте тон мира, уровень сложности боев, зрелость и стиль повествования.",
            color = FantasyTextDim,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = aiNotes,
            onValueChange = onAiNotesChange,
            placeholder = { Text("Правила, например: умные враги, реалистичные ранения, без нравоучений...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )
    }
}

@Composable
private fun Step3HeroCreation(
    heroName: String,
    zeroGifts: String,
    onHeroNameChange: (String) -> Unit,
    onZeroGiftsChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = heroName,
            onValueChange = onHeroNameChange,
            label = { Text("Имя героя") },
            placeholder = { Text("Например: Селеста, Элрион...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ИЗНАЧАЛЬНЫЕ ДАРЫ ЗЕРО:",
            color = GoldPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Особая способность, оружие или артефакт, полученный при пробуждении в этом мире.",
            color = FantasyTextDim,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = zeroGifts,
            onValueChange = onZeroGiftsChange,
            placeholder = { Text("Опишите силу, оружие или артефакт...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}
