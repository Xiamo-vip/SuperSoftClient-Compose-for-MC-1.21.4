package com.xiamo.module.modules.render

import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.ComposeModule
import com.xiamo.notification.NotificationManager
import com.xiamo.notification.Notify
import kotlinx.coroutines.delay
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object DynamicIsland : ComposeModule("DynamicIsland","灵动岛") {
    init {
        this.enabled = true
    }


    override fun onTick() {
        NotificationManager.notifies.removeAll { it.isExpired }
        super.onTick()
    }

    @Composable
    override fun renderCompose() {
        var fps by remember { mutableStateOf(MinecraftClient.getInstance().currentFps) }
        LaunchedEffect(Unit) {
            while (true) {
                fps=MinecraftClient.getInstance().currentFps
                kotlinx.coroutines.delay(16)
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(modifier = Modifier
                .padding(top=16.dp)
                .shadow(elevation = 10.dp)
                .background(Color.Black, shape = RoundedCornerShape(10.dp))
                .animateContentSize()
                .padding(horizontal = 10.dp)
                .padding(vertical = 5.dp)

            ) {
                if (NotificationManager.notifies.count() == 0) {
                    item {
                        Text("DynamicIsland Beta 🙂 FPS：$fps", color = Color.White, fontSize = 8.sp)
                    }
                }else {
                    NotificationManager.notifies.forEach { notify ->
                        item { notify.composeContent()}
                    }
                }
            }
        }

        super.renderCompose()
    }
}