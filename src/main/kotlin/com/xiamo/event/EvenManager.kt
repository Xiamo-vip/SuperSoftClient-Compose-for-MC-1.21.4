package com.xiamo.event

import com.xiamo.SuperSoft
import com.xiamo.module.ModuleManager
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
        if (event.action == GLFW.GLFW_PRESS){
            ModuleManager.modules.forEach {
                if (event.key == it.key){
                    it.toggle()
                }

            }
        }

    }








}