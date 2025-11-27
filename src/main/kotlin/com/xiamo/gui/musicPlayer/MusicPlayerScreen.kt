package com.xiamo.gui.musicPlayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.SuperSoft
import com.xiamo.gui.ComposeScreen
import com.xiamo.module.ModuleManager
import com.xiamo.module.modules.render.ClickGui.instance
import com.xiamo.utils.misc.MediaPlayer
import kotlinx.coroutines.delay
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Constructor

class MusicPlayerScreen(var parentScreen : Screen? = null) : ComposeScreen(Text.of("MusicPlayer")) {
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Preview
    @Composable
    override fun renderCompose() {
        val density = LocalDensity.current

        val backgroundColor = Color(34,11,28)

        val width = with(density) {400.dp }

        val height = with(density) {200.dp }

        val buttonWidth = with(density) {90.dp }

        val buttonHeight = with(density) {20.dp }

        val leftButtonModifier = Modifier.width(width = buttonWidth).height(buttonHeight)
        val leftButtonShape = RoundedCornerShape(20.dp)
        val leftButtonColors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent, contentColor = Color.White)

        LaunchedEffect(Unit) {
            isVisible = true
        }
        LaunchedEffect(isVisible){
            if (!isVisible){
                delay(300)
                MinecraftClient.getInstance().execute {
                    MinecraftClient.getInstance().setScreen(null)
                    MinecraftClient.getInstance().overlay = null
                }





            }
        }

        AnimatedVisibility(
            isVisible,
            enter = expandIn(),
            exit = shrinkOut(tween(durationMillis = 300))
        ) {
            MaterialTheme {
                Box(modifier = Modifier.Companion.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {

                    Column(modifier = Modifier
                        .width(width)
                        .height(height)
                        .background(backgroundColor,RoundedCornerShape(5))

                    ) {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(height-25.dp)
                            ,
                             horizontalArrangement = Arrangement.Center
                        ) {
                            Column(modifier = Modifier
                                .width(width/4)
                                .fillMaxHeight()
                                .dropShadow(RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp),Shadow(1.dp))
                                , horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Music Player", fontSize = 10.sp, color = Color.White)

                                Spacer(modifier = Modifier.size(30.dp))

                                Button(modifier = leftButtonModifier, onClick = {
                                }, shape = leftButtonShape, colors = leftButtonColors, contentPadding = PaddingValues(0.dp)){
                                    Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
                                        val bitmap =  SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/search.png").readAllBytes().decodeToImageBitmap()
                                        Icon(bitmap = bitmap,contentDescription = "Search", tint = Color.White, modifier = Modifier.size(12.dp).padding(end = 5.dp))
                                        Text("搜索",fontSize = 5.sp,color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.size(3.dp))

                                Button(modifier = leftButtonModifier,onClick = {

                                },shape = leftButtonShape,colors = leftButtonColors, contentPadding = PaddingValues(0.dp)){
                                    Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
                                        val bitmap = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/list.png").readAllBytes().decodeToImageBitmap()
                                        Icon(bitmap = bitmap,contentDescription = "List", tint = Color.White, modifier = Modifier.size(12.dp).padding(end = 5.dp))
                                        Text("列表",fontSize = 5.sp,color = Color.White)
                                    }
                                }

                            }
                            Column(modifier=Modifier.fillMaxSize()){

                            }

                        }



                        Row(modifier = Modifier
                            .fillMaxSize()
                            .background(Color(224,224,224,180)
                                ,RoundedCornerShape(10.dp))
                            , verticalAlignment = Alignment.CenterVertically)
                        {


                        }



                    }


                }
            }
        }



        super.renderCompose()
    }
}