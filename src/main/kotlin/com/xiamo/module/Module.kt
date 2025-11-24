package com.xiamo.module

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.xiamo.setting.AbstractSetting
import net.minecraft.client.gui.DrawContext
import java.util.concurrent.CopyOnWriteArrayList


open class Module(val name : String,val description : String,val category: Category) {
    var enabled by  mutableStateOf(false)
    var key : Int =  -1

    val settings = CopyOnWriteArrayList<AbstractSetting<*>>()


    open fun onRender(drawContext: DrawContext){}

    open fun onTick(){}

    open fun onMouseClicked(mouseX: Int, mouseY : Int){}
    open fun onMouseReleased(mouseX: Int, mouseY : Int){}

    open fun enable(){
        this.enabled = true
    }

    open fun disable(){
        this.enabled = false

    }

    open fun toggle(){
        if (this.enabled){
            disable()
        }else enable()
    }







}