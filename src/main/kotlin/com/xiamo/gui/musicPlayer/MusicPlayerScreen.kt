package com.xiamo.gui.musicPlayer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xiamo.SuperSoft
import com.xiamo.gui.ComposeScreen
import com.xiamo.utils.misc.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class MusicPlayerScreen(var parentScreen: Screen? = null) : ComposeScreen(Text.of("MusicPlayer")) {
    private var songs = mutableStateListOf<Song>()
    private var hotTopics = mutableStateListOf<HotTopic>()
    private var searchHots = mutableStateListOf<SearchHot>()
    private var newSongs = mutableStateListOf<Song>()
    private var likeSongs = mutableStateListOf<Song>()
    private var recommendPlaylists = mutableStateListOf<Playlist>()
    private var playlistSongs = mutableStateListOf<Song>()
    private var currentPlaylist = mutableStateOf<Playlist?>(null)

    private val primaryColor = Color(0xFFFC404A)
    private val backgroundColor = Color(0xFF1A0A14)
    private val surfaceColor = Color(0xFF2D1D28)
    private val cardColor = Color(0xFF3A2A35)

    private var localIsLoggedIn = mutableStateOf(false)
    private var localUserProfile = mutableStateOf<UserProfile?>(null)
    private var localCurrentSong = mutableStateOf<Song?>(null)
    private var localIsPlaying = mutableStateOf(false)
    private var localTick = mutableStateOf(0)
    private var localTotalDuration = mutableStateOf(0L)
    private var localVolume = mutableStateOf(1f)
    private var likeSongsLoaded = mutableStateOf(false)

    private fun syncState() {
        localIsLoggedIn.value = NeteaseCloudApi.isLoggedIn.value
        localUserProfile.value = NeteaseCloudApi.userProfile.value
        localCurrentSong.value = MediaPlayer.song.value
        localIsPlaying.value = MediaPlayer.isPlaying.value
        localTick.value = MediaPlayer.tick.value
        localTotalDuration.value = MediaPlayer.totalDuration.value
        localVolume.value = MediaPlayer.volume.value
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Preview
    @Composable
    override fun renderCompose() {
        val density = LocalDensity.current
        var currentPage by remember { mutableStateOf("Home") }
        var showPlayerDetail by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            isVisible = true
            scope.launch {
                loadHomeData()
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                syncState()
                delay(50)
            }
        }

        LaunchedEffect(isVisible) {
            if (!isVisible) {
                delay(300)
                MinecraftClient.getInstance().execute {
                    MinecraftClient.getInstance().setScreen(null)
                    MinecraftClient.getInstance().overlay = null
                }
            }
        }

        AnimatedVisibility(
            isVisible,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.9f),
            exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.9f)
        ) {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .width(380.dp)
                            .height(260.dp)
                            .shadow(16.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(backgroundColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            SideBar(
                                currentPage = currentPage,
                                onPageChange = { currentPage = it },
                                modifier = Modifier.width(70.dp).fillMaxHeight()
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(surfaceColor.copy(alpha = 0.3f))
                            ) {
                                AnimatedContent(
                                    targetState = currentPage,
                                    transitionSpec = {
                                        (fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 4 })
                                            .togetherWith(fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 })
                                    }
                                ) { page ->
                                    when (page) {
                                        "Home" -> HomePage()
                                        "Search" -> SearchPage()
                                        "Like" -> LikePage()
                                        "Login" -> LoginPage()
                                    }
                                }
                            }
                        }

                        BottomPlayerBar(
                            onExpandClick = { showPlayerDetail = true },
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        )
                    }

                    AnimatedVisibility(
                        showPlayerDetail,
                        enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
                        exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
                    ) {
                        PlayerDetailPage(onClose = { showPlayerDetail = false })
                    }
                }
            }
        }
        super.renderCompose()
    }

    private fun loadHomeData() {
        Thread {
            try {
                val songs = NeteaseCloudApi.getTopNewSongs()
                newSongs.clear()
                newSongs.addAll(songs)

                val playlists = NeteaseCloudApi.getRecommendPlaylists(6)
                recommendPlaylists.clear()
                recommendPlaylists.addAll(playlists)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    @Composable
    fun SideBar(currentPage: String, onPageChange: (String) -> Unit, modifier: Modifier = Modifier) {
        val isLoggedIn by localIsLoggedIn
        val userProfile by localUserProfile

        Column(
            modifier = modifier
                .background(Color.Black.copy(0.6f))
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Music", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoggedIn && userProfile != null) {
                AsyncImage(
                    model = userProfile!!.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, primaryColor, CircleShape)
                )
                Text(
                    userProfile!!.nickname,
                    fontSize = 5.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            NavButton("首页", "Home", currentPage, onPageChange)
            NavButton("搜索", "Search", currentPage, onPageChange)
            NavButton("喜欢", "Like", currentPage, onPageChange)

            Spacer(modifier = Modifier.weight(1f))

            if (isLoggedIn) {
                NavButton("退出", "Logout", currentPage) {
                    NeteaseCloudApi.logout()
                    likeSongs.clear()
                    likeSongsLoaded.value = false
                    syncState()
                }
            } else {
                NavButton("登录", "Login", currentPage, onPageChange)
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun NavButton(label: String, page: String, currentPage: String, onClick: (String) -> Unit) {
        val isSelected = currentPage == page
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        val bgAlpha by animateFloatAsState(
            when {
                isSelected -> 0.3f
                isHovered -> 0.15f
                else -> 0f
            },
            tween(150)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 1.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(primaryColor.copy(alpha = bgAlpha))
                .hoverable(interactionSource)
                .clickable { onClick(page) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                fontSize = 7.sp,
                color = if (isSelected) primaryColor else Color.White.copy(0.8f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    fun HomePage() {
        val scope = rememberCoroutineScope()
        var isLoading by remember { mutableStateOf(newSongs.isEmpty()) }
        var carouselIndex by remember { mutableStateOf(0) }
        val playlist by currentPlaylist

        LaunchedEffect(Unit) {
            if (newSongs.isEmpty()) {
                scope.launch {
                    loadHomeData()
                    isLoading = false
                }
            }
        }

        LaunchedEffect(newSongs.size) {
            if (newSongs.isNotEmpty()) {
                while (true) {
                    delay(3000)
                    carouselIndex = (carouselIndex + 1) % minOf(5, newSongs.size)
                }
            }
        }

        AnimatedContent(
            targetState = playlist,
            transitionSpec = {
                (fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 4 })
                    .togetherWith(fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 })
            }
        ) { selectedPlaylist ->
            if (selectedPlaylist != null) {
                PlaylistDetailPage(selectedPlaylist) { currentPlaylist.value = null }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Text("热门歌曲", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        if (isLoading || newSongs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                val carouselSongs = newSongs.take(5)
                                AnimatedContent(
                                    targetState = carouselIndex,
                                    transitionSpec = {
                                        (fadeIn(tween(500)) + slideInHorizontally(tween(500)) { it })
                                            .togetherWith(fadeOut(tween(500)) + slideOutHorizontally(tween(500)) { -it })
                                    }
                                ) { index ->
                                    val song = carouselSongs.getOrNull(index)
                                    if (song != null) {
                                        CarouselItem(song) {
                                            Thread {
                                                MediaPlayer.setPlaylist(newSongs, newSongs.indexOf(song))
                                                NeteaseCloudApi.playSong(song)
                                            }.start()
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(minOf(5, newSongs.size)) { i ->
                                        Box(
                                            modifier = Modifier
                                                .size(if (i == carouselIndex) 8.dp else 4.dp, 4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (i == carouselIndex) primaryColor else Color.White.copy(0.5f))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("推荐歌单", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        if (recommendPlaylists.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                recommendPlaylists.forEach { pl ->
                                    PlaylistItem(pl) {
                                        currentPlaylist.value = pl
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("新歌速递", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(newSongs.drop(5).take(6)) { song ->
                        SongItem(song = song, onClick = {
                            Thread {
                                MediaPlayer.setPlaylist(newSongs, newSongs.indexOf(song))
                                NeteaseCloudApi.playSong(song)
                            }.start()
                        })
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun CarouselItem(song: Song, onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
        ) {
            AsyncImage(
                model = song.image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(0.7f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    song.name,
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    song.singer,
                    fontSize = 6.sp,
                    color = Color.White.copy(0.7f),
                    maxLines = 1
                )
            }

            AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        bitmap = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/play_fill.png")!!
                            .readAllBytes().decodeToImageBitmap(),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun PlaylistItem(playlist: Playlist, onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val scale by animateFloatAsState(if (isHovered) 1.05f else 1f, tween(150))

        Column(
            modifier = Modifier
                .width(55.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .hoverable(interactionSource)
                .clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                AsyncImage(
                    model = playlist.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Black.copy(0.6f))
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                ) {
                    Text(
                        formatPlayCount(playlist.playCount),
                        fontSize = 4.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                playlist.name,
                fontSize = 5.sp,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 6.sp
            )
        }
    }

    @Composable
    fun PlaylistDetailPage(playlist: Playlist, onBack: () -> Unit) {
        var isLoading by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(playlist.id) {
            Thread {
                try {
                    val songs = NeteaseCloudApi.getPlaylistDetail(playlist.id)
                    playlistSongs.clear()
                    playlistSongs.addAll(songs)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isLoading = false
            }.start()
        }

        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.1f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = Color.White, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        playlist.name,
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${playlistSongs.size}首歌曲",
                        fontSize = 5.sp,
                        color = Color.White.copy(0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(playlistSongs) { index, song ->
                        SongItem(
                            song = song,
                            index = index + 1,
                            onClick = {
                                Thread {
                                    MediaPlayer.setPlaylist(playlistSongs.toList(), index)
                                    NeteaseCloudApi.playSong(song)
                                }.start()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun formatPlayCount(count: Long): String {
        return when {
            count >= 100000000 -> String.format("%.1f亿", count / 100000000.0)
            count >= 10000 -> String.format("%.1f万", count / 10000.0)
            else -> count.toString()
        }
    }

    @Composable
    fun SearchPage() {
        var songName by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchTextField(
                    value = songName,
                    onValueChange = { songName = it },
                    modifier = Modifier.weight(1f)
                )
                SearchButton(onClick = {
                    if (songName.isNotBlank()) {
                        scope.launch {
                            search(songName)
                        }
                    }
                })
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(songs) { index, song ->
                    SongItem(
                        song = song,
                        index = index + 1,
                        onClick = {
                            Thread {
                                MediaPlayer.setPlaylist(songs.toList(), index)
                                NeteaseCloudApi.playSong(song)
                            }.start()
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun SearchTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
        var isFocused by remember { mutableStateOf(false) }
        val borderAlpha by animateFloatAsState(if (isFocused) 1f else 0.3f, tween(200))

        Box(
            modifier = modifier
                .height(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(cardColor)
                .border(1.dp, primaryColor.copy(alpha = borderAlpha), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 7.sp),
                cursorBrush = SolidColor(primaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .onPointerEvent(PointerEventType.Enter) { isFocused = true }
                    .onPointerEvent(PointerEventType.Exit) { isFocused = false }
            )
            if (value.isEmpty()) {
                Text(
                    "输入歌曲名或歌手...",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 6.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun SearchButton(onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val scale by animateFloatAsState(if (isHovered) 1.05f else 1f, tween(100))

        Box(
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(4.dp))
                .background(primaryColor)
                .hoverable(interactionSource)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                bitmap = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/search.png")!!
                    .readAllBytes().decodeToImageBitmap(),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun SongItem(song: Song, index: Int? = null, onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val currentSong by localCurrentSong
        val isPlaying by localIsPlaying
        val isCurrentSong = currentSong?.id == song.id
        val bgAlpha by animateFloatAsState(
            when {
                isCurrentSong -> 0.4f
                isHovered -> 0.25f
                else -> 0.1f
            },
            tween(150)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(if (isCurrentSong) primaryColor.copy(alpha = bgAlpha) else Color.White.copy(alpha = bgAlpha))
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (index != null) {
                Text(
                    "$index",
                    fontSize = 6.sp,
                    color = if (index <= 3) primaryColor else Color.White.copy(0.5f),
                    modifier = Modifier.width(14.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = if (index <= 3) FontWeight.Bold else FontWeight.Normal
                )
            }

            AsyncImage(
                model = song.image,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(3.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f).padding(start = 5.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    song.name,
                    fontSize = 6.sp,
                    color = if (isCurrentSong) primaryColor else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    song.singer,
                    fontSize = 5.sp,
                    color = Color.White.copy(0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isCurrentSong && isPlaying) {
                PlayingIndicator()
            }
        }
    }

    @Composable
    fun PlayingIndicator() {
        val infiniteTransition = rememberInfiniteTransition()
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.height(10.dp)
        ) {
            repeat(3) { index ->
                val height by infiniteTransition.animateFloat(
                    initialValue = 3f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(300 + index * 100, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(height.dp)
                        .background(primaryColor, RoundedCornerShape(1.dp))
                )
            }
        }
    }

    @Composable
    fun LikePage() {
        val isLoggedIn by localIsLoggedIn
        var isLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(isLoggedIn, likeSongsLoaded.value) {
            if (isLoggedIn && !likeSongsLoaded.value) {
                isLoading = true
                Thread {
                    try {
                        val songs = NeteaseCloudApi.getLikeSongs()
                        likeSongs.clear()
                        likeSongs.addAll(songs)
                        likeSongsLoaded.value = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    isLoading = false
                }.start()
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Text("我喜欢的音乐", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            if (!isLoggedIn) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("请先登录", fontSize = 7.sp, color = Color.White.copy(0.5f))
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(likeSongs) { index, song ->
                        SongItem(
                            song = song,
                            onClick = {
                                Thread {
                                    MediaPlayer.setPlaylist(likeSongs.toList(), index)
                                    NeteaseCloudApi.playSong(song)
                                }.start()
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun LoginPage() {
        var loginType by remember { mutableStateOf("phone") }

        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("登录网易云音乐", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LoginTypeTab("手机号", loginType == "phone") { loginType = "phone" }
                Spacer(modifier = Modifier.width(12.dp))
                LoginTypeTab("二维码", loginType == "qr") { loginType = "qr" }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedContent(
                targetState = loginType,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                }
            ) { type ->
                when (type) {
                    "phone" -> PhoneLoginContent()
                    "qr" -> QrLoginContent()
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun LoginTypeTab(label: String, selected: Boolean, onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val bgAlpha by animateFloatAsState(
            when {
                selected -> 1f
                isHovered -> 0.5f
                else -> 0f
            },
            tween(150)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(primaryColor.copy(alpha = bgAlpha))
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                label,
                fontSize = 7.sp,
                color = if (selected) Color.White else Color.White.copy(0.6f),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    @Composable
    fun PhoneLoginContent() {
        var phone by remember { mutableStateOf("") }
        var captcha by remember { mutableStateOf("") }
        var countdown by remember { mutableStateOf(0) }
        var message by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(countdown) {
            if (countdown > 0) {
                delay(1000)
                countdown--
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoginTextField(
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
                placeholder = "手机号"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LoginTextField(
                    value = captcha,
                    onValueChange = { captcha = it.filter { c -> c.isDigit() }.take(6) },
                    placeholder = "验证码",
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (countdown > 0) cardColor else primaryColor)
                        .clickable(enabled = countdown == 0 && phone.length == 11) {
                            scope.launch {
                                Thread {
                                    val success = NeteaseCloudApi.sendCaptcha(phone)
                                    if (success) {
                                        countdown = 60
                                        message = "验证码已发送"
                                    } else {
                                        message = "发送失败"
                                    }
                                }.start()
                            }
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (countdown > 0) "${countdown}s" else "获取验证码",
                        fontSize = 6.sp,
                        color = Color.White
                    )
                }
            }

            if (message.isNotEmpty()) {
                Text(message, fontSize = 6.sp, color = if (message.contains("失败")) Color.Red else Color.Green)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isLoading) cardColor else primaryColor)
                    .clickable(enabled = !isLoading && phone.length == 11 && captcha.length >= 4) {
                        isLoading = true
                        scope.launch {
                            Thread {
                                val success = NeteaseCloudApi.loginWithCaptcha(phone, captcha)
                                message = if (success) "登录成功" else "登录失败"
                                isLoading = false
                            }.start()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                } else {
                    Text("登录", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun LoginTextField(
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String,
        modifier: Modifier = Modifier
    ) {
        var isFocused by remember { mutableStateOf(false) }
        val borderAlpha by animateFloatAsState(if (isFocused) 1f else 0.3f, tween(200))

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(26.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(cardColor)
                .border(1.dp, primaryColor.copy(alpha = borderAlpha), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 7.sp),
                cursorBrush = SolidColor(primaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .onPointerEvent(PointerEventType.Enter) { isFocused = true }
                    .onPointerEvent(PointerEventType.Exit) { isFocused = false }
            )
            if (value.isEmpty()) {
                Text(placeholder, color = Color.White.copy(0.4f), fontSize = 6.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }

    @Composable
    fun QrLoginContent() {
        var qrKey by remember { mutableStateOf<String?>(null) }
        var qrImage by remember { mutableStateOf<String?>(null) }
        var status by remember { mutableStateOf("等待扫码") }
        var shouldCheck by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            Thread {
                val key = NeteaseCloudApi.getQrKey()
                if (key != null) {
                    qrKey = key
                    val img = NeteaseCloudApi.getQrImage(key)
                    qrImage = img
                }
            }.start()
        }

        LaunchedEffect(qrKey, shouldCheck) {
            if (qrKey != null && shouldCheck) {
                while (shouldCheck) {
                    delay(2000)
                    val checkStatus = NeteaseCloudApi.checkQrStatus(qrKey!!)
                    status = when (checkStatus) {
                        QrStatus.WAITING -> "等待扫码"
                        QrStatus.SCANNED -> "已扫码，请确认"
                        QrStatus.SUCCESS -> {
                            shouldCheck = false
                            "登录成功"
                        }
                        QrStatus.EXPIRED -> {
                            shouldCheck = false
                            "二维码已过期"
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (qrImage != null) {
                    val base64Data = qrImage!!.substringAfter("base64,")
                    val bytes = java.util.Base64.getDecoder().decode(base64Data)
                    val bitmap = bytes.decodeToImageBitmap()
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(4.dp)
                    )
                } else {
                    CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(18.dp), strokeWidth = 1.5.dp)
                }
            }

            Text(status, fontSize = 6.sp, color = Color.White.copy(0.7f))

            if (status == "二维码已过期") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(primaryColor)
                        .clickable {
                            scope.launch {
                                Thread {
                                    val key = NeteaseCloudApi.getQrKey()
                                    if (key != null) {
                                        qrKey = key
                                        val img = NeteaseCloudApi.getQrImage(key)
                                        qrImage = img
                                        shouldCheck = true
                                        status = "等待扫码"
                                    }
                                }.start()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("刷新", fontSize = 6.sp, color = Color.White)
                }
            }

            Text("请使用网易云音乐APP扫码登录", fontSize = 5.sp, color = Color.White.copy(0.4f))
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun BottomPlayerBar(onExpandClick: () -> Unit, modifier: Modifier = Modifier) {
        val song by localCurrentSong
        val isPlaying by localIsPlaying
        val tick by localTick
        val totalDuration by localTotalDuration
        val progress = if (totalDuration > 0) tick.toFloat() / totalDuration else 0f

        Box(
            modifier = modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(0.8f))
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animateFloatAsState(progress, tween(100)).value)
                        .background(primaryColor)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (song != null) {
                    val rotation by rememberInfiniteTransition().animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(8000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clickable(onClick = onExpandClick)
                    ) {
                        AsyncImage(
                            model = song!!.image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .rotate(if (isPlaying) rotation else 0f)
                                .border(1.dp, primaryColor.copy(0.5f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp).clickable(onClick = onExpandClick),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            song!!.name,
                            fontSize = 7.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${song!!.singer} · ${MediaPlayer.formatTime(tick.toLong())}/${MediaPlayer.formatTime(totalDuration)}",
                            fontSize = 5.sp,
                            color = Color.White.copy(0.5f),
                            maxLines = 1
                        )
                    }
                } else {
                    Text("暂无播放", fontSize = 7.sp, color = Color.White.copy(0.5f), modifier = Modifier.weight(1f))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerControlButton(
                        icon = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/music_last.png")!!
                            .readAllBytes().decodeToImageBitmap(),
                        size = 14.dp,
                        onClick = { MediaPlayer.playPrevious() }
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .clickable { MediaPlayer.toggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            bitmap = if (isPlaying) {
                                SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/pause.png")!!
                                    .readAllBytes().decodeToImageBitmap()
                            } else {
                                SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/play_fill.png")!!
                                    .readAllBytes().decodeToImageBitmap()
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    PlayerControlButton(
                        icon = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/music_next.png")!!
                            .readAllBytes().decodeToImageBitmap(),
                        size = 14.dp,
                        onClick = { MediaPlayer.playNext() }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun TextButton(text: String, onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val alpha by animateFloatAsState(if (isHovered) 1f else 0.7f, tween(100))

        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White.copy(alpha),
            modifier = Modifier
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(4.dp)
        )
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun PlayerControlButton(
        icon: ImageBitmap,
        size: androidx.compose.ui.unit.Dp,
        isPrimary: Boolean = false,
        onClick: () -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val scale by animateFloatAsState(if (isHovered) 1.1f else 1f, tween(100))

        Box(
            modifier = Modifier
                .size(if (isPrimary) 28.dp else 20.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(CircleShape)
                .background(if (isPrimary) primaryColor else Color.Transparent)
                .hoverable(interactionSource)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                bitmap = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size)
            )
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun PlayerDetailPage(onClose: () -> Unit) {
        val song by localCurrentSong
        val isPlaying by localIsPlaying
        val tick by localTick
        val totalDuration by localTotalDuration
        val lyric = remember { MediaPlayer.lyric }
        val progress = if (totalDuration > 0) tick.toFloat() / totalDuration else 0f
        val currentLyricIndex = LyricLineProcessor.findCurrentIndex(lyric, tick.toLong())
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var lyricListHeight by remember { mutableStateOf(0) }

        LaunchedEffect(currentLyricIndex) {
            if (lyric.isNotEmpty() && currentLyricIndex >= 0 && lyricListHeight > 0) {
                scope.launch {
                    val itemHeight = 24
                    val centerOffset = (lyricListHeight / 2) - (itemHeight / 2)
                    listState.animateScrollToItem(
                        index = currentLyricIndex,
                        scrollOffset = -centerOffset
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .width(380.dp)
                .height(260.dp)
                .shadow(16.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor)
        ) {
            if (song != null) {
                AsyncImage(
                    model = song!!.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp)
                        .graphicsLayer(alpha = 0.3f),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(0.3f), Color.Black.copy(0.8f))
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.1f))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", color = Color.White, fontSize = 10.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            song?.name ?: "",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            song?.singer ?: "",
                            fontSize = 7.sp,
                            color = Color.White.copy(0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.size(24.dp))
                }

                Row(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val rotation by rememberInfiniteTransition().animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(20000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .shadow(12.dp, CircleShape)
                                .clip(CircleShape)
                                .rotate(if (isPlaying) rotation else 0f)
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = song?.image,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(1.dp, Color.White.copy(0.2f), CircleShape)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coordinates ->
                                lyricListHeight = coordinates.size.height
                            },
                        state = listState,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 40.dp)
                    ) {
                        itemsIndexed(lyric) { index, line ->
                            val isCurrent = index == currentLyricIndex
                            val alpha by animateFloatAsState(
                                if (isCurrent) 1f else 0.4f,
                                tween(300)
                            )
                            val scale by animateFloatAsState(
                                if (isCurrent) 1.1f else 1f,
                                tween(300)
                            )

                            Text(
                                line.text.ifEmpty { "···" },
                                fontSize = if (isCurrent) 8.sp else 6.sp,
                                color = if (isCurrent) primaryColor else Color.White.copy(alpha),
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .graphicsLayer(scaleX = scale, scaleY = scale)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp)
                ) {
                    var isDragging by remember { mutableStateOf(false) }
                    var dragProgress by remember { mutableStateOf(0f) }
                    var sliderWidth by remember { mutableStateOf(0f) }
                    val localDensity = LocalDensity.current
                    val displayProgress = if (isDragging) dragProgress else progress

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .onGloballyPositioned { coordinates ->
                                sliderWidth = coordinates.size.width.toFloat()
                            }
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    if (sliderWidth > 0) {
                                        val newProgress = (offset.x / sliderWidth).coerceIn(0f, 1f)
                                        MediaPlayer.seekToPercent(newProgress)
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        isDragging = true
                                        if (sliderWidth > 0) {
                                            dragProgress = (offset.x / sliderWidth).coerceIn(0f, 1f)
                                        }
                                    },
                                    onDragEnd = {
                                        if (isDragging) {
                                            MediaPlayer.seekToPercent(dragProgress)
                                        }
                                        isDragging = false
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        if (sliderWidth > 0) {
                                            dragProgress = (change.position.x / sliderWidth).coerceIn(0f, 1f)
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(displayProgress)
                                    .background(primaryColor)
                            )
                        }

                        val thumbOffset = with(localDensity) {
                            ((sliderWidth - 10.dp.toPx()) * displayProgress).toDp()
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = thumbOffset)
                                .size(10.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            MediaPlayer.formatTime(tick.toLong()),
                            fontSize = 5.sp,
                            color = Color.White.copy(0.6f)
                        )
                        Text(
                            MediaPlayer.formatTime(totalDuration),
                            fontSize = 5.sp,
                            color = Color.White.copy(0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlayerControlButton(
                            icon = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/music_last.png")!!
                                .readAllBytes().decodeToImageBitmap(),
                            size = 16.dp,
                            onClick = { MediaPlayer.playPrevious() }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                                .clickable { MediaPlayer.toggle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                bitmap = if (isPlaying) {
                                    SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/pause.png")!!
                                        .readAllBytes().decodeToImageBitmap()
                                } else {
                                    SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/play_fill.png")!!
                                        .readAllBytes().decodeToImageBitmap()
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        PlayerControlButton(
                            icon = SuperSoft.javaClass.getResourceAsStream("/assets/supersoft/ui/icon/music_next.png")!!
                                .readAllBytes().decodeToImageBitmap(),
                            size = 16.dp,
                            onClick = { MediaPlayer.playNext() }
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text("🔈", fontSize = 8.sp, color = Color.White.copy(0.6f))
                        Spacer(modifier = Modifier.width(4.dp))
                        var volumeSliderWidth by remember { mutableStateOf(0f) }
                        val volume by localVolume
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(12.dp)
                                .onGloballyPositioned { volumeSliderWidth = it.size.width.toFloat() }
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        if (volumeSliderWidth > 0) {
                                            val newVolume = (offset.x / volumeSliderWidth).coerceIn(0f, 1f)
                                            MediaPlayer.setVolume(newVolume)
                                            localVolume.value = newVolume
                                        }
                                    }
                                },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color.White.copy(0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(volume)
                                        .background(primaryColor)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🔊", fontSize = 8.sp, color = Color.White.copy(0.6f))
                    }
                }
            }
        }
    }

    fun search(songName: String) {
        songs.clear()
        val data = NeteaseCloudApi.search(songName)
        if (data != null) {
            parseSearchResult(data)
        }
    }

    private fun parseSearchResult(data: String) {
        val root = Json.parseToJsonElement(data).jsonObject
        val songArray = root["result"]?.jsonObject?.get("songs")?.jsonArray ?: return

        songArray.forEach { item ->
            val obj = item.jsonObject
            val name = obj["name"]?.jsonPrimitive?.content ?: ""
            val id = obj["id"]?.jsonPrimitive?.content ?: "0"
            val singer = obj["ar"]?.jsonArray?.firstOrNull()?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
            val image = obj["al"]?.jsonObject?.get("picUrl")?.jsonPrimitive?.content ?: ""
            songs.add(Song(name = name, image = "$image?param=200y200", singer = singer, id = id.toLong()))
        }
    }
}
