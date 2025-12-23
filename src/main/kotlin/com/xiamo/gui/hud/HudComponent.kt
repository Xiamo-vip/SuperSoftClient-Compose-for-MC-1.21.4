package com.xiamo.gui.hud

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.minecraft.client.MinecraftClient

@Composable
fun HudComponent(
    componentId: String,
    moduleName: String,
    defaultX: Float = 0f,
    defaultY: Float = 0f,
    defaultScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val mc = MinecraftClient.getInstance()
    val windowWidth = mc.window.width.toFloat()
    val windowHeight = mc.window.height.toFloat()

    val isEditMode = HudEditorManager.isEditMode

    val componentData = remember(componentId, moduleName) {
        HudEditorManager.getOrCreateComponent(componentId, moduleName)
    }

    LaunchedEffect(componentId, moduleName) {
        val (savedX, savedY, savedScale) = HudEditorManager.loadComponentPosition(componentId, moduleName, defaultX, defaultY, defaultScale)
        componentData.x = savedX
        componentData.y = savedY
        componentData.scale = savedScale
    }

    val displayX = componentData.x.coerceIn(0f, (windowWidth - componentData.width).coerceAtLeast(0f))
    val displayY = componentData.y.coerceIn(0f, (windowHeight - componentData.height).coerceAtLeast(0f))

    val isSelected = HudEditorManager.isSelected(componentData)

    val borderAlpha by animateFloatAsState(
        targetValue = if (isEditMode) 1f else 0f,
        label = "borderAlpha"
    )

    val animatedScale by animateFloatAsState(
        targetValue = componentData.scale,
        label = "scale"
    )

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    displayX.toInt(),
                    displayY.toInt()
                )
            }
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .then(
                if (isSelected && isEditMode) {
                    Modifier.border(2.dp, Color.Cyan.copy(alpha = borderAlpha), RoundedCornerShape(4.dp))
                } else if (isEditMode) {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.3f * borderAlpha), RoundedCornerShape(4.dp))
                } else {
                    Modifier
                }
            )
            .onGloballyPositioned { coordinates ->
                val newWidth = coordinates.size.width.toFloat()
                val newHeight = coordinates.size.height.toFloat()

                if (componentData.baseWidth != newWidth || componentData.baseHeight != newHeight) {
                    componentData.baseWidth = newWidth
                    componentData.baseHeight = newHeight
                }
            }
    ) {
        content()

        if (isSelected && isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Red.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "-",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        "${(componentData.scale * 100).toInt()}%",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Green.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
