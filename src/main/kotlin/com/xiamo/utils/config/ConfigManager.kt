package com.xiamo.utils.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.xiamo.SuperSoft
import com.xiamo.gui.hud.HudEditorManager
import com.xiamo.module.ComposeModule
import com.xiamo.module.Module
import com.xiamo.module.ModuleManager
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object ConfigManager {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    val mainDir = File("SuperSoftClient").also {
        if (!it.exists()) it.mkdirs()
    }


    private val configDir = File(mainDir, "Config").also {
        if (!it.exists()) it.mkdirs()
    }


    private val modulesDir = File(configDir, "modules").also {
        if (!it.exists()) it.mkdirs()
    }

    private var loaded = false


    private fun getModuleConfigFile(module: Module): File {
        return File(modulesDir, "${module.name}.json")
    }

    fun init() {
        load()
//        Runtime.getRuntime().addShutdownHook(Thread {
//            save()
//        })
        SuperSoft.logger.info("ConfigManager initialized")
    }


    fun save() {
        ModuleManager.modules.forEach { module ->
            saveModule(module)
        }
        SuperSoft.logger.info("All module configs saved to ${modulesDir.absolutePath}")
    }


    fun saveModule(module: Module) {
        try {

            val moduleObject = JsonObject()
            moduleObject.addProperty("enabled", module.enabled)
            moduleObject.addProperty("key", module.key)
            val settingsObject = JsonObject()
            module.settings.forEach { setting ->
                settingsObject.add(setting.name, setting.toJson())
            }
            moduleObject.add("settings", settingsObject)


            val configFile = getModuleConfigFile(module)
            FileWriter(configFile).use { writer ->
                gson.toJson(moduleObject, writer)
            }
        } catch (e: Exception) {
            SuperSoft.logger.error("Failed to save module config for ${module.name}: ${e.message}")
            e.printStackTrace()
        }
    }


    fun load() {
        ModuleManager.modules.forEach { module ->
            loadModule(module)
        }
        SuperSoft.logger.info("All module configs loaded from ${modulesDir.absolutePath}")
        loaded = true
    }


    private fun loadModule(module: Module) {
        val configFile = getModuleConfigFile(module)

        if (!configFile.exists()) {
            return
        }

        try {
            FileReader(configFile).use { reader ->
                val moduleObject = JsonParser.parseReader(reader).asJsonObject


                if (moduleObject.has("enabled")) {
                    val shouldBeEnabled = moduleObject.get("enabled").asBoolean
                    module.enabled = shouldBeEnabled
                }


                if (moduleObject.has("key")) {
                    module.key = moduleObject.get("key").asInt
                }


                if (moduleObject.has("settings")) {
                    val settingsObject = moduleObject.getAsJsonObject("settings")
                    module.settings.forEach { setting ->
                        if (settingsObject.has(setting.name)) {
                            setting.fromJson(settingsObject.get(setting.name))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            SuperSoft.logger.error("Failed to load module config for ${module.name}: ${e.message}")
            e.printStackTrace()
        }
    }


    fun resetAll() {
        ModuleManager.modules.forEach { module ->
            module.enabled = false
            module.settings.forEach { it.reset() }
            saveModule(module)
        }
    }


    fun resetModule(module: Module) {
        module.settings.forEach { it.reset() }
        saveModule(module)
    }


    fun exportConfig(exportDir: File) {
        try {
            val exportModulesDir = File(exportDir, "modules").also {
                if (!it.exists()) it.mkdirs()
            }

            ModuleManager.modules.forEach { module ->
                val sourceFile = getModuleConfigFile(module)
                if (sourceFile.exists()) {
                    val targetFile = File(exportModulesDir, "${module.name}.json")
                    sourceFile.copyTo(targetFile, overwrite = true)
                }
            }
            SuperSoft.logger.info("Config exported to ${exportDir.absolutePath}")
        } catch (e: Exception) {
            SuperSoft.logger.error("Failed to export config: ${e.message}")
        }
    }


    fun importConfig(importDir: File) {
        try {
            val importModulesDir = File(importDir, "modules")
            if (!importModulesDir.exists()) {
                SuperSoft.logger.error("Import directory does not contain modules folder")
                return
            }

            ModuleManager.modules.forEach { module ->
                val sourceFile = File(importModulesDir, "${module.name}.json")
                if (sourceFile.exists()) {
                    val targetFile = getModuleConfigFile(module)
                    sourceFile.copyTo(targetFile, overwrite = true)
                }
            }
            load()
            SuperSoft.logger.info("Config imported from ${importDir.absolutePath}")
        } catch (e: Exception) {
            SuperSoft.logger.error("Failed to import config: ${e.message}")
        }
    }


    fun deleteModuleConfig(module: Module) {
        try {
            val configFile = getModuleConfigFile(module)
            if (configFile.exists()) {
                configFile.delete()
            }
        } catch (e: Exception) {
            SuperSoft.logger.error("Failed to delete module config for ${module.name}: ${e.message}")
        }
    }
}