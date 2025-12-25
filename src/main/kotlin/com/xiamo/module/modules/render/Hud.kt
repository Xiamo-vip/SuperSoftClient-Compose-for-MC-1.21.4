package com.xiamo.module.modules.render

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xiamo.gui.hud.HudComponent
import com.xiamo.gui.hud.HudEditorManager
import com.xiamo.module.ComposeModule
import com.xiamo.module.ModuleManager
import com.xiamo.module.modules.combat.KillAura
import com.xiamo.setting.ModeSetting
import net.minecraft.client.MinecraftClient
import kotlin.math.sin

object Hud : ComposeModule("Hud", "界面") {
    var title = stringSetting("Title", "HUD标题", "SuperSoft")

    val fontSize = numberSetting("FontSize","字体大小",8.0,1.0,15.0)
    val scale = numberSetting("Scale","缩放",1.0,0.1,5.0)
    val colorMode = modeSetting("ColorMode", "颜色模式", "Rainbow", "Rainbow", "Static", "Fade", "Gradient", "Wave")


    val rainbowSpeed = numberSetting("RainbowSpeed", "彩虹速度", 3.0, 0.5, 10.0, 0.5)
    val rainbowSaturation = numberSetting("RainbowSaturation", "彩虹饱和度", 0.6, 0.1, 1.0, 0.05)
    val rainbowBrightness = numberSetting("RainbowBrightness", "彩虹亮度", 1.0, 0.3, 1.0, 0.05)
    val rainbowOffset = numberSetting("RainbowOffset", "彩虹偏移", 0.05, 0.01, 0.2, 0.01)


    val staticColor = colorSetting("StaticColor", "静态颜色", 0xFF6C63FF.toInt())


    val gradientStartColor = colorSetting("GradientStart", "渐变起始色", 0xFF6C63FF.toInt())
    val gradientEndColor = colorSetting("GradientEnd", "渐变结束色", 0xFFFF6B6B.toInt())


    val waveSpeed = numberSetting("WaveSpeed", "波浪速度", 2.0, 0.5, 5.0, 0.5)
    val waveColor = colorSetting("WaveColor", "波浪颜色", 0xFF00BFFF.toInt())


    val showBackground = booleanSetting("ShowBackground", "显示背景", true)
    val backgroundOpacity = numberSetting("BackgroundOpacity", "背景透明度", 0.6, 0.0, 1.0, 0.1)



    init {
        this.enabled = true
    }


    @Composable
    private fun getModuleColor(index: Int, totalModules: Int, hueOffset: Float, timeMs: Long): Color {
        return when (colorMode.value) {
            "Rainbow" -> {
                val currentHue = (hueOffset + index * rainbowOffset.floatValue) % 1f
                Color.hsv(
                    currentHue * 360f,
                    rainbowSaturation.floatValue,
                    rainbowBrightness.floatValue
                )
            }
            "Static" -> {
                Color(staticColor.value)
            }
            "Fade" -> {
                val alpha = (sin(timeMs / 1000.0 + index * 0.3) * 0.3 + 0.7).toFloat()
                Color.White.copy(alpha = alpha.coerceIn(0.4f, 1f))
            }
            "Gradient" -> {
                val fraction = if (totalModules > 1) index.toFloat() / (totalModules - 1) else 0f
                lerpColor(Color(gradientStartColor.value), Color(gradientEndColor.value), fraction)
            }
            "Wave" -> {
                val wave = (sin(timeMs / (500.0 / waveSpeed.value) + index * 0.5) * 0.3 + 0.7).toFloat()
                val baseColor = Color(waveColor.value)
                baseColor.copy(
                    red = (baseColor.red * wave).coerceIn(0f, 1f),
                    green = (baseColor.green * wave).coerceIn(0f, 1f),
                    blue = (baseColor.blue * wave).coerceIn(0f, 1f)
                )
            }
            else -> Color.White
        }
    }

