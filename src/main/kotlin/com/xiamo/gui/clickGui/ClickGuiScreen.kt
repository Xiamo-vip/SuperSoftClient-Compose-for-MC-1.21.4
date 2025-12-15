package com.xiamo.gui.clickGui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.gui.ComposeScreen
import com.xiamo.gui.hud.HudEditorManager
import com.xiamo.module.Category
import com.xiamo.module.ModuleManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.util.concurrent.CopyOnWriteArrayList

class ClickGuiScreen(val parentScreen: Screen? = null) : ComposeScreen(Text.of("ClickGui")) {
    val categories = CopyOnWriteArrayList<ClickGuiWindow>()

    override fun removed() {
        ModuleManager.modules.find { it.name == "ClickGui" }?.enabled = false
        super.removed()
    }

    @Composable
    override fun renderCompose() {
        val width = 100f
        val height = 20f
        val start = 20f

        LaunchedEffect(Unit) {
            isVisible = true
        }

        val scale by animateSizeAsState(
            if (isVisible) Size(1f, 1f) else Size(0f, 0f),
            tween(durationMillis = 300),
            finishedListener = {
                if (!isVisible) {
                    MinecraftClient.getInstance().setScreen(parentScreen)
                }
            }
        )

        val blurAlpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(durationMillis = 400)
        )

        if (categories.count() == 0) {
            var x = start
            val y = 200
            Category.entries.forEach { category ->
                categories.add(ClickGuiWindow(x.toInt(), y, category, width, height))
                x += width * MinecraftClient.getInstance().window.scaleFactor.toInt() + 20 * MinecraftClient.getInstance().window.scaleFactor.toInt()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(blurAlpha * 0.6f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0, 0, 0, 180),
                                Color(0, 0, 0, 220)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(blurAlpha * 0.4f)
                    .background(Color(0, 0, 0, 100))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f).value)
                .safeContentPadding()
                .graphicsLayer(shadowElevation = 1f)



        ) {
            if (!HudEditorManager.isEditMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize()
                        .scale(scale.width, scale.height)
                ) {
                    categories.forEach { it.renderCompose() }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Button(
                    onClick = {
                        HudEditorManager.toggleEditMode()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (HudEditorManager.isEditMode) Color(108, 53, 222) else Color(40, 40, 40),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (HudEditorManager.isEditMode) "完成编辑" else "编辑HUD",
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    override fun shouldPause(): Boolean {
        return false
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (HudEditorManager.isEditMode) {
            val scale = MinecraftClient.getInstance().window.scaleFactor.toDouble()
            HudEditorManager.getAllComponents()
                .filter { it.isDragging }
                .forEach { it.onDragged(mouseX, mouseY, scale) }
        }

        categories.forEach { it.onDragged(mouseX.toInt(), mouseY.toInt()) }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (HudEditorManager.isEditMode) {
            val scale = MinecraftClient.getInstance().window.scaleFactor.toDouble()
            val allComponents = HudEditorManager.getAllComponents()

            println("[HUD Editor] Click at screen ($mouseX, $mouseY), physical (${mouseX * scale}, ${mouseY * scale})")
            println("[HUD Editor] Total components: ${allComponents.size}")
            allComponents.forEach { comp ->
                println("[HUD Editor] Component: ${comp.id} at (${comp.x}, ${comp.y}) size: ${comp.width}x${comp.height}")
            }

            val clickedComponents = allComponents.filter { it.isMouseOver(mouseX, mouseY, scale) }

            if (clickedComponents.isNotEmpty()) {
                println("[HUD Editor] Clicked components: ${clickedComponents.map { it.id }}")
                val component = clickedComponents.first()
                HudEditorManager.selectComponent(component)
                component.startDragging(mouseX, mouseY, scale)
                return true
            } else {
                println("[HUD Editor] No component clicked, deselecting")
                HudEditorManager.selectComponent(null)
            }
        }

        if (categories.any { it.isHover }) {
            categories.last { it.isHover }.onClicked(mouseX.toInt(), mouseY.toInt())
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (HudEditorManager.isEditMode) {
            HudEditorManager.getAllComponents().forEach { it.stopDragging() }
        }

        categories.forEach { it.onMouseReleased() }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == 256) {
            if (HudEditorManager.isEditMode) {
                HudEditorManager.toggleEditMode()
                return true
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun close() {
        if (HudEditorManager.isEditMode) {
            HudEditorManager.toggleEditMode()
        }
        ModuleManager.modules.find { it.name == "ClickGui" }?.enabled = false
        super.close()
    }
}
