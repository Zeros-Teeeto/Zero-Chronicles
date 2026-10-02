package com.example

import com.example.data.GameEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTagExtraction() {
        val raw = "Свет озарил древние руины замка. [TIME: 3, \"Вечер\"] [STATUS: Невредим]"
        val (timeTag, withoutTime) = GameEngine.extractBracketTag(raw, "TIME")
        assertEquals("3, \"Вечер\"", timeTag)

        val (statusTag, cleanText) = GameEngine.extractBracketTag(withoutTime, "STATUS")
        assertEquals("Невредим", statusTag)
        assertTrue(cleanText.contains("Свет озарил древние руины замка."))
    }

    @Test
    fun testAbilityDeduplication() {
        val gifts = "Власть над священным звездным светом"
        val abilities = listOf(
            "Власть над священным звездным светом", // duplicate of gift
            "Огненная стрела: наносит урон",
            "огненная стрела: наносит урон", // duplicate
            "Ледяной шип"
        )
        val unique = GameEngine.deduplicateAbilities(abilities, gifts)
        assertEquals(2, unique.size)
        assertTrue(unique.any { it.contains("Огненная стрела") })
        assertTrue(unique.any { it.contains("Ледяной шип") })
    }

    @Test
    fun testCleanResidualEndings() {
        val textWithEnding = "Враг отступил в глубь пещеры.\nЧто ты сделаешь?"
        val cleaned = GameEngine.cleanResidualBracketsAndTags(textWithEnding)
        assertEquals("Враг отступил в глубь пещеры.", cleaned)
    }

    @Test
    fun testDistillText() {
        val story = "Герой Селеста Лучезарная переходит через бурную реку на рассвете и встречает эльфийского дозорного."
        val fact = GameEngine.distillTextToShortFact(story, maxWords = 7)
        assertTrue(fact.split(" ").size <= 7)
        assertTrue(fact.contains("Герой"))
    }
}
