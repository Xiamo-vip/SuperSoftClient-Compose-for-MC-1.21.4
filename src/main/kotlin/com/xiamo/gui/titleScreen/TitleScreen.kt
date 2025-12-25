package com.xiamo.gui.titleScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.SuperSoft
import com.xiamo.alt.AltManagerScreen
import com.xiamo.gui.ComposeScreen
import kotlinx.coroutines.delay
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import net.minecraft.client.gui.screen.option.OptionsScreen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.text.Text
import kotlin.math.sqrt
import kotlin.random.Random

class TitleScreen : ComposeScreen(Text.of("Title Screen")) {
    var particles = List(80) {
        Particle()
    }
    @Composable
    override fun renderCompose() {
        var isVisiable by remember { mutableStateOf(false) }
        var lastWidth by remember { mutableStateOf(0) }
        var lastHeight by remember { mutableStateOf(0) }

//        val particles = remember { mutableStateListOf<Particle>().apply { repeat(100){ add(Particle(
//            x1 = Random.nextFloat() * MinecraftClient.getInstance().window.width,
//            y1 = Random.nextFloat() * MinecraftClient.getInstance().window.height,
//            velocity = 0.2f
//        )) } } }


        Box(Modifier.fillMaxSize().background(Color(23,8,20))) {
            LaunchedEffect(Unit) {
                isVisiable = true
            }
            LaunchedEffect(Unit) {
                lastWidth = MinecraftClient.getInstance().window.width
                lastHeight = MinecraftClient.getInstance().window.height
                var t = 0
                while (true) {
                    particles.forEach { it.update() }
                    delay(16)
                    t++
                    if (lastWidth != MinecraftClient.getInstance().window.width || lastHeight != MinecraftClient.getInstance().window.height) {
                        particles = List(80) {
                            Particle()
                        }
                        lastWidth = MinecraftClient.getInstance().window.width
                        lastHeight = MinecraftClient.getInstance().window.height
                    }
                }
            }
            SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/background.jpg")?.let {
                Image(it.readAllBytes().decodeToImageBitmap(),
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = null)
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    drawCircle(Color.White, 6f, Offset(p.x, p.y))
                }
                particles.forEach { pA ->
                    particles.forEach { pB->
                        val dx = (pA.x - pB.x)
                        val dy = (pA.y - pB.y)
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < 150 ){
                            drawLine(Color.White.copy(1-(dist / 150)),Offset(pA.x,pA.y), Offset(pB.x,pB.y))
                        }
                    }
                }
            }
            AnimatedVisibility(isVisiable,
                enter = fadeIn(tween(500))+ scaleIn(tween(500, easing = FastOutSlowInEasing)) + expandVertically(tween(500)),exit = fadeOut()+ scaleOut()) {
                Column(modifier = Modifier.scale(0.5f).align(Alignment.Center)) {
                    Text(
                        "SuperSoft Client", fontSize = 30.sp, color = Color.White, style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(
                            Color.Yellow, offset = Offset(1f,1f), blurRadius = 5f
                        )), modifier = Modifier.align(Alignment.CenterHorizontally))

                    Column(modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),horizontalAlignment = Alignment.CenterHorizontally) {
                        MenuButton("SinglePlay") { MinecraftClient.getInstance().setScreen(SelectWorldScreen(this@TitleScreen)) }
                        MenuButton("Multiplayer") { MinecraftClient.getInstance().setScreen(MultiplayerScreen(this@TitleScreen)) }
                        MenuButton("Alt Manager") { MinecraftClient.getInstance().setScreen(AltManagerScreen()) }
                        MenuButton("Option") { MinecraftClient.getInstance().setScreen(OptionsScreen(this@TitleScreen, MinecraftClient.getInstance().options)) }
                        MenuButton("Quit Game") { MinecraftClient.getInstance().scheduleStop() }
                    }
                }
            }

        }

    }

}



class Particle() {
    var x by mutableStateOf(Random.nextFloat() * MinecraftClient.getInstance().window.width)
    var y by mutableStateOf(Random.nextFloat() * MinecraftClient.getInstance().window.height)
    var speed = 0.5
    var vX = (Random.nextFloat() * 2 - 1) * speed.toFloat()
    var vY = (Random.nextFloat() * 2 - 1) * speed.toFloat()
    fun update() {
        if (x >= MinecraftClient.getInstance().window.width.toFloat() || x <= 0){vX *= -1 }
        if (y >= MinecraftClient.getInstance().window.height.toFloat() || y <=0 ){vY *= -1 }
        x += vX
        y += vY
    }
}

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(5.dp)
            .width(200.dp)
            .height(40.dp)
            .clickable(onClick = onClick),
        color = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = Color.White)
        }
    }
}

