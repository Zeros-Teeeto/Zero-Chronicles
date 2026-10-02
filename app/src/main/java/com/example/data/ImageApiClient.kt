package com.example.data

import java.net.URLEncoder
import kotlin.random.Random

object ImageApiClient {

    fun buildFantasyImageUrl(prompt: String, genre: String, forceNewSeed: Boolean = false): String {
        val styleTag = if (genre == "Классическое фэнтези") {
            "epic high fantasy, cinematic chiaroscuro lighting, highly detailed concept art, 8k, masterpiece"
        } else {
            "dark fantasy, grimdark, gloomy volumetric fog, gritty, moody cinematic lighting, Frank Frazetta style, 8k, masterpiece"
        }

        val cleanPrompt = prompt.replace(Regex("[^\\p{L}\\p{Nd}\\s,\\-]"), "")
        val fullPrompt = "$cleanPrompt, $styleTag"
        val encoded = try {
            URLEncoder.encode(fullPrompt, "UTF-8")
        } catch (_: Exception) {
            "dark+fantasy+scene"
        }

        val seed = if (forceNewSeed) Random.nextInt(100000, 9999999) else Random.nextInt(1, 999999)
        return "https://image.pollinations.ai/prompt/$encoded?width=700&height=850&nologo=true&model=flux&seed=$seed"
    }
}
