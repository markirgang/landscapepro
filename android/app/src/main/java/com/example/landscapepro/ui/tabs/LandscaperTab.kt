package com.example.landscapepro.ui.tabs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscaperTab(
    state: GardenState,
    onStateChanged: (GardenState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // Dynamically load the bitmap for the active workspace image
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(verticalScrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Concept Tabs Navigation Row
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

        // Before/After Swipe Slider
        BeforeAfterSwipeSlider(
            beforeImage = {
                // Render original bare image
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
                // Render landscaped image with organic plant overlays
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawIntoCanvas { canvas ->
                        val nativeCanvas = canvas.nativeCanvas
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
                        // Draw organic procedurals on top
                        PlantOverlayDrawer.drawPlantOverlay(nativeCanvas, size.width, size.height, state, conceptIndex)
                    }
                }

                // If settings are dirty (user changed properties but didn't generate), show scanning laser indicator
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

        // Generate AI Designs Trigger
        Button(
            onClick = {
                onStateChanged(state.copy(settingsDirty = false, generationToken = state.generationToken + 1))
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

        // Themes List horizontally scrollable
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

        // Perennials vs Annuals Slider
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

        // Seasonal Selector dropdown
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
}
