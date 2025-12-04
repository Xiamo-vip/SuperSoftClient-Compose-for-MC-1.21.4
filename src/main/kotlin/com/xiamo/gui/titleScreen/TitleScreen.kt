package com.xiamo.gui.titleScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.gui.ComposeScreen
import com.xiamo.module.modules.render.Hud
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import net.minecraft.client.gui.screen.option.OptionsScreen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.text.Text
class TitleScreen : ComposeScreen(Text.of("Title Screen")) {

    @Composable
    override fun renderCompose() {
        var isVisiable by remember { mutableStateOf(false) }
        Box(Modifier.fillMaxSize().background(Color(23,8,20))) {
            LaunchedEffect(Unit) {
                isVisiable = true
            }
            AnimatedVisibility(isVisiable,
                enter = fadeIn(tween(500))+ scaleIn(tween(500, easing = FastOutSlowInEasing)) + expandVertically(tween(500)),exit = fadeOut()+ scaleOut()) {
                Column(modifier = Modifier.padding(top = 50.dp).align(Alignment.TopCenter)) {
                    Text(
                        "SuperSoft Client", fontSize = 30.sp, color = Color.White, style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(
                            Color.Yellow, offset = Offset(1f,1f), blurRadius = 5f
                        )), modifier = Modifier.align(Alignment.CenterHorizontally))

                    Column(modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),horizontalAlignment = Alignment.CenterHorizontally) {
                        MenuButton("SinglePlay") { MinecraftClient.getInstance().setScreen(SelectWorldScreen(this@TitleScreen)) }
                        MenuButton("Multiplayer") { MinecraftClient.getInstance().setScreen(MultiplayerScreen(this@TitleScreen)) }
                        MenuButton("Option") { MinecraftClient.getInstance().setScreen(OptionsScreen(this@TitleScreen, MinecraftClient.getInstance().options)) }
                        MenuButton("Quit Game") { MinecraftClient.getInstance().scheduleStop() }
                    }
                }
            }

        }

    }

}




@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val strokeWidth by animateFloatAsState(if (isHovered) 4f else 8f)
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .hoverable(interactionSource)
            .drawBehind {
                drawLine(
                    color = Color.Blue,
                    start = Offset(0f, size.height - 5.dp.toPx()),
                    end = Offset(size.width, size.height - 5.dp.toPx()),
                    strokeWidth = strokeWidth
                )
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        interactionSource = interactionSource
    ) {
        Text(text)
    }
}
