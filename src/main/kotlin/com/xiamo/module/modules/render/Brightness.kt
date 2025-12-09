package com.xiamo.module.modules.render

import com.xiamo.module.Category
import com.xiamo.module.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry

object Brightness  : Module("Brightness","夜视", Category.Render) {

    override fun onTick() {
        MinecraftClient.getInstance().player?.addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION,0))
        super.onTick()
    }

    override fun disable() {
        MinecraftClient.getInstance().player?.removeStatusEffect(StatusEffects.NIGHT_VISION)
        super.disable()
    }



}