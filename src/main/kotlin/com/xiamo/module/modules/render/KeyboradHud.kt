package com.xiamo.module.modules.render


import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.ComposeModule
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient

object KeyboradHud : ComposeModule("KeyboradHud","键盘可视化") {
    val forwardKeyIsPressed = mutableStateOf(false)
    val leftKeyIsPressed = mutableStateOf(false)
    val rightKeyIsPressed = mutableStateOf(false)
    val backKeyIsPressed = mutableStateOf(false)


    @Composable
    override fun renderCompose() {
        val density = LocalDensity.current
        val forwardInteractionSource = remember { MutableInteractionSource() }
        val backInteractionSource = remember { MutableInteractionSource() }
        val leftInteractionSource = remember { MutableInteractionSource() }
        val rightInteractionSource = remember { MutableInteractionSource() }
        val scope = rememberCoroutineScope()
        val press =PressInteraction.Press(Offset(10f,10f))
        val forwardPress = remember { mutableStateOf<PressInteraction.Press?>(null) }
        val backPress = remember { mutableStateOf<PressInteraction.Press?>(null) }
        val leftPress = remember { mutableStateOf<PressInteraction.Press?>(null) }
        val rightPress = remember { mutableStateOf<PressInteraction.Press?>(null) }
        val dirctionKeyWidth = remember { mutableStateOf(with(density) {5.dp.toPx()}  ) }
        val dirctionKeyHeight = remember { mutableStateOf(with(density) {5.dp.toPx()}  ) }
        val offsetX = remember { mutableStateOf(with(density){0.dp.toPx()}) }
        val offsetY = remember { mutableStateOf(with(density){20.dp.toPx()}) }
        val buttonColor = ButtonDefaults.buttonColors(contentColor = Color.White, containerColor = Color.Black.copy(0.7f))
        val buttonShape = RoundedCornerShape(10)
        val buttonModifier = Modifier.padding(3.dp).width(dirctionKeyWidth.value.dp).height(dirctionKeyHeight.value.dp)
        val textStyle = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal)


        LaunchedEffect(forwardKeyIsPressed.value) {
            if (forwardKeyIsPressed.value) {
                scope.launch {
                    val press = PressInteraction.Press(Offset(12f,12f))
                    forwardPress.value = press
                    forwardInteractionSource.emit(press)
                }
            }else { if (forwardKeyIsPressed.value == false) {
                scope.launch {
                    forwardPress.value?.let { press ->
                        forwardInteractionSource.emit(PressInteraction.Release(press))
                    }
                    forwardPress.value = null
                }
            } }

        }
        LaunchedEffect(backKeyIsPressed.value) {
            if (backKeyIsPressed.value) {
                scope.launch {
                    val press = PressInteraction.Press(Offset(12f,12f))
                    backPress.value = press
                    backInteractionSource.emit(press)
                }
            }else { if (backKeyIsPressed.value == false) {
                scope.launch {
                    backPress.value?.let { press ->
                        backInteractionSource.emit(PressInteraction.Release(press))
                    }
                    backPress.value = null
                }
            } }

        }
        LaunchedEffect(leftKeyIsPressed.value) {
            if (leftKeyIsPressed.value) {
                scope.launch {
                    val press = PressInteraction.Press(Offset(12f,12f))
                    leftPress.value = press
                    leftInteractionSource.emit(press)
                }
            }else { if (leftKeyIsPressed.value == false) {
                scope.launch {
                    leftPress.value?.let { press ->
                        leftInteractionSource.emit(PressInteraction.Release(press))
                    }
                    leftPress.value = null
                }
            } }

        }
        LaunchedEffect(rightKeyIsPressed.value) {
            if (rightKeyIsPressed.value) {
                scope.launch {
                    val press = PressInteraction.Press(Offset(12f,12f))
                    rightPress.value = press
                    rightInteractionSource.emit(press)
                }
            }else { if (rightKeyIsPressed.value == false) {
                scope.launch {
                    rightPress.value?.let { press ->
                        rightInteractionSource.emit(PressInteraction.Release(press))
                    }
                    rightPress.value = null
                }
            } }

        }


        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            Column(modifier = Modifier.offset(offsetX.value.dp,offsetY.value.dp).width(100.dp).align(Alignment.TopStart)) {
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Center){
                   Button(interactionSource = forwardInteractionSource,onClick = {
                   }, modifier = buttonModifier
                    , colors = buttonColor,shape = buttonShape, contentPadding = PaddingValues(0.dp)
                   ){
                       Text(MinecraftClient.getInstance().options.forwardKey.boundKeyLocalizedText.string, style = textStyle)
                    }

                }
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Center){
                    Button(interactionSource = leftInteractionSource,onClick = {}, modifier =buttonModifier
                        , colors = buttonColor, shape = buttonShape, contentPadding = PaddingValues(0.dp)){
                        Text(MinecraftClient.getInstance().options.leftKey.boundKeyLocalizedText.string, style = textStyle)

                    }
                    Button(interactionSource = backInteractionSource,onClick = {}, modifier = buttonModifier
                        , colors = buttonColor, shape = buttonShape, contentPadding = PaddingValues(0.dp)){
                        Text(MinecraftClient.getInstance().options.backKey.boundKeyLocalizedText.string, style = textStyle)

                    }
                    Button(interactionSource = rightInteractionSource,onClick = {}, modifier = buttonModifier
                        , colors = buttonColor, shape = buttonShape, contentPadding = PaddingValues(0.dp)){
                        Text(MinecraftClient.getInstance().options.rightKey.boundKeyLocalizedText.string, style = textStyle)

                    }

                }

            }
        }



        super.renderCompose()
    }


    override fun onKey(keyCode: Int, keyState: Int) {
        if (keyCode == MinecraftClient.getInstance().options.forwardKey.defaultKey.code) {
            if (keyState == 1 || keyState == 2) {
                forwardKeyIsPressed.value = true
            }else if (keyState == 0) {forwardKeyIsPressed.value = false}
        }




        if (keyCode == MinecraftClient.getInstance().options.backKey.defaultKey.code) {
            if (keyState == 1 || keyState == 2) {
                backKeyIsPressed.value = true
            }else if (keyState == 0) {backKeyIsPressed.value = false}
        }

        if (keyCode == MinecraftClient.getInstance().options.leftKey.defaultKey.code) {
            if (keyState == 1 || keyState == 2) {
                leftKeyIsPressed.value = true
            }else if (keyState == 0) {leftKeyIsPressed.value = false}
        }

        if (keyCode == MinecraftClient.getInstance().options.rightKey.defaultKey.code) {
            if (keyState == 1 || keyState == 2) {
                rightKeyIsPressed.value = true
            }else if (keyState == 0) {rightKeyIsPressed.value = false}
        }




    }





}