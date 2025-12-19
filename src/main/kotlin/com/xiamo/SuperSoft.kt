package com.xiamo

import com.xiamo.event.EvenManager
import com.xiamo.module.ModuleManager
import com.xiamo.utils.config.ConfigManager
import net.fabricmc.api.ModInitializer
import org.jetbrains.skiko.hostOs
import org.slf4j.LoggerFactory

object SuperSoft : ModInitializer {
    val logger = LoggerFactory.getLogger("SuperSoft Client")
    val dataPath = "SuperSoftClient"

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("SuperSoft Loaded")

		if (hostOs.isMacOS) {
			System.setProperty("skiko.macos.opengl.enabled", "true")
		}

		EvenManager
		ModuleManager
        ConfigManager.init()


	}
}