package com.xiamo.gui.musicPlayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xiamo.SuperSoft
import com.xiamo.gui.ComposeScreen
import com.xiamo.notification.NotificationManager
import com.xiamo.notification.Notify
import com.xiamo.utils.misc.MediaPlayer
import com.xiamo.utils.misc.NeteaseCloudApi
import com.xiamo.utils.misc.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class MusicPlayerScreen(var parentScreen : Screen? = null) : ComposeScreen(Text.of("MusicPlayer")) {
    var songs = mutableStateListOf<Song>()


    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Preview
    @Composable
    override fun renderCompose() {
        val density = LocalDensity.current

        val backgroundColor = Color(34,11,28)

        val width = with(density) {400.dp }

        val height = with(density) {250.dp }

        val buttonWidth = with(density) {90.dp }

        val buttonHeight = with(density) {20.dp }


        val leftButtonModifier = Modifier.width(width = buttonWidth).height(buttonHeight)
        val leftButtonShape = RoundedCornerShape(20.dp)
        val leftButtonColors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White)

        var searchVisible = remember { mutableStateOf(false) }



        var page = mutableStateOf("Search")

        LaunchedEffect(Unit) {
            isVisible = true
        }
        LaunchedEffect(isVisible){
            if (!isVisible){
                delay(300)
                MinecraftClient.getInstance().execute {
                    MinecraftClient.getInstance().setScreen(null)
                    MinecraftClient.getInstance().overlay = null
                }





            }
        }

        AnimatedVisibility(
            isVisible,
            enter = expandIn(),
            exit = shrinkOut(tween(durationMillis = 300))
        ) {
            MaterialTheme {
                Box(modifier = Modifier.Companion.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {

                    Column(modifier = Modifier
                        .width(width)
                        .height(height)
                        .background(backgroundColor,RoundedCornerShape(5))

                    ) {

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(height-30.dp)
                            ,
                             horizontalArrangement = Arrangement.Center
                        ) {
                            Column(modifier = Modifier
                                .width(width/4)
                                .fillMaxHeight()
                                .background(Color.Black.copy(0.8f), shape = RoundedCornerShape(5))
                                .padding(top = 20.dp)
                                , horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Music Player", fontSize = 11.sp, color = Color.White)


                                Button(modifier = leftButtonModifier, onClick = {
                                    page.value = "Search"
                                }, shape = leftButtonShape, colors = leftButtonColors, contentPadding = PaddingValues(0.dp)){
                                    Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
                                        val bitmap =  SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/search.png").readAllBytes().decodeToImageBitmap()
                                        Icon(bitmap = bitmap,contentDescription = "Search", tint = Color.White, modifier = Modifier.size(12.dp).padding(end = 5.dp))
                                        Text("搜索",fontSize = 5.sp,color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.size(3.dp))
                                Button(modifier = leftButtonModifier,onClick = {
                                    page.value = "List"

                                },shape = leftButtonShape,colors = leftButtonColors, contentPadding = PaddingValues(0.dp)){
                                    Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
                                        val bitmap = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/list.png").readAllBytes().decodeToImageBitmap()
                                        Icon(bitmap = bitmap,contentDescription = "List", tint = Color.White, modifier = Modifier.size(12.dp).padding(end = 5.dp))
                                        Text("列表",fontSize = 5.sp,color = Color.White)
                                    }
                                }

                            }


                            Column(modifier=Modifier.fillMaxSize()){
                                when (page.value) {

                                    "Search" -> searchPage()

                                    "List" ->{

                                    }
                                }


                            }

                        }


                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(45,45,56), RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 12.dp))
                                .padding(horizontal = 15.dp)
                                .height(40.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .shadow(5.dp, RoundedCornerShape(10.dp))
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Transparent)
                                ) {
                                    AsyncImage(
                                        model = MediaPlayer.song.value?.image,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Column(modifier = Modifier.padding(start=5.dp)) {
                                    if (MediaPlayer.song.value != null) {
                                        Text(MediaPlayer.song.value!!.name, fontSize = 7.sp, color = Color.White, lineHeight = 0.sp)
                                        Text(MediaPlayer.song.value!!.singer, fontSize = 5.sp, color = Color.DarkGray, lineHeight = 0.sp)
                                    }
                                }
                            }

                            Button(
                                onClick = { MediaPlayer.toggle() },
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(252,64,74)),
                                modifier = Modifier
                                    .size(width = 50.dp, height = 16.dp)
                                    .align(Alignment.Center)
                            ) {
                                Crossfade(
                                    targetState = MediaPlayer.isPlaying.value,
                                    animationSpec = tween(durationMillis = 200)
                                ) { isPlaying ->
                                    val icon: ImageBitmap = if (isPlaying) {
                                        SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/pause.png")!!
                                            .readAllBytes().decodeToImageBitmap()
                                    } else {
                                        SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/play_fill.png")!!
                                            .readAllBytes().decodeToImageBitmap()
                                    }
                                    Icon(icon, contentDescription = if(isPlaying) "Pause" else "Play", tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                            }
                            Text(
                                "6",
                                modifier = Modifier.align(Alignment.CenterEnd),
                                color = Color.White
                            )
                        }


                    }


                }
            }
        }



        super.renderCompose()
    }



    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun SearchTextField(
        value: String,
        onValueChange: (String) -> Unit,
        width: Int = 200,
        height: Int = 20,
    ) {
        var isHovered by remember { mutableStateOf(false) }


        val borderAlpha by animateFloatAsState(when{
            isHovered -> 1f
            !isHovered -> 0.3f

            else -> {0.3f}
        },tween(200))

        Box(
            modifier = Modifier
                .width(width.dp)
                .height(height.dp)
                .background(Color(50, 20, 40), RoundedCornerShape(4.dp))
                .border(1.dp, Color.White.copy(alpha = borderAlpha), RoundedCornerShape(4.dp))
                .shadow(elevation = 5.dp, shape = RoundedCornerShape(4.dp))
            ,
            contentAlignment = Alignment.CenterStart,

            ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 8.sp,
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .fillMaxWidth()
                    .onPointerEvent(PointerEventType.Enter){isHovered = true}
                    .onPointerEvent(PointerEventType.Exit){isHovered = false}
                    .onFocusChanged { isHovered = it.isFocused }

            )

            if (value.isEmpty()) {
                Text(
                    "请输入歌曲名…",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 8.sp,
                    modifier = Modifier.padding(horizontal = 6.dp).offset(y = -2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }


    @Composable
    fun searchPage() {
        val interactionSource = remember { MutableInteractionSource() }
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit){
            visible = true
        }
        var songName by remember { mutableStateOf("")}
        AnimatedVisibility(visible,enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(horizontalArrangement = Arrangement.Center,verticalAlignment = Alignment.CenterVertically,modifier = Modifier.padding(top = 15.dp).fillMaxWidth()) {
                    SearchTextField(songName,onValueChange = {songName = it })
                    Button(onClick = {
                        search(songName)

                    }, modifier = Modifier
                        .shadow(50.dp, shape = RoundedCornerShape(5.dp))
                        .padding(start = 10.dp)
                        .width(30.dp)
                        .height(20.dp)

                        ,interactionSource = interactionSource,contentPadding = PaddingValues(0.dp),colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f),contentColor = Color.White,)){
                        Icon(SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/search.png").readAllBytes().decodeToImageBitmap(), contentDescription = "Serach", tint = Color.White,modifier = Modifier.size(12.dp))
                    }
                }
                LazyColumn(modifier = Modifier
                    .padding(15.dp)
                    .background(Color(55,67,87).copy(alpha = 0.3f)
                        ,RoundedCornerShape(5.dp))
                    .animateContentSize()
                ) {
                    songs.forEach { song ->
                        item {
                            val interactionSource = remember { MutableInteractionSource() }
                            val isHover = interactionSource.collectIsHoveredAsState()
                            val alpha = animateFloatAsState(targetValue = if (isHover.value) 0.3f else 0.1f).value
                            Box(modifier = Modifier
                                .clickable(onClick = {
                                    Thread{
                                        NeteaseCloudApi.playSong(song)
                                    }.start()

                                })
                                .padding(start = 5.dp,end = 5.dp)
                                .padding(top = 3.dp, bottom = 3.dp)
                                .background(Color.White.copy(alpha = alpha),RoundedCornerShape(5.dp))
                                .fillMaxSize()
                                .hoverable(interactionSource)
                                .padding(vertical = 5.dp)
                                .animateItem()
                               ,
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 5.dp)
                                    ) {
                                        AsyncImage(
                                            model = song.image,
                                            contentDescription = "Song Image",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(0.dp),
                                            modifier = Modifier

                                        ) {
                                            Text(
                                                song.name,
                                                fontSize = 6.sp,
                                                overflow = TextOverflow.Clip,
                                                modifier = Modifier.wrapContentWidth(),
                                                color = Color.White,
                                                lineHeight = 0.sp

                                            )
                                            Text(
                                                song.singer,
                                                fontSize = 5.sp,
                                                modifier = Modifier.wrapContentWidth(),
                                                lineHeight = 0.sp
                                            )
                                        }
                                    }


                                }
                            }

                        }


                    }
                }
            }

        }
    }




    fun search(songName: String) {
        songs.clear()
        val data = NeteaseCloudApi.search(songName)
        if (data != null){
            val root = Json.parseToJsonElement(data).jsonObject
            val songArray = root["result"]!!
                .jsonObject["songs"]!!
                .jsonArray

            songArray.forEach { item ->
                val obj = item.jsonObject

                val name = obj["name"]!!.jsonPrimitive.content

                val id = obj["id"]!!.jsonPrimitive.content

                val singer = obj["ar"]!!
                    .jsonArray[0]
                    .jsonObject["name"]!!
                    .jsonPrimitive.content

                val image = obj["al"]!!
                    .jsonObject["picUrl"]!!
                    .jsonPrimitive.content

                songs.add(Song(name = name, image = image + "?param=200y200", singer = singer, id = id.toLong()))
            }
        }else return
    }







}







