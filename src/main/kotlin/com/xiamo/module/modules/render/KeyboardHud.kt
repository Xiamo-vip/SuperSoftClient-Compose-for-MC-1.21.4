package com.xiamo.module.modules.render

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.ComposeModule
import com.xiamo.setting.ColorSetting
import net.minecraft.client.MinecraftClient

object KeyboradHud : ComposeModule("KeyboardHud", "键盘可视化") {

    private val keySize = numberSetting("KeySize", "按键大小", 24.0, 16.0, 48.0, 1.0)
    private val keySpacing = numberSetting("KeySpacing", "按键间距", 2.0, 0.0, 8.0, 1.0)
    private val cornerRadius = numberSetting("CornerRadius", "圆角大小", 4.0, 0.0, 12.0, 1.0)
    private val fontSize = numberSetting("FontSize", "字体大小", 9.0, 6.0, 16.0, 1.0)
    private val borderWidth = numberSetting("BorderWidth", "边框宽度", 1.0, 0.0, 3.0, 0.5)

    private val showJump = booleanSetting("ShowJump", "显示跳跃键", true)
    private val showSneak = booleanSetting("ShowSneak", "显示潜行键", false)
    private val showSprint = booleanSetting("ShowSprint", "显示疾跑键", false)
    private val showMouse = booleanSetting("ShowMouse", "显示鼠标按键", false)

    private val styleMode = modeSetting("Style", "样式", "Modern", "Modern", "Classic", "Minimal", "Gradient")

    private val primaryColor = colorSetting("PrimaryColor", "主色调", colorToArgb(100, 180, 255))
    private val bgOpacity = numberSetting("BgOpacity", "背景透明度", 0.6, 0.0, 1.0, 0.05)

    val forwardKeyIsPressed = mutableStateOf(false)
    val leftKeyIsPressed = mutableStateOf(false)
    val rightKeyIsPressed = mutableStateOf(false)
    val backKeyIsPressed = mutableStateOf(false)
    val jumpKeyIsPressed = mutableStateOf(false)
    val sneakKeyIsPressed = mutableStateOf(false)
    val sprintKeyIsPressed = mutableStateOf(false)
    val attackKeyIsPressed = mutableStateOf(false)
    val useKeyIsPressed = mutableStateOf(false)

