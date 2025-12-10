package com.xiamo.module.modules.render

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.ComposeModule
import com.xiamo.utils.misc.LyricLineProcessor.findCurrentIndex
import com.xiamo.utils.misc.MediaPlayer
import kotlinx.coroutines.launch
import kotlin.math.abs

object Lyric : ComposeModule("Lyric", "歌词显示") {
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
        val containerHeight by remember { mutableStateOf(40) }
        val containerWidth by remember { mutableStateOf(200) }

        LaunchedEffect(MediaPlayer.isPlaying.value) {
            isVisible.value = MediaPlayer.isPlaying.value
        }

        AnimatedVisibility(
            visible = isVisible.value,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .height(containerHeight.dp)
                        .width(containerWidth.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        contentPadding = PaddingValues(vertical = (containerHeight / 2 - 10).dp)
                    ) {
                        itemsIndexed(MediaPlayer.lyric) { index, line ->
                            val distance = abs(index - currentIndex.value)
                            AppleMusicLyricLine(
                                text = line.text,
                                distance = distance,
                                isCurrent = index == currentIndex.value
                            )
                        }
                    }
                }
            }
        }

        LaunchedEffect(currentIndex.value) {
            scope.launch {
                if (MediaPlayer.lyric.isNotEmpty() && currentIndex.value < MediaPlayer.lyric.size) {
                    listState.animateScrollToItem(
                        index = currentIndex.value,
                        scrollOffset = 0
                    )
                }
            }
        }
    }
}

@Composable
fun AppleMusicLyricLine(
    text: String,
    distance: Int,
    isCurrent: Boolean
) {
    val targetAlpha = when {
        isCurrent -> 1f
        distance == 1 -> 0.5f
        distance == 2 -> 0.25f
        else -> 0.1f
    }

    val targetScale = when {
        isCurrent -> 1.05f
        distance == 1 -> 0.9f
        else -> 0.85f
    }

    val targetFontSize = when {
        isCurrent -> 7.5f
        distance == 1 -> 6f
        else -> 5f
    }

    val targetBlur = when {
        isCurrent -> 0f
        distance == 1 -> 0.3f
        else -> 0.8f
    }

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val fontSize by animateFloatAsState(
        targetValue = targetFontSize,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val blurAmount by animateFloatAsState(
        targetValue = targetBlur,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
            .then(
                if (blurAmount > 0.1f) Modifier.blur(blurAmount.dp) else Modifier
            )
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = fontSize.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
