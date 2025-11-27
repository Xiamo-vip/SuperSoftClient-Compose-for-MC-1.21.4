package com.xiamo.utils.misc

import com.goxr3plus.streamplayer.stream.StreamPlayer
import com.goxr3plus.streamplayer.stream.StreamPlayerEvent
import com.goxr3plus.streamplayer.stream.StreamPlayerListener
import com.xiamo.utils.config.ConfigManager.mainDir
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import net.fabricmc.loader.impl.game.minecraft.applet.AppletMain
import net.minecraft.client.MinecraftClient
import javazoom.jl.player.*
import javazoom.jl.player.advanced.AdvancedPlayer
import java.io.File
import java.io.FileInputStream
import javax.sound.sampled.AudioSystem
import javax.swing.JLayer
import javax.swing.JLayeredPane

object MediaPlayer : StreamPlayerListener {
    var player : StreamPlayer = StreamPlayer()

    fun playSound(file : File){
        Thread{
            player.open(file)
            player.addStreamPlayerListener(this)
            player.play()
        }.start()
    }

    fun play(){
        Thread{
            player.play()
        }.start()
    }

    fun pause(){
        Thread{
            player.pause()
        }
    }




    override fun opened(p0: Any?, p1: Map<String?, Any?>?) {
        TODO("Not yet implemented")
    }

    override fun progress(
        p0: Int,
        p1: Long,
        p2: ByteArray?,
        p3: Map<String?, Any?>?
    ) {
        TODO("Not yet implemented")
    }

    override fun statusUpdated(p0: StreamPlayerEvent?) {
        TODO("Not yet implemented")
    }


}