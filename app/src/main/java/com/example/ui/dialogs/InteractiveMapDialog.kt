package com.example.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MapMarker
import com.example.ui.theme.*

@Composable
fun InteractiveMapDialog(
    worldName: String,
    markers: List<MapMarker>,
    onAddMarker: (String, String, String, Float?, Float?) -> Unit,
    onDeleteMarker: (MapMarker) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMarker by remember { mutableStateOf<MapMarker?>(null) }
    var showAddForm by remember { mutableStateOf(false) }

    var tapCoords by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var newName by remember { mutableStateOf("") }
    var newRegion by remember { mutableStateOf("Центр") }
    var newType by remember { mutableStateOf("город") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkPanel),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🗺️ ИНТЕРАКТИВНАЯ КАРТА",
                            color = GoldPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$worldName (Открыто: ${markers.size})",
                            color = FantasyTextDim,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = FantasyTextDim)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Canvas Map representation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(Color(0xFF070910), RoundedCornerShape(10.dp))
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                        .pointerInput(markers) {
                            detectTapGestures { offset ->
                                val normX = offset.x / size.width
                                val normY = offset.y / size.height

                                // Check if tapped near an existing marker
                                val tapped = markers.find { m ->
                                    val mx = m.x * size.width
                                    val my = m.y * size.height
                                    val distSq = (offset.x - mx) * (offset.x - mx) + (offset.y - my) * (offset.y - my)
                                    distSq < 32 * 32
                                }

                                if (tapped != null) {
                                    selectedMarker = tapped
                                } else {
                                    tapCoords = Pair(normX, normY)
                                    showAddForm = true
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f

                        // Draw fantasy parchment compass / circular continent guide
                        drawCircle(
                            color = Color(0x15D4AF37),
                            radius = minOf(w, h) * 0.45f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color(0x3038BDF8),
                            radius = minOf(w, h) * 0.25f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5f)
                        )
                        drawCircle(
                            color = Color(0x40D4AF37),
                            radius = minOf(w, h) * 0.45f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5f)
                        )

                        // Subtle coordinate crosshairs
                        drawLine(
                            color = Color(0x20D4AF37),
                            start = Offset(cx, 16f),
                            end = Offset(cx, h - 16f),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color(0x20D4AF37),
                            start = Offset(16f, cy),
                            end = Offset(w - 16f, cy),
                            strokeWidth = 1f
                        )

                        // Render markers
                        for (m in markers) {
                            val px = m.x * w
                            val py = m.y * h
                            val isSelected = selectedMarker?.name == m.name

                            val color = when (m.type.lowercase()) {
                                "замок", "крепость" -> Color(0xFFEAB308)
                                "город", "поселение" -> Color(0xFF38BDF8)
                                "пещера", "подземелье" -> Color(0xFFA855F7)
                                "руины", "храм" -> Color(0xFFF97316)
                                "лагерь" -> Color(0xFF84CC16)
                                "остров" -> Color(0xFFEF4444)
                                else -> Color(0xFFE2DAC8)
                            }

                            // Halo
                            drawCircle(
                                color = color.copy(alpha = if (isSelected) 0.5f else 0.25f),
                                radius = if (isSelected) 14f else 9f,
                                center = Offset(px, py)
                            )
                            // Pin dot
                            drawCircle(
                                color = color,
                                radius = if (isSelected) 7f else 5f,
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2f,
                                center = Offset(px, py)
                            )
                        }
                    }

                    Text(
                        text = "Коснитесь карты для отметки точки или нажмите на метку",
                        color = FantasyTextDim.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selected Marker card or Add Form
                if (showAddForm) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141724)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Новая точка на карте",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Название точки") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newRegion,
                                    onValueChange = { newRegion = it },
                                    label = { Text("Регион") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = newType,
                                    onValueChange = { newType = it },
                                    label = { Text("Тип (город/замок/пещера)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showAddForm = false }) {
                                    Text("Отмена", color = FantasyTextDim)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (newName.isNotBlank()) {
                                            onAddMarker(
                                                newName,
                                                newRegion,
                                                newType,
                                                tapCoords?.first,
                                                tapCoords?.second
                                            )
                                            newName = ""
                                            showAddForm = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                                ) {
                                    Text("Добавить метку")
                                }
                            }
                        }
                    }
                } else if (selectedMarker != null) {
                    val m = selectedMarker!!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141724)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "📍 ${m.name} (${m.type})",
                                    color = GoldPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Регион: ${m.region}  |  Открыто в День ${m.day}",
                                    color = FantasyTextDim,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = {
                                onDeleteMarker(m)
                                selectedMarker = null
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = CrimsonLight)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Markers List
                Text(
                    text = "СПИСОК ЛОКАЦИЙ:",
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (markers.isEmpty()) {
                        item {
                            Text(
                                text = "На карте пока нет отметок. Они наносятся по ходу сюжета!",
                                color = FantasyTextDim,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(markers) { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0E1019), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(GoldPrimary, CircleShape)
                                    )
                                    Column {
                                        Text(m.name, color = FantasyText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${m.region} • ${m.type}", color = FantasyTextDim, fontSize = 10.sp)
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteMarker(m) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Удалить", tint = CrimsonLight, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        tapCoords = null
                        showAddForm = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2333))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Добавить отметку вручную", fontSize = 12.sp)
                }
            }
        }
    }
}
