package com.xiamo.module

import com.xiamo.SuperSoft
import com.xiamo.module.modules.combat.KillAura
import com.xiamo.module.modules.misc.MusicPlayer
import com.xiamo.module.modules.movement.Speed
import com.xiamo.module.modules.movement.Sprint
import com.xiamo.module.modules.render.Brightness
import com.xiamo.module.modules.render.ClickGui
import com.xiamo.module.modules.render.DynamicIsland
import com.xiamo.module.modules.render.ESP
import com.xiamo.module.modules.render.EffectHud
import com.xiamo.module.modules.render.Hud
import com.xiamo.module.modules.render.KeyboradHud
import com.xiamo.module.modules.render.Lyric
import java.util.concurrent.CopyOnWriteArrayList

object ModuleManager {
    val modules = CopyOnWriteArrayList<Module>()



    init {
        modules.add(DynamicIsland)
        modules.add(Hud)
        modules.add(ClickGui)
        modules.add(MusicPlayer)
        modules.add(Lyric)
        modules.add(Sprint)
        modules.add(Speed)
        modules.add(KillAura)
        modules.add(ESP)
        modules.add(Brightness)
        modules.add(EffectHud)
        modules.add(KeyboradHud)






        SuperSoft.logger.info("Module Loaded")
    }







}