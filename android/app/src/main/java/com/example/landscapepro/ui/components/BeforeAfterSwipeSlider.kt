package com.example.landscapepro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class SwipeClipShape(private val progress: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val left = size.width * progress
        return Outline.Rectangle(Rect(left, 0f, size.width, size.height))
    }
}

@Composable
fun BeforeAfterSwipeSlider(
    modifier: Modifier = Modifier,
    beforeImage: @Composable BoxScope.() -> Unit,
    afterImage: @Composable BoxScope.() -> Unit
) {
    var progress by remember { mutableStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f) // Standard photo aspect ratio matching original templates
            .background(Color.Black)
    ) {
        val width = constraints.maxWidth.toFloat()
        if (width > 0) {
            // 1. Before Image (Bottom Layer)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = beforeImage
            )

            // 2. After Image (Top Layer, clipped based on progress)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(SwipeClipShape(progress)),
                contentAlignment = Alignment.Center,
                content = afterImage
            )

            // 3. Draggable divider line and handle
            val dividerX = (progress * maxWidth.value).dp

            // Divider Line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .offset(x = dividerX)
                    .background(Color.White.copy(alpha = 0.8f))
            )

            // Drag Handle (Circle button)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .offset(x = dividerX - 20.dp, y = (maxHeight / 2) - 20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .pointerInput(width) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newProgress = progress + (dragAmount.x / width)
                            progress = newProgress.coerceIn(0f, 1f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Two horizontal indicator lines/arrows representation
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Swipe Handle",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
