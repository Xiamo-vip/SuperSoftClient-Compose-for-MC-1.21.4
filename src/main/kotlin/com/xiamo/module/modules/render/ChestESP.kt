package com.xiamo.module.modules.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.xiamo.module.Category
import com.xiamo.module.ComposeModule
import com.xiamo.module.Module
import com.xiamo.utils.render.RenderUtils
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Box

object ChestESP : Module("ChestESP","箱子透视", Category.Render) {
    val color = colorSetting("Color","颜色", Color.Red.toArgb())
    override fun renderBlock(blockEntity: BlockEntity, matrix: MatrixStack, vertexConsumer: VertexConsumerProvider) {
        if (blockEntity.type == BlockEntityType.CHEST) {
            val r = color.red.toFloat() / 255f
            val g = color.green.toFloat() / 255f
            val b = color.blue.toFloat() / 255f
            val a = color.alpha.toFloat() / 255f
            RenderUtils.drawFillBox3D(matrix, Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), r, g, b, a)
        }
        super.renderBlock(blockEntity, matrix, vertexConsumer)
    }

}