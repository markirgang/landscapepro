package com.example.landscapepro.ui.tabs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.landscapepro.data.GardenState
import com.example.landscapepro.ui.components.BeforeAfterSwipeSlider
import com.example.landscapepro.ui.drawing.PlantOverlayDrawer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

fun loadBitmapFromAsset(context: Context, filename: String): Bitmap? {
    return try {
        val stream = context.assets.open(filename)
        BitmapFactory.decodeStream(stream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? {
    return try {
        val uri = Uri.parse(uriString)
        val stream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(stream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun loadBitmapFromFile(path: String): Bitmap? {
    return try {
        BitmapFactory.decodeFile(path)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

suspend fun callGeminiApi(apiKey: String, base64Image: String, prompt: String): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.readTimeout = 60000
        conn.connectTimeout = 60000

        val payload = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseModalities", org.json.JSONArray().apply { put("IMAGE") })
            })
        }

        val os = conn.outputStream
        os.write(payload.toString().toByteArray(Charsets.UTF_8))
        os.close()

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = conn.inputStream
            val responseString = inputStream.bufferedReader().use { it.readText() }
            
            val resJson = JSONObject(responseString)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val inlineData = part.optJSONObject("inlineData") ?: part.optJSONObject("inline_data")
                        if (inlineData != null) {
                            return@withContext inlineData.optString("data")
                        }
                    }
                }
            }
        } else {
            val errStream = conn.errorStream
            val errText = errStream?.bufferedReader()?.use { it.readText() } ?: ""
            println("Gemini API Error Response: $errText")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    null
}

fun saveBase64ToFile(context: Context, base64Str: String, conceptId: String, imageId: String): String? {
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        val filename = "gen_${imageId}_${conceptId}.png"
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { fos ->
            fos.write(bytes)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun generateGeminiLandscape(
    context: Context,
    state: GardenState,
    onStateChanged: (GardenState) -> Unit,
    baseBitmap: Bitmap
) {
    onStateChanged(state.copy(isGenerating = true))

    val themeLabel = when (state.activeTheme) {
        "cottage" -> "English Cottage Garden"
        "xeriscape" -> "Modern Dry Xeriscape"
        "zen" -> "Japanese Zen Garden"
        "wildflower" -> "Wildflower Meadow"
        "mediterranean" -> "Mediterranean Terrace"
        "rain_garden" -> "Pacific NW Rain Garden"
        "desert_oasis" -> "Desert Southwest Oasis"
        "woodland_shade" -> "Woodland Shade Glen"
        "formal_french" -> "Formal French Parterre"
        "tropical_jungle" -> "Tropical Paradise Jungle"
        "pollinator" -> "Pollinator Sanctuary"
        "rock_alpine" -> "Rock Garden Alpine"
        else -> "English Cottage Garden"
    }

    val sunLabel = when (state.sunExposure) {
        "sun_full" -> "Full Sun"
        "sun_partial" -> "Partial Shade"
        "sun_shade" -> "Full Shade"
        else -> "Full Sun"
    }

    val prompt = "Add a professional landscape design planting bed of $themeLabel style " +
        "appropriate for the ${state.activeSeason.replaceFirstChar { it.uppercase() }} season and under $sunLabel exposure. " +
        "Keep all of the basic objects already present in the existing image (including the fence, " +
        "house, background trees, sky, lawn, and overall yard layout) exactly the same. Do not overwrite " +
        "or change them. Only add the new garden bed with mulch or gravel and beautiful, healthy " +
        "plant species suitable for USDA Hardiness Zone ${state.currentZone} along the ground."

    val base64Img = bitmapToBase64(baseBitmap)
    val resultB64 = callGeminiApi(state.geminiApiKey, base64Img, prompt)

    if (resultB64 != null) {
        val path = saveBase64ToFile(context, resultB64, state.activeConcept, state.activeImageId)
        if (path != null) {
            onStateChanged(state.copy(
                isGenerating = false,
                settingsDirty = false,
                generatedConcepts = state.generatedConcepts + (state.activeConcept to path)
            ))
            return
        }
    }

    onStateChanged(state.copy(isGenerating = false))
    withContext(Dispatchers.Main) {
        android.widget.Toast.makeText(context, "Gemini API Generation failed. Using offline composite engine.", android.widget.Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscaperTab(
    state: GardenState,
    onStateChanged: (GardenState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val baseBitmap = remember(state.activeImageId, state.images) {
        val activeImg = state.images.find { it.id == state.activeImageId }
        if (activeImg != null) {
            if (activeImg.assetPath != null) {
                loadBitmapFromAsset(context, activeImg.assetPath)
            } else if (activeImg.localUri != null) {
                loadBitmapFromUri(context, activeImg.localUri)
            } else {
                null
            }
        } else {
            null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val concepts = listOf("concept-1" to "Concept 1", "concept-2" to "Concept 2 (Lush)", "concept-3" to "Concept 3 (Minimalist)")
            val activeIndex = concepts.indexOfFirst { it.first == state.activeConcept }.coerceAtLeast(0)

            TabRow(selectedTabIndex = activeIndex) {
                concepts.forEachIndexed { idx, (id, label) ->
                    Tab(
                        selected = activeIndex == idx,
                        onClick = {
                            onStateChanged(state.copy(activeConcept = id))
                        },
                        text = { Text(label) }
                    )
                }
            }

            BeforeAfterSwipeSlider(
                beforeImage = {
                    if (baseBitmap != null) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawIntoCanvas { canvas ->
                                val nativeCanvas = canvas.nativeCanvas
                                val dstRect = android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt())
                                nativeCanvas.drawBitmap(baseBitmap, null, dstRect, null)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No base image selected", color = Color.White)
                        }
                    }
                },
                afterImage = {
                    val generatedPath = state.generatedConcepts[state.activeConcept]
                    val generatedBitmap = remember(generatedPath) {
                        if (generatedPath != null) loadBitmapFromFile(generatedPath) else null
                    }

                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawIntoCanvas { canvas ->
                            val nativeCanvas = canvas.nativeCanvas
                            if (generatedBitmap != null) {
                                val dstRect = android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt())
                                nativeCanvas.drawBitmap(generatedBitmap, null, dstRect, null)
                            } else {
                                if (baseBitmap != null) {
                                    val dstRect = android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt())
                                    nativeCanvas.drawBitmap(baseBitmap, null, dstRect, null)
                                }
                                
                                val conceptIndex = when (state.activeConcept) {
                                    "concept-1" -> 1
                                    "concept-2" -> 2
                                    "concept-3" -> 3
                                    else -> 1
                                }
                                PlantOverlayDrawer.drawPlantOverlay(context, nativeCanvas, size.width, size.height, state, conceptIndex)
                            }
                        }
                    }

                    if (state.settingsDirty) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                    Text("Click 'Generate' to apply changes", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            )

            Button(
                onClick = {
                    if (state.geminiApiKey.isNotBlank() && baseBitmap != null) {
                        scope.launch {
                            generateGeminiLandscape(context, state, onStateChanged, baseBitmap)
                        }
                    } else {
                        onStateChanged(state.copy(settingsDirty = false, generationToken = state.generationToken + 1))
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.settingsDirty) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (state.settingsDirty) Icons.Default.Refresh else Icons.Default.CheckCircle,
                        contentDescription = "Generate"
                    )
                    Text(
                        text = if (state.settingsDirty) "Generate AI Designs" else "Designs Applied",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider()

            Text(
                text = "Garden Style Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            val themes = listOf(
                "cottage" to "English Cottage",
                "xeriscape" to "Modern Xeriscape",
                "zen" to "Japanese Zen",
                "wildflower" to "Wildflower Meadow",
                "mediterranean" to "Mediterranean",
                "rain_garden" to "Rain Garden / Bog",
                "desert_oasis" to "Desert Oasis",
                "woodland_shade" to "Woodland Shade",
                "formal_french" to "Formal French",
                "tropical_jungle" to "Tropical Jungle",
                "pollinator" to "Pollinator Paradise",
                "rock_alpine" to "Rock & Alpine"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                themes.forEach { (id, label) ->
                    val isSelected = state.activeTheme == id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .width(140.dp)
                            .height(80.dp)
                            .clickable {
                                onStateChanged(state.copy(activeTheme = id, settingsDirty = true))
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Composition Ratio",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${state.perennialRatio}% Perennials / ${100 - state.perennialRatio}% Annuals",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = state.perennialRatio.toFloat(),
                        onValueChange = {
                            onStateChanged(state.copy(perennialRatio = it.toInt(), settingsDirty = true))
                        },
                        valueRange = 0f..100f
                    )
                }
            }

            var seasonExpanded by remember { mutableStateOf(false) }
            val seasons = listOf("spring" to "Spring blooms", "summer" to "Summer lushness", "autumn" to "Autumn gold", "winter" to "Winter structure")
            val activeSeasonLabel = seasons.find { it.first == state.activeSeason }?.second ?: state.activeSeason

            ExposedDropdownMenuBox(
                expanded = seasonExpanded,
                onExpandedChange = { seasonExpanded = it }
            ) {
                OutlinedTextField(
                    value = activeSeasonLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Season") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = seasonExpanded,
                    onDismissRequest = { seasonExpanded = false }
                ) {
                    seasons.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onStateChanged(state.copy(activeSeason = value, settingsDirty = true))
                                seasonExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (state.isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Generating photo-realistic concept via Gemini API...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
