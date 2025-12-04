package com.xiamo.module.modules.render

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.contextmenu.modifier.filterTextContextMenuComponents
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.ComposeModule
import com.xiamo.module.ModuleManager
import com.xiamo.module.modules.combat.KillAura
import com.xiamo.setting.AbstractSetting
import com.xiamo.setting.BooleanSetting
import com.xiamo.setting.ModeSetting
import com.xiamo.setting.StringSetting
import com.xiamo.utils.config.ConfigManager
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity

import org.jetbrains.skia.paragraph.Shadow

object Hud : ComposeModule("Hud","界面") {
    var title = StringSetting("Title","HUD标题","SuperSoft")

    init {
        this.enabled = true
        this.settings.add(title)
    }

    @Composable
    override fun renderCompose() {
        Box(Modifier.fillMaxSize()){
            Column(modifier = Modifier.padding(start = 5.dp)) {
                Text(title.value, fontSize = 30.sp, color = Color.White, style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(
                    Color.Black, offset = Offset(1f,1f), blurRadius = 5f
                )))
                Text("\uD83C\uDFC4", fontSize = 10.sp,color = Color.White)
            }

            Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxSize()) {
                LazyColumn(horizontalAlignment = Alignment.End, modifier = Modifier.width(200.dp)){
                    ModuleManager.modules.filter { it.enabled }.sortedBy { it.name.length }.forEach {module ->
                       item {
                           val args = module.settings.filterIsInstance(ModeSetting::class.java).firstOrNull()?.value
                           Text(
                               text = module.name + if (args == null) "" else " | ${args}",
                               fontSize = 8.sp,
                               color = Color.White,
                               modifier = Modifier
                                   .background(Color(255f, 255f, 255f, 0.6f), RoundedCornerShape(2.dp))
                                   .padding(horizontal = 4.dp, vertical = 2.dp)
                                   .animateContentSize()
                                   .animateItem()
                               ,
                               textAlign = TextAlign.Right,
                               style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(Color.Black, offset = Offset(1f,1f), blurRadius = 5f))
                           )
                       }

                    }
                }



//                Column(horizontalAlignment = Alignment.End) {
//                    ModuleManager.modules.forEach { module ->
//                        key(module.name) {
//                            AnimatedVisibility(
//                                visible = module.enabled,
//                                enter =slideInHorizontally(initialOffsetX = {it}) + fadeIn(),
//                                exit =slideOutHorizontally(targetOffsetX = {it})+fadeOut()
//                            ) {
//                                Text(
//                                    text = module.name,
//                                    fontSize = 8.sp,
//                                    color = Color.White,
//                                    modifier = Modifier
//                                        .background(Color(255f, 255f, 255f, 0.6f), RoundedCornerShape(2.dp))
//                                        .shadow(5.dp)
//                                        .padding(horizontal = 4.dp, vertical = 2.dp)
//                                        .animateContentSize()
//                                    ,
//                                    textAlign = TextAlign.Right
//                                )
//                            }
//                        }
//                    }
//
//
//                }


            }


            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart){
                AnimatedVisibility(visible = KillAura.isAttacking.value,enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut() ) {
                    var positon : Offset? = null
                    if (KillAura.targetBarSetting.value) {
                        Row(modifier = Modifier
                            .padding(start = 50.dp)
                            .width(150.dp)
                            .height(60.dp)
                            .background(Color.Black.copy(0.5f),RoundedCornerShape(10.dp))
                            .onGloballyPositioned{
                                layoutCoordinates ->
                                    positon = layoutCoordinates.positionInParent()
                            }
                        )
                        {
                            KillAura.targetObject.
                        }
                    }
                }


            }

        }
    }
}