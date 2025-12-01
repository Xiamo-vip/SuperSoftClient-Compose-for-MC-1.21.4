package com.xiamo.gui.clickGui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llamalad7.mixinextras.lib.antlr.runtime.atn.Transition
import com.xiamo.SuperSoft
import com.xiamo.gui.ComposeScreen
import com.xiamo.module.Category
import com.xiamo.module.ModuleManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.util.concurrent.CopyOnWriteArrayList

class ClickGuiScreen(val parentScreen : Screen? = null) : ComposeScreen(Text.of("ClickGui")) {
    val categories = CopyOnWriteArrayList<ClickGuiWindow>()


    override fun removed() {
        ModuleManager.modules.find { it.name == "ClickGui" }?.enabled = false
        super.removed()
    }
    @Composable
    override fun renderCompose() {
        val density = LocalDensity.current
        val width = with(density){15.dp.toPx()}
        val height = with(density){4.dp.toPx()}
        val spacer = with(density){5.dp.toPx()}
        val start = with(density){10.dp.toPx()}

        LaunchedEffect(Unit )
        {
            isVisible = true
        }
        val scale by animateSizeAsState(if (isVisible)Size(1f,1f,) else Size(0f,0f,) ,tween (durationMillis = 300),
            finishedListener = {
               if (!isVisible){
                   MinecraftClient.getInstance().setScreen(parentScreen)
               }
            }
            )
        if (categories.count() ==0){
            var x = start
            val y = with(density){40.dp.toPx()}

            Category.entries.forEach { category ->
                categories.add(ClickGuiWindow(x.toInt(),y.toInt(),category,width,height))
                x += with(density){(width + spacer).dp.toPx()}
            }

        }



        Box(modifier = Modifier.fillMaxSize()
            .alpha(animateFloatAsState(if (isVisible) 1f else 0f).value)
            .dropShadow(
                RoundedCornerShape(32.dp),
                Shadow(8.dp, Color(0,0,0,50))
            )
            .safeContentPadding()
        ){
            Box(modifier = Modifier.fillMaxSize().animateContentSize().scale(scale.width,scale.height)){
                categories.forEach { it.renderCompose() }
            }
        }



    }



    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        categories.forEach { it.onDragged(mouseX.toInt(),mouseY.toInt()) }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (categories.any{it.isHover}){
            categories.last { it.isHover }.onClicked(mouseX.toInt(),mouseY.toInt())
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
       categories.forEach { it.onMouseReleased() }
        return super.mouseReleased(mouseX, mouseY, button)
    }


    override fun close() {
        ModuleManager.modules.find { it.name == "ClickGui" }?.enabled = false
        super.close()
    }

}


