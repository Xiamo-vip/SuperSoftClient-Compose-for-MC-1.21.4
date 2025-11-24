package com.xiamo.gui.clickGui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llamalad7.mixinextras.lib.antlr.runtime.atn.Transition
import com.xiamo.gui.ComposeScreen
import com.xiamo.module.Category
import com.xiamo.module.ModuleManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import java.util.concurrent.CopyOnWriteArrayList

class ClickGuiScreen : ComposeScreen(Text.of("ClickGui")) {
    val categories = CopyOnWriteArrayList<ClickGuiWindow>()
    var isVisible by mutableStateOf(false)

    var width = 400



    @Composable
    override fun renderCompose() {
        if (categories.count() ==0){
            var x = 120
            val y = 200
            Category.entries.forEach { category ->
                categories.add(ClickGuiWindow(x,y,category,width))
                x+=width+50
            }

        }
       LaunchedEffect(Unit )
       {
           isVisible = true
       }




        AnimatedVisibility(isVisible,
            enter = fadeIn() + scaleIn(tween(easing = FastOutSlowInEasing)),
            exit = fadeOut() + scaleOut(tween (easing = FastOutSlowInEasing, durationMillis = 300)),
        ){
            Box(modifier = Modifier.fillMaxSize().animateContentSize()){
                categories.forEach { it.renderCompose() }
            }

        }


    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        categories.forEach { it.onDragged(mouseX.toInt(),mouseY.toInt()) }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        categories.forEach { it.onClicked(mouseX.toInt(),mouseY.toInt()) }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
       categories.forEach { it.onMouseReleased() }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun close() {

        isVisible = false
        ModuleManager.modules.find { it.name == "ClickGui" }?.toggle()
        super.close()




    }



}


