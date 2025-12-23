package com.xiamo.gui.hud

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.xiamo.module.ComposeModule
import com.xiamo.module.ModuleManager


data class ComponentPositionData(val x: Float, val y: Float, val scale: Float)

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

    fun onScroll(delta: Double) {
        if (isEditMode && selectedComponent != null) {
            selectedComponent?.adjustScale(delta.toFloat() * 0.05f)
        }
    }

    fun saveAllPositions() {
        val positionsMap = mutableMapOf<String, MutableMap<String, ComponentPositionData>>()

        components.values.forEach { component ->
            val moduleMap = positionsMap.getOrPut(component.parentModuleName) { mutableMapOf() }
            moduleMap[component.id] = ComponentPositionData(component.x, component.y, component.scale)
        }

        positionsMap.forEach { (moduleName, positions) ->
            ModuleManager.modules.find { it.name == moduleName }?.let { module ->
                if (module is ComposeModule) {
                    module.saveComponentPositions(positions)
                }
            }
        }
    }

    fun loadComponentPosition(componentId: String, moduleName: String, defaultX: Float, defaultY: Float, defaultScale: Float = 1f): ComponentPositionData {
        val module = ModuleManager.modules.find { it.name == moduleName }
        if (module is ComposeModule) {
            return module.loadComponentPosition(componentId, defaultX, defaultY, defaultScale)
        }
        return ComponentPositionData(defaultX, defaultY, defaultScale)
    }


}
