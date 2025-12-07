package com.xiamo.module.modules.render

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.xiamo.module.Category
import com.xiamo.module.Module
import com.xiamo.utils.render.RenderUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper

object ESP : Module("ESP", "透视", Category.Render) {
    val ESPMode = modeSetting("ESP Mode", "绘制模式", "Outline", "Outline", "FillBox")
    val outlineColor = colorSetting("Color", "颜色", Color.Red.toArgb())
    val distance = numberSetting("Distance", "绘制距离", 80.0, 1.0, 150.0, 1.0)
    val expand = numberSetting("Expand", "扩大范围", 0.2, 0.0, 0.5, 0.05)
    val renderPlayers = booleanSetting("Players", "渲染玩家", true)
    val renderMobs = booleanSetting("Mobs", "渲染生物", true)

    override fun renderEntity(entity: Entity, matrix: MatrixStack, tickDelta: Float) {
        if (entity !is LivingEntity) return
        val mc = MinecraftClient.getInstance()
        if (mc.world == null || mc.player == null) return
        if (entity == mc.player) return
        if (entity.distanceTo(mc.player) > distance.value) return

        val shouldRender = when {
            entity is PlayerEntity && renderPlayers.value -> true
            entity is MobEntity && renderMobs.value -> true
            else -> false
        }
        if (!shouldRender) return

        val cameraX = mc.gameRenderer.camera.pos.x
        val cameraY = mc.gameRenderer.camera.pos.y
        val cameraZ = mc.gameRenderer.camera.pos.z

        val entityX = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderX, entity.x)
        val entityY = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderY, entity.y)
        val entityZ = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderZ, entity.z)

        val offsetX = entityX - cameraX
        val offsetY = entityY - cameraY
        val offsetZ = entityZ - cameraZ

        val box = entity.boundingBox
            .offset(-entity.x, -entity.y, -entity.z)
            .offset(offsetX, offsetY, offsetZ)
            .expand(expand.value)


        val r = outlineColor.red.toFloat() / 255f
        val g = outlineColor.green.toFloat() / 255f
        val b = outlineColor.blue.toFloat() / 255f
        val a = outlineColor.alpha.toFloat() / 255f

        when (ESPMode.value) {
            "Outline" -> RenderUtils.drawBox3D(matrix, box, r, g, b, a)
            "FillBox" -> RenderUtils.drawFillBox3D(matrix, box, r, g, b, a)
        }
    }
}