    private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
        return Color(
            red = start.red + (end.red - start.red) * fraction,
            green = start.green + (end.green - start.green) * fraction,
            blue = start.blue + (end.blue - start.blue) * fraction,
            alpha = start.alpha + (end.alpha - start.alpha) * fraction
        )
    }

    @Composable
    override fun renderCompose() {
        val screenWidth = MinecraftClient.getInstance().window.width.toFloat()
        val screenHeight = MinecraftClient.getInstance().window.height.toFloat()
        val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
        val textMeasurer = rememberTextMeasurer()
        val hueOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween((3000 / rainbowSpeed.value).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "hue"
        )

        var timeMs by remember { mutableStateOf(0L) }
        LaunchedEffect(Unit) {
            while (true) {
                timeMs = System.currentTimeMillis()
                kotlinx.coroutines.delay(16)
            }
        }

        Box(Modifier.fillMaxSize()) {

            HudComponent(
                componentId = "hud_title",
                moduleName = "Hud",
                defaultX = 5f,
                defaultY = 5f
            ) {
                Column(modifier = Modifier.padding(5.dp)) {
                    Text(
                        title.value,
                        fontSize = 30.sp,
                        color = Color.White,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                Color.Black, offset = Offset(1f, 1f), blurRadius = 5f
                            )
                        )
                    )
                    Text("🏄", fontSize = 10.sp, color = Color.White)
                }
            }

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                LazyColumn(horizontalAlignment = Alignment.End, modifier = Modifier.width(200.dp).scale(scale.value.toFloat())) {
                    val enabledModules = ModuleManager.modules.filter { it.enabled }.sortedByDescending { module ->
                        val mode = module.settings.filterIsInstance<ModeSetting>().firstOrNull()?.value
                        val displayName = if (mode != null) "${module.name} | $mode" else module.name
                        textMeasurer.measure(displayName, style = TextStyle(textAlign = TextAlign.Right, fontSize = fontSize.value.toFloat().sp)).size.width
                    }

                    itemsIndexed(enabledModules) { index, module ->
                        val moduleColor = getModuleColor(index, enabledModules.size, hueOffset, timeMs)
                        val args = module.settings.filterIsInstance<ModeSetting>().firstOrNull()?.value

                        val backgroundModifier = if (showBackground.value) {
                            Modifier.background(
                                Color(0f, 0f, 0f, backgroundOpacity.floatValue),
                                RoundedCornerShape(2.dp)
                            )
                        } else {
                            Modifier
                        }

                        Text(
                            text = module.name + (if (args == null) "" else " | $args"),
                            fontSize = fontSize.value.toFloat().sp,
                            color = moduleColor,
                            modifier = backgroundModifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .animateContentSize()
                                .animateItem(placementSpec = spring(stiffness = Spring.StiffnessLow)),
                            textAlign = TextAlign.Right,
                            style = TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    Color.Black,
                                    offset = Offset(1f, 1f),
                                    blurRadius = 5f
                                )
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = KillAura.isAttacking.value || HudEditorManager.isEditMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                if (KillAura.targetBarSetting.value || HudEditorManager.isEditMode) {
                    val target = KillAura.targetObject.value
                    if (target != null && !HudEditorManager.isEditMode) {
                        HudComponent(
                            componentId = "target_hud",
                            moduleName = "Hud",
                            defaultX = screenWidth / 2 + 100f,
                            defaultY = screenHeight / 2
                        ) {
                            key(target.uuid) {
                                val targetName = target.name.string
                                var currentHealth by remember { mutableStateOf(target.health) }
                                var maxHealth by remember { mutableStateOf(target.maxHealth) }

                                LaunchedEffect(Unit) {
                                    while (true) {
                                        currentHealth = target.health
                                        maxHealth = target.maxHealth
                                        kotlinx.coroutines.delay(50)
                                    }
                                }

                                val healthPercent = (currentHealth / maxHealth).coerceIn(0f, 1f)
                                val animatedHealthPercent by animateFloatAsState(
                                    targetValue = healthPercent,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "health"
                                )
                                val targetHead = "https://minotar.net/avatar/$targetName/64"

                                val delayedHealthPercent = remember { mutableStateOf(healthPercent) }
                                LaunchedEffect(healthPercent) {
                                    kotlinx.coroutines.delay(100)
                                    delayedHealthPercent.value = healthPercent
                                }

                                val animatedDelayedHealth by animateFloatAsState(
                                    targetValue = delayedHealthPercent.value,
                                    animationSpec = tween(durationMillis = 500),
                                    label = "delayedHealth"
                                )

                                val healthColor by animateColorAsState(
                                    targetValue = when {
                                        healthPercent > 0.6f -> Color(0xFF4CAF50)
                                        healthPercent > 0.3f -> Color(0xFFFF9800)
                                        else -> Color(0xFFF44336)
                                    },
                                    animationSpec = tween(durationMillis = 300),
                                    label = "healthColor"
                                )

                                Row(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .background(Color.Black.copy(0.6f), RoundedCornerShape(10.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Gray.copy(0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = targetHead,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = targetName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            style = TextStyle(
                                                shadow = androidx.compose.ui.graphics.Shadow(
                                                    Color.Black, offset = Offset(1f, 1f), blurRadius = 3f
                                                )
                                            )
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .background(Color.Gray.copy(0.5f), RoundedCornerShape(4.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(animatedDelayedHealth)
                                                    .height(5.dp)
                                                    .background(Color(0xFFFF5252), RoundedCornerShape(4.dp))
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(animatedHealthPercent)
                                                    .height(5.dp)
                                                    .background(healthColor, RoundedCornerShape(4.dp))
                                            )
                                        }

                                        Text(
                                            text = "%.1f / %.1f".format(currentHealth, maxHealth),
                                            color = Color.White.copy(0.8f),
                                            fontSize = 7.sp
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