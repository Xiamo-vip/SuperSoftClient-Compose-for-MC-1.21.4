package com.xiamo.module

import com.xiamo.SuperSoft
import com.xiamo.gui.musicPlayer.MusicPlayerScreen
import com.xiamo.module.modules.misc.MusicPlayer
import com.xiamo.module.modules.render.ClickGui
import com.xiamo.module.modules.render.DynamicIsland
import com.xiamo.module.modules.render.Hud
import com.xiamo.module.modules.render.Lyric
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.minecraft.client.MinecraftClient
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration

object ModuleManager {
    val modules = CopyOnWriteArrayList<Module>()



    init {
        modules.add(Hud)
        modules.add(ClickGui)
        modules.add(MusicPlayer)
        modules.add(DynamicIsland)
        modules.add(Lyric)





        SuperSoft.logger.info("Module Loaded")
    }







}