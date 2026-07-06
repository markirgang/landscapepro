package com.example.landscapepro.ui.tabs

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.landscapepro.data.GardenImage
import com.example.landscapepro.data.GardenState
import com.example.landscapepro.ui.drawing.PlantOverlayDrawer

fun saveBitmapToPictures(context: Context, bitmap: Bitmap, filename: String): Uri? {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/LandscapePro")
    }
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (imageUri != null) {
        try {
            val stream = resolver.openOutputStream(imageUri)
            if (stream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                stream.close()
                return imageUri
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return null
}

fun exportDesign(context: Context, state: GardenState, conceptId: String): Boolean {
    val activeImg = state.images.find { it.id == state.activeImageId } ?: return false
    val baseBitmap = (if (activeImg.assetPath != null) {
        loadBitmapFromAsset(context, activeImg.assetPath)
    } else if (activeImg.localUri != null) {
        loadBitmapFromUri(context, activeImg.localUri)
    } else {
        null
    }) ?: return false

    // Create a mutable copy of the bitmap
    val width = baseBitmap.width
    val height = baseBitmap.height
    val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(resultBitmap)

    // Draw base
    canvas.drawBitmap(baseBitmap, 0f, 0f, null)

    // Draw overlay on top
    val conceptIndex = when (conceptId) {
        "concept-1" -> 1
        "concept-2" -> 2
        "concept-3" -> 3
        else -> 1
    }
    PlantOverlayDrawer.drawPlantOverlay(canvas, width.toFloat(), height.toFloat(), state, conceptIndex)

    val filename = "LandscapePro_${state.activeTheme}_${conceptId}_${System.currentTimeMillis()}.jpg"
    val uri = saveBitmapToPictures(context, resultBitmap, filename)
    return uri != null
}

@Composable
fun GalleryTab(
    state: GardenState,
    onStateChanged: (GardenState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // To persist access permission across reboots if needed, but for runtime it is fine
                val newId = "user_${System.currentTimeMillis()}"
                val newImg = GardenImage(
                    id = newId,
                    name = "User Uploaded (${state.images.size - 2})",
                    localUri = uri.toString()
                )
                onStateChanged(
                    state.copy(
                        images = state.images + newImg,
                        activeImageId = newId,
                        settingsDirty = true
                    )
                )
                Toast.makeText(context, "Photo imported successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Garden Space Gallery",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Image")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Photo")
            }
        }

        // Active workspace details
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Active Space: " + (state.activeImage?.name ?: "None"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val success = exportDesign(context, state, state.activeConcept)
                            if (success) {
                                Toast.makeText(context, "Exported active concept successfully to Gallery/Pictures!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Export failed. Please check storage permissions.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export Active")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Current")
                    }

                    OutlinedButton(
                        onClick = {
                            var allSuccess = true
                            listOf("concept-1", "concept-2", "concept-3").forEach { cId ->
                                val success = exportDesign(context, state, cId)
                                if (!success) allSuccess = false
                            }
                            if (allSuccess) {
                                Toast.makeText(context, "All 3 concepts exported successfully to Gallery/Pictures!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to export some concepts.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export All 3")
                    }
                }
            }
        }

        // Grid of images
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.images) { img ->
                val isSelected = img.id == state.activeImageId
                val context = LocalContext.current
                val bitmap = remember(img) {
                    if (img.assetPath != null) {
                        loadBitmapFromAsset(context, img.assetPath)
                    } else if (img.localUri != null) {
                        loadBitmapFromUri(context, img.localUri)
                    } else {
                        null
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onStateChanged(state.copy(activeImageId = img.id, settingsDirty = true))
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = img.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(4.dp)
                        ) {
                            Text(
                                text = img.name,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
