package com.xiamo.gui.clickGui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.Category
import com.xiamo.module.Module
import com.xiamo.module.ModuleManager
import com.xiamo.setting.*
import net.minecraft.client.MinecraftClient
import kotlin.math.roundToInt

object ClickGuiColors {
    var backgroundColor by mutableStateOf(Color(26, 26, 26))
    var titleBgColor by mutableStateOf(Color(0, 0, 0))
    var moduleHoverBgColor by mutableStateOf(Color(105, 180, 255))
    var moduleEnabledBgColor by mutableStateOf(Color(108, 53, 222))
    var settingBgColor by mutableStateOf(Color(40, 40, 40))
    var settingHoverColor by mutableStateOf(Color(60, 60, 60))
    var accentColor by mutableStateOf(Color(108, 53, 222))
    var textColor by mutableStateOf(Color.White)
    var textSecondaryColor by mutableStateOf(Color.White.copy(alpha = 0.9f))
    var dropdownBgColor by mutableStateOf(Color(30, 30, 30))
    var sliderTrackColor by mutableStateOf(Color.Gray.copy(alpha = 0.3f))
    var textFieldBgColor by mutableStateOf(Color(50, 20, 40))
}

class ClickGuiWindow(val x: Int, val y: Int, val category: Category, val width: Float, val height: Float) {

    var windowX by mutableStateOf(x.toFloat())
    var windowY by mutableStateOf(y.toFloat())
    var isDragging = false
    var isHover = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    val categoryTitleFont = 10.sp
    val categoryContentFont = 7.sp
    val settingFont = 6.sp
    val radius: Dp = 2.dp

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun renderCompose() {
        val interfaceSource = remember { MutableInteractionSource() }
        isHover = interfaceSource.collectIsHoveredAsState().value

        Column(
            modifier = Modifier
                .offset { IntOffset(windowX.roundToInt(), windowY.roundToInt()) }
                .width(width.dp)
                .background(ClickGuiColors.backgroundColor, RoundedCornerShape(radius)),
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height.dp)
                    .shadow(5.dp, RoundedCornerShape(radius))
                    .hoverable(interfaceSource),
                colors = CardDefaults.cardColors(containerColor = ClickGuiColors.titleBgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                shape = RoundedCornerShape(radius)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = category.name,
                    fontSize = categoryTitleFont,
                    fontWeight = FontWeight.Normal,
                    color = ClickGuiColors.textColor
                )
            }

