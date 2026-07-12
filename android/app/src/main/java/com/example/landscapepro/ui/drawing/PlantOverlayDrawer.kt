package com.example.landscapepro.ui.drawing

import android.content.Context
import android.graphics.*
import com.example.landscapepro.data.GardenState
import com.example.landscapepro.data.SeededRandom
import java.util.HashMap

object PlantOverlayDrawer {

    data class PlantSpec(
        val file: String,
        val name: String,
        val sun: List<String>,
        val water: List<String>,
        val themes: List<String>,
        val type: String,
        val evergreen: Boolean
    )

    private val PLANT_DB = mapOf(
        "lavender" to PlantSpec(
            "lavender.png", "English Lavender",
            listOf("full-sun"), listOf("dry", "well-drained"),
            listOf("cottage", "meadow", "mediterranean", "pollinator"),
            "midground", false
        ),
        "boxwood" to PlantSpec(
            "boxwood.png", "Green Velvet Boxwood",
            listOf("full-sun", "partial-shade"), listOf("moist", "well-drained"),
            listOf("formal-french", "zen", "mediterranean", "woodland-shade"),
            "midground", true
        ),
        "hosta" to PlantSpec(
            "hosta.png", "Variegated Hosta",
            listOf("partial-shade", "full-shade"), listOf("moist", "wet"),
            listOf("woodland-shade", "rain-garden", "tropical-jungle"),
            "foreground", false
        ),
        "hydrangea" to PlantSpec(
            "hydrangea.png", "Blue Hydrangea",
            listOf("partial-shade"), listOf("moist", "wet"),
            listOf("cottage", "rain-garden", "pollinator"),
            "background", false
        ),
        "agave" to PlantSpec(
            "agave.png", "Blue Agave",
            listOf("full-sun"), listOf("dry"),
            listOf("xeriscape", "desert-oasis"),
            "foreground", true
        ),
        "fern" to PlantSpec(
            "fern.png", "Ostrich Fern",
            listOf("partial-shade", "full-shade"), listOf("moist", "wet"),
            listOf("woodland-shade", "rain-garden", "tropical-jungle", "zen"),
            "midground", false
        ),
        "grass" to PlantSpec(
            "grass.png", "Feather Reed Grass",
            listOf("full-sun", "partial-shade"), listOf("dry", "moist", "well-drained"),
            listOf("meadow", "xeriscape", "desert-oasis", "mediterranean", "rock-alpine"),
            "background", false
        ),
        "rose_bush" to PlantSpec(
            "rose_bush.png", "Red Shrub Rose",
            listOf("full-sun"), listOf("moist", "well-drained"),
            listOf("cottage", "formal-french", "pollinator"),
            "background", false
        ),
        "stone" to PlantSpec(
            "stone.png", "Mossy Garden Stone",
            listOf("full-sun", "partial-shade", "full-shade"), listOf("dry", "moist", "wet", "well-drained"),
            listOf("zen", "rock-alpine", "xeriscape", "desert-oasis"),
            "foreground", true
        )
    )

    private val bitmapCache = HashMap<String, Bitmap>()

