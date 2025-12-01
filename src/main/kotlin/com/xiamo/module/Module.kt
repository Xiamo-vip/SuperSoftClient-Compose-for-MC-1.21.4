package com.xiamo.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.SuperSoft
import com.xiamo.notification.NotificationManager
import com.xiamo.notification.Notify
import com.xiamo.setting.AbstractSetting
import net.minecraft.client.gui.DrawContext
import java.util.concurrent.CopyOnWriteArrayList


open class Module(val name : String,val description : String,val category: Category) {
    var isComposeScreen = false
    var enabled by  mutableStateOf(false)
    var key : Int =  -1

    val settings = CopyOnWriteArrayList<AbstractSetting<*>>()


    open fun onRender(drawContext: DrawContext){}

    open fun onTick(){}

    open fun onMouseClicked(mouseX: Int, mouseY : Int){}
    open fun onMouseReleased(mouseX: Int, mouseY : Int){}

    open fun enable(){
        NotificationManager.notifies.add(Notify(this.name,"Toggled",2000L,{
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/enable.png").readAllBytes().decodeToImageBitmap(), contentDescription = "Disable", tint = Color.Green,modifier = Modifier.padding(end = 10.dp).size(12.dp))
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White, fontSize = 10.sp)){
                        append(name)
                    }
                    withStyle(style = SpanStyle(color = Color.Green, fontSize = 10.sp)){
                        append("  Enabled")
                    }
                })
            }
        }))
        this.enabled = true
    }

    open fun disable(){
        NotificationManager.notifies.add(Notify(this.name,"Toggled",2000L,{
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/disable.png").readAllBytes().decodeToImageBitmap(), contentDescription = "Disable", tint = Color.Red,modifier = Modifier.padding(end = 10.dp).size(12.dp))
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White, fontSize = 10.sp)){
                        append(name)
                    }
                    withStyle(style = SpanStyle(color = Color.Red, fontSize = 10.sp)){
                        append("  Disabled")
                    }
                })
            }
        }))
        this.enabled = false

    }

    open fun toggle(){
        if (this.enabled){
            disable()
        }else enable()
    }







}