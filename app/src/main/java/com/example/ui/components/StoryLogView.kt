package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun StoryLogView(
    storyText: String,
    fontSize: Int,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val paragraphs = remember(storyText) {
        if (storyText.isBlank()) return@remember emptyList<String>()
        storyText.split(Regex("\\n{2,}"))
            .flatMap { chunk ->
                chunk.split(Regex("(?=\\n[➤\\[💀])"))
            }
            .map { it.trim() }
            .map { com.example.data.GameEngine.cleanResidualBracketsAndTags(it) }
            .filter { it.isNotBlank() }
    }

    LaunchedEffect(paragraphs.size) {
        if (paragraphs.isNotEmpty()) {
            listState.animateScrollToItem(paragraphs.size - 1)
        }
    }

    if (paragraphs.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📜 Свиток летописи готов. Ожидаем главу от Мастера...",
                color = FantasyTextDim,
                fontSize = fontSize.sp,
                fontStyle = FontStyle.Italic
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(paragraphs) { paragraph ->
                ParagraphItem(paragraph = paragraph, fontSize = fontSize)
            }
        }
    }
}

@Composable
private fun ParagraphItem(paragraph: String, fontSize: Int) {
    val trimmed = paragraph.trim()

    // 1. Player Action (starts with ➤)
    if (trimmed.startsWith("➤")) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF10192A), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = trimmed,
                color = StoryAction,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = (fontSize * 1.35).sp
            )
        }
        return
    }

    // 2. Danger / Death / Combat separator
    if (trimmed.contains("💀") || trimmed.contains("ГЕРОЙ ПОГИБ") || trimmed.contains("ЗАВЕРШЕНИЕ ПУТИ")) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF261010), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = trimmed,
                color = StoryCombat,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = (fontSize * 1.35).sp
            )
        }
        return
    }

    // 3. System / Chronicle Tag (starts with [ and ends with ])
    val isSystemTag = trimmed.startsWith("[") && trimmed.endsWith("]") &&
            (trimmed.length < 180 || trimmed.startsWith("[❌") || trimmed.startsWith("[⏳") ||
             trimmed.startsWith("[🔄") || trimmed.startsWith("[📂") || trimmed.startsWith("[🗺️") || trimmed.startsWith("[✨"))
    if (isSystemTag) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF191224), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = trimmed,
                color = StorySystem,
                fontSize = (fontSize - 1).sp,
                fontStyle = FontStyle.Italic,
                lineHeight = (fontSize * 1.3).sp
            )
        }
        return
    }

    // 4. Header lines like ### День 1. Утро.
    if (trimmed.startsWith("###") || trimmed.startsWith("##") || trimmed.startsWith("#")) {
        val headerText = trimmed.replace(Regex("^#+\\s*"), "")
        Text(
            text = headerText,
            color = GoldPrimary,
            fontSize = (fontSize + 2).sp,
            fontWeight = FontWeight.Bold,
            lineHeight = ((fontSize + 2) * 1.35).sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
        return
    }

    // 5. Standard narrative text with styled bold **...** and direct quotes «...» / "..."
    val annotated = buildFantasyFormattedString(trimmed)

    Text(
        text = annotated,
        color = StoryParchment,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.48).sp
    )
}

private fun buildFantasyFormattedString(raw: String): AnnotatedString {
    return buildAnnotatedString {
        // Regex matches: **bold text**, «quote in guillemets», or "quote in standard quotes"
        val tokenRegex = Regex("(\\*\\*.*?\\*\\*|«[^»]+»|\"[^\"]+\")")
        var cursor = 0
        val matches = tokenRegex.findAll(raw)

        for (m in matches) {
            if (m.range.first > cursor) {
                append(raw.substring(cursor, m.range.first))
            }
            val token = m.value
            when {
                token.startsWith("**") && token.endsWith("**") && token.length >= 4 -> {
                    val inner = token.substring(2, token.length - 2)
                    withStyle(
                        SpanStyle(
                            color = GoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(inner)
                    }
                }
                (token.startsWith("«") && token.endsWith("»")) ||
                (token.startsWith("\"") && token.endsWith("\"")) -> {
                    withStyle(
                        SpanStyle(
                            color = StorySpeech,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(token)
                    }
                }
                else -> {
                    append(token)
                }
            }
            cursor = m.range.last + 1
        }
        if (cursor < raw.length) {
            append(raw.substring(cursor))
        }
    }
}
