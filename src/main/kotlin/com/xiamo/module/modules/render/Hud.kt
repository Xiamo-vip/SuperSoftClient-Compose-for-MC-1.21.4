package com.xiamo.module.modules.render

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiamo.module.ComposeModule
import com.xiamo.module.ModuleManager
import com.xiamo.module.modules.combat.KillAura
import com.xiamo.setting.ModeSetting
import com.xiamo.setting.StringSetting
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.setValue
import coil3.compose.AsyncImage

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
                    if (KillAura.targetBarSetting.value) {
                        val target = KillAura.targetObject.value
                        if (target != null) {
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

                                val targetHead = "https://mc-heads.net/avatar/$targetName/64"

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
                                        .padding(start = 50.dp)
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
                                        AsyncImage(model = targetHead, contentDescription = null, modifier = Modifier.fillMaxSize())
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




