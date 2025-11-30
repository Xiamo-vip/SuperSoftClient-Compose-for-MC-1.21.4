package com.xiamo.gui.clickGui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
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
import kotlin.math.roundToInt

class ClickGuiWindow(val x: Int, val y: Int, val category: Category, val width: Float,val height : Float) {


    var windowX by mutableStateOf(x.toFloat())
    var windowY by mutableStateOf(y.toFloat())
    var isDragging = false
    var isHover = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    val categoryTitleFont = 10.sp
    val categoryContentFont = 7.sp
    val textStyle = TextStyle(fontSize = categoryTitleFont)
    val radius: Dp = 2.dp


    val backgroundColor = Color(26, 26, 26)
    val titleBgColor = Color(0, 0, 0)
    val moduelHoverBgColor = Color(105, 180, 255)
    val moduelEnabledBgColor = Color(108, 53, 222)

    @Composable
    fun renderCompose() {
        val interfaceSource = remember { MutableInteractionSource() }
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        isHover = interfaceSource.collectIsHoveredAsState().value


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
                    .height(height.dp)
                    .shadow(5.dp, RoundedCornerShape(radius))
                    .hoverable(interfaceSource)
                ,
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
                    fontSize = categoryContentFont,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    modifier = Modifier
                        .clickable(onClick = {
                            it.toggle()
                        },interactionSource = remember { MutableInteractionSource() } ,indication = ripple(true,200.dp,Color.DarkGray))
                        .hoverable(interactionSource)
                        .fillMaxWidth()
                        .background(moduleBgColor)
                        .padding(horizontal = 8.dp)
                        .wrapContentSize(Alignment.Center,true)
                        .wrapContentWidth(align = Alignment.CenterHorizontally),

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

        val scaledWidth = width * scale
        val scaledHeight = height * scale

        if (physX >= windowX && physX <= windowX + scaledWidth &&
            physY >= windowY && physY <= windowY + scaledHeight) {

            isDragging = true
            dragOffsetX = (physX - windowX).toFloat()
            dragOffsetY = (physY - windowY).toFloat()

        }

    }

    fun onMouseReleased() {
        isDragging = false
    }
}