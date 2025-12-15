package com.xiamo.gui.hud

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.xiamo.setting.NumberSetting
import net.minecraft.client.MinecraftClient

@Composable
fun HudComponent(
    componentId: String,
    moduleName: String,
    defaultX: Float = 0f,
    defaultY: Float = 0f,
    content: @Composable () -> Unit
) {



    val isEditMode = HudEditorManager.isEditMode

    val componentData = remember(componentId, moduleName) {
        HudEditorManager.getOrCreateComponent(componentId, moduleName)
    }


    LaunchedEffect(componentId, moduleName) {
        val (savedX, savedY) = HudEditorManager.loadComponentPosition(componentId, moduleName, defaultX, defaultY)
        componentData.x = savedX
        componentData.y = savedY
    }
    val isSelected = HudEditorManager.isSelected(componentData)

    val borderAlpha by animateFloatAsState(
        targetValue = if (isEditMode) 1f else 0f,
        label = "borderAlpha"
    )

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (componentData.x).toInt(),
                    (componentData.y).toInt()
                )
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

                if (componentData.width != newWidth || componentData.height != newHeight) {
                    componentData.width = newWidth
                    componentData.height = newHeight
                }
            }
    ) {
        content()
    }
}