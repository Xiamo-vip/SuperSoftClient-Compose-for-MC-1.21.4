package com.xiamo.module.modules.render

import androidx.compose.material.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.xiamo.module.Category
import com.xiamo.module.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects


object Brightness  : Module("Brightness","夜视", Category.Render) {
    override fun onTick() {

        MinecraftClient.getInstance().player?.statusEffects?.contains(StatusEffectInstance(StatusEffects.RESISTANCE))?.let {
            if (!it) {
                MinecraftClient.getInstance().player?.addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION,-1))
            }
        }
        super.onTick()
    }

    override fun enable() {
        DynamicIsland.registerPermanent(this){
            Text("夜视：开",fontSize = 6.sp, color = Color.White)
        }
        super.enable()
    }

    override fun disable() {
        DynamicIsland.unregisterPermanent(this)
        MinecraftClient.getInstance().player?.removeStatusEffect(StatusEffects.NIGHT_VISION)
        super.disable()
    }



}