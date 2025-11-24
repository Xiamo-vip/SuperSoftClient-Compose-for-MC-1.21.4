package com.xiamo.module

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.scene.ComposeScenePointer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.mojang.blaze3d.systems.RenderSystem
import com.xiamo.mixin.MixinMouse
import com.xiamo.utils.GlStateUtil
import com.xiamo.utils.bridge.MouseBridge
import com.xiamo.utils.bridge.MousePosition
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.lwjgl.opengl.GL33C

@OptIn(InternalComposeUiApi::class)
open class ComposeModule(name : String, description : String) : Module(name,description, Category.Render) {

    var skiaContext : DirectContext? = null
    var renderTarget : BackendRenderTarget? = null

    private var currentScale = 1f
    private var lastScaleFactor: Float = -1f

    var surface: Surface? = null
    @OptIn(InternalComposeUiApi::class)
    var composeScene : ComposeScene? = null



    private fun initCompose(width : Int,height : Int){
        composeScene =  CanvasLayersComposeScene(
            density = Density(1f),
            invalidate = {},
        ).apply {
            setContent {
                renderCompose()
            }
        }

        composeScene?.size = IntSize(width,height)
    }

    @OptIn(InternalComposeUiApi::class)
    private fun buildCompose(){
        val mc = MinecraftClient.getInstance()
        renderTarget = BackendRenderTarget.makeGL(
            mc.window.framebufferWidth,
            mc.window.framebufferHeight,
            0,8,
            mc.framebuffer.fbo,
            FramebufferFormat.Companion.GR_GL_RGBA8
        )
        surface = skiaContext?.let {
            renderTarget?.let { rt ->
                Surface.makeFromBackendRenderTarget(
                    it,
                    rt,
                    SurfaceOrigin.BOTTOM_LEFT,
                    SurfaceColorFormat.BGRA_8888,
                    ColorSpace.Companion.sRGB
                )
            }
        }

        composeScene?.let { it.size = IntSize(mc.window.framebufferWidth, mc.window.framebufferHeight) }

    }

    @OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
    override fun onRender(drawContext: DrawContext) {
        val mc = MinecraftClient.getInstance()

        if (composeScene == null) initCompose(mc.window.framebufferWidth,mc.window.framebufferHeight)

        if (lastScaleFactor != currentScale){
            composeScene?.density = Density(currentScale)
            lastScaleFactor = currentScale
        }
        drawContext.matrices.push()
        if (skiaContext == null) skiaContext = DirectContext.makeGL()
        composeScene?.size = IntSize(mc.window.width, mc.window.height)
        currentScale = mc.window.scaleFactor.toFloat()
        GlStateUtil.save()
        glStorePixel()
        skiaContext?.resetAll()

       if (renderTarget == null){
           renderTarget = BackendRenderTarget.makeGL(
               mc.window.framebufferWidth,
               mc.window.framebufferHeight,
               0,8,
               mc.framebuffer.fbo,
               FramebufferFormat.Companion.GR_GL_RGBA8
           )
       }



        if (surface ==null){
            surface = skiaContext?.let {
                renderTarget?.let { rt ->
                    Surface.makeFromBackendRenderTarget(
                        it,
                        rt,
                        SurfaceOrigin.BOTTOM_LEFT,
                        SurfaceColorFormat.BGRA_8888,
                        ColorSpace.Companion.sRGB
                    )
                }
            }
        }

        if (surface?.width != mc.window.framebufferWidth || surface?.height != mc.window.framebufferHeight) buildCompose()

        RenderSystem.enableBlend()
        surface?.let { composeScene?.render(it.canvas.asComposeCanvas(), System.nanoTime()) }
        surface?.flush()
        GlStateUtil.restore()
        RenderSystem.disableBlend()
        drawContext.matrices.pop()

//        val pointer = ComposeScenePointer(
//            id = PointerId(0),
//            position = toComposeOffset(MousePosition.xPos.toDouble(), MousePosition.yPos.toDouble()),
//            pressed = false,
//            type = PointerType.Mouse
//        )
//
//        composeScene.sendPointerEvent(
//            PointerEventType.Move,
//            listOf(pointer)
//        )


    }



    private fun toComposeOffset(mouseX: Double, mouseY: Double): Offset {
        return Offset(
            (mouseX * currentScale).toFloat(),
            (mouseY * currentScale).toFloat()
        )
    }
    private fun glStorePixel(){
        GL33C.glBindBuffer(GL33C.GL_PIXEL_UNPACK_BUFFER, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_SWAP_BYTES, GL33C.GL_FALSE)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_LSB_FIRST, GL33C.GL_FALSE)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_ROW_LENGTH, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_SKIP_ROWS, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_SKIP_PIXELS, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_ALIGNMENT, 4)
    }

    @Composable
    open fun renderCompose(){


    }

}