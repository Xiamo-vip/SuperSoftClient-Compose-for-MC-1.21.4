package com.xiamo.gui.hud

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.xiamo.module.ComposeModule
import com.xiamo.module.ModuleManager
import com.xiamo.utils.config.ConfigManager

object HudEditorManager {
    var isEditMode by mutableStateOf(false)
    var selectedComponent by mutableStateOf<HudComponentData?>(null)


    private val components = mutableStateMapOf<String, HudComponentData>()

    fun toggleEditMode() {
        isEditMode = !isEditMode
        if (!isEditMode) {
            selectedComponent = null
            saveAllPositions()
        }
    }

    fun getOrCreateComponent(componentId: String, moduleName: String): HudComponentData {
        return components.getOrPut(componentId) {
            HudComponentData(componentId, moduleName)
        }
    }

    fun selectComponent(component: HudComponentData?) {
        if (isEditMode) {
            selectedComponent = component
        }
    }

    fun isSelected(component: HudComponentData): Boolean {
        return isEditMode && selectedComponent == component
    }

    fun getAllComponents(): List<HudComponentData> {
        return components.values.toList()
    }

    fun saveAllPositions() {
        val positionsMap = mutableMapOf<String, MutableMap<String, Pair<Float, Float>>>()

        components.values.forEach { component ->
            val moduleMap = positionsMap.getOrPut(component.parentModuleName) { mutableMapOf() }
            moduleMap[component.id] = Pair(component.x, component.y)
        }

        positionsMap.forEach { (moduleName, positions) ->
            com.xiamo.module.ModuleManager.modules.find { it.name == moduleName }?.let { module ->
                if (module is com.xiamo.module.ComposeModule) {
                    module.saveComponentPositions(positions)
                }
            }
        }
    }

    fun loadComponentPosition(componentId: String, moduleName: String, defaultX: Float, defaultY: Float): Pair<Float, Float> {
        val module = com.xiamo.module.ModuleManager.modules.find { it.name == moduleName }
        if (module is com.xiamo.module.ComposeModule) {
            return module.loadComponentPosition(componentId, defaultX, defaultY)
        }
        return Pair(defaultX, defaultY)
    }


}
