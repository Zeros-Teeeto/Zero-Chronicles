package com.example.data

import org.json.JSONObject
import java.util.regex.Pattern

object GameEngine {

    fun cleanNpcName(name: String?): String {
        if (name.isNullOrBlank()) return ""
        var clean = name.replace(Regex("\\s*\\(.*?\\)"), "")
        clean = clean.replace(Regex("\\s*\\[.*?\\]"), "")
        return clean.trim(' ', '\t', '\n', '\r', '"', '\'', '«', '»', '•', '-', '–', '—')
    }

    fun normalizeAbilityName(text: String?): String {
        if (text.isNullOrBlank()) return ""
        val namePart = text.split(Regex("[:—–\\-]")).firstOrNull() ?: text
        val noParens = namePart.replace(Regex("\\(.*?\\)"), "")
        val clean = noParens.replace(Regex("[^\\p{L}\\p{Nd}\\s]"), "").lowercase()
        return clean.trim().split(Regex("\\s+")).joinToString(" ")
    }

    fun isSameAbility(ab1: String?, ab2: String?): Boolean {
        val n1 = normalizeAbilityName(ab1)
        val n2 = normalizeAbilityName(ab2)
        if (n1.isEmpty() || n2.isEmpty()) return false
        if (n1 == n2) return true
        if (n1.length >= 8 && n2.length >= 8) {
            if (n1.contains(n2) || n2.contains(n1)) return true
        }
        return false
    }

    fun deduplicateAbilities(abilities: List<String>, zeroGifts: String): List<String> {
        val unique = mutableListOf<String>()
        for (ab in abilities) {
            val abStr = ab.trim()
            if (abStr.isEmpty()) continue
            if (isSameAbility(abStr, zeroGifts)) continue
            if (unique.none { isSameAbility(abStr, it) }) {
                unique.add(abStr)
            }
        }
        return unique
    }

    fun addAbilityIfUnique(abilities: MutableList<String>, abilityText: String, zeroGifts: String): Boolean {
        val cleanAb = abilityText.trim().trim('"', '\'')
        if (cleanAb.isEmpty()) return false
        if (isSameAbility(cleanAb, zeroGifts)) return false
        if (abilities.any { isSameAbility(cleanAb, it) }) return false
        abilities.add(cleanAb)
        return true
    }

    /**
     * Extracts bracket tags like [TAG: content] or [TAG]
     * Handles nested brackets properly.
     */
    fun extractBracketTag(text: String, tagName: String): Pair<String?, String> {
        val regex = Pattern.compile("\\[\\s*${Pattern.quote(tagName)}\\s*[:\\]]", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)
        if (!matcher.find()) {
            return Pair(null, text)
        }

        val startIdx = matcher.start()
        val contentStart = matcher.end()

        var bracketCount = 0
        var endIdx = -1
        for (i in startIdx until text.length) {
            if (text[i] == '[') {
                bracketCount++
            } else if (text[i] == ']') {
                bracketCount--
                if (bracketCount == 0) {
                    endIdx = i
                    break
                }
            }
        }

        if (endIdx != -1) {
            val content = text.substring(contentStart, endIdx).trim()
            var cutEnd = endIdx + 1
            while (cutEnd < text.length && text[cutEnd] in charArrayOf(']', ' ', '\t', '\r')) {
                cutEnd++
            }
            val cleanedText = text.substring(0, startIdx) + text.substring(cutEnd)
            return Pair(content, cleanedText)
        }

        return Pair(null, text)
    }

