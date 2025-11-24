package com.xiamo.gui.clickGui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.Category
import com.xiamo.module.ModuleManager
import net.minecraft.client.MinecraftClient
import org.jetbrains.skia.FontStyleSet
import kotlin.math.roundToInt

class ClickGuiWindow(val x: Int, val y: Int, val category: Category, val width: Int) {


    var windowX by mutableStateOf(x.toFloat())
    var windowY by mutableStateOf(y.toFloat())
    public var isDragging = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    val categoryTitleFont = 45.sp
    val textStyle = TextStyle(fontSize = categoryTitleFont)
    val radius: Dp = 6.dp
    var height : Int = 30

    val backgroundColor = Color(26, 26, 26)
    val titleBgColor = Color(0, 0, 0)
    val moduelHoverBgColor = Color(105, 180, 255)
    val moduelEnabledBgColor = Color(108, 53, 222)

    @Composable
    fun renderCompose() {
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val textHeight = textMeasurer.measure(category.name, textStyle).size.height
        val heightDp = with(density) { textHeight.toDp() }
        height = (heightDp + 20.dp).value.toInt()
        

        Column(
            modifier = Modifier
                .offset { IntOffset(windowX.roundToInt(), windowY.roundToInt()) }
                .width(width.dp)
                .background(backgroundColor, RoundedCornerShape(radius))
               ,
            verticalArrangement = Arrangement.Center
        ) {

            Card(
                elevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heightDp + 20.dp)
                    .shadow(5.dp, RoundedCornerShape(radius)),
                backgroundColor = titleBgColor,
                shape = RoundedCornerShape(radius)
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = category.name,
                    fontSize = categoryTitleFont,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }

            ModuleManager.modules.filter { it.category.name == category.name }.forEach {
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()
                val moduleBgColor by animateColorAsState(
                    targetValue = if (isHovered) moduelHoverBgColor else if (it.enabled) moduelEnabledBgColor else titleBgColor,
                    animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                )



                Text(
                    text = it.name,
                    fontSize = (categoryTitleFont.value - 5).sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    modifier = Modifier
                        .hoverable(interactionSource)
                        .fillMaxWidth()
                        .background(moduleBgColor)
                        .padding(horizontal = 8.dp)
                        .clickable(onClick = {
                            it.toggle()
                        })
                )
            }
        }
    }

    fun onDragged(mouseX:Int , mouseY:Int){
        if (!isDragging) return

        val scale = MinecraftClient.getInstance().window.scaleFactor
        val physX = mouseX * scale
        val physY = mouseY * scale
        windowX = (physX - dragOffsetX).toFloat()
        windowY = (physY - dragOffsetY).toFloat()

    }

    fun onClicked(mouseX: Int,mouseY: Int){
        val scale = MinecraftClient.getInstance().window.scaleFactor
        val physX = mouseX * scale
        val physY = mouseY * scale
        val physWidth = width
        if (physX >= windowX && physX <= windowX + physWidth &&
            physY >= windowY && physY <= windowY + height) {
            isDragging = true
            dragOffsetX = (physX - windowX).toFloat()
            dragOffsetY = (physY - windowY).toFloat()

        }

    }

    fun onMouseReleased() {
        isDragging = false
    }
}