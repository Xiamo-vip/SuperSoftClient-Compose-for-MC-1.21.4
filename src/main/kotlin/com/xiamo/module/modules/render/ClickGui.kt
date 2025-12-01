package com.xiamo.module.modules.render

import com.xiamo.gui.ComposeScreen
import com.xiamo.gui.clickGui.ClickGuiScreen
import com.xiamo.module.Category
import com.xiamo.module.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import org.lwjgl.glfw.GLFW

object ClickGui : Module("ClickGui","ClickGui", Category.Render){


    var instance : ComposeScreen? = null



    init {
        this.key  = GLFW.GLFW_KEY_RIGHT_SHIFT
        this.isComposeScreen  = true
    }


    override fun disable() {
        instance?.isVisible = false
        super.disable()
    }
    override fun enable() {
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen is ComposeScreen){
            currentScreen.isVisible = false
        }
        instance = ClickGuiScreen(currentScreen)
        MinecraftClient.getInstance().setScreen(instance)
        super.enable()
    }



}