package com.xiamo.module.modules.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.gui.hud.HudComponent
import com.xiamo.module.ComposeModule
import kotlinx.coroutines.delay
import net.minecraft.client.MinecraftClient
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.ScoreboardEntry
import net.minecraft.text.Style
import net.minecraft.text.Text as MCText

object ScoreBoard : ComposeModule("ScoreBoard", "显示计分板侧边栏") {

    var boardWidth = numberSetting("ScoreBoard Width","计分板宽度",80.0,10.0,200.0)
    var textSize = numberSetting("ScoreBoard TextSize","计分板文字大小",6.0,1.0,20.0)
    var alpha = numberSetting("ScoreBoard Alpha","计分板透明度",0.6,0.0,1.0)
    var radius = numberSetting("ScoreBoard Rounded","计分板圆角",4.0,1.0,20.0)
    var displayScore = booleanSetting("ScoreBoard Score","计分板分数显示",false)

    init {
        this.enabled = true
    }

    @Composable
    override fun renderCompose() {
        val client = MinecraftClient.getInstance()
        if (client.world == null) return

        HudComponent("scoreboard","ScoreBoard"){
            ScoreboardSidebar()
        }


    }

    @Composable
    fun ScoreboardSidebar() {
        val client = MinecraftClient.getInstance()
        var scoreData by remember { mutableStateOf<Pair<AnnotatedString, List<ScoreLine>>?>(null) }
        LaunchedEffect(Unit) {
            while (true) {
                val world = client.world
                val scoreboard = world?.scoreboard
                val objective = scoreboard?.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR)
                if (objective != null) {
                    val entries = scoreboard.getScoreboardEntries(objective)
                        .sortedWith(compareByDescending<ScoreboardEntry> { it.value }.thenBy { it.owner })
                        .take(15)
                        .map { entry ->
                            val team = scoreboard.getScoreHolderTeam(entry.owner)
                            val nameText = team?.decorateName(MCText.literal(entry.owner)) ?: MCText.literal(entry.owner)
                            ScoreLine(nameText.toAnnotatedString(), entry.value.toString())
                        }

                    scoreData = Pair(objective.displayName.toAnnotatedString(), entries)
                } else {
                    scoreData = null
                }
                delay(50)
            }
        }

        val data = scoreData ?: return

        Column(
            modifier = Modifier
                .width(boardWidth.value.dp)
                .background(Color.Black.copy(alpha = alpha.value.toFloat()), RoundedCornerShape(radius.value.dp))
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = data.first,
                fontSize = (textSize.value.toFloat() + 1).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            )

            data.second.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = line.name,
                        fontSize = textSize.value.toFloat().sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                   if (displayScore.value){
                       Text(
                           text = line.value,
                           color = Color(0xFFFC5454),
                           fontSize = textSize.value.toFloat().sp,
                           fontWeight = FontWeight.Bold,
                           modifier = Modifier.padding(start = 4.dp)
                       )
                   }
                }
            }
        }
    }


    data class ScoreLine(val name: AnnotatedString, val value: String)

    private fun MCText.toAnnotatedString(): AnnotatedString {
        val builder = AnnotatedString.Builder()
        this.visit({ style, part ->
            val parts = part.split("§")
            if (parts.size <= 1) {
                builder.withStyle(style.toSpanStyle()) { append(part) }
            } else {
                var currentStyle = style.toSpanStyle()
                parts.forEachIndexed { i, s ->
                    if (s.isEmpty()) return@forEachIndexed
                    if (i == 0) {
                        builder.withStyle(currentStyle) { append(s) }
                    } else {
                        currentStyle = applyColor(currentStyle, s[0])
                        if (s.length > 1) builder.withStyle(currentStyle) { append(s.substring(1)) }
                    }
                }
            }
            java.util.Optional.empty<Any>()
        }, Style.EMPTY)
        return builder.toAnnotatedString()
    }

    private fun applyColor(style: SpanStyle, code: Char): SpanStyle {
        return when (code.lowercaseChar()) {
            '0' -> style.copy(color = Color(0xFF000000))
            '1' -> style.copy(color = Color(0xFF0000AA))
            '2' -> style.copy(color = Color(0xFF00AA00))
            '3' -> style.copy(color = Color(0xFF00AAAA))
            '4' -> style.copy(color = Color(0xFFAA0000))
            '5' -> style.copy(color = Color(0xFFAA00AA))
            '6' -> style.copy(color = Color(0xFFFFAA00))
            '7' -> style.copy(color = Color(0xFFAAAAAA))
            '8' -> style.copy(color = Color(0xFF555555))
            '9' -> style.copy(color = Color(0xFF5555FF))
            'a' -> style.copy(color = Color(0xFF55FF55))
            'b' -> style.copy(color = Color(0xFF55FFFF))
            'c' -> style.copy(color = Color(0xFFFF5555))
            'd' -> style.copy(color = Color(0xFFFF55FF))
            'e' -> style.copy(color = Color(0xFFFFFF55))
            'f' -> style.copy(color = Color(0xFFFFFFFF))
            'l' -> style.copy(fontWeight = FontWeight.Bold)
            'r' -> SpanStyle(color = Color.White)
            else -> style
        }
    }

    private fun Style.toSpanStyle(): SpanStyle {
        val c = this.color?.rgb
        return SpanStyle(
            color = if (c != null) Color(c or -0x1000000) else Color.White,
            fontWeight = if (this.isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}