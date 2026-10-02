package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.dialogs.ApiKeyGuideDialog
import com.example.ui.dialogs.ApiKeysDialog
import com.example.ui.dialogs.FinishedWorldDialog
import com.example.ui.dialogs.SavesListDialog
import com.example.ui.dialogs.TemplatesDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.WorldCreationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val config by viewModel.config.collectAsState()
                val creationStep by viewModel.creationStep.collectAsState()

                val wizWorldName by viewModel.wizWorldName.collectAsState()
                val wizGenre by viewModel.wizGenre.collectAsState()
                val wizWorldLore by viewModel.wizWorldLore.collectAsState()
                val wizWorldMap by viewModel.wizWorldMap.collectAsState()
                val wizKeyNpcs by viewModel.wizKeyNpcs.collectAsState()
                val wizSapientRaces by viewModel.wizSapientRaces.collectAsState()
                val wizBeasts by viewModel.wizBeasts.collectAsState()
                val wizAiNotes by viewModel.wizAiNotes.collectAsState()
                val wizHeroName by viewModel.wizHeroName.collectAsState()
                val wizZeroGifts by viewModel.wizZeroGifts.collectAsState()

                val showKeys by viewModel.showKeysDialog.collectAsState()
                val showSaves by viewModel.showSavesDialog.collectAsState()
                val showTemplates by viewModel.showTemplatesDialog.collectAsState()
                val showKeyGuide by viewModel.showApiKeyGuideDialog.collectAsState()
                val savedSessions by viewModel.savedSessions.collectAsState()
                val savedTemplates by viewModel.savedTemplates.collectAsState()
                val finishedWorld by viewModel.showFinishedWorldDialog.collectAsState()
                val infoMsg by viewModel.infoMessage.collectAsState()
                val errorMsg by viewModel.errorMessage.collectAsState()

                // Back navigation handler
                BackHandler(enabled = currentScreen != Screen.MainMenu) {
                    when (currentScreen) {
                        Screen.WorldCreation -> {
                            if (creationStep > 1) {
                                viewModel.setCreationStep(creationStep - 1)
                            } else {
                                viewModel.navigateTo(Screen.MainMenu)
                            }
                        }
                        Screen.Game -> {
                            viewModel.navigateTo(Screen.MainMenu)
                        }
                        Screen.MainMenu -> {
                            // Let system handle exit
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.statusBars
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                        when (currentScreen) {
                            Screen.MainMenu -> {
                                MainMenuScreen(
                                    selectedModel = config.model,
                                    apiKeysCount = config.apiKeys.size,
                                    validKeysCount = config.apiKeys.count { it.isNotBlank() },
                                    onModelSelected = { viewModel.setSelectedModel(it) },
                                    onOpenKeys = { viewModel.showKeysDialog.value = true },
                                    onOpenKeyGuide = { viewModel.showApiKeyGuideDialog.value = true },
                                    onNewGame = { viewModel.startCreationFlow() },
                                    onOpenSaves = {
                                        viewModel.refreshSavesList()
                                        viewModel.showSavesDialog.value = true
                                    },
                                    onOpenTemplates = {
                                        viewModel.refreshTemplatesList()
                                        viewModel.showTemplatesDialog.value = true
                                    }
                                )
                            }
                            Screen.WorldCreation -> {
                                WorldCreationScreen(
                                    currentStep = creationStep,
                                    worldName = wizWorldName,
                                    genre = wizGenre,
                                    worldLore = wizWorldLore,
                                    worldMap = wizWorldMap,
                                    keyNpcs = wizKeyNpcs,
                                    sapientRaces = wizSapientRaces,
                                    beasts = wizBeasts,
                                    aiNotes = wizAiNotes,
                                    heroName = wizHeroName,
                                    zeroGifts = wizZeroGifts,
                                    onWorldNameChange = { viewModel.wizWorldName.value = it },
                                    onGenreChange = { viewModel.wizGenre.value = it },
                                    onWorldLoreChange = { viewModel.wizWorldLore.value = it },
                                    onWorldMapChange = { viewModel.wizWorldMap.value = it },
                                    onKeyNpcsChange = { viewModel.wizKeyNpcs.value = it },
                                    onSapientRacesChange = { viewModel.wizSapientRaces.value = it },
                                    onBeastsChange = { viewModel.wizBeasts.value = it },
                                    onAiNotesChange = { viewModel.wizAiNotes.value = it },
                                    onHeroNameChange = { viewModel.wizHeroName.value = it },
                                    onZeroGiftsChange = { viewModel.wizZeroGifts.value = it },
                                    onRandomizeWorld = { viewModel.randomizeWorldWizard() },
                                    onRandomizeAiNotes = { viewModel.randomizeAiNotesWizard() },
                                    onRandomizeHero = { viewModel.randomizeHeroWizard() },
                                    onSaveTemplate = { viewModel.saveCurrentWorldAsTemplate() },
                                    onOpenTemplates = {
                                        viewModel.refreshTemplatesList()
                                        viewModel.showTemplatesDialog.value = true
                                    },
                                    onStepChange = { viewModel.setCreationStep(it) },
                                    onStartAdventure = { viewModel.startGameSession() },
                                    onBackToMenu = { viewModel.navigateTo(Screen.MainMenu) }
                                )
                            }
                            Screen.Game -> {
                                GameScreen(viewModel = viewModel)
                            }
                        }

                        // Global Modals accessible from Main Menu
                        if (showKeys) {
                            ApiKeysDialog(
                                initialKeys = config.apiKeys,
                                currentKeyIdx = viewModel.currentKeyIndex.collectAsState().value,
                                onSaveKeys = { viewModel.updateKeysPool(it) },
                                onDismiss = { viewModel.showKeysDialog.value = false }
                            )
                        }

                        if (showKeyGuide) {
                            ApiKeyGuideDialog(
                                onDismiss = { viewModel.showApiKeyGuideDialog.value = false },
                                onOpenKeysInput = { viewModel.showKeysDialog.value = true }
                            )
                        }

                        if (showSaves) {
                            SavesListDialog(
                                saves = savedSessions,
                                onLoadSave = { viewModel.loadGame(it) },
                                onDeleteSave = { viewModel.deleteSave(it) },
                                onDismiss = { viewModel.showSavesDialog.value = false }
                            )
                        }

                        if (showTemplates) {
                            TemplatesDialog(
                                templates = savedTemplates,
                                onApplyTemplate = { viewModel.applyTemplateToWizard(it) },
                                onDeleteTemplate = { viewModel.deleteTemplate(it) },
                                onDismiss = { viewModel.showTemplatesDialog.value = false }
                            )
                        }

                        finishedWorld?.let { finishedSession ->
                            FinishedWorldDialog(
                                session = finishedSession,
                                onDismiss = { viewModel.showFinishedWorldDialog.value = null }
                            )
                        }

                        // Info Alert Dialog
                        infoMsg?.let { msg ->
                            AlertDialog(
                                onDismissRequest = { viewModel.infoMessage.value = null },
                                confirmButton = {
                                    TextButton(onClick = { viewModel.infoMessage.value = null }) {
                                        Text("Отлично")
                                    }
                                },
                                title = { Text("Информация") },
                                text = { Text(msg) }
                            )
                        }

                        // Error Alert Dialog
                        errorMsg?.let { msg ->
                            AlertDialog(
                                onDismissRequest = { viewModel.errorMessage.value = null },
                                confirmButton = {
                                    TextButton(onClick = { viewModel.errorMessage.value = null }) {
                                        Text("Понятно")
                                    }
                                },
                                title = { Text("Внимание") },
                                text = { Text(msg) }
                            )
                        }
                    }
                }
            }
        }
    }
}
