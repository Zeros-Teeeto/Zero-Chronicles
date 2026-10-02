package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun ApiKeyGuideDialog(
    onDismiss: () -> Unit,
    onOpenKeysInput: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(2.dp, GoldPrimary, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkPanel),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔑 КАК ПОЛУЧИТЬ БЕСПЛАТНЫЙ API-КЛЮЧ",
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Займет всего 1 минуту • Официально и бесплатно от Google",
                            color = FantasyTextDim,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = FantasyTextDim)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content steps
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Step 1
                    StepCard(
                        stepNumber = "1",
                        title = "Перейдите на сайт Google AI Studio",
                        description = "Google предоставляет бесплатный доступ к Gemini. Нажмите кнопку ниже, чтобы открыть официальный сайт в браузере.",
                        actionButton = {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldLight)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ОТКРЫТЬ AISTUDIO.GOOGLE.COM", fontSize = 12.sp, color = GoldLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    )

                    // Step 2
                    StepCard(
                        stepNumber = "2",
                        title = "Войдите и нажмите «Create API key»",
                        description = "Войдите под своим обычным Google/Gmail аккаунтом. Нажмите синюю кнопку «Create API key» (Создать ключ API) и подтвердите создание.",
                        actionButton = null
                    )

                    // Step 3
                    StepCard(
                        stepNumber = "3",
                        title = "Скопируйте ключ и вставьте в игру",
                        description = "Ключ выглядит как строка из букв и цифр, начинающаяся на «AIzaSy...». Нажмите кнопку копирования рядом с ключом, вернитесь в игру и сохраните его.",
                        actionButton = {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenKeysInput()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ВВЕСТИ КЛЮЧ В НАСТРОЙКИ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )

                    // Security & Limits Note
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14121F)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF3E2D58))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🛡️ Безопасность и лимиты:",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Ключ сохраняется исключительно в локальной памяти вашего телефона и никуда не передается сторонним лицам.\n• Бесплатный тарифный план Google дает тысячи запросов в день — этого с головой хватит на сотни часов прохождения.\n• Вы можете добавить сразу несколько ключей для автоматической ротации пула.",
                                color = FantasyTextDim,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FantasyText)
                ) {
                    Text("ПОНЯТНО, ЗАКРЫТЬ")
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: String,
    title: String,
    description: String,
    actionButton: (@Composable () -> Unit)?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(GoldPrimary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = FantasyText,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            if (actionButton != null) {
                Spacer(modifier = Modifier.height(10.dp))
                actionButton()
            }
        }
    }
}
