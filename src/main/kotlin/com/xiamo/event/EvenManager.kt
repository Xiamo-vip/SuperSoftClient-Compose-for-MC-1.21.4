package com.xiamo.event

import androidx.compose.material3.Switch
import com.xiamo.SuperSoft
import com.xiamo.gui.titleScreen.TitleScreen
import com.xiamo.module.ModuleManager
import com.xiamo.utils.rotation.RotationManager
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW


object  EvenManager {


    init {
        eventBus.subscribe()


        SuperSoft.logger.info("EventBus Loaded")
    }


    @EventTarget
    fun renderEvent(event: RenderEvent){
        ModuleManager.modules.filter { it.enabled }.forEach {
            it.onRender(event.drawContext)
        }
    }


    @EventTarget
    fun mouseClickedEvent(event: MouseClickedEvent){
        ModuleManager.modules.filter { it.enabled }.forEach {
            it.onMouseClicked(event.mouseX,event.mouseY)
        }

    }

    @EventTarget
    fun mouseReleaseEvent(event: MouseReleasedEvent){
        ModuleManager.modules.filter { it.enabled }.forEach {
            it.onMouseReleased(event.mouseX,event.mouseY)
        }

    }

    @EventTarget
    fun keyBoardEvent(event: KeyBoardEvent){
        if (event.action == GLFW.GLFW_PRESS && MinecraftClient.getInstance().currentScreen == null) {
            ModuleManager.modules.forEach {
                if (event.key == it.key){
                    it.toggle()
                }

            }
        }

        ModuleManager.modules.filter { it.enabled }.forEach {
            it.onKey(event.key,event.action,event.scanCode)
        }


    }

    @EventTarget
    fun tickEvent(tickEvent: TickEvent){
        ModuleManager.modules.filter { it.enabled }.forEach {
            it.onTick()
        }
        RotationManager.tick()
    }


    @EventTarget
    fun navigateEvent(navigateEvent: NavigateEvent){
        when(navigateEvent.screen.javaClass){
            net.minecraft.client.gui.screen.TitleScreen().javaClass -> {
                MinecraftClient.getInstance().setScreen(TitleScreen())
            }
        }

    }


    @EventTarget
    fun renderEntityEvent(entityRenderEvent: EntityRenderEvent){
        ModuleManager.modules.filter { it.enabled }.forEach {
            it.renderEntity(entityRenderEvent.entity,entityRenderEvent.matrix,entityRenderEvent.tickDelta)
        }
    }








}