    fun cleanResidualBracketsAndTags(text: String?): String {
        if (text.isNullOrBlank()) return ""
        var t: String = text
        // 1. Remove JSON blocks or pseudo-JSON blocks
        t = t.replace(Regex("(?is)\\{\\s*\"?(?:locations|npcs_nearby|npcs_dead|notes)\"?\\s*:[^\\}]*\\}"), "")
        // 2. Remove all bracket tags with any known tag name
        t = t.replace(Regex("(?is)\\[\\s*(?:STATUS|ABILITIES|NEW_ABILITY|SCORE|SUMMARY|HERO_DEATH|CODEX|IMAGE|TIME|DAY|MAP_PIN|MAP_MARKER|REMEMBER|ALLY|NPCS?_NEARBY|NPCS?_DEAD|LOCATIONS?|NOTES?)\\b[^\\]]*\\]?"), "")
        // 3. Remove standalone key: value lines
        t = t.replace(Regex("(?im)^\\s*\\*?\\*?\\s*(?:NPCS?_NEARBY|NPCS?_DEAD|LOCATIONS?|NOTES?|IMAGE|CODEX|STATUS|TIME)\\s*\\*?\\*?\\s*[:=].*$"), "")
        // 4. Remove any leftover npcs_nearby ... fragments
        t = t.replace(Regex("(?im)[,\\s]*\"?npcs?_nearby\"?\\s*[:=]?\\s*(\\[[^\\]]*\\]|[^\\n\\r]*)"), "")
        t = t.replace(Regex("(?im)[,\\s]*\"?npcs?_dead\"?\\s*[:=]?\\s*(\\[[^\\]]*\\]|[^\\n\\r]*)"), "")
        t = t.replace(Regex("(?im)[,\\s]*\"?locations?\"?\\s*[:=]?\\s*(\\[[^\\]]*\\]|[^\\n\\r]*)"), "")
        t = t.replace(Regex("(?im)[,\\s]*\"?notes?\"?\\s*[:=]?\\s*(\\[[^\\]]*\\]|[^\\n\\r]*)"), "")
        t = t.replace(Regex("(?iu)\\bNPCS?_nearby\\s+[^\\n\\r]*"), "")

        val unwantedEndings = listOf(
            Regex("(?iu)[\\r\\n]*(?:твой|ваш)\\s+ход[\\.\\?!:\\s]*$"),
            Regex("(?iu)[\\r\\n]*что\\s+(?:ты\\s+будешь\\s+делать|ты\\s+сделаешь|ты\\s+предпримешь|вы\\s+будете\\s+делать|вы\\s+сделаете)[\\.\\?!:\\s]*$"),
            Regex("(?iu)[\\r\\n]*каков\\s+(?:твой|ваш)\\s+(?:выбор|следующий\\s+шаг|план|ответ)[\\.\\?!:\\s]*$"),
            Regex("(?iu)[\\r\\n]*каково\\s+(?:твоё|ваше)\\s+решение[\\.\\?!:\\s]*$"),
            Regex("(?iu)[\\r\\n]*что\\s+предпримешь[\\.\\?!:\\s]*$")
        )

        for (pattern in unwantedEndings) {
            t = pattern.replace(t.trim(), "").trim()
        }

        t = t.replace(Regex("[\\s\\]\\[\\}>]+$"), "")
        t = t.replace(Regex("^\\s*\\]+\\s*$", RegexOption.MULTILINE), "")

        return t.trim()
    }

    fun distillTextToShortFact(text: String?, maxWords: Int = 10): String {
        if (text.isNullOrBlank()) return ""
        var cleaned = text.replace(
            Regex("\\[(?:STATUS|ABILITIES|NEW_ABILITY|SCORE|SUMMARY|HERO_DEATH|CODEX|IMAGE|TIME|DAY|MAP_PIN|MAP_MARKER|REMEMBER|ALLY|СТРОГИЕ ДИРЕКТИВЫ.*?|ГЛАВНАЯ ИНСТРУКЦИЯ.*?):.*?\\]", RegexOption.DOT_MATCHES_ALL),
            ""
        )
        cleaned = cleaned.replace(Regex("\\[.*?\\]"), "")
        cleaned = cleaned.replace("➤", "")
        cleaned = cleaned.replace(Regex("^[—–\\-]\\s*"), "")
        cleaned = cleaned.replace("\n", " ").trim()

        val sentences = cleaned.split(Regex("(?<=[.!?])\\s+"))
        val targetSentence = sentences.firstOrNull()?.trim() ?: cleaned
        val words = targetSentence.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return ""
        return if (words.size > maxWords) {
            words.take(maxWords).joinToString(" ") + "..."
        } else {
            words.joinToString(" ")
        }
    }

