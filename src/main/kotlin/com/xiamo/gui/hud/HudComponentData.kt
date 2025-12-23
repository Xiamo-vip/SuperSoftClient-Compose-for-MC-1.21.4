package com.xiamo.gui.hud

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class HudComponentData(
    val id: String,
    val parentModuleName: String,
    initialX: Float = 0f,
    initialY: Float = 0f,
    initialScale: Float = 1f
) {

    var x by mutableStateOf(initialX)
    var y by mutableStateOf(initialY)
    var scale by mutableStateOf(initialScale)

    var baseWidth by mutableStateOf(0f)
    var baseHeight by mutableStateOf(0f)

    val width: Float get() = baseWidth * scale
    val height: Float get() = baseHeight * scale

    var isVisible by mutableStateOf(true)
    var isDragging = false

    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    fun startDragging(mouseX: Double, mouseY: Double, windowScale: Double) {
        isDragging = true
        val physX = mouseX * windowScale
        val physY = mouseY * windowScale

        dragOffsetX = (physX - x).toFloat()
        dragOffsetY = (physY - y).toFloat()
    }

    fun onDragged(mouseX: Double, mouseY: Double, windowScale: Double) {
        if (!isDragging) return
        val physX = mouseX * windowScale
        val physY = mouseY * windowScale
        x = (physX - dragOffsetX).toFloat()
        y = (physY - dragOffsetY).toFloat()
    }

    fun stopDragging() {
        isDragging = false
    }

    fun adjustScale(delta: Float) {
        val newScale = scale + delta
        scale = (kotlin.math.round(newScale * 20f) / 20f).coerceIn(0.5f, 2.0f)
    }

    fun resetScale() {
        scale = 1.0f
    }

    fun isMouseOver(mouseX: Double, mouseY: Double, windowScale: Double): Boolean {
        if (!isVisible || baseWidth <= 0 || baseHeight <= 0) return false

        val physX = mouseX * windowScale
        val physY = mouseY * windowScale

        return physX >= x && physX <= x + width &&
                physY >= y && physY <= y + height
    }
}
