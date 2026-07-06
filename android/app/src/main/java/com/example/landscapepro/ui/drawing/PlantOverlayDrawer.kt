package com.example.landscapepro.ui.drawing

import android.graphics.*
import com.example.landscapepro.data.GardenState
import com.example.landscapepro.data.SeededRandom
import kotlin.math.*

object PlantOverlayDrawer {

    fun parseColor(color: String): Int {
        val clean = color.trim()
        if (clean.startsWith("#")) {
            return try {
                Color.parseColor(clean)
            } catch (e: Exception) {
                // Handle 3-digit hex like #f00 to #ff0000
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
        if (clean.startsWith("rgba")) {
            return try {
                val parts = clean.substringAfter("rgba(").substringBefore(")").split(",")
                val r = parts[0].trim().toInt()
                val g = parts[1].trim().toInt()
                val b = parts[2].trim().toInt()
                val a = (parts[3].trim().toFloat() * 255).toInt()
                Color.argb(a, r, g, b)
            } catch (e: Exception) {
                Color.GREEN
            }
        }
        if (clean.startsWith("rgb")) {
            return try {
                val parts = clean.substringAfter("rgb(").substringBefore(")").split(",")
                val r = parts[0].trim().toInt()
                val g = parts[1].trim().toInt()
                val b = parts[2].trim().toInt()
                Color.rgb(r, g, b)
            } catch (e: Exception) {
                Color.GREEN
            }
        }
        return when (clean.lowercase()) {
            "black" -> Color.BLACK
            "white" -> Color.WHITE
            "red" -> Color.RED
            "green" -> Color.GREEN
            "blue" -> Color.BLUE
            "yellow" -> Color.YELLOW
            "cyan" -> Color.CYAN
            "magenta" -> Color.MAGENTA
            "gray" -> Color.GRAY
            "darkgray" -> Color.DKGRAY
            "lightgray" -> Color.LTGRAY
            else -> Color.GREEN
        }
    }

    fun drawPlantOverlay(
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

        // Set mulch or gravel color based on soil and theme
        val bedColors = when (state.activeTheme) {
            "xeriscape" -> intArrayOf(parseColor("#e5e7eb"), parseColor("#9ca3af"))
            "zen" -> intArrayOf(parseColor("#365314"), parseColor("#064e3b"))
            else -> intArrayOf(parseColor("#451a03"), parseColor("#1c1917"))
        }
        bedPaint.shader = LinearGradient(
            0f, groundLevel, 0f, canvasHeight,
            bedColors, null, Shader.TileMode.CLAMP
        )
        canvas.drawPath(bedPath, bedPaint)

        // 2. Draw procedural plants
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
            val px = w * 0.05f + random.nextFloat() * (w * 0.9f)
            val py = groundLevel + (canvasHeight - groundLevel) * (0.05f + 0.9f * random.nextFloat())

            val plantType = when {
                py < groundLevel + (canvasHeight - groundLevel) * 0.3f -> "background"
                py > groundLevel + (canvasHeight - groundLevel) * 0.7f -> "foreground"
                else -> "midground"
            }
            plants.add(PlantInstance(px, py, plantType, random.nextFloat()))
        }

        // Painter's algorithm: sort by Y coordinate
        plants.sortBy { it.y }

        // Draw each plant
        val fillPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        plants.forEach { plant ->
            canvas.save()
            canvas.translate(plant.x, plant.y)

            val scale = 0.7f + plant.rVal * 0.6f
            canvas.scale(scale, scale)

            // Try winter override first
            val isWinter = checkWinterOverride(canvas, plant.type, plant.rVal, state.activeSeason, fillPaint, strokePaint)
            if (!isWinter) {
                when (state.activeTheme) {
                    "cottage" -> drawCottagePlant(canvas, plant.type, plant.rVal, state.perennialRatio, state.soilAcidity, fillPaint, strokePaint)
                    "xeriscape" -> drawXeriscapePlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    "zen" -> drawZenPlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    "wildflower" -> drawMeadowPlant(canvas, plant.type, plant.rVal, state.perennialRatio, fillPaint, strokePaint)
                    "mediterranean" -> drawMediterraneanPlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    "rain_garden" -> drawRainGardenPlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    "desert_oasis" -> drawDesertOasisPlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    "woodland_shade" -> drawWoodlandShadePlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    "formal_french" -> drawFormalFrenchPlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    "tropical_jungle" -> drawTropicalJunglePlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    "pollinator" -> drawPollinatorPlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    "rock_alpine" -> drawRockAlpinePlant(canvas, plant.type, plant.rVal, fillPaint, strokePaint)
                    else -> drawCottagePlant(canvas, plant.type, plant.rVal, state.perennialRatio, state.soilAcidity, fillPaint, strokePaint)
                }
            }
            canvas.restore()
        }

        // Draw Zen Stones if theme is Zen
        if (state.activeTheme == "zen") {
            drawZenStones(canvas, w, h, fillPaint, strokePaint)
        }
    }

    private fun checkWinterOverride(
        canvas: Canvas,
        type: String,
        rVal: Float,
        season: String,
        fillPaint: Paint,
        strokePaint: Paint
    ): Boolean {
        if (season != "winter") return false

        fillPaint.clearShadowLayer()
        strokePaint.clearShadowLayer()

        if (type == "background") {
            // Bare woody branches
            strokePaint.color = parseColor("#374151")
            strokePaint.strokeWidth = 3.5f
            val branchPath = Path().apply {
                moveTo(0f, 0f)
                quadTo(-8f, -35f, -2f, -75f)
            }
            canvas.drawPath(branchPath, strokePaint)

            strokePaint.strokeWidth = 1.8f
            val twigPath = Path().apply {
                moveTo(-4f, -40f)
                quadTo(-18f, -55f, -22f, -45f)
                moveTo(-3f, -55f)
                quadTo(15f, -65f, 20f, -52f)
            }
            canvas.drawPath(twigPath, strokePaint)

            // Tiny red berries
            fillPaint.color = parseColor("#be123c")
            canvas.drawCircle(-22f, -45f, 3f, fillPaint)
            canvas.drawCircle(20f, -52f, 3f, fillPaint)
            canvas.drawCircle(-2f, -75f, 3f, fillPaint)

            // Snow caps on branches
            fillPaint.color = Color.WHITE
            val snowCap = RectF(-7f, -80.5f, 3f, -75.5f)
            canvas.drawOval(snowCap, fillPaint)
        } else if (type == "midground") {
            // Dormant/frosted grass clump
            strokePaint.color = parseColor("#475569")
            strokePaint.strokeWidth = 1.5f
            for (i in 0 until 7) {
                val h = 20f + rVal * 10f
                val angle = -0.5f + (i * 1.0f) / 6f
                canvas.save()
                canvas.rotate(Math.toDegrees(angle.toDouble()).toFloat())
                canvas.drawLine(0f, 0f, 0f, -h, strokePaint)
                canvas.restore()
            }

            // Snow pile at base
            fillPaint.color = parseColor("#f8fafc")
            val snowBase = RectF(-16f, -5f, 16f, 5f)
            canvas.drawOval(snowBase, fillPaint)
        } else {
            // Snow mound with small grey rock
            fillPaint.color = parseColor("#64748b")
            val rockPath = Path().apply {
                moveTo(-8f, 0f)
                lineTo(-4f, -6f)
                lineTo(4f, -5f)
                lineTo(8f, 0f)
                close()
            }
            canvas.drawPath(rockPath, fillPaint)

            fillPaint.color = Color.WHITE
            val snowCap = RectF(-12f, -4f, 12f, 4f)
            canvas.drawOval(snowCap, fillPaint)
        }

        return true
    }

    private fun drawCottagePlant(
        canvas: Canvas,
        type: String,
        rVal: Float,
        perennialRatio: Int,
        acidity: String,
        fillPaint: Paint,
        strokePaint: Paint
    ) {
        val isPerennial = (rVal * 100) < perennialRatio
        var bloomColor1 = "#ec4899"
        var bloomColor2 = "#f43f5e"

        when (acidity) {
            "acidic" -> {
                bloomColor1 = "#3b82f6"
                bloomColor2 = "#1d4ed8"
                if (isPerennial && rVal > 0.5f) {
                    bloomColor1 = "#8b5cf6"
                }
            }
            "alkaline" -> {
                bloomColor1 = "#f472b6"
                bloomColor2 = "#be185d"
                if (!isPerennial && rVal > 0.7f) {
                    bloomColor1 = "#f43f5e"
                }
            }
            else -> {
                if (isPerennial) {
                    bloomColor1 = "#8b5cf6"
                    bloomColor2 = "#6366f1"
                } else if (rVal > 0.6f) {
                    bloomColor1 = "#f59e0b"
                    bloomColor2 = "#ef4444"
                }
            }
        }

        if (type == "background") {
            // Delphiniums / tall spires
            strokePaint.color = parseColor("#065f46")
            strokePaint.strokeWidth = 4f
            canvas.drawLine(0f, 0f, 0f, -90f, strokePaint)

            fillPaint.color = parseColor(bloomColor2)
            for (i in 0 until 15) {
                val fy = -40f - (i * 3.5f)
                val fx = sin(i.toDouble()).toFloat() * 5f
                canvas.drawCircle(fx, fy, 8f - (i * 0.3f), fillPaint)
            }

            fillPaint.color = parseColor(bloomColor1)
            for (i in 0 until 15) {
                val fy = -40f - (i * 3.5f)
                val fx = sin(i.toDouble()).toFloat() * 5f
                canvas.drawCircle(fx + 2f, fy, 4f - (i * 0.15f), fillPaint)
            }
        } else if (type == "midground") {
            // Bush
            fillPaint.color = parseColor("#0f766e")
            canvas.drawCircle(0f, -10f, 25f, fillPaint)
            canvas.drawCircle(-15f, -20f, 20f, fillPaint)
            canvas.drawCircle(15f, -20f, 20f, fillPaint)

            fillPaint.color = parseColor(bloomColor1)
            val flowerCount = 6 + (rVal * 6).toInt()
            for (i in 0 until flowerCount) {
                val fx = -20f + (i * 7f) + (sin(i * 10.0).toFloat() * 4f)
                val fy = -15f - (abs(cos(i * 5.0)).toFloat() * 15f)
                canvas.drawCircle(fx, fy, 6f, fillPaint)

                val centerPaint = Paint(fillPaint).apply { color = parseColor("#fef08a") }
                canvas.drawCircle(fx, fy, 2f, centerPaint)
            }
        } else {
            // Low border
            fillPaint.color = parseColor("#10b981")
            canvas.drawCircle(0f, -5f, 12f, fillPaint)

            fillPaint.color = parseColor(bloomColor2)
            for (i in 0 until 8) {
                val fx = sin(i.toDouble()).toFloat() * 12f
                val fy = -5f - (rVal * 15f) * cos(i.toDouble()).toFloat()
                canvas.drawCircle(fx, fy, 3.5f, fillPaint)
            }
        }
    }

    private fun drawXeriscapePlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            strokePaint.color = parseColor("#5f370e")
            strokePaint.strokeWidth = 6f
            val trunkPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, -50f)
                lineTo(-15f, -75f)
                moveTo(0f, -50f)
                lineTo(15f, -70f)
            }
            canvas.drawPath(trunkPath, strokePaint)

            val drawYuccaHead = { hx: Float, hy: Float ->
                canvas.save()
                canvas.translate(hx, hy)
                strokePaint.color = parseColor("#166534")
                strokePaint.strokeWidth = 1.5f
                var angle = 0f
                while (angle < Math.PI * 2) {
                    val len = 15f + rVal * 8f
                    canvas.drawLine(0f, 0f, cos(angle) * len, sin(angle) * len, strokePaint)
                    angle += 0.2f
                }
                canvas.restore()
            }

            drawYuccaHead(-15f, -75f)
            drawYuccaHead(15f, -70f)
            drawYuccaHead(0f, -50f)
        } else if (type == "midground") {
            fillPaint.color = parseColor("#065f46")
            strokePaint.color = parseColor("#047857")
            strokePaint.strokeWidth = 2f

            for (i in 0 until 12) {
                canvas.save()
                val angle = -Math.PI + (i * Math.PI) / 11.0
                canvas.rotate(Math.toDegrees(angle).toFloat())

                val leafPath = Path().apply {
                    moveTo(0f, 0f)
                    quadTo(8f, -12f, 0f, -35f - (rVal * 15f))
                    quadTo(-8f, -12f, 0f, 0f)
                }
                canvas.drawPath(leafPath, fillPaint)
                canvas.drawPath(leafPath, strokePaint)
                canvas.restore()
            }
        } else {
            val rad = 14f + rVal * 8f
            fillPaint.color = parseColor("#15803d")
            strokePaint.color = parseColor("#f59e0b")
            strokePaint.strokeWidth = 1.5f

            canvas.drawCircle(0f, -rad, rad, fillPaint)

            var angle = 0f
            while (angle < Math.PI * 2) {
                val x1 = cos(angle) * rad
                val y1 = -rad + sin(angle) * rad
                val x2 = cos(angle) * (rad + 4f)
                val y2 = -rad + sin(angle) * (rad + 4f)
                canvas.drawLine(x1, y1, x2, y2, strokePaint)
                angle += 0.3f
            }
        }
    }

    private fun drawZenPlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            strokePaint.color = parseColor("#292524")
            strokePaint.strokeWidth = 4f
            val trunkPath = Path().apply {
                moveTo(0f, 0f)
                quadTo(-15f, -40f, 5f, -80f)
            }
            canvas.drawPath(trunkPath, strokePaint)

            strokePaint.strokeWidth = 2f
            val branchPath = Path().apply {
                moveTo(5f, -80f)
                quadTo(-20f, -90f, -35f, -70f)
                moveTo(5f, -80f)
                quadTo(30f, -90f, 40f, -75f)
            }
            canvas.drawPath(branchPath, strokePaint)

            val drawMapleFoliage = { fx: Float, fy: Float, scale: Float ->
                fillPaint.color = parseColor("rgba(153, 27, 27, 0.9)")
                for (i in 0 until 8) {
                    val ox = sin(i.toDouble()).toFloat() * 18f * scale
                    val oy = cos(i.toDouble()).toFloat() * 12f * scale
                    canvas.drawCircle(fx + ox, fy + oy, 12f * scale, fillPaint)
                }
            }

            drawMapleFoliage(5f, -80f, 1.0f)
            drawMapleFoliage(-35f, -70f, 0.8f)
            drawMapleFoliage(40f, -75f, 0.8f)
        } else if (type == "midground") {
            strokePaint.color = parseColor("#14532d")
            strokePaint.strokeWidth = 3f

            val count = 3 + (rVal * 3).toInt()
            for (b in 0 until count) {
                val bx = -15f + b * 12f
                val length = 70f + rVal * 30f

                canvas.drawLine(bx, 0f, bx - (b * 2f), -length, strokePaint)

                fillPaint.color = parseColor("#166534")
                for (l in 0 until 5) {
                    val ly = -10f - (l * 12f)
                    canvas.save()
                    val ratio = ly / -length
                    canvas.translate(bx - (b * 2f * ratio), ly)
                    canvas.rotate(Math.toDegrees(0.5 - (l % 2)).toFloat())
                    val leafOval = RectF(-8f, -2f, 8f, 2f)
                    canvas.drawOval(leafOval, fillPaint)
                    canvas.restore()
                }
            }
        } else {
            fillPaint.color = parseColor("#065f46")
            for (i in 0 until 10) {
                canvas.save()
                val angle = -Math.PI * 0.9 + (i * Math.PI * 0.8) / 9.0
                canvas.rotate(Math.toDegrees(angle).toFloat())

                val leafPath = Path().apply {
                    moveTo(0f, 0f)
                    quadTo(12f, -8f, 0f, -22f)
                    quadTo(-12f, -8f, 0f, 0f)
                }
                canvas.drawPath(leafPath, fillPaint)
                canvas.restore()
            }

            strokePaint.color = parseColor("#84cc16")
            strokePaint.strokeWidth = 0.8f
            for (i in 0 until 10) {
                canvas.save()
                val angle = -Math.PI * 0.9 + (i * Math.PI * 0.8) / 9.0
                canvas.rotate(Math.toDegrees(angle).toFloat())

                val leafPath = Path().apply {
                    moveTo(0f, 0f)
                    quadTo(12f, -8f, 0f, -22f)
                    quadTo(-12f, -8f, 0f, 0f)
                }
                canvas.drawPath(leafPath, strokePaint)
                canvas.restore()
            }
        }
    }

    private fun drawMeadowPlant(canvas: Canvas, type: String, rVal: Float, perennialRatio: Int, fillPaint: Paint, strokePaint: Paint) {
        strokePaint.color = parseColor("#065f46")
        strokePaint.strokeWidth = 1.2f
        val grassCount = 12 + (rVal * 10).toInt()
        for (g in 0 until grassCount) {
            val angle = -0.3f + (g * 0.6f) / grassCount
            val length = 40f + rVal * 30f

            val grassPath = Path().apply {
                moveTo(-10f + g * 2f, 0f)
                quadTo(-10f + g * 2f + sin(g.toDouble()).toFloat() * 5f, -length * 0.5f, -10f + g * 2f + angle * 25f, -length)
            }
            canvas.drawPath(grassPath, strokePaint)
        }

        val isPerennial = (rVal * 100) < perennialRatio
        var bloomColor = "#ef4444"
        if (isPerennial) {
            bloomColor = "#3b82f6"
        } else if (rVal > 0.7f) {
            bloomColor = "#eab308"
        }

        fillPaint.color = parseColor(bloomColor)
        val flowerCount = 3 + (rVal * 5).toInt()
        for (f in 0 until flowerCount) {
            val fx = -12f + (f * 6f) + (sin(f.toDouble()).toFloat() * 4f)
            val fy = -25f - (f * 8f)

            canvas.drawCircle(fx, fy, 4f, fillPaint)

            val centerPaint = Paint(fillPaint).apply { color = parseColor("#111827") }
            canvas.drawCircle(fx, fy, 1.2f, centerPaint)
        }
    }

    private fun drawMediterraneanPlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            strokePaint.color = parseColor("#3e2723")
            strokePaint.strokeWidth = 3.5f
            val trunk = Path().apply {
                moveTo(0f, 0f)
                quadTo(-10f, -35f, 0f, -65f)
            }
            canvas.drawPath(trunk, strokePaint)

            strokePaint.color = parseColor("#4e342e")
            strokePaint.strokeWidth = 1.8f
            val branches = Path().apply {
                moveTo(0f, -65f)
                quadTo(-20f, -75f, -25f, -60f)
                moveTo(0f, -65f)
                quadTo(20f, -75f, 25f, -55f)
            }
            canvas.drawPath(branches, strokePaint)

            val drawOliveLeaves = { lx: Float, ly: Float ->
                fillPaint.color = parseColor("rgba(107, 114, 92, 0.85)")
                for (i in 0 until 6) {
                    canvas.drawCircle(lx + sin(i.toDouble()).toFloat() * 10f, ly + cos(i.toDouble()).toFloat() * 6f, 8f, fillPaint)
                }
            }
            drawOliveLeaves(-25f, -60f)
            drawOliveLeaves(25f, -55f)
            drawOliveLeaves(0f, -65f)
        } else if (type == "midground") {
            fillPaint.color = parseColor("#4b5320")
            canvas.drawCircle(0f, -8f, 16f, fillPaint)

            fillPaint.color = parseColor("#8b5cf6")
            for (i in 0 until 8) {
                val h = 25f + rVal * 15f
                val angle = -0.5f + (i * 1.0f) / 7f
                canvas.save()
                canvas.rotate(Math.toDegrees(angle.toDouble()).toFloat())
                canvas.drawRect(-2f, -h, 2f, -8f, fillPaint)

                val dotPaint = Paint(fillPaint).apply { color = parseColor("#a78bfa") }
                canvas.drawCircle(0f, -h, 3.5f, dotPaint)
                canvas.drawCircle(0f, -h + 5f, 3f, dotPaint)
                canvas.restore()
            }
        } else {
            // Terracotta pot with red geraniums
            fillPaint.color = parseColor("#c2410c")
            val pot = Path().apply {
                moveTo(-10f, 0f)
                lineTo(10f, 0f)
                lineTo(7f, 12f)
                lineTo(-7f, 12f)
                close()
            }
            canvas.drawPath(pot, fillPaint)

            fillPaint.color = parseColor("#15803d")
            canvas.drawCircle(0f, -3f, 9f, fillPaint)

            fillPaint.color = parseColor("#ef4444")
            for (i in 0 until 5) {
                canvas.drawCircle(-6f + i * 3f, -4f - (sin(i.toDouble()).toFloat() * 2f), 3f, fillPaint)
            }
        }
    }

    private fun drawRainGardenPlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            strokePaint.color = parseColor("#0f5132")
            strokePaint.strokeWidth = 2.5f
            val rushCount = 4 + (rVal * 3).toInt()
            for (i in 0 until rushCount) {
                val rx = -12f + i * 8f
                val len = 65f + rVal * 25f

                val rushPath = Path().apply {
                    moveTo(rx, 0f)
                    quadTo(rx + sin(i.toDouble()).toFloat() * 6f, -len * 0.5f, rx - 5f, -len)
                }
                canvas.drawPath(rushPath, strokePaint)

                fillPaint.color = parseColor("#422006")
                val seedRect = RectF(rx - 8f, -len - 3f, rx - 2f, -len + 3f)
                canvas.drawOval(seedRect, fillPaint)
            }
        } else if (type == "midground") {
            fillPaint.color = parseColor("#198754")
            for (f in 0 until 8) {
                canvas.save()
                val angle = -0.9 + (f * 1.8) / 7.0
                canvas.rotate(Math.toDegrees(angle).toFloat())

                val fernPath = Path().apply {
                    moveTo(0f, 0f)
                    quadTo(8f, -12f, 0f, -28f - rVal * 12f)
                    quadTo(-8f, -12f, 0f, 0f)
                }
                canvas.drawPath(fernPath, fillPaint)

                val leafletPaint = Paint(fillPaint).apply { color = parseColor("#146c43") }
                for (i in 0 until 4) {
                    canvas.drawCircle(4f, -8f - (i * 4f), 3f, leafletPaint)
                    canvas.drawCircle(-4f, -8f - (i * 4f), 3f, leafletPaint)
                }
                canvas.restore()
            }
        } else {
            strokePaint.color = parseColor("#146c43")
            strokePaint.strokeWidth = 2f
            canvas.drawLine(0f, 0f, -5f, -28f, strokePaint)

            fillPaint.color = parseColor("#3b82f6")
            canvas.drawCircle(-5f, -28f, 5f, fillPaint)

            fillPaint.color = parseColor("#60a5fa")
            canvas.drawCircle(-8f, -25f, 4f, fillPaint)
            canvas.drawCircle(-2f, -25f, 4f, fillPaint)
        }
    }

    private fun drawDesertOasisPlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            fillPaint.color = parseColor("#2d6a4f")
            strokePaint.color = parseColor("#1b4332")
            strokePaint.strokeWidth = 2f

            val trunkW = 12f
            val trunkH = 80f + rVal * 30f

            val trunkPath = Path().apply {
                val rect = RectF(-trunkW / 2f, -trunkH, trunkW / 2f, 0f)
                addRoundRect(rect, floatArrayOf(6f, 6f, 6f, 6f, 0f, 0f, 0f, 0f), Path.Direction.CW)
            }
            canvas.drawPath(trunkPath, fillPaint)
            canvas.drawPath(trunkPath, strokePaint)

            val leftArm = Path().apply {
                moveTo(-trunkW / 2f, -trunkH * 0.4f)
                lineTo(-trunkW / 2f - 14f, -trunkH * 0.4f)
                lineTo(-trunkW / 2f - 14f, -trunkH * 0.7f)
                lineTo(-trunkW / 2f - 4f, -trunkH * 0.7f)
                lineTo(-trunkW / 2f - 4f, -trunkH * 0.48f)
                lineTo(-trunkW / 2f, -trunkH * 0.48f)
                close()
            }
            canvas.drawPath(leftArm, fillPaint)
            canvas.drawPath(leftArm, strokePaint)

            val rightArm = Path().apply {
                moveTo(trunkW / 2f, -trunkH * 0.5f)
                lineTo(trunkW / 2f + 12f, -trunkH * 0.5f)
                lineTo(trunkW / 2f + 12f, -trunkH * 0.78f)
                lineTo(trunkW / 2f + 4f, -trunkH * 0.78f)
                lineTo(trunkW / 2f + 4f, -trunkH * 0.58f)
                lineTo(trunkW / 2f, -trunkH * 0.58f)
                close()
            }
            canvas.drawPath(rightArm, fillPaint)
            canvas.drawPath(rightArm, strokePaint)
        } else if (type == "midground") {
            fillPaint.color = parseColor("#40916c")
            strokePaint.color = parseColor("#f59e0b")
            strokePaint.strokeWidth = 0.8f

            val baseLobe = RectF(-12f, -28f, 12f, 4f)
            canvas.drawOval(baseLobe, fillPaint)
            canvas.drawOval(baseLobe, strokePaint)

            canvas.save()
            canvas.translate(-8f, -24f)
            canvas.rotate(-22.9f) // -0.4 radians
            val leftLobe = RectF(-9f, -12f, 9f, 12f)
            canvas.drawOval(leftLobe, fillPaint)
            canvas.drawOval(leftLobe, strokePaint)
            canvas.restore()

            canvas.save()
            canvas.translate(8f, -22f)
            canvas.rotate(17.2f) // 0.3 radians
            val rightLobe = RectF(-8f, -11f, 8f, 11f)
            canvas.drawOval(rightLobe, fillPaint)
            canvas.drawOval(rightLobe, strokePaint)
            canvas.restore()

            fillPaint.color = parseColor("#ec4899")
            canvas.drawCircle(-14f, -34f, 3f, fillPaint)
            canvas.drawCircle(14f, -30f, 3f, fillPaint)
        } else {
            fillPaint.color = parseColor("#52b788")
            strokePaint.color = parseColor("#2d6a4f")
            strokePaint.strokeWidth = 1.2f

            for (i in 0 until 8) {
                canvas.save()
                val angle = -Math.PI * 0.8 + (i * Math.PI * 0.6) / 7.0
                canvas.rotate(Math.toDegrees(angle).toFloat())

                val agavePath = Path().apply {
                    moveTo(0f, 0f)
                    quadTo(6f, -6f, 0f, -18f)
                    quadTo(-6f, -6f, 0f, 0f)
                }
                canvas.drawPath(agavePath, fillPaint)
                canvas.drawPath(agavePath, strokePaint)
                canvas.restore()
            }
        }
    }

    private fun drawWoodlandShadePlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            strokePaint.color = parseColor("#3e2723")
            strokePaint.strokeWidth = 3f
            val trunk = Path().apply {
                moveTo(0f, 0f)
                quadTo(-8f, -35f, -4f, -70f)
            }
            canvas.drawPath(trunk, strokePaint)

            strokePaint.color = parseColor("#4e342e")
            strokePaint.strokeWidth = 1.5f
            val branches = Path().apply {
                moveTo(-6f, -45f)
                lineTo(-20f, -58f)
                moveTo(-5f, -55f)
                lineTo(15f, -68f)
            }
            canvas.drawPath(branches, strokePaint)

            fillPaint.color = parseColor("#b91c1c")
            val clusters = arrayOf(PointF(-4f, -70f), PointF(-20f, -58f), PointF(15f, -68f))
            clusters.forEach { c ->
                for (i in 0 until 5) {
                    canvas.drawCircle(c.x + sin(i.toDouble()).toFloat() * 8f, c.y + cos(i.toDouble()).toFloat() * 5f, 5f, fillPaint)
                }
            }
        } else if (type == "midground") {
            fillPaint.color = parseColor("#0f5132")
            strokePaint.color = parseColor("#e0e7ff")
            strokePaint.strokeWidth = 1.2f

            for (i in 0 until 9) {
                canvas.save()
                val angle = -Math.PI * 0.8 + (i * Math.PI * 0.6) / 8.0
                canvas.rotate(Math.toDegrees(angle).toFloat())

                val hostaPath = Path().apply {
                    moveTo(0f, 0f)
                    quadTo(12f, -8f, 0f, -22f - rVal * 8f)
                    quadTo(-12f, -8f, 0f, 0f)
                }
                canvas.drawPath(hostaPath, fillPaint)
                canvas.drawPath(hostaPath, strokePaint)
                canvas.restore()
            }
        } else {
            fillPaint.color = parseColor("#3f6212")
            val moss1 = RectF(-20f - rVal * 15f, -6f, 20f + rVal * 15f, 6f)
            canvas.drawOval(moss1, fillPaint)

            fillPaint.color = parseColor("#65a30d")
            canvas.save()
            canvas.rotate(-5.7f) // -0.1 radians
            val moss2 = RectF(-14f - rVal * 8f, -4f, 6f + rVal * 8f, 2f)
            canvas.drawOval(moss2, fillPaint)
            canvas.restore()
        }
    }

    private fun drawFormalFrenchPlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            fillPaint.color = parseColor("#14532d")
            val cone = Path().apply {
                moveTo(0f, -75f)
                lineTo(-20f, 0f)
                lineTo(20f, 0f)
                close()
            }
            canvas.drawPath(cone, fillPaint)

            strokePaint.color = parseColor("#292524")
            strokePaint.strokeWidth = 3f
            canvas.drawLine(0f, 0f, 0f, 8f, strokePaint)
        } else if (type == "midground") {
            fillPaint.color = parseColor("#165b33")
            canvas.drawCircle(0f, -18f, 18f, fillPaint)

            strokePaint.color = parseColor("#292524")
            strokePaint.strokeWidth = 2.5f
            canvas.drawLine(0f, 0f, 0f, -4f, strokePaint)
        } else {
            fillPaint.color = parseColor("#1b4332")
            canvas.drawRect(-22f, -10f, 22f, 0f, fillPaint)

            strokePaint.color = parseColor("#40916c")
            strokePaint.strokeWidth = 1f
            canvas.drawRect(-22f, -10f, 22f, 0f, strokePaint)
        }
    }

    private fun drawTropicalJunglePlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            strokePaint.color = parseColor("#1b4332")
            strokePaint.strokeWidth = 3.5f
            val palmStem = Path().apply {
                moveTo(0f, 0f)
                quadTo(-15f, -30f, 20f, -70f)
            }
            canvas.drawPath(palmStem, strokePaint)

            strokePaint.color = parseColor("#2d6a4f")
            strokePaint.strokeWidth = 2f
            for (i in 0 until 15) {
                val px = 10f + i * 1.5f
                val py = -20f - i * 3.5f
                canvas.drawLine(px, py, px - 16f - (rVal * 10f), py + 12f + (i * 0.5f), strokePaint)
            }
        } else if (type == "midground") {
            fillPaint.color = parseColor("#0f5132")
            for (i in 0 until 5) {
                canvas.save()
                val angle = -0.6f + i * 0.3f
                canvas.rotate(Math.toDegrees(angle.toDouble()).toFloat())

                val leaf = RectF(-16f, -44f - rVal * 8f, 16f, 0f)
                canvas.drawOval(leaf, fillPaint)

                // Slits (transparent overlays simulated by drawing background-colored lines)
                strokePaint.color = parseColor("#141a26")
                strokePaint.strokeWidth = 2f
                canvas.drawLine(-10f, -26f, -4f, -20f, strokePaint)
                canvas.drawLine(10f, -26f, 4f, -20f, strokePaint)
                canvas.restore()
            }
        } else {
            strokePaint.color = parseColor("#1b4332")
            strokePaint.strokeWidth = 2.5f
            canvas.drawLine(0f, 0f, -5f, -30f, strokePaint)

            fillPaint.color = parseColor("#f97316")
            val beak = Path().apply {
                moveTo(-5f, -30f)
                lineTo(-20f, -35f)
                lineTo(-5f, -42f)
                close()
            }
            canvas.drawPath(beak, fillPaint)

            fillPaint.color = parseColor("#06b6d4")
            val blueSpikes = Path().apply {
                moveTo(-5f, -30f)
                lineTo(-2f, -48f)
                lineTo(-8f, -44f)
                close()
            }
            canvas.drawPath(blueSpikes, fillPaint)
        }
    }

    private fun drawPollinatorPlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            strokePaint.color = parseColor("#0f766e")
            strokePaint.strokeWidth = 2f
            canvas.drawLine(0f, 0f, -4f, -55f - rVal * 15f, strokePaint)

            fillPaint.color = parseColor("#df73ff")
            canvas.save()
            canvas.translate(-4f, -55f - rVal * 15f)
            canvas.rotate(11.45f) // 0.2 radians
            val flower = RectF(-12f, -4f, 12f, 4f)
            canvas.drawOval(flower, fillPaint)
            canvas.restore()

            val conePaint = Paint(fillPaint).apply { color = parseColor("#78350f") }
            canvas.drawCircle(-4f, -58f - rVal * 15f, 4.5f, conePaint)
        } else if (type == "midground") {
            fillPaint.color = parseColor("#065f46")
            canvas.drawCircle(0f, -10f, 20f, fillPaint)

            fillPaint.color = parseColor("#a78bfa")
            for (i in 0 until 6) {
                val h = 18f + rVal * 10f
                val fx = -15f + i * 6f
                canvas.drawRect(fx - 2f, -h - 8f, fx + 2f, -8f, fillPaint)
                canvas.drawCircle(fx, -h - 8f, 4f, fillPaint)
            }

            // Draw tiny honeybee dot!
            val beePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                color = parseColor("#fbbf24")
            }
            canvas.save()
            canvas.translate(18f, -32f)
            canvas.rotate(11.45f) // 0.2 radians
            val beeBody = RectF(-2.5f, -1.8f, 2.5f, 1.8f)
            canvas.drawOval(beeBody, beePaint)

            val blackPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = parseColor("#111827")
            }
            canvas.drawLine(-1f, -1.8f, -1f, 1.8f, blackPaint)
            canvas.restore()
        } else {
            fillPaint.color = parseColor("#ec4899")
            val phlox = RectF(-16f - rVal * 10f, -5f, 16f + rVal * 10f, 5f)
            canvas.drawOval(phlox, fillPaint)

            fillPaint.color = parseColor("#f472b6")
            for (i in 0 until 6) {
                canvas.drawCircle(-8f + i * 3.2f, -1f - cos(i.toDouble()).toFloat() * 1.5f, 2.5f, fillPaint)
            }
        }
    }

    private fun drawRockAlpinePlant(canvas: Canvas, type: String, rVal: Float, fillPaint: Paint, strokePaint: Paint) {
        if (type == "background") {
            fillPaint.color = parseColor("#064e3b")
            val conifer = Path().apply {
                moveTo(0f, -55f - rVal * 15f)
                lineTo(-14f, -20f)
                lineTo(14f, -20f)
                close()
            }
            canvas.drawPath(conifer, fillPaint)

            val coniferBase = Path().apply {
                moveTo(0f, -35f)
                lineTo(-18f, 0f)
                lineTo(18f, 0f)
                close()
            }
            canvas.drawPath(coniferBase, fillPaint)
        } else if (type == "midground") {
            fillPaint.color = parseColor("#475569")
            val rock = RectF(-15f, -6f, 15f, 6f)
            canvas.drawOval(rock, fillPaint)

            fillPaint.color = parseColor("#db2777")
            for (i in 0 until 7) {
                canvas.drawCircle(-10f + i * 3.5f, -4f - sin(i.toDouble()).toFloat() * 1.5f, 3f, fillPaint)
            }
        } else {
            fillPaint.color = parseColor("#4b5563")
            val rock = Path().apply {
                moveTo(-10f, 0f)
                lineTo(-4f, -8f)
                lineTo(6f, -6f)
                lineTo(10f, 0f)
                close()
            }
            canvas.drawPath(rock, fillPaint)

            fillPaint.color = parseColor("#4d7c0f")
            canvas.save()
            canvas.rotate(5.73f) // 0.1 radians
            val moss = RectF(-2f, -2f, 10f, 0f)
            canvas.drawOval(moss, fillPaint)
            canvas.restore()
        }
    }

    private fun drawZenStones(canvas: Canvas, w: Float, h: Float, fillPaint: Paint, strokePaint: Paint) {
        fillPaint.color = parseColor("#4b5563")
        strokePaint.color = parseColor("#374151")
        strokePaint.strokeWidth = 2f

        data class Stone(val x: Float, val y: Float, val rx: Float, val ry: Float)
        val stones = arrayOf(
            Stone(w * 0.25f, h * 0.85f, 35f, 12f),
            Stone(w * 0.45f, h * 0.78f, 28f, 9f),
            Stone(w * 0.65f, h * 0.88f, 42f, 15f)
        )

        stones.forEach { stone ->
            canvas.save()
            canvas.translate(stone.x, stone.y)
            canvas.rotate(5.73f) // 0.1 radians in degrees is ~5.73

            val stoneRect = RectF(-stone.rx, -stone.ry, stone.rx, stone.ry)
            canvas.drawOval(stoneRect, fillPaint)
            canvas.drawOval(stoneRect, strokePaint)

            // Texture highlight
            val highlightPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.argb(38, 255, 255, 255) // rgba(255,255,255,0.15)
            }
            val highlightRect = RectF(-stone.rx + 5f, -stone.ry + 3f, stone.rx - 5f, stone.ry - 3f)
            canvas.drawArc(highlightRect, 180f, 180f, false, highlightPaint)

            canvas.restore()
        }
    }
}