            ModuleManager.modules.filter { it.category.name == category.name }.forEach { module ->
                ModuleItem(module)
            }
        }
    }


    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun ModuleItem(module: Module) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val fontWeightAnimation = animateFloatAsState(if (module.enabled) 600f else 300f,

        )
        val moduleBgColor by animateColorAsState(
            targetValue = when {
                isHovered -> ClickGuiColors.moduleHoverBgColor
                module.enabled -> ClickGuiColors.moduleEnabledBgColor
                else -> ClickGuiColors.titleBgColor
            },
            animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        )

        val arrowRotation by animateFloatAsState(
            targetValue = if (module.settingsExpanded) 90f else 0f,
            animationSpec = tween(durationMillis = 200)
        )

        val hasSettings = module.settings.isNotEmpty()

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(moduleBgColor)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource()},
                        indication = ripple(true, 200.dp, Color.DarkGray),
                        onClick = { module.toggle() },
                        onLongClick = {
                            if (hasSettings) {
                                module.settingsExpanded = !module.settingsExpanded
                            }
                        },
                    )
                    .hoverable(interactionSource)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = module.name,
                    fontSize = categoryContentFont,
                    fontWeight = FontWeight(fontWeightAnimation.value.toInt()),
                    color = ClickGuiColors.textColor,
                    modifier = Modifier.weight(1f)
                )

                if (hasSettings) {
                    Text(
                        text = "▶",
                        fontSize = 6.sp,
                        color = ClickGuiColors.textColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .rotate(arrowRotation)
                            .clickable { module.settingsExpanded = !module.settingsExpanded }
                    )
                }
            }

            AnimatedVisibility(
                visible = module.settingsExpanded && hasSettings,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ClickGuiColors.settingBgColor)
                        .padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                        .animateContentSize()
                        .animateEnterExit()
                ) {
                    module.settings.filter { it.isVisible() }.forEach { setting ->
                        item {
                            SettingItem(setting, module)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SettingItem(setting: AbstractSetting<*>, module: Module) {
        when (setting) {
            is BooleanSetting -> BooleanSettingItem(setting, module)
            is NumberSetting -> NumberSettingItem(setting, module)
            is ModeSetting -> ModeSettingItem(setting, module)
            is StringSetting -> StringSettingItem(setting, module)
            is ColorSetting -> ColorSettingItem(setting, module)
            is KeyBindSetting -> KeyBindSettingItem(setting, module)
        }
    }

    @Composable
    private fun BooleanSettingItem(setting: BooleanSetting, module: Module) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        val bgAlpha by animateFloatAsState(
            targetValue = if (isHovered) 1f else 0f,
            animationSpec = tween(durationMillis = 150)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
                .background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
                .hoverable(interactionSource)
                .clickable {
                    setting.value = !setting.value
                    module.onSettingChanged(setting)
                }
                .padding(vertical = 2.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = setting.name,
                fontSize = settingFont,
                color = ClickGuiColors.textSecondaryColor
            )

            CustomSwitch(
                checked = setting.value,
                onCheckedChange = {
                    setting.value = it
                    module.onSettingChanged(setting)
                },
                width = 24.dp,
                height = 12.dp,
                checkedTrackColor = ClickGuiColors.accentColor,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.4f)
            )
        }
    }

    @Composable
    private fun NumberSettingItem(setting: NumberSetting, module: Module) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        val bgAlpha by animateFloatAsState(
            targetValue = if (isHovered) 1f else 0f,
            animationSpec = tween(durationMillis = 150)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
                .hoverable(interactionSource)
                .padding(vertical = 2.dp, horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = setting.name,
                    fontSize = settingFont,
                    color = ClickGuiColors.textSecondaryColor
                )
                Text(
                    text = "%.2f".format(setting.value),
                    fontSize = settingFont,
                    color = ClickGuiColors.accentColor
                )
            }

            CustomSlider(
                value = setting.value.toFloat(),
                onValueChange = { setting.value = it.toDouble() },
                onValueChangeFinished = { module.onSettingChanged(setting) },
                valueRange = setting.min.toFloat()..setting.max.toFloat(),
                modifier = Modifier.fillMaxWidth().height(12.dp),
                trackHeight = 3.dp,
                thumbSize = 8.dp,
                activeColor = ClickGuiColors.accentColor,
                inactiveColor = ClickGuiColors.sliderTrackColor
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ModeSettingItem(setting: ModeSetting, module: Module) {
        var expanded by remember { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        val bgAlpha by animateFloatAsState(
            targetValue = if (isHovered || expanded) 1f else 0f,
            animationSpec = tween(durationMillis = 150)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
                    .hoverable(interactionSource)
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = setting.name,
                    fontSize = settingFont,
                    color = ClickGuiColors.textSecondaryColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = setting.value,
                        fontSize = settingFont,
                        color = ClickGuiColors.accentColor
                    )
                    Text(
                        text = if (expanded) " ▲" else " ▼",
                        fontSize = 5.sp,
                        color = ClickGuiColors.textColor.copy(alpha = 0.6f)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ClickGuiColors.dropdownBgColor)
                        .padding(2.dp)
                ) {
                    setting.modes.forEach { mode ->
                        val modeInteractionSource = remember { MutableInteractionSource() }
                        val modeHovered by modeInteractionSource.collectIsHoveredAsState()
                        val isSelected = mode == setting.value

                        val modeAlpha by animateFloatAsState(
                            targetValue = when {
                                isSelected -> 0.6f
                                modeHovered -> 1f
                                else -> 0f
                            },
                            animationSpec = tween(durationMillis = 150)
                        )

                        val modeBgColor = if (isSelected) ClickGuiColors.accentColor else ClickGuiColors.settingHoverColor

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(modeBgColor.copy(alpha = modeAlpha))
                                .hoverable(modeInteractionSource)
                                .clickable {
                                    setting.value = mode
                                    module.onSettingChanged(setting)
                                    expanded = false
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mode,
                                fontSize = settingFont,
                                color = if (isSelected) ClickGuiColors.textColor else ClickGuiColors.textColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun StringSettingItem(setting: StringSetting, module: Module) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        val bgAlpha by animateFloatAsState(
            targetValue = if (isHovered) 1f else 0f,
            animationSpec = tween(durationMillis = 150)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
                .hoverable(interactionSource)
                .padding(vertical = 2.dp, horizontal = 4.dp)
        ) {
            Text(
                text = setting.name,
                fontSize = settingFont,
                color = ClickGuiColors.textSecondaryColor
            )
            StringTextField(
                value = setting.value,
                onValueChange = {
                    setting.value = it
                    module.onSettingChanged(setting)
                }
            )
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun ColorSettingItem(setting: ColorSetting, module: Module) {
        var expanded by remember { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        val bgAlpha by animateFloatAsState(
            targetValue = if (isHovered || expanded) 1f else 0f,
            animationSpec = tween(durationMillis = 150)
        )

        var red by remember { mutableStateOf(setting.red.toFloat()) }
        var green by remember { mutableStateOf(setting.green.toFloat()) }
        var blue by remember { mutableStateOf(setting.blue.toFloat()) }
        var alpha by remember { mutableStateOf(setting.alpha.toFloat()) }

        LaunchedEffect(setting.value) {
            red = setting.red.toFloat()
            green = setting.green.toFloat()
            blue = setting.blue.toFloat()
            alpha = setting.alpha.toFloat()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
                    .hoverable(interactionSource)
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = setting.name,
                    fontSize = settingFont,
                    color = ClickGuiColors.textSecondaryColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp, 12.dp)
                            .background(
                                Color(red / 255f, green / 255f, blue / 255f, alpha / 255f),
                                RoundedCornerShape(2.dp)
                            )
                            .border(1.dp, ClickGuiColors.textColor.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = if (expanded) " ▲" else " ▼",
                        fontSize = 5.sp,
                        color = ClickGuiColors.textColor.copy(alpha = 0.6f)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ClickGuiColors.dropdownBgColor)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Red, Color.Yellow, Color.Green,
                                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                    )
                                )
                            )
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val hue = (offset.x / size.width) * 360f
                                    val rgb = hueToRgb(hue)
                                    red = rgb.first
                                    green = rgb.second
                                    blue = rgb.third
                                    setting.setARGB(alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
                                    module.onSettingChanged(setting)
                                }
                            }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { change, _ ->
                                    change.consume()
                                    val hue = (change.position.x.coerceIn(0f, size.width.toFloat()) / size.width) * 360f
                                    val rgb = hueToRgb(hue)
                                    red = rgb.first
                                    green = rgb.second
                                    blue = rgb.third
                                    setting.setARGB(alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
                                    module.onSettingChanged(setting)
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    ColorSliderRow("R", red, Color.Red) {
                        red = it
                        setting.setARGB(alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
                        module.onSettingChanged(setting)
                    }

                    ColorSliderRow("G", green, Color.Green) {
                        green = it
                        setting.setARGB(alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
                        module.onSettingChanged(setting)
                    }

                    ColorSliderRow("B", blue, Color.Blue) {
                        blue = it
                        setting.setARGB(alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
                        module.onSettingChanged(setting)
                    }

                    ColorSliderRow("A", alpha, Color.White) {
                        alpha = it
                        setting.setARGB(alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
                        module.onSettingChanged(setting)
                    }
                }
            }
        }
    }

    @Composable
    private fun ColorSliderRow(label: String, value: Float, color: Color, onValueChange: (Float) -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 5.sp,
                color = color,
                modifier = Modifier.width(10.dp)
            )
            CustomSlider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = {},
                valueRange = 0f..255f,
                modifier = Modifier.weight(1f).height(10.dp),
                trackHeight = 2.dp,
                thumbSize = 6.dp,
                activeColor = color,
                inactiveColor = Color.Gray.copy(alpha = 0.3f)
            )
            Text(
                text = value.toInt().toString(),
                fontSize = 5.sp,
                color = ClickGuiColors.textColor,
                modifier = Modifier.width(20.dp),
                textAlign = TextAlign.End
            )
        }
    }

    private fun hueToRgb(hue: Float): Triple<Float, Float, Float> {
        val c = 1f
        val x = c * (1 - kotlin.math.abs((hue / 60f) % 2 - 1))
        val (r, g, b) = when {
            hue < 60 -> Triple(c, x, 0f)
            hue < 120 -> Triple(x, c, 0f)
            hue < 180 -> Triple(0f, c, x)
            hue < 240 -> Triple(0f, x, c)
            hue < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Triple(r * 255, g * 255, b * 255)
    }

    @Composable
    private fun KeyBindSettingItem(setting: KeyBindSetting, module: Module) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        val bgAlpha by animateFloatAsState(
            targetValue = if (isHovered) 1f else 0f,
            animationSpec = tween(durationMillis = 150)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
                .hoverable(interactionSource)
                .clickable { setting.isListening = !setting.isListening }
                .padding(vertical = 2.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = setting.name,
                fontSize = settingFont,
                color = ClickGuiColors.textSecondaryColor
            )
            Text(
                text = if (setting.isListening) "..." else getKeyName(setting.value),
                fontSize = settingFont,
                color = if (setting.isListening) Color.Yellow else ClickGuiColors.accentColor
            )
        }
    }

    private fun getKeyName(keyCode: Int): String {
        return when (keyCode) {
            -1 -> "None"
            else -> org.lwjgl.glfw.GLFW.glfwGetKeyName(keyCode, 0) ?: "Key$keyCode"
        }
    }

    fun onDragged(mouseX: Int, mouseY: Int) {
        if (!isDragging) return

        val scale = MinecraftClient.getInstance().window.scaleFactor
        val physX = mouseX * scale
        val physY = mouseY * scale
        windowX = (physX - dragOffsetX).toFloat()
        windowY = (physY - dragOffsetY).toFloat()
    }

    fun onClicked(mouseX: Int, mouseY: Int) {
        val scale = MinecraftClient.getInstance().window.scaleFactor
        val physX = mouseX * scale
        val physY = mouseY * scale

        val scaledWidth = width * scale
        val scaledHeight = height * scale

        if (physX >= windowX && physX <= windowX + scaledWidth &&
            physY >= windowY && physY <= windowY + scaledHeight
        ) {
            isDragging = true
            dragOffsetX = (physX - windowX).toFloat()
            dragOffsetY = (physY - windowY).toFloat()
        }
    }

    fun onMouseReleased() {
        isDragging = false
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StringTextField(
    value: String,
    onValueChange: (String) -> Unit,
    width: Int = 80,
    height: Int = 15,
) {
    var isHovered by remember { mutableStateOf(false) }

    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered) 1f else 0.3f,
        animationSpec = tween(150)
    )

    Box(
        modifier = Modifier
            .width(width.dp)
            .height(height.dp)
            .background(ClickGuiColors.textFieldBgColor, RoundedCornerShape(4.dp))
            .border(1.dp, ClickGuiColors.textColor.copy(alpha = borderAlpha), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = ClickGuiColors.textColor,
                fontSize = 8.sp
            ),
            cursorBrush = SolidColor(ClickGuiColors.textColor),
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .fillMaxWidth()
                .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                .onFocusChanged { isHovered = it.isFocused }
        )

        if (value.isEmpty()) {
            Text(
                "value",
                color = ClickGuiColors.textColor.copy(alpha = 0.4f),
                fontSize = 8.sp,
                modifier = Modifier.padding(horizontal = 6.dp).offset(y = -2.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 36.dp,
    height: Dp = 20.dp,
    checkedTrackColor: Color = Color(0xFF6200EE),
    uncheckedTrackColor: Color = Color.Gray.copy(alpha = 0.5f),
    thumbColor: Color = Color.White
) {
    val thumbPadding = 2.dp
    val thumbSize = height - thumbPadding * 2

    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 150)
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) checkedTrackColor else uncheckedTrackColor,
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(thumbPadding)
                .offset {
                    val maxOffset = (width - thumbSize - thumbPadding * 2).toPx()
                    IntOffset((thumbOffset * maxOffset).toInt(), 0)
                }
                .size(thumbSize)
                .clip(RoundedCornerShape(50))
                .background(thumbColor)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 4.dp,
    thumbSize: Dp = 10.dp,
    activeColor: Color = Color.Blue,
    inactiveColor: Color = Color.Gray
) {
    var sliderWidth by remember { mutableStateOf(0f) }
    var isHovered by remember { mutableStateOf(false) }
    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

    val thumbScale by animateFloatAsState(
        targetValue = if (isHovered) 1.3f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val newFraction = (offset.x / sliderWidth).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newFraction * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue.coerceIn(valueRange.start, valueRange.endInclusive))
                    onValueChangeFinished()
                }
            }
            .pointerInput(valueRange) {
                detectHorizontalDragGestures(
                    onDragEnd = { onValueChangeFinished() },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val x = change.position.x.coerceIn(0f, sliderWidth)
                        val newFraction = x / sliderWidth
                        val newValue = valueRange.start + newFraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue.coerceIn(valueRange.start, valueRange.endInclusive))
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .background(inactiveColor, RoundedCornerShape(trackHeight / 2))
                .onGloballyPositioned { sliderWidth = it.size.width.toFloat() }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(trackHeight)
                .background(activeColor, RoundedCornerShape(trackHeight / 2))
        )

        Box(
            modifier = Modifier
                .offset { IntOffset((fraction * sliderWidth).toInt() - (thumbSize.toPx() * thumbScale / 2).toInt(), 0) }
                .size(thumbSize * thumbScale)
                .background(activeColor, RoundedCornerShape(50))
        )
    }
}
