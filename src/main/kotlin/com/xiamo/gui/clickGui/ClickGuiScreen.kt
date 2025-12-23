package com.xiamo.gui.clickGui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

        if (categories.isEmpty()) {
            val client = MinecraftClient.getInstance()
            val window = client.window
            val factor = window.scaleFactor.toFloat()


            val screenWidth = window.scaledWidth
            val windowWidth = 100
            val spacing = 20

            val maxPerRow = (screenWidth - spacing) / (windowWidth + spacing)
            val columns = if (maxPerRow > 0) maxPerRow else 1

            Category.entries.forEachIndexed { index, category ->
                val row = index / columns
                val col = index % columns

                val initialX = spacing + col * (windowWidth + spacing)
                val initialY = spacing + row * 40

                categories.add(
                    ClickGuiWindow(
                        (initialX * factor).toInt(),
                        (initialY * factor).toInt(),
                        category,
                        windowWidth.toFloat(),
                        20f
                    )
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(blurAlpha * 0.6f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0, 0, 0, 180), Color(0, 0, 0, 220))
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(animateFloatAsState(if (isVisible) 1f else 0f).value)
                    .safeContentPadding()
                    .graphicsLayer {
                        scaleX = scale.width
                        scaleY = scale.height
                    }
            ) {
                if (!HudEditorManager.isEditMode) {
                    categories.forEach { it.renderCompose() }
                }

                Button(
                    onClick = { HudEditorManager.toggleEditMode() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (HudEditorManager.isEditMode) Color(108, 53, 222) else Color(40, 40, 40),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (HudEditorManager.isEditMode) "完成编辑" else "编辑HUD",
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    override fun shouldPause(): Boolean = false

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
            val clickedComponents = HudEditorManager.getAllComponents().filter { it.isMouseOver(mouseX, mouseY, scale) }
            if (clickedComponents.isNotEmpty()) {
                val component = clickedComponents.first()
                HudEditorManager.selectComponent(component)
                component.startDragging(mouseX, mouseY, scale)
                return true
            }
            HudEditorManager.selectComponent(null)
        }


        val hoveredWindow = categories.reversed().find { it.isHover }
        if (hoveredWindow != null) {
            hoveredWindow.onClicked(mouseX.toInt(), mouseY.toInt())
            categories.remove(hoveredWindow)
            categories.add(hoveredWindow)
            return true
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

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (HudEditorManager.isEditMode && HudEditorManager.selectedComponent != null) {
            HudEditorManager.onScroll(verticalAmount)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ClickGuiWindow.listeningModule != null) {
            categories.firstOrNull()?.onKeyPressed(keyCode)
            return true
        }
        if (keyCode == 256) {
            if (HudEditorManager.isEditMode) {
                HudEditorManager.toggleEditMode()
                return true
            }
            close()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun close() {
        if (HudEditorManager.isEditMode) HudEditorManager.toggleEditMode()
        isVisible = false
    }
}