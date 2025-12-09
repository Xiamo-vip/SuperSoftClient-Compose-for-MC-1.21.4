package com.xiamo.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.xiamo.utils.config.ConfigManager
import com.xiamo.notification.NotificationManager
import com.xiamo.notification.Notify
import com.xiamo.setting.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import java.util.concurrent.CopyOnWriteArrayList

open class Module(val name: String, val description: String, val category: Category) {
    var isComposeScreen = false
    var enabled by mutableStateOf(false)
    var key: Int = -1
    val settings = CopyOnWriteArrayList<AbstractSetting<*>>()
    var settingsExpanded by mutableStateOf(false)

    protected fun booleanSetting(name: String, description: String, defaultValue: Boolean) : BooleanSetting {
        val setting = BooleanSetting(name, description, defaultValue)
        settings.add(setting)
        return setting
    }

    protected fun numberSetting(name: String, description: String, defaultValue: Double, min: Double, max: Double, step: Double = 0.1) : NumberSetting {
        val setting = NumberSetting(name, description, defaultValue, min, max, step)
        settings.add(setting)
        return setting
    }

    protected fun modeSetting(name: String, description: String, defaultValue: String, vararg modes: String) : ModeSetting {
        val setting = ModeSetting(name, description, defaultValue, modes.toList())
        settings.add(setting)
        return setting
    }

    protected fun stringSetting(name: String, description: String, defaultValue: String): StringSetting {
        val setting = StringSetting(name, description, defaultValue)
        settings.add(setting)
        return setting
    }

    protected fun colorSetting(name: String, description: String, defaultValue: Int): ColorSetting {
        val setting = ColorSetting(name, description, defaultValue)
        settings.add(setting)
        return setting
    }

    protected fun keyBindSetting(name: String, description: String, defaultValue: Int): KeyBindSetting {
        val setting = KeyBindSetting(name, description, defaultValue)
        settings.add(setting)
        return setting
    }

    open fun onRender(drawContext: DrawContext) {}
    open fun onTick() {}
    open fun onMouseClicked(mouseX: Int, mouseY: Int) {}
    open fun onMouseReleased(mouseX: Int, mouseY: Int) {}
    open fun renderEntity(entity: Entity,matrix : MatrixStack,tickDelta : Float) {}
    open fun enable() {
        NotificationManager.notifies.add(Notify(this.name, "Toggled", 2000L, {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/enable.png")
                        .readAllBytes().decodeToImageBitmap(),
                    contentDescription = "Disable",
                    tint = Color.Green,
                    modifier = Modifier.padding(end = 10.dp).size(10.dp)
                )
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White, fontSize = 8.sp)) {
                        append(name)
                    }
                    withStyle(style = SpanStyle(color = Color.Green, fontSize = 8.sp)) {
                        append("  Enabled")
                    }
                })
            }
        }))
        this.enabled = true
        ConfigManager.saveModule(this)
    }

    open fun disable() {
        NotificationManager.notifies.add(Notify(this.name, "Toggled", 2000L, {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/disable.png")
                        .readAllBytes().decodeToImageBitmap(),
                    contentDescription = "Disable",
                    tint = Color.Red,
                    modifier = Modifier.padding(end = 10.dp).size(10.dp)
                )
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White, fontSize = 8.sp)) {
                        append(name)
                    }
                    withStyle(style = SpanStyle(color = Color.Red, fontSize = 8.sp)) {
                        append("  Disabled")
                    }
                })
            }
        }))
        this.enabled = false
        ConfigManager.saveModule(this)
    }

    open fun toggle() {
        if (this.enabled) {
            disable()
        } else enable()
    }

    open fun onSettingChanged(setting: AbstractSetting<*>) {
        ConfigManager.saveModule(this)
    }
}
