package com.xiamo.utils.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.SuperSoft
import com.xiamo.notification.NotificationManager
import com.xiamo.notification.Notify
import com.xiamo.utils.misc.MediaPlayer.lyric
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.TimeUnit


object NeteaseCloudApi {
    val host = "39.108.121.101"
    val port = 3000
    val url = "http://$host:$port"

    val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    var cookie = mutableStateOf("")
    var isLoggedIn = mutableStateOf(false)
    var userProfile = mutableStateOf<UserProfile?>(null)

    private val cookieFile = File(SuperSoft.dataPath, "netease_cookie.txt")

    init {
        loadCookie()
    }

    private fun loadCookie() {
        try {
            if (cookieFile.exists()) {
                val savedCookie = cookieFile.readText().trim()
                if (savedCookie.isNotEmpty()) {
                    cookie.value = savedCookie
                    isLoggedIn.value = true
                    Thread {
                        loadUserProfile()
                    }.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveCookie() {
        try {
            val parentDir = cookieFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }
            cookieFile.writeText(cookie.value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseCookie(rawCookie: String): String {
        val cookieParts = mutableListOf<String>()
        val regex = Regex("(MUSIC_U|MUSIC_A_T|MUSIC_R_T|__csrf)=([^;]+)")
        regex.findAll(rawCookie).forEach { match ->
            val name = match.groupValues[1]
            val value = match.groupValues[2]
            if (name == "MUSIC_U" || name == "__csrf") {
                cookieParts.add("$name=$value")
            }
        }
        val result = cookieParts.joinToString("; ")
        println("Parsed cookie: $result")
        return result
    }

    fun sendCaptcha(phone: String, ctcode: String = "86"): Boolean {
        val request = Request.Builder()
            .url("$url/captcha/sent?phone=$phone&ctcode=$ctcode")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    return json.jsonObject["code"]?.jsonPrimitive?.content == "200"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun loginWithCaptcha(phone: String, captcha: String, ctcode: String = "86"): Boolean {
        val request = Request.Builder()
            .url("$url/login/cellphone?phone=$phone&captcha=$captcha&countrycode=$ctcode")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body.string()
                    println("Login response: $body")
                    val json = Json.parseToJsonElement(body)
                    if (json.jsonObject["code"]?.jsonPrimitive?.content == "200") {
                        val rawCookie = json.jsonObject["cookie"]?.jsonPrimitive?.content ?: ""
                        println("Raw cookie from API: $rawCookie")
                        cookie.value = parseCookie(rawCookie)
                        isLoggedIn.value = true
                        saveCookie()
                        loadUserProfile()
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getQrKey(): String? {
        val request = Request.Builder()
            .url("$url/login/qr/key?timestamp=${System.currentTimeMillis()}")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    return json.jsonObject["data"]?.jsonObject?.get("unikey")?.jsonPrimitive?.content
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getQrImage(key: String): String? {
        val request = Request.Builder()
            .url("$url/login/qr/create?key=$key&qrimg=true&timestamp=${System.currentTimeMillis()}")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    return json.jsonObject["data"]?.jsonObject?.get("qrimg")?.jsonPrimitive?.content
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun checkQrStatus(key: String): QrStatus {
        val request = Request.Builder()
            .url("$url/login/qr/check?key=$key&timestamp=${System.currentTimeMillis()}")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body.string()
                    val json = Json.parseToJsonElement(body)
                    val code = json.jsonObject["code"]?.jsonPrimitive?.content?.toInt() ?: 0
                    val cookieStr = json.jsonObject["cookie"]?.jsonPrimitive?.content
                    return when (code) {
                        800 -> QrStatus.EXPIRED
                        801 -> QrStatus.WAITING
                        802 -> QrStatus.SCANNED
                        803 -> {
                            if (!cookieStr.isNullOrEmpty()) {
                                println("QR Login raw cookie from API: $cookieStr")
                                cookie.value = parseCookie(cookieStr)
                                isLoggedIn.value = true
                                saveCookie()
                                loadUserProfile()
                            }
                            QrStatus.SUCCESS
                        }
                        else -> QrStatus.WAITING
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return QrStatus.WAITING
    }

    fun loadUserProfile() {
        val request = Request.Builder()
            .url("$url/login/status?timestamp=${System.currentTimeMillis()}")
            .header("Cookie", cookie.value)
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    val profile = json.jsonObject["data"]?.jsonObject?.get("profile")?.jsonObject
                    if (profile != null) {
                        userProfile.value = UserProfile(
                            userId = profile["userId"]?.jsonPrimitive?.content?.toLong() ?: 0L,
                            nickname = profile["nickname"]?.jsonPrimitive?.content ?: "",
                            avatarUrl = profile["avatarUrl"]?.jsonPrimitive?.content ?: ""
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logout() {
        cookie.value = ""
        isLoggedIn.value = false
        userProfile.value = null
        try {
            if (cookieFile.exists()) {
                cookieFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHotTopics(limit: Int = 10, offset: Int = 0): List<HotTopic> {
        val result = mutableListOf<HotTopic>()
        val request = Request.Builder()
            .url("$url/hot/topic?limit=$limit&offset=$offset")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    val hotTopics = json.jsonObject["data"]?.jsonObject?.get("hot")?.jsonArray
                    hotTopics?.forEach { item ->
                        val obj = item.jsonObject
                        result.add(HotTopic(
                            title = obj["title"]?.jsonPrimitive?.content ?: "",
                            participateCount = obj["participateCount"]?.jsonPrimitive?.content?.toLong() ?: 0L,
                            actId = obj["actId"]?.jsonPrimitive?.content?.toLong() ?: 0L
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getSearchHotDetail(): List<SearchHot> {
        val result = mutableListOf<SearchHot>()
        val request = Request.Builder()
            .url("$url/search/hot/detail")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    val data = json.jsonObject["data"]?.jsonArray
                    data?.forEach { item ->
                        val obj = item.jsonObject
                        result.add(SearchHot(
                            searchWord = obj["searchWord"]?.jsonPrimitive?.content ?: "",
                            score = obj["score"]?.jsonPrimitive?.content?.toLong() ?: 0L,
                            content = obj["content"]?.jsonPrimitive?.content ?: "",
                            iconUrl = obj["iconUrl"]?.jsonPrimitive?.content
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getPersonalizedNewSongs(limit: Int = 20): List<Song> {
        val result = mutableListOf<Song>()
        val request = Request.Builder()
            .url("$url/personalized/newsong?limit=$limit")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    val data = json.jsonObject["result"]?.jsonArray
                    data?.forEach { item ->
                        val obj = item.jsonObject
                        val song = obj["song"]?.jsonObject
                        if (song != null) {
                            val name = obj["name"]?.jsonPrimitive?.content ?: ""
                            val id = obj["id"]?.jsonPrimitive?.content?.toLong() ?: 0L
                            val picUrl = obj["picUrl"]?.jsonPrimitive?.content ?: ""
                            val artists = song["artists"]?.jsonArray
                            val singer = artists?.firstOrNull()?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
                            result.add(Song(name = name, image = "$picUrl?param=200y200", singer = singer, id = id))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getTopNewSongs(type: Int = 0): List<Song> {
        val result = mutableListOf<Song>()
        val request = Request.Builder()
            .url("$url/top/song?type=$type")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    val data = json.jsonObject["data"]?.jsonArray
                    data?.take(20)?.forEach { item ->
                        val obj = item.jsonObject
                        val name = obj["name"]?.jsonPrimitive?.content ?: ""
                        val id = obj["id"]?.jsonPrimitive?.content?.toLong() ?: 0L
                        val album = obj["album"]?.jsonObject
                        val picUrl = album?.get("picUrl")?.jsonPrimitive?.content ?: ""
                        val artists = obj["artists"]?.jsonArray
                        val singer = artists?.firstOrNull()?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
                        result.add(Song(name = name, image = "$picUrl?param=200y200", singer = singer, id = id))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getRecommendPlaylists(limit: Int = 10): List<Playlist> {
        val result = mutableListOf<Playlist>()
        val request = Request.Builder()
            .url("$url/personalized?limit=$limit")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    val data = json.jsonObject["result"]?.jsonArray
                    data?.forEach { item ->
                        val obj = item.jsonObject
                        val id = obj["id"]?.jsonPrimitive?.content?.toLong() ?: 0L
                        val name = obj["name"]?.jsonPrimitive?.content ?: ""
                        val picUrl = obj["picUrl"]?.jsonPrimitive?.content ?: ""
                        val playCount = obj["playCount"]?.jsonPrimitive?.content?.toLong() ?: 0L
                        result.add(Playlist(id = id, name = name, coverUrl = "$picUrl?param=200y200", playCount = playCount))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getPlaylistDetail(id: Long): List<Song> {
        val result = mutableListOf<Song>()
        val request = Request.Builder()
            .url("$url/playlist/track/all?id=$id&limit=50")
            .header("Cookie", cookie.value)
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    val songs = json.jsonObject["songs"]?.jsonArray
                    songs?.forEach { item ->
                        val obj = item.jsonObject
                        val name = obj["name"]?.jsonPrimitive?.content ?: ""
                        val songId = obj["id"]?.jsonPrimitive?.content?.toLong() ?: 0L
                        val album = obj["al"]?.jsonObject
                        val picUrl = album?.get("picUrl")?.jsonPrimitive?.content ?: ""
                        val artists = obj["ar"]?.jsonArray
                        val singer = artists?.firstOrNull()?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
                        result.add(Song(name = name, image = "$picUrl?param=200y200", singer = singer, id = songId))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getLikeList(): List<Long> {
        val result = mutableListOf<Long>()
        val uid = userProfile.value?.userId ?: return result
        val request = Request.Builder()
            .url("$url/likelist?uid=$uid&timestamp=${System.currentTimeMillis()}")
            .header("Cookie", cookie.value)
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    val ids = json.jsonObject["ids"]?.jsonArray
                    ids?.forEach { id ->
                        result.add(id.jsonPrimitive.content.toLong())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getSongDetails(ids: List<Long>): List<Song> {
        val result = mutableListOf<Song>()
        if (ids.isEmpty()) return result
        val idsStr = ids.take(50).joinToString(",")
        val request = Request.Builder()
            .url("$url/song/detail?ids=$idsStr")
            .header("Cookie", cookie.value)
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = Json.parseToJsonElement(response.body.string())
                    val songs = json.jsonObject["songs"]?.jsonArray
                    songs?.forEach { item ->
                        val obj = item.jsonObject
                        val name = obj["name"]?.jsonPrimitive?.content ?: ""
                        val id = obj["id"]?.jsonPrimitive?.content?.toLong() ?: 0L
                        val album = obj["al"]?.jsonObject
                        val picUrl = album?.get("picUrl")?.jsonPrimitive?.content ?: ""
                        val artists = obj["ar"]?.jsonArray
                        val singer = artists?.firstOrNull()?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
                        result.add(Song(name = name, image = "$picUrl?param=200y200", singer = singer, id = id))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getLikeSongs(): List<Song> {
        val likeIds = getLikeList()
        val songs = getSongDetails(likeIds)
        val idToSong = songs.associateBy { it.id }
        return likeIds.mapNotNull { idToSong[it] }
    }

    fun getFile(id: Long): SongFile {
        val cookieHeader = if (cookie.value.isNotEmpty()) cookie.value else "YOUR_TOKEN"
        var request = Request.Builder()
            .url(url + "/song/url/v1?id=${id}&level=exhigh&UnblockNeteaseMusic=true")
            .header("Cookie", cookieHeader)
            .get()
            .build()
        println("=== getFile Debug ===")
        println("Cookie length: ${cookieHeader.length}")
        println("Cookie contains MUSIC_U: ${cookieHeader.contains("MUSIC_U")}")
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body.string()
                println("Song URL response: $bodyStr")
                val json = Json.parseToJsonElement(bodyStr)
                val songUrl = json.jsonObject["data"]?.jsonArray?.get(0)?.jsonObject?.get("url")?.jsonPrimitive?.content ?: ""
                val size = json.jsonObject["data"]?.jsonArray?.get(0)?.jsonObject?.get("size")?.jsonPrimitive?.content ?: "0"
                println("Song URL: $songUrl")
                println("Song size: $size")
                Thread {
                    getLyric(id)
                }.start()
                return SongFile(songUrl, size.toLong())
            }
        }
        return SongFile("", 0)
    }

    fun getLyric(id: Long) {
        val request = Request.Builder()
            .url(url + "/lyric?id=$id")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val json = Json.parseToJsonElement(response.body.string())
                lyric.clear()
                val lyricStr = json.jsonObject["lrc"]?.jsonObject?.get("lyric")?.jsonPrimitive?.content ?: ""
                lyric.addAll(LyricLineProcessor.parseLyricLine(lyricStr))
            }
        }
    }

    fun search(name: String): String? {
        val encode = URLEncoder.encode(name, "utf-8")
        val request = Request.Builder()
            .url(url + "/cloudsearch?keywords=$encode")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return response.body.string()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun playSong(song: Song): Boolean {
        val songID = song.id
        try {
            val songfile = getFile(songID)
            val fileUrl = songfile.url
            val size = songfile.size

            if (fileUrl.isNullOrEmpty()) {
                println("Song URL is null or empty for song: ${song.name}")
                return false
            }

            val cacheDir = File(SuperSoft.dataPath, "cache")
            if (!cacheDir.exists()) {
                val ok = cacheDir.mkdirs()
                if (!ok) {
                    return false
                }
            }

            val file = File(cacheDir, "$songID.mp3")
            if (file.exists() && file.length() >= size) {
                MediaPlayer.playSound(file, song)
                return true
            }
            NotificationManager.add(Notify("MusicPlayer_Downloading", "音乐下载中...", -1L, {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(modifier = Modifier.size(20.dp).padding(end = 5.dp), bitmap = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/download.png").readAllBytes().decodeToImageBitmap(), contentDescription = "Download", tint = Color.Blue)
                    Text("音乐下载中...", color = Color.White, fontSize = 8.sp)
                }
            }))
            val downloadClient = OkHttpClient()
            val downloadRequest = Request.Builder().url(fileUrl).build()
            downloadClient.newCall(downloadRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return false
                }
                val body = response.body
                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            NotificationManager.notifies.removeAll { it.titile == "MusicPlayer_Downloading" }
            NotificationManager.add(Notify("MusicPlayer_Completed", "音乐下载中...", 1000L, {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(modifier = Modifier.size(20.dp).padding(end = 5.dp), bitmap = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/completed.png").readAllBytes().decodeToImageBitmap(), contentDescription = "Completed", tint = Color.Green)
                    Text("音乐缓存完毕", color = Color.White, fontSize = 8.sp)
                }
            }))

            if (!file.exists() || file.length() == 0L) {
                return false
            }
            MediaPlayer.playSound(file, song)
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}

enum class QrStatus {
    WAITING, SCANNED, SUCCESS, EXPIRED
}

data class UserProfile(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String
)

data class HotTopic(
    val title: String,
    val participateCount: Long,
    val actId: Long
)

data class SearchHot(
    val searchWord: String,
    val score: Long,
    val content: String,
    val iconUrl: String?
)

@Serializable
data class Song(val image: String, val name: String, val singer: String, val id: Long)

data class SongFile(val url: String, val size: Long)

data class Playlist(
    val id: Long,
    val name: String,
    val coverUrl: String,
    val playCount: Long
)
