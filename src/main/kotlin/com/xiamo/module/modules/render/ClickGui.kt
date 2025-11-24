package com.xiamo.module.modules.render

import com.xiamo.gui.clickGui.ClickGuiScreen
import com.xiamo.module.Category
import com.xiamo.module.Module
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

object ClickGui : Module("ClickGui","ClickGui", Category.Render){
    init {
        this.key  = GLFW.GLFW_KEY_RIGHT_SHIFT
    }


    override fun toggle() {
        MinecraftClient.getInstance().setScreen(null)
        super.toggle()
    }

    override fun enable() {
        MinecraftClient.getInstance().setScreen(ClickGuiScreen())
        super.enable()
    }



}