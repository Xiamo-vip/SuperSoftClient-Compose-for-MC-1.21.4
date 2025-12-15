package com.xiamo.module.modules.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.xiamo.module.ComposeModule
import net.minecraft.client.MinecraftClient
import net.minecraft.command.argument.EntityArgumentType.entity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.tan

object NameTags : ComposeModule("NameTags", "NameTags") {
    val distanceSetting = numberSetting("Distance", "Distance", 100.0, 1.0, 250.0, 1.0)
    val enemyEntity = booleanSetting("Enemy","Render Mob",true)
    val playerEntity = booleanSetting("Player","Render Player",true)
    val passiveEntity = booleanSetting("Passive","Render Entity",true)
    val neutralEntity = booleanSetting("Neutral","Render Entity",true)
    val fontSize = numberSetting("FontSize", "FontSize", 10.0, 1.0, 30.0, 1.0)
    val backgroundColor = colorSetting("BackgroundColor","BackgroundColor",Color.Black.copy(0.75f).toArgb())
    val textColor = colorSetting("TextColor","TextColor",Color.White.toArgb())
    @Composable
    override fun renderCompose() {
        val textMeasurer: TextMeasurer = rememberTextMeasurer()
        val mc = MinecraftClient.getInstance()

        val frameNanos by produceState(0L) {
            while (true) {
                withFrameNanos { value = it }
            }
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                frameNanos //强行重新绘制，不然这个bCompose不会每帧绘制
                val tickDelta = mc.renderTickCounter.getTickDelta(true)
                val world = mc.world ?: return@Canvas
                val player = mc.player ?: return@Canvas
                world.entities
                    .filterIsInstance<LivingEntity>()
                    .filter { it != player }
                    .filter { it.squaredDistanceTo(player) <= distanceSetting.value * distanceSetting.value }
                    .forEach { entity ->
                        drawTag(textMeasurer,entity,categorize(entity ),tickDelta)

                    }
            }
        }

        super.renderCompose()
    }
    fun Color.invert(): Color {
        return Color(
            red = 1f - this.red,
            green = 1f - this.green,
            blue = 1f - this.blue,
            alpha = this.alpha
        )
    }

    private fun worldToScreen(worldPos: Vec3d, tickDelta: Float): Offset? { //可能能用?
        val mc = MinecraftClient.getInstance()
        val camera = mc.gameRenderer.camera
        val window = mc.window

        val camPos = camera.pos
        val delta = Vector3f(
            (worldPos.x - camPos.x).toFloat(),
            (worldPos.y - camPos.y).toFloat(),
            (worldPos.z - camPos.z).toFloat()
        )

        val cameraRotation = Quaternionf(camera.rotation).conjugate()
        delta.rotate(cameraRotation)

        if (delta.z > 0) {
            return null
        }

        val fov = Math.toRadians(mc.gameRenderer.getFov(camera, tickDelta, true).toDouble()).toFloat()
        val aspectRatio = window.width.toFloat() / window.height.toFloat()
        val halfHeight = tan(fov / 2.0f)
        val halfWidth = halfHeight * aspectRatio
        val ndcX = delta.x / -delta.z / halfWidth
        val ndcY = -delta.y / -delta.z / halfHeight
        if (ndcX < -1f || ndcX > 1f || ndcY < -1f || ndcY > 1f) {
            return null
        }
        val screenX = (ndcX + 1.0f) / 2.0f * window.width
        val screenY = (ndcY + 1.0f) / 2.0f * window.height

        return Offset(screenX, screenY)
    }


    private fun DrawScope.drawTag(textMeasurer: TextMeasurer, entity: LivingEntity,category: EntityCategory,tickDelta: Float) {
        if (shouldRender(category)){
            val textStyle = TextStyle(
                fontSize = fontSize.value.sp,
                color = Color(textColor.value),
                shadow = Shadow(
                    Color(textColor.value).invert(),
                    offset = Offset(2f, 2f)
                )
            )
            val entityPos = Vec3d(
                entity.prevX + (entity.x - entity.prevX) * tickDelta,
                entity.prevY + (entity.y - entity.prevY) * tickDelta + entity.height + 0.65,
                entity.prevZ + (entity.z - entity.prevZ) * tickDelta
            )
            val screenPos = worldToScreen(entityPos, tickDelta) ?: return
            val text = entity.name.string
            val textLayout = textMeasurer.measure(text, style = textStyle)
            val health = "HP：" + entity.health.toString()
            val healthTextLayout = textMeasurer.measure(health, style = textStyle.copy(fontSize = 5.sp))
            val padding = 10f
            val rectWidth = textLayout.size.width +healthTextLayout.size.width +  padding * 2
            val rectHeight = textLayout.size.height + healthTextLayout.size.height + padding * 2 + 10
            drawRoundRect(Color(backgroundColor.value)
                , topLeft = Offset(screenPos.x - rectWidth / 2,
                    screenPos.y - rectHeight /2),
                size = Size(rectWidth.toFloat(), rectHeight.toFloat()),
                cornerRadius = CornerRadius(15.0f),
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    screenPos.x - textLayout.size.width / 2f,
                    screenPos.y - textLayout.size.height / 2f
                )
            )

            drawText(
                textLayoutResult = healthTextLayout,
                topLeft = Offset(
                    screenPos.x - healthTextLayout.size.width / 2f,
                    screenPos.y - healthTextLayout.size.height / 2f + textLayout.size.height / 2 + healthTextLayout.size.height / 2
                )
            )
            drawRoundRect(color = Color.Black.copy(0.4f), topLeft = Offset(
                screenPos.x - rectWidth / 2,screenPos.y - rectHeight /2 + rectHeight + 5),
                size = Size(rectWidth, 10f), cornerRadius = CornerRadius(105.0f),
            )
            drawRoundRect(color = Color.Red, topLeft = Offset(
                screenPos.x - rectWidth / 2,screenPos.y - rectHeight /2 + rectHeight + 5),
                size = Size((entity.health / entity.maxHealth) * rectWidth, 10f), cornerRadius = CornerRadius(105.0f),
            )



        }


    }


    private fun categorize(entity: LivingEntity) : EntityCategory {
        return when(entity){
            is PlayerEntity -> EntityCategory.Player
            is HostileEntity -> EntityCategory.Enemy
            is PassiveEntity -> EntityCategory.Passive
            else -> EntityCategory.NEUTRAL
        }
    }

    private fun shouldRender(category: EntityCategory) : Boolean {
        return when(category){
            EntityCategory.Player -> playerEntity.value
            EntityCategory.NEUTRAL -> neutralEntity.value
            EntityCategory.Passive -> passiveEntity.value
            EntityCategory.Enemy ->  enemyEntity.value
        }

    }


}
enum class EntityCategory {
    Player,
    Enemy,
    Passive,
    NEUTRAL
}
