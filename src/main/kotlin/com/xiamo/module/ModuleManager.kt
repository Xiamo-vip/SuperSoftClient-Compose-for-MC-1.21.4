package com.xiamo.module

import com.xiamo.SuperSoft
import com.xiamo.module.modules.combat.KillAura
import com.xiamo.module.modules.misc.MusicPlayer
import com.xiamo.module.modules.movement.Speed
import com.xiamo.module.modules.movement.Sprint
import com.xiamo.module.modules.player.ChestStealer
import com.xiamo.module.modules.render.Brightness
import com.xiamo.module.modules.render.ClickGui
import com.xiamo.module.modules.render.DynamicIsland
import com.xiamo.module.modules.render.ESP
import com.xiamo.module.modules.render.EffectHud
import com.xiamo.module.modules.render.Hud
import com.xiamo.module.modules.render.KeyboradHud
import com.xiamo.module.modules.render.Lyric
import com.xiamo.module.modules.render.NameTags
import com.xiamo.module.modules.render.PlayerList
import com.xiamo.module.modules.render.ScoreBoard
import java.util.concurrent.CopyOnWriteArrayList

object ModuleManager {
    val modules = CopyOnWriteArrayList<Module>()



    init {
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
        modules.add(NameTags)
        modules.add(ChestStealer)
        modules.add(PlayerList)
        modules.add(ScoreBoard)
        modules.add(DynamicIsland)







        SuperSoft.logger.info("Module Loaded")
    }







}