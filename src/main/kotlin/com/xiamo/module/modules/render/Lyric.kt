package com.xiamo.module.modules.render

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.ComposeModule
import com.xiamo.utils.misc.LyricLine
import com.xiamo.utils.misc.LyricLineProcessor.findCurrentIndex
import com.xiamo.utils.misc.MediaPlayer
import kotlinx.coroutines.launch
import net.minecraft.text.Text
import kotlin.math.abs

object Lyric : ComposeModule("Lyric","歌词显示") {
    var currentIndex = mutableStateOf(0)
    var isVisible = mutableStateOf(false)


    init {
        this.enabled = true
    }

    override fun onTick() {
        currentIndex.value = findCurrentIndex(MediaPlayer.lyric, MediaPlayer.tick.value.toLong())
        super.onTick()
    }

    @Composable
    override fun renderCompose() {
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        LaunchedEffect(MediaPlayer.isPlaying.value) {
            isVisible.value = MediaPlayer.isPlaying.value
        }



        AnimatedVisibility(isVisible.value,enter = fadeIn(),exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .height(120.dp)
                        .width(100.dp)
                        .wrapContentSize(Alignment.Center)
                    ,
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(MediaPlayer.lyric) { index, line ->
                        LyricText(
                            line.text,
                            color = Color.Gray,
                            focusColors = Color.White,
                            size = 5f,
                            focusSize = 7f,
                            isFocused = index == currentIndex.value
                        )
                    }
                }
            }
        }

        LaunchedEffect(currentIndex.value) {
            val centerOffset = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.div(2) ?: 0
            scope.launch {
                listState.animateScrollToItem(currentIndex.value, -centerOffset-150)
            }
        }
    }



}



@Composable
fun LyricText(line:String,color:Color,focusColors: Color,size : Float,focusSize:Float,isFocused:Boolean) {
    val fontColor  = animateColorAsState(targetValue = if (isFocused) focusColors else color, tween(durationMillis = 300))
    val fontSize = animateFloatAsState(targetValue = if (isFocused) focusSize else size,tween(durationMillis = 300))
    Text(line,color = fontColor.value,fontSize = fontSize.value.sp,modifier = Modifier.padding(7.dp),textAlign = TextAlign.Center)

}