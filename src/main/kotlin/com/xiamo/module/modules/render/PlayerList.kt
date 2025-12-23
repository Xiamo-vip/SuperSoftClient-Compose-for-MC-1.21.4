package com.xiamo.module.modules.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xiamo.module.Category
import com.xiamo.module.Module
import com.xiamo.notification.NotificationManager
import com.xiamo.notification.Notify
import kotlinx.coroutines.delay
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.GameMode
import org.lwjgl.glfw.GLFW

object PlayerList : Module("PlayerList", "Show online players", Category.Render) {
    val fontSize = numberSetting("FontSize", "文字大小",4.0,1.0,10.0)

    init {
        this.enabled = true
    }

    override fun onKey(keyCode: Int, keyState: Int,scanCode : Int) {
        val options = MinecraftClient.getInstance().options
        if (options == null) return
        if (keyCode == options.playerListKey.defaultKey.code) {
            if (keyState == GLFW.GLFW_PRESS) {
                DynamicIsland.color.value = DynamicIsland.color.value.copy(alpha = 0.6f)
                NotificationManager.add(
                    Notify(
                        "PlayerList",
                        "PlayerListShow",
                        -1,
                        { PlayerListContent() }
                    )
                )
            } else if (keyState == GLFW.GLFW_RELEASE) {
                DynamicIsland.color.value = DynamicIsland.color.value.copy(alpha = 1f)
                NotificationManager.notifies.removeIf {
                    it.titile == "PlayerList"
                }
            }
        }
        super.onKey(keyCode, keyState,scanCode)
    }

    @Composable
    fun PlayerListContent() {
        val client = MinecraftClient.getInstance()
        val handler = client.networkHandler ?: return
        val entries by remember(handler) {
            derivedStateOf {
                handler.listedPlayerListEntries
                    .sortedWith(
                        compareBy<PlayerListEntry> { if (it.gameMode == GameMode.SPECTATOR) 1 else 0 }
                            .thenBy { it.scoreboardTeam?.name ?: "" }
                            .thenBy { it.profile.name }
                    )
            }
        }

        FlowColumn(maxItemsInEachColumn = 20) {
            for (entry in entries) {
                key(entry.profile.id) {
                    PlayerEntryItem(entry)
                }
            }
        }
    }

    @Composable
    fun PlayerEntryItem(entry: PlayerListEntry) {


        val tick by produceState(0) {
            while(true) {
                delay(1000)
                value++
            }
        }

        val displayNameText = remember(entry, tick) {
            getPlayerName(entry)
        }

        Row(horizontalArrangement = Arrangement.Center,verticalAlignment = Alignment.CenterVertically) {

            AsyncImage( //这个不知道好不好用
                model = "https://minotar.net/avatar/${entry.profile.name}/28", contentDescription = null,
            )
            Text(
                text = displayNameText.toAnnotatedString(),
                fontSize = fontSize.value.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            Canvas(modifier = Modifier.size(width = 10.dp, height = 8.dp)){
                drawRect(Color.Green,size = Size(1.dp.toPx(),1.dp.toPx()),topLeft = Offset(1.dp.toPx(), 6.dp.toPx()))
                if (entry.latency > 1000) return@Canvas
                drawRect(Color.Green,size = Size(1.dp.toPx(),2.dp.toPx()),topLeft = Offset(2.5.dp.toPx(), 5.dp.toPx()))
                if (entry.latency > 600) return@Canvas
                drawRect(Color.Green,size = Size(1.dp.toPx(),3.dp.toPx()),topLeft = Offset(4.dp.toPx(), 4.dp.toPx()))
                if (entry.latency > 300) return@Canvas
                drawRect(Color.Green,size = Size(1.dp.toPx(),4.dp.toPx()),topLeft = Offset(5.5.dp.toPx(), 3.dp.toPx()))
                if (entry.latency > 150) return@Canvas
                drawRect(Color.Green,size = Size(1.dp.toPx(),5.dp.toPx()),topLeft = Offset(7.dp.toPx(), 2.dp.toPx()))
            }
        }

    }


    private fun getPlayerName(entry: PlayerListEntry): Text {
        return if (entry.displayName != null) {
            applyGameModeFormatting(entry, entry.displayName!!.copy())
        } else {
            val baseName = Text.literal(entry.profile.name)
            val decorated = entry.scoreboardTeam?.decorateName(baseName) ?: baseName
            applyGameModeFormatting(entry, decorated)
        }
    }


    private fun applyGameModeFormatting(entry: PlayerListEntry, name: Text): Text {
        return if (entry.gameMode == GameMode.SPECTATOR) {
            val mutableName = if (name is MutableText) name else name.copy()
            mutableName.formatted(Formatting.ITALIC)
        } else {
            name
        }
    }


    private fun Style.toSpanStyle(): SpanStyle {
        val mcColor = this.color?.rgb
        val composeColor = if (mcColor != null) {
            Color(mcColor or 0xFF000000.toInt())
        } else {
            Color.White
        }

        return SpanStyle(
            color = composeColor,
            fontWeight = if (this.isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (this.isItalic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = when {
                this.isUnderlined && this.isStrikethrough ->
                    TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                this.isUnderlined -> TextDecoration.Underline
                this.isStrikethrough -> TextDecoration.LineThrough
                else -> null
            }
        )
    }


    private fun Text.toAnnotatedString(): AnnotatedString {
        return buildAnnotatedString {
            this@toAnnotatedString.visit({ style, part ->
                withStyle(style.toSpanStyle()) {
                    append(part)
                }
                java.util.Optional.empty<Any>()
            }, Style.EMPTY)
        }
    }
}