package com.xiamo.utils.misc

import androidx.compose.foundation.layout.Row
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
import javax.sound.sampled.AudioSystem

object MediaPlayer : StreamPlayerListener {
    val player: StreamPlayer = StreamPlayer()

    var isPlaying = mutableStateOf(false)
    var song = mutableStateOf<Song?>(null)
    var songFile = mutableStateOf<File?>(null)
    var tick = mutableStateOf(0)
    var totalDuration = mutableStateOf(0L)
    var lyric = mutableStateListOf<LyricLine>()
    var playlist = mutableStateListOf<Song>()
    var currentIndex = mutableStateOf(0)
    var volume = mutableStateOf(1f)
    private var isChangingSong = false
    private var lastPlayedTick = 0

    fun playSound(file: File, song: Song) {
        Thread {
            isChangingSong = false
            lastPlayedTick = 0
            songFile.value = file
            player.stop()
            player.open(songFile.value)
            this.song.value = song
            player.addStreamPlayerListener(this)
            isPlaying.value = true
            totalDuration.value = calculateDuration(file)
            player.play()
        }.start()
    }

    private fun calculateDuration(file: File): Long {
        return try {
            val audioInputStream = AudioSystem.getAudioInputStream(file)
            val format = audioInputStream.format
            val frames = audioInputStream.frameLength
            val durationInSeconds = frames / format.frameRate
            audioInputStream.close()
            (durationInSeconds * 1000).toLong()
        } catch (e: Exception) {
            0L
        }
    }

    fun play() {
        Thread {
            player.resume()
            isPlaying.value = true
        }.start()
    }

    fun pause() {
        Thread {
            player.pause()
            isPlaying.value = false
        }.start()
    }

    fun toggle() {
        if (isPlaying.value) pause() else play()
    }

    fun stop() {
        Thread {
            player.stop()
            isPlaying.value = false
            tick.value = 0
        }.start()
    }

    fun seekTo(positionMs: Long) {
        if (songFile.value == null || totalDuration.value <= 0) return
        Thread {
            try {
                val targetBytes = ((positionMs.toDouble() / totalDuration.value) * player.totalBytes).toLong()
                player.seekBytes(targetBytes)
                tick.value = positionMs.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun seekToPercent(percent: Float) {
        val targetMs = (totalDuration.value * percent).toLong()
        seekTo(targetMs)
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        playlist.clear()
        playlist.addAll(songs)
        currentIndex.value = startIndex
    }

    fun playNext() {
        if (playlist.isEmpty() || isChangingSong) return
        isChangingSong = true
        currentIndex.value = (currentIndex.value + 1) % playlist.size
        Thread {
            try {
                NeteaseCloudApi.playSong(playlist[currentIndex.value])
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isChangingSong = false
            }
        }.start()
    }

    fun playPrevious() {
        if (playlist.isEmpty() || isChangingSong) return
        isChangingSong = true
        currentIndex.value = if (currentIndex.value > 0) currentIndex.value - 1 else playlist.size - 1
        Thread {
            try {
                NeteaseCloudApi.playSong(playlist[currentIndex.value])
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isChangingSong = false
            }
        }.start()
    }

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun setVolume(value: Float) {
        volume.value = value.coerceIn(0f, 1f)
        try {
            player.setGain(volume.value.toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        lastPlayedTick = tick.value
    }

    override fun statusUpdated(event: StreamPlayerEvent) {
        if (event.playerStatus == Status.STOPPED || event.playerStatus == Status.PAUSED) {
            DynamicIsland.unregisterPermanent(MusicPlayer)
            isPlaying.value = false
            val playedEnough = lastPlayedTick > 5000 && totalDuration.value > 0 &&
                    lastPlayedTick >= (totalDuration.value * 0.9).toLong()
            if (playlist.isNotEmpty() && playedEnough && !isChangingSong) {
                playNext()
            }
        }
        if (event.playerStatus == Status.PLAYING) {
            DynamicIsland.registerPermanent(MusicPlayer) {
                Text("正在播放：" + this.song.value?.name, color = Color.White, fontSize = 6.sp)
                AsyncImage(this.song.value?.image, modifier = Modifier.padding(start = 5.dp).size(12.dp).clip(RoundedCornerShape(1.dp)), contentDescription = null)
            }
        }
    }
}
