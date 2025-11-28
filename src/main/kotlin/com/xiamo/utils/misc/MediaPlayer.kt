package com.xiamo.utils.misc

import androidx.compose.runtime.mutableStateOf
import com.goxr3plus.streamplayer.enums.Status
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
    val player : StreamPlayer = StreamPlayer()

    var isPlaying = mutableStateOf(false)
    var song = mutableStateOf<Song?>(null)
    var songFile = mutableStateOf<File?>(null)
    var tick =  mutableStateOf(0)

    fun playSound(file : File,song: Song){

        Thread{
            songFile.value = file
            player.stop()
            player.open(songFile.value)
            this.song.value = song
            player.addStreamPlayerListener(this)
            isPlaying.value = true
            player.play()
        }.start()
    }

    fun play(){
        Thread{
            player.resume()
            isPlaying.value = true
        }.start()
    }

    fun pause(){
        Thread{
            player.pause()
            isPlaying.value = false
        }.start()
    }

    fun toggle() {
        if(isPlaying.value) pause() else play()
    }

    override fun opened(dataSource: Any?, properties: Map<String?, Any?>?) {

    }

    override fun progress(
        nEncodedBytes: Int,
        microsecondPosition: Long,
        pcmData: ByteArray?,
        properties: Map<String?, Any?>?
    ) {
        tick.value = microsecondPosition.toInt()
    }

    override fun statusUpdated(event: StreamPlayerEvent?) {
        if (event?.playerStatus == Status.STOPPED){isPlaying.value = false}

    }


}