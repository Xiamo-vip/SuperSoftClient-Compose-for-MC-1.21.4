package com.xiamo.module.modules.movement

import com.xiamo.module.Category
import com.xiamo.module.Module
import net.minecraft.client.MinecraftClient

object Sprint : Module("Sprint","强制疾跑", Category.Movement) {

    override fun onTick() {
        val mc = MinecraftClient.getInstance()
        val player = mc.player
        if(mc != null && player != null) {
            if (mc.options.forwardKey.isPressed
                && !player.isInFluid
                && !player.isSneaking
                && !player.isSwimming
                && !player.isClimbing
                ){
                mc.options.sprintKey.isPressed = true
            }
        }

        super.onTick()
    }


}