    private fun getPlantBitmap(context: Context, filename: String): Bitmap? {
        val cached = bitmapCache[filename]
        if (cached != null) return cached
        return try {
            val stream = context.assets.open("plants/$filename")
            val bitmap = BitmapFactory.decodeStream(stream)
            if (bitmap != null) {
                bitmapCache[filename] = bitmap
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getSeasonFilter(season: String): ColorFilter? {
        val matrix = ColorMatrix()
        when (season) {
            "spring" -> {
                matrix.setSaturation(1.2f)
                val brightMatrix = ColorMatrix(floatArrayOf(
                    1.05f, 0f, 0f, 0f, 0f,
                    0f, 1.05f, 0f, 0f, 0f,
                    0f, 0f, 1.05f, 0f, 0f,
                    0f, 0f, 0f, 1.0f, 0f
                ))
                matrix.postConcat(brightMatrix)
                return ColorMatrixColorFilter(matrix)
            }
            "autumn" -> {
                val satMatrix = ColorMatrix()
                satMatrix.setSaturation(0.9f)
                val tintMatrix = ColorMatrix(floatArrayOf(
                    1.15f, 0f, 0f, 0f, 0f,
                    0f, 0.85f, 0f, 0f, 0f,
                    0f, 0f, 0.70f, 0f, 0f,
                    0f, 0f, 0f, 1.0f, 0f
                ))
                satMatrix.postConcat(tintMatrix)
                return ColorMatrixColorFilter(satMatrix)
            }
            "winter" -> {
                val satMatrix = ColorMatrix()
                satMatrix.setSaturation(0.4f)
                val tintMatrix = ColorMatrix(floatArrayOf(
                    0.85f, 0f, 0f, 0f, 0f,
                    0f, 0.95f, 0f, 0f, 0f,
                    0f, 0f, 1.15f, 0f, 0f,
                    0f, 0f, 0f, 1.0f, 0f
                ))
                satMatrix.postConcat(tintMatrix)
                return ColorMatrixColorFilter(satMatrix)
            }
            else -> return null
        }
    }

    fun parseColor(color: String): Int {
        val clean = color.trim()
        if (clean.startsWith("#")) {
            return try {
                Color.parseColor(clean)
            } catch (e: Exception) {
                if (clean.length == 4) {
                    val r = clean[1]
                    val g = clean[2]
                    val b = clean[3]
                    Color.parseColor("#$r$r$g$g$b$b")
                } else {
                    Color.GREEN
                }
            }
        }
        return Color.GREEN
    }

    fun drawPlantOverlay(
        context: Context,
        canvas: Canvas,
        w: Float,
        h: Float,
        state: GardenState,
        conceptIndex: Int
    ) {
        val groundLevel = h * 0.65f
        val canvasHeight = h

        val seedBase = conceptIndex * 15
        val random = SeededRandom(seedBase)

        // 1. Draw a beautiful curved soil bed or gravel edge
        val bedPath = Path()
        bedPath.moveTo(-50f, canvasHeight + 50f)
        bedPath.lineTo(-50f, groundLevel + (random.nextFloat() * 40f - 20f))

        val cp1x = w * 0.3f
        val cp1y = groundLevel - 40f + (random.nextFloat() * 40f)
        val cp2x = w * 0.7f
        val cp2y = groundLevel + 10f + (random.nextFloat() * 40f)

        bedPath.cubicTo(cp1x, cp1y, cp2x, cp2y, w + 50f, groundLevel - 20f)
        bedPath.lineTo(w + 50f, canvasHeight + 50f)
        bedPath.close()

        val bedPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Map theme ID to match PC compatibility database strings
        val activeThemeMapped = when (state.activeTheme) {
            "wildflower" -> "meadow"
            "rain_garden" -> "rain-garden"
            "desert_oasis" -> "desert-oasis"
            "woodland_shade" -> "woodland-shade"
            "formal_french" -> "formal-french"
            "tropical_jungle" -> "tropical-jungle"
            "rock_alpine" -> "rock-alpine"
            else -> state.activeTheme
        }

        val isGravel = activeThemeMapped in listOf("xeriscape", "zen", "desert-oasis", "rock-alpine")
        val textureFilename = if (isGravel) "gravel.jpg" else "mulch.jpg"
        
        val textureBitmap = getPlantBitmap(context, textureFilename)
        if (textureBitmap != null) {
            val shader = BitmapShader(textureBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            
            val water = when (state.soilMoisture) {
                "water_dry" -> "dry"
                "water_moist" -> "moist"
                "water_wet" -> "wet"
                "water_well" -> "well-drained"
                else -> "well-drained"
            }
            if (!isGravel) {
                val factor = when (water) {
                    "wet", "moist" -> 0.7f
                    "dry" -> 1.1f
                    else -> 1.0f
                }
                val matrix = ColorMatrix(floatArrayOf(
                    factor, 0f, 0f, 0f, 0f,
                    0f, factor, 0f, 0f, 0f,
                    0f, 0f, factor, 0f, 0f,
                    0f, 0f, 0f, 1.0f, 0f
                ))
                bedPaint.colorFilter = ColorMatrixColorFilter(matrix)
            }
            bedPaint.shader = shader
        } else {
            val bedColors = if (isGravel) {
                intArrayOf(parseColor("#e5e7eb"), parseColor("#9ca3af"))
            } else {
                intArrayOf(parseColor("#451a03"), parseColor("#1c1917"))
            }
            bedPaint.shader = LinearGradient(
                0f, groundLevel, 0f, canvasHeight,
                bedColors, null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawPath(bedPath, bedPaint)

        // 2. Filter/select compatible plants
        val sun = when (state.sunExposure) {
            "sun_full" -> "full-sun"
            "sun_partial" -> "partial-shade"
            "sun_shade" -> "full-shade"
            else -> "full-sun"
        }
        val water = when (state.soilMoisture) {
            "water_dry" -> "dry"
            "water_moist" -> "moist"
            "water_wet" -> "wet"
            "water_well" -> "well-drained"
            else -> "well-drained"
        }

        val compatiblePlants = ArrayList<Pair<String, Int>>()
        for ((key, spec) in PLANT_DB) {
            var score = 0
            if (activeThemeMapped in spec.themes) {
                score += 5
            }
            if (sun in spec.sun) {
                score += 3
            } else if ("partial-shade" in spec.sun && (sun == "full-sun" || sun == "full-shade")) {
                score += 1
            }
            if (water in spec.water || "well-drained" in spec.water) {
                score += 2
            }
            if (key == "stone") {
                score += 3
            }
            compatiblePlants.add(Pair(key, score))
        }

        compatiblePlants.sortByDescending { it.second }
        var bestPlants = compatiblePlants.filter { it.second > 0 }.map { it.first }
        if (bestPlants.isEmpty()) {
            bestPlants = PLANT_DB.keys.toList()
        }

        // 3. Generate Plant Positions
        val baseDensity = Math.round(state.sqft / 18f)
        var plantCount = Math.max(6, Math.min(48, baseDensity))

        if (conceptIndex == 2) plantCount = Math.round(plantCount * 1.5f) // Lush
        if (conceptIndex == 3) plantCount = Math.round(plantCount * 0.5f) // Minimalist
        plantCount = Math.max(3, plantCount)

        data class PlantInstance(
            val x: Float,
            val y: Float,
            val type: String,
            val rVal: Float
        )

        val plants = ArrayList<PlantInstance>()
        for (i in 0 until plantCount) {
            val px = w * 0.02f + random.nextFloat() * (w * 0.96f)
            val py = groundLevel + (canvasHeight - groundLevel) * (0.05f + 0.9f * random.nextFloat())
            val depthRatio = (py - groundLevel) / (canvasHeight - groundLevel + 1f)
            val plantType = when {
                depthRatio < 0.25f -> "background"
                depthRatio > 0.65f -> "foreground"
                else -> "midground"
            }
            plants.add(PlantInstance(px, py, plantType, random.nextFloat()))
        }

        plants.sortBy { it.y }

        // 4. Render Plant Bitmaps
        val paint = Paint().apply {
            isAntiAlias = true
        }

        plants.forEach { plant ->
            val typeMatchingPlants = bestPlants.filter { PLANT_DB[it]?.type == plant.type }
            val chosenList = if (typeMatchingPlants.isNotEmpty()) typeMatchingPlants else bestPlants
            val basePlantKey = chosenList[(plant.rVal * chosenList.size).toInt() % chosenList.size]

            var plantKey = basePlantKey
            if (state.activeSeason == "winter" && PLANT_DB[plantKey]?.evergreen == false) {
                if (random.nextFloat() > 0.5f) {
                    plantKey = "stone"
                }
            }

            val spec = PLANT_DB[plantKey]
            if (spec != null) {
                val bitmap = getPlantBitmap(context, spec.file)
                if (bitmap != null) {
                    val pw = bitmap.width
                    val ph = bitmap.height

                    var scaleFactor = 0.35f + (0.55f * (plant.y - groundLevel) / (h - groundLevel + 1f))
                    scaleFactor *= (0.7f + random.nextFloat() * 0.5f)
                    
                    val finalScale = when (plantKey) {
                        "grass", "hydrangea", "rose_bush" -> scaleFactor * 1.1f
                        "agave", "stone" -> scaleFactor * 0.85f
                        else -> scaleFactor
                    }

                    val shouldMirror = random.nextFloat() > 0.5f

                    // 1. Draw Drop Shadow
                    val shadowOffsetX = (pw * finalScale * 0.08f)
                    val shadowOffsetY = (ph * finalScale * 0.05f)
                    val shadowBlur = (pw * finalScale * 0.04f).coerceIn(3f, 15f)

                    canvas.save()
                    canvas.translate(plant.x + shadowOffsetX, plant.y + shadowOffsetY + (ph * finalScale * 0.05f))
                    canvas.scale(if (shouldMirror) -finalScale else finalScale, finalScale)
                    
                    val shadowPaint = Paint().apply {
                        isAntiAlias = true
                        colorFilter = PorterDuffColorFilter(Color.argb(120, 15, 12, 10), PorterDuff.Mode.SRC_IN)
                        maskFilter = BlurMaskFilter(shadowBlur, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.drawBitmap(bitmap, -pw / 2f, -ph.toFloat(), shadowPaint)
                    canvas.restore()

                    // 2. Draw Plant
                    canvas.save()
                    canvas.translate(plant.x, plant.y + (ph * finalScale * 0.05f))
                    canvas.scale(if (shouldMirror) -finalScale else finalScale, finalScale)

                    paint.colorFilter = getSeasonFilter(state.activeSeason)
                    canvas.drawBitmap(bitmap, -pw / 2f, -ph.toFloat(), paint)

                    // 3. Draw Winter Frost Overlay (SRC_ATOP)
                    if (state.activeSeason == "winter") {
                        val frostPaint = Paint().apply {
                            isAntiAlias = true
                            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
                            shader = LinearGradient(
                                0f, -ph.toFloat(), 0f, 0f,
                                Color.argb(70, 245, 248, 255), Color.argb(0, 245, 248, 255),
                                Shader.TileMode.CLAMP
                            )
                        }
                        canvas.drawRect(-pw / 2f, -ph.toFloat(), pw / 2f, 0f, frostPaint)
                    }

                    canvas.restore()
                }
            }
        }
    }
}
