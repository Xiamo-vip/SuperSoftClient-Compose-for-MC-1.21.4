package com.xiamo.utils.misc

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.goxr3plus.streamplayer.enums.Status
import com.goxr3plus.streamplayer.stream.StreamPlayer
import com.goxr3plus.streamplayer.stream.StreamPlayerEvent
import com.goxr3plus.streamplayer.stream.StreamPlayerListener
import com.xiamo.module.modules.misc.MusicPlayer
import com.xiamo.module.modules.render.DynamicIsland
import java.io.File

object MediaPlayer : StreamPlayerListener {
    val player : StreamPlayer = StreamPlayer()

    var isPlaying = mutableStateOf(false)
    var song = mutableStateOf<Song?>(null)
    var songFile = mutableStateOf<File?>(null)
    var tick =  mutableStateOf(0)
    var lyric = mutableStateListOf<LyricLine>()

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
        tick.value = (microsecondPosition / 1000).toInt()
    }

    override fun statusUpdated(event: StreamPlayerEvent) {
        if (event.playerStatus == Status.STOPPED){
            DynamicIsland.unregisterPermanent(MusicPlayer)
            isPlaying.value = false
        }
        if (event.playerStatus == Status.PLAYING){
            DynamicIsland.registerPermanent(MusicPlayer) {
                Text("正在播放：" + this.song.value?.name, color = Color.White, fontSize = 6.sp)
                AsyncImage(this.song.value?.image, modifier = Modifier.padding(start = 5.dp).size(12.dp).clip(RoundedCornerShape(1.dp)), contentDescription = null)
            }
        }

    }




}