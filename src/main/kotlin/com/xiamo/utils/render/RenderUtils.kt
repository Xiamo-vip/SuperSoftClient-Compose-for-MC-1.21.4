package com.xiamo.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.gl.ShaderProgramKey
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

object RenderUtils {
    fun drawBox3D(matrixStack: MatrixStack, box: Box,r: Float = 0.5f, g: Float = 1.0f, b: Float = 1.0f, a: Float = 1.0f,
    ) {

        val matrix4f = matrixStack.peek().positionMatrix

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR)
        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.disableDepthTest()
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR)

        fun v(matrixStack: Matrix4f, x: Float, y: Float, z: Float) {
            buffer.vertex(matrix4f, x, y,  z).color(r, g, b, a)
        }


        v(matrix4f, minX, minY, minZ)
        v(matrix4f, maxX, minY, minZ)

       v(matrix4f, maxX, minY, minZ)
       v(matrix4f, maxX, minY, maxZ)

       v(matrix4f, maxX, minY, maxZ)
       v(matrix4f, minX, minY, maxZ)

       v(matrix4f, minX, minY, maxZ)
       v(matrix4f, minX, minY, minZ)


       v(matrix4f, minX, maxY, minZ)
       v(matrix4f, maxX, maxY, minZ)

       v(matrix4f, maxX, maxY, minZ)
       v(matrix4f, maxX, maxY, maxZ)

       v(matrix4f, maxX, maxY, maxZ)
       v(matrix4f, minX, maxY, maxZ)

       v(matrix4f, minX, maxY, maxZ)
       v(matrix4f, minX, maxY, minZ)


       v(matrix4f, minX, minY, minZ)
       v(matrix4f, minX, maxY, minZ)

       v(matrix4f, maxX, minY, minZ)
       v(matrix4f, maxX, maxY, minZ)

       v(matrix4f, maxX, minY, maxZ)
       v(matrix4f, maxX, maxY, maxZ)

       v(matrix4f, minX, minY, maxZ)
       v(matrix4f, minX, maxY, maxZ)

        val builtBuffer = buffer.end()

        BufferRenderer.drawWithGlobalProgram(builtBuffer)
        RenderSystem.enableDepthTest()
        RenderSystem.disableBlend()
    }


    fun drawFillBox3D(matrixStack: MatrixStack, box: Box, r: Float = 0.5f, g: Float = 1.0f, b: Float = 1.0f, a: Float = 1.0f) {
        val m = matrixStack.peek().positionMatrix
        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()

        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            org.lwjgl.opengl.GL11.GL_SRC_ALPHA,
            org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA,
            org.lwjgl.opengl.GL11.GL_ONE,
            org.lwjgl.opengl.GL11.GL_ZERO
        )
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.disableCull()
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR)

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        fun v(x: Float, y: Float, z: Float) = buffer.vertex(m, x, y, z).color(r, g, b, a)

        v(minX, minY, minZ); v(maxX, minY, minZ); v(maxX, minY, maxZ); v(minX, minY, maxZ)
        v(minX, maxY, minZ); v(minX, maxY, maxZ); v(maxX, maxY, maxZ); v(maxX, maxY, minZ)
        v(minX, minY, maxZ); v(maxX, minY, maxZ); v(maxX, maxY, maxZ); v(minX, maxY, maxZ)
        v(minX, minY, minZ); v(minX, maxY, minZ); v(maxX, maxY, minZ); v(maxX, minY, minZ)
        v(minX, minY, minZ); v(minX, minY, maxZ); v(minX, maxY, maxZ); v(minX, maxY, minZ)
        v(maxX, minY, minZ); v(maxX, maxY, minZ); v(maxX, maxY, maxZ); v(maxX, minY, maxZ)

        BufferRenderer.drawWithGlobalProgram(buffer.end())

        RenderSystem.enableCull()
        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        RenderSystem.disableBlend()
    }


    fun draw2DRect(matrixStack: MatrixStack, x1: Float, y1: Float, z1: Float,x2 : Float, y2 : Float,z2 : Float, color : Int) {
        val tessellator = Tessellator.getInstance()
        val buffer  = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
        val matrixStack = matrixStack.peek().positionMatrix
        buffer.vertex(matrixStack,x1, y1,z2)
        buffer.vertex(matrixStack,x1, y1,z1)
        buffer.vertex(matrixStack,x2, y1,z1)




    }




}