    fun buildSystemInstruction(session: GameSession): String {
        val genreInstruction = if (session.genre == "Классическое фэнтези") {
            """СТИЛЬ ЖАНРА: КЛАССИЧЕСКОЕ / ВЫСОКОЕ ФЭНТАЗИ (High & Epic Fantasy).
- Живой, суровый, опасный мир магии, древних тайн, законов и интриг."""
        } else {
            """СТИЛЬ ЖАНРА: ТЁМНОЕ ФЭНТАЗИ (Dark Fantasy / Grimdark).
- Мрачный, бескомпромиссный, гнетущий мир древних проклятий, жестоких боев и непоправимых последствий."""
        }

        val memorySection = if (session.summaryMemory.isNotBlank()) {
            "\nХРОНИКА ПРОШЛЫХ ГЛАВ:\n${session.summaryMemory}\n"
        } else ""

        val specialNotesSection = if (session.aiSpecialNotes.isNotBlank()) {
            "\nТАЙНЫЕ ДИРЕКТИВЫ ОТ ИГРОКА (ДЛЯ ТЕБЯ КАК МАСТЕРА):\n${session.aiSpecialNotes}\n"
        } else ""

        var worldModules = ""
        if (session.worldMap.isNotBlank()) {
            worldModules += "\nГЕОГРАФИЯ И КАРТА МИРА (С РАССТОЯНИЯМИ В ДНЯХ):\n${session.worldMap}\n"
        }
        if (session.keyNpcs.isNotBlank()) {
            worldModules += "\nВАЖНЫЕ СЮЖЕТНЫЕ NPC МИРА:\n${session.keyNpcs}\n"
        }
        if (session.sapientRaces.isNotBlank()) {
            worldModules += "\nРАЗУМНЫЕ НАРОДЫ И РАСЫ МИРА:\n${session.sapientRaces}\n"
        }
        if (session.beastsAndMonsters.isNotBlank()) {
            worldModules += "\nБЕСТИАРИЙ И ОПАСНЫЕ ТВАРЫ:\n${session.beastsAndMonsters}\n"
        }

        val activeMems = session.storyMemories.filter { it.isActive }
        val memoriesSection = if (activeMems.isNotEmpty()) {
            val memItems = activeMems.joinToString("\n\n") { "• [${it.title}]:\n${it.fullText.trim()}" }
            """
[АКТИВНАЯ ВЕЧНАЯ ПАМЯТЬ (ПОЛНЫЙ ДОСЛОВНЫЙ ТЕКСТ СОБЫТИЙ И СОЮЗОВ)]:
$memItems
(СТРОЖАЙШЕЕ ПРАВИЛО: ИИ обязан помнить эти события целиком со всеми деталями, диалогами и союзами без искажения!)
"""
        } else ""

        val abilitiesStr = if (session.acquiredAbilities.isNotEmpty()) session.acquiredAbilities.joinToString(", ") else "Пока нет новых"
        val nearbyStr = if (session.codexData.npcsNearby.isNotEmpty()) session.codexData.npcsNearby.joinToString(", ") else "Никого нет"
        val markersSummary = if (session.mapMarkers.isNotEmpty()) {
            session.mapMarkers.joinToString(", ") { "${it.name} (${it.type})" }
        } else "Только стартовая локация"

        val anchorSection = """
[ТЕКУЩИЙ ЯКОРЬ МИРА]:
- ПОСЛЕДНЕЕ ВРЕМЯ: День ${session.currentDay} (${session.currentTimeOfDay})
- ГЕРОЙ: ${session.heroName} | СОСТОЯНИЕ: ${session.heroStatus}
- ИЗНАЧАЛЬНЫЕ ДАРЫ ЗЕРО: ${session.zeroGifts}
- ПОЛУЧЕННЫЕ/ПОХИЩЕННЫЕ СПОСОБНОСТИ: $abilitiesStr
- КТО РЯДОМ С ГЕРОЕМ ПРЯМО СЕЙЧАС: $nearbyStr
- ОТКРЫТЫЕ ТОЧКИ НА КАРТЕ МИРА: $markersSummary
"""

        return """Ты — Искусственный Интеллект Гейм-Мастер (DM) в глубокой текстовой RPG игре.
ПАРАМЕТРЫ СЕССИИ:
- ЖАНР: ${session.genre}.
$genreInstruction
- НАЗВАНИЕ МИРА: ${session.worldName}.
- ОСНОВНОЙ ЛОР МИРА: ${session.worldLore}.
$worldModules
$anchorSection
$memoriesSection
$specialNotesSection
$memorySection
СТРОГИЕ ПРАВИЛА ВЕДЕНИЯ ИГРЫ:

1. ГИБКИЙ И АДАПТИВНЫЙ ОБЪЁМ ОТВЕТА (РАЗУМНЫЙ ТЕМП):
   - НЕТ ФИКСИРОВАННОГО ЧИСЛА АБЗАЦЕВ! Объём текста определяется важностью момента:
     * Если действие простое или короткое — отвечай ЛАКОНИЧНО и ёмко, хоть в один абзац, без лишней воды.
     * Если происходит важная сцена, бой, открытие тайны или глубокий разговор — пиши РАЗВЁРНУТО и подробно.
   - КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО отвечать одними служебными тегами!
   - ПОРЯДОК ВЫВОДА:
     1. Сначала пиши литературный художественный текст сюжета.
      2. И только ПОСЛЕ ВСЕГО ТЕКСТА в самом конце хода выведи служебные теги ([TIME], [STATUS]) при необходимости.

2. РЕАЛИСТИЧНАЯ ХРОНОЛОГИЯ И ДИНАМИКА ВРЕМЕНИ:
   - СЕЙЧАС В ИГРЕ: День ${session.currentDay}, ${session.currentTimeOfDay}.
   - ТЫ САМ РАССЧИТЫВАЕШЬ РЕАЛЬНОЕ ВРЕМЯ ПО ХОДУ ДЕЙСТВИЙ:
     * Если игрок говорит: «жду до вечера», «ночуем», «проходит 3 дня» — смени день и время суток соответственно!
     * Если происходит короткий диалог или действие — время суток остаётся прежним.
     * Дальние походы по карте между городами занимают дни пути!
   - В САМОМ КОНЦЕ текста выведи тег актуального времени: [TIME: номер_дня, "время_суток"].

3. ЧИСТОТА ТЕКСТА И ТЕХНИЧЕСКИЙ ЗАПРЕТ:
   - КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО писать в ответе JSON, списки npcs_nearby, codex, image или служебные мета-теги!
   - Весь твой вывод — это чистая, захватывающая художественная проза от лица Мастера.

4. УЧЁТ СПОСОБНОСТЕЙ:
   - Тег [NEW_ABILITY: "Название: эффект"] выводится ИСКЛЮЧИТЕЛЬНО ОДИН РАЗ в самом конце при первом получении силы.
   - При обычном использовании силы в бою теги способностей выводить ЗАПРЕЩЕНО!

5. СОСТОЯНИЕ ГЕРОЯ:
   - Описывай ранения словами в теге [STATUS: текст]. Без цифр HP!
   - Если у героя указано перерождение — не ставь [HERO_DEATH: True], а опиши воскрешение Даром Зеро.

6. КОНЦОВКА ХОДА:
   - КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО писать в конце: «Твой ход», «Что ты сделаешь?», «Что предпримешь?» и задавать вопросы игроку! Обрывай ход на действии мира или повисшей паузе.
""".trimIndent()
    }
}
