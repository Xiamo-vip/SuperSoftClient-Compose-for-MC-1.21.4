package com.xiamo.module.modules.render

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.ComposeModule
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.text.Text

object EffectHud : ComposeModule("EffectHud","效果显示") {
    val tickCounter  = mutableStateOf(0f)

    @Composable
    override fun renderCompose() {
        val player = MinecraftClient.getInstance().player
        if(player == null) return

        Box(modifier = Modifier.fillMaxSize().padding(bottom = 10.dp, end = 2.dp).offset(y = 10.dp), contentAlignment = Alignment.CenterStart) {
            var collection: MutableCollection<StatusEffectInstance?> = player.statusEffects
            if (tickCounter.value != 0f) {
                collection = player.statusEffects
                if (collection.isEmpty()) {
                    return
                }
            }
            LazyColumn(modifier = Modifier.animateContentSize() , reverseLayout = true) {
                for (status in collection ) {
                    item {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                            .padding(2.dp)
                            .width(90.dp)
                            .height(30.dp)
                            .background(color = Color.Black.copy(alpha = 0.6f),shape = RoundedCornerShape(30))
                            .padding(horizontal = 10.dp)
                            ,verticalAlignment = Alignment.CenterVertically)
                        {
                            var level = status?.amplifier.toString()
                            when (level) {
                                "0" -> level = "Ⅰ"
                                "1" -> level = "Ⅱ"
                                "2" -> level = "Ⅲ"
                                "3" -> level = "Ⅳ"
                                "6" -> level = "Ⅴ"
                                "7" -> level = "Ⅵ"
                                "8" -> level = "Ⅶ"
                                "9" -> level = "Ⅷ"
                                "10" -> level = "Ⅸ" //多点没坏处吧？
                            }
                            Column {

                                val fontColor = if (status?.effectType?.value()?.isBeneficial == true) Color.White else Color.Red

                                Text(Text.translatable(status?.translationKey).string.plus(" ").plus(level), fontSize = 7.sp, color = fontColor,
                                    textAlign = TextAlign.Center,
                                )


                                status?.duration?.let {
                                    if (it ==  -1){
                                        Text(("∞"),fontSize = 5.sp, textAlign = TextAlign.Center,color = Color.White.copy(alpha = 0.4f),)
                                    }else {
                                        Text((it / 20).toString() + "s",fontSize = 5.sp, textAlign = TextAlign.Center,color = Color.White.copy(alpha = 0.4f))

                                    }
                                }
                            }
                        }
                    }
                }

            }

        }



        super.renderCompose()
    }




}