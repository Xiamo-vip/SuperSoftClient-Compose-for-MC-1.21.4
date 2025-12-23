package com.xiamo.module.modules.movement

import com.xiamo.module.Category
import com.xiamo.module.Module
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.option.KeyBinding
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
import org.lwjgl.glfw.GLFW

object InventoryMove : Module("Inventory Move","Inventory Move", Category.Movement) {


    override fun onTick() {
        val mc = MinecraftClient.getInstance()
        if (!enabled || mc.currentScreen == null || mc.currentScreen is ChatScreen) return
        val options = mc.options
        val keys = listOf(options.forwardKey, options.backKey, options.leftKey, options.rightKey, options.jumpKey,options.sneakKey)
        for (key in keys) {
            val code = key.defaultKey.code
            if (net.minecraft.client.util.InputUtil.isKeyPressed(mc.window.handle, code)) {
                key.isPressed = true
            }
        }
    }



    override fun onKey(keyCode: Int, keyState: Int,scanCode: Int) {
        val gameOptions = MinecraftClient.getInstance().options
        if (!shouldHandle(keyCode)) return
        if (keyCode == gameOptions.forwardKey.defaultKey.code) {
            gameOptions.forwardKey.isPressed = if (keyState == 1 || keyState == 2) true else false
            return
        }
        if (keyCode == gameOptions.leftKey.defaultKey.code) {
            gameOptions.leftKey.isPressed = if (keyState == 1 || keyState == 2) true else false
            return
        }
        if (keyCode == gameOptions.rightKey.defaultKey.code) {
            gameOptions.rightKey.isPressed = if (keyState == 1 || keyState == 2) true else false
            return
        }
        if (keyCode == gameOptions.backKey.defaultKey.code) {
            gameOptions.backKey.isPressed = if (keyState == 1 || keyState == 2) true else false
            return
        }
        if (keyCode == gameOptions.jumpKey.defaultKey.code) {
            gameOptions.jumpKey.isPressed = if (keyState == 1 || keyState == 2) true else false
            return
        }





        super.onKey(keyCode, keyState,scanCode)
    }

    fun shouldHandle(keyCode: Int): Boolean {
        val screen = MinecraftClient.getInstance().currentScreen ?: return false
        if (MinecraftClient.getInstance().world == null)return false
        if (screen is ChatScreen) return false
        return true

    }

}