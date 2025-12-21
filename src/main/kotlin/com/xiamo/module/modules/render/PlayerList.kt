package com.xiamo.module.modules.render

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.Category
import com.xiamo.module.Module
import com.xiamo.notification.NotificationManager
import com.xiamo.notification.Notify
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.util.Nullables
import net.minecraft.world.GameMode
import org.lwjgl.glfw.GLFW
import java.util.function.Function
import java.util.function.ToIntFunction

object PlayerList : Module("PlayerList","PlayerList", Category.Render) {

    init {
        this.enabled = true
    }


    override fun onKey(keyCode: Int, keyState: Int) {
        if (keyCode == MinecraftClient.getInstance().options.playerListKey.defaultKey.code && keyState == GLFW.GLFW_PRESS)
        {
            NotificationManager.add(
                Notify(
                    "PlayerList",
                    "PlayerListShow",
                    -1,
                    { plauerList() }
                )
            )
        }else if(keyCode == MinecraftClient.getInstance().options.playerListKey.defaultKey.code && keyState == GLFW.GLFW_RELEASE) {
            NotificationManager.notifies.removeIf {
                it.titile == "PlayerList"
            }
        }


        super.onKey(keyCode, keyState)
    }


    @Composable
    fun plauerList(){
        if (MinecraftClient.getInstance().world == null) return
        var isVisible by remember { mutableStateOf(false) }
        val frameNanos by produceState(0L){
            while (true){
                withFrameNanos {
                    value = it
                }
            }
        }

        LaunchedEffect(Unit){
            isVisible = true
        }

        FlowColumn(maxItemsInEachColumn = 9) {

            val players = MinecraftClient.getInstance()
                .player!!
                .networkHandler
                .listedPlayerListEntries
                .sortedBy { if (it.gameMode == GameMode.SPECTATOR) 0 else 1 }
            for (player in players) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn()
                ) {
                    Row(Modifier.padding(3.dp)) {
                        Text(
                            player.profile.name,
                            color = Color.White,
                            fontSize = 5.sp
                        )
                    }
                }
            }
        }











    }




}