package com.xiamo.utils.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.SuperSoft
import com.xiamo.gui.ComposeScreen
import com.xiamo.notification.NotificationManager
import com.xiamo.notification.Notify
import com.xiamo.utils.misc.MediaPlayer.lyric
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.net.URLEncoder


object NeteaseCloudApi {
    val host = "127.0.0.1"
    val port = 3000
    val url = "http://$host:$port"

    val client = OkHttpClient()




    fun getFile(id : Long): SongFile {
        val body = FormBody.Builder()
        var request = Request.Builder()
            .url(url + "/song/url/v1?id=${id.toString()}&level=exhigh&UnblockNeteaseMusic=true")
            .header("Cookie", "MUSIC_U=00BCE1A962BD832C89353973D024F67C5ACD2400E18968A01F9F38494529C4EC51410353E39FD45BE8DD3F9C9F069D5DC18FD712E5DA4CFEE2975606C5FEEA7B8166B421C86F5ED6BEB753FEF5A057A0195B520E1E65ED2FA3428E02E158D3067B09A4EC23111FD6BF77DEFE4B97EB8BF1635DF4ADCE8E39CBFFF1D68075AC051452055C77C846FCAB7D002C6FD6594CE9FC4DD344FAF72685FF29AC5940C450BBEC59ECCA0089A2DA6AD09A5C77FA0432EAB488A370625FC2794CD4355E72C1AC33D2325D40344F2F22EFC1506C2A21A4D3076F30D001A7EF9753A26B052652A839DCB3A8089474D76C68AABA0D2CFDBEC6445BE7678377B768B304C238DD894F19DC44B632EEF12755042E1E03C10F207D7AE1FA1452D27E0E2AFD13B742EE6595219464455B134CF57AB43F98A67AB5915E99D9AC675791303715510E5E683F4E34666BC47382D40A584675DC615C83E9216734147E544CF6BC36E0BA95D9CB")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if(response.isSuccessful){
                val json = Json.parseToJsonElement(response.body.string())
                val url = json.jsonObject["data"]?.jsonArray[0]?.jsonObject["url"]!!.jsonPrimitive.content
                val size = json.jsonObject["data"]?.jsonArray[0]?.jsonObject["size"]!!.jsonPrimitive.content
                Thread{
                    getLyric(id)
                }.start()
                return SongFile(url,size.toLong())
            }
        }

        return SongFile("", 0)
    }

    fun getLyric(id : Long) {
        val request = Request.Builder()
            .url(url+"/lyric?id=${id.toString()}")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if(response.isSuccessful){
                val json = Json.parseToJsonElement(response.body.string())
                lyric.clear()
                lyric.addAll(LyricLineProcessor.parseLyricLine(json.jsonObject.get("lrc")!!.jsonObject.get("lyric")!!.jsonPrimitive.content))

            }
        }

    }




    fun search(name : String): String? {
        val body = FormBody.Builder()
        val encode = URLEncoder.encode(name, "utf-8")
        val request = Request.Builder()
            .url(url + "/cloudsearch?keywords=$encode")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if(response.isSuccessful){
                    return response.body.string()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }



        return null
    }

    fun playSong(song: Song): Boolean {
        val songID = song.id
        try {
            val songfile = NeteaseCloudApi.getFile(songID)
            val url = songfile.url
            val size = songfile.size
            println("Downloading from URL: $url")
            val cacheDir = File(SuperSoft.dataPath, "cache")
            if (!cacheDir.exists()) {
                val ok = cacheDir.mkdirs()
                if (!ok) {
                    println("Failed to create cache directory: ${cacheDir.absolutePath}")
                    return false
                }
            }

            val file = File(cacheDir, "$songID.mp3")
            if (file.exists() && file.length() >= size.toLong()) {
                println("Song already cached, playing directly: ${file.absolutePath}")
                MediaPlayer.playSound(file,song)
                return true
            }
            NotificationManager.add(Notify("MusicPlayer_Downloading","音乐下载中...",-1L,{
                Row(horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically) {
                    Icon(modifier = Modifier.size(20.dp).padding(end = 5.dp), bitmap = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/download.png").readAllBytes().decodeToImageBitmap(), contentDescription = "Download", tint = Color.Blue)
                    Text("音乐下载中...", color = Color.White, fontSize = 8.sp)
                }
            }))
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Request failed with code: ${response.code}")
                    return false
                }
                val body = response.body
                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            NotificationManager.notifies.removeAll{it.titile=="MusicPlayer_Downloading"}
            NotificationManager.add(Notify("MusicPlayer_Completed","音乐下载中...",1000L,{
                Row(horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically) {
                    Icon(modifier = Modifier.size(20.dp).padding(end = 5.dp), bitmap = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/completed.png").readAllBytes().decodeToImageBitmap(), contentDescription = "Completed", tint = Color.Green)
                    Text("音乐缓存完毕",color = Color.White, fontSize = 8.sp)
                }

            }))

            if (!file.exists() || file.length() == 0L) {
                println("File was not written correctly: ${file.absolutePath}")
                return false
            }
            MediaPlayer.playSound(file,song)
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


}


@Serializable
data class Song(val image: String ,val name: String,val singer: String,val id : Long) {
}

data class SongFile(val url:String,val size : Long)