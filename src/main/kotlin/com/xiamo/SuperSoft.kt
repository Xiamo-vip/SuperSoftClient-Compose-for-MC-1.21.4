package com.xiamo

import com.xiamo.alt.AltManager
import com.xiamo.event.EvenManager
import com.xiamo.module.ModuleManager
import com.xiamo.utils.CoilInitializer
import com.xiamo.utils.config.ConfigManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.jetbrains.skiko.hostOs
import org.slf4j.LoggerFactory

object SuperSoft : ModInitializer {
    val logger = LoggerFactory.getLogger("SuperSoft Client")
    val dataPath = "SuperSoftClient"

	override fun onInitialize() {
		logger.info("SuperSoft Loaded")

		if (hostOs.isMacOS) {
			System.setProperty("skiko.macos.opengl.enabled", "true")
		}

		EvenManager
		ModuleManager
        ConfigManager.init()
        CoilInitializer.init()


		ClientLifecycleEvents.CLIENT_STARTED.register {
			AltManager.init()
		}
	}
}