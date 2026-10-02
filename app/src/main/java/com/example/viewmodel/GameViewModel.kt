package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

enum class Screen {
    MainMenu,
    WorldCreation,
    Game
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = StorageManager(application)
    private val geminiClient = GeminiApiClient()

    // Config
    private val _config = MutableStateFlow(storage.loadConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    // Navigation & Screen state
    private val _currentScreen = MutableStateFlow(Screen.MainMenu)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _creationStep = MutableStateFlow(1)
    val creationStep: StateFlow<Int> = _creationStep.asStateFlow()

    // Current Game Session
    private val _session = MutableStateFlow(GameSession())
    val session: StateFlow<GameSession> = _session.asStateFlow()

    // Active key index in pool
    private val _currentKeyIndex = MutableStateFlow(0)
    val currentKeyIndex: StateFlow<Int> = _currentKeyIndex.asStateFlow()

    // Loading / turn status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // Dialog Visibilities
    val showKeysDialog = MutableStateFlow(false)
    val showSavesDialog = MutableStateFlow(false)
    val showTemplatesDialog = MutableStateFlow(false)
    val showAbilitiesDialog = MutableStateFlow(false)
    val showMapDialog = MutableStateFlow(false)
    val showMemoryVaultDialog = MutableStateFlow(false)
    val showTurnCompressionDialog = MutableStateFlow(false)
    val showGmCorrectionDialog = MutableStateFlow(false)
    val showNpcDialogueDialog = MutableStateFlow(false)
    val npcDialogueDefaultTarget = MutableStateFlow<String?>(null)
    val showCodexDialog = MutableStateFlow(false)
    val showFinishedWorldDialog = MutableStateFlow<GameSession?>(null)
    val showArtFullscreen = MutableStateFlow(false)
    val showApiKeyGuideDialog = MutableStateFlow(false)
    val infoMessage = MutableStateFlow<String?>(null)
    val errorMessage = MutableStateFlow<String?>(null)

    // Saved Lists
    private val _savedSessions = MutableStateFlow<List<GameSession>>(emptyList())
    val savedSessions: StateFlow<List<GameSession>> = _savedSessions.asStateFlow()

    private val _savedTemplates = MutableStateFlow<List<WorldTemplate>>(emptyList())
    val savedTemplates: StateFlow<List<WorldTemplate>> = _savedTemplates.asStateFlow()

    // World creation wizard temp fields
    val wizWorldName = MutableStateFlow("")
    val wizGenre = MutableStateFlow("Классическое фэнтези")
    val wizWorldLore = MutableStateFlow("")
    val wizWorldMap = MutableStateFlow("")
    val wizKeyNpcs = MutableStateFlow("")
    val wizSapientRaces = MutableStateFlow("")
    val wizBeasts = MutableStateFlow("")
    val wizAiNotes = MutableStateFlow("")
    val wizHeroName = MutableStateFlow("")
    val wizZeroGifts = MutableStateFlow("")
    val wizStoryMemories = MutableStateFlow<List<StoryMemory>>(emptyList())

    init {
        refreshSavesList()
        refreshTemplatesList()
        randomizeWorldWizard()
        randomizeAiNotesWizard()
        randomizeHeroWizard()
        val loadedConfig = _config.value
        val cleanKeys = loadedConfig.apiKeys.filter { it.isNotBlank() }
        val updated = loadedConfig.copy(
            apiKeys = cleanKeys,
            model = loadedConfig.model.ifBlank { "gemini-2.5-flash" }
        )
        _config.value = updated
        storage.saveConfig(updated)
    }

    fun refreshSavesList() {
        _savedSessions.value = storage.loadAllSessions()
    }

    fun refreshTemplatesList() {
        _savedTemplates.value = storage.loadAllTemplates()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Config & API Keys ---
    fun updateKeysPool(keys: List<String>) {
        val updated = _config.value.copy(apiKeys = keys.filter { it.isNotBlank() })
        _config.value = updated
        storage.saveConfig(updated)
        if (_currentKeyIndex.value >= updated.apiKeys.size) {
            _currentKeyIndex.value = 0
        }
    }

    fun setSelectedModel(model: String) {
        val updated = _config.value.copy(model = model)
        _config.value = updated
        storage.saveConfig(updated)
        _session.value = _session.value.copy(currentModel = model)
        appendStoryText("\n[🔄 Модель переключена на: $model]\n")
        autoSave()
    }

    // --- Wizard Helpers ---
    fun startCreationFlow() {
        val validKeys = _config.value.apiKeys.filter { it.isNotBlank() }
        if (validKeys.isEmpty() && com.example.BuildConfig.GEMINI_API_KEY.isEmpty()) {
            errorMessage.value = "Укажите хотя бы один API-ключ Gemini в настройках!"
            showKeysDialog.value = true
            return
        }
        _creationStep.value = 1
        _currentScreen.value = Screen.WorldCreation
    }

    fun setCreationStep(step: Int) {
        _creationStep.value = step
    }

    fun randomizeWorldWizard() {
        val pool = if (wizGenre.value == "Классическое фэнтези") CLASSIC_WORLDS else DARK_WORLDS
        val picked = pool.random()
        wizWorldName.value = picked.first
        wizWorldLore.value = picked.second
        if (wizWorldMap.value.isBlank()) {
            wizWorldMap.value = "Пример круговой карты:\n• СЕВЕР (Сапихом): королевство людей, замки и пашни.\n• ЗАПАД (Самрэйл): волшебные леса эльфов.\n• ВОСТОК (Дворфы): каньоны и твердыни Гринда.\n• ЮГ (Асма): саванны зверолюдей.\n• ЦЕНТР (Даймон): закрытый остров демонов посреди Внутреннего Моря."
        }
    }

    fun randomizeAiNotesWizard() {
        wizAiNotes.value = AI_NOTES_PRESETS.random()
    }

    fun randomizeHeroWizard() {
        val pool = if (wizGenre.value == "Классическое фэнтези") CLASSIC_HEROES else DARK_HEROES
        val picked = pool.random()
        wizHeroName.value = picked.first
        wizZeroGifts.value = picked.second
    }

    fun applyTemplateToWizard(tpl: WorldTemplate) {
        wizWorldName.value = tpl.worldName
        wizGenre.value = tpl.genre
        wizWorldLore.value = tpl.worldLore
        wizWorldMap.value = tpl.worldMap
        wizKeyNpcs.value = tpl.keyNpcs
        wizSapientRaces.value = tpl.sapientRaces
        wizBeasts.value = tpl.beastsAndMonsters
        wizAiNotes.value = tpl.aiSpecialNotes
        wizStoryMemories.value = tpl.storyMemories
        infoMessage.value = "Шаблон «${tpl.worldName}» применён!"
    }

    fun saveCurrentWorldAsTemplate() {
        val name = wizWorldName.value.trim()
        val lore = wizWorldLore.value.trim()
        if (name.isBlank() || lore.isBlank()) {
            errorMessage.value = "Заполните название и лор мира перед сохранением шаблона!"
            return
        }
        val tpl = WorldTemplate(
            worldName = name,
            genre = wizGenre.value,
            worldLore = lore,
            worldMap = wizWorldMap.value,
            keyNpcs = wizKeyNpcs.value,
            sapientRaces = wizSapientRaces.value,
            beastsAndMonsters = wizBeasts.value,
            aiSpecialNotes = wizAiNotes.value,
            storyMemories = wizStoryMemories.value
        )
        storage.saveTemplate(tpl)
        refreshTemplatesList()
        infoMessage.value = "Шаблон «$name» успешно сохранён!"
    }

    fun deleteTemplate(id: String) {
        storage.deleteTemplate(id)
        refreshTemplatesList()
    }

    // --- Starting Game Session ---
    fun startGameSession() {
        val hero = wizHeroName.value.trim()
        val gifts = wizZeroGifts.value.trim()
        if (hero.isBlank() || gifts.isBlank()) {
            errorMessage.value = "Заполните имя героя и Дары Зеро!"
            return
        }

        val newSession = GameSession(
            id = UUID.randomUUID().toString(),
            worldName = wizWorldName.value.trim(),
            worldLore = wizWorldLore.value.trim(),
            worldMap = wizWorldMap.value.trim(),
            keyNpcs = wizKeyNpcs.value.trim(),
            sapientRaces = wizSapientRaces.value.trim(),
            beastsAndMonsters = wizBeasts.value.trim(),
            genre = wizGenre.value,
            aiSpecialNotes = wizAiNotes.value.trim(),
            heroName = hero,
            zeroGifts = gifts,
            currentModel = geminiClient.resolveModel(_config.value.model),
            storyMemories = wizStoryMemories.value.toMutableList(),
            currentDay = 1,
            currentTimeOfDay = "Утро",
            heroStatus = "В добром здравии (Невредим)",
            totalTurnsCount = 0
        )

        _session.value = newSession
        _currentScreen.value = Screen.Game
        appendStoryText("[✨ Летопись мира «${newSession.worldName}» открыта. Герой ${newSession.heroName} ступает на свой путь...]")

        val sysPrompt = GameEngine.buildSystemInstruction(newSession)
        var startText = "$sysPrompt\n\nНачни историю строго по лору мира '${newSession.worldName}'. Наступает День 1, Утро. Опиши пробуждение героя ${newSession.heroName}, окружающую обстановку и первую интригу. Если герой начинает в конкретном городе или замке, нанеси его на карту тегом [MAP_PIN]."
        if (newSession.aiSpecialNotes.isNotBlank()) {
            startText += "\n\n[ТАЙНЫЕ ДИРЕКТИВЫ НА ЭТОТ ХОД]:\n${newSession.aiSpecialNotes}"
        }

        newSession.chatHistory.add(ChatMessage(role = "user", text = startText))
        requestGeminiTurn(isFinal = false)
    }

    fun loadGame(s: GameSession) {
        if (s.isGameEnded) {
            showFinishedWorldDialog.value = s
            return
        }
        _session.value = s
        _currentScreen.value = Screen.Game
        appendStoryText("[📂 Мир «${s.worldName}» загружен! Шёл День ${s.currentDay} (${s.currentTimeOfDay})]")
    }

    fun deleteSave(id: String) {
        storage.deleteSession(id)
        refreshSavesList()
    }

    // --- Turn Execution ---
    fun sendPlayerAction(rawText: String, mode: String) {
        if (_isLoading.value || _session.value.isGameEnded || _session.value.isHeroDead) return
        val text = rawText.trim()
        if (text.isBlank()) return
        _isLoading.value = true

        val s = _session.value
        val turnCount = s.totalTurnsCount + 1

        val playerMsg = when {
            mode.contains("Толпу") -> "[Обращение ${s.heroName} к толпе]: «$text»"
            mode.contains("Диалог") -> "[Слова ${s.heroName}]: «$text»"
            else -> "[Действие ${s.heroName}]: $text"
        }

        appendStoryText("➤ $playerMsg")

        // Clean previous directives from last user message
        for (i in s.chatHistory.indices.reversed()) {
            if (s.chatHistory[i].role == "user") {
                val cleaned = s.chatHistory[i].text
                    .replace(Regex("\\n\\n\\[ТАЙНЫЕ ДИРЕКТИВЫ НА ЭТОТ ХОД.*?\\]", RegexOption.DOT_MATCHES_ALL), "")
                    .replace(Regex("\\n\\n\\[ГЛАВНАЯ ИНСТРУКЦИЯ МАСТЕРУ НА ЭТОТ ХОД.*?\\]", RegexOption.DOT_MATCHES_ALL), "")
                s.chatHistory[i] = s.chatHistory[i].copy(text = cleaned)
                break
            }
        }

        var aiPayload = playerMsg
        aiPayload += """
            
            [ГЛАВНАЯ ИНСТРУКЦИЯ МАСТЕРУ НА ЭТОТ ХОД:
            1. Отвечай живым литературным сюжетом. Объём свободный и адаптивный: кратко (хоть 1 абзац) для простых действий, подробно для важных сцен.
            2. ЛОГИКА ВРЕМЕНИ: Оцени реальное время действия игрока ('$text'). Если игрок ждёт до вечера/ночи/утра или отдыхает — обнови день и время суток. Тег времени в конце: [TIME: день, "фаза"].
            3. Теги [TIME], [STATUS], [IMAGE] выведи СТРОГО В САМОМ КОНЦЕ ПОСЛЕ ВСЕГО ТЕКСТА! Запрещено отвечать одними тегами!]
        """.trimIndent()

        if (s.aiSpecialNotes.isNotBlank()) {
            aiPayload += "\n\n[ТАЙНЫЕ ДИРЕКТИВЫ НА ЭТОТ ХОД (ТОЛЬКО ДЛЯ DM)]:\n${s.aiSpecialNotes}"
        }

        if (s.chatHistory.isNotEmpty() && s.chatHistory.last().role == "user") {
            val last = s.chatHistory.removeAt(s.chatHistory.size - 1)
            s.chatHistory.add(ChatMessage("user", "${last.text}\n\n$aiPayload"))
        } else {
            s.chatHistory.add(ChatMessage("user", aiPayload))
        }

        _session.value = _session.value.copy(totalTurnsCount = turnCount)
        compressMemoryIfNeeded()
        requestGeminiTurn(isFinal = false)
    }

    fun triggerEndGame() {
        if (_isLoading.value || _session.value.isGameEnded) return
        val s = _session.value
        _session.value = s.copy(isGameEnded = true)

        appendStoryText("\n═════════════ ЗАВЕРШЕНИЕ ПУТИ ═════════════\n[Судьба взвешивает деяния героя на беспристрастных весах истории...]\n")

        val deathNote = if (s.isHeroDead) "ГЕРОЙ ПОГИБ В ХОДЕ ПРИКЛЮЧЕНИЯ!" else "Герой дошёл до финала живым."
        val finalePrompt = """
            Игрок завершает историю в мире '${s.worldName}' (${s.genre}).
            СТАТУС: $deathNote
            ПРОЖИТО ДНЕЙ В МИРЕ: ${s.currentDay}.
            ОТКРЫТО ТОЧЕК НА КАРТЕ: ${s.mapMarkers.size}.
            КОЛИЧЕСТВО СОВЕРШЕННЫХ ХОДОВ: ${s.totalTurnsCount}.

            ТВОЯ ЗАДАЧА:
            1. Напиши масштабный эпилог о последствиях поступков героя для всего мира (не менее 3–4 абзацев).
            2. БЕСПРИСТРАСТНО ОЦЕНИ РЕАЛЬНОЕ ВЛИЯНИЕ ГЕРОЯ ПО ШКАЛЕ ОТ 1 ДО 10 В ТЕГЕ: [SCORE: число].
            3. Напиши краткое изложение наследия героя и мира в теге: [SUMMARY: текст резюме].
            4. Добавь финальный тег картины [IMAGE: ...].
        """.trimIndent()

        if (s.chatHistory.isNotEmpty() && s.chatHistory.last().role == "user") {
            val last = s.chatHistory.removeAt(s.chatHistory.size - 1)
            s.chatHistory.add(ChatMessage("user", "${last.text}\n\n$finalePrompt"))
        } else {
            s.chatHistory.add(ChatMessage("user", finalePrompt))
        }

        requestGeminiTurn(isFinal = true)
    }

    private fun requestGeminiTurn(isFinal: Boolean) {
        _isLoading.value = true
        _statusMessage.value = "Мастер сплетает нити судьбы..."

        viewModelScope.launch {
            val s = _session.value
            val result = geminiClient.generateContent(
                modelName = s.currentModel,
                keysPool = _config.value.apiKeys,
                startKeyIndex = _currentKeyIndex.value,
                chatHistory = s.chatHistory,
                onStatusUpdate = { msg ->
                    _statusMessage.value = msg
                }
            )

            when (result) {
                is GeminiResult.Success -> {
                    _currentKeyIndex.value = result.activeKeyIndex
                    handleGeminiResponse(result.text, isFinal)
                }
                is GeminiResult.Error -> {
                    appendStoryText("[❌ ${result.message}]")
                    _isLoading.value = false
                    _statusMessage.value = ""
                }
                is GeminiResult.KeyWarning -> {
                    // Handled inside callback
                }
            }
        }
    }

    private fun handleGeminiResponse(rawText: String, isFinal: Boolean) {
        val s = _session.value
        val systemNotifications = mutableListOf<String>()

        var cleanText = rawText

        // 1. Time / Day
        var (timeRaw, rem1) = GameEngine.extractBracketTag(cleanText, "TIME")
        if (timeRaw == null) {
            val res = GameEngine.extractBracketTag(rem1, "DAY")
            timeRaw = res.first
            rem1 = res.second
        }
        cleanText = rem1

        var updatedDay = s.currentDay
        var updatedTimeOfDay = s.currentTimeOfDay
        if (!timeRaw.isNullOrBlank()) {
            val dayMatch = Regex("(\\d+)").find(timeRaw)
            if (dayMatch != null) {
                val parsedDay = dayMatch.groupValues[1].toIntOrNull() ?: updatedDay
                updatedDay = maxOf(updatedDay, parsedDay)
            }
            for (phase in listOf("Ночь", "Вечер", "День", "Утро")) {
                if (timeRaw.contains(phase, ignoreCase = true)) {
                    updatedTimeOfDay = phase
                    break
                }
            }
        }

        // 2. Status
        val (statusRaw, rem2) = GameEngine.extractBracketTag(cleanText, "STATUS")
        cleanText = rem2
        val updatedStatus = if (!statusRaw.isNullOrBlank()) {
            statusRaw.trim('"', '\'')
        } else s.heroStatus

        // 3. Map Marker / Pin
        var (pinRaw, rem3) = GameEngine.extractBracketTag(cleanText, "MAP_PIN")
        if (pinRaw == null) {
            val res = GameEngine.extractBracketTag(rem3, "MAP_MARKER")
            pinRaw = res.first
            rem3 = res.second
        }
        cleanText = rem3

        if (!pinRaw.isNullOrBlank()) {
            try {
                val cleanJson = pinRaw.replace(Regex("^```(?:json)?\\s*", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("\\s*```$"), "").trim()
                val json = JSONObject(cleanJson)
                val pName = json.optString("name", "").trim()
                val pReg = json.optString("region", "Дикие земли").trim()
                val pType = json.optString("type", "локация").trim()
                val px = json.optDouble("x", -1.0)
                val py = json.optDouble("y", -1.0)
                if (pName.isNotBlank()) {
                    addMapMarker(pName, pReg, pType, if (px >= 0) px.toFloat() else null, if (py >= 0) py.toFloat() else null)
                    systemNotifications.add("🗺️ [На карту нанесена новая отметка: «$pName» ($pType)]")
                }
            } catch (_: Exception) {}
        }

        // 4. Abilities
        val (abRaw, rem4) = GameEngine.extractBracketTag(cleanText, "ABILITIES")
        cleanText = rem4
        if (!abRaw.isNullOrBlank()) {
            val cleanAb = abRaw.replace(Regex("[\\[\\]\"']"), "")
            cleanAb.split(",").forEach { item ->
                if (item.isNotBlank()) {
                    GameEngine.addAbilityIfUnique(s.acquiredAbilities, item.trim(), s.zeroGifts)
                }
            }
        }

        val (newAbRaw, rem5) = GameEngine.extractBracketTag(cleanText, "NEW_ABILITY")
        cleanText = rem5
        if (!newAbRaw.isNullOrBlank()) {
            if (GameEngine.addAbilityIfUnique(s.acquiredAbilities, newAbRaw.trim(), s.zeroGifts)) {
                systemNotifications.add("✨ [Изучена новая сила: ${newAbRaw.trim()}]")
            }
        }

        // 5. Score & Summary
        val (scoreVal, rem6) = GameEngine.extractBracketTag(cleanText, "SCORE")
        val (summaryVal, rem7) = GameEngine.extractBracketTag(rem6, "SUMMARY")
        cleanText = rem7

        var finalScore = s.finalScore
        var finalSummary = s.finalSummary
        if (isFinal) {
            if (!scoreVal.isNullOrBlank()) {
                val numMatch = Regex("(\\d+(?:\\.\\d+)?)").find(scoreVal)
                if (numMatch != null) {
                    var num = numMatch.groupValues[1].toFloatOrNull() ?: 5.0f
                    if (num > 10.0f) num /= 10.0f
                    if (s.isHeroDead && num > 4.5f) num = 3.5f
                    finalScore = String.format(Locale.US, "%.1f", num)
                }
            }
            if (!summaryVal.isNullOrBlank()) {
                finalSummary = summaryVal
            }
        }

        // 6. Hero Death
        val hasRevive = listOf("воскреш", "возрожд", "перерожд", "бессмерт", "реинкарнац", "феникс", "вторая жизнь", "неумира")
            .any { s.zeroGifts.contains(it, ignoreCase = true) }

        val (deathFlag, rem8) = GameEngine.extractBracketTag(cleanText, "HERO_DEATH")
        cleanText = rem8

        var heroDead = s.isHeroDead
        var gameEnded = s.isGameEnded || isFinal
        var heroFinalStatus = updatedStatus

        if (deathFlag != null && deathFlag.contains("true", ignoreCase = true)) {
            if (!hasRevive) {
                heroDead = true
                gameEnded = true
                heroFinalStatus = "💀 Погиб в бою"
            } else {
                heroFinalStatus = "✨ Воскрес (Сила пробуждена)"
            }
        }

        // 7. Codex
        val (codexRaw, rem9) = GameEngine.extractBracketTag(cleanText, "CODEX")
        cleanText = rem9
        if (!codexRaw.isNullOrBlank()) {
            try {
                val cleanJson = codexRaw.replace(Regex("^```(?:json)?\\s*", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("\\s*```$"), "").trim()
                val obj = JSONObject(cleanJson)
                val locs = obj.optJSONArray("locations")
                if (locs != null) {
                    for (i in 0 until locs.length()) {
                        val loc = GameEngine.cleanNpcName(locs.optString(i))
                        if (loc.isNotBlank() && s.codexData.locations.none { it.equals(loc, ignoreCase = true) }) {
                            s.codexData.locations.add(loc)
                        }
                    }
                }
                val deads = obj.optJSONArray("npcs_dead")
                if (deads != null) {
                    for (i in 0 until deads.length()) {
                        val d = GameEngine.cleanNpcName(deads.optString(i))
                        if (d.isNotBlank() && s.codexData.npcsDead.none { it.equals(d, ignoreCase = true) }) {
                            s.codexData.npcsDead.add(d)
                        }
                    }
                }
                val nearbys = obj.optJSONArray("npcs_nearby")
                if (nearbys != null) {
                    val currentNearby = mutableListOf<String>()
                    for (i in 0 until nearbys.length()) {
                        val n = GameEngine.cleanNpcName(nearbys.optString(i))
                        if (n.isNotBlank()) {
                            val isDead = s.codexData.npcsDead.any { it.equals(n, ignoreCase = true) }
                            if (!isDead && currentNearby.none { it.equals(n, ignoreCase = true) }) {
                                currentNearby.add(n)
                            }
                            if (s.codexData.npcsAll.none { it.equals(n, ignoreCase = true) }) {
                                s.codexData.npcsAll.add(n)
                            }
                        }
                    }
                    s.codexData.npcsNearby.clear()
                    s.codexData.npcsNearby.addAll(currentNearby)
                }
                val notes = obj.optJSONArray("notes")
                if (notes != null) {
                    for (i in 0 until notes.length()) {
                        val note = notes.optString(i)
                        if (note.isNotBlank() && !s.codexData.notes.contains(note)) {
                            s.codexData.notes.add(note)
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 8. Image Prompt
        val (imageRaw, rem10) = GameEngine.extractBracketTag(cleanText, "IMAGE")
        cleanText = rem10
        val imagePrompt = if (!imageRaw.isNullOrBlank()) {
            imageRaw.trim()
        } else {
            "dark fantasy scene in ${s.worldName}, dramatic cinematic lighting, masterpiece 8k"
        }
        val imageUrl = ImageApiClient.buildFantasyImageUrl(imagePrompt, s.genre)

        var display = GameEngine.cleanResidualBracketsAndTags(cleanText).trim()
        if (display.isBlank() && rawText.isNotBlank()) {
            display = GameEngine.cleanResidualBracketsAndTags(rawText).trim()
        }

        s.chatHistory.add(ChatMessage(role = "model", text = display.ifBlank { rawText }))

        if (display.isNotBlank()) {
            appendStoryText(display)
        }

        for (note in systemNotifications) {
            appendStoryText(note)
        }

        if (isFinal && !finalScore.isNullOrBlank()) {
            appendStoryText("⭐ [ОЦЕНКА ВЛИЯНИЯ НА МИР: $finalScore / 10]")
        }

        if (heroDead) {
            appendStoryText("💀 ══════════ ГЕРОЙ ПОГИБ ══════════ 💀")
        } else if (gameEnded) {
            appendStoryText("═════════════ ИСТОРИЯ ЗАВЕРШЕНА ═════════════")
        }

        _session.value = _session.value.copy(
            currentDay = updatedDay,
            currentTimeOfDay = updatedTimeOfDay,
            heroStatus = heroFinalStatus,
            isHeroDead = heroDead,
            isGameEnded = gameEnded,
            finalScore = finalScore,
            finalSummary = finalSummary,
            lastImagePrompt = imagePrompt,
            lastImageUrl = imageUrl
        )

        autoSave()
        _isLoading.value = false
        _statusMessage.value = ""
    }

    // --- Story Text Helper ---
    private fun appendStoryText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val current = _session.value.storyText.trim()
        val newText = if (current.isEmpty()) trimmed else "$current\n\n$trimmed"
        _session.value = _session.value.copy(storyText = newText)
    }

    // --- Map Pins ---
    fun addMapMarker(name: String, region: String = "Дикие земли", type: String = "локация", customX: Float? = null, customY: Float? = null) {
        val s = _session.value
        if (name.isBlank()) return
        if (s.mapMarkers.any { it.name.equals(name, ignoreCase = true) }) return

        val (x, y) = if (customX != null && customY != null) {
            Pair(customX.coerceIn(0.08f, 0.92f), customY.coerceIn(0.08f, 0.92f))
        } else {
            val combo = (region + " " + name).lowercase()
            when {
                combo.contains("север") || combo.contains("сапихом") || combo.contains("замок") ->
                    Pair(0.50f + Random.nextFloat() * 0.16f - 0.08f, 0.22f + Random.nextFloat() * 0.12f - 0.06f)
                combo.contains("запад") || combo.contains("самрэйл") || combo.contains("эльф") || combo.contains("лес") ->
                    Pair(0.23f + Random.nextFloat() * 0.12f - 0.06f, 0.50f + Random.nextFloat() * 0.18f - 0.09f)
                combo.contains("восток") || combo.contains("дворф") || combo.contains("шахт") || combo.contains("каньон") ->
                    Pair(0.77f + Random.nextFloat() * 0.12f - 0.06f, 0.50f + Random.nextFloat() * 0.18f - 0.09f)
                combo.contains("юг") || combo.contains("асма") || combo.contains("зверолюд") || combo.contains("джунгл") ->
                    Pair(0.50f + Random.nextFloat() * 0.22f - 0.11f, 0.80f + Random.nextFloat() * 0.12f - 0.06f)
                combo.contains("центр") || combo.contains("даймон") || combo.contains("демон") || combo.contains("остров") ->
                    Pair(0.50f + Random.nextFloat() * 0.08f - 0.04f, 0.50f + Random.nextFloat() * 0.08f - 0.04f)
                else ->
                    Pair(0.25f + Random.nextFloat() * 0.5f, 0.25f + Random.nextFloat() * 0.5f)
            }
        }

        val marker = MapMarker(
            name = name.trim(),
            region = region.ifBlank { "Дикие земли" },
            type = type.ifBlank { "локация" }.lowercase(),
            x = x,
            y = y,
            day = s.currentDay
        )
        s.mapMarkers.add(marker)
        _session.value = _session.value.copy(mapMarkers = s.mapMarkers.toMutableList())
        autoSave()
    }

    fun deleteMapMarker(marker: MapMarker) {
        val s = _session.value
        s.mapMarkers.remove(marker)
        _session.value = _session.value.copy(mapMarkers = s.mapMarkers.toMutableList())
        autoSave()
    }

    // --- Memories & Vault ---
    fun saveLastTurnAsMemory() {
        val s = _session.value
        var lastModelText = ""
        for (i in s.chatHistory.indices.reversed()) {
            if (s.chatHistory[i].role == "model") {
                lastModelText = s.chatHistory[i].text
                break
            }
        }
        if (lastModelText.isBlank()) {
            errorMessage.value = "Нет последнего хода для сохранения!"
            return
        }
        val clean = GameEngine.cleanResidualBracketsAndTags(lastModelText)
        val shortFact = GameEngine.distillTextToShortFact(clean, maxWords = 6)
        val title = "День ${s.currentDay}: $shortFact"
        s.storyMemories.add(StoryMemory(title = title, fullText = clean, isActive = true))
        _session.value = _session.value.copy(storyMemories = s.storyMemories.toMutableList())
        autoSave()
        infoMessage.value = "Ход сохранён в Вечную Память!"
    }

    fun addCustomMemory(title: String, fullText: String) {
        val s = _session.value
        if (fullText.isBlank()) return
        val t = title.ifBlank { "Важное воспоминание" }
        s.storyMemories.add(StoryMemory(title = t, fullText = fullText.trim(), isActive = true))
        _session.value = _session.value.copy(storyMemories = s.storyMemories.toMutableList())
        autoSave()
    }

    fun toggleMemoryActive(index: Int) {
        val s = _session.value
        if (index in s.storyMemories.indices) {
            val old = s.storyMemories[index]
            s.storyMemories[index] = old.copy(isActive = !old.isActive)
            _session.value = _session.value.copy(storyMemories = s.storyMemories.toMutableList())
            autoSave()
        }
    }

    fun deleteMemory(index: Int) {
        val s = _session.value
        if (index in s.storyMemories.indices) {
            s.storyMemories.removeAt(index)
            _session.value = _session.value.copy(storyMemories = s.storyMemories.toMutableList())
            autoSave()
        }
    }

    // --- Compression ---
    fun compressMemoryIfNeeded() {
        val s = _session.value
        if (s.chatHistory.size > 64) {
            val turnsToCompress = s.chatHistory.subList(1, s.chatHistory.size - 32)
            val recentTurns = s.chatHistory.subList(s.chatHistory.size - 32, s.chatHistory.size)

            val compressedEvents = mutableListOf<String>()
            for (turn in turnsToCompress) {
                val roleName = if (turn.role == "user") "Герой" else "Мир"
                val fact = GameEngine.distillTextToShortFact(turn.text, maxWords = 10)
                if (fact.isNotBlank()) {
                    compressedEvents.add("$roleName: $fact")
                }
            }

            var newSummary = s.summaryMemory
            if (compressedEvents.isNotEmpty()) {
                val chunk = compressedEvents.joinToString("\n• ")
                newSummary = if (newSummary.isNotBlank()) "$newSummary\n• $chunk" else "• $chunk"
            }

            val updatedSession = s.copy(summaryMemory = newSummary)
            val newSysPrompt = GameEngine.buildSystemInstruction(updatedSession)
            val newHistory = mutableListOf(ChatMessage(role = "user", text = newSysPrompt))
            newHistory.addAll(recentTurns)

            _session.value = updatedSession.copy(chatHistory = newHistory)
            autoSave()
        }
    }

    fun compressSelectedTurns(chosenIndices: List<Int>) {
        val s = _session.value
        val compressedFacts = mutableListOf<String>()

        for (idx in chosenIndices) {
            if (idx in s.chatHistory.indices) {
                val turn = s.chatHistory[idx]
                val roleName = if (turn.role == "user") "Герой" else "Мир"
                val fact = GameEngine.distillTextToShortFact(turn.text, maxWords = 10)
                if (fact.isNotBlank()) {
                    compressedFacts.add("$roleName: $fact")
                }
            }
        }

        val newHistory = s.chatHistory.filterIndexed { index, _ -> !chosenIndices.contains(index) }.toMutableList()
        var newSummary = s.summaryMemory
        if (compressedFacts.isNotEmpty()) {
            val chunk = compressedFacts.joinToString("\n• ")
            newSummary = if (newSummary.isNotBlank()) "$newSummary\n• $chunk" else "• $chunk"
        }

        _session.value = s.copy(
            chatHistory = newHistory,
            summaryMemory = newSummary
        )
        autoSave()
        appendStoryText("\n[🗜️ Сжато ${chosenIndices.size} ходов в летопись по 7–10 слов. Память освобождена!]\n")
    }

    // --- GM OOC Dialogue / Correction ---
    fun executeGmRewrite(userCorrection: String) {
        val s = _session.value
        if (userCorrection.isBlank() || _isLoading.value) return

        if (s.chatHistory.isNotEmpty() && s.chatHistory.last().role == "model") {
            s.chatHistory.removeAt(s.chatHistory.size - 1)
        }

        if (s.chatHistory.isNotEmpty() && s.chatHistory.last().role == "user") {
            val lastText = s.chatHistory.last().text
                .replace(Regex("\\n\\n\\[ТРЕБОВАНИЕ ИСПРАВЛЕНИЯ.*?\\]", RegexOption.DOT_MATCHES_ALL), "")
            val directive = """
                
                [ТРЕБОВАНИЕ ИСПРАВЛЕНИЯ ОТ ИГРОКА (OOC)]: $userCorrection
                ГЛАВНОЕ: Перепиши сцену заново строго с учётом правок игрока. Сохраняй характеры NPC, не делай зомби. Служебные теги [TIME], [STATUS], [IMAGE] выведи строго в самом конце!
            """.trimIndent()
            s.chatHistory[s.chatHistory.size - 1] = ChatMessage("user", lastText + directive)
        }

        appendStoryText("\n[🛠️ Мастер переписывает сцену: «$userCorrection»...]\n")
        requestGeminiTurn(isFinal = false)
    }

    fun executeGmOocQuery(query: String) {
        val s = _session.value
        if (query.isBlank() || _isLoading.value) return

        val queryMsg = """
            [ОБРАЩЕНИЕ К МАСТЕРУ (OOC)]: $query
            Дай понятный, содержательный ответ игроку. Запрещено отвечать одними системными тегами!
        """.trimIndent()

        if (s.chatHistory.isNotEmpty() && s.chatHistory.last().role == "user") {
            val last = s.chatHistory.removeAt(s.chatHistory.size - 1)
            s.chatHistory.add(ChatMessage("user", "${last.text}\n\n$queryMsg"))
        } else {
            s.chatHistory.add(ChatMessage("user", queryMsg))
        }

        appendStoryText("\n[🎭 Вопрос Мастеру: «$query»]\n")
        requestGeminiTurn(isFinal = false)
    }

    // --- Direct NPC / Crowd Dialogue ---
    fun sendNpcDialogue(target: String, playerMessage: String, onResponse: (String) -> Unit) {
        val s = _session.value
        if (playerMessage.isBlank() || target.isBlank()) return

        if (s.codexData.npcsDead.any { it.equals(target, ignoreCase = true) }) {
            onResponse("💀 $target мёртв и не может ответить на ваши слова.")
            return
        }

        viewModelScope.launch {
            val fullStory = s.storyText.takeLast(12000)
            val notesReminder = if (s.aiSpecialNotes.isNotBlank()) "ОСОБЫЕ ПРАВИЛА МИРА: ${s.aiSpecialNotes}" else ""
            val deadStr = if (s.codexData.npcsDead.isNotEmpty()) s.codexData.npcsDead.joinToString(", ") else "Нет"
            val notesStr = if (s.codexData.notes.isNotEmpty()) s.codexData.notes.joinToString("; ") else "Нет"

            val antiMetagamingRule = """
                СТРОЖАЙШИЙ ЗАПРЕТ ВСЕВЕДЕНИЯ, ЧТЕНИЯ МЫСЛЕЙ И СДЕЛОК ЗА СПИНОЙ:
                1. ТЫ — ЖИТЕЛЬ ЭТОГО МИРА! Ты знаешь ТОЛЬКО то, что лично видел своими глазами или слышал своими ушами!
                2. ТАЙНА СДЕЛОК И ЦЕН: Если герой купил кого-то или сторговался за спиной этого персонажа — персонаж НЕ ЗНАЕТ эту сумму!
                3. ХРОНОЛОГИЯ: СЕЙЧАС ИДЕТ ДЕНЬ ${s.currentDay} (${s.currentTimeOfDay}).
                4. ПОДЧИНЕНИЕ И ДОЛГ: Если персонаж подчиняется воле героя, он НЕ становится зомби! Он сохраняет эмоции и характер, но не может ослушаться.
            """.trimIndent()

            val sysPrompt = if (target.contains("Толпа", ignoreCase = true)) {
                """
                    Ты моделируешь живую реакцию ТОЛПЫ / ОКРУЖАЮЩИХ ЛЮДЕЙ в мире '${s.worldName}' (${s.genre}).
                    ЛОР МИРА: ${s.worldLore}
                    $notesReminder
                    $antiMetagamingRule

                    ХРОНИКА СЮЖЕТА:
                    \"\"\"$fullStory\"\"\"

                    АКТУАЛЬНЫЙ СТАТУС:
                    - СЕЙЧАС: День ${s.currentDay}, ${s.currentTimeOfDay}
                    - Герой: ${s.heroName} | Состояние: ${s.heroStatus}
                    - Погибшие: $deadStr
                    - Важные заметки: $notesStr

                    ГЕРОЙ ${s.heroName} ОБРАЩАЕТСЯ К ТОЛПЕ/ОКРУЖАЮЩИМ: "$playerMessage"

                    ИНСТРУКЦИЯ:
                    1. Опиши живую реакцию толпы с полным знанием сюжета, но без всеведения тайных фактов.
                    2. Отвечай естественно по ситуации: кратко или развёрнуто.
                    3. Запрещены системные теги и лишние скобки.
                """.trimIndent()
            } else {
                """
                    Ты играешь роль персонажа/существа '$target' в мире '${s.worldName}' (${s.genre}).
                    ЛОР МИРА: ${s.worldLore}
                    $notesReminder
                    $antiMetagamingRule

                    ХРОНИКА СЮЖЕТА:
                    \"\"\"$fullStory\"\"\"

                    АКТУАЛЬНЫЙ СТАТУС:
                    - СЕЙЧАС: День ${s.currentDay}, ${s.currentTimeOfDay}
                    - Герой: ${s.heroName} | Состояние: ${s.heroStatus}
                    - Погибшие: $deadStr
                    - Заметки: $notesStr

                    ГЕРОЙ ${s.heroName} ОБРАЩАЕТСЯ К ТЕБЕ ('$target'): "$playerMessage"

                    СТРОГИЕ ИНСТРУКЦИИ:
                    1. Помни события сюжета, но строго соблюдай рамки того, что персонаж мог знать лично!
                    2. Не раскрывай сумм сделок и чужих разговоров, если персонаж не присутствовал при них!
                    3. Отвечай строго от первого лица ('Я'). Длина реплики адаптивная.
                    4. СТРОГО ЗАПРЕЩЕНО писать любые системные теги и скобки.
                """.trimIndent()
            }

            val dialogHistory = listOf(
                ChatMessage("user", "$sysPrompt\n\nСлова героя: $playerMessage")
            )

            val result = geminiClient.generateContent(
                modelName = s.currentModel,
                keysPool = _config.value.apiKeys,
                startKeyIndex = _currentKeyIndex.value,
                chatHistory = dialogHistory
            )

            val reply = when (result) {
                is GeminiResult.Success -> {
                    GameEngine.cleanResidualBracketsAndTags(result.text).ifBlank { result.text.trim() }
                }
                else -> "[Персонаж молчит или задумался...]"
            }

            onResponse(reply)

            val syncEntry = "[СОБЫТИЕ ДИАЛОГА (День ${s.currentDay}, ${s.currentTimeOfDay}): Герой ${s.heroName} обратился к $target: «$playerMessage». Ответ $target: «$reply».]"
            if (s.chatHistory.isNotEmpty() && s.chatHistory.last().role == "user") {
                val last = s.chatHistory.removeAt(s.chatHistory.size - 1)
                s.chatHistory.add(ChatMessage("user", "${last.text}\n\n$syncEntry"))
            } else {
                s.chatHistory.add(ChatMessage("user", syncEntry))
            }

            appendStoryText("💬 [Диалог с $target]:\n— ${s.heroName}: «$playerMessage»\n— $target: «$reply»\n")
            autoSave()
        }
    }

    // --- Deduplicate Abilities ---
    fun deduplicateAbilities() {
        val s = _session.value
        val unique = GameEngine.deduplicateAbilities(s.acquiredAbilities, s.zeroGifts)
        _session.value = _session.value.copy(acquiredAbilities = unique.toMutableList())
        autoSave()
    }

    fun removeAbility(index: Int) {
        val s = _session.value
        if (index in s.acquiredAbilities.indices) {
            s.acquiredAbilities.removeAt(index)
            _session.value = _session.value.copy(acquiredAbilities = s.acquiredAbilities.toMutableList())
            autoSave()
        }
    }

    // --- Art Reroll ---
    fun rerollArt() {
        val s = _session.value
        val prompt = s.lastImagePrompt.ifBlank {
            "dark fantasy scene in ${s.worldName}, dramatic cinematic lighting, masterpiece 8k"
        }
        val newUrl = ImageApiClient.buildFantasyImageUrl(prompt, s.genre, forceNewSeed = true)
        _session.value = _session.value.copy(lastImageUrl = newUrl)
        autoSave()
    }

    // --- Font Size ---
    fun adjustFontSize(delta: Int) {
        val s = _session.value
        val newSize = (s.fontSize + delta).coerceIn(12, 28)
        _session.value = _session.value.copy(fontSize = newSize)
        autoSave()
    }

    // --- Export Book ---
    fun getExportStoryText(): String {
        val s = _session.value
        val cleanBook = s.storyText.replace(
            Regex("\\[(?:STATUS|ABILITIES|NEW_ABILITY|SCORE|SUMMARY|HERO_DEATH|CODEX|IMAGE|TIME|DAY|MAP_PIN|MAP_MARKER)[^\\]]*\\]?", RegexOption.IGNORE_CASE),
            ""
        ).replace(Regex("\\n{3,}"), "\n\n")

        val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        return """
            ======================================================
                      ХРОНИКИ ЗЕРО: ${s.worldName.uppercase(Locale.getDefault())}
            ======================================================
            Герой: ${s.heroName}
            Жанр: ${s.genre}
            Дары Зеро: ${s.zeroGifts}
            Время в пути: ${s.currentDay} дней
            Дата хроники: $dateStr
            ======================================================

            $cleanBook
        """.trimIndent()
    }

    // --- Auto Save ---
    private fun autoSave() {
        val s = _session.value
        if (s.worldName.isNotBlank() && s.chatHistory.isNotEmpty()) {
            val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val updated = s.copy(lastSaved = dateStr)
            storage.saveSession(updated)
            refreshSavesList()
        }
    }
}
