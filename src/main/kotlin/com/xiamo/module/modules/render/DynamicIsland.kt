package com.xiamo.module.modules.render

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.xiamo.module.ComposeModule
import com.xiamo.module.Module
import com.xiamo.notification.NotificationManager
import net.minecraft.client.MinecraftClient

object DynamicIsland : ComposeModule("DynamicIsland","灵动岛") {

    var defaultTitle = mutableStateOf("SuperSoft 🙂 FPS：{fps}")
    var color = mutableStateOf(Color.Black.copy(0.85f))


    var permanentList = mutableStateMapOf<Module,@Composable () -> Unit>()

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
        val bgColor by animateColorAsState(targetValue = color.value)
        LaunchedEffect(Unit) {
            while (true) {
                fps=MinecraftClient.getInstance().currentFps
                kotlinx.coroutines.delay(16)
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(modifier = Modifier
                .zIndex(10f)
                .padding(top=16.dp)
                .background(bgColor, shape = RoundedCornerShape(10.dp))
                .animateContentSize()
                .padding(horizontal = 10.dp)
                .padding(vertical = 5.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally

            ) {

                if (NotificationManager.notifies.count() == 0) {
                    item {
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text(defaultTitle.value.replace("{fps}",fps.toString()), color = Color.White, fontSize = 6.sp)
                            permanentList.onEachIndexed { index, pair ->
                                if (index != permanentList.count()-1) {
                                    Text(" ・ ", color = Color.White, fontSize = 6.sp)
                                    pair.value.invoke()
                                    Text(" ・ ", color = Color.White, fontSize = 6.sp)
                                } else {
                                    Text(" ・ ", color = Color.White, fontSize = 6.sp)
                                    pair.value.invoke()
                                }
                            }
                        }
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

    fun registerPermanent(module: Module,composeContext:@Composable () -> Unit) {
        permanentList[module] = composeContext
    }
    fun unregisterPermanent(module: Module) {
        permanentList.remove(module)
    }


}