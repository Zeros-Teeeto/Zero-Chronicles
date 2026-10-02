package com.example.data

import com.squareup.moshi.JsonClass
import java.util.UUID

const val USER_SAVED_API_KEY = ""

val GEMINI_MODELS = listOf(
    "gemini-2.5-flash",
    "gemini-3.5-flash-lite",
    "gemini-3.5-flash",
    "gemini-3.8-flash",
    "gemini-3.1-pro-preview"
)

val CLASSIC_WORLDS = listOf(
    Pair(
        "Кольцевые Земли: Раскол Внутреннего Моря",
        "Круговой континент вокруг Внутреннего Моря. На Севере — королевство людей Сапихом с замками и полями. На Западе — древний биолюминесцентный лес эльфов Самрэйл. На Востоке — каньоны и подземные твердыни Дворфов. На Юге — саванны и джунгли зверолюдей Асма. В самом центре посреди вод возвышается закрытый остров демонов Даймон."
    ),
    Pair(
        "Королевство Элдория: Век Золотого Грифона",
        "Континент древней магии, могущественных королевств и суровых рыцарских законов. За сияющими фасадами замков кипят политические интриги, в лесах рыщут разбойники и культисты, а инквизиция жестоко карает за преступления. Мир грандиозен, но суров к отступникам."
    ),
    Pair(
        "Архипелаг Астралис: Парящие Острова",
        "Расколотый мир летающих островов и небесных пиратов. За кристаллы левитации ведутся торговые войны. Древние воздушные левиафаны нападают на корабли, а города защищают элитные стрелки и маги стихий."
    )
)

val DARK_WORLDS = listOf(
    Pair(
        "Мортхейм: Земли Чёрного Пепла",
        "Мир, поглощенный затмением тысячу лет назад. Солнце угасло, превратившись в угольный диск. Древние боги обезумели и обратились в колоссальных тварей, бродящих по руинам городов. Выжившие ютятся вокруг угасающих алтарей из костей и защитных рун крови."
    ),
    Pair(
        "Ледяная Бездна Айсттема",
        "Континент вечной мерзлоты и свирепого мороза, где температура замораживает саму душу. Смертные здесь теряют рассудок от ледяного шепота, превращаясь в мутировавших Морозных Преследователей. Лишь те, кто обладают иммунитетом ко тьме, способны выжить среди замерзших исполинов."
    ),
    Pair(
        "Обсидиановый Разлом Морграта",
        "Расколотая земля черного камня и лавовых рек, где граница между миром живых и преисподней стерта. Воздух пропитан серой, а выжившие ордены рыцарей носят маски из тяжелой стали, чтобы не вдохнуть пепел падших душ."
    )
)

val CLASSIC_HEROES = listOf(
    Pair(
        "Селеста Лучезарная",
        "Способность: Власть над священным звездным светом, исцеление ран и рассеивание чар.\nОружие: 'Солнечный Палаш', сияющий ярким пламенем в бою."
    ),
    Pair(
        "Валериан из Серебряного Ордена",
        "Способность: Несокрушимый кинетический щит и мастерство парирования.\nОружие: 'Копье Рассвета', рассекающее магические барьеры."
    ),
    Pair(
        "Магистр Элиот",
        "Способность: Бездонный Пространственный Карман Зеро (бесконечный инвентарь) и телекинез.\nОружие: 'Астральный Жезл', поглощающий вражеские заклинания."
    )
)

val DARK_HEROES = listOf(
    Pair(
        "Элрион Пепельный Жнец",
        "Способность: Власть над теневым пламенем и телепортация. При смертельном ударе перерождается и становится сильнее.\nОружие: Проклятый меч 'Клеймо Затмения'."
    ),
    Pair(
        "Астарот Безучастный",
        "Способность: Полный иммунитет к ледяной скверне и перерождение после смерти.\nОружие: Тяжелый костяной тесак 'Хладный Ужас'."
    ),
    Pair(
        "Ворон, Охотник на Монстров",
        "Способность: Звериное чутье, виденье сквозь стены и трансформация крови в оружие.\nОружие: Пара зазубренных клинков из обсидиана."
    )
)

val AI_NOTES_PRESETS = listOf(
    "1. Игра 18+. Враги, разбойники и персонажи имеют право ругаться, выражаться грубо и действовать коварно.\n2. Враги умные, используют тактику и засады.\n3. Никаких нотаций и нравоучений.",
    "1. Тёмное взрослое фэнтези 18+. Описывай бои, ранения и последствия реалистично.\n2. Персонажи не доверяют чужакам и проявляют осторожность.\n3. Запрещено читать нотации игроку."
)

@JsonClass(generateAdapter = true)
data class MapMarker(
    val name: String,
    val region: String = "Дикие земли",
    val type: String = "локация",
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val day: Int = 1
)

@JsonClass(generateAdapter = true)
data class StoryMemory(
    val title: String,
    val fullText: String,
    val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class CodexData(
    val locations: MutableList<String> = mutableListOf(),
    val npcsNearby: MutableList<String> = mutableListOf(),
    val npcsDead: MutableList<String> = mutableListOf(),
    val npcsAll: MutableList<String> = mutableListOf(),
    val notes: MutableList<String> = mutableListOf()
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String
)

@JsonClass(generateAdapter = true)
data class GameSession(
    val id: String = UUID.randomUUID().toString(),
    val worldName: String = "",
    val worldLore: String = "",
    val worldMap: String = "",
    val worldMapImage: String = "",
    val mapMarkers: MutableList<MapMarker> = mutableListOf(),
    val keyNpcs: String = "",
    val sapientRaces: String = "",
    val beastsAndMonsters: String = "",
    val genre: String = "Классическое фэнтези",
    val aiSpecialNotes: String = "",
    val heroName: String = "",
    val zeroGifts: String = "",
    val acquiredAbilities: MutableList<String> = mutableListOf(),
    val storyMemories: MutableList<StoryMemory> = mutableListOf(),
    val currentModel: String = "gemini-2.5-flash",
    val summaryMemory: String = "",
    val totalTurnsCount: Int = 0,
    val currentDay: Int = 1,
    val currentTimeOfDay: String = "Утро",
    val heroStatus: String = "В добром здравии (Невредим)",
    val codexData: CodexData = CodexData(),
    val chatHistory: MutableList<ChatMessage> = mutableListOf(),
    val isGameEnded: Boolean = false,
    val isHeroDead: Boolean = false,
    val finalScore: String? = null,
    val finalSummary: String = "",
    val storyText: String = "",
    val lastImagePrompt: String = "",
    val lastImageUrl: String = "",
    val fontSize: Int = 15,
    val lastSaved: String = ""
)

@JsonClass(generateAdapter = true)
data class WorldTemplate(
    val id: String = UUID.randomUUID().toString(),
    val worldName: String = "",
    val worldLore: String = "",
    val worldMap: String = "",
    val worldMapImage: String = "",
    val mapMarkers: List<MapMarker> = emptyList(),
    val keyNpcs: String = "",
    val sapientRaces: String = "",
    val beastsAndMonsters: String = "",
    val genre: String = "Классическое фэнтези",
    val aiSpecialNotes: String = "",
    val storyMemories: List<StoryMemory> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AppConfig(
    val apiKeys: List<String> = emptyList(),
    val model: String = "gemini-2.5-flash"
)