    private fun colorToArgb(r: Int, g: Int, b: Int, a: Int = 255): Int {
        return ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)
    }

    private fun colorSettingToColor(setting: ColorSetting): Color {
        return Color(
            red = setting.red / 255f,
            green = setting.green / 255f,
            blue = setting.blue / 255f,
            alpha = setting.alpha / 255f
        )
    }

    @Composable
    override fun renderCompose() {
        val density = LocalDensity.current
        val offsetX = mutableStateOf(with(density) { 10.dp.toPx() })
        val offsetY = mutableStateOf(with(density) { 10.dp.toPx() })

        val pColor = colorSettingToColor(primaryColor)
        val keySizeDp = keySize.value.dp
        val keySpacingDp = keySpacing.value.dp
        val cornerRadiusDp = cornerRadius.value.dp
        val fontSizeSp = fontSize.value.sp

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            com.xiamo.gui.hud.HudComponent(
                componentId = "keyboard",
                moduleName = "KeyboradHud",
                defaultX = offsetX.value,
                defaultY = offsetY.value,
                defaultScale = 1f
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(keySpacingDp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        KeyButton(
                            label = getKeyName { it.forwardKey },
                            isPressed = forwardKeyIsPressed.value,
                            size = keySizeDp,
                            cornerRadius = cornerRadiusDp,
                            fontSize = fontSizeSp,
                            pColor = pColor
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(keySpacingDp)) {
                        KeyButton(
                            label = getKeyName { it.leftKey },
                            isPressed = leftKeyIsPressed.value,
                            size = keySizeDp,
                            cornerRadius = cornerRadiusDp,
                            fontSize = fontSizeSp,
                            pColor = pColor
                        )
                        KeyButton(
                            label = getKeyName { it.backKey },
                            isPressed = backKeyIsPressed.value,
                            size = keySizeDp,
                            cornerRadius = cornerRadiusDp,
                            fontSize = fontSizeSp,
                            pColor = pColor
                        )
                        KeyButton(
                            label = getKeyName { it.rightKey },
                            isPressed = rightKeyIsPressed.value,
                            size = keySizeDp,
                            cornerRadius = cornerRadiusDp,
                            fontSize = fontSizeSp,
                            pColor = pColor
                        )
                    }

                    if (showJump.value || showSneak.value || showSprint.value) {
                        Row(horizontalArrangement = Arrangement.spacedBy(keySpacingDp)) {
                            if (showSneak.value) {
                                KeyButton(
                                    label = "Snk",
                                    isPressed = sneakKeyIsPressed.value,
                                    size = keySizeDp,
                                    cornerRadius = cornerRadiusDp,
                                    fontSize = fontSizeSp,
                                    pColor = pColor
                                )
                            }
                            if (showJump.value) {
                                val jumpWidth = when {
                                    showSneak.value && showSprint.value -> keySizeDp
                                    showSneak.value || showSprint.value -> keySizeDp * 1.5f + keySpacingDp * 0.5f
                                    else -> keySizeDp * 3f + keySpacingDp * 2f
                                }
                                KeyButton(
                                    label = "——",
                                    isPressed = jumpKeyIsPressed.value,
                                    size = keySizeDp,
                                    cornerRadius = cornerRadiusDp,
                                    fontSize = fontSizeSp,
                                    pColor = pColor,
                                    customWidth = jumpWidth
                                )
                            }
                            if (showSprint.value) {
                                KeyButton(
                                    label = "Spr",
                                    isPressed = sprintKeyIsPressed.value,
                                    size = keySizeDp,
                                    cornerRadius = cornerRadiusDp,
                                    fontSize = fontSizeSp,
                                    pColor = pColor
                                )
                            }
                        }
                    }

                    if (showMouse.value) {
                        Row(horizontalArrangement = Arrangement.spacedBy(keySpacingDp)) {
                            KeyButton(
                                label = "L",
                                isPressed = attackKeyIsPressed.value,
                                size = keySizeDp,
                                cornerRadius = cornerRadiusDp,
                                fontSize = fontSizeSp,
                                pColor = pColor,
                                customWidth = keySizeDp * 1.4f
                            )
                            KeyButton(
                                label = "R",
                                isPressed = useKeyIsPressed.value,
                                size = keySizeDp,
                                cornerRadius = cornerRadiusDp,
                                fontSize = fontSizeSp,
                                pColor = pColor,
                                customWidth = keySizeDp * 1.4f
                            )
                        }
                    }
                }
            }
        }
        super.renderCompose()
    }

    @Composable
    private fun KeyButton(
        label: String,
        isPressed: Boolean,
        size: Dp,
        cornerRadius: Dp,
        fontSize: TextUnit,
        pColor: Color,
        customWidth: Dp? = null
    ) {
        val animDuration = 300

        val backgroundColor by animateColorAsState(
            targetValue = when {
                isPressed -> pColor
                else -> when (styleMode.value) {
                    "Minimal" -> Color.Transparent
                    else -> Color.Black.copy(alpha = bgOpacity.floatValue)
                }
            },
            animationSpec = tween(durationMillis = animDuration)
        )

        val textColor by animateColorAsState(
            targetValue = if (isPressed) Color.White else when (styleMode.value) {
                "Minimal" -> pColor
                else -> Color.White.copy(alpha = 0.85f)
            },
            animationSpec = tween(durationMillis = animDuration)
        )

        val pressOffset by animateDpAsState(
            targetValue = if (isPressed) 1.dp else 0.dp,
            animationSpec = tween(durationMillis = animDuration)
        )

        val shape = RoundedCornerShape(cornerRadius)
        val buttonWidth = customWidth ?: size

        val baseModifier = Modifier
            .width(buttonWidth)
            .height(size)
            .offset(y = pressOffset)

        val styledModifier = when (styleMode.value) {
            "Modern" -> {
                baseModifier
                    .shadow(if (isPressed) 0.dp else 2.dp, shape)
                    .clip(shape)
                    .background(
                        if (isPressed) {
                            Brush.verticalGradient(listOf(pColor, pColor.copy(alpha = 0.85f)))
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = bgOpacity.floatValue),
                                    Color.Black.copy(alpha = (bgOpacity.floatValue * 0.7f).coerceIn(0f, 1f))
                                )
                            )
                        },
                        shape
                    )
                    .border(borderWidth.floatValue.dp, pColor.copy(alpha = if (isPressed) 0.6f else 0.25f), shape)
            }
            "Classic" -> {
                baseModifier
                    .clip(shape)
                    .background(backgroundColor, shape)
                    .border(
                        borderWidth.floatValue.dp,
                        if (isPressed) Color.White.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.4f),
                        shape
                    )
            }
            "Minimal" -> {
                baseModifier
                    .clip(shape)
                    .background(backgroundColor, shape)
                    .border(
                        (if (isPressed) borderWidth.floatValue + 0.5f else borderWidth.floatValue).dp,
                        pColor,
                        shape
                    )
            }
            "Gradient" -> {
                baseModifier
                    .shadow(if (isPressed) 0.dp else 2.dp, shape)
                    .clip(shape)
                    .background(
                        if (isPressed) {
                            Brush.linearGradient(
                                listOf(pColor, pColor.copy(red = (pColor.red + 0.15f).coerceAtMost(1f)))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color.DarkGray.copy(alpha = bgOpacity.floatValue),
                                    Color.Black.copy(alpha = bgOpacity.floatValue)
                                )
                            )
                        },
                        shape
                    )
                    .border(borderWidth.floatValue.dp, pColor.copy(alpha = if (isPressed) 0.7f else 0.3f), shape)
            }
            else -> baseModifier.background(backgroundColor, shape)
        }

        Box(
            modifier = styledModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Normal,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            )
        }
    }

    private fun getKeyName(keySelector: (net.minecraft.client.option.GameOptions) -> net.minecraft.client.option.KeyBinding): String {
        val mc = MinecraftClient.getInstance()
        val key = keySelector(mc.options)
        val name = key.boundKeyLocalizedText.string
        return when {
            name.length > 3 -> name.take(2)
            else -> name
        }
    }

    override fun onKey(keyCode: Int, keyState: Int, scanCode: Int) {
        val mc = MinecraftClient.getInstance()
        val isPressed = keyState == 1 || keyState == 2
        val isReleased = keyState == 0

        when (keyCode) {
            mc.options.forwardKey.defaultKey.code -> {
                if (isPressed) forwardKeyIsPressed.value = true
                else if (isReleased) forwardKeyIsPressed.value = false
            }
            mc.options.backKey.defaultKey.code -> {
                if (isPressed) backKeyIsPressed.value = true
                else if (isReleased) backKeyIsPressed.value = false
            }
            mc.options.leftKey.defaultKey.code -> {
                if (isPressed) leftKeyIsPressed.value = true
                else if (isReleased) leftKeyIsPressed.value = false
            }
            mc.options.rightKey.defaultKey.code -> {
                if (isPressed) rightKeyIsPressed.value = true
                else if (isReleased) rightKeyIsPressed.value = false
            }
            mc.options.jumpKey.defaultKey.code -> {
                if (isPressed) jumpKeyIsPressed.value = true
                else if (isReleased) jumpKeyIsPressed.value = false
            }
            mc.options.sneakKey.defaultKey.code -> {
                if (isPressed) sneakKeyIsPressed.value = true
                else if (isReleased) sneakKeyIsPressed.value = false
            }
            mc.options.sprintKey.defaultKey.code -> {
                if (isPressed) sprintKeyIsPressed.value = true
                else if (isReleased) sprintKeyIsPressed.value = false
            }
        }
    }

    fun onMouseButton(button: Int, action: Int) {
        val isPressed = action == 1
        when (button) {
            0 -> attackKeyIsPressed.value = isPressed
            1 -> useKeyIsPressed.value = isPressed
        }
    }
}
