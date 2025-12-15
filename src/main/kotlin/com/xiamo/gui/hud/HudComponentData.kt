package com.xiamo.gui.hud

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class HudComponentData(
    val id: String,
    val parentModuleName: String,
    initialX: Float = 0f,
    initialY: Float = 0f
) {

    var x by mutableStateOf(initialX)
    var y by mutableStateOf(initialY)

    var width by mutableStateOf(0f)
    var height by mutableStateOf(0f)

    var isVisible by mutableStateOf(true)
    var isDragging = false

    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    fun startDragging(mouseX: Double, mouseY: Double, scale: Double) {
        isDragging = true
        val physX = mouseX * scale
        val physY = mouseY * scale

        dragOffsetX = (physX - x).toFloat()
        dragOffsetY = (physY - y).toFloat()
    }

    fun onDragged(mouseX: Double, mouseY: Double, scale: Double) {
        if (!isDragging) return
        val physX = mouseX * scale
        val physY = mouseY * scale
        x = (physX - dragOffsetX).toFloat()
        y = (physY - dragOffsetY).toFloat()
    }

    fun stopDragging() {
        isDragging = false
    }

    fun isMouseOver(mouseX: Double, mouseY: Double, scale: Double): Boolean {
        if (!isVisible || width <= 0 || height <= 0) return false

        val physX = mouseX * scale
        val physY = mouseY * scale


        return physX >= x && physX <= x + width &&
                physY >= y && physY <= y + height
    }
}